import java.util.regex.Pattern;
import static java.lang.Integer.parseInt;

record Grab(long r, long g, long b) {
    private static final Pattern SPLIT_PATTERN = Pattern.compile(", ");

    long power() {
        return r * g * b;
    }

    static Grab of(String s) {
        int r = 0, g = 0, b = 0;

        var parts = SPLIT_PATTERN.split(s);
        for (var p : parts) {
            var num = parseInt(p.substring(0, p.indexOf(' ')));

            if (p.endsWith("red")) r = num;
            if (p.endsWith("green")) g = num;
            if (p.endsWith("blue")) b = num;
        }

        return new Grab(r, g, b);
    }
}
