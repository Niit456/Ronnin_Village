package gameDev;

import java.awt.AlphaComposite;
import java.awt.Composite;
import static gameDev.Gfx.*;


public final class Player {
	
	private int potions;
    
	public static final int MAX_HP = 5;
    public static final double HIT_INVULNERABLE = 1.1;       
    static final double WALK_SPEED = 80, RUN_SPEED = 130;    
    static final double DASH_DURATION = 0.20, DASH_SPEED = 700;
    public static final double DASH_COOLDOWN = 1.0;          
    static final double START_X = 4.5, START_Y = 16.2;
    static final String DASH_SOUND = "res/DashSound.wav", HIT_SOUND = "res/HitSound.wav", WALK_SOUND = "res/WalkingSound.wav";

    private final PlayerAnimator anim = new PlayerAnimator();
    private final Audio.Loop footsteps = new Audio.Loop(WALK_SOUND, -6f);

    private double dashVx, dashVy;                 
    private double x = START_X, y = START_Y, walk, dashTimer, dashCooldown, invulnerable;
    private int face = 1, hp = MAX_HP;
    private boolean moving, running, dashing, frozen;

    public double x() { return x; }
    public double y() { return y; }
    public int face() { return face; }
    public boolean isMoving() { return moving; }
    public boolean isFrozen() { return frozen; }
    public boolean isDashing() { return dashing; }
    public boolean isRunning() { return running && moving && !dashing; }
    public double dashCharge() { return 1 - Math.max(0, Math.min(1, dashCooldown / DASH_COOLDOWN)); }
    public boolean isAnimationLoaded() { return anim.isLoaded(); }
    public boolean isHitReacting() { return anim.isHitReacting(); }
    public int hp() { return hp; }
    public boolean isDead() { return hp <= 0; }
    public boolean isInvulnerable() { return invulnerable > 0; }
    public double distanceTo(double ox, double oy) { return Math.hypot(x - ox, y - oy); }
    public PlayerAnimator animator() { return anim; }

    public void reset() {
        x = START_X; y = START_Y; face = 1; walk = 0; moving = running = dashing = frozen = false; dashTimer = 0; dashCooldown = 0;
        restoreHealth();
    }
    public void restoreHealth() { hp = MAX_HP; invulnerable = 0; }
    public void heal(int amount) { hp = Math.min(MAX_HP, hp + amount); }
    public void addPotion() { potions++; }
    public int potions() { return potions; }
    public void setInvulnerable(double seconds) { invulnerable = seconds; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }
    public void placeAt(double x, double y, int face) { this.x = x; this.y = y; this.face = face; }
    public void nudgeTo(double x, double y) { this.x = x; this.y = y; }
    public void halt() { moving = false; dashing = false; }
    public void stopMoving() { moving = false; }
    public void silence() { footsteps.pause(); }
    public void attack() { anim.attack(); }

    public void dash(Controls controls) {
        if (dashing || dashCooldown > 0 || frozen || anim.isHitReacting() || !anim.isLoaded()) return;
        double vx = controls.dirX(), vy = controls.dirY();
        if (vx == 0 && vy == 0) vx = face;
        if (vx != 0 && vy != 0) vy *= .5;
        double len = Math.hypot(vx, vy);
        dashVx = vx / len; dashVy = vy / len;
        if (vx > 0) face = 1; else if (vx < 0) face = -1;         
        anim.dash();
        dashing = true;
        dashTimer = DASH_DURATION;
        dashCooldown = DASH_COOLDOWN;
        Audio.playOnce(DASH_SOUND, 0f);
    }

    public boolean takeHit(Area area, double fromX, double fromY) {
        if (invulnerable > 0 || dashing) return false;      
        hp--; invulnerable = HIT_INVULNERABLE;
        anim.hit();
        Audio.playOnce(HIT_SOUND, 0f);
        double d = Math.hypot(x - fromX, y - fromY);
        if (d < .01) { fromX = x; fromY = y - 1; d = 1; }
        for (double k = 1.4; k > .2; k -= .3) {
            double nx = x + (x - fromX) / d * k, ny = y + (y - fromY) / d * k;
            if (!area.blocked(nx, ny)) { x = nx; y = ny; break; }
        }
        if (hp <= 0) { frozen = true; moving = false; }
        return true;
    }

    public void move(double dt, Controls controls, Area area) {
        double vx = controls.dirX(), vy = controls.dirY();
        if (frozen || anim.isHitReacting()) { vx = 0; vy = 0; }
        moving = vx != 0 || vy != 0;
        running = controls.running();

        if (dashing && anim.isHitReacting()) dashing = false;      
        if (dashing) {
            dashTimer -= dt;
            double sx = dashVx * DASH_SPEED, sy = dashVy * DASH_SPEED;
            double dx = (sx / 16.0 + sy / 8.0) / 2 * dt, dy = (sy / 8.0 - sx / 16.0) / 2 * dt;
            if (!area.blocked(x + dx, y)) x += dx;
            if (!area.blocked(x, y + dy)) y += dy;
            if (dashTimer <= 0) dashing = false;
        } else if (moving) {
            if (vx != 0 && vy != 0) vy *= .5;
            double len = Math.hypot(vx, vy);
            vx /= len; vy /= len;
            double sp = running ? RUN_SPEED : WALK_SPEED;
            vx *= sp; vy *= sp;
            double dx = (vx / 16.0 + vy / 8.0) / 2 * dt, dy = (vy / 8.0 - vx / 16.0) / 2 * dt;
            if (!area.blocked(x + dx, y)) x += dx;
            if (!area.blocked(x, y + dy)) y += dy;
            if (vx > 1) face = 1; else if (vx < -1) face = -1;
            walk += dt * (running ? 1.5 : 1);
        }
    }

    public void update(double dt) {
        if (invulnerable > 0) invulnerable -= dt;
        if (dashCooldown > 0) dashCooldown -= dt;
        anim.update(dt, moving && !dashing, moving && !dashing && running, face > 0);

        boolean walking = moving && !dashing && !frozen && !anim.isHitReacting() && !anim.isAttacking();
        if (walking) footsteps.resume(); else footsteps.pause();     
    }

    public void draw() {
        int fx = sx(x, y), fy = sy(x, y, 0);
        shadowOval(fx, fy - 1, 11, 3);
        if (anim.isLoaded()) anim.draw(g, fx, fy);
        else FallbackSprite.draw(g, fx, fy, face > 0, moving, walk);    
        light(x, y, 18, 34, 0xdfe3ff);
    }

    public void drawFlickering() {
        float alpha = (invulnerable > 0 && ((int) (time * 18) % 2 == 0)) ? .35f : 1f;
        Composite oc = g.getComposite();
        if (alpha < 1f) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        draw();
        g.setComposite(oc);
    }

    public Prop asProp() { return new Prop(x - .2, y - .2, x + .2, y + .2, this::drawFlickering); }
}