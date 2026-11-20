package gameDev;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class Gfx {
    private Gfx() { }

    public static final int LW = 480, LH = 300;    
    public static final int TW = 32, TH = 16;            
    public static final int MAP = 32;                    

    public static Graphics2D g;
    public static int camX, camY;
    public static double time;
    public static final List<Light> lights = new ArrayList<>();

    public static Color col(int rgb) { return new Color(rgb); }
    public static int cl(double v) { return (int) Math.max(0, Math.min(255, v)); }
    public static Color shade(Color c, double f) { return new Color(cl(c.getRed() * f), cl(c.getGreen() * f), cl(c.getBlue() * f), c.getAlpha()); }
    public static Color al(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), a); }
    public static Color mix(Color a, Color b, double t) {
        return new Color(cl(a.getRed() + (b.getRed() - a.getRed()) * t), cl(a.getGreen() + (b.getGreen() - a.getGreen()) * t), cl(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }
    public static double hash(int x, int y, int s) {
        long h = x * 374761393L + y * 668265263L + s * 2147483647L;
        h = (h ^ (h >>> 13)) * 1274126177L;
        h ^= (h >>> 16);
        return (h & 0xffff) / 65535.0;
    }
    public static int sx(double x, double y) { return (int) Math.round((x - y) * TW / 2.0) - camX; }
    public static int sy(double x, double y, double z) { return (int) Math.round((x + y) * TH / 2.0 - z) - camY; }
    
    public static void quad(Color c, double... p) {
        int n = p.length / 3;
        int[] xs = new int[n], ys = new int[n];
        for (int i = 0; i < n; i++) { xs[i] = sx(p[3 * i], p[3 * i + 1]); ys[i] = sy(p[3 * i], p[3 * i + 1], p[3 * i + 2]); }
        g.setColor(c); g.fillPolygon(xs, ys, n); g.drawPolygon(xs, ys, n);
    }
    public static void fillq(Color c, double... p) {
        int n = p.length / 3;
        int[] xs = new int[n], ys = new int[n];
        for (int i = 0; i < n; i++) { xs[i] = sx(p[3 * i], p[3 * i + 1]); ys[i] = sy(p[3 * i], p[3 * i + 1], p[3 * i + 2]); }
        g.setColor(c); g.fillPolygon(xs, ys, n);
    }
    public static void ln(Color c, double x1, double y1, double z1, double x2, double y2, double z2) {
        g.setColor(c); g.drawLine(sx(x1, y1), sy(x1, y1, z1), sx(x2, y2), sy(x2, y2, z2));
    }
    public static void box(double x, double y, double w, double d, double z0, double h, Color base) {
        double z1 = z0 + h;
        quad(shade(base, .72), x, y + d, z0, x + w, y + d, z0, x + w, y + d, z1, x, y + d, z1);
        quad(shade(base, .50), x + w, y, z0, x + w, y + d, z0, x + w, y + d, z1, x + w, y, z1);
        quad(base, x, y, z1, x + w, y, z1, x + w, y + d, z1, x, y + d, z1);
        Color e = shade(base, .36);
        ln(e, x, y + d, z0, x + w, y + d, z0); ln(e, x + w, y + d, z0, x + w, y, z0);
        ln(e, x, y + d, z0, x, y + d, z1); ln(e, x + w, y + d, z0, x + w, y + d, z1); ln(e, x + w, y, z0, x + w, y, z1);
        Color t = shade(base, 1.28);
        ln(t, x, y + d, z1, x + w, y + d, z1); ln(t, x + w, y + d, z1, x + w, y, z1);
    }
    public static void wallL(double x, double y, double w, double d, double u0, double u1, double za, double zb, Color c) {
        quad(c, x + u0 * w, y + d, za, x + u1 * w, y + d, za, x + u1 * w, y + d, zb, x + u0 * w, y + d, zb);
    }
    public static void wallR(double x, double y, double w, double d, double u0, double u1, double za, double zb, Color c) {
        quad(c, x + w, y + u0 * d, za, x + w, y + u1 * d, za, x + w, y + u1 * d, zb, x + w, y + u0 * d, zb);
    }
    public static void shadowOval(int cx, int cy, int rx, int ry) {
        g.setColor(new Color(0, 0, 0, 70)); g.fillOval(cx - rx, cy - ry, rx * 2, ry * 2);
    }
    /** a light at a world position (x, y, height z) */
    public static void light(double x, double y, double z, int r, int rgb) {
        lights.add(new Light(sx(x, y), sy(x, y, z), r, rgb));
    }
}
