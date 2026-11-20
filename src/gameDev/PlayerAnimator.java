package gameDev;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

public final class PlayerAnimator {

    public enum Anim {
        IDLE(false), WALK(false), ATTACK(true), DASH(true), HIT(true);
        final boolean oneShot;
        Anim(boolean oneShot) { this.oneShot = oneShot; }
    }

    //sheet configuration
    static final String[] IDLE_PATHS = {"res/Idle.png", "Idle.png"};
    static final String[] WALK_PATHS = {"res/Walking.png", "Walking.png"};
    static final String[] ATTACK_PATHS = {"res/Attacking.png", "Attacking.png"};
    static final String[] DASH_PATHS = {"res/Dash.png", "Dash.png"};
    static final String[] HIT_PATHS = {"res/Hit.png", "Hit.png"};

    static final int IDLE_FRAMES = 8, IDLE_W = 480, IDLE_H = 480, IDLE_FRAME_MS = 150;
    static final int WALK_FRAMES = 8, WALK_W = 96, WALK_H = 96, WALK_FRAME_MS = 80;
    static final int ATTACK_FRAMES = 10, ATTACK_W = 480, ATTACK_H = 480, ATTACK_FRAME_MS = 70;
    static final int DASH_FRAMES = 9, DASH_W = 480, DASH_H = 480, DASH_FRAME_MS = 65;
    static final int HIT_FRAMES = 4, HIT_W = 480, HIT_H = 480, HIT_FRAME_MS = 90;
    /** true if the character in that sheet faces RIGHT (flip it if he walks / strikes backwards) */
    static final boolean IDLE_FACES_RIGHT = true, WALK_FACES_RIGHT = false, ATTACK_FACES_RIGHT = true, DASH_FACES_RIGHT = true, HIT_FACES_RIGHT = true;
    static final double RUN_SPEED_FACTOR = 1.5;
    static final int[][] COMBO = {{0, 3}, {4, 6}, {7, 8}};   // first and last frame of each swing
    static final double COMBO_WINDOW_MS = 350;               // how long after a swing you can still continue the combo
    private int comboStep;
    private double comboTimeLeftMs;
    private boolean comboQueued;

    //state
    private final Map<Anim, SpriteSheet> sheets = new EnumMap<>(Anim.class);
    private Anim current;
    private int frame;
    private double timerMs;
    private boolean loaded, moving, faceRight = true;

    public PlayerAnimator() {
        SpriteSheet idle = SpriteSheet.load(IDLE_PATHS, IDLE_FRAMES, IDLE_W, IDLE_H, IDLE_FRAME_MS, IDLE_FACES_RIGHT, 0);
        double s = idle != null ? idle.scale() : 0;           
        put(Anim.IDLE, idle);
        put(Anim.WALK, SpriteSheet.load(WALK_PATHS, WALK_FRAMES, WALK_W, WALK_H, WALK_FRAME_MS, WALK_FACES_RIGHT, 0));
        put(Anim.ATTACK, SpriteSheet.load(ATTACK_PATHS, ATTACK_FRAMES, ATTACK_W, ATTACK_H, ATTACK_FRAME_MS, ATTACK_FACES_RIGHT, s));
        put(Anim.DASH, SpriteSheet.load(DASH_PATHS, DASH_FRAMES, DASH_W, DASH_H, DASH_FRAME_MS, DASH_FACES_RIGHT, s));
        put(Anim.HIT, SpriteSheet.load(HIT_PATHS, HIT_FRAMES, HIT_W, HIT_H, HIT_FRAME_MS, HIT_FACES_RIGHT, s));

        current = sheets.containsKey(Anim.IDLE) ? Anim.IDLE : Anim.WALK;
        loaded = sheets.containsKey(current);
        for (Anim a : Anim.values())
            System.out.println(a + ": " + (sheets.containsKey(a) ? sheets.get(a).length() + " frames" : "NOT FOUND"));
    }

    private void put(Anim a, SpriteSheet s) { if (s != null) sheets.put(a, s); }

    public boolean isLoaded() { return loaded; }
    public boolean isMoving() { return moving; }
    public boolean isAttacking() { return current == Anim.ATTACK; }
    public boolean isDashing() { return current == Anim.DASH; }
    public boolean isHitReacting() { return current == Anim.HIT; }
    public Anim current() { return current; }
    public int getFrame() { return frame; }
    public int getFrameCount() { return sheets.containsKey(current) ? sheets.get(current).length() : 0; }
    public BufferedImage getCurrentFrame() { return sheets.containsKey(current) ? sheets.get(current).frame(frame) : null; }
    private boolean busy() { return current.oneShot; }

    public void attack() {
        if (!canStart(Anim.ATTACK)) return;
        if (current == Anim.ATTACK) { comboQueued = true; return; }  
        if (busy()) return;                                          
        if (comboTimeLeftMs <= 0) comboStep = 0;                     
        startSwing();
    }
    private void startSwing() {
        current = Anim.ATTACK;
        frame = COMBO[comboStep][0];
        timerMs = 0;
        comboQueued = false;
        Audio.playOnce("res/AttackSound.wav", 0f);
    }
    private void finishSwing() {
        comboStep++;
        boolean more = comboStep < COMBO.length;
        if (!more) comboStep = 0;                                    
        if (comboQueued) { startSwing(); return; }                   
        comboTimeLeftMs = more ? COMBO_WINDOW_MS : 0;
        frame = 0; timerMs = 0;
        selectMovementAnimation();
    }
    public void dash() { if (canStart(Anim.DASH) && !busy()) start(Anim.DASH); }
    public void hit() {
        if (canStart(Anim.HIT) && current != Anim.HIT) { comboStep = 0; comboQueued = false; comboTimeLeftMs = 0; start(Anim.HIT); }
    }

    private boolean canStart(Anim a) { return loaded && sheets.containsKey(a); }
    private void start(Anim a) { current = a; frame = 0; timerMs = 0; }

    public void update(double dt, boolean moving, boolean running, boolean facingRight) {
        if (!loaded) return;
        this.moving = moving;
        this.faceRight = facingRight;

        if (busy()) {
            timerMs += dt * 1000.0;
            SpriteSheet s = sheets.get(current);
            int end = current == Anim.ATTACK ? COMBO[comboStep][1] + 1 : s.length();   // a swing stops at the end of its own range
            while (timerMs >= s.frameMs() && frame < end) { timerMs -= s.frameMs(); frame++; }
            if (frame >= end) {
                if (current == Anim.ATTACK) finishSwing();
                else { frame = 0; timerMs = 0; selectMovementAnimation(); }
            }
            return;
        }
        if (comboTimeLeftMs > 0) comboTimeLeftMs -= dt * 1000.0;

        selectMovementAnimation();
        SpriteSheet s = sheets.get(current);
        timerMs += dt * 1000.0 * (running ? RUN_SPEED_FACTOR : 1.0);
        while (timerMs >= s.frameMs()) { timerMs -= s.frameMs(); frame = (frame + 1) % s.length(); }
    }

    private void selectMovementAnimation() {
        Anim next = moving && sheets.containsKey(Anim.WALK) ? Anim.WALK : sheets.containsKey(Anim.IDLE) ? Anim.IDLE : Anim.WALK;
        if (!sheets.containsKey(next)) return;
        if (current != next) start(next);
    }

    public void draw(Graphics2D g, int footX, int footY) {
        if (!loaded || !sheets.containsKey(current)) return;
        sheets.get(current).draw(g, frame, footX, footY, faceRight);
    }
}
