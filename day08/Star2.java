import java.nio.file.*;
import java.util.ArrayList;
import java.util.*;
import static java.lang.System.out;
import static java.util.Arrays.*;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

void main() throws Exception {
    doFile("input-test1.txt");
    doFile("input-test2.txt");
    //doFile("input-test3.txt"); // Doesn't work because 2nd entry to Z has different instruction
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
        var map = stream(la)
                .skip(1)
                .collect(toMap(s -> s.substring(0, 3),
                        s -> new String[] { s.substring(7, 10), s.substring(12, 15) }));

        var steps = solve(instr, map);

        out.println("steps = " + steps);
    }

    out.println();
}

long solve(char[] instr, Map<String, String[]> map) {
    var startNodes = startNodes(map.keySet());

    var zSAL = stream(startNodes)
            .map(n -> findZStatesAndLoop(n, instr, map))
            .toArray(Long[][]::new);

    out.println("| zSAL = " + deepToString(zSAL));

    var firstHit = firstHit(zSAL);
    equalizeStarts(firstHit, zSAL);
    out.println("| zSAL = " + deepToString(zSAL));
    shiftToZero(firstHit, zSAL);
    out.println("- zSAL = " + deepToString(zSAL));

    var base = 0L;
    var loopSize = firstHit;

    for (Long[] z : zSAL) {
        if (z[2] == 0) continue;

        long m = 0;
        while ((base + m * loopSize) % z[0] != z[2]) m++;

        out.printf("- (%12d + %3d * %12d) %% %5d == %5d%n", base, m, loopSize, z[0], z[2]);
        base += m * loopSize;
        loopSize = lcm(loopSize, z[0]);
    }

    return base + firstHit;
}

long lcm(long l1, long l2) {
    long m = 1;
    while ((l1 * m) % l2 != 0) m++;
    //out.printf("lcm(%d, %d) = %d%n", l1, l2, l1 * m);
    return l1 * m;
}

void equalizeStarts(long firstHit, Long[][] zSAL) {
    stream(zSAL)
            .forEach(z -> {
                var shift = firstHit - z[1];
                if (shift < 0) throw new AssertionError(deepToString(z));
                z[0] += shift;
                z[1] += shift;
            });
}

void shiftToZero(long shiftSize, Long[][] zSAL) {
    stream(zSAL).forEach(z -> {
        for (int i = 0; i < z.length; i++)
            z[i] -= shiftSize;
    });
}

long firstHit(Long[][] zSAL) {
    return stream(zSAL)
            .mapToLong(z -> z[2])
            .min()
            .orElseThrow();
}

Long[] findZStatesAndLoop(String startNode, char[] instr, Map<String, String[]> map) {
    var vals = new ArrayList<Long>();

    long steps = 0;
    int ip = 0;
    String curr = startNode;

    var visitedStates = new HashMap<String, Long>();
    var currState = curr + '-' + ip;

    do {
        //out.printf("* %3d (%3d): %s %c -> ", steps, ip, curr, instr[ip]);
        visitedStates.put(currState, steps);
        if (curr.endsWith("Z")) vals.add(steps);

        if (instr[ip] == 'L') curr = map.get(curr)[0];
        if (instr[ip] == 'R') curr = map.get(curr)[1];

        steps++;
        ip = (ip >= instr.length - 1) ? 0 : ip + 1;
        currState = curr + '-' + ip;
        //out.println(currState);
    } while (!visitedStates.containsKey(currState));

    vals.addFirst(visitedStates.get(currState));
    vals.addFirst(steps);
    return vals.toArray(Long[]::new);
}

String[] startNodes(Set<String> allNodes) {
    return allNodes.stream()
            .filter(s -> s.endsWith("A"))
            .toArray(String[]::new);
}
