package gameDev;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import static gameDev.Gfx.*;
import static gameDev.PixelFont.*;


public final class PauseOverlay {

    public interface Listener {
        void resume();
        void mainMenu();
    }

    static final String[] ITEMS = {"RESUME", "CONTROLS", "MAIN MENU", "QUIT"};
    static final String[] CONTROLS = {"WASD / ARROWS: MOVE", "SHIFT: RUN", "ESC: PAUSE"};
    static final int PW = 228, PH = 176, PX0 = (LW - PW) / 2, PY0 = (LH - PH) / 2;
    static final int ITEM_Y = 60, ITEM_STEP = 26, ITEM_SC = 3, SCREEN_MAIN = 0, SCREEN_CONTROLS = 1;
    static final Color CREAM = col(0xf0d9b8), DIM = col(0xb0997f), GOLD = col(0xf2c14e), EDGE = col(0x6a4a54);

    private final Listener listener;
    private final MenuList list = new MenuList(ITEMS.length);
    private final BufferedImage ui = new BufferedImage(LW, LH, BufferedImage.TYPE_INT_ARGB);
    private boolean open;
    private int screen = SCREEN_MAIN;
    private double t;                    
    private String areaName = "";

    public PauseOverlay(Listener listener) { this.listener = listener; }

    public boolean isOpen() { return open; }

    public void open(String areaName) {
        open = true; screen = SCREEN_MAIN; list.reset(ITEMS.length, 0); t = 0;
        this.areaName = areaName;
    }

    public void close() { open = false; }
    public void update(double dt) { if (open) t += dt; }

    private String[] items() { return screen == SCREEN_MAIN ? ITEMS : new String[]{"BACK"}; }

    private static Rectangle rect(int screen, int i) {
        int slot = screen == SCREEN_MAIN ? i : ITEMS.length - 1;       // BACK sits where the last item is
        return new Rectangle(PX0 + 14, PY0 + ITEM_Y + slot * ITEM_STEP - 4, PW - 28, 23);
    }

    /** item under a point of the small picture, or -1 */
    public int hit(int lx, int ly) {
        for (int i = 0; i < items().length; i++) if (rect(screen, i).contains(lx, ly)) return i;
        return -1;
    }

    public boolean hover(int lx, int ly) {
        int i = hit(lx, ly);
        if (i >= 0) list.select(i);
        return i >= 0;
    }

    public void click(int lx, int ly) {
        int i = hit(lx, ly);
        if (i >= 0) { list.select(i); Audio.playButton(); confirm(); }
    }

    private void confirm() {
        if (screen == SCREEN_CONTROLS) { screen = SCREEN_MAIN; list.reset(ITEMS.length, 1); return; }
        switch (list.selected()) {
            case 0: listener.resume(); break;
            case 1: screen = SCREEN_CONTROLS; list.reset(1, 0); break;
            case 2: listener.mainMenu(); break;
            default: System.exit(0);
        }
    }

    public void key(int k) {
        if (k == KeyEvent.VK_UP || k == KeyEvent.VK_W) { list.previous(); Audio.playButton(); }
        else if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) { list.next(); Audio.playButton(); }
        else if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) { Audio.playButton(); confirm(); }
        else if (k == KeyEvent.VK_ESCAPE) {
            Audio.playButton();
            if (screen == SCREEN_CONTROLS) { screen = SCREEN_MAIN; list.reset(ITEMS.length, 1); }
            else listener.resume();
        }
    }

    private static void centered(String s, int y, int sc, Color c) { textS(s, (LW - textW(s, sc)) / 2, y, sc, c); }

    public void draw(Graphics2D target, int offX, int offY, int w, int h) {
        Graphics2D og = ui.createGraphics();
        og.setComposite(AlphaComposite.Clear); og.fillRect(0, 0, LW, LH);
        og.setComposite(AlphaComposite.SrcOver);
        Gfx.g = og;                       
        paint(og);
        og.dispose();
        target.drawImage(ui, offX, offY, w, h, null);
    }

    private void paint(Graphics2D og) {
        double a = Math.min(1, t / .18);
        int alpha = (int) (255 * a);
        og.setColor(new Color(8, 4, 14, (int) (150 * a))); og.fillRect(0, 0, LW, LH);
        og.setColor(new Color(10, 6, 14, (int) (228 * a))); og.fillRect(PX0, PY0, PW, PH);
        og.setColor(al(EDGE, alpha)); og.drawRect(PX0, PY0, PW - 1, PH - 1);
        og.setColor(new Color(58, 38, 46, alpha)); og.drawRect(PX0 + 3, PY0 + 3, PW - 7, PH - 7);

        String title = screen == SCREEN_MAIN ? "PAUSED" : "CONTROLS";
        int tx = (LW - textW(title, 4)) / 2;
        text(title, tx + 1, PY0 + 13, 4, al(col(0x8a1f24), alpha));
        text(title, tx, PY0 + 12, 4, al(col(0xf5e1be), alpha));
        if (screen == SCREEN_MAIN) centered(areaName, PY0 + 36, 1, al(DIM, alpha));
        og.setColor(al(EDGE, alpha)); og.fillRect(PX0 + 14, PY0 + 47, PW - 28, 1);

        if (screen == SCREEN_CONTROLS)
            for (int i = 0; i < CONTROLS.length; i++) centered(CONTROLS[i], PY0 + 64 + i * 20, 2, al(CREAM, alpha));

        String[] it = items();
        for (int i = 0; i < it.length; i++) {
            Rectangle r = rect(screen, i);
            boolean on = i == list.selected();
            int ty = r.y + 4;
            if (on) {
                og.setColor(new Color(122, 30, 34, (int) (140 * a))); og.fillRect(r.x, r.y, r.width, r.height);
                og.setColor(al(col(0xa83530), alpha)); og.fillRect(r.x, r.y, 2, r.height);
                int bob = (int) Math.round(Math.sin(t * 6) * 2);
                textS(">", r.x + 10 + bob, ty, ITEM_SC, al(GOLD, alpha));
                textS("<", r.x + r.width - 10 - textW("<", ITEM_SC) - bob, ty, ITEM_SC, al(GOLD, alpha));
            }
            Color c = on ? mix(CREAM, GOLD, .5 + .5 * Math.sin(t * 5)) : DIM;
            centered(it[i], ty, ITEM_SC, al(c, alpha));
        }
        String hint = screen == SCREEN_MAIN ? "W/S: SELECT   ENTER: CONFIRM   ESC: RESUME" : "ENTER OR ESC: BACK";
        centered(hint, PY0 + PH - 11, 1, new Color(230, 210, 190, (int) (200 * a)));
    }
}
