package gameDev;

//the fade out / fade in when the ronin walks through an exit into another map
public final class Transition {
    private enum Phase { NONE, FADE_OUT, FADE_IN }
    static final double DURATION = .4;                

    private Phase phase = Phase.NONE;
    private double timer;
    private Exit exit;

    public boolean isActive() { return phase != Phase.NONE; }
    public Exit exit() { return exit; }

    public void begin(Exit e) { exit = e; phase = Phase.FADE_OUT; timer = 0; }

    //black screen
    public double fade() {
        switch (phase) {
            case FADE_OUT: return timer / DURATION;
            case FADE_IN: return 1 - timer / DURATION;
            default: return 0;
        }
    }

    public boolean advance(double dt) {
        timer += dt;
        if (phase == Phase.FADE_OUT && timer >= DURATION) { phase = Phase.FADE_IN; timer = 0; return true; }
        if (phase == Phase.FADE_IN && timer >= DURATION) phase = Phase.NONE;
        return false;
    }
}
