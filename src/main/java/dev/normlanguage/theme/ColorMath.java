package dev.normlanguage.theme;

import java.util.Locale;

public final class ColorMath {
    private ColorMath() {}

    public static int parse(String hex) {
        if (hex == null || !hex.matches("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?")) {
            throw new IllegalArgumentException("Expected #RRGGBB or #RRGGBBAA");
        }
        long value = Long.parseLong(hex.substring(1), 16);
        return (int) (hex.length() == 7 ? (value << 8) | 255 : value);
    }

    public static String format(int rgba) {
        return String.format(Locale.ROOT, "#%08X", rgba);
    }

    public static double alpha(int rgba) {
        return (rgba & 255) / 255.0;
    }

    public static int opacity(int rgba, double alpha) {
        range(alpha, 0.0, 1.0);
        return (rgba & 0xffffff00) | (int) Math.round(alpha * 255);
    }

    public static int mix(int first, int second, double amount) {
        range(amount, 0.0, 1.0);
        int result = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int a = (first >>> shift) & 255;
            int b = (second >>> shift) & 255;
            result |= (int) Math.round(a + (b - a) * amount) << shift;
        }
        return result;
    }

    public static double contrast(int first, int second) {
        if (alpha(first) != 1.0 || alpha(second) != 1.0) {
            throw new IllegalArgumentException("Contrast requires opaque, composited colors");
        }
        double a = luminance(first);
        double b = luminance(second);
        return (Math.max(a, b) + 0.05) / (Math.min(a, b) + 0.05);
    }

    public static int tone(int rgba, double lightness, double chromaScale) {
        range(lightness, 0.0, 1.0);
        range(chromaScale, 0.0, 2.0);
        double r = linear((rgba >>> 24) & 255);
        double g = linear((rgba >>> 16) & 255);
        double b = linear((rgba >>> 8) & 255);
        double l = Math.cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b);
        double m = Math.cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b);
        double s = Math.cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b);
        double a = (1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s) * chromaScale;
        double labB = (0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s) * chromaScale;
        double[] rgb = linearRgb(lightness, a, labB);
        if (!inGamut(rgb)) {
            double lower = 0.0;
            double upper = 1.0;
            for (int iteration = 0; iteration < 24; iteration++) {
                double scale = (lower + upper) / 2.0;
                if (inGamut(linearRgb(lightness, a * scale, labB * scale))) lower = scale;
                else upper = scale;
            }
            rgb = linearRgb(lightness, a * lower, labB * lower);
        }
        return (encoded(rgb[0]) << 24) | (encoded(rgb[1]) << 16) | (encoded(rgb[2]) << 8) | 255;
    }

    private static double[] linearRgb(double lightness, double a, double b) {
        double l = lightness + 0.3963377774 * a + 0.2158037573 * b;
        double m = lightness - 0.1055613458 * a - 0.0638541728 * b;
        double s = lightness - 0.0894841775 * a - 1.2914855480 * b;
        l = l * l * l;
        m = m * m * m;
        s = s * s * s;
        return new double[]{4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
                -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
                -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s};
    }

    private static boolean inGamut(double[] rgb) {
        for (double channel : rgb) if (channel < -1e-7 || channel > 1.0000001) return false;
        return true;
    }

    private static int encoded(double channel) {
        double bounded = Math.clamp(channel, 0.0, 1.0);
        double encoded = bounded <= 0.0031308 ? 12.92 * bounded : 1.055 * Math.pow(bounded, 1.0 / 2.4) - 0.055;
        return (int) Math.round(encoded * 255);
    }

    private static double linear(int channel) {
        double encoded = channel / 255.0;
        return encoded <= 0.04045 ? encoded / 12.92 : Math.pow((encoded + 0.055) / 1.055, 2.4);
    }

    private static double luminance(int rgba) {
        return 0.2126 * linear((rgba >>> 24) & 255) + 0.7152 * linear((rgba >>> 16) & 255) + 0.0722 * linear((rgba >>> 8) & 255);
    }

    private static void range(double value, double minimum, double maximum) {
        if (!Double.isFinite(value) || value < minimum || value > maximum) {
            throw new IllegalArgumentException("Color parameter out of range");
        }
    }
}
