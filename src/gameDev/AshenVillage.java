package gameDev;

import java.awt.*;
import java.util.*;
import java.util.List;
import static gameDev.Gfx.*;
import static gameDev.Painters.*;


public class AshenVillage extends Area {
    static final double HOUSE_H = 1.4;   

    public AshenVillage() {
        super(AreaId.ASHEN_VILLAGE, "Ashen Village", "A RONIN WANDERS THE ASHES", 11);
        dirt = new Color[]{col(0x4a3d36), col(0x544539), col(0x3f342f), col(0x5d4c3f)};
        pathColor = col(0x76634f); grass = col(0x6a6a34);
        path(0, 16, 32, 16, 1.5);
        path(13, 16, 13, 4, 1.2);
        path(13, 16, 13, 32, 1.2);
        disc(13, 16, 3.6, 0x655c52);
        patch(14.2, 17.2, 2.2, 2, 0x2a2220); 
        edgeExit(new Zone(MAP - 1.7, 13, MAP, 19), AreaId.HOLLOW_SHRINE, 3.8, 16, 1);
        gate(MAP - 2.6, 16, 6.6, false, true);   // east exit gate

        house(3, 5, 5, 4, 26, 0x7a6a58, 0x5a2e2c, 0);    
        house(16, 4, 5, 4.5, 28, 0x6f6252, 0x4f2f2c, 1);
        house(24.5, 6, 4, 4, 24, 0x756753, 0x5c3230, 3); 
        house(16.5, 22, 5, 4, 25, 0x6e6150, 0x5a3431, 5);  
        house(24.5, 21.5, 4, 4, 24, 0x786a58, 0x4e2a2a, 2); 

        solid(10.4, 13, 1.4, 1.4); prop(10.4, 13, 11.8, 14.4, () -> drawWell(10.4, 13));
        solid(14.6, 17.6, 1.2, 1.2); prop(14.6, 17.6, 15.8, 18.8, () -> drawFire(15.2, 18.2, false));
        solid(18.5, 19.2, 2.4, 1.3); prop(18.5, 19.2, 20.9, 20.5, () -> drawCart(18.5, 19.2));
        solid(8.6, 10.5, 1.4, .8); prop(8.6, 10.5, 10, 11.3, () -> drawCrates(8.6, 10.5));
        solid(22, 11, 1.4, .8); prop(22, 11, 23.4, 11.8, () -> drawCrates(22, 11));
        solid(28.2, 26.6, 1.4, .8); prop(28.2, 26.6, 29.6, 27.4, () -> drawCrates(28.2, 26.6));
        for (double[] b : new double[][]{{9.2, 19.7}, {9.9, 20.2}, {22.4, 19.4}, {5.6, 11.6}}) {
            solid(b[0] - .3, b[1] - .3, .6, .6); prop(b[0] - .35, b[1] - .35, b[0] + .35, b[1] + .35, () -> drawBarrel(b[0], b[1]));
        }
        for (double[] l : new double[][]{{6.8, 14.4}, {6.8, 17.6}, {19.5, 14.3}, {19.5, 17.7}, {23.5, 14.3}, {23.5, 17.7}}) lamp(l[0], l[1]);
        banner(27.4, 13.5, 0x7a1e22); banner(27.4, 18.5, 0x7a1e22); banner(9.6, 11.2, 0x6a1a24);
        fence(4, 11.2, 6, true); fence(17, 10.8, 5, true); fence(22.8, 6.5, 6, false); fence(3, 20, 5, true);
        wall(8.6, 5, .4, 3, 20); wall(21.6, 4.2, .4, 3.2, 24); wall(2, 28.6, 3, .4, 17);
        double[][] ts = {{1.5, 3, 130}, {1.8, 12.5, 110}, {2, 19, 120}, {1.4, 29.5, 100}, {7, 2.2, 125}, {10, 29.5, 115}, {28.6, 2.4, 135},
                {29.6, 12, 105}, {29.4, 28.8, 120}, {22.5, 30, 110}, {11, 3, 95}, {20.5, 12.2, 100}, {13.5, 24.5, 80}, {5.5, 30, 90}};
        for (int i = 0; i < ts.length; i++) tree(ts[i][0], ts[i][1], (int) ts[i][2], 3 + i * 5);
        rock(7.8, 12.8, 7); rock(20.4, 28.4, 8); rock(28, 9.2, 6);
    }

    static void drawHouse(double x, double y, double w, double d, double h, Color wall, Color roof, int v) {
        int sd = (int) (x * 7 + y * 13);
        switch (((v % 6) + 6) % 6) {
            case 1: houseMinka(x, y, w, d, h, wall, roof, sd, false, 1, true); break;
            case 2: houseBurnt(x, y, w, d, h, wall, sd); break;
            case 3: houseKura(x, y, w, d, h, wall, roof, sd); break;
            case 4: houseMachiya(x, y, w, d, h, wall, roof, sd); break;
            case 5: houseMinka(x, y, w, d, h, wall, roof, sd, true, 2, false); break;
            default: houseMinka(x, y, w, d, h, wall, roof, sd, true, 0, false); break;
        }
    }

    static void houseMinka(double x, double y, double w, double d, double h, Color wall, Color roof, int sd, boolean thatch, int dmg, boolean lit) {
        jShadow(x, y, w, d);
        double vd = .9, bd = d - vd, z0 = 3, zE = z0 + h * .85, H = zE - z0;
        box(x, y, w, d, 0, 3, J_STONE);
        Color plaster = mix(wall, col(0xcbc2ae), .45);
        final int n = Math.max(2, (int) Math.round(w / 1.25)), doorBay = (sd & 1) == 0 ? 0 : n - 1, litBay = lit ? (doorBay == 0 ? n - 1 : 0) : -1;
        final double zs = z0 + H * .30;
        jFace(true, x, y, w, bd);
        jFrameWall(plaster, J_WOOD, z0, zE, 1.0, sd, true, dmg, () -> {
            for (int i = 0; i < n; i++) {
                double u0 = (double) i / n + .004, u1 = (i + 1.0) / n - .004;
                if (i == doorBay) jDoor(u0, u1, z0, zE - 3, 1.0, sd);
                else if (dmg == 2 && i == 1) {                                               
                    jpoly(J_DARK, u0, z0, u1, z0, u1, zE - 4, (u0 + u1) / 2, zE - 11, u0, zE - 6);
                    jl(col(0x4a3626), u0, z0, u1 - .01, zE - 8); jl(col(0x5e4a38), u0 + .01, z0 + 2, (u0 + u1) / 2, zE - 10);
                } else if (hash(i, sd, 71) < .13) jq(u0, u1, zs, zE - 3, J_DARK);                
                else jShoji(u0, u1, zs, zE - 3, 1.0, i == litBay, sd + i);
            }
        });
        jFace(false, x, y, w, bd);
        jFrameWall(plaster, J_WOOD, z0, zE, .66, sd + 7, false, dmg, () -> jLattice(.34, .66, z0 + H * .38, z0 + H * .8, .66, null));
        jVeranda(x, y, w, bd, vd, z0, zE, sd);
        if (litBay >= 0) light(x + w * (litBay + .5) / n, y + bd + .5, z0 + H * .55, 28, 0xffa040);
        Color base = thatch ? mix(col(0x9a8658), roof, .12) : mix(col(0x4e5360), roof, .15);
        double rh = h * (thatch ? .8 : .75);
        jRoof(x, y, w, d, zE, rh, thatch ? .45 : .4, true, thatch, base, plaster, sd, dmg);
        if (thatch && dmg == 0) {                                                             
            double cxm = x + w / 2, zt = zE + rh;
            box(cxm - .8, y + d / 2 - .4, 1.6, .8, zt - 1, 6, col(0x241912));
            jRoof(cxm - .8, y + d / 2 - .4, 1.6, .8, zt + 5, 8, .3, true, true, shade(base, 1.05), plaster, sd + 5, 0);
        }
    }

    static void houseKura(double x, double y, double w, double d, double h, Color wall, Color roof, int sd) {
        jShadow(x, y, w, d);
        double z0 = 4, zE = z0 + h * 1.0, H = zE - z0;
        box(x, y, w, d, 0, z0, J_STONE);
        Color plaster = mix(wall, col(0xd5cdb8), .6);
        for (int f = 0; f < 2; f++) {
            boolean front = f == 0; double lum = front ? 1.0 : .66;
            jFace(front, x, y, w, d);
            double L = jL(), zn = z0 + H * .46;
            jq(0, 1, z0, zE, shade(plaster, lum));
            for (int i = 0; i < 7; i++) { double u = hash(i, sd + f, 3); jl(shade(mix(plaster, col(0x1c140e), .4), lum), u, zE - 1, u, zE - 1 - 4 - hash(i, sd, 4) * 14); }
            jq(0, 1, z0, zn, shade(col(0x2b3036), lum));                                    
            Color seam = shade(col(0xcfc8b4), lum); double dz = (zn - z0) / 12;
            for (double a = -dz; a < L + dz; a += .7) { jDiag(seam, L, a, z0, a + dz, zn); jDiag(seam, L, a, z0, a - dz, zn); }
            jl(seam, 0, zn, 1, zn); jq(0, 1, zn, zn + 1.2, shade(col(0xcfc8b4), lum * .9));
            for (int i = 0; i < 3; i++) if (hash(i, sd + f, 5) < .7) {                       
                double a = .08 + hash(i, sd + f, 6) * .7, b = a + .08 + hash(i, sd + f, 7) * .12, za = zn + 4 + hash(i, sd + f, 8) * H * .25;
                jq(a, b, za, za + 4 + hash(i, sd + f, 9) * 6, shade(col(0x5b4632), lum));
            }
            for (int i = 0; i < 4; i++) { double a = hash(i, sd + f, 14); jq(a, Math.min(1, a + .1), z0, z0 + 1 + hash(i, sd + f, 15) * 3, shade(col(0x4a5a30), lum)); }
            double uc = front ? .5 : .5, ww = 1.0 / L;
            if (front) {                                                                       
                double zd = z0 + H * .5;
                jq(uc - ww * .65, uc + ww * .65, z0, zd, shade(col(0x6a6358), lum));
                jq(uc - ww * .5, uc + ww * .5, z0, zd - 2, shade(col(0x3a2a20), lum));
                for (int i = 1; i < 6; i++) jl(shade(col(0x241912), lum), uc - ww * .5 + ww * i / 6, z0, uc - ww * .5 + ww * i / 6, zd - 2);
                for (double zz = z0 + 4; zz < zd - 3; zz += 6) jl(col(0x15110f), uc - ww * .5, zz, uc + ww * .5, zz);
                jq(uc + ww * .02, uc + ww * .22, z0, zd - 2, J_DARK);
            }
            double zw = z0 + H * .72;                                                        
            double wu = front ? .2 : .65;
            jq(wu - ww * .42, wu + ww * .42, zw - 1, zw + 11, shade(col(0xd5cdb8), lum * 1.05));
            jLattice(wu - ww * .3, wu + ww * .3, zw, zw + 10, lum, null);
        }
        jRoof(x, y, w, d, zE, h * .62, .22, false, false, mix(col(0x454a55), roof, .12), plaster, sd, 1);
    }

    static void houseMachiya(double x, double y, double w, double d, double h, Color wall, Color roof, int sd) {
        jShadow(x, y, w, d);
        double hd2 = .8, bd = d - hd2, z0 = 3, zE = z0 + h * 1.0, H = zE - z0, zm = z0 + H * .46;
        box(x, y, w, d, 0, 3, J_STONE);
        Color plaster = mix(wall, col(0xcbc2ae), .5);
        final int n = Math.max(2, (int) Math.round(w / 1.25));
        jFace(true, x, y, w, bd);
        jFrameWall(plaster, J_WOOD, z0, zE, 1.0, sd, false, 1, () -> {
            for (int i = 0; i < n; i++) {                                                        
                double u0 = (double) i / n + .004, u1 = (i + 1.0) / n - .004;
                if (i == n / 2) jDoor(u0, u1, z0, zm - 3, 1.0, sd);
                else jLattice(u0, u1, z0 + 3, zm - 3, 1.0, null);
            }
            jq(0, 1, zm - 2, zm + 1, shade(J_WOOD, 1.0));
            double u0 = .5 - .75 / w, u1 = .5 + .75 / w;                                        
            jShoji(u0, u1, zm + 7, zE - 7, 1.0, true, sd);
            jShoji(.1, .1 + 1.5 / w, zm + 8, zE - 8, 1.0, false, sd + 3);
        });
        jFace(false, x, y, w, bd);
        jFrameWall(plaster, J_WOOD, z0, zE, .66, sd + 7, false, 1, () -> jLattice(.4, .6, zm + 7, zE - 8, .66, null));

        Color tile = mix(col(0x4e5360), roof, .15);
        double yf = y + d, yb = y + bd, xa = x - .12, xb = x + w + .12;
        quad(shade(tile, .86), xa, yb, zm + 5, xb, yb, zm + 5, xb, yf, zm, xa, yf, zm);
        for (int k = 1; k < 5; k++) { double t = k / 5.0; ln(shade(tile, .65), xa, yb + (yf - yb) * t, zm + 5 - 5 * t, xb, yb + (yf - yb) * t, zm + 5 - 5 * t); }
        for (double xx = xa + .3; xx < xb; xx += .55) ln(shade(tile, .65), xx, yb, zm + 5, xx, yf, zm);
        quad(shade(tile, .52), xb, yb, zm + 5, xb, yf, zm, xb, yb, zm);
        quad(col(0x25252b), xa, yf, zm, xb, yf, zm, xb, yf, zm - 2, xa, yf, zm - 2);
        for (double xx = x + .1; xx <= x + w; xx += (w - .2) / 2) box(xx, yf - .22, .16, .16, 3, zm - 5, J_WOOD);   // eave posts
        light(x + w * .5, yb + .3, zm + 16, 28, 0xffa040);
        jRoof(x, y, w, d, zE, h * .5, .3, false, false, tile, plaster, sd, 1);
    }

    static void houseBurnt(double x, double y, double w, double d, double h, Color wall, int sd) {
        jShadow(x, y, w, d);
        box(x, y, w, d, 0, 3, J_STONE);
        fillq(col(0x1d1719), x + .25, y + .25, 3.1, x + w - .25, y + .25, 3.1, x + w - .25, y + d - .25, 3.1, x + .25, y + d - .25, 3.1);
        Color c1 = col(0x2a2024), c2 = col(0x171113), scorch = mix(wall, col(0x120c0e), .72);
        // far (back / left) wall stumps first
        for (int i = 0; i < 4; i++) if (hash(i, sd, 81) < .8) { double a = x + w * i / 4.0; box(a + .05, y, w / 4 - .1, .25, 3, 7 + hash(i, sd, 82) * 16, scorch); }
        for (int i = 0; i < 4; i++) if (hash(i, sd, 83) < .7) { double a = y + d * i / 4.0; box(x, a + .05, .25, d / 4 - .1, 3, 6 + hash(i, sd, 84) * 14, shade(scorch, .9)); }
        // charred posts, far to near
        java.util.List<double[]> posts = new ArrayList<>();
        for (int i = 0; i <= 4; i++) { posts.add(new double[]{x + .1 + (w - .4) * i / 4, y + .1}); posts.add(new double[]{x + .1 + (w - .4) * i / 4, y + d - .35}); }
        for (int i = 1; i < 4; i++) { posts.add(new double[]{x + .1, y + .1 + (d - .45) * i / 4}); posts.add(new double[]{x + w - .35, y + .1 + (d - .45) * i / 4}); }
        posts.add(new double[]{x + w * .45, y + d * .5}); posts.add(new double[]{x + w * .7, y + d * .4});
        posts.sort((p, q) -> Double.compare(p[0] + p[1], q[0] + q[1]));
        double[] prev = null; double prevTop = 0;
        for (int i = 0; i < posts.size(); i++) {
            double[] p = posts.get(i);
            double ph = hash(i, sd, 85) < .3 ? 8 + hash(i, sd, 86) * 8 : h * (.7 + hash(i, sd, 86) * .5);
            box(p[0], p[1], .24, .24, 3, ph, hash(i, sd, 87) < .5 ? c1 : c2);
            if (hash(i, sd, 88) < .4) ln(col(0x5a2a1c), p[0] + .05, p[1] + .24, 3 + ph * .3, p[0] + .05, p[1] + .24, 3 + ph * .75);   // glowing scorch line
            if (prev != null && hash(i, sd, 89) < .5 && Math.hypot(p[0] - prev[0], p[1] - prev[1]) < 2.2) {                             // cross beam
                double hb = Math.min(ph, prevTop) * .8 + 3; ln(c2, prev[0] + .1, prev[1] + .1, hb, p[0] + .1, p[1] + .1, hb); ln(c2, prev[0] + .1, prev[1] + .1, hb + 1, p[0] + .1, p[1] + .1, hb + 1);
            }
            prev = p; prevTop = ph;
        }

        for (int i = 0; i < 5; i++) {
            double ax = x + .4 + hash(i, sd, 91) * (w - 1), ay = y + .4 + hash(i, sd, 92) * (d - 1), bx = ax + (hash(i, sd, 93) - .3) * 1.8, by = ay + (hash(i, sd, 94) - .4) * 1.4;
            double ta = 3.5 + hash(i, sd, 95) * 14;
            ln(c1, ax, ay, ta, bx, by, 3.3); ln(c1, ax, ay + .06, ta, bx, by + .06, 3.3);
        }
        for (int i = 0; i < 4; i++) if (hash(i, sd, 96) < .6) { double a = x + w * i / 4.0; box(a + .05, y + d - .25, w / 4 - .1, .25, 3, 3 + hash(i, sd, 97) * 5, scorch); }
        for (int i = 0; i < 4; i++) if (hash(i, sd, 98) < .5) { double a = y + d * i / 4.0; box(x + w - .25, a + .05, .25, d / 4 - .1, 3, 3 + hash(i, sd, 99) * 5, shade(scorch, .9)); }
        for (int i = 0; i < 8; i++) { double ex = x + .5 + hash(i, sd, 101) * (w - 1), ey = y + .5 + hash(i, sd, 102) * (d - 1); box(ex, ey, .25, .18, 3, .8 + hash(i, sd, 103), c1); }
        g.setColor(col(0xff7a2a));
        for (int i = 0; i < 7; i++) { double ex = x + .5 + hash(i, sd, 104) * (w - 1), ey = y + .5 + hash(i, sd, 105) * (d - 1); g.fillRect(sx(ex, ey), sy(ex, ey, 3.3), 1, 1); }
        light(x + w * .5, y + d * .5, 8, 26, 0xff6a2a);
    }

    static void drawWall(double x, double y, double w, double d, double h) {
        fillq(new Color(0, 0, 0, 60), x + .2, y + .2, 0, x + w + .6, y + .2, 0, x + w + .6, y + d + .6, 0, x + .2, y + d + .6, 0);
        Color c = col(0x7d766f);
        box(x, y, w, d, 0, h, c);
        boolean alongX = w > d;
        if (alongX) {
            box(x + w * .05, y, w * .22, d, h, 7, c); box(x + w * .6, y, w * .18, d, h, 12, c);
        } else {
            box(x, y + d * .05, w, d * .22, h, 7, c); box(x, y + d * .6, w, d * .18, h, 12, c);
        }
        // brick lines
        for (int k = 1; k < 4; k++) ln(shade(c, .55), x, y + d, h * k / 4, x + w, y + d, h * k / 4);
    }

    static void drawLamp(double x, double y) {
        int bx = sx(x, y), by = sy(x, y, 0);
        shadowOval(bx, by, 5, 2);
        g.setColor(col(0x2c2226)); g.fillRect(bx - 1, by - LAMP_H, 2, LAMP_H); g.fillRect(bx - 1, by - LAMP_H - 1, 9, 2);
        g.setColor(col(0x3a2c2a)); g.fillRect(bx + 5, by - LAMP_H + 1, 6, 8);
        g.setColor(((int) (time * 9 + x) % 5 == 0) ? col(0xffd27a) : col(0xffa844)); g.fillRect(bx + 6, by - LAMP_H + 2, 4, 6);
        light(x + .4, y + .1, LAMP_H - 4, 56, 0xff9a44);
    }

    static void drawWell(double x, double y) {
        final double k = WELL_H;
        fillq(new Color(0, 0, 0, 60), x, y, 0, x + 1.9, y + .2, 0, x + 1.9, y + 1.9, 0, x + .2, y + 1.9, 0);
        Color st = col(0x7b766f), wood = col(0x4c3828);
        box(x, y, 1.4, 1.4, 0, 7 * k, st);
        quad(col(0x0a0f17), x + .22, y + .22, 7.1 * k, x + 1.18, y + .22, 7.1 * k, x + 1.18, y + 1.18, 7.1 * k, x + .22, y + 1.18, 7.1 * k);
        box(x + .02, y + .6, .13, .13, 7 * k, 24 * k, wood);
        box(x + 1.25, y + .6, .13, .13, 7 * k, 24 * k, wood);
        box(x - .05, y + .58, 1.5, .17, 29 * k, 2, shade(wood, 1.2));
        ln(col(0xb9a98a), x + .7, y + .66, 29 * k, x + .7, y + .66, 14 * k);
        box(x + .6, y + .56, .2, .2, 11 * k, 4, col(0x6b5038));
    }

    static void drawCrates(double x, double y) {
        fillq(new Color(0, 0, 0, 60), x, y, 0, x + 1.5, y + .1, 0, x + 1.5, y + 1.3, 0, x + .1, y + 1.3, 0);
        Color a = col(0x7a5a38), b = col(0x6b4e30);
        box(x, y, .7, .7, 0, 11, a); box(x + .75, y + .25, .6, .6, 0, 8, b); box(x + .06, y + .06, .55, .55, 11, 8, shade(a, 1.1));
        ln(shade(a, .5), x, y + .7, 5, x + .7, y + .7, 5);
        ln(shade(b, .5), x + .75, y + .85, 4, x + 1.35, y + .85, 4);
    }

    static void drawBarrel(double x, double y) {
        int bx = sx(x, y), by = sy(x, y, 0);
        shadowOval(bx, by, 6, 2);
        g.setColor(col(0x6b4a2c)); g.fillRect(bx - 4, by - 11, 9, 11);
        g.setColor(col(0x8a6339)); g.fillRect(bx - 4, by - 11, 2, 11);
        g.setColor(col(0x2f2a2c)); g.fillRect(bx - 4, by - 8, 9, 1); g.fillRect(bx - 4, by - 3, 9, 1);
        g.setColor(col(0x8a6339)); g.fillOval(bx - 4, by - 13, 9, 4);
        g.setColor(col(0x3a2a1c)); g.fillOval(bx - 2, by - 12, 5, 2);
    }

    static void drawCart(double x, double y) {
        fillq(new Color(0, 0, 0, 60), x, y, 0, x + 2.8, y + .2, 0, x + 2.8, y + 1.7, 0, x + .2, y + 1.7, 0);
        Color wood = col(0x5e4630);
        box(x, y, 2.4, 1.3, 5, 4, wood);
        box(x, y + 1.2, 2.4, .1, 9, 5, shade(wood, .9));
        box(x + 2.3, y, .1, 1.3, 9, 5, shade(wood, .8));
        ln(col(0x2e2118), x, y + 1.3, 3, x - 1.6, y + 1.3, 6);
        int wx = sx(x + 1.1, y + 1.35), wy = sy(x + 1.1, y + 1.35, 5);
        g.setColor(col(0x2e2118)); g.drawOval(wx - 6, wy - 6, 12, 12); g.drawOval(wx - 5, wy - 5, 10, 10);
        g.drawLine(wx - 6, wy, wx + 6, wy); g.drawLine(wx, wy - 6, wx, wy + 6);
        // spilled sacks
        g.setColor(col(0x8b7a56)); g.fillOval(sx(x + 1.2, y + .6) - 5, sy(x + 1.2, y + .6, 9) - 6, 10, 7);
    }

    protected void house(double x, double y, double w, double d, double h, int wall, int roof, int v) {
        solid(x, y, w, d);
        prop(x, y, x + w, y + d, () -> drawHouse(x, y, w, d, h * HOUSE_H, col(wall), col(roof), v));
    }

    protected void lamp(double x, double y) { solid(x - .15, y - .15, .3, .3); prop(x - .3, y - .3, x + .3, y + .3, () -> drawLamp(x, y)); }

    protected void wall(double x, double y, double w, double d, double h) {
        solid(x, y, w, d); prop(x, y, x + w, y + d, () -> drawWall(x, y, w, d, h));
    }
}
