package gameDev;

public interface Enemy {
    void update(double dt);
    boolean canBeHit();
    void damage();
    boolean isDead();
    void reset();
    void drawBar();
}
