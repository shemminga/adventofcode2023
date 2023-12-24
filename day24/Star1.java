import java.nio.file.*;
import java.time.LocalTime;
import java.util.regex.Pattern;
import static java.lang.Long.parseLong;
import static java.lang.Math.abs;
import static java.lang.System.*;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test1.txt", 7, 27);
    doFile("input.txt", 200_000_000_000_000L, 400_000_000_000_000L);
    out.printf("*** %s *** DONE ***%n", LocalTime.now());
}

long fileStartTime = 0;
boolean test = false;

void doFile(String filename, long minCoord, long maxCoord) throws Exception {
    out.printf("*** %s *** input file: %s ***%n", LocalTime.now(), filename);
    test = filename.contains("test");
    fileStartTime = nanoTime();

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var stones = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map (PtV::of)
                .toArray(PtV[]::new);

        var solution = solve(stones, minCoord, maxCoord);

        out.println("solution = " + solution);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

long solve(PtV[] stones, long minCoord, long maxCoord) {
    var intersects = 0;
    for (int i = 0; i < stones.length; i++) {
        for (int j = 0; j < stones.length; j++) {
            if (i <= j) continue;

            var li = stones[i];
            var lj = stones[j];

            if (li.leF() == lj.leF()) continue; // Parallel

            var x = (lj.leC() - li.leC()) / (li.leF() - lj.leF());

            var yi = li.leC() + li.leF() * x;
            var yj = lj.leC() + lj.leF() * x;

            if (abs(yi-yj) > 1) {
                // Floating point math inaccuracies can go as high as 64.0 here, with all those big numbers.
                // But for all yi and yj are both outside the range, so it works out fine.

                //out.printf(",- li: %s // %f + %f * x%n", li, li.leC(), li.leF());
                //out.printf("|  lj: %s // %f + %f * x%n", lj, lj.leC(), lj.leF());
                //out.printf("|  Intersect at x = %f // yi = %f // yj = %f%n", x, yi, yj);
                //out.printf("`- yi - yj = %f%n", yi - yj);

                if (yi < minCoord != yj < minCoord) throw new AssertionError();
                if (yi > maxCoord != yj > maxCoord) throw new AssertionError();
            }

            if (x < minCoord || x > maxCoord) continue; // Out of range
            if (yi < minCoord || yi > maxCoord) continue; // Out of range

            var tyi = (yi - li.py) / li.vy;
            var tyj = (yj - lj.py) / lj.vy;
            var txi = (x - li.px) / li.vx;
            var txj = (x - lj.px) / lj.vx;

            if (tyi < 0 != txi < 0) throw new AssertionError(tyi + " " + txi);
            if (tyj < 0 != txj < 0) throw new AssertionError(tyj + " " + txj);

            if (tyi < 0) continue; // In the past for line i
            if (tyj < 0) continue; // In the past for line j

            intersects++;

            if (test) {
                out.printf("li: %s // %f + %f * x%n", li, li.leC(), li.leF());
                out.printf("lj: %s // %f + %f * x%n", lj, lj.leC(), lj.leF());
                out.printf("Intersect at x = %f // yi = %f // yj = %f%n", x, yi, yj);
            }
        }
    }

    return intersects;
}

record PtV(double px, double py, double pz, double vx, double vy, double vz) {
    private static final Pattern AT = Pattern.compile(" @ +");
    private static final Pattern COMMA = Pattern.compile(", +");

    static PtV of(String s) {
        var split = AT.split(s);
        var pSplit = COMMA.split(split[0]);
        var vSplit = COMMA.split(split[1]);

        return new PtV(parseLong(pSplit[0]),
                parseLong(pSplit[1]),
                parseLong(pSplit[2]),
                parseLong(vSplit[0]),
                parseLong(vSplit[1]),
                parseLong(vSplit[2]));
    }

    double leC() {
        return py - (vy*px/vx);
    }

    double leF() {
        return vy/vx;
    }
}
