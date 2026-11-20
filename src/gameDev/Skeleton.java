package gameDev;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import static gameDev.Gfx.*;

public class Skeleton implements Enemy {

    public interface AttackListener { void onPlayerHit(double fromX, double fromY); }
    
    public interface DeathListener { void onSkeletonDied(double x, double y); }
    private DeathListener deathListener;

    public void setDeathListener(DeathListener l) { this.deathListener = l; }

    private enum State { IDLE, CHASE, WINDUP, RECOVER, DEAD }

    static final String[] WALK_PATHS = {"res/Skeleton_Walking.png", "Skeleton_Walking.png"};
    static final int WALK_FRAMES = 8;
    static final double MAX_HP = 4;
    static final double AGGRO_RANGE = 7.5;
    static final double ATTACK_RANGE = 1.45;
    static final double CHASE_SPEED = 2.1;
    static final double REACH = 2.2;

    private final Area area;
    private final Player player;
    private final AttackListener listener;
    private final Prop prop;
    private final SpriteSheet sheet;

    private double spawnX, spawnY;
    private double x, y, hp = MAX_HP;
    private double stT, hurtT, slashT, deadT = -1, animMs;
    private int frame, face = 1;
    private State st = State.IDLE;

    public Skeleton(Area area, Player player, double spawnX, double spawnY, AttackListener listener) {
        this.area = area;
        this.player = player;
        this.listener = listener;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.sheet = loadWalkSheet();
        this.prop = new Prop(0, 0, 1, 1, this::draw);
        area.addProp(prop);
        reset();
    }

    private static SpriteSheet loadWalkSheet() {
        for (String p : WALK_PATHS) {
            File f = new File(p);
            if (f.isFile()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        int fw = img.getWidth() / WALK_FRAMES;
                        int fh = img.getHeight();
                        return SpriteSheet.load(WALK_PATHS, WALK_FRAMES, fw, fh, 95, true, 0);
                    }
                } catch (Exception ignored) { }
            }
        }
        return null;
    }

    public void setSpawn(double sx, double sy) {
        this.spawnX = sx;
        this.spawnY = sy;
    }

    @Override public void reset() {
        x = spawnX; y = spawnY;
        hp = MAX_HP;
        st = State.IDLE;
        stT = 0; hurtT = 0; slashT = 0; deadT = -1;
        frame = 0; animMs = 0;
        syncProp();
    }

    @Override public boolean isDead() { return st == State.DEAD; }

    private double dist() { return player.distanceTo(x, y); }
    private void syncProp() { prop.centerOn(x, y, .4); }

    @Override public void update(double dt) {
        if (hurtT > 0) hurtT -= dt;
        if (slashT > 0) slashT -= dt;

        if (st != State.DEAD) {
            face = ((x - y) - (player.x() - player.y())) >= 0 ? -1 : 1;
        }

        double d = dist(), px = player.x(), py = player.y();
        double ux = d > .001 ? (px - x) / d : 0;
        double uy = d > .001 ? (py - y) / d : 1;
        stT += dt;

        switch (st) {
            case IDLE:
                advanceAnim(dt * 0.45);
                if (d < AGGRO_RANGE && !player.isDead()) { st = State.CHASE; stT = 0; }
                break;

            case CHASE:
                advanceAnim(dt);
                if (d > AGGRO_RANGE * 1.4 || player.isDead()) { st = State.IDLE; stT = 0; break; }
                if (d > ATTACK_RANGE * 0.85) {
                    move(ux * CHASE_SPEED * dt, uy * CHASE_SPEED * dt);
                }
                if (d <= ATTACK_RANGE && stT > 0.35) {
                    st = State.WINDUP; stT = 0;
                }
                break;

            case WINDUP:
                if (stT >= 0.45) {
                    slashT = 0.22;
                    if (dist() <= ATTACK_RANGE + 0.35) listener.onPlayerHit(x, y);
                    st = State.RECOVER; stT = 0;
                }
                break;

            case RECOVER:
                if (stT >= 0.65) { st = State.CHASE; stT = 0; }
                break;

            case DEAD:
                deadT += dt;
                break;
        }

        // Solid collision push-out so player can't walk straight through the skeleton
        if (st != State.DEAD) {
            double curD = dist();
            if (curD < .65 && curD > .001) {
                double nx = player.x() + (player.x() - x) / curD * (.65 - curD);
                double ny = player.y() + (player.y() - y) / curD * (.65 - curD);
                if (!area.blocked(nx, ny)) player.nudgeTo(nx, ny);
            }
        }
        syncProp();
    }

    private void advanceAnim(double dt) {
        if (sheet == null) return;
        animMs += dt * 1000.0;
        while (animMs >= sheet.frameMs()) {
            animMs -= sheet.frameMs();
            frame = (frame + 1) % sheet.length();
        }
    }

    private void move(double dx, double dy) {
        if (!area.blocked(x + dx, y)) x += dx;
        if (!area.blocked(x, y + dy)) y += dy;
    }

    @Override public boolean canBeHit() {
        if (st == State.DEAD) return false;
        double sdx = (x - y) - (player.x() - player.y());
        return dist() < REACH && sdx * player.face() > -.4;
    }

    @Override public void damage() {
        if (st == State.DEAD) return;
        hp -= 1;
        hurtT = 0.18;
        // Knockback away from player
        double d = dist();
        if (d > 0.01) move((x - player.x()) / d * 0.45, (y - player.y()) / d * 0.45);
        if (hp <= 0) {
            hp = 0;
            st = State.DEAD;
            deadT = 0;
        } else if (st == State.IDLE) {
            st = State.CHASE;
            stT = 0;
        }
        
        if (hp <= 0) {
            hp = 0;
            st = State.DEAD;
            deadT = 0;
            if (deathListener != null) deathListener.onSkeletonDied(x, y);
        }
    }

    @Override public void drawBar() {
        if (st == State.DEAD || hp >= MAX_HP) return;
        int px = sx(x, y), py = sy(x, y, 48);
        int w = 22, bx = px - w / 2, by = py;
        g.setColor(new Color(10, 6, 14, 190)); g.fillRect(bx - 1, by - 1, w + 2, 5);
        g.setColor(col(0x3a1a20)); g.fillRect(bx, by, w, 3);
        g.setColor(col(0xc42a30)); g.fillRect(bx, by, (int) Math.round(w * hp / MAX_HP), 3);
    }

    private void draw() {
        final double vanishDuration = 0.95;
        if (st == State.DEAD && deadT > vanishDuration) return;

        int px = sx(x, y), py = sy(x, y, 0);

        // Red telegraph circle before claw strike
        if (st == State.WINDUP) {
            double pulse = .5 + .5 * Math.sin(time * 24);
            Shogun.groundCircle(x, y, ATTACK_RANGE, new Color(220, 45, 35, (int) (45 + 45 * pulse)));
        }

        double p = st == State.DEAD ? Math.min(1.0, deadT / vanishDuration) : 0.0;
        double ease = p * p; // accelerates slightly as it sinks into the ground

        // Shrink shadow as the skeleton disappears
        int shRx = (int) Math.round(11 * (1.0 - ease));
        int shRy = (int) Math.round(3 * (1.0 - ease));
        if (shRx > 0 && shRy > 0) shadowOval(px, py - 1, shRx, shRy);

        Composite oc = g.getComposite();
        Shape oldClip = g.getClip();

        int sinkY = 0;
        if (st == State.DEAD) {
            // Clip everything below the ground line (py + 2) so it sinks into the floor
            g.clipRect(px - 60, py - 120, 120, 122);
            sinkY = (int) Math.round(ease * 48);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.max(0.0, 1.0 - p * 0.85)));
        }

        if (sheet != null) {
            sheet.draw(g, frame, px, py + sinkY, face > 0);
        } else {
            g.setColor(col(0xc8c2b0));
            g.fillRect(px - 6, py - 34 + sinkY, 12, 34);
        }

        // Flash overlay when damaged
        if (hurtT > 0 && st != State.DEAD) {
            g.setColor(new Color(255, 255, 255, 140));
            g.fillOval(px - 10, py - 38, 20, 36);
        }

        // Claw slash arc on attack
        if (slashT > 0 && st != State.DEAD) {
            Stroke os = g.getStroke();
            g.setStroke(new BasicStroke(2f));
            g.setColor(new Color(220, 240, 255, (int) (220 * (slashT / 0.22))));
            g.drawArc(px - 22, py - 32, 44, 26, face > 0 ? -40 : 140, 80);
            g.setStroke(os);
        }

        g.setClip(oldClip);

        // Ground seam & rising soul/dust wisps while sinking
        if (st == State.DEAD) {
            int alpha = (int) (190 * Math.sin(p * Math.PI));
            g.setColor(new Color(140, 230, 205, Math.max(0, alpha)));
            int seamW = (int) Math.round(14 * Math.sin(p * Math.PI));
            g.fillOval(px - seamW, py - 2, seamW * 2, 3);

            for (int i = 0; i < 5; i++) {
                double ox = Math.sin(i * 2.1 + deadT * 5.0) * (4 + i * 2);
                double oy = -deadT * (18 + i * 4) - (i % 3) * 3;
                g.fillRect((int) Math.round(px + ox), (int) Math.round(py + oy), 1, 2);
            }
        }

        g.setComposite(oc);
        if (st != State.DEAD) light(x, y, 20, 26, 0x7be0c8);
    }
}