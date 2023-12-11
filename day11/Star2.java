import java.nio.file.*;
import static java.lang.Math.*;
import static java.lang.System.out;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test1.txt");
    doFile("input.txt");
}

void doFile(String filename) throws Exception {
    out.println(STR."*** input file: \{filename} ***");

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var inGrid = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(String::toCharArray)
                .toArray(char[][]::new);

        var sum = solve(inGrid);
        out.println("sum = " + sum);
    }

    out.println();
}

long solve(char[][] grid) {
    var usedRow = new boolean[grid.length];
    var usedCol = new boolean[grid[0].length];

    markUsedRowCol(grid, usedRow, usedCol);

    var sum = 0L;

    for (int i = 0; i < grid.length; i++)
        for (int j = 0; j < grid[i].length; j++)
            if (grid[i][j] != '.')
                sum += subsolve(i, j, grid, usedRow, usedCol);

    return sum;
}

//int subsolveCnt = 0;

long subsolve(int fI, int fJ, char[][] grid, boolean[] usedRow, boolean[] usedCol) {
    var sum = 0L;

    //subsolveCnt++;
    //out.printf("Star %d: (%3d, %3d)%n", subsolveCnt, fI, fJ);

    for (int i = 0; i < grid.length; i++)
        for (int j = 0; j < grid[i].length; j++) {
            if (fI > i) continue;
            if (fI == i && fJ >= j) continue;
            if (grid[i][j] == '.') continue;

            sum += distBetw(fI, fJ, i, j, usedRow, usedCol);
        }


    return sum;
}

long distBetw(int fI, int fJ, int sI, int sJ, boolean[] usedRow, boolean[] usedCol) {
    int minI = min(fI, sI);
    int maxI = max(fI, sI);
    int minJ = min(fJ, sJ);
    int maxJ = max(fJ, sJ);

    var dist = (maxI - minI) + (maxJ - minJ);

    for (int i = minI; i < maxI; i++)
        if (!usedRow[i]) dist += 999_999;

    for (int j = minJ; j < maxJ; j++)
        if (!usedCol[j]) dist += 999_999;

    //out.printf("Dist (%3d, %3d) -> (%3d, %3d): %4d%n", fI, fJ, sI, sJ, dist);

    return dist;
}

void markUsedRowCol(char[][] grid, boolean[] usedRow, boolean[] usedCol) {
    for (int i = 0; i < grid.length; i++)
        for (int j = 0; j < grid[i].length; j++)
            if (grid[i][j] != '.') {
                usedRow[i] = true;
                usedCol[j] = true;
            }

    //out.println("usedRow = " + Arrays.toString(usedRow));
    //out.println("usedCol = " + Arrays.toString(usedCol));
}
