package gameDev;

import java.awt.image.BufferedImage;
import static gameDev.Gfx.*;


public final class Game {
	
    static final double TITLE_SECONDS = 3.2;

    private final World world;
    private final Player player;
    private final Controls controls = new Controls();
    private final Camera camera = new Camera();
    private final Transition transition = new Transition();
    private final SceneRenderer renderer = new SceneRenderer();

    private Area current;
    private double titleT = TITLE_SECONDS;
    private double endT = -1;                  

    public Game(World world) {
        this.world = world;
        this.player = world.player();
        this.current = world.start();
        player.reset();
        world.reset();                         
        camera.snapTo(player);
    }

    public Player player() { return player; }
    public Controls controls() { return controls; }
    public Area current() { return current; }
    public boolean isEnding() { return endT >= 0; }
    public double endTime() { return endT; }

    public void dash() { if (endT < 0) player.dash(controls); }
    public void attack() { player.attack(); current.onAttack(); }
    public void onPause() { controls.clear(); player.stopMoving(); }

    public void update(double dt) {
        time += dt;

        if (endT >= 0) {
            endT += dt;
            player.stopMoving();
            player.update(dt);
            return;
        }
        if (titleT > 0) titleT -= dt;

        if (!transition.isActive()) {
            player.move(dt, controls, current);
            for (Exit e : current.exits()) {
                if (e.zone.contains(player.x(), player.y())) { transition.begin(e); player.halt(); break; }
            }
            if (!transition.isActive() && current.goal() != null && current.goal().contains(player.x(), player.y())) {
                endT = 0;
                player.stopMoving();
            }
        } else {
            player.stopMoving();
            if (transition.advance(dt)) enter(transition.exit());
        }
        if (!transition.isActive()) current.update(dt);
        player.update(dt);
        camera.follow(player, dt);
    }

    private void enter(Exit e) {
        current = world.get(e.target);
        player.placeAt(e.spawnX, e.spawnY, e.dir > 0 ? 1 : -1);
        camera.snapTo(player);
        titleT = TITLE_SECONDS;
    }

    public void render(BufferedImage buf) {
        camera.apply();
        renderer.render(buf, current, player, transition.fade(), titleT > 2.4 ? 1 : titleT / 2.4, endT);
    }

    public void renderAt(BufferedImage buf, Area area, double x, double y, int face, double atTime) {
        current = area;
        player.placeAt(x, y, face); player.stopMoving();
        time = atTime;
        camera.snapTo(player); camera.apply();
        renderer.render(buf, area, player, 0, 0, -1);
    }
}