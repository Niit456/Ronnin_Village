package gameDev;

public final class MenuList {
    private int count, selected;

    public MenuList(int count) { this.count = count; }

    public int selected() { return selected; }
    public int count() { return count; }
    public void select(int i) { selected = i; }
    public void next() { selected = (selected + 1) % count; }
    public void previous() { selected = (selected + count - 1) % count; }
    public void reset(int count, int selected) { this.count = count; this.selected = selected; }
}
