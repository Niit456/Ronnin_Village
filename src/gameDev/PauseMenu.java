package gameDev;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import static gameDev.Gfx.*;
import static gameDev.PixelFont.*;


public class PauseMenu extends MenuScreen {

    static final String TITLE = "RONIN VILLAGE";
    static final String TAGLINE = "A RONIN WANDERS THE ASHES";
    static final String[] ITEMS = {"PLAY", "CONTROLS", "QUIT"};
    static final String[] CONTROLS = {"WASD / ARROWS: MOVE", "SHIFT: RUN", "ESC: PAUSE"};
    static final int RONIN_X = 378, RONIN_Y = 196;   // where the ronin stands on the cliff

    private final Atmosphere atmosphere = new Atmosphere();

    public PauseMenu(World world) {
        super(world);
        dim = col(0xb0997f); highlightAlpha = 140; panelAlpha = 190; hintAlpha = 210;
    }

    @Override protected String[] mainItems() { return ITEMS; }
    @Override protected String[] controlLines() { return CONTROLS; }

    //background art
    static BufferedImage scene, fore;

    static Color c(int rgb) { return col(rgb); }
    static double h(int x, int y, int s) { return hash(x, y, s); }

    static void poly(Graphics2D g, Color col, int... p) {
        int n = p.length / 2;
        int[] xs = new int[n], ys = new int[n];
        for (int i = 0; i < n; i++) { xs[i] = p[2 * i]; ys[i] = p[2 * i + 1]; }
        g.setColor(col); g.fillPolygon(xs, ys, n);
    }
    static final int[] FAR_L = {0, 128, 24, 112, 44, 104, 60, 110, 78, 96, 92, 90, 106, 100, 124, 108, 142, 118, 164, 128, 190, 140, 215, 152};
    static final int[] FAR_R = {330, 132, 360, 118, 385, 106, 410, 100, 436, 90, 458, 96, 480, 88};
    static final int[] BIG = {176, 178, 196, 152, 214, 136, 232, 124, 248, 112, 262, 98, 272, 104, 286, 92, 300, 82, 316, 100, 332, 110, 348, 122, 364, 134, 380, 146, 396, 160, 410, 178};
    static final int[] HILL_L = {0, 168, 30, 156, 60, 160, 90, 152, 120, 160, 150, 166, 180, 172, 210, 178, 250, 184};
    static final int[] HILL_R = {250, 184, 290, 178, 330, 176, 370, 170, 420, 176, 480, 170};
    static final int[] CLIFF = {480, 88, 445, 92, 422, 100, 416, 118, 420, 138, 415, 160, 416, 190, 395, 193, 372, 196, 350, 197,
            338, 206, 326, 226, 316, 248, 306, 272, 300, 300, 480, 300};
    static final int[][] RIVER = {{300, 186, 5}, {284, 198, 6}, {262, 210, 8}, {240, 222, 10}, {222, 238, 12}, {206, 256, 15}, {194, 276, 19}, {186, 300, 24}};
    static final int[] FALL_Y = {240, 262, 284};

    //village layout
    static final int[][] HOUSES;   
    static final int[][] FIRES;    
    static {
        java.util.List<int[]> hs = new java.util.ArrayList<>();
        int[] rowY = {172, 180, 188, 196}, rowX = {182, 170, 174, 184}, rowStep = {12, 12, 13, 14}, rowN = {7, 9, 8, 6};
        for (int r = 0; r < 4; r++)
            for (int i = 0; i < rowN[r]; i++) {
                int x = rowX[r] + i * rowStep[r] + (int) (h(i, r, 41) * 5), y = rowY[r] + (int) (h(i, r, 42) * 3);
                int w = 7 + (int) (h(i, r, 43) * 4), hh = 4 + (int) (h(i, r, 44) * 2);
                hs.add(new int[]{x, y, w, hh, h(i, r, 45) < .32 ? 1 : 0, h(i, r, 46) > .5 ? 1 : 0});
            }
        HOUSES = hs.toArray(new int[0][]);
        java.util.List<int[]> fs = new java.util.ArrayList<>();
        for (int[] H : HOUSES) if (H[4] == 1) fs.add(new int[]{H[0] + H[2] / 2, H[1] - H[3] - 2, 5 + (int) (h(H[0], H[1], 47) * 4)});
        FIRES = fs.toArray(new int[0][]);
    }

    static double edge(int[] pts, int x, int seed, double jit) {
        int n = pts.length / 2;
        x = Math.max(pts[0], Math.min(pts[2 * n - 2], x));
        double y = pts[2 * n - 1];
        for (int i = 0; i < n - 1; i++) {
            int x0 = pts[2 * i], x1 = pts[2 * i + 2];
            if (x >= x0 && x <= x1) {
                double t = (x - x0) / (double) Math.max(1, x1 - x0);
                y = pts[2 * i + 1] + (pts[2 * i + 3] - pts[2 * i + 1]) * t;
                break;
            }
        }
        return y + (h(x / 2, 0, seed) - .5) * 2 * jit;
    }

    static void mountain(Graphics2D g, int[] pts, int base, int seed, int shadowRGB, int litRGB, int hazeRGB, double litAmt, int capRGB, int capY) {
        int n = pts.length / 2, x0 = pts[0], x1 = pts[2 * n - 2];
        Color shadow = c(shadowRGB), lit = c(litRGB), haze = c(hazeRGB);
        for (int x = x0; x <= x1; x++) {
            int top = (int) edge(pts, x, seed, 1.0);
            double slope = (edge(pts, x + 14, seed, 0) - edge(pts, x - 14, seed, 0)) / 28.0;   // wide window = smooth lighting
            double facing = Math.max(0, Math.min(1, .35 - slope * 1.6));
            for (int y = top; y < base; y++) {
                double t = (y - top) / (double) Math.max(1, base - top);
                double rid = h((x + (int) ((y - top) * 1.2)) / 4, (y - top) / 16, seed + 1);   // ridges run down-left from the peaks
                double L = facing * litAmt * (1 - t * .85) * (.45 + .9 * rid);
                Color col = mix(shadow, lit, Math.max(0, Math.min(1, L)));
                col = mix(col, haze, Math.max(0, Math.min(1, (t - .5) * 1.2)));
                if (capRGB != 0 && top < capY && y - top < 7 + h(x / 4, 2, seed) * 4)
                    col = mix(c(capRGB), col, Math.min(1, (y - top) / 12.0));
                g.setColor(col); g.fillRect(x, y, 1, 1);
            }
        }
    }

    static void pine(Graphics2D g, int x, int base, int hgt, Color dark, Color lite) {
        int tiers = Math.max(3, hgt / 14), add = hgt > 20 ? 3 : 1;
        g.setColor(c(0x0e090c)); g.fillRect(x - 1, base - 5, 3, 5);
        for (int i = 0; i < tiers; i++) {
            double t = i / (double) tiers;
            int w = (int) (hgt * .34 * (1 - t * .8)) + add;
            int yb = base - 4 - (int) (t * (hgt - 8));
            int th = (int) (hgt / (double) tiers * 1.5) + 2;
            poly(g, dark, x - w, yb, x + w, yb, x, yb - th);
            poly(g, lite, x - w, yb, x, yb, x, yb - th);       
        }
    }

    static void hill(Graphics2D g, int[] pts, int base, int seed, int lightRGB, int darkRGB, int pineDark, int pineLite) {
        int n = pts.length / 2, x0 = pts[0], x1 = pts[2 * n - 2];
        for (int x = x0; x <= x1; x++) {
            int top = (int) edge(pts, x, seed, 1.5);
            for (int y = top; y < base; y++) {
                g.setColor(mix(c(lightRGB), c(darkRGB), Math.min(1, (y - top) / 26.0)));
                g.fillRect(x, y, 1, 1);
            }
        }
        for (int x = x0; x <= x1; x += 3)
            if (h(x, 1, seed + 5) > .2)
                pine(g, x + (int) (h(x, 2, seed) * 2), (int) edge(pts, x, seed, 1.5) + 4, 6 + (int) (h(x, 3, seed) * 8), c(pineDark), c(pineLite));
    }

    static void sky(Graphics2D g) {
        for (int y = 0; y < LH; y += 2) {
            double t = Math.min(1, y / 150.0);
            g.setColor(mix(c(0x140f1d), c(0x5e3e4a), Math.pow(t, 1.4)));
            g.fillRect(0, y, LW, 2);
        }
        for (int i = 0; i < 70; i++) {
            g.setColor(new Color(225, 210, 220, 40 + (int) (h(i, 3, 21) * 90)));
            g.fillRect((int) (h(i, 1, 21) * LW), (int) (h(i, 2, 21) * 100), 1, 1);
        }
        for (int i = 0; i < 18; i++) {   // dark cloud streaks, lit from below by the fires
            int w = 50 + (int) (h(i, 1, 31) * 120), x = (int) (h(i, 2, 31) * (LW + 60)) - 30;
            int y = 12 + (int) (h(i, 3, 31) * 110), hh = 3 + (int) (h(i, 4, 31) * 6);
            g.setColor(new Color(24, 17, 32, 110)); g.fillOval(x, y, w, hh);
            double near = Math.max(0, 1 - Math.abs(x + w / 2 - 215) / 260.0) * Math.max(0, 1 - Math.abs(y - 130) / 120.0);
            g.setColor(new Color(176, 84, 80, (int) (46 * near))); g.fillOval(x + 5, y + hh / 2, w - 10, Math.max(2, hh / 2));
        }
        for (int k = 9; k >= 1; k--) {   // glow of the burning village on the horizon
            int rx = 34 + k * 20, ry = 12 + k * 8;
            g.setColor(new Color(214, 92, 66, 5)); g.fillOval(220 - rx, 160 - ry, rx * 2, ry * 2);
        }
    }

    static void house(Graphics2D g, int[] H) {
        int x = H[0], y = H[1], w = H[2], hh = H[3];
        boolean burn = H[4] == 1, win = H[5] == 1;
        g.setColor(burn ? c(0x4a2220) : c(0x33262c)); g.fillRect(x, y - hh, w, hh);
        g.setColor(burn ? c(0x2a1412) : c(0x1c1519)); g.fillRect(x + w - 2, y - hh, 2, hh);
        poly(g, burn ? c(0x9a3a28) : c(0x4a2c34), x - 1, y - hh, x + w + 1, y - hh, x + w / 2, y - hh - 4);
        g.setColor(burn ? c(0xe8682a) : c(0x68424a)); g.drawLine(x - 1, y - hh, x + w / 2, y - hh - 4);
        if (win) { g.setColor(c(0xffa040)); g.fillRect(x + 2, y - 3, 1, 2); }
    }

    static void village(Graphics2D g) {
        g.setColor(c(0x2a2428)); g.fillOval(148, 164, 176, 46);        // scorched ground
        g.setColor(c(0x372c2e)); g.fillOval(160, 168, 152, 34);
        for (int[] H : HOUSES) house(g, H);
        int x = 146, y = 204;
        g.setColor(c(0xc2403a)); g.fillRect(x, y - 15, 3, 15); g.fillRect(x + 13, y - 15, 3, 15); g.fillRect(x + 3, y - 10, 10, 2);
        g.setColor(c(0xe0524a)); g.fillRect(x - 3, y - 18, 22, 2);
        g.setColor(c(0x231b20)); g.fillRect(x - 3, y - 16, 22, 2);
    }

    static double riverLerp(double y, int idx) {
        for (int i = 0; i < RIVER.length - 1; i++)
            if (y <= RIVER[i + 1][1]) {
                double t = (y - RIVER[i][1]) / (double) (RIVER[i + 1][1] - RIVER[i][1]);
                t = Math.max(0, Math.min(1, t));
                return RIVER[i][idx] + (RIVER[i + 1][idx] - RIVER[i][idx]) * t;
            }
        return RIVER[RIVER.length - 1][idx];
    }
    static double riverX(double y) { return riverLerp(y, 0); }
    static double riverW(double y) { return riverLerp(y, 2); }

    static void river(Graphics2D g) {
        for (int y = RIVER[0][1]; y < LH; y++) {
            int x = (int) riverX(y), w = (int) riverW(y);
            g.setColor(c(0x1a222a)); g.fillRect(x - w / 2 - 2, y, w + 4, 1);
            g.setColor(mix(c(0x6f93a3), c(0xa8cad6), (y - RIVER[0][1]) / 114.0)); g.fillRect(x - w / 2, y, w, 1);
            if (h(x, y / 3, 7) > .72) { g.setColor(c(0xd8eef4)); g.fillRect(x - w / 2 + (int) (h(x, y, 8) * w), y, 2, 1); }
        }
        for (int fy : FALL_Y) {
            int cx = (int) riverX(fy), w = (int) riverW(fy) + 2, hgt = 12;
            poly(g, c(0x2c2833), cx - w / 2 - 10, fy - 1, cx - w / 2, fy - 3, cx - w / 2, fy + hgt + 3, cx - w / 2 - 13, fy + hgt + 4);
            poly(g, c(0x2c2833), cx + w / 2 + 10, fy - 1, cx + w / 2, fy - 3, cx + w / 2, fy + hgt + 3, cx + w / 2 + 13, fy + hgt + 4);
            g.setColor(c(0x4a4256));
            g.drawLine(cx - w / 2 - 10, fy - 1, cx - w / 2, fy - 3); g.drawLine(cx + w / 2 + 10, fy - 1, cx + w / 2, fy - 3);
            for (int i = 0; i < w; i++) {
                g.setColor(i % 2 == 0 ? c(0xe4f2f6) : c(0xa9c4ce));
                g.fillRect(cx - w / 2 + i, fy, 1, hgt - (int) (h(i, fy, 3) * 4));
            }
            g.setColor(c(0xeef7f9));
            for (int k = 0; k < 5; k++) g.fillOval(cx - w / 2 + k * w / 5 - 2, fy + hgt - 2, 6, 4);
        }
    }

    static void forest(Graphics2D g) {
        int[][] P = {{6, 262, 74}, {22, 288, 90}, {48, 272, 66}, {76, 296, 98}, {104, 278, 70}, {130, 298, 96}, {156, 284, 72}, {168, 262, 56},
                {34, 246, 58}, {66, 240, 50}, {244, 262, 58}, {262, 284, 76}, {232, 292, 70}, {286, 298, 86}, {276, 258, 52}};
        java.util.Arrays.sort(P, (p, q) -> p[1] - q[1]);
        for (int[] p : P) {
            double d = Math.max(0, Math.min(1, (p[1] - 238) / 62.0));
            pine(g, p[0], p[1], p[2], mix(c(0x16302e), c(0x070d0e), d), mix(c(0x244a44), c(0x10201f), d));
        }
    }

    static void cliff(Graphics2D g) {
        int n = CLIFF.length / 2;
        int[] xs = new int[n], ys = new int[n];
        for (int i = 0; i < n; i++) { xs[i] = CLIFF[2 * i]; ys[i] = CLIFF[2 * i + 1]; }
        Polygon cl = new Polygon(xs, ys, n);
        g.setClip(cl);
        int[] bands = {0x9a7050, 0x7a5642, 0x5a4034, 0x42312f, 0x30242a};
        for (int k = 0; k < bands.length; k++) {
            Polygon s = new Polygon(xs, ys, n); s.translate(k * 4, k * 2);
            g.setColor(c(bands[k])); g.fill(s);
        }
        for (int i = 0; i < 280; i++) {  
            int x = 296 + (int) (h(i, 1, 81) * 184), y = 88 + (int) (h(i, 2, 81) * 212);
            g.setColor(h(i, 3, 81) > .55 ? new Color(190, 150, 110, 70) : new Color(10, 6, 8, 90));
            g.fillRect(x, y, 2 + (int) (h(i, 4, 81) * 6), 1);
        }
        for (int i = 0; i < 34; i++) {   
            int x = 300 + (int) (h(i, 5, 82) * 180), y = 90 + (int) (h(i, 6, 82) * 200);
            g.setColor(new Color(10, 6, 8, 140));
            g.drawLine(x, y, x + (int) (h(i, 7, 82) * 5) - 2, y + 3 + (int) (h(i, 8, 82) * 9));
        }
        g.setClip(null);
        g.setColor(c(0xb48c68));        
        g.drawLine(480, 88, 445, 92); g.drawLine(445, 92, 420, 100);
        g.drawLine(416, 190, 395, 193); g.drawLine(395, 193, 372, 196); g.drawLine(372, 196, 350, 197);
    }

    static void clump(Graphics2D g, int cx, int cy, int w, int hh, int seed) {
        g.setColor(c(0x1f3f2c)); g.fillOval(cx - w / 2, cy - hh / 2 + 2, w, hh);
        g.setColor(c(0x2f5a3a)); g.fillOval(cx - w / 2 + 1, cy - hh / 2, w - 3, hh - 1);
        g.setColor(c(0x4f8a4e)); g.fillOval(cx - w / 2 + 3, cy - hh / 2 - 1, w * 2 / 3, hh / 2);
        for (int i = 0; i < w; i += 2) {
            double r = h(i, seed, 71);
            g.setColor(r > .5 ? c(0x5f9a58) : c(0x1c3626));
            g.fillRect(cx - w / 2 + i, cy - hh / 2 + (int) (r * hh), 1, 1);
            if (r < .5) { g.setColor(c(0x1f3f2c)); g.fillRect(cx - w / 2 + i, cy + hh / 2 + 1, 1, 2 + (int) (r * 6)); }
        }
    }

    static void tree(Graphics2D g) {  
        int[][] tr = {{462, 92}, {460, 80}, {454, 68}, {452, 56}, {446, 44}, {438, 34}};
        g.setColor(c(0x3a2a26));
        for (int i = 0; i < tr.length - 1; i++)
            for (int o = 0; o < 4 - i / 2; o++) g.drawLine(tr[i][0] + o, tr[i][1], tr[i + 1][0] + o, tr[i + 1][1]);
        g.setColor(c(0x5a4234));
        for (int i = 0; i < tr.length - 1; i++) g.drawLine(tr[i][0], tr[i][1], tr[i + 1][0], tr[i + 1][1]);
        g.setColor(c(0x3a2a26));
        int[][] br = {{454, 66, 428, 60}, {452, 54, 424, 48}, {446, 44, 416, 38}, {458, 74, 478, 66}, {448, 48, 470, 40}};
        for (int[] b : br) { g.drawLine(b[0], b[1], b[2], b[3]); g.drawLine(b[0], b[1] + 1, b[2], b[3] + 1); }
        int[][] cp = {{424, 58, 30, 11}, {418, 46, 28, 10}, {434, 36, 34, 12}, {454, 28, 34, 12}, {470, 40, 30, 12}, {474, 60, 24, 10}, {410, 36, 22, 9}, {440, 50, 22, 9}};
        for (int i = 0; i < cp.length; i++) clump(g, cp[i][0], cp[i][1], cp[i][2], cp[i][3], i + 3);
    }

    static void fern(Graphics2D g, int x, int y, int len, int seed) {
        for (int k = -3; k <= 3; k++) {
            double ang = -Math.PI / 2 + k * .32;
            int ex = x + (int) (Math.cos(ang) * len * (.7 + h(k, seed, 5) * .5)), ey = y + (int) (Math.sin(ang) * len * (.7 + h(k, seed, 6) * .5));
            g.setColor(c(0x2f5a3a)); g.drawLine(x, y, ex, ey);
            g.setColor(c(0x4f8a4e)); g.drawLine(x, y - 1, ex + 1, ey);
        }
    }

    static double ledgeY(int x) {  
        int[][] p = {{350, 197}, {372, 196}, {395, 193}, {416, 190}};
        x = Math.max(350, Math.min(416, x));
        for (int i = 0; i < 3; i++) if (x <= p[i + 1][0]) return p[i][1] + (p[i + 1][1] - p[i][1]) * (x - p[i][0]) / (double) (p[i + 1][0] - p[i][0]);
        return 190;
    }

    static void tuft(Graphics2D g, int x, int y) {   
        g.setColor(c(0x6a6a34)); g.fillRect(x, y - 3, 1, 3); g.fillRect(x - 2, y - 2, 1, 2); g.fillRect(x + 2, y - 2, 1, 2);
        g.setColor(c(0x8a8a44)); g.fillRect(x, y - 4, 1, 1);
    }

    static void foreground(Graphics2D g) {
        for (int i = 0; i < 16; i++) { int x = 352 + i * 4 + (int) (h(i, 1, 91) * 3); tuft(g, x, (int) ledgeY(x) + 1); }
        for (int i = 0; i < 9; i++) { int x = 426 + i * 6 + (int) (h(i, 2, 91) * 3); tuft(g, x, (int) (100 - (x - 420) * 12 / 60.0 + 1)); }
        fern(g, 452, 299, 22, 1); fern(g, 466, 301, 26, 2); fern(g, 476, 297, 20, 3); fern(g, 440, 301, 18, 4); fern(g, 458, 290, 16, 5);
    }

    static void bake() {
        scene = new BufferedImage(LW, LH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scene.createGraphics();
        sky(g);
        for (int k = 5; k >= 1; k--) {  
            int rx = 30 + k * 16, ry = 8 + k * 6;
            g.setColor(new Color(214, 130, 140, 9)); g.fillOval(125 - rx, 122 - ry, rx * 2, ry * 2);
        }
        mountain(g, FAR_L, 190, 11, 0x2e2a40, 0x7e6c84, 0x5a4450, .55, 0xc4c2d8, 102);
        mountain(g, FAR_R, 190, 21, 0x2e2a40, 0x6e5e78, 0x5a4450, .40, 0xb4b2c8, 98);
        mountain(g, BIG, 200, 31, 0x33263a, 0xa8705c, 0x5a4450, 1.0, 0, 0);
        hill(g, HILL_L, 300, 41, 0x37605a, 0x18302f, 0x142a2a, 0x244844);
        hill(g, HILL_R, 300, 51, 0x37605a, 0x18302f, 0x142a2a, 0x244844);
        village(g);
        river(g);
        forest(g);
        cliff(g);
        tree(g);
        g.dispose();

        fore = new BufferedImage(LW, LH, BufferedImage.TYPE_INT_ARGB);
        g = fore.createGraphics();
        foreground(g);
        g.dispose();
    }

    //animated parts
    static void fires(Graphics2D g, double time) {
        for (int f = 0; f < FIRES.length; f++) {
            int x = FIRES[f][0], y = FIRES[f][1], s = FIRES[f][2];
            for (int k = 0; k < 5; k++) {   // smoke drifting up
                double life = (time * 9 + k * 11 + f * 5) % 46;
                int px = x + (int) (Math.sin(time * 1.1 + k + f) * 3 + life * .12), py = y - 2 - (int) life;
                int r = 2 + (int) (life / 12), al = (int) (80 * (1 - life / 46));
                g.setColor(new Color(64, 54, 62, Math.max(0, al))); g.fillOval(px - r, py - r, r * 2, r * 2);
            }
            double fl = Math.sin(time * 11 + x * .7) * .5 + .5;
            int hgt = (int) (s * (1.3 + .6 * fl));
            poly(g, c(0xd8481f), x - s / 2, y, x + s / 2, y, x, y - hgt);
            poly(g, c(0xf28a26), x - s / 3, y, x + s / 3, y, x, y - hgt * 3 / 4);
            poly(g, c(0xffd868), x - s / 6, y, x + s / 6, y, x, y - hgt / 2);
            lights.add(new Light(x, y - s / 2, 20 + s * 2, 0xff7030));
        }
    }

    static void valleyMist(Graphics2D g, double time) {
        for (int i = 0; i < 6; i++) {
            double w = 70 + h(i, 1, 61) * 70, x = ((i * 83 + time * (3 + i)) % (LW + 160)) - 80, y = 176 + i * 7 + Math.sin(time * .5 + i) * 2;
            g.setColor(new Color(190, 175, 190, 22)); g.fillOval((int) (x - w / 2), (int) y, (int) w, 6 + i % 2 * 2);
        }
    }

    static void waterfalls(Graphics2D g, double time) {
        for (int fy : FALL_Y) {
            int cx = (int) riverX(fy), w = (int) riverW(fy) + 2;
            for (int i = 0; i < w; i += 2) {
                int dy = (int) (time * 26 + i * 5) % 12;
                g.setColor(new Color(235, 245, 248, 190)); g.fillRect(cx - w / 2 + i, fy + dy, 1, 3);
            }
        }
    }


    //the ronin
    private static void ronin(Graphics2D g, int feetX, int feetY, boolean faceRight, double t) {
        BufferedImage s = FallbackSprite.IMAGE;
        int hgt = s.getHeight(), leg = FallbackSprite.LEG, top = feetY - hgt + 2, ax = FallbackSprite.AX, w = s.getWidth();
        int bob = Math.sin(t * 2.0) > .6 ? -1 : 0;
        shadowOval(feetX, feetY - 1, 11, 3);
        part(g, s, feetX, top, faceRight, leg, hgt, 0, ax, w);
        part(g, s, feetX, top, faceRight, 0, leg + 1, bob, ax, w);
    }

    private static void part(Graphics2D g, BufferedImage s, int feetX, int top, boolean faceRight, int r0, int r1, int dy, int ax, int w) {
        if (faceRight) g.drawImage(s, feetX - ax, top + r0 + dy, feetX - ax + w, top + r1 + dy, 0, r0, w, r1, null);
        else g.drawImage(s, feetX + ax + 1, top + r0 + dy, feetX + ax + 1 - w, top + r1 + dy, 0, r0, w, r1, null);
    }

    private void title(Graphics2D g) {
        int sc = 5, w = textW(TITLE, sc), x = (LW - w) / 2, y = 28;
        text(TITLE, x + 2, y + 3, sc, new Color(0, 0, 0, 170));
        text(TITLE, x + 1, y + 1, sc, mix(col(0x8a1f24), col(0xd8481f), .5 + .5 * Math.sin(time * 2)));
        text(TITLE, x, y, sc, col(0xf5e1be));
        g.setColor(EDGE); g.fillRect(x, y + 31, w, 1);
        textS(TAGLINE, (LW - textW(TAGLINE, 2)) / 2, y + 37, 2, new Color(215, 190, 170, 235));
    }

    @Override protected void render(BufferedImage out, double fade) {
        Graphics2D g = out.createGraphics();
        Gfx.g = g; Gfx.time = time; camX = 0; camY = 0;
        lights.clear();
        if (scene == null) bake();

        g.drawImage(scene, 0, 0, null);
        waterfalls(g, time);
        fires(g, time);
        valleyMist(g, time);
        ronin(g, RONIN_X, RONIN_Y, false, time);
        g.drawImage(fore, 0, 0, null);

        lights.add(new Light(220, 184, 78, 0xff6a30));
        lights.add(new Light(352, 176, 40, 0xff7a40));
        lights.add(new Light(RONIN_X, 176, 36, 0xdfe3ff));
        lights.add(new Light(262, 210, 30, 0x9ec8dc));
        lights.add(new Light(222, 238, 32, 0x9ec8dc));
        lights.add(new Light(204, 262, 36, 0x9ec8dc));
        lights.add(new Light(192, 286, 38, 0x9ec8dc));
        atmosphere.fog();
        atmosphere.lightPass();
        atmosphere.embers();
        atmosphere.vignette();

        for (int x = 0; x < 300; x += 3) { g.setColor(new Color(0, 0, 0, (int) (70 * (1 - x / 300.0)))); g.fillRect(x, 0, 3, LH); }   // keeps the menu readable

        title(g);
        drawItemsAndHint();
        if (fade > 0) { g.setColor(new Color(0, 0, 0, (int) (255 * Math.min(1, fade)))); g.fillRect(0, 0, LW, LH); }
        g.dispose();
    }

    public static void main(String[] args) throws Exception {
        World world = new World();
        if (args.length > 0 && args[0].equals("--screenshots")) {
            PauseMenu m = new PauseMenu(world);
            String dir = args.length > 1 ? args[1] : ".";
            int[] screens = {SCREEN_MAIN, SCREEN_CONTROLS};
            String[] names = {"menu_main.png", "menu_controls.png"};
            for (int i = 0; i < screens.length; i++) {
                BufferedImage lo = new BufferedImage(LW, LH, BufferedImage.TYPE_INT_RGB);
                m.renderState(lo, screens[i], 0, 1.4, 0);
                BufferedImage big = new BufferedImage(LW * 3, LH * 3, BufferedImage.TYPE_INT_RGB);
                Graphics2D bg = big.createGraphics();
                bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                bg.drawImage(lo, 0, 0, LW * 3, LH * 3, null); bg.dispose();
                ImageIO.write(big, "png", new File(dir, names[i]));
                System.out.println("Wrote " + names[i]);
            }
            return;
        }
        SwingUtilities.invokeLater(() -> {
            Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
            int sc = (int) Math.max(1, Math.min(4, Math.floor(Math.min(scr.width * .92 / LW, scr.height * .86 / LH))));
            JFrame f = new JFrame("Ronin Village");
            PauseMenu m = new PauseMenu(world);
            m.setPreferredSize(new Dimension(LW * sc, LH * sc));
            f.setContentPane(m);
            f.pack(); f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
            m.requestFocusInWindow();
        });
    }
}
