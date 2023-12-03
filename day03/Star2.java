import java.nio.CharBuffer;
import java.nio.file.*;
import static java.lang.Character.isDigit;
import static java.lang.Long.parseLong;
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
        var grid = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(String::toCharArray)
                .toArray(char[][]::new);

        boolean[][] used = new boolean[grid.length][grid[0].length];

        long sum = 0;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] != '*') continue;
                sum += gearRatio(i, j, grid, used);
            }
        }

        out.println("sum = " + sum);
    }

    out.println();
}

private long gearRatio(int i, int j, char[][] grid, boolean[][] used) {
    long pCnt = 0, gearRatio = 1;

    for (var pI = i - 1; pI <= i + 1; pI++)
        for (var pJ = j - 1; pJ <= j + 1; pJ++) {
            var pNr = partNr(pI, pJ, grid, used);

            if (pNr != 0) {
                pCnt++;
                gearRatio *= pNr;
            }
        }

    if (pCnt != 2) gearRatio = 0;
    return gearRatio;
}

long partNr(int i, int j, char[][] grid, boolean[][] used) {
    if (i < 0 || j < 0) return 0;
    if (i >= grid.length || j >= grid[i].length) return 0;
    if (!isDigit(grid[i][j])) return 0;

    var startJ = findStartJ(i, j, grid);
    var endJ = findEndJ(i, j, grid);

    if (used[i][startJ]) return 0;
    used[i][startJ] = true;

    return parseLong(CharBuffer.wrap(grid[i]), startJ, endJ + 1, 10);
}

int findStartJ(int i, int j, char[][] grid) {
    var startJ = j;

    while (startJ >= 0 && isDigit(grid[i][startJ])) startJ--;

    return startJ + 1;
}

int findEndJ(int i, int j, char[][] grid) {
    var endJ = j;

    while (endJ < grid[i].length && isDigit(grid[i][endJ])) endJ++;

    return endJ - 1;
}