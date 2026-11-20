package gameDev;

//the four maps
public enum AreaId {
    ASHEN_VILLAGE, HOLLOW_SHRINE, SHOGUN_CASTLE, THRONE_ROOM;

    public int number() { return ordinal() + 1; }
    public static int count() { return values().length; }
}
