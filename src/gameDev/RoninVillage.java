package gameDev;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import static gameDev.Gfx.*;

//games' window panel
public class RoninVillage extends JPanel implements KeyListener, MouseListener, PauseOverlay.Listener {

    static final String MUSIC_FILE = "res/MainMusic.wav";
    static final float MUSIC_DB = -30f;           
    static final float MUSIC_PAUSED_DB = -22f;    

    private final World world;
    private final Game game;
    private final PauseOverlay pause = new PauseOverlay(this);
    private final Audio.Loop music = new Audio.Loop(MUSIC_FILE, MUSIC_DB);
    private final BufferedImage buf = new BufferedImage(LW, LH, BufferedImage.TYPE_INT_RGB);
    private final javax.swing.Timer timer;
    private long last = System.nanoTime();
    private double scale = 1; private int offX, offY;

    public RoninVillage(World world) {
        this.world = world;
        this.game = new Game(world);                   //a fresh run
        setPreferredSize(new Dimension(LW * 3, LH * 3));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                if (!pause.isOpen()) return;
                setCursor(Cursor.getPredefinedCursor(pause.hover(lx(e), ly(e)) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
        music.start();
        timer = new javax.swing.Timer(16, e -> { tick(); repaint(); });
        timer.start();
    }

    private int lx(MouseEvent e) { return (int) ((e.getX() - offX) / scale); }
    private int ly(MouseEvent e) { return (int) ((e.getY() - offY) / scale); }
    //time
    private void tick() {
        long now = System.nanoTime();
        double dt = Math.min(.05, (now - last) / 1e9);
        last = now;
        if (pause.isOpen()) { pause.update(dt); return; }
        game.update(dt);
    }

    @Override protected void paintComponent(Graphics gr) {
        super.paintComponent(gr);
        game.render(buf);
        Graphics2D g2 = (Graphics2D) gr;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        scale = Math.min(getWidth() / (double) LW, getHeight() / (double) LH);
        int w = (int) (LW * scale), h = (int) (LH * scale);
        offX = (getWidth() - w) / 2; offY = (getHeight() - h) / 2;
        g2.drawImage(buf, offX, offY, w, h, null);
        if (pause.isOpen()) pause.draw(g2, offX, offY, w, h);
    }

    //pause
    private void openPause() {
        pause.open(game.current().name);
        music.setGain(MUSIC_PAUSED_DB);
        game.onPause();                               
        last = System.nanoTime();
    }

    @Override public void resume() {
        pause.close();
        music.setGain(MUSIC_DB);
        setCursor(Cursor.getDefaultCursor());
        game.controls().clear();
        last = System.nanoTime();                     
    }

    
    @Override public void mainMenu() { toMenu(); }

    private void toMenu() {
        Window w = SwingUtilities.getWindowAncestor(this);
        if (!(w instanceof JFrame)) return;
        timer.stop();
        music.stop();
        game.controls().clear();
        MainMenu m = new MainMenu(world);
        m.setPreferredSize(getPreferredSize());
        JFrame f = (JFrame) w;
        f.setContentPane(m);
        f.revalidate(); f.repaint();
        SwingUtilities.invokeLater(m::requestFocusInWindow);
        game.player().silence();
    }

    //input
    @Override public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (game.isEnding()) {
            if (game.endTime() > 3 && (k == KeyEvent.VK_ENTER || k == KeyEvent.VK_SPACE || k == KeyEvent.VK_ESCAPE)) toMenu();
            return;
        }
        if (pause.isOpen()) { pause.key(k); return; }
        if (k == KeyEvent.VK_ESCAPE) { openPause(); return; }
        game.controls().press(k);
        if (k == KeyEvent.VK_Q) game.dash();
    }
    @Override public void keyReleased(KeyEvent e) { game.controls().release(e.getKeyCode()); }
    @Override public void keyTyped(KeyEvent e) { }

    @Override public void mousePressed(MouseEvent e) {
        if (!pause.isOpen() && e.getButton() == MouseEvent.BUTTON1) game.attack();
    }
    @Override public void mouseClicked(MouseEvent e) { if (pause.isOpen()) pause.click(lx(e), ly(e)); }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }

    //entry points
    private static final Object[][] SHOTS = {
        {AreaId.ASHEN_VILLAGE, 8.0, 16.4, 1, "area1_ashen_village.png"},
        {AreaId.HOLLOW_SHRINE, 14.4, 15.2, 1, "area2_hollow_shrine.png"},
    };

    static void screenshots(World world, String dir) throws Exception {
        Game game = new Game(world);
        for (Object[] s : SHOTS) {
            BufferedImage lo = new BufferedImage(LW, LH, BufferedImage.TYPE_INT_RGB);
            game.renderAt(lo, world.get((AreaId) s[0]), (Double) s[1], (Double) s[2], (Integer) s[3], 1.4);
            BufferedImage big = new BufferedImage(LW * 3, LH * 3, BufferedImage.TYPE_INT_RGB);
            Graphics2D bg = big.createGraphics();
            bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            bg.drawImage(lo, 0, 0, LW * 3, LH * 3, null); bg.dispose();
            ImageIO.write(big, "png", new File(dir, (String) s[4]));
            System.out.println("Wrote " + s[4]);
        }
    }

    public static void main(String[] args) throws Exception {
        World world = new World();
        if (args.length > 0 && args[0].equals("--screenshots")) { screenshots(world, args.length > 1 ? args[1] : "."); return; }
        SwingUtilities.invokeLater(() -> {
            Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
            int sc = (int) Math.max(1, Math.min(4, Math.floor(Math.min(scr.width * .92 / LW, scr.height * .86 / LH))));
            JFrame f = new JFrame("Ronin Village");
            RoninVillage p = new RoninVillage(world);
            p.setPreferredSize(new Dimension(LW * sc, LH * sc));
            f.setContentPane(p);
            f.pack(); f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
            p.requestFocusInWindow();
        });
    }
}
