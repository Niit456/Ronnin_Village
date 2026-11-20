package gameDev;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import static gameDev.Gfx.*;

public final class Atmosphere {
    private final BufferedImage mask = new BufferedImage(LW, LH, BufferedImage.TYPE_INT_ARGB);
    private final double[][] embers = new double[80][5];   

    public Atmosphere() {
        Random r = new Random(5);
        for (double[] e : embers) { e[0] = r.nextDouble() * (LW + 20); e[1] = r.nextDouble() * LH; e[2] = 4 + r.nextDouble() * 10; e[3] = 6 + r.nextDouble() * 12; e[4] = r.nextDouble(); }
    }

    public void background() {
        for (int y = 0; y < LH; y += 6) {
            double t = y / (double) LH;
            g.setColor(mix(col(0x120d17), col(0x3a1f2a), t));
            g.fillRect(0, y, LW, 6);
        }
        Random r = new Random(3);
        for (int i = 0; i < 50; i++) { g.setColor(new Color(200, 180, 190, 40 + r.nextInt(60))); g.fillRect(r.nextInt(LW), r.nextInt(LH / 2), 1, 1); }
    }

    public void fog() {
        for (int b = 0; b < 5; b++) {
            int y = 150 + b * 36;
            for (int x = -8; x < LW + 8; x += 8) {
                double w = Math.sin((x + time * (5 + b * 2)) * .035 + b * 1.7) * .5 + .5;
                int al = (int) (8 + w * 20);
                g.setColor(new Color(150, 120, 130, al));
                g.fillRect(x, y + (int) (Math.sin(x * .05 + b) * 4), 8, 14);
            }
        }
    }

    public void lightPass() {
        Graphics2D m = mask.createGraphics();
        m.setComposite(AlphaComposite.Src); m.setColor(new Color(8, 4, 18, 125)); m.fillRect(0, 0, LW, LH);
        m.setComposite(AlphaComposite.DstOut);
        for (int i = 0; i < lights.size(); i++) {
            Light L = lights.get(i);
            int r = (int) (L.r * (1 + Math.sin(time * 7 + i * 1.9) * .04));
            for (int k = 0; k < 5; k++) { int rr = r * (5 - k) / 5; m.setColor(new Color(0, 0, 0, 70)); m.fillOval(L.x - rr, L.y - rr, rr * 2, rr * 2); }
        }
        m.dispose();
        g.drawImage(mask, 0, 0, null);
        for (Light L : lights) {
            Color c = col(L.rgb);
            for (int k = 0; k < 4; k++) { int rr = L.r * (4 - k) / 4; g.setColor(al(c, 10)); g.fillOval(L.x - rr, L.y - rr, rr * 2, rr * 2); }
        }
    }

    public void embers() {
        for (double[] e : embers) {
            double x = (e[0] + time * e[2] * .6 + Math.sin(time * 1.3 + e[4] * 9) * 6) % (LW + 20) - 10;
            double y = ((e[1] - time * e[3]) % LH + LH) % LH;
            int a = (int) (90 + 120 * (Math.sin(time * 3 + e[4] * 20) * .5 + .5));
            g.setColor(new Color(255, 150 + (int) (e[4] * 70), 60, a));
            g.fillRect((int) x, (int) y, e[4] > .85 ? 2 : 1, e[4] > .85 ? 2 : 1);
        }
    }

    public void vignette() {
        for (int i = 1; i <= 9; i++) {
            int w = i * 5;
            g.setColor(new Color(0, 0, 0, 20));
            g.fillRect(0, 0, LW, w); g.fillRect(0, LH - w, LW, w); g.fillRect(0, w, w, LH - 2 * w); g.fillRect(LW - w, w, w, LH - 2 * w);
        }
    }
}
