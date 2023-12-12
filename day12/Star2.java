import java.nio.file.*;
import java.util.*;
import static java.lang.String.join;
import static java.lang.System.*;
import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test1.txt");
    doFile("input-test2.txt");
    doFile("input-test3.txt");
    doFile("input.txt");
}

long fileStartTime = 0;

void doFile(String filename) throws Exception {
    out.println(STR."*** input file: \{filename} ***");
    fileStartTime = nanoTime();

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var sum = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(this::expand)
                .mapToLong(this::countPossibleArrangements)
                .sum();

        out.printf("sum = %d  %,d ns%n", sum, nanoTime() - fileStartTime);
    }

    out.println();
}

String expand(String s) {
    var parts = s.split(" ");
    return join("?", parts[0], parts[0], parts[0], parts[0], parts[0]) + ' ' +
            join(",", parts[1], parts[1], parts[1], parts[1], parts[1]);
}

Map<String, Long> cache = new HashMap<>();

long countPossibleArrangements(String line) {
    out.printf("In   : %s%n", line);
    var rowStartTime = nanoTime();

    cache = new HashMap<>();

    var parts = line.split(" ");

    var sLengths = getSuppliedLengths(parts[1]);
    var record = parts[0].toCharArray();

    var remH = stream(sLengths).sum();
    var remD = record.length - remH;
    for (char c : record)
        if (c == '#') remH--;
        else if (c == '.') remD--;

    var remPos = new int[sLengths.length];
    for (int i = sLengths.length - 1; i >= 0; i--) {
        remPos[i] = sLengths[i];
        if (i < sLengths.length - 1) remPos[i] += remPos[i + 1] + 1;
    }

    //out.printf("sLengths: %s, remPos = %s%n", Arrays.toString(sLengths), Arrays.toString(remPos));

    var count = countPossibleArrangements(record, sLengths, 0, 0, 0, remH, remD, remPos);
    out.printf("%s %d  Row time: %,d ns  File time: %,d ns%n",
            "-".repeat(60),
            count,
            nanoTime() - rowStartTime,
            nanoTime() - fileStartTime);
    return count;
}

int[] getSuppliedLengths(String str) {
    return stream(str.split(",")).mapToInt(Integer::parseInt)
            .toArray();
}

long countPossibleArrangements(char[] line, int[] sLengths, int pos, int slPos, int cgLen, int remH, int remD,
        int[] remPos) {
    var cacheKey = pos + "-" + slPos + '-' + cgLen + '-' + remH + '-' + remD;

    if (cache.containsKey(cacheKey)) return cache.get(cacheKey);

    var rv = countPossibleArrangements1(line, sLengths, pos, slPos, cgLen, remH, remD, remPos);

    cache.put(cacheKey, rv);

    //out.printf("Try: %s, pos: %2d, slPos: %2d, cgLen: %2d, remH: %2d, remD: %2d %c%n",
    //        new String(line),
    //        pos,
    //        slPos,
    //        cgLen,
    //        remH,
    //        remD,
    //        rv > 0 ? 'X' : ' ');

    return rv;
}

long countPossibleArrangements1(char[] line, int[] sLengths, int pos, int slPos, int cgLen, int remH, int remD,
        int[] remPos) {

    if (remH < 0) return 0; // Using too many hashes
    if (remD < 0) return 0; // Using too many dots

    if (slPos >= sLengths.length) // Ran out of groups, so don't expect any # in the rest
        return new String(line).lastIndexOf('#') < pos ? 1 : 0;

    if (line.length - pos < remPos[slPos] - cgLen) {
        //out.printf("%s %d - %d < %d - %d%n", new String(line), line.length, pos, remPos[slPos], cgLen);
        return 0; // Not enough space left to fit the rest
    }

    if (pos >= line.length) {
        if (slPos < sLengths.length - 1) return 0; // There are more groups
        if (sLengths[slPos] != cgLen) return 0; // Last group is improper size
        return 1;
    }

    if (cgLen > sLengths[slPos]) return 0; // Current group is longer than expected

    if (line[pos] == '.') {
        if (pos == 0 || line[pos - 1] == '.')
            return countPossibleArrangements(line, sLengths, pos + 1, slPos, 0, remH, remD, remPos);

        if (line[pos - 1] == '#') {
            if (cgLen != sLengths[slPos]) return 0; // Group is too small
            return countPossibleArrangements(line, sLengths, pos + 1, slPos + 1, 0, remH, remD, remPos);
        }

        throw new AssertionError();
    }

    if (line[pos] == '#')
        return countPossibleArrangements(line, sLengths, pos + 1, slPos, cgLen + 1, remH, remD, remPos);

    if (line[pos] == '?') {
        var sum = 0L;

        line[pos] = '#';
        sum += countPossibleArrangements(line, sLengths, pos, slPos, cgLen, remH - 1, remD, remPos);
        line[pos] = '.';
        sum += countPossibleArrangements(line, sLengths, pos, slPos, cgLen, remH, remD - 1, remPos);
        line[pos] = '?';

        return sum;
    }

    throw new AssertionError();
}
