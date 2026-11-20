package gameDev;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import static gameDev.Gfx.*;
import static gameDev.PixelFont.*;


public abstract class MenuScreen extends JPanel implements KeyListener {

    //layout / palette
    static final int MENU_X = 74, MAIN_Y = 130, CTRL_BACK_Y = 204, MENU_STEP = 28, MENU_SC = 3;
    static final Color CREAM = col(0xf0d9b8), GOLD = col(0xf2c14e), EDGE = col(0x6a4a54);
    static final int SCREEN_MAIN = 0, SCREEN_CONTROLS = 1;

    protected final World world;
    private final BufferedImage buf = new BufferedImage(LW, LH, BufferedImage.TYPE_INT_RGB);
    protected final javax.swing.Timer timer;
    protected final MenuList list;
    protected int screen = SCREEN_MAIN;
    protected double time, fadeIn = 1, leaving = -1;      
    protected Color dim = col(0x9a8576);                  
    protected int highlightAlpha = 120, panelAlpha = 170, hintAlpha = 200;
    private long last = System.nanoTime();
    private double scale = 1; private int offX, offY;
    private boolean launched;

    protected MenuScreen(World world) {
        this.world = world;
        list = new MenuList(mainItems().length);
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        MouseAdapter m = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int i = hitTest(e);
                if (i >= 0) list.select(i);
                setCursor(Cursor.getPredefinedCursor(i >= 0 ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
            @Override public void mouseClicked(MouseEvent e) {
                int i = hitTest(e);
                if (i >= 0) { list.select(i); Audio.playButton(); confirm(); }
            }
        };
        addMouseListener(m);
        addMouseMotionListener(m);
        timer = new javax.swing.Timer(16, e -> { tick(); repaint(); });
        timer.start();
    }

    protected abstract String[] mainItems();
    protected abstract String[] controlLines();
    protected abstract void render(BufferedImage out, double fade);

    private String[] items() { return screen == SCREEN_MAIN ? mainItems() : new String[]{"BACK"}; }

    protected Rectangle itemRect(int i) {
        String s = items()[i];
        int y = (screen == SCREEN_MAIN ? MAIN_Y : CTRL_BACK_Y) + i * MENU_STEP;
        return new Rectangle(MENU_X - 24, y - 4, textW(s, MENU_SC) + 40, 5 * MENU_SC + 8);
    }

    private int hitTest(MouseEvent e) {
        int lx = (int) ((e.getX() - offX) / scale), ly = (int) ((e.getY() - offY) / scale);
        for (int i = 0; i < items().length; i++) if (itemRect(i).contains(lx, ly)) return i;
        return -1;
    }

    private void confirm() {
        if (leaving >= 0) return;
        if (screen == SCREEN_CONTROLS) { showScreen(SCREEN_MAIN, 1); return; }
        switch (list.selected()) {
            case 0: leaving = 0; break;                              // PLAY: fade out, then start the game
            case 1: showScreen(SCREEN_CONTROLS, 0); break;
            default: System.exit(0);                                 // QUIT
        }
    }

    private void showScreen(int s, int selected) { screen = s; list.reset(items().length, selected); }

    protected void drawItems() {
        String[] it = items();
        for (int i = 0; i < it.length; i++) {
            Rectangle r = itemRect(i);
            int y = r.y + 4;
            boolean on = i == list.selected();
            if (on) {
                g.setColor(new Color(122, 30, 34, highlightAlpha)); g.fillRect(r.x, r.y, r.width, r.height);
                g.setColor(col(0xa83530)); g.fillRect(r.x, r.y, 2, r.height);
                int bob = (int) Math.round(Math.sin(time * 6) * 2);
                textS(">", MENU_X - 16 + bob, y, MENU_SC, GOLD);
            }
            Color c = on ? mix(CREAM, GOLD, .5 + .5 * Math.sin(time * 5)) : dim;
            textS(it[i], MENU_X, y, MENU_SC, c);
        }
    }

    protected void drawControlsPanel() {
        int px = MENU_X - 24, py = 96, pw = 236, ph = 138;
        g.setColor(new Color(10, 6, 14, panelAlpha)); g.fillRect(px, py, pw, ph);
        g.setColor(EDGE); g.drawRect(px, py, pw - 1, ph - 1);
        textS("CONTROLS", MENU_X, 108, 3, GOLD);
        g.setColor(EDGE); g.fillRect(MENU_X, 128, textW("CONTROLS", 3), 1);
        String[] lines = controlLines();
        for (int i = 0; i < lines.length; i++) textS(lines[i], MENU_X, 140 + i * 20, 2, CREAM);
        drawItems();
    }

    protected void drawItemsAndHint() {
        if (screen == SCREEN_MAIN) drawItems(); else drawControlsPanel();
        String hint = screen == SCREEN_MAIN ? "W/S OR ARROWS: SELECT   ENTER: CONFIRM" : "ENTER OR ESC: BACK";
        textS(hint, (LW - textW(hint, 1)) / 2, LH - 12, 1, new Color(230, 210, 190, hintAlpha));
    }

    private void tick() {
        long now = System.nanoTime();
        double dt = Math.min(.05, (now - last) / 1e9); last = now;
        time += dt;
        if (fadeIn > 0) fadeIn = Math.max(0, fadeIn - dt / .8);
        if (leaving >= 0) onLeaving(dt);
    }

    protected void onLeaving(double dt) {
        leaving += dt / .5;
        if (leaving >= 1) startGame();
    }

    protected void beforeStart() { }

    protected final void startGame() {
        if (launched) return;
        launched = true;
        timer.stop();
        beforeStart();
        Window w = SwingUtilities.getWindowAncestor(this);
        if (!(w instanceof JFrame)) return;
        JFrame f = (JFrame) w;
        RoninVillage game = new RoninVillage(world);
        game.setPreferredSize(getPreferredSize());
        f.setContentPane(game);
        f.revalidate(); f.repaint();
        SwingUtilities.invokeLater(game::requestFocusInWindow);
    }

    //input
    @Override public void keyPressed(KeyEvent e) {
        if (leaving >= 0) return;
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_UP || k == KeyEvent.VK_W) { list.previous(); Audio.playButton(); }
        else if (k == KeyEvent.VK_DOWN || k == KeyEvent.VK_S) { list.next(); Audio.playButton(); }
        else if (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE) { Audio.playButton(); confirm(); }
        else if (k == KeyEvent.VK_ESCAPE) {
            if (screen == SCREEN_CONTROLS) { Audio.playButton(); showScreen(SCREEN_MAIN, 1); }
            else System.exit(0);
        }
    }
    @Override public void keyReleased(KeyEvent e) { }
    @Override public void keyTyped(KeyEvent e) { }

    protected void renderFrame(BufferedImage out) {
        render(out, leaving >= 0 ? Math.min(1, leaving) : fadeIn);
    }

    @Override protected void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        renderFrame(buf);
        Graphics2D g2 = (Graphics2D) gr;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        scale = Math.min(getWidth() / (double) LW, getHeight() / (double) LH);
        int w = (int) (LW * scale), h = (int) (LH * scale);
        offX = (getWidth() - w) / 2; offY = (getHeight() - h) / 2;
        g2.drawImage(buf, offX, offY, w, h, null);
    }

    public void renderState(BufferedImage out, int screen, int selected, double atTime, double fade) {
        timer.stop();
        this.screen = screen; list.reset(items().length, selected); time = atTime;
        render(out, fade);
    }
}
