package gameDev;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import static gameDev.Gfx.*;
import static gameDev.PixelFont.*;

public class MainMenu extends MenuScreen {

    static final String TITLE = "CURSED RONIN";
    static final String KANJI = "呪われた浪人";
    static final String TAGLINE = "A RONIN WANDERS THE ASHES";
    static final String[] ITEMS = {"PLAY", "CONTROLS", "QUIT"};
    static final String[] CONTROLS = {"WASD / ARROWS: MOVE", "SHIFT: RUN", "ESC: QUIT GAME"};
    static final int GX = 392;                       // x of the torii gate and the moon
    static final double LOADING_TIME = 2.5, BAR_TIME = 1.5;

    private final Atmosphere atmosphere = new Atmosphere();
    private final Audio.Loop music = new Audio.Loop("res/menu_music.wav", 0);
    private boolean loading;
    private double loadingTime;

    public MainMenu(World world) {
        super(world);
        music.start();
    }

    @Override protected String[] mainItems() { return ITEMS; }
    @Override protected String[] controlLines() { return CONTROLS; }

    @Override protected void onLeaving(double dt) {
        if (!loading) {
            leaving += dt / .5;
            if (leaving >= 1) { leaving = 1; loading = true; music.stop(); Audio.playLoading(); }
        } else {
            loadingTime += dt;
            if (loadingTime >= LOADING_TIME) startGame();
        }
    }

    @Override protected void beforeStart() { music.stop(); }

    @Override protected void renderFrame(BufferedImage out) {
        super.renderFrame(out);
        if (loading) renderLoading(out, Math.min(1, loadingTime / BAR_TIME), time);
    }

    @Override protected void render(BufferedImage out, double fade) {
        Graphics2D g2 = out.createGraphics();
        Gfx.g = g2; Gfx.time = time; camX = 0; camY = 0;
        lights.clear();
        atmosphere.background();
        moon(g2, GX, 92, 34);
        ridge(g2, col(0x2c2238), 172, 38, 3, .021);
        ridge(g2, col(0x1a1223), 196, 30, 8, .027);
        ground(g2);
        Painters.drawTree(wx(292, 248), wy(292, 248), 78, 5);
        Painters.drawTree(wx(16, 262), wy(16, 262), 132, 11);
        Painters.drawTree(wx(466, 258), wy(466, 258), 120, 17);
        gate(g2, GX, 234);
        Painters.drawFire(wx(372, 240), wy(372, 240), false);
        lights.add(new Light(GX, 92, 80, 0x9fb4ff));
        atmosphere.fog();
        atmosphere.lightPass();
        atmosphere.embers();
        atmosphere.vignette();
        title(g2);
        drawItemsAndHint();
        if (fade > 0) { g2.setColor(new Color(0, 0, 0, (int) (255 * Math.min(1, fade)))); g2.fillRect(0, 0, LW, LH); }
        g2.dispose();
    }

    private static double wx(int px, int py) { return (py / 8.0 + px / 16.0) / 2; }
    private static double wy(int px, int py) { return (py / 8.0 - px / 16.0) / 2; }

    static void moon(Graphics2D g, int cx, int cy, int r) {
        for (int k = 7; k >= 1; k--) {
            int rr = r + k * 6;
            g.setColor(new Color(255, 214, 190, 7)); g.fillOval(cx - rr, cy - rr, rr * 2, rr * 2);
        }
        g.setColor(col(0xe9d7c0)); g.fillOval(cx - r, cy - r, r * 2, r * 2);
        g.setColor(col(0xd2bba5));
        int[][] craters = {{-12, -8, 7}, {10, -14, 5}, {6, 10, 9}, {-16, 12, 4}, {16, 2, 4}};
        for (int[] c : craters) g.fillOval(cx + c[0] - c[2], cy + c[1] - c[2], c[2] * 2, c[2] * 2);
    }

    static void ridge(Graphics2D g, Color c, int base, double amp, int seed, double freq) {
        int n = LW / 6 + 2;
        int[] xs = new int[n + 2], ys = new int[n + 2];
        for (int i = 0; i < n; i++) {
            int x = i * 6;
            double h = Math.sin(x * freq + seed) * .5 + Math.sin(x * freq * 2.3 + seed * 1.7) * .3 + hash(i, 0, seed) * .35;
            xs[i] = x; ys[i] = (int) (base - amp * (h + .5));
        }
        xs[n] = LW + 6; ys[n] = LH; xs[n + 1] = 0; ys[n + 1] = LH;
        g.setColor(c); g.fillPolygon(xs, ys, n + 2);
    }

    static void ground(Graphics2D g) {
        ridge(g, col(0x1b121a), 228, 5, 9, .02);
        for (int y = 230; y < LH; y += 4) { g.setColor(new Color(0, 0, 0, 12 + (y - 230) / 2)); g.fillRect(0, y, LW, 4); }
        for (int i = 0; i < 170; i++) {
            int x = (int) (hash(i, 1, 4) * LW), y = 232 + (int) (hash(i, 2, 4) * (LH - 234));
            g.setColor(hash(i, 3, 4) > .5 ? col(0x2e2229) : col(0x0d080d));
            g.fillRect(x, y, 2, 1);
        }
        for (int i = 0; i < 30; i++) {                // sickly grass tufts, same look as the game
            int x = (int) (hash(i, 5, 8) * LW), y = 236 + (int) (hash(i, 6, 8) * (LH - 244));
            g.setColor(col(0x4a4a28));
            g.fillRect(x, y - 3, 1, 3); g.fillRect(x - 2, y - 2, 1, 2); g.fillRect(x + 2, y - 2, 1, 2);
        }
    }

    static void gate(Graphics2D g, int gx, int base) {
        Color post = col(0x5a1d20), hi = col(0x9a3330), dk = col(0x2e0f12), blk = col(0x1d1418), red = col(0x7a2428);
        int half = 44, pw = 9;
        for (int s = -1; s <= 1; s += 2) {
            int px = gx + s * half - pw / 2;
            shadowOval(px + pw / 2, base, 11, 3);
            g.setColor(post); g.fillRect(px, 104, pw, base - 104);
            g.setColor(hi); g.fillRect(px, 104, 2, base - 104);                 // moonlit edge
            g.setColor(dk); g.fillRect(px + pw - 2, 104, 2, base - 104);
            g.setColor(blk); g.fillRect(px - 1, base - 6, pw + 2, 6);           // stone foot
        }
        g.setColor(red); g.fillRect(gx - half, 128, half * 2, 6);               // lower tie beam
        g.setColor(dk); g.fillRect(gx - half, 133, half * 2, 1);
        g.setColor(blk); g.fillRect(gx - 4, 113, 8, 15);                        // centre plaque
        g.setColor(red); g.fillRect(gx - half - 8, 106, half * 2 + 16, 6);      // upper tie beam
        g.setColor(dk); g.fillRect(gx - half - 8, 111, half * 2 + 16, 1);
        int[] xs = {gx - 66, gx - 40, gx + 40, gx + 66, gx + 61, gx + 40, gx - 40, gx - 61};
        int[] ys = {90, 97, 97, 90, 99, 106, 106, 99};
        g.setColor(blk); g.fillPolygon(xs, ys, xs.length);                      // curved top beam
        g.setColor(col(0x4a363c));
        g.drawLine(gx - 66, 90, gx - 40, 97); g.drawLine(gx - 40, 97, gx + 40, 97); g.drawLine(gx + 40, 97, gx + 66, 90);
    }

    private void title(Graphics2D g2) {
        int sc = 5, w = textW(TITLE, sc);
        int x = (LW - w) / 2, y = 28;
        text(TITLE, x + 2, y + 3, sc, new Color(0, 0, 0, 170));
        text(TITLE, x + 1, y + 1, sc, mix(col(0x8a1f24), col(0xd8481f), .5 + .5 * Math.sin(time * 2)));
        text(TITLE, x, y, sc, col(0xf5e1be));
        g2.setFont(new Font("Dialog", Font.BOLD, 18));
        g2.setColor(col(0xf5e1be));
        g2.drawString(KANJI, (LW - g2.getFontMetrics().stringWidth(KANJI)) / 2, y + 49);
        g2.setColor(EDGE);
        g2.fillRect(x, y + 57, w, 1);
        textS(TAGLINE, (LW - textW(TAGLINE, 2)) / 2, y + 64, 2, new Color(205, 180, 160, 230));
    }

    //loading screen
    private void renderLoading(BufferedImage out, double progress, double t) {
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        progress = Math.max(0, Math.min(1, progress));
        Gfx.g = g2; Gfx.time = t; camX = 0; camY = 0;
        atmosphere.background();
        atmosphere.fog();
        atmosphere.embers();
        g2.setColor(new Color(8, 6, 12, 155)); g2.fillRect(0, 0, LW, LH);          // dark overlay
        for (int i = 5; i >= 1; i--) {                                              // crimson glow
            g2.setColor(new Color(120, 25, 35, 8));
            g2.fillOval(LW / 2 - i * 35, LH / 2 - i * 20, i * 70, i * 40);
        }
        String msg = "PREPARING THE VILLAGE";
        text(msg, (LW - textW(msg, 2)) / 2, LH / 2 - 22, 2, col(0xf0d9b8));
        int barW = 190, barH = 12, bx = (LW - barW) / 2, by = LH / 2;
        g2.setColor(new Color(0, 0, 0, 180)); g2.fillRect(bx - 5, by - 5, barW + 10, barH + 10);
        g2.setColor(col(0x21151c)); g2.fillRect(bx, by, barW, barH);
        int fillW = (int) (barW * progress);
        g2.setColor(col(0x751f29)); g2.fillRect(bx, by, fillW, barH);
        if (fillW > 0) {
            g2.setColor(col(0xc34a38)); g2.fillRect(bx, by, fillW, 2);
            g2.setColor(col(0xf2c14e)); g2.fillRect(bx, by, fillW, 1);
        }
        g2.setColor(col(0xb18a50));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(bx - 1, by - 1, barW + 2, barH + 2);
        int d = 5;                                                                  // decorative corners
        g2.fillRect(bx - 4, by - 4, d, 2); g2.fillRect(bx - 4, by - 4, 2, d);
        g2.fillRect(bx + barW - 1, by - 4, d, 2); g2.fillRect(bx + barW + 2, by - 4, 2, d);
        g2.fillRect(bx - 4, by + barH + 2, d, 2); g2.fillRect(bx - 4, by + barH - 1, 2, d);
        g2.fillRect(bx + barW - 1, by + barH + 2, d, 2); g2.fillRect(bx + barW + 2, by + barH - 1, 2, d);
        String percent = (int) (progress * 100) + "%";
        g2.setFont(new Font("Dialog", Font.BOLD, 11));
        g2.setColor(col(0xf0d9b8));
        g2.drawString(percent, (LW - g2.getFontMetrics().stringWidth(percent)) / 2, by + 30);
        g2.setFont(new Font("Dialog", Font.BOLD, 13));
        g2.setColor(col(0x9a3330));
        g2.drawString(KANJI, (LW - g2.getFontMetrics().stringWidth(KANJI)) / 2, by + 53);
        g2.dispose();
    }

    //entry points
    static void screenshots(World world, String dir) throws Exception {
        MainMenu m = new MainMenu(world);
        m.music.stop();
        Object[][] shots = {{SCREEN_MAIN, "menu_main.png"}, {SCREEN_CONTROLS, "menu_controls.png"}};
        for (Object[] s : shots) {
            BufferedImage lo = new BufferedImage(LW, LH, BufferedImage.TYPE_INT_RGB);
            m.renderState(lo, (Integer) s[0], 0, 1.4, 0);
            BufferedImage big = new BufferedImage(LW * 3, LH * 3, BufferedImage.TYPE_INT_RGB);
            Graphics2D bg = big.createGraphics();
            bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            bg.drawImage(lo, 0, 0, LW * 3, LH * 3, null); bg.dispose();
            ImageIO.write(big, "png", new File(dir, (String) s[1]));
            System.out.println("Wrote " + s[1]);
        }
    }

    public static void main(String[] args) throws Exception {
        World world = new World();
        if (args.length > 0 && args[0].equals("--screenshots")) { screenshots(world, args.length > 1 ? args[1] : "."); return; }
        SwingUtilities.invokeLater(() -> {
            Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
            int sc = (int) Math.max(1, Math.min(4, Math.floor(Math.min(scr.width * .92 / LW, scr.height * .86 / LH))));
            JFrame f = new JFrame("Ronin Village");
            MainMenu m = new MainMenu(world);
            m.setPreferredSize(new Dimension(LW * sc, LH * sc));
            f.setContentPane(m);
            f.pack(); f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
            m.requestFocusInWindow();
        });
    }
}
