package gameDev;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import static gameDev.Gfx.*;

public class ItemDrop {

    private static final String[] PATHS = {"res/SkeletonItemDrop.png", "SkeletonItemDrop.png"};
    private static final BufferedImage SPRITE = loadSprite();
    private static final double PICKUP_RADIUS = 0.85;

    private final double x, y;
    private double age;
    private boolean collected;

    public ItemDrop(Area area, double x, double y) {
        this.x = x;
        this.y = y;
        area.addProp(new Prop(x - 0.35, y - 0.35, x + 0.35, y + 0.35, this::draw));
    }

    private static BufferedImage loadSprite() {
        for (String p : PATHS) {
            File f = new File(p);
            if (!f.isFile()) continue;
            try {
                BufferedImage raw = ImageIO.read(f);
                if (raw == null) continue;
                int w = raw.getWidth(), h = raw.getHeight();
                int bg = raw.getRGB(0, 0);
                int bgR = (bg >> 16) & 255, bgG = (bg >> 8) & 255, bgB = bg & 255;

                BufferedImage clean = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                int minX = w, minY = h, maxX = 0, maxY = 0;
                for (int yy = 0; yy < h; yy++) {
                    for (int xx = 0; xx < w; xx++) {
                        int c = raw.getRGB(xx, yy);
                        int a = (c >>> 24) & 255;
                        int r = (c >> 16) & 255, g = (c >> 8) & 255, b = c & 255;
                        if (a < 32 || (Math.abs(r - bgR) < 28 && Math.abs(g - bgG) < 28 && Math.abs(b - bgB) < 28)) {
                            continue;
                        }
                        clean.setRGB(xx, yy, 0xFF000000 | (r << 16) | (g << 8) | b);
                        minX = Math.min(minX, xx); minY = Math.min(minY, yy);
                        maxX = Math.max(maxX, xx); maxY = Math.max(maxY, yy);
                    }
                }
                return maxX >= minX ? clean.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1) : raw;
            } catch (Exception ignored) { }
        }
        return null;
    }

    public boolean isCollected() { return collected; }
    public static BufferedImage sprite() { return SPRITE; }

    /** Returns true on the exact frame the player picks up this drop. */
    public boolean update(double dt, Player player) {
        if (collected) return false;
        age += dt;
        // Brief 0.25s pop delay so the player sees it drop from the skeleton
        if (age > 0.25 && player.distanceTo(x, y) <= PICKUP_RADIUS) {
            collected = true;
            return true;
        }
        return false;
    }

    private void draw() {
        if (collected) return;
        int px = sx(x, y), py = sy(x, y, 0);

        // Initial hop arc when spawned, followed by gentle floating bob
        double hop = age < 0.35 ? Math.sin((age / 0.35) * Math.PI) * 9.0 : 0.0;
        double bob = Math.sin(age * 4.5) * 2.0 + hop;

        shadowOval(px, py - 1, 6, 2);

        if (SPRITE != null) {
            int dw = 12, dh = 14;
            g.drawImage(SPRITE, px - dw / 2, (int) Math.round(py - dh - 2 - bob), dw, dh, null);
        } else {
            g.setColor(col(0xc83c38));
            g.fillOval(px - 4, (int) Math.round(py - 11 - bob), 8, 10);
        }

        light(x, y, 16, 35, 0xff6a50);
    }
}