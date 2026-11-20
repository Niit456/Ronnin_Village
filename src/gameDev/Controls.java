package gameDev;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public final class Controls {
    private final Set<Integer> down = new HashSet<>();

    public void press(int keyCode) { down.add(keyCode); }
    public void release(int keyCode) { down.remove(keyCode); }
    public void clear() { down.clear(); }

    private boolean any(int a, int b) { return down.contains(a) || down.contains(b); }

    /** -1 = up (W / UP arrow), 0, +1 = down (S / DOWN arrow) */
    public int dirY() { return (any(KeyEvent.VK_S, KeyEvent.VK_DOWN) ? 1 : 0) - (any(KeyEvent.VK_W, KeyEvent.VK_UP) ? 1 : 0); }
    /** -1 = left (A / LEFT arrow), 0, +1 = right (D / RIGHT arrow) */
    public int dirX() { return (any(KeyEvent.VK_D, KeyEvent.VK_RIGHT) ? 1 : 0) - (any(KeyEvent.VK_A, KeyEvent.VK_LEFT) ? 1 : 0); }
    public boolean running() { return down.contains(KeyEvent.VK_SHIFT); }
}
