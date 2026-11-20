package gameDev;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import static gameDev.Gfx.*;
import static gameDev.PixelFont.*;

public final class SceneRenderer {
    static final String HUD_HINT = "ESC: PAUSE";

    private final Atmosphere atmosphere = new Atmosphere();

    public void render(BufferedImage buf, Area a, Player player, double fade, double titleA, double endT) {
        g = buf.createGraphics();
        lights.clear();
        if (a.isIndoor()) { g.setColor(col(0x0b0709)); g.fillRect(0, 0, LW, LH); } else atmosphere.background();
        a.drawGround();
        paintSorted(a, player);
        if (Math.abs(fade) < 2) {                                  // see-through ghost: the ronin stays visible when a gate, wall or roof is in front of him
            Composite oc = g.getComposite(); int nl = lights.size();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .38f));
            player.draw();
            while (lights.size() > nl) lights.remove(lights.size() - 1);
            g.setComposite(oc);
        }
        drawWisps(a);
        atmosphere.fog();
        atmosphere.lightPass();
        drawExitArrows(a);
        atmosphere.embers();
        atmosphere.vignette();
        a.drawHud();
        drawChrome(a, titleA);
        if (fade > 0) { g.setColor(new Color(0, 0, 0, (int) (255 * Math.min(1, fade)))); g.fillRect(0, 0, LW, LH); }
        if (endT >= 0) drawEnding(endT);
        g.dispose();
    }

    private void drawChrome(Area a, double titleA) {
        g.setColor(new Color(10, 6, 14, 150)); g.fillRect(6, 6, textW(a.name, 2) + 12, 18);
        g.setColor(col(0x6a4a54)); g.drawRect(6, 6, textW(a.name, 2) + 11, 17);
        textS(a.name, 12, 10, 2, col(0xf0d9b8));
        textS("WASD / ARROWS: MOVE   SHIFT: RUN", 8, LH - 12, 1, new Color(230, 210, 190, 200));
        textS(HUD_HINT, LW - textW(HUD_HINT, 1) - 8, LH - 12, 1, new Color(230, 210, 190, 200));
        String an = "AREA " + a.id.number() + "/" + AreaId.count();
        textS(an, LW - textW(an, 1) - 8, 10, 1, new Color(230, 210, 190, 200));
        if (titleA > 0) {
            int al = (int) (255 * Math.min(1, titleA));
            textS(a.name, (LW - textW(a.name, 4)) / 2, 78, 4, new Color(245, 225, 190, al));
            String s2 = a.subtitle != null ? a.subtitle : "";
            textS(s2, (LW - textW(s2, 1)) / 2, 106, 1, new Color(200, 180, 160, al));
        }
    }

    private void paintSorted(Area a, Player player) {
        List<Prop> vis = new ArrayList<>();
        for (Prop p : a.props()) {
            int cx = sx((p.x0 + p.x1) / 2, (p.y0 + p.y1) / 2), cy = sy((p.x0 + p.x1) / 2, (p.y0 + p.y1) / 2, 0);
            if (cx < -150 || cx > LW + 150 || cy < -60 || cy > LH + 240) continue;
            vis.add(p);
        }
        vis.add(player.asProp());
        vis.sort(Comparator.comparingDouble(Prop::sortKey));
        int[] state = new int[vis.size()];                       // 0 = not drawn, 1 = being visited, 2 = drawn
        for (int i = 0; i < vis.size(); i++) visit(vis, state, i);
    }

    private void visit(List<Prop> v, int[] state, int i) {
        if (state[i] != 0) return;
        state[i] = 1;
        for (int j = 0; j < v.size(); j++) if (j != i && state[j] == 0 && v.get(j).mustPrecede(v.get(i))) visit(v, state, j);
        state[i] = 2;
        v.get(i).paint();
    }

    private void drawWisps(Area a) {
        for (Wisp w : a.wisps()) {
            int x = sx(w.x, w.y) + (int) Math.round(Math.sin(time * 1.4 + w.x) * 4), y = sy(w.x, w.y, w.z + Math.sin(time * 2 + w.y) * 3);
            g.setColor(new Color(120, 255, 220, 70)); g.fillRect(x - 2, y - 2, 5, 5);
            g.setColor(new Color(200, 255, 245, 220)); g.fillRect(x - 1, y - 1, 2, 2);
            lights.add(new Light(x, y, 22, 0x60f0d0));
        }
    }

    private void drawExitArrows(Area a) {
        for (Exit e : a.exits()) {
            if (!e.hasArrow()) continue;                // this exit has its own marker
            int bx = sx(e.arrowX, e.arrowY), by = sy(e.arrowX, e.arrowY, e.arrowZ) + (int) Math.round(Math.sin(time * 4) * 2);
            double dx = e.dir * 16, dy = e.dir * 8, len = Math.hypot(dx, dy); dx /= len; dy /= len;
            double px = -dy, py = dx;
            int[] xs = {(int) (bx + dx * 9), (int) (bx - dx * 4 + px * 6), (int) (bx - dx * 4 - px * 6)};
            int[] ys = {(int) (by + dy * 9), (int) (by - dy * 4 + py * 6), (int) (by - dy * 4 - py * 6)};
            g.setColor(col(0xf2c14e)); g.fillPolygon(xs, ys, 3);
            g.setColor(col(0x3a2a14)); g.drawPolygon(xs, ys, 3);
        }
    }

    private void drawEnding(double endT) {
        double a = Math.min(1, endT / 1.6);
        g.setColor(new Color(0, 0, 0, (int) (255 * a))); g.fillRect(0, 0, LW, LH);
        double b = Math.max(0, Math.min(1, (endT - 1.6) / 1.2));
        if (b <= 0) return;
        int al = (int) (255 * b);
        String t1 = "THE END";
        textS(t1, (LW - textW(t1, 6)) / 2, 80, 6, new Color(245, 225, 190, al));
        g.setColor(new Color(106, 74, 84, al)); g.fillRect(LW / 2 - 70, 122, 140, 1);
        String[] l = {"THE SHOGUN HAS FALLEN.", "THE ASHES FINALLY REST,", "AND THE RONINS WANDERING IS OVER."};
        for (int i = 0; i < l.length; i++) textS(l[i], (LW - textW(l[i], 1)) / 2, 134 + i * 12, 1, new Color(210, 190, 170, al));
        if (endT > 3) {
            String s = "PRESS ENTER FOR THE MAIN MENU";
            int pa = (int) (120 + 100 * Math.sin(endT * 4));
            textS(s, (LW - textW(s, 1)) / 2, 200, 1, new Color(240, 217, 184, Math.max(0, Math.min(255, pa))));
        }
    }
}
