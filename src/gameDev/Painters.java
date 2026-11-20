package gameDev;

import java.awt.*;
import java.util.*;
import java.util.List;
import static gameDev.Gfx.*;


public final class Painters {
    private Painters() { }

    static void drawGable(double y, double d, double z, double rh, Color roof, double xa, double xb, double ov) {
        quad(shade(roof, 1.2), xa, y - ov, z, xb, y - ov, z, xb, y + d / 2, z + rh, xa, y + d / 2, z + rh);
        quad(shade(roof, .86), xa, y + d + ov, z, xb, y + d + ov, z, xb, y + d / 2, z + rh, xa, y + d / 2, z + rh);
        quad(shade(roof, .52), xb, y - ov, z, xb, y + d + ov, z, xb, y + d / 2, z + rh);
        Color tl = shade(roof, .58);
        for (int k = 1; k < 4; k++) {
            double f = k / 4.0;
            ln(tl, xa, y + d / 2 + (d / 2 + ov) * f, z + rh * (1 - f), xb, y + d / 2 + (d / 2 + ov) * f, z + rh * (1 - f));
            ln(shade(roof, .9), xa, y + d / 2 - (d / 2 + ov) * f, z + rh * (1 - f), xb, y + d / 2 - (d / 2 + ov) * f, z + rh * (1 - f));
        }
        ln(shade(roof, 1.55), xa, y + d / 2, z + rh, xb, y + d / 2, z + rh);
        ln(shade(roof, .35), xa, y + d + ov, z, xb, y + d + ov, z);
        ln(shade(roof, .35), xb, y + d + ov, z, xb, y + d / 2, z + rh);
    }
    static void drawRafters(double x0, double x1, double y, double d, double z, double rh) {
        if (x1 - x0 < .2) return;
        Color wood = col(0x3b281c);
        int n = Math.max(2, (int) ((x1 - x0) * 1.6));
        for (int i = 0; i < n; i++) {
            double rx = x0 + (x1 - x0) * (i + .5) / n;
            ln(wood, rx, y + d + .1, z, rx, y + d / 2, z + rh * .92);
            ln(wood, rx, y - .1, z, rx, y + d / 2, z + rh * .92);
        }
        ln(wood, x0, y + d / 2, z + rh * .92, x1, y + d / 2, z + rh * .92);
    }

    static final Color J_DARK = col(0x120c0f), J_STONE = col(0x5d5852), J_WOOD = col(0x2d2119);

    static double jX, jY, jW, jD;
    static boolean jF;
    static void jFace(boolean front, double x, double y, double w, double d) { jF = front; jX = x; jY = y; jW = w; jD = d; }
    static double jL() { return jF ? jW : jD; }
    static double jcx(double u) { return jF ? jX + jW * u : jX + jW; }
    static double jcy(double u) { return jF ? jY + jD : jY + jD * u; }
    static void jq(double u0, double u1, double za, double zb, Color c) { if (jF) wallL(jX, jY, jW, jD, u0, u1, za, zb, c); else wallR(jX, jY, jW, jD, u0, u1, za, zb, c); }
    static void jl(Color c, double u0, double z0, double u1, double z1) { ln(c, jcx(u0), jcy(u0), z0, jcx(u1), jcy(u1), z1); }
    static void jpost(double u, double pw, double za, double zb, Color c) { double L = jL(); jq(Math.max(0, u - pw / 2 / L), Math.min(1, u + pw / 2 / L), za, zb, c); }
    static void jpoly(Color c, double... uz) {
        int n = uz.length / 2; double[] p = new double[n * 3];
        for (int i = 0; i < n; i++) { p[3 * i] = jcx(uz[2 * i]); p[3 * i + 1] = jcy(uz[2 * i]); p[3 * i + 2] = uz[2 * i + 1]; }
        quad(c, p);
    }
    static double jc(double v, double a, double b) { return Math.max(a, Math.min(b, v)); }
    static void jShadow(double x, double y, double w, double d) {
        fillq(new Color(0, 0, 0, 70), x + .3, y + .3, 0, x + w + .9, y + .3, 0, x + w + .9, y + d + .9, 0, x + .3, y + d + .9, 0);
    }

    static void jFrameWall(Color plaster, Color timber, double z0, double z1, double lum, int sd, boolean boards, int dmg, Runnable openings) {
        double L = jL(), H = z1 - z0;
        int n = Math.max(2, (int) Math.round(L / 1.25));
        double zs = boards ? z0 + H * .30 : z0;
        Color pl = shade(plaster, lum), tb = shade(timber, lum);
        jq(0, 1, z0, z1, pl);
        Color st = shade(mix(plaster, col(0x1c140e), .4), lum);                       // rain streaks
        for (int i = 0; i < n * 3; i++) { double u = hash(i, sd, 3), len = 3 + hash(i, sd, 4) * H * .45; jl(st, u, z1 - 2, u, z1 - 2 - len); }
        for (int i = 0; i < n; i++) if (hash(i, sd, 5) < (dmg > 0 ? .5 : .3)) {       // plaster has fallen off: bare mud + bamboo lath
            double a = (i + .12 + hash(i, sd, 6) * .2) / n, b = a + (.4 + hash(i, sd, 7) * .35) / n;
            double za = zs + H * (.12 + hash(i, sd, 8) * .3), zb = Math.min(z1 - 3, za + 5 + hash(i, sd, 9) * 7);
            jq(a, b, za, zb, shade(col(0x5b4632), lum));
            for (double zz = za + 1.5; zz < zb; zz += 2.5) jl(shade(col(0x8a7456), lum), a, zz, b, zz);
        }
        if (boards) {
            jq(0, 1, z0, zs, shade(col(0x4a382b), lum));
            Color bl = shade(col(0x2a1e17), lum);
            int nb = (int) Math.round(L / .4);
            for (int i = 1; i < nb; i++) { double u = (double) i / nb; jl(bl, u, z0, u, zs - (hash(i, sd, 10) < .15 ? 3 : 0)); }
            for (int i = 0; i < nb; i++) if (hash(i, sd, 11) < .07) jq((double) i / nb, (i + .8) / nb, z0, zs, J_DARK);   // missing boards
            jl(shade(col(0x7a6046), lum), 0, zs, 1, zs);
        } else jq(0, 1, z0, z0 + 2, shade(pl, .7));
        for (int i = 0; i < n * 2; i++) if (hash(i, sd, 13) < .4) {                    // moss at the foot of the wall
            double a = hash(i, sd, 14); jq(a, Math.min(1, a + .12), z0, z0 + 1 + hash(i, sd, 15) * 3, shade(col(0x4a5a30), lum));
        }
        if (openings != null) openings.run();
        for (int i = 0; i <= n; i++) jpost((double) i / n, .2, z0, z1, tb);              // exposed timber frame
        jq(0, 1, z1 - 2, z1, tb);
        if (boards) jq(0, 1, zs - .6, zs + .6, tb);
        for (int i = 0; i < n; i++) if (hash(i, sd, 12) < .3) jl(tb, (double) i / n, zs + 1, (i + 1.0) / n, z1 - 2);   // diagonal brace
    }

    static void jShoji(double u0, double u1, double za, double zb, double lum, boolean lit, int seed) {
        double L = jL(), pu = .07 / L;
        Color fr = shade(col(0x2b1f18), lum);
        jq(u0, u1, za, zb, fr);
        Color paper = lit ? col(0xe8a040) : mix(shade(col(0x9a9482), lum), col(0x2a2622), .3);
        jq(u0 + pu, u1 - pu, za + 1, zb - 1, paper);
        int cols = Math.max(2, (int) Math.round((u1 - u0) * L / .55));
        for (int i = 1; i < cols; i++) { double u = u0 + (u1 - u0) * i / cols; jl(fr, u, za + 1, u, zb - 1); }
        for (int j = 1; j < 3; j++) { double z = za + (zb - za) * j / 3; jl(fr, u0, z, u1, z); }
        if (!lit && hash(seed, 1, 21) < .6) {                                              // torn paper
            double a = u0 + (u1 - u0) * (.2 + hash(seed, 2, 22) * .3), b = a + (u1 - u0) * .3, zA = za + (zb - za) * (.25 + hash(seed, 3, 23) * .3);
            jpoly(J_DARK, a, zA, b, zA + 1, b - (b - a) * .2, zA + (zb - za) * .45, a + (b - a) * .3, zA + (zb - za) * .4);
        }
    }
    static void jDoor(double u0, double u1, double za, double zb, double lum, int seed) {
        jq(u0, u1, za, zb, J_DARK);
        jq(u0, u1, zb - 1.5, zb, shade(J_WOOD, lum));
        double zt = zb - 2;
        Color cloth = shade(col(0x2f4a6a), lum);
        for (int i = 0; i < 3; i++) {
            double a = u0 + (u1 - u0) * (i + .06) / 3, b = u0 + (u1 - u0) * (i + .94) / 3, len = 5 + hash(seed, i, 24) * 5;
            jq(a, b, zt - len, zt, cloth); jl(shade(cloth, 1.4), a, zt, b, zt);
        }
    }
    static void jLattice(double u0, double u1, double za, double zb, double lum, Color glow) {
        jq(u0, u1, za, zb, shade(col(0x3a2b20), lum));
        jq(u0 + .012, u1 - .012, za + 1, zb - 1, glow != null ? glow : J_DARK);
        double L = jL(); int nb = Math.max(3, (int) Math.round((u1 - u0) * L / .22));
        for (int i = 1; i < nb; i++) { double u = u0 + (u1 - u0) * i / nb; jl(shade(col(0x4a382b), lum), u, za + 1, u, zb - 1); }
    }

    static void jVeranda(double x, double y, double w, double bd, double vd, double z0, double zE, int sd) {
        double yf = y + bd + vd, d = bd + vd;
        Color wood = col(0x5e4a38);
        wallL(x, y, w, d, 0, 1, 0, z0 - 1, J_DARK);
        wallR(x, y, w, d, bd / d, 1, 0, z0 - 1, col(0x08060a));
        wallL(x, y, w, d, 0, 1, z0 - 1, z0 + .5, shade(wood, .7));
        wallR(x, y, w, d, bd / d, 1, z0 - 1, z0 + .5, shade(wood, .45));
        quad(wood, x, y + bd, z0 + .5, x + w, y + bd, z0 + .5, x + w, yf, z0 + .5, x, yf, z0 + .5);
        for (double yy = y + bd + .28; yy < yf - .05; yy += .28) ln(shade(wood, .76), x, yy, z0 + .5, x + w, yy, z0 + .5);
        for (int i = 0; i < 3; i++) if (hash(i, sd, 61) < .6) {                            // rotten boards
            double bx = x + hash(i, sd, 62) * (w - .8);
            quad(J_DARK, bx, yf - .5, z0 + .52, bx + .5, yf - .5, z0 + .52, bx + .5, yf - .2, z0 + .52, bx, yf - .2, z0 + .52);
        }
        int np = Math.max(2, (int) Math.round(w / 1.8));
        for (int i = 0; i <= np; i++) {
            double px = x + .15 + (w - .5) * i / np;
            box(px, yf - .38, .26, .26, 0, z0 - 1, col(0x55524e));
            if (!(hash(i, sd, 63) < .15 && i != 0 && i != np)) box(px + .03, yf - .35, .2, .2, z0 + .5, zE - z0 - .5, J_WOOD);
        }
    }

    static double RXA, RXB, RYA, RYB, RYM, RRI, RZ, RRH;
    static double RC = 1, RUP = 0;      // roof curve: RC > 1 = concave Japanese profile, RUP = how far the eave corners flick upward
    static boolean RSHR = false;        // shrine ridge: katsuogi logs + chigi finials
    static double rz(double t, double e) { return RZ + RRH * (RC == 1 ? t : Math.pow(t, RC)) + RUP * e * (1 - t) * (1 - t); }
    static double ru(double u) { return RUP == 0 ? 0 : Math.pow(Math.abs(2 * u - 1), 3); }
    static double[] rrF(double u, double t) { double xl = RXA + RRI * t, xr = RXB - RRI * t; return new double[]{xl + (xr - xl) * u, RYB + (RYM - RYB) * t, rz(t, ru(u))}; }
    static double[] rrB(double u, double t) { double xl = RXA + RRI * t, xr = RXB - RRI * t; return new double[]{xl + (xr - xl) * u, RYA + (RYM - RYA) * t, rz(t, ru(u))}; }
    static double[] rrS(double s, double t) { double y0 = RYA + (RYB - RYA) * s; return new double[]{RXB - RRI * t, y0 + (RYM - y0) * t, rz(t, ru(s))}; }
    static double[] rrAny(int k, double a, double b) { return k == 0 ? rrB(a, b) : k == 1 ? rrF(a, b) : rrS(a, b); }
    static void slope(int k, Color c) {
        if (RC == 1 && RUP == 0) {
            if (k == 2) rpoly(c, rrS(0, 0), rrS(1, 0), rrS(.5, 1)); else rpoly(c, rrAny(k, 0, 0), rrAny(k, 1, 0), rrAny(k, 1, 1), rrAny(k, 0, 1));
            return;
        }
        int nu = 8, nt = 6;
        for (int i = 0; i < nu; i++) for (int j = 0; j < nt; j++)
            rpoly(c, rrAny(k, (double) i / nu, (double) j / nt), rrAny(k, (i + 1.0) / nu, (double) j / nt), rrAny(k, (i + 1.0) / nu, (j + 1.0) / nt), rrAny(k, (double) i / nu, (j + 1.0) / nt));
    }
    static void eaveStrip(Color eF, Color eS, double th, double z) {
        if (RUP == 0) {
            quad(eF, RXA, RYB, z, RXB, RYB, z, RXB, RYB, z - th, RXA, RYB, z - th);
            quad(eS, RXB, RYA, z, RXB, RYB, z, RXB, RYB, z - th, RXB, RYA, z - th);
            return;
        }
        for (int i = 0; i < 8; i++) {
            double[] p = rrF(i / 8.0, 0), q = rrF((i + 1) / 8.0, 0), a = rrS(i / 8.0, 0), b = rrS((i + 1) / 8.0, 0);
            quad(eF, p[0], p[1], p[2], q[0], q[1], q[2], q[0], q[1], q[2] - th, p[0], p[1], p[2] - th);
            quad(eS, a[0], a[1], a[2], b[0], b[1], b[2], b[0], b[1], b[2] - th, a[0], a[1], a[2] - th);
        }
    }
    static void rpoly(Color c, double[]... p) {
        double[] a = new double[p.length * 3];
        for (int i = 0; i < p.length; i++) { a[3 * i] = p[i][0]; a[3 * i + 1] = p[i][1]; a[3 * i + 2] = p[i][2]; }
        quad(c, a);
    }
    static void rline(Color c, double[] p, double[] q) { ln(c, p[0], p[1], p[2], q[0], q[1], q[2]); }
    /** ragged hole in the front slope: dark inside, broken rafters showing */
    static void jHole(double u, double t, double ru, double rt, int seed) { jHole(u, t, ru, rt, seed, false); }
    static void jHole(double u, double t, double ru, double rt, int seed, boolean cave) {
        int n = 9; double[][] v = new double[n][];
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n, r = .65 + hash(i, seed, 51) * .6;
            v[i] = rrF(jc(u + Math.cos(a) * ru * r, 0, 1), jc(t + Math.sin(a) * rt * r, .02, .98));
        }
        rpoly(col(0x150d0c), v);
        Color wd = col(0x5e4630);
        if (cave) {                                                                          
            double[][] sl = {rrF(jc(u - ru * .7, 0, 1), jc(t - rt * .15, .02, .98)), rrF(jc(u + ru * .8, 0, 1), jc(t - rt * .5, .02, .98)), rrF(jc(u + ru * .6, 0, 1), jc(t + rt * .25, .02, .98)), rrF(jc(u - ru * .1, 0, 1), jc(t + rt * .1, .02, .98))};
            for (double[] q : sl) q[2] -= 3;
            rpoly(shade(col(0x6a5a3c), .8), sl);
            for (int i = 0; i < 4; i++) rline(shade(col(0x6a5a3c), .55), new double[]{sl[0][0] + (sl[1][0] - sl[0][0]) * i / 4, sl[0][1] + (sl[1][1] - sl[0][1]) * i / 4, sl[0][2]}, new double[]{sl[3][0] + (sl[2][0] - sl[3][0]) * i / 4, sl[3][1] + (sl[2][1] - sl[3][1]) * i / 4, sl[3][2]});
            for (int i = 0; i < n; i += 2) { double[] q = v[i]; ln(wd, q[0], q[1], q[2], q[0] + (hash(i, seed, 52) - .5) * .4, q[1] - .15, q[2] + 4 + hash(i, seed, 53) * 5); ln(shade(wd, 1.3), q[0] + .05, q[1], q[2], q[0] + (hash(i, seed, 52) - .5) * .4 + .05, q[1] - .15, q[2] + 4 + hash(i, seed, 53) * 5); }
        } else for (int i = -1; i <= 1; i++) rline(wd, rrF(jc(u - ru * .9, 0, 1), jc(t + i * rt * .4 - rt * .3, .02, .98)), rrF(jc(u + ru * .9, 0, 1), jc(t + i * rt * .4 + rt * .3, .02, .98)));
        for (int i = 0; i < n; i += 2) rline(shade(wd, 1.4), v[i], v[(i + 1) % n]);
    }
    static void jRoof(double x, double y, double w, double d, double z, double rh, double ov, boolean hip, boolean thatch, Color base, Color gable, int sd, int dmg) {
        RXA = x - ov; RXB = x + w + ov; RYA = y - ov; RYB = y + d + ov; RYM = (RYA + RYB) / 2; RZ = z; RRH = rh;
        RRI = hip ? Math.min((RYB - RYA) / 2 * .5, (RXB - RXA) / 2 - .3) : 0;
        slope(0, shade(base, 1.12));
        if (hip) rpoly(base, rrB(0, 0), rrF(0, 0), rrF(0, 1));
        slope(1, shade(base, .86));
        slope(2, hip ? shade(base, .72) : shade(gable, .66));
        int rows = thatch ? 7 : 10;
        Color dk = shade(base, thatch ? .6 : .72), hi = shade(base, thatch ? 1.35 : 1.22);
        for (int k = 1; k < rows; k++) {
            double t = (double) k / rows;
            if (thatch) {
                for (int s = 0; s < 6; s++) if (hash(k, s, sd + 31) > .2) rline(dk, rrF(s / 6.0, t), rrF((s + 1) / 6.0, t));
                for (int s = 0; s < 9; s++) { double u = hash(k, s, sd + 32); rline(hi, rrF(u, t), rrF(Math.min(1, u + .05), t + .9 / rows)); rline(dk, rrF(Math.min(1, u + .03), t), rrF(Math.min(1, u + .08), t + .9 / rows)); }
                if (hip) for (int s = 0; s < 5; s++) { double u = hash(k, s, sd + 33); rline(hi, rrS(u, t), rrS(Math.min(1, u + .06), t + .9 / rows)); }
            } else {
                for (int i = 0; i < 8; i++) rline(dk, rrF(i / 8.0, t), rrF((i + 1) / 8.0, t));
                for (double u = (k % 2) * .06 + .06; u < 1; u += .12) rline(dk, rrF(u, t - 1.0 / rows), rrF(u, t));
            }
            if (hip) for (int i = 0; i < 8; i++) rline(shade(dk, .85), rrS(i / 8.0, t), rrS((i + 1) / 8.0, t));
        }
        for (int i = 0; i < 4; i++) if (hash(i, sd, 41) < (dmg > 0 ? .9 : .6)) {              // moss
            double u = hash(i, sd, 42) * .8, t = .1 + hash(i, sd, 43) * .6, wd = .12 + hash(i, sd, 44) * .1;
            rpoly(mix(base, col(0x4f6a36), .55), rrF(u, t), rrF(u + wd, t), rrF(u + wd + .02, t + .1), rrF(u + .03, t + .12));
        }
        double th = thatch ? 3.2 : 2.0;                                                       // thick edge of the roof
        Color eF = thatch ? col(0x8a7650) : col(0x25252b), eS = shade(eF, .62);
        eaveStrip(eF, eS, th, z);
        if (thatch) for (double xx = RXA + .2; xx < RXB; xx += .45) ln(shade(eF, .6), xx, RYB, z - .5, xx, RYB, z - th + (hash((int) (xx * 9), sd, 45) < .4 ? .5 : 0));
        else for (int i = 0; i < 8; i++) { double[] p = rrF(i / 8.0, 0), q = rrF((i + 1) / 8.0, 0); ln(shade(base, 1.5), p[0], p[1], p[2] - .4, q[0], q[1], q[2] - .4); }
        if (dmg == 1) { jHole(.28, .42, .13, .2, sd); jHole(.74, .28, .09, .13, sd + 1); }
        if (dmg == 2) jHole(.46, .45, .26, .3, sd, true);
        if (dmg == 3) { jHole(.76, .4, .19, .3, sd, true); jHole(.2, .3, .08, .12, sd + 3); }
        double[] r0 = rrF(0, 1), r1 = rrF(1, 1); double zt = z + rh, len = r1[0] - r0[0];
        double[][] pieces = dmg == 2 ? new double[][]{{0, .3}, {.84, 1}} : dmg == 3 ? new double[][]{{0, .58}, {.93, 1}} : new double[][]{{0, 1}};
        for (double[] pc : pieces) {
            double a = r0[0] + len * pc[0], b = r0[0] + len * pc[1];
            if (b - a < .1) continue;
            if (thatch) {
                Color rc = mix(shade(base, 1.25), col(0x8a7650), .4);
                box(a, RYM - .3, b - a, .6, zt - .6, 3, rc);
                for (double xx = a + .35; xx < b; xx += .55) ln(shade(rc, .6), xx, RYM + .3, zt - .6, xx, RYM + .3, zt + 2.4);
            } else if (RSHR) {
                box(a, RYM - .26, b - a, .52, zt - .3, 2.4, shade(base, .55));
                for (double xx = a + .3; xx < b - .15; xx += .5) box(xx, RYM - .42, .18, .84, zt + 2, 1.7, col(0x9a8660));   // katsuogi: the short logs on a shrine ridge
            } else {
                box(a, RYM - .22, b - a, .44, zt - .3, 2.6, shade(base, .6));
            }
        }
        if (hip) for (int e = 0; e < 2; e++) {
            double xe = e == 0 ? r0[0] + .1 : r1[0] - .1;
            if (thatch) { Color wd = col(0x3a2a1e); ln(wd, xe, RYM - .4, zt + 1, xe, RYM + .4, zt + 6); ln(wd, xe, RYM + .4, zt + 1, xe, RYM - .4, zt + 6); }
            else if (RSHR) { Color wd = col(0xb09a6c); for (int k = 0; k < 2; k++) { ln(wd, xe + k * .05, RYM - .6, zt + 1, xe + k * .05, RYM + .6, zt + 11); ln(wd, xe + k * .05, RYM + .6, zt + 1, xe + k * .05, RYM - .6, zt + 11); } }
            else { box(xe - .12, RYM - .26, .25, .52, zt - .3, 5, shade(base, .5)); box(xe - .09, RYM - .2, .19, .4, zt + 4.7, 1, shade(base, 1.2)); }
        }
        if (!hip) {                                                                           // barge boards on the gable end
            Color bb = shade(gable, .4);
            ln(bb, RXB, RYA, z, RXB, RYM, zt); ln(bb, RXB, RYM, zt, RXB, RYB, z);
            if (!thatch) { box(RXB - .25, RYM - .26, .25, .52, zt - .3, 5, shade(base, .5)); box(RXA, RYM - .26, .25, .52, zt - .3, 5, shade(base, .5)); }
        }
    }

    //the six house types
    static void jDiag(Color c, double L, double a0, double z0, double a1, double z1) {
        if (a0 > a1) { double t = a0; a0 = a1; a1 = t; t = z0; z0 = z1; z1 = t; }
        if (a1 < 0 || a0 > L) return;
        if (a0 < 0) { z0 += (z1 - z0) * (0 - a0) / (a1 - a0); a0 = 0; }
        if (a1 > L) { z1 = z0 + (z1 - z0) * (L - a0) / (a1 - a0); a1 = L; }
        jl(c, a0 / L, z0, a1 / L, z1);
    }

    static void drawFence(double x, double y, int n, boolean alongX) {
        double step = .5;
        int[] px = new int[n], py = new int[n], ph = new int[n];
        for (int i = 0; i < n; i++) {
            double fx = alongX ? x + i * step : x, fy = alongX ? y : y + i * step;
            px[i] = sx(fx, fy); py[i] = sy(fx, fy, 0);
            ph[i] = (hash(i, (int) (x * 3 + y), 4) < .25) ? 6 : 11 + (int) (hash(i, (int) y, 6) * 4);
        }
        g.setColor(col(0x4a3626));
        for (int i = 1; i < n; i++) {
            if (hash(i, (int) (x + y), 8) < .8) g.drawLine(px[i - 1], py[i - 1] - 5, px[i], py[i] - 6);
            if (hash(i, (int) (x + y), 9) < .5) g.drawLine(px[i - 1], py[i - 1] - 9, px[i], py[i] - 10);
        }
        for (int i = 0; i < n; i++) {
            g.setColor(col(0x5a432f)); g.fillRect(px[i] - 1, py[i] - ph[i], 2, ph[i]);
            g.setColor(col(0x7a5e42)); g.fillRect(px[i] - 1, py[i] - ph[i], 1, 1);
        }
    }

    static int toff(int r, int seed) { return (int) Math.round(Math.sin(r * .09 + seed) * 2.5 + r * .025 * ((seed & 1) == 0 ? 1 : -1)); }
    static void drawTree(double x, double y, int H, int seed) {
        int bx = sx(x, y), by = sy(x, y, 0);
        shadowOval(bx, by - 1, 10, 3);
        Color trunk = col(0x2b2125), hi = col(0x45353a);
        for (int r = 0; r < H; r++) {
            int off = toff(r, seed), wd = Math.max(1, 5 - r / (H / 4 + 1)) + (r < 5 ? 2 : 0);
            g.setColor(trunk); g.fillRect(bx + off - wd / 2, by - r, wd, 1);
            g.setColor(hi); g.fillRect(bx + off - wd / 2, by - r, 1, 1);
        }
        Random rr = new Random(seed * 977L);
        for (int i = 0; i < 6; i++) {
            int r = (int) (H * (.32 + .11 * i)), off = toff(r, seed);
            int dir = (i % 2 == 0) ? -1 : 1, len = 9 + rr.nextInt(12);
            int sxp = bx + off, syp = by - r, ex = sxp + dir * len, ey = syp - 5 - rr.nextInt(9);
            g.setColor(trunk);
            g.drawLine(sxp, syp, ex, ey); g.drawLine(sxp, syp - 1, ex, ey - 1);
            if (i < 3) g.drawLine(sxp, syp + 1, ex, ey + 1);
            g.drawLine(ex, ey, ex + dir * 4, ey - 5); g.drawLine(ex, ey, ex + dir * 6, ey - 1);
            g.drawLine(ex - dir * 3, ey + 2, ex - dir * 2, ey - 4);
        }
    }

    static void drawBanner(double x, double y, Color c) {
        int bx = sx(x, y), by = sy(x, y, 0);
        shadowOval(bx, by, 4, 2);
        g.setColor(col(0x3a2a20)); g.fillRect(bx - 1, by - 50, 2, 50);
        g.fillRect(bx - 1, by - 52, 14, 2);
        int fl = (int) Math.round(Math.sin(time * 3 + x) * 1.5);
        int[] xs = {bx + 1, bx + 12, bx + 12 + fl, bx + 10 + fl, bx + 12 + fl, bx + 6, bx + 1};
        int[] ys = {by - 50, by - 50, by - 36, by - 31, by - 24, by - 28, by - 24};
        g.setColor(c); g.fillPolygon(xs, ys, xs.length);
        g.setColor(shade(c, .6)); g.drawPolygon(xs, ys, xs.length);
        g.setColor(col(0xe3d8c0)); g.drawOval(bx + 4, by - 46, 5, 5); g.drawLine(bx + 3, by - 38, bx + 10, by - 45);
    }

    static final int LAMP_H = 48;   // lamp post height in pixels

    static void drawToro(double x, double y, int rgb) {
        Color st = col(0x8a8884);
        fillq(new Color(0, 0, 0, 55), x - .4, y - .4, 0, x + .9, y - .4, 0, x + .9, y + .9, 0, x - .4, y + .9, 0);
        box(x - .3, y - .3, .6, .6, 0, 3, st);
        box(x - .15, y - .15, .3, .3, 3, 9, shade(st, .95));
        box(x - .38, y - .38, .76, .76, 12, 6, st);
        wallL(x - .38, y - .38, .76, .76, .22, .78, 13.5, 16.5, col(rgb));
        box(x - .52, y - .52, 1.04, 1.04, 18, 2, shade(st, 1.1));
        box(x - .26, y - .26, .52, .52, 20, 2, st);
        light(x, y + .2, 15, 50, rgb);
    }

    static void drawFire(double x, double y, boolean spirit) {
        int bx = sx(x, y), by = sy(x, y, 0);
        shadowOval(bx, by, 14, 5);
        g.setColor(new Color(20, 12, 10, 120)); g.fillOval(bx - 11, by - 4, 22, 8);
        for (int i = 0; i < 10; i++) {
            double a = i * Math.PI * 2 / 10;
            int px = bx + (int) Math.round(Math.cos(a) * 11), py = by + (int) Math.round(Math.sin(a) * 5) - 1;
            g.setColor(col(i % 2 == 0 ? 0x6a6a6c : 0x4a4a4e)); g.fillRect(px - 1, py - 1, 3, 2);
        }
        g.setColor(col(0x4a2f1c)); g.drawLine(bx - 7, by - 1, bx + 7, by - 4); g.drawLine(bx - 7, by - 4, bx + 7, by - 1);
        g.drawLine(bx - 7, by - 2, bx + 7, by - 5);
        double t = time * 10;
        int[] hs = {16 + (int) (Math.sin(t + x) * 3), 11 + (int) (Math.sin(t * 1.3 + 2) * 2), 6 + (int) (Math.sin(t * 1.7) * 2)};
        int[] ws = {9, 6, 3};
        Color[] cs = spirit ? new Color[]{col(0x1f8f86), col(0x37d1b8), col(0xcafff0)} : new Color[]{col(0xd8481f), col(0xf28a26), col(0xffd868)};
        for (int l = 0; l < 3; l++) {
            g.setColor(cs[l]);
            for (int r = 0; r < hs[l]; r++) {
                int wd = Math.max(1, (int) Math.round(ws[l] * (1 - Math.pow(r / (double) hs[l], 1.7))));
                int off = (int) Math.round(Math.sin(t * .8 + r * .4 + l) * r / 6.0);
                g.fillRect(bx - wd / 2 + off, by - 3 - r, wd, 1);
            }
        }
        double fl = 1 + Math.sin(time * 13) * .08;
        light(x, y, 10, (int) (74 * fl), spirit ? 0x40e0c8 : 0xff8a30);
    }

    static final double WELL_H = 1.5;   // well height multiplier

    static void drawRock(double x, double y, int sz) {
        int bx = sx(x, y), by = sy(x, y, 0);
        shadowOval(bx + 2, by, sz, sz / 3 + 1);
        int[] xs = {bx - sz, bx - sz * 2 / 3, bx + sz / 4, bx + sz, bx + sz * 2 / 3};
        int[] ys = {by, by - sz * 2 / 3 - 2, by - sz - 2, by - sz / 2, by};
        g.setColor(col(0x6d6a6b)); g.fillPolygon(xs, ys, 5);
        g.setColor(col(0x8e8b8c)); g.fillPolygon(new int[]{bx - sz, bx - sz * 2 / 3, bx + sz / 4, bx - sz / 4}, new int[]{by, by - sz * 2 / 3 - 2, by - sz - 2, by - sz / 3}, 4);
        g.setColor(col(0x3d3a3c)); g.drawPolygon(xs, ys, 5);
    }

    static void bx(boolean alongX, double u, double v, double du, double dv, double z0, double h, Color c) {
        if (alongX) box(u, v, du, dv, z0, h, c); else box(v, u, dv, du, z0, h, c);
    }
    

    static final double TORII_H = 1.4;

    static void drawTorii(double cx, double cy, double span, boolean alongX, boolean broken, boolean beamOnly, int which) {

	    final double k = TORII_H;

	    Color red = col(0xa83530);
	    Color blk = col(0x231b20);
	    Color moss = col(0x405b35);
	    Color darkMoss = col(0x293e29);
	    Color nemaki = col(0x302329);
	    Color nemakiEdge = col(0x554047);
	    Color contactShadow = new Color(0, 0, 0, 100);

	    double half = span / 2;
	    double pw = .5;

	    double u0 = (alongX ? cx : cy) - half;
	    double u1 = (alongX ? cx : cy) + half;
	    double v = (alongX ? cy : cx);

	    double top = 41 * k;        
	    double pTop = top + 2;       

	    final double sleeveH = 4.5, sw = .68;
	    for (int p = 0; p < 2; p++) {
	        if (!beamOnly && which == p) {
	            double u = (p == 0) ? u0 : u1;
	            double pillarTop = (p == 1 && broken) ? 28 * k : pTop;
	            bx(alongX, u - .42, v - .42, .84, .84, 0, 1.5, contactShadow);                       
	            if (broken) {                                                                        
	                if (p == 0) { bx(alongX, u - .38, v - .32, .30, .20, 0, .08, moss); bx(alongX, u + .18, v + .12, .22, .18, 0, .06, darkMoss); }
	                else        { bx(alongX, u - .30, v + .12, .28, .18, 0, .07, moss); bx(alongX, u + .12, v - .30, .22, .20, 0, .06, darkMoss); }
	            }
	            bx(alongX, u - sw / 2, v - sw / 2, sw, sw, 0, sleeveH, nemaki);                       // NEMAKI sleeve
	            bx(alongX, u - sw / 2, v + sw / 2 - .1, sw, .1, 0, sleeveH, nemakiEdge);              // its lit front edge
	            bx(alongX, u - pw / 2, v - pw / 2, pw, pw, sleeveH, pillarTop - sleeveH, red);        // PILLAR, standing on the sleeve
	        }
	    }

	    if (beamOnly && which == 0) {
	        double a0 = u0 + pw / 2, a1 = u1 - pw / 2;
	        bx(alongX, a0, v - .19, a1 - a0, .38, 22 * k, 2 * k, red);                     // NUKI
	        if (!broken) bx(alongX, (u0 + u1) / 2 - .17, v - .19, .34, .38, 24 * k, 9 * k, red);   // central support
	        bx(alongX, a0, v - .17, a1 - a0, .34, 33 * k, 2.5 * k, red);                   // upper red crossbeam
	        if (broken) bx(alongX, u1 - .9, v - .2, .35, .4, 33 * k, 6, shade(blk, 1.4));   // dangling broken section
	        Color rope = col(0xcdbb90);                                                     // SHIMENAWA
	        for (int i = 0; i < 6; i++) {
	            double p0 = a0 + (a1 - a0) * i / 6, p1 = a0 + (a1 - a0) * (i + 1) / 6;
	            ln(rope, alongX ? p0 : v, alongX ? v : p0, 31 * k - (i % 2), alongX ? p1 : v, alongX ? v : p1, 31 * k - ((i + 1) % 2));
	        }
	        if (broken) {                                                                   // fallen wooden fragments
	            Color wood = col(0x594034), darkWood = col(0x382921);
	            bx(alongX, u0 - .85, v + .55, .35, .12, 0, .08, wood); bx(alongX, u1 + .35, v - .65, .28, .13, 0, .06, darkWood);
	            bx(alongX, u0 + .65, v + .75, .20, .10, 0, .05, wood);
	        }
	    }
	    // BLACK TOP BEAM with raised ends, resting on both pillars
	    if (beamOnly && which == 2) {
	        if (!broken) {
	            bx(alongX, u0 - .7, v - .25, span + 1.4, .5, top, 3, blk);
	            bx(alongX, u0 - 1.0, v - .25, .4, .5, top + 2, 2, blk);
	            bx(alongX, u1 + .6, v - .25, .4, .5, top + 2, 2, blk);
	        } else {
	            bx(alongX, u0 - .7, v - .25, span * .55, .5, top, 3, blk);
	            bx(alongX, u0 - 1.0, v - .25, .4, .5, top + 2, 2, blk);
	        }
	    }
	}


}
