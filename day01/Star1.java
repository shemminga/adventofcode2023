import java.nio.file.Files;
import java.nio.file.Paths;

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
        final int totalCalVals = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(String::toCharArray)
                .mapToInt(this::decodeCalVal)
                .sum();

        out.println("totalCalVals = " + totalCalVals);
    }

    out.println();
}

int decodeCalVal(char[] chars) {
    int calVal = 0;
    for (final char c : chars)
        if (c >= '0' && c <= '9') {
            calVal = (c - '0') * 10;
            break;
        }

    for (int i = chars.length - 1; i >= 0; i--)
        if (chars[i] >= '0' && chars[i] <= '9') {
            calVal += (chars[i] - '0');
            break;
        }

    return calVal;
}

