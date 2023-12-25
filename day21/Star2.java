import java.nio.file.*;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;
import static java.lang.Math.max;
import static java.lang.System.*;
import static java.util.Arrays.fill;
import static java.util.Comparator.comparingInt;
import static java.util.function.Predicate.not;

void main() throws Exception {
    //doFile("input-test1.txt"); // Real input has nice gangways to all cardinal directions. The test doesn't have that.
    doFile("input.txt");
    out.printf("*** %s *** DONE ***%n", LocalTime.now());
}

long fileStartTime = 0;
boolean test = false;

void doFile(String filename) throws Exception {
    out.printf("*** %s *** input file: %s ***%n", LocalTime.now(), filename);
    test = filename.contains("test");
    fileStartTime = nanoTime();

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var grid = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map (String::toCharArray)
                .toArray(char[][]::new);

        var solution = solve(grid);

        out.println("solution = " + solution);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

long solve(char[][] grid) {
    Map<Pt, Marks> allMarks = new HashMap<>();

    var s = findS(grid);
    addMarks(s, grid, allMarks);

    var lastI = grid.length - 1;
    var lastJ = grid[0].length - 1;

    var ptN = new Pt(0, s.j);
    var ptE = new Pt(s.i, lastJ);
    var ptS = new Pt(lastI, s.j);
    var ptW = new Pt(s.i, 0);

    var ptNE = new Pt(0, lastJ);
    var ptSE = new Pt(lastI, lastJ);
    var ptSW = new Pt(lastI, 0);
    var ptNW = new Pt(0, 0);

    addMarks(ptN, grid, allMarks);
    addMarks(ptE, grid, allMarks);
    addMarks(ptS, grid, allMarks);
    addMarks(ptW, grid, allMarks);

    addMarks(ptNE, grid, allMarks);
    addMarks(ptSE, grid, allMarks);
    addMarks(ptSW, grid, allMarks);
    addMarks(ptNW, grid, allMarks);

    if (s.i != s.j) throw new AssertionError();
    if (grid.length != grid[0].length) throw new AssertionError();

    long total = allMarks.get(s).counts[STEPS % 2];
    total += countCardis(STEPS - s.i - 1,
            grid.length,
            allMarks.get(ptN),
            allMarks.get(ptE),
            allMarks.get(ptS),
            allMarks.get(ptW));
    total += countOrdis(STEPS - s.i - 1 - s.j - 1,
            grid.length,
            allMarks.get(ptNE),
            allMarks.get(ptSE),
            allMarks.get(ptSW),
            allMarks.get(ptNW));

    return total;
}

static final int STEPS = 26501365;

long countCardis(int steps, int size, Marks marksN, Marks marksE, Marks marksS, Marks marksW) {
    return countCardi(steps, size, marksN) +
            countCardi(steps, size, marksE) +
            countCardi(steps, size, marksS) +
            countCardi(steps, size, marksW);
}

long countCardi(int steps, int size, Marks m) {
    var sum = 0L;
    while (steps > m.max) {
        sum += m.counts[steps % 2];
        steps -= size;
    }

    while (steps >= 0) {
        sum += count(steps, m);
        steps -= size;
    }
    return sum;
}

long countOrdis(int steps, int size, Marks marksNE, Marks marksSE, Marks marksSW, Marks marksNW) {
    return countOrdi(steps, size, marksNE) +
            countOrdi(steps, size, marksSE) +
            countOrdi(steps, size, marksSW) +
            countOrdi(steps, size, marksNW);
}

long countOrdi(int steps, int size, Marks m) {
    var sum = 0L;
    var multiplier = 1L;
    while (steps > m.max) {
        sum += multiplier * m.counts[steps % 2];
        steps -= size;
        multiplier++;
    }

    while (steps >= 0) {
        sum += multiplier * count(steps, m);
        steps -= size;
        multiplier++;
    }
    return sum;
}

long count(int steps, Marks m) {
    var count = 0L;
    for (int i = 0; i < m.min.length; i++)
        for (int j = 0; j < m.min[i].length; j++)
            if (m.min[i][j] <= steps && m.min[i][j] % 2 == steps % 2)
                count++;
    return count;
}

void dump(Marks m) {
    if (!test) return;
    out.printf("(%d, %s):%n", m.max, Arrays.toString(m.counts));

    out.printf("| %2s | ", "");
    for (int j = 0; j < m.min[0].length; j++) {
        out.printf(" %3d", j);
    }
    out.printf("%n%s%n", "-".repeat(7 + 4 * m.min[0].length));

    for (int i = 0; i < m.min.length; i++) {
        out.printf("| %2d | ", i);
        for (int j = 0; j < m.min[i].length; j++) {
            if (m.min[i][j] == Integer.MAX_VALUE) out.printf(" %3c", '-');
            else out.printf(" %3d", m.min[i][j]);
        }
        out.println();
    }
}

void addMarks(Pt p, char[][] grid, Map<Pt, Marks> allMarks) {
    var min = new int[grid.length][grid[0].length];
    mark(grid, p, min);

    var max = 0L;
    int[] counts = {0, 0};
    for (int i = 0; i < grid.length; i++)
        for (int j = 0; j < grid[i].length; j++)
            if (min[i][j] != Integer.MAX_VALUE) {
                max = max(max, min[i][j]);
                counts[min[i][j] % 2]++;
            }

    allMarks.put(p, new Marks(min, max, counts));
}


void mark(char[][] grid, Pt s, int[][] min) {
    for (int i = 0; i < grid.length; i++) fill(min[i], Integer.MAX_VALUE);

    var q = new PriorityQueue<Pt>(comparingInt(pt -> min[pt.i][pt.j]));

    min[s.i][s.j] = 0;
    q.add(s);

    while (!q.isEmpty()) {
        var cPt = q.remove();
        var nSteps = min[cPt.i][cPt.j] + 1;

        Stream.of(new Pt(cPt.i - 1, cPt.j),
                        new Pt(cPt.i + 1, cPt.j),
                        new Pt(cPt.i, cPt.j - 1),
                        new Pt(cPt.i, cPt.j + 1))
                .filter(pt -> canMove(pt.i, pt.j, grid))
                .filter(pt -> nSteps < min[pt.i][pt.j])
                .forEach(pt -> {
                    min[pt.i][pt.j] = nSteps;
                    q.add(pt);
                });
    }
}

boolean canMove(int i, int j, char[][] grid) {
    if (i < 0 || i >= grid.length) return false;
    if (j < 0 || j >= grid[i].length) return false;
    return grid[i][j] != '#';
}

Pt findS(char[][] grid) {
    for (int i = 0; i < grid.length; i++)
        for (int j = 0; j < grid[0].length; j++)
            if (grid[i][j] == 'S')
                return new Pt(i, j);
    throw new AssertionError();
}

record Pt(int i, int j) {}
record Marks(int[][] min, long max, int[] counts) {}
