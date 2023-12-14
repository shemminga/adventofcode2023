import java.nio.file.*;
import static java.lang.System.*;
import static java.util.Arrays.copyOfRange;

void main() throws Exception {
    doFile("input-test1.txt");
    doFile("input.txt");
}

long fileStartTime = 0;

void doFile(String filename) throws Exception {
    out.println(STR."*** input file: \{filename} ***");
    fileStartTime = nanoTime();

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var grids = lines
                .map(String::trim)
                .map(String::toCharArray)
                .toArray(char[][]::new);

        long score = solve(grids);
        out.println("score = " + score);

        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

long solve(char[][] grids) {
    var score = 0L;

    var start = 0;
    for (int i = 0; i < grids.length + 1; i++) {
        if (i == grids.length || grids[i].length == 0) {
            var grid = copyOfRange(grids, start, i);
            score += solveGrid(grid, i);
            start = i + 1;
        }
    }

    return score;
}

long solveGrid(char[][] grid, int lineNr) {
    var mr = mirrorRow(grid);

    if (mr >= 0) {
        out.printf("Grid mirrors in ROW %2d%n", mr + 1);
        return 100L * (mr + 1);
    }

    var cr = mirrorCol(grid);

    if (cr < 0) throw new AssertionError("grid ending line " + lineNr);

    out.printf("Grid mirrors in COL %2d%n", cr + 1);
    return cr + 1;
}

int mirrorRow(char[][] grid) {
    for (int i = 0; i < grid.length - 1; i++)
        if (mirrorAfterRow(grid, i))
            return i;

    return -1;
}

boolean mirrorAfterRow(char[][] grid, int i) {
    var topR = i;
    var botR = i + 1;

    while (topR >= 0 && botR < grid.length) {
        for (int j = 0; j < grid[topR].length; j++)
            if (grid[topR][j] != grid[botR][j])
                return false;

        topR--;
        botR++;
    }

    return true;
}

int mirrorCol(char[][] grid) {
    for (int j = 0; j < grid[0].length - 1; j++)
        if (mirrorAfterCol(grid, j))
            return j;

    return -1;
}

boolean mirrorAfterCol(char[][] grid, int j) {
    var lC = j;
    var rC = j + 1;

    while (lC >= 0 && rC < grid[0].length) {
        for (int i = 0; i < grid.length; i++)
            if (grid[i][lC] != grid[i][rC])
                return false;

        lC--;
        rC++;
    }

    return true;
}
