package gameDev;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import static gameDev.Gfx.*;
import static gameDev.PixelFont.*;
import static gameDev.Shogun.State.*;


public class Shogun implements Enemy {

    public interface AttackListener { void onPlayerHit(double fromX, double fromY); }

    //boss behaviour
    public enum State { INTRO, CHASE, WIND_SLASH, WIND_CHARGE, CHARGE, WIND_FIRE, RECOVER, DEAD }

    //a fireball flying across the room
    private static final class Fireball {
        double x, y; final double vx, vy; double life;
        final Prop prop;
        Fireball(double x, double y, double vx, double vy, double life) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.life = life;
            prop = new Prop(x - .3, y - .3, x + .3, y + .3, () -> drawFireball(this));
        }
        void step(double dt) { x += vx * dt; y += vy * dt; life -= dt; prop.centerOn(x, y, .3); }
    }

    static final double BOSS_HP = 20;                                 // hits needed (1 damage per hit that lands)
    static final double CHASE_SPEED = 2.0, CHASE_SPEED_2 = 2.7;       // tiles per second (phase 1 / phase 2)
    static final double SLASH_WIND = .75, SLASH_WIND_2 = .55;         // warning time before the slash lands
    static final double REACH = 2.5;                                  // how far the player's sword reaches (tiles)

    //palette
    static final Color GOLD = col(0xe3b341), RED = col(0x8a1f24), DRED = col(0x5a1318), NAVY = col(0x1c2236), STEEL = col(0x7c8290), HELM = col(0x1d1a22), BLADE = col(0xd6dde8);

    //state
    private final Area area;
    private final Player player;
    private final AttackListener listener;
    private final Prop prop;
    private final List<Fireball> fires = new ArrayList<>();
    private double x, y, hp = BOSS_HP, stT, chaseT, hurt, strikeT, sparkT, deadT = -1, cdx, cdy, chargeLeft;
    private State st = INTRO;
    private int face = 1;
    private boolean p2, chargeHit;

    public Shogun(Area area, Player player, AttackListener listener) {
        this.area = area; this.player = player; this.listener = listener;
        prop = new Prop(0, 0, 1, 1, this::draw);
        area.addProp(prop);
        reset();
    }

    @Override public void reset() {
        x = 16; y = 8.6; hp = BOSS_HP; st = INTRO; stT = 0; chaseT = 0; p2 = false; hurt = 0; strikeT = 0; sparkT = 0; deadT = -1;
        clearFireballs();
        syncProp();
    }

    @Override public boolean isDead() { return st == DEAD; }
    public boolean isIntro() { return st == INTRO; }
    /** seconds since he fell (-1 while alive) */
    public double deadTime() { return deadT; }
    public void skipIntro(double left) { if (st == INTRO) stT = 2.2 - left; }

    private double dist() { return player.distanceTo(x, y); }
    private void syncProp() { prop.centerOn(x, y, .5); }
    private void clearFireballs() { for (Fireball f : fires) area.removeProp(f.prop); fires.clear(); }

    //the fight
    @Override public void update(double dt) {
        if (hurt > 0) hurt -= dt;
        if (strikeT > 0) strikeT -= dt;
        if (sparkT > 0) sparkT -= dt;
        face = ((x - y) - (player.x() - player.y())) >= 0 ? -1 : 1;         //he looks toward the ronin (screen left/right)
        brain(dt);
        for (int i = fires.size() - 1; i >= 0; i--) {                       //fireballs
            Fireball f = fires.get(i);
            f.step(dt);
            boolean gone = f.life <= 0 || area.blocked(f.x, f.y);
            if (!gone && player.distanceTo(f.x, f.y) < .6) { listener.onPlayerHit(f.x - f.vx * .2, f.y - f.vy * .2); gone = true; }
            if (gone) { area.removeProp(f.prop); fires.remove(i); }
        }
        if (st != DEAD) {                                                    //he is a solid body: you can't walk through him
            double d = dist(), px = player.x(), py = player.y();
            if (d < .8 && d > .001) { double nx = px + (px - x) / d * (.8 - d), ny = py + (py - y) / d * (.8 - d); if (!area.blocked(nx, ny)) player.nudgeTo(nx, ny); }
        }
        syncProp();
    }

    private void move(double dx, double dy) {
        if (!area.blocked(x + dx, y)) x += dx;
        if (!area.blocked(x, y + dy)) y += dy;
    }

    private void brain(double dt) {
        double d = dist(), px = player.x(), py = player.y();
        double ux = d > .001 ? (px - x) / d : 0, uy = d > .001 ? (py - y) / d : 1;
        stT += dt;
        switch (st) {
            case INTRO:
                if (stT > 2.2) { st = CHASE; stT = 0; chaseT = 0; }
                break;
            case CHASE: {
                chaseT += dt;
                double sp = p2 ? CHASE_SPEED_2 : CHASE_SPEED;
                if (d > 1.7) move(ux * sp * dt, uy * sp * dt);
                if (d < 2.0 && chaseT > .35) { begin(WIND_SLASH); break; }
                if (chaseT > (p2 ? .9 : 1.4)) {
                    double r = Math.random();
                    if (d > 3.5 && r < .45) begin(WIND_CHARGE);
                    else if (r < (p2 ? .75 : .45)) begin(WIND_FIRE);
                    else if (d < 3.2) begin(WIND_SLASH);
                    else chaseT = .4;
                }
                break;
            }
            case WIND_SLASH:
                if (stT >= (p2 ? SLASH_WIND_2 : SLASH_WIND)) {
                    strikeT = .25;
                    if (dist() < 2.35) listener.onPlayerHit(x, y);
                    begin(RECOVER); chargeLeft = p2 ? .55 : .8;
                }
                break;
            case WIND_CHARGE:
                if (stT >= (p2 ? .65 : .9)) { st = CHARGE; stT = 0; chargeLeft = .5; chargeHit = false; }
                break;
            case CHARGE: {
                double sp = 11, ox = x, oy = y;
                move(cdx * sp * dt, cdy * sp * dt);
                if (!chargeHit && dist() < 1.15) { chargeHit = true; listener.onPlayerHit(x - cdx, y - cdy); }
                chargeLeft -= dt;
                if (chargeLeft <= 0 || (Math.abs(x - ox) + Math.abs(y - oy) < sp * dt * .3)) { begin(RECOVER); chargeLeft = 1.0; }
                break;
            }
            case WIND_FIRE:
                if (stT >= .8) {
                    int n = p2 ? 5 : 3;
                    double base = Math.atan2(py - y, px - x);
                    for (int i = 0; i < n; i++) {
                        double ang = base + (i - (n - 1) / 2.0) * .26;
                        Fireball f = new Fireball(x + Math.cos(ang) * .8, y + Math.sin(ang) * .8, Math.cos(ang) * 5.4, Math.sin(ang) * 5.4, 3.5);
                        fires.add(f); area.addProp(f.prop);
                    }
                    begin(RECOVER); chargeLeft = .9;
                }
                break;
            case RECOVER:
                if (stT >= chargeLeft) { st = CHASE; stT = 0; chaseT = 0; }
                break;
            case DEAD:
                deadT += dt;
                break;
        }
    }

    private void begin(State s) {
        st = s; stT = 0;
        if (s == WIND_CHARGE) { double d = dist(); cdx = d > .001 ? (player.x() - x) / d : 0; cdy = d > .001 ? (player.y() - y) / d : 1; }
    }

    //true if a sword swing from the ronin (facing the way he does) would land right now
    @Override public boolean canBeHit() {
        if (st == DEAD || st == INTRO) return false;
        double sdx = (x - y) - (player.x() - player.y());                   // > 0 = he is to your screen-right
        return dist() < REACH && sdx * player.face() > -.4;
    }

    //one hit from the player
    @Override public void damage() {
        if (st == DEAD) return;
        hp -= 1; hurt = .15; sparkT = .18;
        if (hp <= 0) {
            hp = 0; st = DEAD; deadT = 0;
            clearFireballs();
        } else if (!p2 && hp <= BOSS_HP / 2) { p2 = true; strikeT = .3; hurt = .35; }
    }

    //health bar (draw from the area's HUD)
    @Override public void drawBar() {
        if (st == DEAD) return;
        int w = 200, bx = (LW - w) / 2, by = 15;
        textS("THE SHOGUN", (LW - textW("THE SHOGUN", 1)) / 2, 5, 1, col(0xf0d9b8));
        g.setColor(new Color(10, 6, 14, 200)); g.fillRect(bx - 2, by - 2, w + 4, 9);
        g.setColor(col(0x3a1a20)); g.fillRect(bx, by, w, 5);
        g.setColor(p2 ? col(0xff5a20) : col(0xc42a30)); g.fillRect(bx, by, (int) Math.round(w * hp / BOSS_HP), 5);
        g.setColor(col(0xe8c870)); g.fillRect(bx + w / 2, by - 2, 1, 9);
        g.setColor(col(0x6a4a54)); g.drawRect(bx - 2, by - 2, w + 3, 8);
    }

    //drawing: the Shogun
    Color fc(Color c) { return hurt > 0 ? mix(c, Color.WHITE, .75) : c; }
    void rc(int px, int py, int f, int dx, int dy, int w, int h, Color c) {
        g.setColor(fc(c)); g.fillRect(f > 0 ? px + dx : px - dx - w, py + dy, w, h);
    }
    void poly(int px, int py, int f, Color c, int... xy) {
        int n = xy.length / 2; int[] xs = new int[n], ys = new int[n];
        for (int i = 0; i < n; i++) { xs[i] = px + xy[2 * i] * f; ys[i] = py + xy[2 * i + 1]; }
        g.setColor(fc(c)); g.fillPolygon(xs, ys, n);
    }
    static void groundCircle(double cx, double cy, double r, Color c) {
        double[] p = new double[36 * 3];
        for (int i = 0; i < 36; i++) { double a = i * Math.PI * 2 / 36; p[3 * i] = cx + Math.cos(a) * r; p[3 * i + 1] = cy + Math.sin(a) * r; p[3 * i + 2] = 0; }
        fillq(c, p);
    }

    private void draw() {
        int px = sx(x, y), py = sy(x, y, 0);
        int f = face;
        double pulse = .5 + .5 * Math.sin(time * 22);
        // warnings painted on the floor
        if (st == WIND_SLASH) groundCircle(x, y, 2.35, new Color(230, 40, 30, (int) (55 + 55 * pulse)));
        if (st == WIND_CHARGE) {
            double nx = -cdy * .75, ny = cdx * .75, L = 7;
            fillq(new Color(230, 40, 30, (int) (55 + 55 * pulse)), x + nx, y + ny, 0, x + cdx * L + nx, y + cdy * L + ny, 0, x + cdx * L - nx, y + cdy * L - ny, 0, x - nx, y - ny, 0);
        }
        shadowOval(px, py - 1, 17, 5);
        Composite oc = g.getComposite();
        AffineTransform at = g.getTransform();
        if (st == DEAD) {                                                    // he kneels and fades away
            double k = Math.min(1, deadT / 1.4), fade = Math.max(0, 1 - Math.max(0, deadT - 1.2) / 1.4);
            g.translate(px, py); g.scale(1, 1 - .32 * k); g.translate(-px, -py);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) fade));
        }
        boolean moving = st == CHASE || st == CHARGE;
        int stride = moving ? (int) Math.round(Math.sin(time * 9) * 2) : 0;
        int bob = (int) Math.round(Math.sin(time * (moving ? 8 : 2)) * (moving ? 1.2 : .8));
        py -= Math.max(0, bob);
        // cloak behind
        poly(px, py, 1, DRED, -13, -46, 13, -46, 17, -4, -17, -4);
        poly(px, py, 1, shade(DRED, .7), -4, -46, 4, -46, 6, -4, -6, -4);
        // legs
        rc(px, py, 1, -10 + stride, -20, 8, 17, NAVY); rc(px, py, 1, 2 - stride, -20, 8, 17, NAVY);
        rc(px, py, 1, -11 + stride, -4, 10, 4, HELM); rc(px, py, 1, 1 - stride, -4, 10, 4, HELM);
        // skirt plates + torso
        rc(px, py, 1, -12, -28, 24, 9, RED);
        rc(px, py, 1, -12, -24, 24, 1, GOLD);
        for (int k = 0; k < 6; k++) rc(px, py, 1, -12 + k * 4 + 3, -28, 1, 9, DRED);
        rc(px, py, 1, -11, -46, 22, 19, RED);
        for (int k = 0; k < 4; k++) rc(px, py, 1, -11, -44 + k * 5, 22, 1, shade(GOLD, .75));
        rc(px, py, 1, -1, -46, 2, 19, DRED); rc(px, py, 1, -9, -45, 4, 6, shade(RED, 1.3));
        g.setColor(fc(GOLD)); g.fillOval(px - 3, py - 40, 6, 6); g.setColor(fc(DRED)); g.fillOval(px - 1, py - 38, 2, 2);
        // shoulders
        rc(px, py, 1, -17, -48, 8, 13, DRED); rc(px, py, 1, 9, -48, 8, 13, DRED);
        rc(px, py, 1, -17, -48, 8, 1, GOLD); rc(px, py, 1, 9, -48, 8, 1, GOLD);
        // head: steel mask, glowing eyes, dark helmet with golden horns
        rc(px, py, 1, -3, -49, 6, 3, HELM);
        rc(px, py, 1, -5, -58, 10, 10, STEEL);
        Color eye = p2 ? col(0xff6a30) : col(0xe03030);
        g.setColor(eye); g.fillRect(px - 3, py - 54, 2, 2); g.fillRect(px + 1, py - 54, 2, 2);
        rc(px, py, 1, -3, -50, 6, 1, HELM);
        rc(px, py, 1, -7, -62, 14, 6, HELM); rc(px, py, 1, -9, -58, 18, 3, HELM);
        poly(px, py, 1, GOLD, -4, -62, -16, -66, -20, -78, -14, -72, -5, -65);
        poly(px, py, 1, GOLD, 4, -62, 16, -66, 20, -78, 14, -72, 5, -65);
        rc(px, py, 1, -1, -70, 2, 8, GOLD);
        // sword
        double ang, len = 34;
        switch (st) {
            case WIND_SLASH: ang = -100 - 10 * Math.sin(time * 14); break;
            case WIND_CHARGE: ang = 172; len = 36; break;
            case CHARGE: ang = 0; len = 38; break;
            case WIND_FIRE: ang = -90; break;
            case RECOVER: ang = 78; break;
            case DEAD: ang = 80; break;
            default: ang = 58;
        }
        if (strikeT > 0) ang = -100 + 150 * (1 - strikeT / .25);
        double rad = Math.toRadians(ang);
        int hx = px + f * 9, hy = py - 34, tx = hx + (int) Math.round(Math.cos(rad) * len * f), ty = hy + (int) Math.round(Math.sin(rad) * len);
        Stroke os = g.getStroke();
        g.setStroke(new BasicStroke(3f)); g.setColor(fc(DRED)); g.drawLine(px + f * 8, py - 42, hx, hy);
        g.setStroke(new BasicStroke(2f)); g.setColor(fc(BLADE)); g.drawLine(hx, hy, tx, ty);
        g.setStroke(new BasicStroke(1f)); g.setColor(Color.WHITE); g.drawLine(hx, hy - 1, tx, ty - 1);
        g.setStroke(os);
        g.setColor(fc(GOLD)); g.fillRect(hx - 2, hy - 1, 4, 3);
        if (strikeT > 0) {                                                  // slash arc
            g.setStroke(new BasicStroke(3f)); g.setColor(new Color(255, 235, 210, (int) Math.min(255, 230 * strikeT / .25)));
            g.drawArc(px - 40, py - 44, 80, 44, f > 0 ? -30 : 120, 90 + 0);
            g.setStroke(os);
        }
        if (sparkT > 0) {
            g.setColor(new Color(255, 230, 150, 230));
            for (int i = 0; i < 8; i++) { double a = i * Math.PI / 4 + .3; int r0 = 6, r1 = 6 + (int) (14 * (1 - sparkT / .18)) + 4; g.drawLine(px + (int) (Math.cos(a) * r0), py - 34 + (int) (Math.sin(a) * r0), px + (int) (Math.cos(a) * r1), py - 34 + (int) (Math.sin(a) * r1)); }
        }
        g.setTransform(at); g.setComposite(oc);
        if (st == DEAD && deadT < 2.6) for (int i = 0; i < 14; i++) {      // embers rising from the fallen warlord
            double a = hash(i, 3, 9) * 6.28, r = hash(i, 4, 9) * 16, u = (deadT * 30 + i * 9) % 60;
            g.setColor(new Color(255, 190 + (i * 9) % 60, 80, 200)); g.fillRect(px + (int) (Math.cos(a) * r), py - 8 - (int) u, 2, 2);
        }
        if (st != DEAD) light(x, y, 36, p2 ? 70 : 50, p2 ? 0xff5a20 : 0xc02a2a);
        if (st == WIND_FIRE) light(x, y, 40, 60, 0xff8a30);
    }

    static void drawFireball(Fireball f) {
        int x = sx(f.x, f.y), y = sy(f.x, f.y, 14);
        double t = time * 20;
        g.setColor(new Color(230, 60, 20, 90)); g.fillOval(x - 8, y - 8, 16, 16);
        g.setColor(new Color(255, 130, 30, 200)); g.fillOval(x - 5, y - 5, 10, 10);
        g.setColor(new Color(255, 230, 120)); g.fillOval(x - 3, y - 3, 6, 6);
        for (int i = 1; i < 5; i++) { double a = Math.atan2(f.vy, f.vx); int tx = x - (int) (Math.cos(a) * i * 4) * 1, ty = y - (int) (Math.sin(a) * i * 2); g.setColor(new Color(255, 140 + i * 10, 40, 160 - i * 30)); g.fillRect(tx + (int) Math.round(Math.sin(t + i) * 1), ty, 3 - i / 3, 3 - i / 3); }
        light(f.x, f.y, 14, 44, 0xff7a30);
        shadowOval(sx(f.x, f.y), sy(f.x, f.y, 0), 5, 2);
    }
}