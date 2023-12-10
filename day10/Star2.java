import java.nio.file.*;
import java.util.ArrayList;
import java.util.*;
import static java.lang.System.out;
import static java.util.Arrays.*;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test4.txt");
    doFile("input-test5.txt");
    doFile("input-test6.txt");
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

        grid = cleanGrid(grid);
        out.println();

        var inTiles = 0;
        for (int i = 0; i < grid.length; i++) {
            var linesCrossed = 0;
            var lastCorner = '.';
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == 'L' || grid[i][j] == 'F') {
                    lastCorner = grid[i][j];
                } else if (grid[i][j] == '7') {
                    linesCrossed += lastCorner == 'L' ? 1 : 0;
                    lastCorner = '.';
                } else if (grid[i][j] == 'J') {
                    linesCrossed += lastCorner == 'F' ? 1 : 0;
                    lastCorner = '.';
                } else if (grid[i][j] == '|') {
                    linesCrossed++;
                } else if (grid[i][j] == '.' && linesCrossed % 2 == 1) {
                    grid[i][j] = 'X';
                    inTiles++;
                }
            }

            if (linesCrossed % 2 != 0) {
                out.println();
                printGrid(new char[][]{grid[i]});
                throw new AssertionError("%d %d".formatted(i, linesCrossed));
            }
        }

        printGrid(grid);

        out.println("inTiles = " + inTiles);
    }

    out.println();
}

char[][] cleanGrid(char[][] grid) {
    var cleanGrid = new char[grid.length][grid[0].length];
    for (char[] row : cleanGrid) fill(row, '.');

    var s = findS(grid);
    cleanGrid[s.i()][s.j()] = 'S';

    var visited = new HashSet<Point>();
    visited.add(s);
    var pts = s.getConnectedPipes(grid);

    while (!pts[0].equals(pts[1])) {
        cleanGrid[pts[0].i()][pts[0].j()] = grid[pts[0].i()][pts[0].j()];
        cleanGrid[pts[1].i()][pts[1].j()] = grid[pts[1].i()][pts[1].j()];

        visited.add(pts[0]);
        visited.add(pts[1]);

        var p0 = pts[0].getConnectedPipes(grid);
        if (!visited.contains(p0[0])) pts[0] = p0[0];
        if (!visited.contains(p0[1])) pts[0] = p0[1];

        var p1 = pts[1].getConnectedPipes(grid);
        if (!visited.contains(p1[0])) pts[1] = p1[0];
        if (!visited.contains(p1[1])) pts[1] = p1[1];
    }

    cleanGrid[pts[0].i()][pts[0].j()] = grid[pts[0].i()][pts[0].j()];
    overwriteS(s, cleanGrid);

    printGrid(cleanGrid);

    return cleanGrid;
}

void overwriteS(Point s, char[][] grid) {
    grid[s.i()][s.j()] = s.overwriteCharS(grid);
}

String ptToStr(Point pt, char[][] grid) {
    return ptToStr(pt.i(), pt.j(), grid);
}

String ptToStr(int i, int j, char[][] grid) {
    return "(%d, %d) -> %c".formatted(i, j, grid[i][j]);
}

void printGrid(char[][] grid) {
    for (int i = 0; i < grid.length; i++) {
        for (int j = 0; j < grid[i].length; j++) {
            out.print(grid[i][j]);
        }
        out.println();
    }
}

Point findS(char[][] grid) {
    for (int i = 0; i < grid.length; i++)
        for (int j = 0; j < grid[i].length; j++)
            if (grid[i][j] == 'S')
                return new Point(i, j);
    throw new IllegalArgumentException(deepToString(grid));
}

record Point(int i, int j) {
    Point[] getConnectedPipes(char[][] grid) {
        if (grid[i][j] == 'S') {
            var pipes = new ArrayList<Point>();

            if (i > 0 && connectsDown(grid[i - 1][j])) pipes.add(new Point(i - 1, j));
            if (i < grid.length - 1 && connectsUp(grid[i + 1][j])) pipes.add(new Point(i + 1, j));
            if (j > 0 && connectsRight(grid[i][j - 1])) pipes.add(new Point(i, j - 1));
            if (j < grid[i].length - 1 && connectsLeft(grid[i][j + 1])) pipes.add(new Point(i, j + 1));

            if (pipes.size() != 2) throw new AssertionError(pipes);
            return pipes.toArray(Point[]::new);
        }

        return switch (grid[i][j]) {
            case '|' -> new Point[]{new Point(i - 1, j), new Point(i + 1, j)};
            case '-' -> new Point[]{new Point(i, j - 1), new Point(i, j + 1)};
            case 'J' -> new Point[]{new Point(i - 1, j), new Point(i, j - 1)};
            case 'L' -> new Point[]{new Point(i - 1, j), new Point(i, j + 1)};
            case '7' -> new Point[]{new Point(i + 1, j), new Point(i, j - 1)};
            case 'F' -> new Point[]{new Point(i + 1, j), new Point(i, j + 1)};
            default -> throw new IllegalStateException("Unexpected value: " + grid[i][j]);
        };
    }

    char overwriteCharS(char[][] grid) {
        if (grid[i][j] != 'S') throw new AssertionError();

        var goesUp = i > 0 && connectsDown(grid[i - 1][j]);
        var goesDown = i < grid.length - 1 && connectsUp(grid[i + 1][j]);
        var goesLeft = j > 0 && connectsRight(grid[i][j - 1]);
        var goesRight = j < grid[i].length - 1 && connectsLeft(grid[i][j + 1]);

        if (goesUp && goesDown) return '|';
        if (goesLeft && goesRight) return '-';
        if (goesUp && goesRight) return 'L';
        if (goesDown && goesRight) return 'F';
        if (goesUp && goesLeft) return 'J';
        if (goesDown && goesLeft) return '7';

        throw new AssertionError();
    }

    static boolean connectsDown(char c) {
        return c == '|' || c == 'F' || c == '7';
    }

    static boolean connectsUp(char c) {
        return c == '|' || c == 'L' || c == 'J';
    }

    static boolean connectsRight(char c) {
        return c == '-' || c == 'L' || c == 'F';
    }

    static boolean connectsLeft(char c) {
        return c == '-' || c == '7' || c == 'J';
    }
}
