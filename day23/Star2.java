import java.nio.file.*;
import java.time.LocalTime;
import static java.lang.Math.max;
import static java.lang.System.*;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test1.txt");
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
    var visited = new boolean[grid.length][grid[0].length];
    return solveR(0, 1, 0, visited, grid);
}

long solveR(int i, int j, long curLen, boolean[][] visited, char[][] grid) {
    if (i < 0 || i >= grid.length) return -1;
    if (j < 0 || j >= grid[0].length) return -1;
    if (grid[i][j] == '#') return -1;
    if (visited[i][j]) return -1;

    if (i == grid.length - 1 && j == grid[0].length - 2) {
        //out.printf("+++ %s *** Found route of length: %d%n", LocalTime.now(), curLen);
        return curLen;
    }

    visited[i][j] = true;
    var max = 0L;
    max = max(max, solveR(i - 1, j, curLen + 1, visited, grid));
    max = max(max, solveR(i + 1, j, curLen + 1, visited, grid));
    max = max(max, solveR(i, j - 1, curLen + 1, visited, grid));
    max = max(max, solveR(i, j + 1, curLen + 1, visited, grid));

    visited[i][j] = false;
    return max;
}
