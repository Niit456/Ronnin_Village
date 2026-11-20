package gameDev;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;


public final class SpriteSheet {
    static final int TARGET_HEIGHT = 44;       
    static final double SHARPEN = 0.6;         
    static final int FOOT_OFFSET_X = 0, FOOT_OFFSET_Y = 0;

    private final BufferedImage[] frames;
    private final int anchorX, anchorY;      
    private final int frameMs;
    private final double scale;
    private final boolean facesRight;         

    private SpriteSheet(BufferedImage[] frames, int anchorX, int anchorY, int frameMs, double scale, boolean facesRight) {
        this.frames = frames; this.anchorX = anchorX; this.anchorY = anchorY; this.frameMs = frameMs; this.scale = scale; this.facesRight = facesRight;
    }

    public int length() { return frames.length; }
    public int frameMs() { return frameMs; }
    public double scale() { return scale; }
    public BufferedImage frame(int i) { return frames[i]; }

    public void draw(Graphics2D g, int frame, int footX, int footY, boolean faceRight) {
        BufferedImage img = frames[frame];
        int w = img.getWidth(), h = img.getHeight();
        int y = footY - anchorY + FOOT_OFFSET_Y;
        if (faceRight == facesRight) {
            g.drawImage(img, footX - anchorX + FOOT_OFFSET_X, y, null);
        } else {
            int x = footX + anchorX - FOOT_OFFSET_X;
            g.drawImage(img, x, y, x - w, y + h, 0, 0, w, h, null);
        }
    }

    //loading
    public static SpriteSheet load(String[] paths, int frameCount, int frameW, int frameH, int frameMs, boolean facesRight, double forcedScale) {
        File file = null;
        for (String path : paths) {
            File f = new File(path);
            if (f.isFile()) { file = f; break; }
        }
        if (file == null) {
            System.err.println("Sprite sheet not found: " + paths[0]);
            return null;
        }
        try {
            BufferedImage sheet = ImageIO.read(file);
            if (sheet == null) throw new IllegalStateException("Invalid image");
            if (sheet.getWidth() < frameW * frameCount || sheet.getHeight() < frameH)
                throw new IllegalStateException("Incorrect dimensions. Expected at least " + (frameW * frameCount) + "x" + frameH
                        + ", got " + sheet.getWidth() + "x" + sheet.getHeight());

            BufferedImage[] raw = new BufferedImage[frameCount];
            int minY = frameH, maxY = -1;
            for (int i = 0; i < frameCount; i++) {                       
                raw[i] = new BufferedImage(frameW, frameH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = raw[i].createGraphics();
                g.drawImage(sheet.getSubimage(i * frameW, 0, frameW, frameH), 0, 0, null);
                g.dispose();
                for (int y = 0; y < frameH; y++)
                    for (int x = 0; x < frameW; x++)
                        if ((raw[i].getRGB(x, y) >>> 24) > 32) { minY = Math.min(minY, y); maxY = Math.max(maxY, y); }
            }
            if (maxY < 0) { minY = 0; maxY = frameH - 1; }

            double scale = forcedScale > 0 ? forcedScale : TARGET_HEIGHT / (double) (maxY - minY + 1);
            int nw = Math.max(1, (int) Math.round(frameW * scale)), nh = Math.max(1, (int) Math.round(frameH * scale));
            BufferedImage[] result = new BufferedImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                if (Math.abs(scale - 1.0) < 0.000001) result[i] = raw[i];
                else if (scale > 1.0) result[i] = enlarge(raw[i], nw, nh);
                else result[i] = crisp(shrink(raw[i], nw, nh), SHARPEN);
            }
            System.out.println(file.getName() + " loaded: " + frameCount + " frames (" + nw + "x" + nh + " displayed)");
            return new SpriteSheet(result, nw / 2, Math.min(nh, (int) Math.round((maxY + 1) * scale)), frameMs, scale, facesRight);
        } catch (Exception e) {
            System.err.println("Could not load " + file.getName() + ": " + e);
            return null;
        }
    }

    private static BufferedImage shrink(BufferedImage src, int nw, int nh) {
        int sw = src.getWidth(), sh = src.getHeight();
        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
        double fx = sw / (double) nw, fy = sh / (double) nh;
        for (int y = 0; y < nh; y++)
            for (int x = 0; x < nw; x++) {
                double x0 = x * fx, x1 = x0 + fx, y0 = y * fy, y1 = y0 + fy;
                double a = 0, r = 0, gr = 0, b = 0, wsum = 0;
                for (int sy = (int) y0; sy < Math.min(sh, (int) Math.ceil(y1)); sy++) {
                    double wy = Math.min(sy + 1, y1) - Math.max(sy, y0);
                    for (int sx = (int) x0; sx < Math.min(sw, (int) Math.ceil(x1)); sx++) {
                        double w = (Math.min(sx + 1, x1) - Math.max(sx, x0)) * wy;
                        int p = src.getRGB(sx, sy);
                        double al = (p >>> 24) / 255.0;
                        a += w * al; r += w * al * ((p >> 16) & 255); gr += w * al * ((p >> 8) & 255); b += w * al * (p & 255);
                        wsum += w;
                    }
                }
                if (a > 0) {
                    int A = (int) Math.round(255 * a / wsum);
                    out.setRGB(x, y, (A << 24) | (c255(r / a) << 16) | (c255(gr / a) << 8) | c255(b / a));
                }
            }
        return out;
    }

    
    private static BufferedImage crisp(BufferedImage src, double amount) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                int p = src.getRGB(x, y);
                if ((p >>> 24) < 110) continue;                          
                double wsum = 0, r = 0, g = 0, b = 0;                    
                for (int dy = -1; dy <= 1; dy++)
                    for (int dx = -1; dx <= 1; dx++) {
                        int xx = x + dx, yy = y + dy;
                        if (xx < 0 || yy < 0 || xx >= w || yy >= h) continue;
                        int q = src.getRGB(xx, yy);
                        double a = (q >>> 24) / 255.0, k = (dx == 0 && dy == 0 ? 4 : (dx == 0 || dy == 0 ? 2 : 1)) * a;
                        wsum += k; r += k * ((q >> 16) & 255); g += k * ((q >> 8) & 255); b += k * (q & 255);
                    }
                int R = (p >> 16) & 255, G = (p >> 8) & 255, B = p & 255;
                if (wsum > 0) { R = c255(R + amount * (R - r / wsum)); G = c255(G + amount * (G - g / wsum)); B = c255(B + amount * (B - b / wsum)); }
                out.setRGB(x, y, 0xFF000000 | (R << 16) | (G << 8) | B);
            }
        return out;
    }

    private static BufferedImage enlarge(BufferedImage src, int nw, int nh) {
        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(src, 0, 0, nw, nh, null);
        g.dispose();
        return out;
    }

    private static int c255(double v) { return (int) Math.max(0, Math.min(255, Math.round(v))); }
}
