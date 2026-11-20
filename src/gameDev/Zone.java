package gameDev;


public class Zone {
    public final double x0, y0, x1, y1;

    public Zone(double x0, double y0, double x1, double y1) {
        this.x0 = x0; this.y0 = y0; this.x1 = x1; this.y1 = y1;
    }

    public static Zone sized(double x, double y, double w, double d) { return new Zone(x, y, x + w, y + d); }
    public boolean contains(double x, double y) { return x >= x0 && x <= x1 && y >= y0 && y <= y1; }
    public boolean covers(double x, double y) { return x >= x0 && x < x1 && y >= y0 && y < y1; }
    public boolean blocks(double x, double y, double r) { return x > x0 - r && x < x1 + r && y > y0 - r && y < y1 + r; }
}
