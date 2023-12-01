import java.nio.file.Files;
import java.nio.file.Paths;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.out;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test1.txt");
    doFile("input-test2.txt");
    doFile("input.txt");
}

void doFile(String filename) throws Exception {
    out.println(STR."*** input file: \{filename} ***");

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        final int totalCalVals = lines.filter(not(String::isBlank))
                .map(String::trim)
                .mapToInt(this::decodeCalVal)
                .sum();

        out.println("totalCalVals = " + totalCalVals);
    }

    out.println();
}

static final String[] VALUES = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};

int decodeCalVal(String s) {
    int firstIdx = Integer.MAX_VALUE, lastIdx = -1, firstVal = 0, lastVal = 0;

    for (int i = 0; i < VALUES.length; i++) {
        var val = i + 1;
        var cVal = val + '0';

        var firstWord = s.indexOf(VALUES[i]);
        var lastWord = s.lastIndexOf(VALUES[i]);
        var firstDigit = s.indexOf(cVal);
        var lastDigit = s.lastIndexOf(cVal);

        var first = firstWord < 0 ? firstDigit : (firstDigit < 0 ? firstWord : min(firstWord, firstDigit));
        var last = lastWord < 0 ? lastDigit : (lastDigit < 0 ? lastWord : max(lastWord, lastDigit));

        if (first > -1 && first < firstIdx) {
            firstIdx = first;
            firstVal = val;
        }
        if (last > -1 && last > lastIdx) {
            lastIdx = last;
            lastVal = val;
        }
    }

    return firstVal * 10 + lastVal;
}

