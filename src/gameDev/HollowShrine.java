package gameDev;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import static gameDev.Gfx.*;
import static gameDev.Painters.*;
import static gameDev.PixelFont.*;

public class HollowShrine extends Area {
	
	private static final double ATTACK_COOLDOWN = .42;
	private final Player player;
	private final List<Skeleton> skeletons = new ArrayList<>();
	private final List<ItemDrop> drops = new ArrayList<>();
	private final List<SkeletonArcher> archers = new ArrayList<>();
	private final List<ArrowProjectile> arrows = new ArrayList<>();
	List<double[]> usedSpots = new ArrayList<>();
	private final Random rng = new Random();
	private double atkCd, deadT = -1;

    public HollowShrine(Player player) {
    	
    	super(AreaId.HOLLOW_SHRINE, "Hollow Shrine", "THE SHRINE REMEMBERS NO ONE", 23);
        this.player = player;
        
                dirt = new Color[]{col(0x3a3d3a), col(0x434640), col(0x30332f), col(0x4b4e47)};
        pathColor = col(0x6a695f); grass = col(0x3f7a66);
        path(0, 16, 16, 16, 1.5);
        path(16, 16, 16, 9, 1.3);
        path(16, 16, 32, 16, 1.3);
        patch(19, 20, 11, 10, 0x2a2826);
        disc(16, 11.2, 3.6, 0x6f6d64);
        water(3, 22, 7, 6);
        edgeExit(new Zone(0, 13, 1.7, 19), AreaId.ASHEN_VILLAGE, MAP - 3.8, 16, -1);
        gate(2.6, 16, 6.6, false, true);           // west exit gate (half collapsed)

        solid(12, 2.5, 8, 6.5); prop(12, 2.5, 20, 10, () -> drawShrine(12, 2.5, 8, 6.5));
        solid(13.8, 9.1, .8, .8); solid(17.5, 9.1, .8, .8);   // the two stone guardian lions in front of the steps
        gate(16, 12.4, 6, true, true);
        solid(24.5, 11, 2, 2); prop(24.5, 11, 27.5, 13.8, () -> drawStatue(24.5, 11));
        solid(25, 3, 2.6, 2.6); prop(25, 3, 28.2, 5.6, () -> drawTower(25, 3, 2.6, 46));
        solid(3, 22, 7, 6);
        for (double[] r : new double[][]{{3.6, 21.8}, {6.2, 21.7}, {9.4, 21.9}, {2.8, 24.5}, {10.3, 26}}) prop(r[0] - .2, r[1] - .2, r[0] + .2, r[1] + .2, () -> drawReeds(r[0], r[1]));
        for (double[] t : new double[][]{{9, 14.4}, {9, 17.6}, {13, 14.4}, {13, 17.6}, {19.5, 14.4}, {19.5, 17.6}, {12.4, 10.6}, {19.6, 10.6}}) toro(t[0], t[1], 0x6af0d4);
        solid(6.4, 12.4, 1.2, 1.2); prop(6.4, 12.4, 7.6, 13.6, () -> drawFire(7, 13, true));
        banner(11.2, 11.4, 0x2a5a60); banner(20.8, 11.4, 0x2a5a60);
        // graveyard
        for (int j = 0; j < 4; j++) for (int i = 0; i < 5; i++) {
            if (hash(i, j, 99) < .15) continue;
            final double tx = 20.5 + 2.1 * i, ty = 21.2 + 2.2 * j;
            final int th = 6 + (int) (hash(i, j, 5) * 5);
            final boolean cross = hash(i, j, 6) > .65;
            solid(tx, ty, .7, .3); prop(tx - .15, ty, tx + .85, ty + .35, () -> drawTomb(tx, ty, th, cross));
        }
        fence(19, 19.6, 22, true);
        for (double[] w : new double[][]{{22, 24.5, 5}, {26.5, 27, 4}, {23.5, 29, 6}}) wisp(w[0], w[1], w[2]);
        for (double[] w : new double[][]{{6, 15, 12}, {18, 6, 20}}) wisp(w[0], w[1], w[2]);
        double[][] ts = {  // x, y, height, seed. (a tree used to stand at 29.5, 17.8 - it is gone: the east gate is there now)
                {3, 4, 120, 7}, {6, 8.5, 105, 11}, {9, 3, 125, 15}, {2.5, 10.5, 100, 19}, {28.8, 8.6, 115, 23}, {28.4, 22, 110, 31}, {12.6, 24, 105, 35},
                {14.8, 29, 120, 39}, {1.8, 29.6, 100, 43}, {30, 2, 130, 47}, {22.8, 7.6, 90, 51}, {1.6, 20, 95, 55}, {16.5, 26.6, 85, 59}, {10.5, 7.5, 85, 63}};
        for (double[] t : ts) tree(t[0], t[1], (int) t[2], (int) t[3]);
        rock(7.8, 6.8, 8); rock(27.6, 20.4, 7); rock(11.2, 29.4, 9); rock(3.4, 18.4, 6);
        // the east gate leads to the Shogun Castle
        gate(MAP - 2.6, 16, 6.6, false, false);
        edgeExit(new Zone(MAP - 1.7, 13, MAP, 19), AreaId.SHOGUN_CASTLE, 3.8, 16, 1);
        
        spawnSkeletons();
    
    }
    
    private void spawnSkeletons() {
        int count = 5 + rng.nextInt(4); // randomly picks 5, 6, 7, or 8 skeletons

        // Ensure we have enough Skeleton instances in the list
        while (skeletons.size() < count) {
            Skeleton sk = new Skeleton(this, player, 16, 16, this::hurtPlayer);
            sk.setDeathListener((dx, dy) -> drops.add(new ItemDrop(this, dx, dy)));
            skeletons.add(sk);
        }
        
        List<double[]> usedSpots = new ArrayList<>();
        for (int i = 0; i < skeletons.size(); i++) {
            Skeleton s = skeletons.get(i);
            if (i >= count) {
                // Hide any extra skeletons from a previous run with a larger count
                s.setSpawn(-50, -50);
                s.reset();
                while (!s.isDead()) s.damage();
                continue;
            }
            double[] spot = pickGraveyardOrShrineSpot(usedSpots);
            
            usedSpots.add(spot);
            s.setSpawn(spot[0], spot[1]);
            s.reset();
        }
        
        int archerCount = 4 + rng.nextInt(3); // randomly picks 4, 5, or 6 archers
        while (archers.size() < archerCount) {
            SkeletonArcher ar = new SkeletonArcher(
                this, player, 16, 16,
                (fx, fy, tx, ty) -> arrows.add(new ArrowProjectile(this, fx, fy, tx, ty)),
                (dx, dy) -> drops.add(new ItemDrop(this, dx, dy))
            );
            archers.add(ar);
        }
        for (int i = 0; i < archers.size(); i++) {
            SkeletonArcher ar = archers.get(i);
            if (i >= archerCount) {
                ar.setSpawn(-50, -50);
                ar.reset();
                while (!ar.isDead()) ar.damage();
                continue;
            }
            double[] spot = pickGraveyardOrShrineSpot(usedSpots);
            usedSpots.add(spot);
            ar.setSpawn(spot[0], spot[1]);
            ar.reset();
        }
    }
    
    private double[] pickGraveyardOrShrineSpot(List<double[]> usedSpots) {
        for (int tries = 0; tries < 250; tries++) {
            double rx, ry;
            if (rng.nextDouble() < 0.65) {
                // 65% chance: Graveyard & east woods (x: 18..29, y: 12..29)
                rx = 18.0 + rng.nextDouble() * 11.0;
                ry = 12.0 + rng.nextDouble() * 17.0;
            } else {
                // 35% chance: Shrine courtyard grounds (x: 7..25, y: 9..20)
                rx = 7.0 + rng.nextDouble() * 18.0;
                ry = 9.0 + rng.nextDouble() * 11.0;
            }

            if (blocked(rx, ry)) continue;
            if (Math.hypot(rx - 3.8, ry - 16.0) < 6.5) continue; // keep away from west entrance

            // Prevent skeletons from stacking on top of each other
            boolean tooClose = false;
            for (double[] other : usedSpots) {
                if (Math.hypot(rx - other[0], ry - other[1]) < 1.8) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) return new double[]{rx, ry};
        }
        return new double[]{21.0, 21.0}; // safe graveyard fallback
    }

    @Override public void reset() {
    	
    	for (ItemDrop d : drops) {
            // Mark collected so any uncollected drops from a previous run vanish
            while (!d.isCollected()) d.update(10, player);
        }
        drops.clear();
    	
        deadT = -1;
        atkCd = 0;
        spawnSkeletons();
        
        for (ArrowProjectile a : arrows) a.expire();
        arrows.clear();
        
    }

    @Override public void update(double dt) {
    	
    	for (int i = drops.size() - 1; i >= 0; i--) {
            ItemDrop d = drops.get(i);
            if (d.update(dt, player)) {
                player.heal(1);       // restores 1 heart on pickup
                player.addPotion();   // increments collected item counter
                drops.remove(i);
            }
        }
    	
        if (atkCd > 0) atkCd -= dt;
        if (deadT >= 0) {
            deadT += dt;
            player.setFrozen(true);
            if (deadT > 2.2) {
                player.placeAt(3.8, 16, 1);
                player.restoreHealth();
                player.setFrozen(false);
                player.setInvulnerable(1.5);
                reset();
            }
            return;
        }
        
        for (Skeleton s : skeletons) {
            s.update(dt);
        }
        
        for (SkeletonArcher ar : archers) {
            ar.update(dt);
        }
        
        for (int i = arrows.size() - 1; i >= 0; i--) {
            ArrowProjectile a = arrows.get(i);
            if (a.update(dt, this, player)) {
                hurtPlayer(a.x(), a.y());
            }
            if (!a.isActive()) arrows.remove(i);
        }
    }

    private void hurtPlayer(double fx, double fy) {
        if (player.takeHit(this, fx, fy) && player.isDead()) deadT = 0;
    }

    @Override public void onAttack() {
        if (deadT >= 0 || atkCd > 0) return;
        boolean hitAny = false;
        for (Skeleton s : skeletons) {
            if (s.canBeHit()) {
                s.damage();
                hitAny = true;
            }
        }
        
        for (SkeletonArcher ar : archers) {
            if (ar.canBeHit()) {
                ar.damage();
                hitAny = true;
            }
        }
        
        if (hitAny) atkCd = ATTACK_COOLDOWN;
    }

    @Override public void drawHud() {
        // Player health hearts
        for (int i = 0; i < Player.MAX_HP; i++) {
            int x = 8 + i * 11, y = 30;
            for (int r = 0; r < ThroneRoom.HEART.length; r++) {
                for (int c = 0; c < 7; c++) {
                    if (ThroneRoom.HEART[r].charAt(c) == 'X') {
                        g.setColor(i < player.hp() ? (r < 2 && c < 3 ? col(0xff6a6a) : col(0xd8262e)) : col(0x3a2a30));
                        g.fillRect(x + c, y + r, 1, 1);
                    }
                }
            }
        }
        // Individual skeleton health bars
        for (Skeleton s : skeletons) {
            s.drawBar();
        }
        
        if (ItemDrop.sprite() != null) {
            g.drawImage(ItemDrop.sprite(), 8, 40, 10, 12, null);
        }
        textS("x" + player.potions(), 21, 43, 1, col(0xf0d9b8));
        
        // Death fade overlay if killed by skeletons
        if (deadT >= 0) {
            double a = Math.min(1, deadT / 1.2);
            g.setColor(new Color(0, 0, 0, (int) (200 * a)));
            g.fillRect(0, 0, LW, LH);
            if (deadT > 0.6) {
                String msg = "YOU DIED";
                textS(msg, (LW - textW(msg, 6)) / 2, 100, 6, new Color(200, 36, 40, (int) (255 * Math.min(1, (deadT - 0.6) / 0.6))));
            }
        }
        
        //Skeleton Archer HUDS
        for (SkeletonArcher ar : archers) {
            ar.drawBar();
        }
    }

    static void drawTomb(double x, double y, int h, boolean cross) {
        fillq(new Color(0, 0, 0, 50), x, y, 0, x + .9, y + .1, 0, x + .9, y + .55, 0, x + .1, y + .55, 0);
        Color st = col(0x8d8b8a);
        box(x, y, .7, .3, 0, h, st);
        if (cross) { box(x - .15, y - .01, 1.0, .3, h * .6, 3, st); }
        else box(x + .05, y, .6, .3, h, 2, shade(st, 1.1));
        int mx = sx(x + .3, y + .3), my = sy(x + .3, y + .3, h * .4);
        g.setColor(col(0x3f6a4a)); g.fillRect(mx - 3, my, 3, 2); g.fillRect(mx + 1, my + 2, 2, 2);
    }

    static void drawStatue(double x, double y) {
        fillq(new Color(0, 0, 0, 60), x, y, 0, x + 2.8, y + .2, 0, x + 2.8, y + 2.8, 0, x + .2, y + 2.8, 0);
        Color st = col(0x85827f);
        box(x, y, 2, 2, 0, 6, st);
        box(x + .15, y + .15, 1.7, 1.7, 6, 2, shade(st, 1.1));
        int cx = sx(x + 1, y + 1), cy = sy(x + 1, y + 1, 8);
        int[] xs = {cx - 11, cx - 9, cx - 5, cx - 3, cx + 4, cx + 6, cx + 10, cx + 12};
        int[] ys = {cy, cy - 18, cy - 28, cy - 33, cy - 31, cy - 25, cy - 16, cy};
        g.setColor(shade(st, .85)); g.fillPolygon(xs, ys, xs.length);
        g.setColor(shade(st, 1.15)); g.fillPolygon(new int[]{cx - 11, cx - 9, cx - 5, cx - 4, cx - 6}, new int[]{cy, cy - 18, cy - 28, cy - 20, cy}, 5);
        g.setColor(shade(st, .55)); g.drawPolygon(xs, ys, xs.length);
        g.drawLine(cx - 6, cy - 14, cx + 7, cy - 12); g.drawLine(cx - 4, cy - 10, cx + 8, cy - 8);
        g.drawLine(cx, cy - 33, cx + 1, cy - 28); // broken neck
        g.setColor(col(0x3f6a4a)); g.fillRect(cx - 9, cy - 6, 3, 2); g.fillRect(cx + 5, cy - 10, 2, 3);
        g.setColor(col(0x6f6c6a)); g.fillOval(sx(x + 2.3, y + 1.6) - 5, sy(x + 2.3, y + 1.6, 0) - 6, 10, 7);
    }

    static void drawTower(double x, double y, double s, double hgt) {
        fillq(new Color(0, 0, 0, 60), x, y, 0, x + s + .6, y + .2, 0, x + s + .6, y + s + .6, 0, x + .2, y + s + .6, 0);
        Color wood = col(0x52402e), wd = shade(wood, .8);
        double p = .35;
        box(x, y, p, p, 0, hgt, wood);
        box(x + s - p, y, p, p, 0, hgt - 6, wood);
        box(x, y + s - p, p, p, 0, hgt, wood);
        box(x + s - p, y + s - p, p, p, 0, hgt - 3, wood);
        ln(wd, x, y + s, 3, x + s, y + s, hgt - 8); ln(wd, x + s, y + s, 3, x, y + s, hgt - 8);
        ln(wd, x + s, y, 3, x + s, y + s, hgt - 8); ln(wd, x + s, y + s, 3, x + s, y, hgt - 8);
        box(x - .3, y - .3, s + .6, s + .6, hgt - 4, 2, shade(wood, 1.15));
        // broken railing
        box(x - .25, y + s + .05, .15, .15, hgt - 2, 7, wood);
        box(x + s * .5, y + s + .1, .15, .15, hgt - 2, 4, wood);
        box(x + s + .1, y - .2, .15, .15, hgt - 2, 8, wood);
        // ladder on left face
        double lx = x + s * .55;
        ln(wd, lx, y + s + .4, 0, lx, y + s + .1, hgt - 4); ln(wd, lx + .6, y + s + .4, 0, lx + .6, y + s + .1, hgt - 4);
        for (int i = 1; i < 8; i++) { double z = i * (hgt - 8) / 8.0; if (i != 5) ln(col(0x6a5238), lx, y + s + .35, z, lx + .6, y + s + .35, z); }
        // tattered cloth
        int cx = sx(x + s, y + s), cy = sy(x + s, y + s, hgt - 6);
        g.setColor(col(0x7a2628)); g.fillRect(cx - 2, cy, 5, 10); g.fillRect(cx - 1, cy + 10, 3, 3);
    }

    static void drawReeds(double x, double y) {
        int bx = sx(x, y), by = sy(x, y, 0);
        g.setColor(col(0x4a5a3a));
        for (int i = -3; i <= 3; i += 2) g.drawLine(bx + i, by, bx + i + (i < 0 ? -1 : 1), by - 8 - (i & 3));
        g.setColor(col(0x6b4a2c)); g.fillRect(bx - 1, by - 11, 2, 3);
    }

    static void drawKomainu(double x, double y, boolean headless, boolean mirror) {
        Color st = col(0x8a867f), dk = shade(st, .62), moss = col(0x4a5a30);
        box(x - .08, y - .08, .76, .76, 0, 2.2, shade(st, .85));                    // pedestal
        box(x + .1, y + .12, .4, .4, 2.2, 2.3, st);                                   // body
        box(x + .06, y + .08, .48, .16, 2.2, 1.0, dk);                                // haunch
        if (!headless) {
            box(x + .12, y + .16, .36, .34, 4.5, 1.7, shade(st, 1.08));                 // head
            box(x + .2, y + .48, .2, .1, 4.6, .8, dk);                                    // muzzle / open mouth
            box(x + .1, y + .2, .09, .1, 6.1, .9, st); box(x + .41, y + .2, .09, .1, 6.1, .9, st);   // ears
            ln(dk, x + .2, y + .5, 5.6, x + .4, y + .5, 5.6);
        } else box(x + .75, y + .5, .35, .3, 0, 1.4, shade(st, 1.05));                  // the head lies on the ground
        ln(dk, x + .12, y + .54, 3.0, x + .12, y + .54, 4.4); ln(dk, x + .48, y + .54, 3.0, x + .48, y + .54, 4.4);   // mane / chest lines
        g.setColor(moss); g.fillRect(sx(x + .1, y + .7), sy(x + .1, y + .7, 1.6), 4, 1); g.fillRect(sx(x + .5, y + .2), sy(x + .5, y + .2, 4.2), 2, 1);
    }

    static void drawShrine(double x, double y, double w, double d) {
        int sd = 23;
        double cx = x + w / 2;
        jShadow(x, y, w, d);
        Color stone = col(0x8a867f), vermil = col(0x9a3a32), wood = col(0x6a503c);
        double z0 = 4.5, zE = z0 + 34;
        //stone platform with a lighter base course, cracks and moss
        box(x, y, w, d, 0, 2.5, shade(stone, .9));
        box(x + .12, y + .12, w - .24, d - .24, 2.5, 2.0, stone);
        for (int i = 1; i < 6; i++) { double a = x + w * i / 6.0; ln(shade(stone, .6), a, y + d, 0, a, y + d, 2.5); }
        ln(shade(stone, 1.25), x + .12, y + d - .12, 4.5, x + w - .12, y + d - .12, 4.5);
        for (int i = 0; i < 7; i++) if (hash(i, sd, 101) < .8) { double a = x + .3 + hash(i, sd, 102) * (w - 1); fillq(col(0x4a5a30), a, y + d - .45, 4.52, a + .5 + hash(i, sd, 103) * .6, y + d - .45, 4.52, a + .5, y + d - .12, 4.52, a, y + d - .12, 4.52); }
        //three steps up to the platform
        double sx0 = cx - 1.1, sw = 2.2;
        box(sx0, y + d + .64, sw, .36, 0, 1.7, shade(stone, 1.05));
        box(sx0, y + d + .3, sw, .36, 0, 3.2, stone);
        ln(shade(stone, 1.3), sx0, y + d + 1, 1.7, sx0 + sw, y + d + 1, 1.7);
        ln(shade(stone, 1.3), sx0, y + d + .66, 3.2, sx0 + sw, y + d + .66, 3.2);
        drawKomainu(sx0 - 1.0, y + d + .2, false, false);
        drawKomainu(sx0 + sw + .42, y + d + .2, true, true);
        //the hall: pillar rectangle X0..X1 x Y0..Y1, walls set back from the colonnade
        double X0 = x + .95, X1 = x + w - .95, Y0 = y + 1.0, Y1 = y + d - .55;
        double hx = X0, hy = Y0, hw = (X1 - X0) - .45, bd = (Y1 - Y0) - .55;
        double pa = X0 + (X1 - X0) / 3, pb = X0 + 2 * (X1 - X0) / 3;
        double ua = (pa - hx) / hw, ub = (pb - hx) / hw;
        for (int f = 0; f < 2; f++) {
            boolean front = f == 0; double lum = front ? 1.0 : .66;
            jFace(front, hx, hy, hw, bd);
            double L = jL();
            jq(0, 1, z0, zE, shade(wood, lum));
            int np = (int) Math.round(L / .35);
            for (int i = 1; i < np; i++) { double u = (double) i / np; jl(shade(wood, .62 * lum), u, z0, u, zE); }
            for (int i = 0; i < 5; i++) { double u = hash(i, sd + f, 111), len = 4 + hash(i, sd + f, 112) * 12; jl(shade(col(0x1c140e), lum), u, zE - 1, u, zE - 1 - len); }
            if (front) {
                jLattice(.02, ua - .02, z0 + 3, zE - 10, lum, null);                    // lattice doors (koshi-do)
                jLattice(ub + .02, .98, z0 + 3, zE - 10, lum, null);
                jq(ua, ub, z0, zE - 8, J_DARK);                                          // the open doorway
                jq(ua + .01, ub - .01, z0, z0 + 12, col(0x0d1d1c));
                jq(ua - .012, ua, z0, zE - 8, shade(vermil, .8)); jq(ub, ub + .012, z0, zE - 8, shade(vermil, .8));
            } else {
                jLattice(.2, .42, z0 + 5, zE - 10, lum, null); jLattice(.58, .8, z0 + 5, zE - 10, lum, null);
                jq(.46, .54, z0, zE - 12, J_DARK);
            }
            jq(0, 1, zE - 9.5, zE - 7.8, shade(vermil, lum));                           // tie beam (nuki)
            jq(0, 1, zE - 6.8, zE - 1.8, shade(wood, .8 * lum));                          // transom
            for (int i = 1; i < np * 2 / 3; i++) { double u = (double) i / (np * 2 / 3); jl(shade(J_DARK, 1), u, zE - 6.6, u, zE - 2); }
            jq(0, 1, zE - 1.8, zE, shade(vermil, .9 * lum));
            jpost(0, .26, z0, zE, shade(vermil, lum)); jpost(1, .26, z0, zE, shade(vermil, lum));
            for (int i = 0; i < 4; i++) if (hash(i, sd + f, 113) < .5) { double a = hash(i, sd + f, 114) * .8; jq(a, a + .1, z0 + 2 + hash(i, sd + f, 115) * 10, z0 + 6 + hash(i, sd + f, 116) * 14, shade(col(0x2a1e17), lum)); }   // missing boards
        }
        light(cx, Y1 + .1, z0 + 8, 34, 0x3de0c8);
        //colonnade pillars (back to front), vermilion paint peeling
        java.util.List<double[]> pil = new ArrayList<>();
        for (int i = 0; i < 4; i++) pil.add(new double[]{X0 + (X1 - X0) * i / 3.0, Y1});
        for (int i = 0; i < 3; i++) pil.add(new double[]{X1, Y0 + (Y1 - Y0) * i / 3.0});
        pil.sort((p, q) -> Double.compare(p[0] + p[1], q[0] + q[1]));
        for (int i = 0; i < pil.size(); i++) {
            double[] p = pil.get(i);
            box(p[0] - .24, p[1] - .24, .48, .48, z0, 1.3, shade(stone, .8));                      // stone footing
            Color pc = hash(i, sd, 121) < .35 ? mix(vermil, col(0x4a3a40), .45) : vermil;
            box(p[0] - .17, p[1] - .17, .34, .34, z0 + 1.3, zE - z0 - 1.5, pc);
            ln(shade(pc, 1.3), p[0] - .17, p[1] + .17, z0 + 4, p[0] - .17, p[1] + .17, z0 + 4 + 6 + hash(i, sd, 122) * 8);
        }
        for (int i = 0; i < 3; i++) {                                                                 // tie beams between the front pillars
            double a = X0 + (X1 - X0) * i / 3.0, b = X0 + (X1 - X0) * (i + 1) / 3.0;
            box(a, Y1 - .11, b - a, .22, zE - 9.5, 1.7, vermil);
        }
        for (int i = 0; i < 2; i++) { double a = Y0 + (Y1 - Y0) * i / 3.0, b = Y0 + (Y1 - Y0) * (i + 1) / 3.0; box(X1 - .11, a, .22, b - a, zE - 9.5, 1.7, shade(vermil, .9)); }
        //shimenawa: straw rope with paper streamers (shide) across the doorway, and the bell
        double ra = pa, rb = pb, zr = zE - 12;
        Color straw = col(0xcdbb90);
        for (int i = 0; i < 10; i++) {
            double p0 = ra + (rb - ra) * i / 10, p1 = ra + (rb - ra) * (i + 1) / 10;
            double za = zr - 2.2 * Math.sin(Math.PI * i / 10), zb2 = zr - 2.2 * Math.sin(Math.PI * (i + 1) / 10);
            ln(straw, p0, Y1 + .12, za, p1, Y1 + .12, zb2); ln(shade(straw, .75), p0, Y1 + .12, za - 1, p1, Y1 + .12, zb2 - 1); ln(shade(straw, 1.1), p0, Y1 + .12, za + 1, p1, Y1 + .12, zb2 + 1);
        }
        for (int i = 1; i < 6; i++) if (hash(i, sd, 131) < .75) {
            double px = ra + (rb - ra) * i / 6, zt = zr - 2.2 * Math.sin(Math.PI * i / 6) - 1;
            Color pc = col(0xd8d4c4);
            ln(pc, px, Y1 + .12, zt, px + .06, Y1 + .12, zt - 3); ln(pc, px + .06, Y1 + .12, zt - 3, px - .06, Y1 + .12, zt - 5); ln(pc, px - .06, Y1 + .12, zt - 5, px + .06, Y1 + .12, zt - 7);
        }
        g.setColor(col(0xa88a3a)); g.fillOval(sx(cx, Y1 + .3) - 3, sy(cx, Y1 + .3, zr - 4) - 2, 6, 5);
        g.setColor(col(0x6b5420)); g.fillRect(sx(cx, Y1 + .3) - 1, sy(cx, Y1 + .3, zr - 4) + 1, 2, 1);
        for (int i = 0; i < 9; i++) ln(i % 2 == 0 ? col(0xb03a34) : col(0xd8d4c4), cx, Y1 + .3, zr - 6 - i * 1.1, cx, Y1 + .3, zr - 7 - i * 1.1);
        //offering box (saisen-bako)
        box(cx - .75, Y1 + .1, 1.5, .5, z0, 3.4, col(0x3a2a20));
        for (int i = 1; i < 8; i++) ln(col(0x1f1611), cx - .75 + 1.5 * i / 8, Y1 + .6, z0, cx - .75 + 1.5 * i / 8, Y1 + .6, z0 + 3.4);
        //red railings (koran) with onion-cap posts (giboshi), broken in places
        java.util.List<double[]> rp = new ArrayList<>();
        for (double a = x + .3; a <= sx0 - .5; a += 1.5) rp.add(new double[]{a, y + d - .16, 0});
        for (double a = sx0 + sw + .6; a <= x + w - .3; a += 1.5) rp.add(new double[]{a, y + d - .16, 0});
        for (double b = y + 1.2; b < y + d - .3; b += 1.6) rp.add(new double[]{x + w - .16, b, 1});
        rp.sort((p, q) -> Double.compare(p[0] + p[1], q[0] + q[1]));
        for (int i = 0; i < rp.size(); i++) {
            double[] p = rp.get(i);
            boolean gone = hash(i, sd, 141) < .18;
            if (!gone) {
                box(p[0] - .1, p[1] - .1, .2, .2, z0, 7, vermil);
                box(p[0] - .13, p[1] - .13, .26, .26, z0 + 7, 1.3, shade(vermil, 1.2));
                box(p[0] - .08, p[1] - .08, .16, .16, z0 + 8.3, 1.2, col(0xc9b27a));
            }
            for (int j = i - 1; j >= 0; j--) {
                double[] q = rp.get(j);
                if (q[2] != p[2] || Math.hypot(q[0] - p[0], q[1] - p[1]) > 1.8) continue;
                if (!gone && hash(i, sd, 142) < .8) { ln(vermil, q[0], q[1], z0 + 5, p[0], p[1], z0 + 5); ln(vermil, q[0], q[1], z0 + 5.6, p[0], p[1], z0 + 5.6); ln(shade(vermil, .8), q[0], q[1], z0 + 2.6, p[0], p[1], z0 + 2.6); }
                break;
            }
        }
        //the roof: curved, corners flicked up, partly caved in
        Color tile = col(0x5b8f82);                                                   // copper roof gone green with verdigris
        RC = 1.7; RUP = 3.4; RSHR = true;
        jRoof(X0, Y0, X1 - X0, Y1 - Y0, zE, 18, .65, true, false, tile, wood, sd, 3);
        //vermilion fascia board under the front and right eave
        for (int i = 0; i < 8; i++) { double[] p = rrF(i / 8.0, 0), q = rrF((i + 1) / 8.0, 0), a = rrS(i / 8.0, 0), b = rrS((i + 1) / 8.0, 0);
            quad(vermil, p[0], p[1], p[2] - 3.2, q[0], q[1], q[2] - 3.2, q[0], q[1], q[2] - 5.2, p[0], p[1], p[2] - 5.2);
            quad(shade(vermil, .6), a[0], a[1], a[2] - 3.2, b[0], b[1], b[2] - 3.2, b[0], b[1], b[2] - 5.2, a[0], a[1], a[2] - 5.2); }
        //curved entrance gable (karahafu) set on the front slope
        double td = .24;
        double[] bp = rrF(.5, td); double yd = bp[1], zd = bp[2] - .5, zr2 = zd + 11, hwid = 1.45;
        double ym2 = yd; for (double t = td; t <= 1; t += .02) { double[] q = rrF(.5, t); ym2 = q[1]; if (q[2] >= zr2) break; }
        rpoly(shade(tile, .72), new double[]{cx + hwid, yd, zd}, new double[]{cx, yd, zr2}, new double[]{cx, ym2, zr2}, new double[]{cx + hwid, ym2, zr2});
        rpoly(shade(tile, 1.0), new double[]{cx - hwid, yd, zd}, new double[]{cx, yd, zr2}, new double[]{cx, ym2, zr2}, new double[]{cx - hwid, ym2, zr2});
        double[][] kc = {{1.45, 0}, {1.3, 2.4}, {.95, 5.2}, {.58, 7.3}, {.28, 9.0}, {0, 10.6}};
        double[] kp = new double[kc.length * 6];
        for (int i = 0; i < kc.length; i++) { kp[3 * i] = cx + kc[i][0]; kp[3 * i + 1] = yd; kp[3 * i + 2] = zd + kc[i][1]; }
        for (int i = 0; i < kc.length; i++) { int j = kc.length + i, k = kc.length - 1 - i; kp[3 * j] = cx - kc[k][0]; kp[3 * j + 1] = yd; kp[3 * j + 2] = zd + kc[k][1]; }
        quad(col(0x6a2a26), kp);
        for (int i = 0; i + 1 < kc.length; i++) { ln(col(0xc9b27a), cx + kc[i][0], yd, zd + kc[i][1], cx + kc[i + 1][0], yd, zd + kc[i + 1][1]); ln(col(0xc9b27a), cx - kc[i][0], yd, zd + kc[i][1], cx - kc[i + 1][0], yd, zd + kc[i + 1][1]); }
        for (int i = 1; i < 6; i++) ln(col(0x3a1a18), cx - 1.2 + i * .4, yd, zd + 1, cx - 1.2 + i * .4, yd, zd + 5.5);
        g.setColor(col(0xd0b45a)); g.fillOval(sx(cx, yd) - 3, sy(cx, yd, zd + 6.5) - 3, 6, 6);
        g.setColor(col(0x3de0c8)); g.fillRect(sx(cx, yd) - 1, sy(cx, yd, zd + 6.5) - 1, 2, 2);
        //inner sanctum (honden): small drum + second hipped roof on top of the ridge
        double zt = zE + 18, ym = RYM, dx0 = cx - 1.9;
        box(dx0, ym - 1.2, 3.8, 2.4, zt - 3, 11, wood);
        jFace(true, dx0, ym - 1.2, 3.8, 2.4);
        jpost(0, .24, zt - 3, zt + 8, vermil); jpost(.5, .22, zt - 3, zt + 8, vermil); jpost(1, .24, zt - 3, zt + 8, vermil);
        jLattice(.1, .45, zt, zt + 5.5, 1.0, col(0x1d6e62)); jLattice(.55, .9, zt, zt + 5.5, 1.0, null);
        jq(0, 1, zt + 6.2, zt + 7.8, vermil);
        RC = 1.5; RUP = 2.2;
        jRoof(dx0, ym - 1.2, 3.8, 2.4, zt + 8, 12, .7, true, false, shade(tile, 1.05), wood, sd + 9, 0);
        RC = 1; RUP = 0; RSHR = false;
        //vines hanging off the eaves
        Color vine = col(0x3f5a30);
        for (int i = 0; i < 7; i++) {
            double vx = X0 + .3 + hash(i, sd, 151) * (X1 - X0 - .6), vy = Y1 + .85, vl = 5 + hash(i, sd, 152) * 12;
            ln(vine, vx, vy, zE - 2, vx + .04, vy, zE - 2 - vl); ln(shade(vine, 1.3), vx + .04, vy, zE - 2 - vl * .5, vx + .09, vy, zE - 2 - vl * .5 - 2);
        }
    }
}
