import java.nio.file.*;
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

long solve(char[][] grid) {
    moveAllNorth(grid);
    return calcLoad(grid);
}

void moveAllNorth(char[][] grid) {
    var changed = true;

    while (changed) {
        changed = false;
        for (int i = 1; i < grid.length; i++)
            for (int j = 0; j < grid[i].length; j++)
                if (grid[i][j] == 'O' && grid[i - 1][j] == '.') {
                    grid[i][j] = '.';
                    grid[i - 1][j] = 'O';
                    changed = true;
                }
    }
}

long calcLoad(char[][] grid) {
    var totalLoad = 0L;

    for (int i = 0; i < grid.length; i++)
        for (int j = 0; j < grid[i].length; j++)
            if (grid[i][j] == 'O')
                totalLoad += grid.length - i;

    return totalLoad;
}