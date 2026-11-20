package gameDev;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static gameDev.Gfx.*;
import static gameDev.Painters.*;

public abstract class Area {

    //ground description (what bakeGround paints)
    public static final class PathSegment {
        final double x1, y1, x2, y2, halfWidth;
        PathSegment(double x1, double y1, double x2, double y2, double halfWidth) { this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.halfWidth = halfWidth; }
    }
    //a round patch of ground in another colour
    public static final class Disc {
        final double cx, cy, r; final int rgb;
        Disc(double cx, double cy, double r, int rgb) { this.cx = cx; this.cy = cy; this.r = r; this.rgb = rgb; }
    }
    public static final class Patch {
        final Zone zone; final int rgb;
        Patch(Zone zone, int rgb) { this.zone = zone; this.rgb = rgb; }
    }

    public final AreaId id;
    public final String name;
    public final String subtitle;               
    protected final int seed;                      
    protected boolean indoor;                

    private final List<Prop> props = new ArrayList<>();
    private final List<Zone> solids = new ArrayList<>();
    private final List<Exit> exits = new ArrayList<>();
    private final List<Wisp> wisps = new ArrayList<>();
    private Zone goal;                             //walking in here ends the game
    
    //ground
    protected Color[] dirt;                        //the base ground colours, picked per tile
    protected Color pathColor, grass;
    private final List<PathSegment> paths = new ArrayList<>();
    private final List<Disc> discs = new ArrayList<>();
    private final List<Patch> patches = new ArrayList<>();
    private final List<Zone> waters = new ArrayList<>();
    private BufferedImage ground;
    private int groundOx, groundOy;

    protected Area(AreaId id, String name, String subtitle, int seed) {
        this.id = id; this.name = name; this.subtitle = subtitle; this.seed = seed;
    }

    public void update(double dt) { }
    public void drawHud() { }
    public void onAttack() { }
    protected void paintGroundDecor() { }
    public void reset() { }

    public List<Prop> props() { return Collections.unmodifiableList(props); }
    public List<Exit> exits() { return Collections.unmodifiableList(exits); }
    public List<Wisp> wisps() { return Collections.unmodifiableList(wisps); }
    public boolean isIndoor() { return indoor; }
    public Zone goal() { return goal; }
    public void setGoal(Zone goal) { this.goal = goal; }

    public boolean blocked(double x, double y) {
        if (x < .5 || y < .5 || x > MAP - .5 || y > MAP - .5) return true;
        for (Zone s : solids) if (s.blocks(x, y, .22)) return true;
        return false;
    }

    public void drawGround() { g.drawImage(ground, -groundOx - camX, -groundOy - camY, null); }

    public void addProp(Prop p) { props.add(p); }
    public void removeProp(Prop p) { props.remove(p); }

    protected void solid(double x, double y, double w, double d) { solids.add(Zone.sized(x, y, w, d)); }
    protected void prop(double x0, double y0, double x1, double y1, Painter p) { props.add(new Prop(x0, y0, x1, y1, p)); }
    protected void path(double x1, double y1, double x2, double y2, double halfWidth) { paths.add(new PathSegment(x1, y1, x2, y2, halfWidth)); }
    protected void disc(double cx, double cy, double r, int rgb) { discs.add(new Disc(cx, cy, r, rgb)); }
    protected void patch(double x, double y, double w, double d, int rgb) { patches.add(new Patch(Zone.sized(x, y, w, d), rgb)); }
    protected void water(double x, double y, double w, double d) { waters.add(Zone.sized(x, y, w, d)); }
    protected void wisp(double x, double y, double z) { wisps.add(new Wisp(x, y, z)); }
    protected void exit(Exit e) { exits.add(e); }

    protected void edgeExit(Zone zone, AreaId target, double spawnX, double spawnY, int dir) {
        exits.add(new Exit(zone, target, spawnX, spawnY, dir, dir > 0 ? MAP - 2.6 : 2.6, 16, 66 * TORII_H));
    }

    protected void tree(double x, double y, int h, int seed) {
        solid(x - .3, y - .3, .6, .6);
        prop(x - .4, y - .4, x + .4, y + .4, () -> drawTree(x, y, h, seed));
    }
    protected void banner(double x, double y, int rgb) { prop(x - .2, y - .2, x + .2, y + .2, () -> drawBanner(x, y, col(rgb))); }
    protected void toro(double x, double y, int rgb) { solid(x - .45, y - .45, .9, .9); prop(x - .55, y - .55, x + .55, y + .55, () -> drawToro(x, y, rgb)); }
    protected void fence(double x, double y, int n, boolean alongX) {
        double L = n * .5;
        if (alongX) { solid(x - .1, y - .15, L, .3); prop(x - .2, y - .2, x + L, y + .2, () -> drawFence(x, y, n, true)); }
        else { solid(x - .15, y - .1, .3, L); prop(x - .2, y - .2, x + .2, y + L, () -> drawFence(x, y, n, false)); }
    }
    protected void rock(double x, double y, int sz) {
        solid(x - sz / 16.0, y - sz / 16.0, sz / 8.0, sz / 8.0);
        prop(x - .4, y - .4, x + .4, y + .4, () -> drawRock(x, y, sz));
    }

    protected void gate(double cx, double cy, double span, boolean alongX, boolean broken) {
        double u0 = (alongX ? cx : cy) - span / 2, u1 = (alongX ? cx : cy) + span / 2;
        for (int w = 0; w < 2; w++) {
            final int which = w; double u = (w == 0) ? u0 : u1;
            double px = alongX ? u : cx, py = alongX ? cy : u;
            solid(px - .3, py - .3, .6, .6);
            prop(px - .4, py - .4, px + .4, py + .4, () -> drawTorii(cx, cy, span, alongX, broken, false, which));
        }
        final double ins = .4;   
        if (alongX) {
            prop(u0 + ins, cy - .25, u1 - ins, cy + .25, () -> drawTorii(cx, cy, span, true, broken, true, 0));
            prop(u0 - .7, cy + ins, u1 + .7, cy + ins + .5, () -> drawTorii(cx, cy, span, true, broken, true, 2));
        } else {
            prop(cx - .25, u0 + ins, cx + .25, u1 - ins, () -> drawTorii(cx, cy, span, false, broken, true, 0));
            prop(cx + ins, u0 - .7, cx + ins + .5, u1 + .7, () -> drawTorii(cx, cy, span, false, broken, true, 2));
        }
    }

    private boolean onPath(double x, double y) {
        for (PathSegment s : paths) {
            double dx = s.x2 - s.x1, dy = s.y2 - s.y1;
            double t = Math.max(0, Math.min(1, ((x - s.x1) * dx + (y - s.y1) * dy) / (dx * dx + dy * dy)));
            double px = s.x1 + dx * t, py = s.y1 + dy * t;
            if (Math.hypot(x - px, y - py) < s.halfWidth) return true;
        }
        return false;
    }

    private boolean inWater(double cx, double cy) {
        for (Zone w : waters) if (w.covers(cx, cy)) return true;
        return false;
    }

    public void bakeGround() {
        int ox0 = MAP * TW / 2 + 16, oy0 = 16;
        BufferedImage img = new BufferedImage(MAP * TW + 32, MAP * TH + 44, BufferedImage.TYPE_INT_ARGB);
        Graphics2D old = g; int ocx = camX, ocy = camY;
        g = img.createGraphics(); camX = -ox0; camY = -oy0;
        // slab sides first
        for (int i = 0; i < MAP; i++) {
            Color s1 = (i % 3 == 0) ? col(0x2a1f20) : col(0x33262a);
            quad(s1, i, MAP, 0, i + 1, MAP, 0, i + 1, MAP, -14, i, MAP, -14);
            Color s2 = (i % 3 == 0) ? col(0x1b1416) : col(0x241a1d);
            quad(s2, MAP, i, 0, MAP, i + 1, 0, MAP, i + 1, -14, MAP, i, -14);
            ln(col(0x4a3a36), i, MAP, -4, i + 1, MAP, -4);
            ln(col(0x15100f), i, MAP, -9, i + 1, MAP, -9);
        }
        for (int ty = 0; ty < MAP; ty++) for (int tx = 0; tx < MAP; tx++) {
            double cx = tx + .5, cy = ty + .5;
            Color base = dirt[(int) (hash(tx, ty, seed) * dirt.length) % dirt.length];
            boolean water = inWater(cx, cy);
            if (water) {
                base = (hash(tx, ty, 5) > .5) ? col(0x1d3b4a) : col(0x234759);
            } else {
                for (Patch r : patches) if (r.zone.covers(cx, cy)) base = mix(base, col(r.rgb), .8);
                if (onPath(cx, cy)) base = mix(base, pathColor, .72 + hash(tx, ty, 9) * .2);
                for (Disc c : discs) if (Math.hypot(cx - c.cx, cy - c.cy) < c.r) base = mix(base, col(c.rgb), .8 + hash(tx, ty, 3) * .15);
                if (hash(tx, ty, 77) < .12) base = shade(base, .76);
            }
            quad(base, tx, ty, 0, tx + 1, ty, 0, tx + 1, ty + 1, 0, tx, ty + 1, 0);
            // pixel speckle
            for (int k = 0; k < 4; k++) {
                double u = hash(tx * 7 + k, ty * 13 + k, seed + 1), v = hash(tx * 5 + k, ty * 11 + k, seed + 2);
                g.setColor(shade(base, k % 2 == 0 ? 1.22 : .78));
                g.fillRect(sx(tx + u, ty + v), sy(tx + u, ty + v, 0), 1, 1);
            }
            if (water) {
                if (hash(tx, ty, 41) > .55) { g.setColor(col(0x4f8aa0)); g.fillRect(sx(tx + .3, ty + .5), sy(tx + .3, ty + .5, 0), 3, 1); }
                continue;
            }
            if (indoor) continue;
            double hh = hash(tx, ty, 123);
            if (hh > .86 && !onPath(cx, cy)) {
                int px = sx(tx + .5, ty + .5), py = sy(tx + .5, ty + .5, 0);
                g.setColor(grass); g.fillRect(px, py - 3, 1, 3); g.fillRect(px - 2, py - 2, 1, 2); g.fillRect(px + 2, py - 2, 1, 2);
                g.setColor(shade(grass, 1.35)); g.fillRect(px, py - 4, 1, 1);
            } else if (hh < .05) { // small stones
                int px = sx(tx + .4, ty + .6), py = sy(tx + .4, ty + .6, 0);
                g.setColor(col(0x6f6c6e)); g.fillRect(px, py - 2, 3, 2); g.setColor(col(0x8d8a8c)); g.fillRect(px, py - 2, 2, 1);
                g.setColor(col(0x4b494d)); g.fillRect(px + 4, py - 1, 2, 1);
            }
        }
        paintGroundDecor();
        g.dispose(); g = old; camX = ocx; camY = ocy;
        ground = img; groundOx = ox0; groundOy = oy0;
    }
}
