package gameDev;

public final class Exit {
    public final Zone zone;
    public final AreaId target;
    public final double spawnX, spawnY;
    public final int dir;                       // +1 = leads east, -1 = leads west (also the facing on arrival)
    public final double arrowX, arrowY, arrowZ;

    public Exit(Zone zone, AreaId target, double spawnX, double spawnY, int dir, double arrowX, double arrowY, double arrowZ) {
        this.zone = zone; this.target = target; this.spawnX = spawnX; this.spawnY = spawnY; this.dir = dir;
        this.arrowX = arrowX; this.arrowY = arrowY; this.arrowZ = arrowZ;
    }

    public boolean hasArrow() { return arrowZ >= 0; }
}
