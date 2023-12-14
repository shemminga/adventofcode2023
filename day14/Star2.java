import java.nio.file.*;
import java.util.HashMap;
import static java.lang.System.*;
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
                .map(String::toCharArray)
                .toArray(char[][]::new);

        long load = solve(grid);
        out.println("load = " + load);

        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

static final int LAST_CYCLE = 1_000_000_000;

long solve(char[][] grid) {
    var cache = new HashMap<String, char[][]>();
    var cycles = new HashMap<String, Integer>();

    for (int i = 0; i < 1_000_000_000; i++) {
        var cKey = gridToStr(grid);

        if (cycles.containsKey(cKey)) {
            grid = cache.get(cKey);
            int sameAs = cycles.get(cKey);
            var cycleSize = i - sameAs;
            var rem = LAST_CYCLE - i - 1;

            out.printf("i: %3d, sameAs: %3d, cycleSize: %3d, rem: %9d, load: %d%n",
                    i, sameAs, cycleSize, rem, calcLoad(grid));

            if (rem % cycleSize == 0) // Last cycle will be same as this cycle
                break;
        } else {
            grid = spinCycle(grid);
            cache.put(cKey, grid);
            cycles.put(cKey, i);
        }
    }

    return calcLoad(grid);
}

String gridToStr(char[][] grid) {
    var sb = new StringBuilder();
    for (char[] row : grid) sb.append(row);
    return sb.toString();
}

char[][] spinCycle(char[][] inGrid) {
    var outGrid = new char[inGrid.length][inGrid[0].length];
    for (int i = 0; i < inGrid.length; i++)
        arraycopy(inGrid[i], 0, outGrid[i], 0, inGrid[i].length);

    moveAllNorth(outGrid);
    moveAllWest(outGrid);
    moveAllSouth(outGrid);
    moveAllEast(outGrid);
    return outGrid;
}

void moveAllNorth(char[][] grid) {
    var changed = true;

    while (changed) {
        changed = false;
        for (int i = 1; i < grid.length; i++)
            for (int j = 0; j < grid[i].length; j++)
                changed = swap(grid, i, j, i - 1, j) || changed;
    }
}

void moveAllWest(char[][] grid) {
    var changed = true;

    while (changed) {
        changed = false;
        for (int i = 0; i < grid.length; i++)
            for (int j = 1; j < grid[i].length; j++)
                changed = swap(grid, i, j, i, j - 1) || changed;
    }
}

void moveAllSouth(char[][] grid) {
    var changed = true;

    while (changed) {
        changed = false;
        for (int i = grid.length - 2; i >= 0; i--)
            for (int j = 0; j < grid[i].length; j++)
                changed = swap(grid, i, j, i + 1, j) || changed;
    }
}

void moveAllEast(char[][] grid) {
    var changed = true;

    while (changed) {
        changed = false;
        for (int i = 0; i < grid.length; i++)
            for (int j = grid[i].length - 2; j >= 0; j--)
                changed = swap(grid, i, j, i, j + 1) || changed;
    }
}

boolean swap(char[][] grid, int tI, int tJ, int oI, int oJ) {
    var changed = false;
    if (grid[tI][tJ] == 'O' && grid[oI][oJ] == '.') {
        grid[tI][tJ] = '.';
        grid[oI][oJ] = 'O';
        changed = true;
    }
    return changed;
}

long calcLoad(char[][] grid) {
    var totalLoad = 0L;

    for (int i = 0; i < grid.length; i++)
        for (int j = 0; j < grid[i].length; j++)
            if (grid[i][j] == 'O')
                totalLoad += grid.length - i;

    return totalLoad;
}

void dumpGrid(String header, char[][] grid) {
    if (test) {
        out.println(header);
        for (char[] row : grid) {
            for (char c : row) out.print(c);
            out.println();
        }
        out.println();
    }
}