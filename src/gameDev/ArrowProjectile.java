package gameDev;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import static gameDev.Gfx.*;

public class ArrowProjectile {

    private static final String[] PATHS = {"res/Arrow.png", "Arrow.png"};
    private static final BufferedImage SPRITE = loadSprite();
    private static final double SPEED = 7.2;
    private static final double MAX_LIFE = 2.4;
    private static final double HIT_RADIUS = 0.52;

    private double x, y;
    private final double vx, vy;
    private double life;
    private boolean active = true;

    public ArrowProjectile(Area area, double startX, double startY, double targetX, double targetY) {
        this.x = startX;
        this.y = startY;
        double dx = targetX - startX, dy = targetY - startY;
        double len = Math.hypot(dx, dy);
        this.vx = len > 0.001 ? (dx / len) * SPEED : SPEED;
        this.vy = len > 0.001 ? (dy / len) * SPEED : 0;
        area.addProp(new Prop(startX - 0.25, startY - 0.25, startX + 0.25, startY + 0.25, this::draw));
    }

    private static BufferedImage loadSprite() {
        for (String p : PATHS) {
            File f = new File(p);
            if (f.isFile()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) return img;
                } catch (Exception ignored) { }
            }
        }
        return null;
    }

    public boolean isActive() { return active; }
    public void expire() { active = false; }

    /** Returns true if the arrow hits the player on this frame. */
    public boolean update(double dt, Area area, Player player) {
        if (!active) return false;
        life += dt;
        if (life >= MAX_LIFE) {
            active = false;
            return false;
        }

        x += vx * dt;
        y += vy * dt;

        // Stop if arrow hits a solid wall, grave, shrine, or tree
        if (area.blocked(x, y)) {
            active = false;
            return false;
        }

        if (!player.isDead() && player.distanceTo(x, y) <= HIT_RADIUS) {
            active = false;
            return true;
        }
        return false;
    }

    public double x() { return x; }
    public double y() { return y; }

    private void draw() {
        if (!active) return;
        int px = sx(x, y), py = sy(x, y, 22);
        int groundY = sy(x, y, 0);

        shadowOval(px, groundY - 1, 5, 1);

        // Screen-space angle matching isometric velocity
        double screenDx = (vx - vy) * 16.0;
        double screenDy = (vx + vy) * 8.0;
        double angle = Math.atan2(screenDy, screenDx);

        AffineTransform old = g.getTransform();
        g.translate(px, py);
        g.rotate(angle);

        if (SPRITE != null) {
            g.drawImage(SPRITE, -9, -2, 18, 4, null);
        } else {
            g.setColor(col(0x9a6b3e));
            g.fillRect(-8, -1, 14, 2);
            g.setColor(col(0xdce6f2));
            g.fillRect(6, -2, 3, 4);
        }

        g.setTransform(old);
        light(x, y, 10, 28, 0xffd28a);
    }
}