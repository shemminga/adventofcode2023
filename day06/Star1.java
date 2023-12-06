import java.nio.file.*;
import static java.lang.System.out;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test.txt");
    doFile("input.txt");
}

void doFile(String filename) throws Exception {
    out.println(STR."*** input file: \{filename} ***");

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var race = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(s -> s.split(":"))
                .map(a -> a[1])
                .map(s -> s.replaceAll(" ", ""))
                .mapToLong(Long::parseLong)
                .toArray();

        var time = race[0];
        var dist = race[1];
        var ways = countWays(time, dist);

        out.println("ways = " + ways);
    }

    out.println();
}

int countWays(long time, long dist) {
    int ways = 0;
    for (int i = 0; i <= time; i++) {
        var timeLeft = time - i;
        var speed = i;

        if (dist < timeLeft * speed) ways++;
    }

    return ways;
}