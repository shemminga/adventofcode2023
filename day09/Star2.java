import java.nio.file.*;
import static java.lang.System.out;
import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test1.txt");
    doFile("input.txt");
}

void doFile(String filename) throws Exception {
    out.println(STR."*** input file: \{filename} ***");

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var sum = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(s -> s.split(" "))
                .map(a -> stream(a).mapToLong(Long::parseLong)
                        .toArray())
                .mapToLong(this::calcPrev)
                .sum();

        out.println("sum = " + sum);
    }

    out.println();
}

private long calcPrev(long[] values) {
    var allZero = stream(values).allMatch(n -> n == 0);
    if (allZero) return 0;

    var diffs = new long[values.length - 1];
    for (int i = 0; i < diffs.length; i++)
        diffs[i] = values[i + 1] - values[i];

    return values[0] - calcPrev(diffs);
}
