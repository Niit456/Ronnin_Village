package gameDev;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import static gameDev.Gfx.*;

public class SkeletonArcher implements Enemy {

    public interface ShootListener { void onShoot(double fromX, double fromY, double targetX, double targetY); }
    public interface DeathListener { void onArcherDied(double x, double y); }

    private enum State { IDLE, CHASE, RETREAT, AIMING, COOLDOWN, DEAD }

    static final String[] WALK_PATHS = {"res/SkeletonArcher_Walking.png", "SkeletonArcher_Walking.png"};
    static final String[] ATK_PATHS  = {"res/SkeletonArcher_Attack.png",  "SkeletonArcher_Attack.png"};
    static final int FRAMES = 8;

    static final double MAX_HP = 3;
    static final double AGGRO_RANGE = 11.5;
    static final double SHOOT_RANGE = 8.8;
    static final double RETREAT_RANGE = 3.2;
    static final double MOVE_SPEED = 1.75;
    static final double REACH = 2.2;

    private final Area area;
    private final Player player;
    private final ShootListener shootListener;
    private final DeathListener deathListener;
    private final Prop prop;
    private final SpriteSheet walkSheet;
    private final SpriteSheet atkSheet;

    private double spawnX, spawnY;
    private double x, y, hp = MAX_HP;
    private double stT, hurtT, deadT = -1, animMs;
    private int frame, face = 1;
    private boolean firedThisCycle;
    private State st = State.IDLE;

    public SkeletonArcher(Area area, Player player, double spawnX, double spawnY,
                          ShootListener shootListener, DeathListener deathListener) {
        this.area = area;
        this.player = player;
        this.shootListener = shootListener;
        this.deathListener = deathListener;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.walkSheet = loadSheet(WALK_PATHS, 100, 0);
        double scale = this.walkSheet != null ? this.walkSheet.scale() : 0;
        this.atkSheet = loadSheet(ATK_PATHS, 95, scale);
        this.prop = new Prop(0, 0, 1, 1, this::draw);
        area.addProp(prop);
        reset();
    }

    private static SpriteSheet loadSheet(String[] paths, int frameMs, double lockScale) {
        for (String p : paths) {
            File f = new File(p);
            if (f.isFile()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        int fw = img.getWidth() / FRAMES;
                        int fh = img.getHeight();
                        return SpriteSheet.load(paths, FRAMES, fw, fh, frameMs, true, lockScale);
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
        stT = 0; hurtT = 0; deadT = -1;
        frame = 0; animMs = 0;
        firedThisCycle = false;
        syncProp();
    }

    @Override public boolean isDead() { return st == State.DEAD; }

    private double dist() { return player.distanceTo(x, y); }
    private void syncProp() { prop.centerOn(x, y, .4); }

    @Override public void update(double dt) {
        if (hurtT > 0) hurtT -= dt;

        if (st != State.DEAD) {
            face = ((x - y) - (player.x() - player.y())) >= 0 ? -1 : 1;
        }

        double d = dist(), px = player.x(), py = player.y();
        double ux = d > .001 ? (px - x) / d : 0;
        double uy = d > .001 ? (py - y) / d : 1;
        stT += dt;

        switch (st) {
            case IDLE:
                advanceWalkAnim(dt * 0.45);
                if (d < AGGRO_RANGE && !player.isDead()) {
                    st = State.CHASE;
                    stT = 0;
                }
                break;

            case CHASE:
                advanceWalkAnim(dt);
                if (d > AGGRO_RANGE * 1.35 || player.isDead()) {
                    st = State.IDLE; stT = 0; break;
                }
                if (d < RETREAT_RANGE) {
                    st = State.RETREAT; stT = 0; break;
                }
                if (d > SHOOT_RANGE) {
                    move(ux * MOVE_SPEED * dt, uy * MOVE_SPEED * dt);
                } else if (stT > 0.35) {
                    startAiming();
                }
                break;

            case RETREAT:
                advanceWalkAnim(dt * 1.1);
                if (player.isDead()) { st = State.IDLE; stT = 0; break; }
                // Back away from player if too close, then stop and shoot
                move(-ux * MOVE_SPEED * 0.9 * dt, -uy * MOVE_SPEED * 0.9 * dt);
                if (d >= RETREAT_RANGE + 1.4 || stT > 1.1) {
                    startAiming();
                }
                break;

            case AIMING:
                if (player.isDead()) { st = State.IDLE; stT = 0; break; }
                if (atkSheet != null) {
                    animMs += dt * 1000.0;
                    int nextFrame = (int) (animMs / atkSheet.frameMs());
                    if (nextFrame >= 6 && !firedThisCycle) {
                        firedThisCycle = true;
                        if (shootListener != null) shootListener.onShoot(x, y, px, py);
                    }
                    if (nextFrame >= atkSheet.length()) {
                        frame = 0;
                        st = State.COOLDOWN;
                        stT = 0;
                    } else {
                        frame = nextFrame;
                    }
                } else if (stT >= 0.65) {
                    if (shootListener != null) shootListener.onShoot(x, y, px, py);
                    st = State.COOLDOWN;
                    stT = 0;
                }
                break;

            case COOLDOWN:
                advanceWalkAnim(dt * 0.35);
                if (stT >= 0.85) {
                    st = d < RETREAT_RANGE ? State.RETREAT : State.CHASE;
                    stT = 0;
                }
                break;

            case DEAD:
                deadT += dt;
                break;
        }

        // Solid collision push-out
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

    private void startAiming() {
        st = State.AIMING;
        stT = 0;
        frame = 0;
        animMs = 0;
        firedThisCycle = false;
    }

    private void advanceWalkAnim(double dt) {
        if (walkSheet == null) return;
        animMs += dt * 1000.0;
        while (animMs >= walkSheet.frameMs()) {
            animMs -= walkSheet.frameMs();
            frame = (frame + 1) % walkSheet.length();
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
        double d = dist();
        if (d > 0.01) move((x - player.x()) / d * 0.45, (y - player.y()) / d * 0.45);
        if (hp <= 0) {
            hp = 0;
            st = State.DEAD;
            deadT = 0;
            if (deathListener != null) deathListener.onArcherDied(x, y);
        } else if (st == State.IDLE) {
            st = State.CHASE;
            stT = 0;
        }
    }

    @Override public void drawBar() {
        if (st == State.DEAD || hp >= MAX_HP) return;
        int px = sx(x, y), py = sy(x, y, 48);
        int w = 20, bx = px - w / 2, by = py;
        g.setColor(new Color(10, 6, 14, 190)); g.fillRect(bx - 1, by - 1, w + 2, 5);
        g.setColor(col(0x3a1a20)); g.fillRect(bx, by, w, 3);
        g.setColor(col(0xd45a2a)); g.fillRect(bx, by, (int) Math.round(w * hp / MAX_HP), 3);
    }

    private void draw() {
        final double vanishDuration = 0.95;
        if (st == State.DEAD && deadT > vanishDuration) return;

        int px = sx(x, y), py = sy(x, y, 0);

        // Subtle red aim line while drawing the bow
        if (st == State.AIMING && !firedThisCycle) {
            int tx = sx(player.x(), player.y()), ty = sy(player.x(), player.y(), 20);
            g.setColor(new Color(220, 60, 45, 75));
            g.drawLine(px, py - 22, tx, ty);
        }

        double p = st == State.DEAD ? Math.min(1.0, deadT / vanishDuration) : 0.0;
        double ease = p * p;

        int shRx = (int) Math.round(11 * (1.0 - ease));
        int shRy = (int) Math.round(3 * (1.0 - ease));
        if (shRx > 0 && shRy > 0) shadowOval(px, py - 1, shRx, shRy);

        Composite oc = g.getComposite();
        Shape oldClip = g.getClip();

        int sinkY = 0;
        if (st == State.DEAD) {
            g.clipRect(px - 60, py - 120, 120, 122);
            sinkY = (int) Math.round(ease * 48);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.max(0.0, 1.0 - p * 0.85)));
        }

        SpriteSheet activeSheet = (st == State.AIMING && atkSheet != null) ? atkSheet : walkSheet;
        if (activeSheet != null) {
            int fIdx = Math.min(frame, activeSheet.length() - 1);
            activeSheet.draw(g, fIdx, px, py + sinkY, face > 0);
        } else {
            g.setColor(col(0x9a8f82));
            g.fillRect(px - 6, py - 34 + sinkY, 12, 34);
        }

        if (hurtT > 0 && st != State.DEAD) {
            g.setColor(new Color(255, 255, 255, 140));
            g.fillOval(px - 10, py - 38, 20, 36);
        }

        g.setClip(oldClip);

        if (st == State.DEAD) {
            int alpha = (int) (190 * Math.sin(p * Math.PI));
            g.setColor(new Color(140, 230, 205, Math.max(0, alpha)));
            int seamW = (int) Math.round(14 * Math.sin(p * Math.PI));
            g.fillOval(px - seamW, py - 2, seamW * 2, 3);
        }

        g.setComposite(oc);
        if (st != State.DEAD) light(x, y, 18, 24, 0x9be0b8);
    }
}