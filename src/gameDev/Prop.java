package gameDev;


public final class Prop {
    public double x0, y0, x1, y1;
    private final Painter painter;

    public Prop(double x0, double y0, double x1, double y1, Painter painter) {
        this.x0 = x0; this.y0 = y0; this.x1 = x1; this.y1 = y1; this.painter = painter;
    }

    public void paint() { painter.paint(); }
    public void centerOn(double x, double y, double half) { x0 = x - half; x1 = x + half; y0 = y - half; y1 = y + half; }
    public boolean mustPrecede(Prop other) {
        boolean f = x1 <= other.x0 + 1e-6 || y1 <= other.y0 + 1e-6;
        boolean r = other.x1 <= x0 + 1e-6 || other.y1 <= y0 + 1e-6;
        return f && !r;
    }

    public double sortKey() { return x0 + x1 + y0 + y1; }
}
