package gameDev;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import static gameDev.Gfx.*;

public final class FallbackSprite {
    private FallbackSprite() { }

    private static final String KEY = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int[] PALETTE = {0x161616, 0x100C0B, 0x101212, 0x050606, 0x0A0C0C, 0x272727, 0x2A3136, 0x393C3A, 0x16191B, 0x212221, 0x2F252A, 0x000000, 0x4B3E31, 0x6B4F3D, 0xE6B588, 0x9C826C, 0x000001, 0x494F4F, 0x43211C, 0x180D0D, 0x1C2327, 0x121618, 0x2E3941, 0x222A2F, 0x3C4950, 0x333B3F, 0x2B0E0E, 0x171F23};
    private static final String[] ROWS = {
        ".........0102134222...........",
        ".......10567754256524.........",
        "......3877767775195503........",
        ".....496675765779357703.......",
        ".....220757556AB2384960.......",
        "....34090550706CDDD99694......",
        "....119829459DDEEEEF9794......",
        "...4483483350DDEEEEF092B......",
        "....3B223BG94B9FEEDGB02B......",
        ".....32BCD194FGAFEHIJ30B......",
        "......40DED0DEJDEEED9323......",
        "......42CFFFFEEEEEEEI42.......",
        ".......23GCEEEEEEEEEA1........",
        ".........43DEEEEEEED..........",
        ".........2KL3DFFFFD...........",
        "........8MM6L3JC244...........",
        ".......LNOOPPK0CA86.........6.",
        ".......466MMPOPMN883.....A.AD7",
        ".......4K6MOOMOOOMCFFA9CFFIDQ.",
        ".......4KKMMMOO6M0FEEEEEFIDQ..",
        "........K866OM6M8ICDDDDFCDQ...",
        "........R6RK666M248K..1DDJ....",
        "........KP6KK66NLKRA3F5CQC....",
        "........3J8KRR6RKK1FDHH9......",
        ".......29JQQJJJJJQJJIAHH......",
        "........C1QQIIJQQQ31CD97......",
        "......0CC9KLR2Q01J82II........",
        "......ADD96KKJQ8QJNK1J........",
        "....JQ9CC28NQIQRQJR68IJ1JJQ...",
        "...9QQ3LRKMRIINJIQKMM1IQQQIQ9.",
        ".9QQJ.26KNPQI0KQI3MPORJQQQ..Q.",
        "1Q0...NNROM6QR4QI8NOOO8..II...",
        "J0...R6LPOM6N6428NKOMO6..QQ...",
        ".....NMROOMK6PL8K68OOKPK......",
        "....8MNMOMO8MP2NKMKPOKP8......",
        "...KNMKOOMPRM64NKMNNORPR......",
        "..RNM6POPPNNOR46RMNKOMNK......",
        ".5NMONOONONNO4LNMMN8PORP......",
        ".KMONOOMNORMP3KN6M68PONP......",
        ".8KKKOO8PORM6BKK6M68OONMK.....",
        "...2NN68PMLMN383NNRLPNKN9.....",
        "...6FH42...KK......3OHH.......",
        "...7FFK............NFEF7H.....",
        "..7EFF656666666M660679HHH0...."
    };
    public static final int AX = 13;     
    public static final int LEG = 34;  
    public static final BufferedImage IMAGE = build();

    private static BufferedImage build() {
        int w = ROWS[0].length(), h = ROWS.length;
        BufferedImage im = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                char ch = ROWS[y].charAt(x);
                if (ch != '.') im.setRGB(x, y, 0xFF000000 | PALETTE[KEY.indexOf(ch)]);
            }
        return im;
    }

    private static void blit(Graphics2D g, int footX, boolean faceRight, int a, int b, int r0, int r1, int dy) {
        if (faceRight) g.drawImage(IMAGE, footX - AX + a, dy + r0, footX - AX + b, dy + r1, a, r0, b, r1, null);
        else g.drawImage(IMAGE, footX + AX - a + 1, dy + r0, footX + AX - b + 1, dy + r1, a, r0, b, r1, null);
    }

    public static void draw(Graphics2D g, int footX, int footY, boolean faceRight, boolean moving, double walk) {
        int w = IMAGE.getWidth(), h = IMAGE.getHeight();
        int step = ((int) (walk * 8)) % 2;
        int bob = (moving && step == 0) ? -1 : 0;
        int y0 = footY - h + 2;
        int liftL = (moving && step == 0) ? 2 : 0, liftR = (moving && step == 1) ? 2 : 0;
        blit(g, footX, faceRight, 0, AX, LEG, h, y0 - liftL);
        blit(g, footX, faceRight, AX, w, LEG, h, y0 - liftR);
        blit(g, footX, faceRight, 0, w, 0, LEG + 1, y0 + bob);
    }
}
