package gameDev;

import java.util.EnumMap;
import java.util.Map;

public final class World {
    private final Map<AreaId, Area> areas = new EnumMap<>(AreaId.class);
    private final Player player = new Player();       //the one ronin that walks through all the maps

    public World() {
        add(new AshenVillage());
        add(new HollowShrine(player)); // pass player here
        add(new ShogunCastle());
        add(new ThroneRoom(player));
        for (Area a : areas.values()) a.bakeGround();
    }

    private void add(Area a) { areas.put(a.id, a); }

    public Player player() { return player; }
    public Area get(AreaId id) { return areas.get(id); }
    public Area start() { return areas.get(AreaId.ASHEN_VILLAGE); }

    //new run
    public void reset() { for (Area a : areas.values()) a.reset(); }
}
