import java.nio.file.*;
import java.time.LocalTime;
import java.util.regex.Pattern;
import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.lang.System.*;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test1.txt");
    doFile("input.txt");
    out.printf("*** %s *** DONE ***%n", LocalTime.now());
}

long fileStartTime = 0;
boolean test = false;

void doFile(String filename) throws Exception {
    out.printf("*** %s *** input file: %s ***%n", LocalTime.now(), filename);
    test = filename.contains("test");
    fileStartTime = nanoTime();

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var stones = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map (PtV::of)
                .toArray(PtV[]::new);

        generateMapleCommand(stones);

        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

void generateMapleCommand(PtV[] stones) {
    var sb = new StringBuilder();
    sb.append("solve({");

    for (int i = 0; i < stones.length; i++) {
        var li = stones[i];

        sb.append(format("%.0f+%.0f t[%d]=p[x]+v[x] t[%d],", li.px, li.vx, i+1, i+1));
        sb.append(format("%.0f+%.0f t[%d]=p[y]+v[y] t[%d],", li.py, li.vy, i+1, i+1));
        sb.append(format("%.0f+%.0f t[%d]=p[z]+v[z] t[%d],", li.pz, li.vz, i+1, i+1));
        sb.append(format("t[%d]>0,", i+1));

        if (i >= 2) break; // 3 is enough to solve it. More makes Maple slower.
    }

    sb.deleteCharAt(sb.length() - 1);
    sb.append("});");

    out.println(sb);
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
}
