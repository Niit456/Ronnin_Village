package gameDev;

import java.awt.*;
import static gameDev.Gfx.*;
import static gameDev.Painters.*;

public class ShogunCastle extends Area {

    //palette
    static final Color STN = col(0x717682), PLS = col(0xdad6c9), TIM = col(0x1b1a20), ROOFC = col(0x2d313b);
    static final Color GOLD = col(0xe3b341), LAC = col(0x7a1e24), WOODB = col(0x5a3f2c);
    static final Color WARM = col(0xe8a040);

    //building blocks
    static void stoneFace(boolean front, double x, double y, double w, double d, double z0, double z1, double lum, int sd) {
        jFace(front, x, y, w, d);
        double L = jL();
        jq(0, 1, z0, z1, shade(STN, lum));
        int rows = Math.max(1, (int) Math.round((z1 - z0) / 5.5));
        double rh = (z1 - z0) / rows;
        int n = Math.max(1, (int) Math.round(L / .9));
        for (int r = 0; r < rows; r++) {
            double za = z0 + r * rh, zb = za + rh, s = (r % 2) * .5;
            for (int k = -1; k < n; k++) {
                double u0 = Math.max(0, (k + s) / n), u1 = Math.min(1, (k + 1 + s) / n);
                if (u1 - u0 < .015) continue;
                jq(u0, u1, za + .3, zb - .3, shade(STN, lum * (.86 + hash(k + 5, r + sd, 31) * .3)));
                jl(shade(STN, lum * .5), u1, za, u1, zb);
            }
            jl(shade(STN, lum * .5), 0, za, 1, za);
        }
    }

    static void plFace(boolean front, double x, double y, double w, double d, double z0, double z1, double lum, int sd, boolean posts) {
        jFace(front, x, y, w, d);
        double L = jL();
        jq(0, 1, z0, z1, shade(PLS, lum));
        for (int i = 0; i < 5; i++) { double u = hash(i, sd, 3); jl(shade(PLS, lum * .8), u, z1 - 2, u, z1 - 2 - 3 - hash(i, sd, 4) * 6); }
        Color tb = shade(TIM, lum);
        jq(0, 1, z1 - 1.8, z1, tb); jq(0, 1, z0, z0 + 1.2, tb);
        if (posts) { int n = Math.max(1, (int) Math.round(L / 1.5)); for (int i = 0; i <= n; i++) jpost((double) i / n, .2, z0, z1, tb); }
    }

    //lattice window
    static void win(double u, double wf, double za, double zb, double lum, boolean lit) {
        jLattice(u - wf / 2, u + wf / 2, za, zb, lum, lit ? WARM : null);
        if (lit) light(jcx(u), jcy(u), (za + zb) / 2, 26, 0xffa040);
    }

    //little square loop-holes
    static void sama(double zm, double lum) {
        double L = jL();
        for (double u = .6; u < L - .3; u += 1.15) jq((u - .17) / L, (u + .17) / L, zm - 1.6, zm + 1.6, shade(J_DARK, 1));
    }

    //roofs
    static double[] rp(double[] E, double[] T, double u, double t, double z, double rh, double up) {
        double ex = E[0] + (E[2] - E[0]) * u, ey = E[1] + (E[3] - E[1]) * u, tx = T[0] + (T[2] - T[0]) * u, ty = T[1] + (T[3] - T[1]) * u;
        double e = Math.pow(Math.abs(2 * u - 1), 3);
        return new double[]{ex + (tx - ex) * t, ey + (ty - ey) * t, z + rh * Math.pow(t, 1.7) + up * e * (1 - t) * (1 - t)};
    }
    static void rq(Color c, double[]... p) { rpoly(c, p); }

   
     //A curved Japanese hip roof.
    static void roofHip(double ex0, double ey0, double ex1, double ey1, double z, double rh, double tx0, double ty0, double tx1, double ty1, double up, Color base, boolean gold) {
        final int NU = 10, NT = 5, ROWS = 8;
        double[][] E = {{ex0, ey0, ex1, ey0}, {ex0, ey1, ex1, ey1}, {ex1, ey0, ex1, ey1}, {ex0, ey0, ex0, ey1}};
        double[][] T = {{tx0, ty0, tx1, ty0}, {tx0, ty1, tx1, ty1}, {tx1, ty0, tx1, ty1}, {tx0, ty0, tx0, ty1}};
        int[] order = {0, 3, 1, 2};                                  
        double[] lum = {1.12, .95, .88, .7};
        for (int k : order) for (int i = 0; i < NU; i++) for (int j = 0; j < NT; j++) {
            Color c = shade(base, lum[k] * ((i + j) % 2 == 0 ? 1.04 : .96));
            rq(c, rp(E[k], T[k], (double) i / NU, (double) j / NT, z, rh, up), rp(E[k], T[k], (i + 1.0) / NU, (double) j / NT, z, rh, up),
                  rp(E[k], T[k], (i + 1.0) / NU, (j + 1.0) / NT, z, rh, up), rp(E[k], T[k], (double) i / NU, (j + 1.0) / NT, z, rh, up));
        }
        //flat top 
        quad(shade(base, .6), tx0, ty0, z + rh, tx1, ty0, z + rh, tx1, ty1, z + rh, tx0, ty1, z + rh);
        for (int k = 1; k <= 2; k++) {                                
            Color dk = shade(base, k == 1 ? .66 : .5);
            for (int r = 1; r < ROWS; r++) {
                double t = (double) r / ROWS;
                for (int i = 0; i < NU; i++) { double[] p = rp(E[k], T[k], (double) i / NU, t, z, rh, up), q = rp(E[k], T[k], (i + 1.0) / NU, t, z, rh, up); ln(dk, p[0], p[1], p[2], q[0], q[1], q[2]); }
                for (double u = (r % 2) * .05 + .05; u < 1; u += .1) { double[] p = rp(E[k], T[k], u, t - 1.0 / ROWS, z, rh, up), q = rp(E[k], T[k], u, t, z, rh, up); ln(dk, p[0], p[1], p[2], q[0], q[1], q[2]); }
            }
        }
        double th = 2.4;                                             
        for (int k = 1; k <= 2; k++) for (int i = 0; i < NU; i++) {
            double[] p = rp(E[k], T[k], (double) i / NU, 0, z, rh, up), q = rp(E[k], T[k], (i + 1.0) / NU, 0, z, rh, up);
            quad(k == 1 ? shade(PLS, .62) : shade(PLS, .42), p[0], p[1], p[2], q[0], q[1], q[2], q[0], q[1], q[2] - th, p[0], p[1], p[2] - th);
            ln(shade(base, 1.7), p[0], p[1], p[2], q[0], q[1], q[2]);
        }
        double[] a = rp(E[1], T[1], 1, 0, z, rh, up), b = rp(E[1], T[1], 1, 1, z, rh, up);                     // hip lines
        ln(shade(PLS, .8), a[0], a[1], a[2], b[0], b[1], b[2]);
        a = rp(E[1], T[1], 0, 0, z, rh, up); b = rp(E[1], T[1], 0, 1, z, rh, up); ln(shade(base, .5), a[0], a[1], a[2], b[0], b[1], b[2]);
        a = rp(E[2], T[2], 0, 0, z, rh, up); b = rp(E[2], T[2], 0, 1, z, rh, up); ln(shade(base, .5), a[0], a[1], a[2], b[0], b[1], b[2]);
        double zt = z + rh;
        if (Math.abs(ty1 - ty0) < 1e-6 && tx1 > tx0) {                
            box(tx0 - .12, ty0 - .24, tx1 - tx0 + .24, .48, zt - .4, 2.6, shade(base, .62));
            if (gold) { shachi(tx0, ty0, zt + 2, 1); shachi(tx1, ty0, zt + 2, -1); }
        } else if (Math.abs(tx1 - tx0) < 1e-6 && ty1 > ty0) {         
            box(tx0 - .24, ty0 - .12, .48, ty1 - ty0 + .24, zt - .4, 2.6, shade(base, .62));
            if (gold) { shachi(tx0, ty0, zt + 2, -1); shachi(tx0, ty1, zt + 2, 1); }
        }
    }

    //golden fish ornament (shachihoko) on a ridge end; dir = side the tail curls toward on screen */
    static void shachi(double x, double y, double z, int dir) {
        int bx = sx(x, y), by = sy(x, y, z);
        int[][] p = {{-2, 0}, {3, 0}, {3, -3}, {2, -5}, {3, -8}, {5, -11}, {5, -13}, {3, -13}, {1, -11}, {0, -8}, {-1, -5}, {-2, -3}};
        int[] xs = new int[p.length], ys = new int[p.length];
        for (int i = 0; i < p.length; i++) { xs[i] = bx + p[i][0] * dir; ys[i] = by + p[i][1]; }
        g.setColor(GOLD); g.fillPolygon(xs, ys, p.length);
        g.setColor(shade(GOLD, .55)); g.drawPolygon(xs, ys, p.length);
        g.setColor(shade(GOLD, 1.35)); g.fillRect(bx - dir, by - 4, 1, 3); g.fillRect(bx + 3 * dir, by - 9, 1, 3);
    }

    //simple tiled cap over a wall that runs along y (ridge along y)
    static void capY(double x, double y, double w, double d, double z, double rh, double ov) {
        quad(shade(PLS, .55), x - ov, y + d + ov, z, x + w + ov, y + d + ov, z, x + w / 2, y + d + ov, z + rh);
        quad(shade(ROOFC, .74), x + w + ov, y - ov, z, x + w + ov, y + d + ov, z, x + w / 2, y + d + ov, z + rh, x + w / 2, y - ov, z + rh);
        Color dk = shade(ROOFC, .5);
        for (double yy = y; yy < y + d; yy += .5) ln(dk, x + w + ov, yy, z, x + w / 2, yy, z + rh);
        ln(shade(ROOFC, 1.5), x + w + ov, y - ov, z, x + w + ov, y + d + ov, z);
        ln(shade(ROOFC, 1.2), x + w / 2, y - ov, z + rh, x + w / 2, y + d + ov, z + rh);
    }

    //kara-hafu: the curved gable over the keep entrance
    static void karahafu(double cx, double yb, double yf, double hw, double z, double h) {
        final int NU = 5, NT = 5;
        for (int i = 0; i < NU; i++) for (int j = 0; j < NT; j++) {
            double u0 = (double) i / NU, u1 = (i + 1.0) / NU, t0 = (double) j / NT, t1 = (j + 1.0) / NT;
            double[][] q = new double[4][];
            double[][] uv = {{u0, t0}, {u1, t0}, {u1, t1}, {u0, t1}};
            for (int m = 0; m < 4; m++) {
                double u = uv[m][0], t = uv[m][1];
                q[m] = new double[]{cx + hw - hw * t, yb + (yf - yb) * u, z + h * Math.pow(t, 1.8) + 2.4 * u * u * u * (1 - t) * (1 - t)};
            }
            rq(shade(ROOFC, .72 * ((i + j) % 2 == 0 ? 1.04 : .96)), q);
        }
        for (int r = 1; r < 6; r++) { double t = r / 6.0; ln(shade(ROOFC, .5), cx + hw - hw * t, yb, z + h * Math.pow(t, 1.8), cx + hw - hw * t, yf, z + h * Math.pow(t, 1.8) + 2.4 * (1 - t) * (1 - t)); }
        // front fascia: concave profile, plaster with a gold edge and a crest
        int n = 10; double[] pts = new double[(2 * n + 2) * 3]; int k = 0;
        for (int i = 0; i <= n; i++) { double s = (double) i / n; pts[k++] = cx - hw * (1 - s); pts[k++] = yf; pts[k++] = z + 2.4 * (1 - s) * (1 - s) + h * Math.pow(s, 1.8); }
        for (int i = n; i >= 0; i--) { double s = (double) i / n; pts[k++] = cx + hw * (1 - s); pts[k++] = yf; pts[k++] = z + 2.4 * (1 - s) * (1 - s) + h * Math.pow(s, 1.8); }
        quad(shade(PLS, .78), pts);
        for (int i = 0; i < n; i++) {
            double s0 = (double) i / n, s1 = (i + 1.0) / n;
            double za = z + 2.4 * (1 - s0) * (1 - s0) + h * Math.pow(s0, 1.8), zb = z + 2.4 * (1 - s1) * (1 - s1) + h * Math.pow(s1, 1.8);
            ln(GOLD, cx - hw * (1 - s0), yf, za, cx - hw * (1 - s1), yf, zb); ln(GOLD, cx + hw * (1 - s0), yf, za, cx + hw * (1 - s1), yf, zb);
        }
        ln(shade(ROOFC, 1.4), cx - hw, yf, z + 2.4, cx + hw, yf, z + 2.4);
        int bx = sx(cx, yf), by = sy(cx, yf, z + h * .42);
        g.setColor(GOLD); g.fillOval(bx - 3, by - 3, 7, 7); g.setColor(shade(LAC, 1)); g.fillOval(bx - 1, by - 1, 3, 3);
    }

    //walls, gate, turrets
    static void shadow(double x, double y, double w, double d) {
        fillq(new Color(0, 0, 0, 70), x + .2, y + .2, 0, x + w + 1.1, y + .2, 0, x + w + 1.1, y + d + 1.1, 0, x + .2, y + d + 1.1, 0);
    }

    //curtain wall: stone foot, white plaster with loop-holes, tiled cap
    static void drawCurtain(double x, double y, double w, double d, boolean alongY) {
        shadow(x, y, w, d);
        int sd = (int) (x * 3 + y * 5);
        stoneFace(true, x, y, w, d, 0, 14, .9, sd); stoneFace(false, x, y, w, d, 0, 14, .62, sd + 1);
        plFace(true, x, y, w, d, 14, 30, .95, sd, alongY ? false : true); plFace(false, x, y, w, d, 14, 30, .66, sd + 1, alongY ? true : false);
        jFace(alongY ? false : true, x, y, w, d); sama(22, 1);
        if (alongY) capY(x, y, w, d, 30, 4.4, .35); else drawGable(y, d, 30, 4.4, ROOFC, x, x + w, .35);
    }

    //one pier of the gatehouse: stone to the wall top, then a plaster tower
    static void drawPier(double y, boolean north) {
        double x = 7.2, w = 2.6, d = 2.2;
        shadow(x, y, w, d);
        double fl = north ? .5 : .92;
        stoneFace(true, x, y, w, d, 0, 24, fl, 2); stoneFace(false, x, y, w, d, 0, 24, .62, 3);
        plFace(true, x, y, w, d, 24, 56, north ? .5 : .95, 4, false); plFace(false, x, y, w, d, 24, 56, .66, 5, true);
        box(x - .08, y - .08, w + .16, d + .16, 24, 1.4, shade(STN, 1.25));
        if (!north) { jFace(true, x, y, w, d); win(.5, .26, 36, 50, .95, false); }
        jFace(false, x, y, w, d); win(.5, .4, 36, 50, .66, false);
    }

    //the gatehouse's upper floor, bridging both piers over the passage
    static void drawGateTop() {
        double x = 7.2, y = 12.4, w = 2.6, d = 7.2;
        box(x, y, w, d, 56, 4, TIM);
        ln(GOLD, x + w, y, 59.5, x + w, y + d, 59.5);
        plFace(true, x, y, w, d, 60, 80, .95, 9, false); plFace(false, x, y, w, d, 60, 80, .66, 10, true);
        jFace(true, x, y, w, d); win(.5, .3, 64, 76, .95, true);
        jFace(false, x, y, w, d); win(.2, .1, 64, 76, .66, true); win(.5, .1, 64, 76, .66, false); win(.8, .1, 64, 76, .66, true);
        roofHip(x - .8, y - .8, x + w + .8, y + d + .8, 80, 11, 8.5, 13.3, 8.5, 18.7, 2.4, ROOFC, true);
    }

    static void drawTurret(double x, double y, double s, double d) {
        shadow(x, y, s, d);
        stoneFace(true, x, y, s, d, 0, 16, .9, 6); stoneFace(false, x, y, s, d, 0, 16, .62, 7);
        plFace(true, x, y, s, d, 16, 42, .95, 8, true); plFace(false, x, y, s, d, 16, 42, .66, 11, true);
        jFace(true, x, y, s, d); win(.5, .3, 24, 36, .95, true);
        jFace(false, x, y, s, d); win(.5, .3, 24, 36, .66, false);
        double cx = x + s / 2, cy = y + d / 2;
        roofHip(x - .7, y - .7, x + s + .7, y + d + .7, 42, 12, cx - .5, cy, cx + .5, cy, 2.2, ROOFC, true);
    }

    //long barracks (nagaya): dark lower tiles with white joints, windows, tiled roof
    static void drawNagaya(double x, double y, double w, double d) {
        shadow(x, y, w, d);
        double z0 = 3, zn = 13, zE = 25;
        box(x, y, w, d, 0, z0, J_STONE);
        for (int f = 0; f < 2; f++) {
            boolean front = f == 0; double lum = front ? 1 : .66;
            jFace(front, x, y, w, d); double L = jL();
            jq(0, 1, z0, zE, shade(PLS, lum));
            jq(0, 1, z0, zn, shade(col(0x2b3036), lum));
            Color seam = shade(col(0xcfc8b4), lum); double dz = (zn - z0) / 10;
            for (double a = -dz; a < L + dz; a += .7) { jDiag(seam, L, a, z0, a + dz, zn); jDiag(seam, L, a, z0, a - dz, zn); }
            jl(seam, 0, zn, 1, zn); jq(0, 1, zn, zn + 1.2, shade(col(0xcfc8b4), lum * .9));
            Color tb = shade(TIM, lum);
            int n = Math.max(1, (int) Math.round(L / 1.7));
            for (int i = 0; i <= n; i++) jpost((double) i / n, .2, zn + 1.2, zE, tb);
            jq(0, 1, zE - 1.8, zE, tb);
            if (front) {
                int cnt = (int) Math.round(L / 2.0);
                for (int i = 0; i < cnt; i++) { double u = (i + .5) / cnt; if (Math.abs(u - .5) < .06) continue; win(u, .6 / L, zn + 3, zE - 3, lum, i % 3 == 1); }
                jq(.5 - .5 / L, .5 + .5 / L, z0, zn + 6, shade(col(0x2a1c14), lum));
                jq(.5 - .5 / L, .5 + .5 / L, zn + 5, zn + 6.4, shade(GOLD, .7));
            } else for (int i = 0; i < 1; i++) win(.5, .22, zn + 3, zE - 3, lum, false);
        }
        drawGable(y, d, zE, 9, ROOFC, x, x + w, .6);
    }

    //lacquered bridge rail along x (side = y of the rail)
    static void drawBridgeRail(double y) {
        Color red = LAC;
        box(4.8, y, 2.4, .2, 7.4, 1.5, red);
        box(4.8, y, 2.4, .2, 3.6, 1, shade(red, .8));
        for (double px = 4.85; px < 7.2; px += .6) box(px - .08, y, .2, .2, 0, 9, shade(red, .9));
        for (double px : new double[]{4.84, 7.04}) { box(px - .05, y - .02, .24, .24, 9, 1.4, shade(red, 1.1)); box(px, y + .03, .14, .14, 10.4, 2.6, GOLD); }
    }

    static void drawBrazier(double x, double y) {
        int bx = sx(x, y), by = sy(x, y, 0);
        shadowOval(bx, by, 6, 2);
        g.setColor(col(0x24242a)); g.drawLine(bx - 5, by, bx - 2, by - 15); g.drawLine(bx + 5, by, bx + 2, by - 15); g.drawLine(bx, by + 1, bx, by - 15);
        g.fillRect(bx - 5, by - 18, 11, 3); g.setColor(col(0x50505a)); g.fillRect(bx - 5, by - 18, 11, 1);
        double t = time * 11 + x;
        int[] hs = {11 + (int) (Math.sin(t) * 2), 7 + (int) (Math.sin(t * 1.4 + 1) * 2), 3 + (int) (Math.sin(t * 1.9) * 1)};
        int[] ws = {8, 5, 3}; Color[] cs = {col(0xd8481f), col(0xf28a26), col(0xffd868)};
        for (int l = 0; l < 3; l++) {
            g.setColor(cs[l]);
            for (int r = 0; r < hs[l]; r++) { int wd = Math.max(1, (int) Math.round(ws[l] * (1 - Math.pow(r / (double) hs[l], 1.6)))); g.fillRect(bx - wd / 2 + (int) Math.round(Math.sin(t * .8 + r * .4) * r / 6.0), by - 19 - r, wd, 1); }
        }
        light(x, y, 22, 70, 0xff8a30);
    }

    //the keep (tenshu)
    static final double KX = 18.6, KY = 3.6, KW = 8.8, KD = 6.8;       // first-floor walls
    static final double F2X = 19.7, F2Y = 4.7, F2W = 6.6, F2D = 4.6;   // second floor
    static final double F3X = 20.6, F3Y = 5.6, F3W = 4.8, F3D = 2.8;   // third floor

    static void drawKeep() {
        fillq(new Color(0, 0, 0, 70), 17.9, 2.9, 0, 29.2, 2.9, 0, 29.2, 12.2, 0, 17.9, 12.2, 0);
        //stone plinth with the entrance stairs
        stoneFace(true, 17.7, 2.7, 10.6, 8.3, 0, 8, .9, 3); stoneFace(false, 17.7, 2.7, 10.6, 8.3, 0, 8, .62, 4);
        quad(shade(STN, 1.18), 17.7, 2.7, 8, 28.3, 2.7, 8, 28.3, 11.0, 8, 17.7, 11.0, 8);
        Color st = shade(STN, 1.1);
        box(21.0, 11.0, 4.0, .8, 0, 2.7, st); box(21.2, 11.0, 3.6, .55, 2.7, 2.7, st); box(21.4, 11.0, 3.2, .3, 5.4, 2.6, st);
        //first floor: dark namako tiles below, white plaster above, black posts
        for (int f = 0; f < 2; f++) {
            boolean front = f == 0; double lum = front ? 1 : .68;
            jFace(front, KX, KY, KW, KD); double L = jL();
            jq(0, 1, 8, 56, shade(PLS, lum));
            jq(0, 1, 8, 22, shade(col(0x2b3036), lum));
            Color seam = shade(col(0xcfc8b4), lum); double dz = 14.0 / 10;
            for (double a = -dz; a < L + dz; a += .7) { jDiag(seam, L, a, 8, a + dz, 22); jDiag(seam, L, a, 8, a - dz, 22); }
            jl(seam, 0, 22, 1, 22); jq(0, 1, 22, 23.2, shade(col(0xcfc8b4), lum * .9));
            Color tb = shade(TIM, lum);
            int n = Math.max(2, (int) Math.round(L / 1.45));
            for (int i = 0; i <= n; i++) jpost((double) i / n, .22, 23.2, 56, tb);
            jq(0, 1, 53.6, 56, tb);
        }
        jFace(true, KX, KY, KW, KD);
        for (double u : new double[]{.14, .26, .74, .86}) win(u, .085, 29, 46, 1, u > .8 || u < .15);
        double u0 = .364, u1 = .636;                                    // the great door, standing open
        jq(u0 - .014, u1 + .014, 8, 53, shade(GOLD, .5));
        jq(u0, u1, 8, 50, col(0x1e130d));
        jq(u0 + .01, u1 - .01, 8, 30, col(0x3a2412)); jq(u0 + .01, u1 - .01, 8, 18, col(0x7a4e1e)); jq(u0 + .01, u1 - .01, 8, 11, col(0xb27a2c));
        jq(u0, u0 + .04, 8, 50, shade(LAC, .7)); jq(u1 - .04, u1, 8, 50, shade(LAC, .7));
        for (double zz = 14; zz < 48; zz += 5) { jq(u0 + .008, u0 + .014, zz, zz + 1, GOLD); jq(u1 - .014, u1 - .008, zz, zz + 1, GOLD); }
        jq(u0 - .03, u1 + .03, 50, 53.6, TIM); jl(GOLD, u0 - .03, 52.6, u1 + .03, 52.6);
        light(23, KY + KD + .4, 26, 58, 0xffb050);
        jFace(false, KX, KY, KW, KD);
        for (double u : new double[]{.22, .5, .78}) win(u, .1, 29, 46, .68, u < .3);
        //roofs and upper floors
        roofHip(KX - .9, KY - .9, KX + KW + .9, KY + KD + .9, 56, 8, F2X, F2Y, F2X + F2W, F2Y + F2D, 2.4, ROOFC, false);
        karahafu(23, 9.5, KY + KD + .9, 2.2, 56.2, 12.5);
        for (int s = 0; s < 3; s++) {                                    // floors 2 and 3
            double x, y, w, d, z0, z1; int nf, nr;
            if (s == 0) { x = F2X; y = F2Y; w = F2W; d = F2D; z0 = 64; z1 = 86; nf = 3; nr = 2; }
            else if (s == 1) { x = F3X; y = F3Y; w = F3W; d = F3D; z0 = 94; z1 = 112; nf = 2; nr = 1; }
            else break;
            plFace(true, x, y, w, d, z0, z1, 1, 20 + s, true); plFace(false, x, y, w, d, z0, z1, .68, 30 + s, true);
            jFace(true, x, y, w, d); for (int i = 0; i < nf; i++) win((i + .5) / nf, .13 + .02 * s, z0 + 5, z1 - 4, 1, i != 1);
            jFace(false, x, y, w, d); for (int i = 0; i < nr; i++) win((i + .5) / nr, .13, z0 + 5, z1 - 4, .68, s == 0);
            if (s == 0) roofHip(F2X - .8, F2Y - .8, F2X + F2W + .8, F2Y + F2D + .8, 86, 8, F3X, F3Y, F3X + F3W, F3Y + F3D, 2.4, ROOFC, false);
        }
        roofHip(F3X - .8, F3Y - .8, F3X + F3W + .8, F3Y + F3D + .8, 112, 14, 21.4, 7.0, 24.6, 7.0, 2.6, ROOFC, true);
        light(23, 8, 90, 60, 0xffa040);
        //"go in here" marker(arrow)
        int bx = sx(23, KY + KD + .9), by = sy(23, KY + KD + .9, 80) + (int) Math.round(Math.sin(time * 4) * 2);
        int[] xs = {bx, bx - 6, bx + 6}, ys = {by + 9, by - 3, by - 3};
        g.setColor(col(0xf2c14e)); g.fillPolygon(xs, ys, 3); g.setColor(col(0x3a2a14)); g.drawPolygon(xs, ys, 3);
    }

    //ground (baked once)
    static void ring(double cx, double cy, double r, Color c) {
        double px = cx + r, py = cy;
        for (int i = 1; i <= 36; i++) {
            double a = i * Math.PI * 2 / 36, nx = cx + Math.cos(a) * r, ny = cy + Math.sin(a) * r;
            ln(c, px, py, 0, nx, ny, 0); px = nx; py = ny;
        }
    }

    @Override protected void paintGroundDecor() {
        Color joint = new Color(18, 20, 30, 80);
        for (int ty = 1; ty < 31; ty++) for (int tx = 10; tx < 31; tx++) { ln(joint, tx, ty, 0, tx + 1, ty, 0); ln(joint, tx, ty, 0, tx, ty + 1, 0); }
        Color rake = new Color(240, 238, 226, 90), rakeD = new Color(60, 58, 52, 90);
        for (double[] gd : new double[][]{{10.6, 3, 5.8, 8}, {10.6, 20.5, 5.4, 5.5}})
            for (double yy = gd[1] + .3; yy < gd[1] + gd[3]; yy += .5) { ln(rakeD, gd[0], yy + .08, 0, gd[0] + gd[2], yy + .08, 0); ln(rake, gd[0], yy, 0, gd[0] + gd[2], yy, 0); }
        for (double r = .9; r < 2.3; r += .5) { ring(13.7, 7.0, r, rake); ring(13.5, 23.2, r, rake); }
        // moat banks: stone coping
        Color cp = col(0x2a2e36), cp2 = col(0x8a8f9a);
        for (double yy = 0; yy < 32; yy += 1) {
            if (yy > 13.9 && yy < 17.6) continue;
            quad(shade(STN, .8), 4.6, yy, 0, 4.8, yy, 0, 4.8, yy + 1, 0, 4.6, yy + 1, 0); ln(cp, 4.6, yy, 0, 4.6, yy + 1, 0);
        }
        fillq(new Color(0, 0, 0, 80), 7.2, 14.6, 0, 9.8, 14.6, 0, 9.8, 17.4, 0, 7.2, 17.4, 0);   // shade inside the gate passage
        // bridge deck: planks across, side beams
        quad(WOODB, 4.8, 14.6, 0, 7.2, 14.6, 0, 7.2, 17.4, 0, 4.8, 17.4, 0);
        for (double xx = 4.8; xx < 7.2; xx += .3) ln(shade(WOODB, .62), xx, 14.6, 0, xx, 17.4, 0);
        for (double xx = 4.95; xx < 7.2; xx += .6) ln(shade(WOODB, 1.25), xx, 14.7, 0, xx, 17.3, 0);
        quad(shade(WOODB, .6), 4.8, 14.6, 0, 7.2, 14.6, 0, 7.2, 14.78, 0, 4.8, 14.78, 0);
        quad(shade(WOODB, .6), 4.8, 17.22, 0, 7.2, 17.22, 0, 7.2, 17.4, 0, 4.8, 17.4, 0);
    }

    //the area
    public ShogunCastle() {
        super(AreaId.SHOGUN_CASTLE, "Shogun Castle", "THE HALL OF THE SHOGUN AWAITS", 37);
        dirt = new Color[]{col(0x3b4049), col(0x424852), col(0x353a43), col(0x4a505a)};
        pathColor = col(0x8a8e98); grass = col(0x4d6a5e);
        water(4.8, 0, 2.4, 32);                         //the moat
        patch(10, .5, 21.5, 30.5, 0x585d70);              //courtyard paving
        patch(7.2, 14.6, 2.6, 2.8, 0x6a6e78);             //floor of the gate passage
        patch(10.6, 3, 5.8, 8, 0xaaa89e);                  //gravel gardens
        patch(10.6, 20.5, 5.4, 5.5, 0xaaa89e);
        path(0, 16, 4.8, 16, 1.2);
        path(9.8, 16, 23, 16, 1.45);
        path(23, 16, 23, 11.9, 1.4);
        path(21.65, 16, 21.65, 26.4, 1.0);
        exit(new Exit(new Zone(0, 13, 1.7, 19), AreaId.HOLLOW_SHRINE, 28.2, 16, -1, 2.6, 16, 40));   //back west, to the Hollow Shrine
        exit(new Exit(new Zone(21.8, 11.0, 24.2, 12.5), AreaId.THRONE_ROOM, 16, 20, 1, 23, 11.6, -1));   //the keep door leads into the throne room
        // moat: solid except where the bridge crosses (rails sit exactly on its edge)
        solid(4.8, 0, 2.4, 14.6); solid(4.8, 17.4, 2.4, 14.6);
        prop(4.8, 14.4, 7.2, 14.6, () -> drawBridgeRail(14.4));
        prop(4.8, 17.4, 7.2, 17.6, () -> drawBridgeRail(17.4));
        toro(4.2, 13.7, 0xffa844); toro(4.2, 18.3, 0xffa844);
        // the castle wall and its gatehouse
        solid(7.2, 3, 2.6, 11.6); solid(7.2, 17.4, 2.6, 14.6);
        prop(7.2, 3, 9.8, 12.4, () -> drawCurtain(7.2, 3, 2.6, 9.4, true));
        prop(7.2, 12.4, 9.8, 14.6, () -> drawPier(12.4, true));
        prop(7.2, 17.4, 9.8, 19.6, () -> drawPier(17.4, false));
        prop(7.2, 19.6, 9.8, 32, () -> drawCurtain(7.2, 19.6, 2.6, 12.4, true));
        prop(9.8, 12.4, 10.0, 19.6, ShogunCastle::drawGateTop);
        // north wall and the two corner turrets
        solid(7.2, 0, 2.8, 3); prop(7.2, 0, 10, 3, () -> drawTurret(7.2, 0, 2.8, 3));
        solid(10, .4, 18.8, 1.2); prop(10, .4, 28.8, 1.6, () -> drawCurtain(10, .4, 18.8, 1.2, false));
        solid(28.8, .2, 2.8, 3); prop(28.8, .2, 31.6, 3.2, () -> drawTurret(28.8, .2, 2.8, 3));
        // the keep
        solid(17.7, 2.7, 10.6, 9.1); prop(17.7, 2.7, 28.3, 11.8, ShogunCastle::drawKeep);
        // barracks
        solid(17, 26.5, 12.5, 2.5); prop(17, 26.5, 29.5, 29, () -> drawNagaya(17, 26.5, 12.5, 2.5));
        // light and colour along the road
        for (double[] t : new double[][]{{11.4, 14.2}, {11.4, 17.8}, {15.4, 14.2}, {15.4, 17.8}, {19.4, 14.2}, {19.4, 17.8}, {21.0, 20.4}, {24.6, 20.4}}) toro(t[0], t[1], 0xffa844);
        for (double[] b : new double[][]{{20.2, 13.0}, {25.8, 13.0}}) { solid(b[0] - .3, b[1] - .3, .6, .6); prop(b[0] - .4, b[1] - .4, b[0] + .4, b[1] + .4, () -> drawBrazier(b[0], b[1])); }
        banner(18.4, 13.4, 0x23284a); banner(27.6, 13.4, 0x23284a); banner(12.4, 12.8, 0x7a1e22); banner(12.4, 19.4, 0x7a1e22);
        // gardens
        rock(13.7, 7.0, 9); rock(14.9, 6.0, 6); rock(12.4, 8.1, 5); rock(13.5, 23.2, 9); rock(14.5, 22.2, 6);
        tree(11.6, 4.6, 95, 71); tree(11.4, 22.2, 90, 75);
        // trees and rocks around the grounds
        double[][] ts = {{1.6, 5.5, 115}, {3.2, 9.4, 100}, {2.4, 11.2, 105}, {2.6, 21.6, 110}, {1.4, 25.5, 120}, {3.6, 28.8, 100}, {1.4, 1.8, 130}, {30.2, 11.5, 105},
                {30.4, 16.5, 115}, {30.1, 21.5, 100}, {30.3, 25.5, 120}, {13, 30, 100}, {16, 30.6, 115}, {30.6, 30, 130}, {10.6, 28.4, 95}, {30.4, 6.6, 110}};
        for (int i = 0; i < ts.length; i++) tree(ts[i][0], ts[i][1], (int) ts[i][2], 41 + i * 6);
        rock(3.4, 13.0, 7); rock(3.6, 19.4, 6); rock(28.5, 22.5, 8); rock(12.5, 15.0 - 3.6, 6);
        for (double[] w : new double[][]{{2, 8, 30}, {2, 24, 28}}) wisp(w[0], w[1], w[2]);
    }
}
