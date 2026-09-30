package dev.normlanguage.theme;

public final class ColorMathTest {
    public static void main(String[] args) {
        int white = ColorMath.parse("#FFFFFF");
        int black = ColorMath.parse("#000000");
        check(ColorMath.contrast(white, black) == 21.0, "contrast reference");
        check(ColorMath.format(ColorMath.parse("#b2dbeb")).equals("#B2DBEBFF"), "hex canonicalization");
        check(ColorMath.tone(white, 0.0, 1.0) == black, "black endpoint");
        check(ColorMath.tone(black, 1.0, 1.0) == white, "white endpoint");
        check(ColorMath.format(ColorMath.tone(ColorMath.parse("#FF0000"), 0.6279553606, 1.0)).equals("#FF0000FF"), "Oklab red reference");
        for (String seed : new String[]{"#B2DBEB", "#FF0000", "#00FF00", "#0000FF", "#FFFFFF", "#000000"}) {
            double previous = 1.0;
            for (int index = 0; index <= 100; index++) {
                int color = ColorMath.tone(ColorMath.parse(seed), index / 100.0, 1.0);
                double contrast = ColorMath.contrast(color, black);
                check(contrast + 0.04 >= previous, "lightness ramp monotonicity");
                previous = contrast;
            }
        }
        for (String invalid : new String[]{"red", "#12", "#GG0000", "#123456789"}) {
            boolean failed = false;
            try { ColorMath.parse(invalid); } catch (IllegalArgumentException expected) { failed = true; }
            check(failed, "invalid hex rejected");
        }
        boolean failed = false;
        try { ColorMath.tone(black, Double.NaN, 1.0); } catch (IllegalArgumentException expected) { failed = true; }
        check(failed, "nonfinite lightness rejected");
        System.out.println("Color kernel reference tests passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
