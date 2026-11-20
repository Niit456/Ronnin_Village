package gameDev;

import static gameDev.Gfx.*;

public final class Camera {
    private double x, y;

    private static double targetX(Player p) { return (p.x() - p.y()) * TW / 2.0 - LW / 2.0; }
    private static double targetY(Player p) { return (p.x() + p.y()) * TH / 2.0 - (LH / 2.0 + 24); }

    /** jump straight to the ronin (entering a map) */
    public void snapTo(Player p) { x = targetX(p); y = targetY(p); }

    /** glide toward the ronin */
    public void follow(Player p, double dt) {
        double k = 1 - Math.exp(-dt * 7);
        x += (targetX(p) - x) * k;
        y += (targetY(p) - y) * k;
    }

    /** makes this camera the one the painters project through */
    public void apply() { camX = (int) Math.round(x); camY = (int) Math.round(y); }
}
