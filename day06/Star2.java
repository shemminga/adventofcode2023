import java.nio.file.*;
import java.util.Arrays;
import java.util.regex.Pattern;
import static java.lang.System.out;
import static java.util.Arrays.copyOfRange;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test.txt");
    doFile("input.txt");
}

void doFile(String filename) throws Exception {
    out.println(STR."*** input file: \{filename} ***");

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var races = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(SPACES::split)
                .map(a -> copyOfRange(a, 1, a.length))
                .map(a -> Arrays.stream(a)
                        .mapToInt(Integer::parseInt)
                        .toArray())
                .toArray(int[][]::new);

        var times = races[0];
        var dists = races[1];
        var ways = new int[times.length];

        for (int i = 0; i < times.length; i++) ways[i] = countWays(times[i], dists[i]);

        out.println("Arrays.toString(ways) = " + Arrays.toString(ways));

        var prod = Arrays.stream(ways)
                .mapToLong(n -> n)
                .reduce(1, (n1, n2) -> n1 * n2);

        out.println("prod = " + prod);
    }

    out.println();
}

private static final Pattern SPACES = Pattern.compile(" +");

int countWays(int time, int dist) {
    int ways = 0;
    for (int i = 0; i <= time; i++) {
        var timeLeft = time - i;
        var speed = i;

        if (dist < timeLeft * speed) ways++;
    }

    return ways;
}