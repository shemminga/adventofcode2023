import java.nio.file.*;
import java.util.*;
import static java.lang.System.out;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

void main() throws Exception {
    doFile("input-test1.txt");
    doFile("input-test2.txt");
    doFile("input.txt");
}

void doFile(String filename) throws Exception {
    out.println(STR."*** input file: \{filename} ***");

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var la = lines.filter(not(String::isBlank))
                .map(String::trim)
                .toArray(String[]::new);

        var instr = la[0].toCharArray();
        var map = Arrays.stream(la)
                .skip(1)
                .collect(toMap(s -> s.substring(0, 3),
                        s -> new String[] { s.substring(7, 10), s.substring(12, 15) }));

        var steps = countStepsToZZZ(instr, map);

        out.println("steps = " + steps);
    }

    out.println();
}

long countStepsToZZZ(char[] instr, Map<String, String[]> map) {
    long steps = 0;
    int ip = 0;
    String curr = "AAA";

    while (!"ZZZ".equals(curr)) {
        out.printf("* %3d: %s %c%n", steps, curr, instr[ip]);

        if (instr[ip] == 'L') curr = map.get(curr)[0];
        if (instr[ip] == 'R') curr = map.get(curr)[1];

        steps++;
        ip = (ip >= instr.length - 1) ? 0 : ip + 1;
    }

    return steps;
}
