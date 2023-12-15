import java.nio.file.*;
import static java.lang.System.*;
import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test1.txt");
    doFile("input.txt");
}

long fileStartTime = 0;
boolean test = false;

void doFile(String filename) throws Exception {
    out.println(STR."*** input file: \{filename} ***");
    test = filename.contains("test");
    fileStartTime = nanoTime();

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var instr = lines.filter(not(String::isBlank))
                .map(String::trim)
                .findFirst()
                .orElseThrow()
                .split(",");

        var sum = stream(instr).map(String::toCharArray)
                .mapToLong(this::hash)
                .sum();

        out.println("sum = " + sum);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

long hash(char[] instr) {
    var cv = 0;

    for (char c : instr) {
        cv += c;
        cv *= 17;
        cv %= 256;
    }

    return cv;
}
