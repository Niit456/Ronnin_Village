package gameDev;

import java.awt.*;
import static gameDev.Gfx.*;
import static gameDev.PixelFont.*;
import static gameDev.Painters.*;


public class ThroneRoom extends Area {

    static final double ATTACK_COOLDOWN = .42;                     // seconds between sword swings that can hurt the boss
    static final Color GOLD = Shogun.GOLD, DRED = Shogun.DRED;
    static final Zone GOLDEN_DOOR = new Zone(14.5, 2.9, 17.5, 4.6);

    private final Player player;
    private final Shogun boss;
    private double doorT, hintT, atkCd, deadT = -1;
    private boolean exitOpen, started;

    public ThroneRoom(Player player) {
        super(AreaId.THRONE_ROOM, "Throne Room", "THE SHOGUN RISES", 53);
        this.player = player;
        indoor = true;
        dirt = new Color[]{col(0x2e1c14), col(0x35211a), col(0x2a1812), col(0x3b261c)};
        pathColor = col(0x7a1e24); grass = col(0x2a1812);
        patch(14.2, 3, 3.6, 29, 0x7a1e24);
        disc(16, 10, 4.0, 0x4a1418);
        solid(0, 0, 32, 3); solid(0, 0, 3, 32);
        prop(0, 0, 32, 3, this::drawNorthWall);
        prop(0, 3, 3, 32, ThroneRoom::drawWestWall);
        for (double[] p : new double[][]{{9.5, 13.5}, {21.5, 13.5}, {9.5, 22}, {21.5, 22}}) {
            solid(p[0], p[1], 1, 1); prop(p[0] - .45, p[1] - .45, p[0] + 1.45, p[1] + 1.45, () -> drawPillar(p[0], p[1]));
        }
        for (double[] b : new double[][]{{11.5, 5.2}, {20.5, 5.2}, {5.5, 12}, {26.5, 12}, {5.5, 25}, {26.5, 25}}) {
            solid(b[0] - .3, b[1] - .3, .6, .6); prop(b[0] - .4, b[1] - .4, b[0] + .4, b[1] + .4, () -> ShogunCastle.drawBrazier(b[0], b[1]));
        }
        boss = new Shogun(this, player, this::hurtPlayer);           // the boss lives in Shogun.java
        resetFight();
    }

    @Override public void reset() { started = false; }

    private void resetFight() {
        boss.reset();
        doorT = 0; exitOpen = false; player.restoreHealth(); deadT = -1; atkCd = 0; hintT = 0;
        setGoal(null); player.setFrozen(false);
    }

    @Override public void update(double dt) {
        if (!started) { started = true; resetFight(); }
        hintT += dt;
        if (atkCd > 0) atkCd -= dt;
        if (deadT >= 0) {                                                    //the ronin has fallen
            deadT += dt; player.setFrozen(true);
            if (deadT > 2.6) respawn();
            return;
        }
        boss.update(dt);
        if (boss.isDead() && boss.deadTime() > 2.6) {                        //he has fallen: the golden door opens
            exitOpen = true; doorT = Math.min(1, doorT + dt / 1.6);
            if (doorT > .6 && goal() == null) setGoal(GOLDEN_DOOR);
        }
    }

    private void hurtPlayer(double fx, double fy) {
        if (boss.isDead()) return;
        if (player.takeHit(this, fx, fy) && player.isDead()) deadT = 0;
    }

    private void respawn() {
        player.placeAt(16, 20, 1); player.setFrozen(false);
        resetFight(); boss.skipIntro(1.0); player.setInvulnerable(1.5);
    }

    @Override public void onAttack() {
        if (!started || deadT >= 0 || atkCd > 0 || !boss.canBeHit()) return;
        atkCd = ATTACK_COOLDOWN;
        boss.damage();
    }

    //HUD
    static final String[] HEART = {" XX XX ", "XXXXXXX", "XXXXXXX", " XXXXX ", "  XXX  ", "   X   "};

    @Override public void drawHud() {
        for (int i = 0; i < Player.MAX_HP; i++) {
            int x = 8 + i * 11, y = 30;
            for (int r = 0; r < HEART.length; r++) for (int c = 0; c < 7; c++) if (HEART[r].charAt(c) == 'X') {
                g.setColor(i < player.hp() ? (r < 2 && c < 3 ? col(0xff6a6a) : col(0xd8262e)) : col(0x3a2a30));
                g.fillRect(x + c, y + r, 1, 1);
            }
        }
        if (started) boss.drawBar();
        if (hintT < 9 && !boss.isDead()) { String s = "CLICK: ATTACK   DODGE THE RED WARNINGS"; textS(s, (LW - textW(s, 1)) / 2, LH - 26, 1, new Color(240, 217, 184, (int) (255 * Math.min(1, (9 - hintT))))); }
        if (exitOpen) { String s = "THE SHOGUN HAS FALLEN - GO THROUGH THE GOLDEN DOOR"; textS(s, (LW - textW(s, 1)) / 2, LH - 26, 1, new Color(255, 225, 150, (int) (170 + 85 * Math.sin(time * 4)))); }
        if (deadT >= 0) {
            double a = Math.min(1, deadT / 1.2);
            g.setColor(new Color(0, 0, 0, (int) (200 * a))); g.fillRect(0, 0, LW, LH);
            if (deadT > .6) { String s = "YOU DIED"; textS(s, (LW - textW(s, 6)) / 2, 100, 6, new Color(200, 36, 40, (int) (255 * Math.min(1, (deadT - .6) / .6)))); }
        }
    }

    //the hall
    static void sumi(double u0, double u1, double z0, double z1, double lum) {
        Color ink = shade(col(0x2a1a10), lum);
        double um = (u0 + u1) / 2, w = (u1 - u0) * .32;
        jl(ink, um - .004, z0 + 2, um + .004, z1 - 6);
        for (int i = 0; i < 4; i++) {
            double zz = z0 + 7 + i * (z1 - z0 - 18) / 4.0, dir = i % 2 == 0 ? 1 : -1;
            jl(ink, um, zz, um + dir * w, zz + 5); jl(ink, um + dir * w, zz + 5, um + dir * w * 1.3, zz + 4);
            jq(um + dir * w - .004, um + dir * w + .004, zz + 3, zz + 7, shade(col(0x3a4a2a), lum));
        }
    }

    static void panelWall(double lum, int n, boolean withDoor) {
        double L = jL();
        jq(0, 1, 0, 76, shade(col(0x2a1a14), lum));
        jq(0, 1, 0, 14, shade(col(0x3a2418), lum));
        for (int i = 1; i < (int) (L / .5); i++) jl(shade(col(0x1c110c), lum), i * .5 / L, 0, i * .5 / L, 14);
        for (int i = 0; i < n; i++) {
            double u0 = (double) i / n + .004, u1 = (i + 1.0) / n - .004;
            jq(u0, u1, 14, 62, shade(col(0xb8903e), lum));
            jq(u0, u1, 14, 30, shade(col(0xa27c32), lum));
            sumi(u0, u1, 16, 60, lum);
        }
        for (int i = 0; i <= n; i++) jpost((double) i / n, .3, 14, 62, shade(TIMBER(), lum));
        jq(0, 1, 13, 15, shade(TIMBER(), lum)); jq(0, 1, 61, 63, shade(TIMBER(), lum));
        jq(0, 1, 63, 72, shade(col(0x5a1318), lum));
        for (double u = .3; u < L; u += 1.6) jpoly(shade(col(0xd2a540), lum), (u - .35) / L, 67.5, u / L, 70.5, (u + .35) / L, 67.5, u / L, 64.5);
        jq(0, 1, 72, 76, shade(TIMBER(), lum)); jl(shade(GOLD, lum), 0, 72.5, 1, 72.5);
    }
    static Color TIMBER() { return col(0x15110f); }

    private void drawNorthWall() {
        double lum = 1.0;
        jFace(true, 0, 0, 32, 3);
        panelWall(lum, 8, true);
        for (double u : new double[]{.30, .70}) {
            jq(u - .035, u + .035, 22, 62, shade(DRED, 1)); jq(u - .035, u + .035, 22, 24, shade(GOLD, .8));
            jpoly(GOLD, u, 36, u + .014, 42, u, 48, u - .014, 42);
        }
        //the door sealed until the Shogun falls
        double u0 = 14.5 / 32, u1 = 17.5 / 32, um = (u0 + u1) / 2;
        jq(u0 - .012, u1 + .012, 0, 56, shade(GOLD, .8));
        if (exitOpen) {
            jq(u0, u1, 0, 52, col(0xfff0b8));
            jq(u0, u1, 0, 26, col(0xffe08a)); jq(u0, u1, 0, 8, col(0xffc860));
            double w = (um - u0) * (1 - doorT);
            jq(u0, u0 + w, 0, 52, col(0x2a1c14)); jq(u1 - w, u1, 0, 52, col(0x2a1c14));
            light(16, 4, 26, 100, 0xffd890);
            int bx2 = sx(16, 3.2), by2 = sy(16, 3.2, 66) + (int) Math.round(Math.sin(time * 4) * 2);
            int[] xs = {bx2, bx2 - 6, bx2 + 6}, ys = {by2 + 9, by2 - 3, by2 - 3};
            g.setColor(col(0xf2c14e)); g.fillPolygon(xs, ys, 3); g.setColor(col(0x3a2a14)); g.drawPolygon(xs, ys, 3);
        } else {
            jq(u0, u1, 0, 52, col(0x16131a));
            jl(col(0x050407), um, 0, um, 52);
            for (double zz = 6; zz < 50; zz += 8) { jq(u0 + .006, u0 + .012, zz, zz + 1.5, shade(GOLD, .6)); jq(u1 - .012, u1 - .006, zz, zz + 1.5, shade(GOLD, .6)); }
            double pr = .6 + .4 * Math.sin(time * 3);
            jq(um - .016, um + .016, 22, 34, new Color(cl(120 + 100 * pr), 20, 24));
            jq(um - .008, um + .008, 26, 30, col(0xff9a70));
        }
        jFace(false, 0, 0, 32, 3);
        jq(0, 1, 0, 76, shade(col(0x2a1a14), .6)); jq(0, 1, 72, 76, shade(TIMBER(), .6));
        quad(col(0x0d0a09), 0, 0, 76, 32, 0, 76, 32, 3, 76, 0, 3, 76);
    }
    private static void drawWestWall() {
        jFace(false, 0, 3, 3, 29);
        panelWall(.72, 7, false);
        jFace(true, 0, 3, 3, 29);
        jq(0, 1, 0, 76, shade(col(0x2a1a14), .9));
        quad(col(0x0d0a09), 0, 3, 76, 3, 3, 76, 3, 32, 76, 0, 32, 76);
    }

    static void drawPillar(double x, double y) {
        fillq(new Color(0, 0, 0, 70), x - .3, y - .3, 0, x + 1.2, y - .3, 0, x + 1.2, y + 1.2, 0, x - .3, y + 1.2, 0);
        Color red = col(0x7a1e24), gold = GOLD;
        box(x - .1, y - .1, 1.2, 1.2, 0, 4, col(0x5d5852));                
        double[] bands = {10, 54};                                         
        double z0 = 4;                                                     
        for (double z : bands) {
            box(x, y, 1.0, 1.0, z0, z - z0, red);
            box(x - .04, y - .04, 1.08, 1.08, z, 1.4, shade(gold, .85));
            z0 = z + 1.4;
        }
        box(x, y, 1.0, 1.0, z0, 66 - z0, red);
        box(x - .25, y - .25, 1.5, 1.5, 66, 3, col(0x15110f));            
        box(x - .45, y - .45, 1.9, 1.9, 69, 3, col(0x1d1a22));
        ln(shade(red, 1.4), x + 1, y + 1, 6, x + 1, y + 1, 62);
    }

    @Override protected void paintGroundDecor() {
        Color seam = new Color(10, 6, 4, 90), butt = new Color(10, 6, 4, 70);
        for (double xx = 0; xx < 32; xx += .5) {
            ln(seam, xx, 3, 0, xx, 32, 0);
            int k = (int) (xx * 2);
            for (double yy = 3 + hash(k, 1, 4) * 3; yy < 32; yy += 3 + hash(k, (int) yy, 5) * 2) ln(butt, xx, yy, 0, xx + .5, yy, 0);
        }
        Color gold = new Color(226, 180, 70, 170);
        ln(gold, 14.2, 3, 0, 14.2, 32, 0); ln(gold, 17.8, 3, 0, 17.8, 32, 0);       
        ln(shade(gold, .6), 14.5, 3, 0, 14.5, 32, 0); ln(shade(gold, .6), 17.5, 3, 0, 17.5, 32, 0);
        ShogunCastle.ring(16, 10, 4.0, gold); ShogunCastle.ring(16, 10, 3.6, new Color(226, 180, 70, 90)); ShogunCastle.ring(16, 10, 1.2, new Color(226, 180, 70, 90));
    }
}