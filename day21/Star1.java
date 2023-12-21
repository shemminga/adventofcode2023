import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import static java.lang.System.*;
import static java.util.Arrays.*;
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
    var minOdd = new int[grid.length][grid[0].length];
    var minEven = new int[grid.length][grid[0].length];

    for (int i = 0; i < grid.length; i++) {
        fill(minOdd[i], Integer.MAX_VALUE);
        fill(minEven[i], Integer.MAX_VALUE);
    }

    var q = new PriorityQueue<Pt>(Comparator.comparingInt(pt -> minSteps(pt, minOdd, minEven)));

    q.add(markS(grid, minEven));

    while (!q.isEmpty()) {
        var cPt = q.remove();
        var cSteps = minSteps(cPt, minOdd, minEven);

        //out.printf("%s -> %d%n", cPt, cSteps);

        if (cSteps > 64) break;

        Stream.of(new Pt(cPt.i - 1, cPt.j, !cPt.odd),
                        new Pt(cPt.i + 1, cPt.j, !cPt.odd),
                        new Pt(cPt.i, cPt.j - 1, !cPt.odd),
                        new Pt(cPt.i, cPt.j + 1, !cPt.odd))
                .filter(pt -> canMove(pt.i, pt.j, grid))
                .filter(pt -> cSteps + 1 < minSteps(pt, minOdd, minEven))
                .forEach(pt -> move(pt, cSteps, minOdd, minEven, q));
    }

    return stream(minEven).flatMapToInt(Arrays::stream)
            .filter(n -> n < Integer.MAX_VALUE)
            .count();
}

void move(Pt pt, int cSteps, int[][] minOdd, int[][] minEven, PriorityQueue<Pt> q) {
    var newSteps = cSteps + 1;
    var markArr = pt.odd ? minOdd : minEven;

    if (markArr[pt.i][pt.j] <= newSteps) throw new AssertionError(pt + " " + newSteps);

    markArr[pt.i][pt.j] = newSteps;
    q.add(pt);
}

boolean canMove(int i, int j, char[][] grid) {
    if (i < 0 || i >= grid.length) return false;
    if (j < 0 || j >= grid[i].length) return false;
    return grid[i][j] != '#';
}

int minSteps(Pt pt, int[][] minOdd, int[][] minEven) {
    if (pt.odd) return minOdd[pt.i][pt.j];
    return minEven[pt.i][pt.j];
}

Pt markS(char[][] grid, int[][] minEven) {
    for (int i = 0; i < grid.length; i++)
        for (int j = 0; j < grid[0].length; j++)
            if (grid[i][j] == 'S') {
                minEven[i][j] = 0;
                return new Pt(i, j, false);
            }
    throw new AssertionError();
}

record Pt(int i, int j, boolean odd) {}
