import java.nio.file.*;
import java.util.*;
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
                .map(String::toCharArray)
                .mapToLong(this::countPossibleArrangements)
                .sum();

        out.println("sum = " + sum);
    }

    out.println();
}

long countPossibleArrangements(char[] line) {
    out.printf("In   : %s%n", new String(line));

    var count = countPossibleArrangements(line, 0);
    out.printf("%s %d%n", "-".repeat(60), count);
    return count;
}

long countPossibleArrangements(char[] line, int pos) {
    if (line[pos] == ' ') return isValid(line);

    if (line[pos] != '?') return countPossibleArrangements(line, pos + 1);

    var sum = 0L;
    line[pos] = '.';
    sum += countPossibleArrangements(line, pos + 1);
    line[pos] = '#';
    sum += countPossibleArrangements(line, pos + 1);
    line[pos] = '?';

    return sum;
}

long isValid(char[] line) {
    var lengths = new ArrayList<Integer>();

    var i = 0;
    var lastChar = '\0';
    var groupSize = 0;
    while (line[i] != ' ') {
        if (line[i] == '#') groupSize++;

        if (lastChar == '#' && line[i] == '.') {
            lengths.add(groupSize);
            groupSize = 0;
        }

        lastChar = line[i];
        i++;
    }

    if (groupSize > 0) lengths.add(groupSize);

    var suppliedLengths = getSuppliedLengths(line, i + 1);

    var valid = suppliedLengths.equals(lengths) ? 1 : 0;
    //out.printf("Try %d: %s, S: %s, C: %s%n", valid, new String(line), suppliedLengths, lengths);
    return valid;
}

List<Integer> getSuppliedLengths(char[] line, int spaceIdx) {
    var s = new String(line, spaceIdx, line.length - spaceIdx);
    var ss = s.split(",");

    return stream(ss).mapToInt(Integer::parseInt)
            .boxed()
            .toList();
}