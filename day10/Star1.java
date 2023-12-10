import java.nio.file.*;
import java.util.*;
import static java.lang.System.out;
import static java.util.Arrays.deepToString;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test1.txt");
    doFile("input-test2.txt");
    doFile("input-test3.txt");
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

        var s = findS(grid);

        var visited = new HashSet<Point>();
        visited.add(s);
        var pts = s.getConnectedPipes(grid);
        var steps = 1;

        while (!pts[0].equals(pts[1])) {
            out.printf("- %2d: %s, %s%n", steps, ptToStr(pts[0], grid), ptToStr(pts[1], grid));
            //out.println("visited = " + visited);

            visited.add(pts[0]);
            visited.add(pts[1]);

            var p0 = pts[0].getConnectedPipes(grid);
            if (!visited.contains(p0[0])) pts[0] = p0[0];
            if (!visited.contains(p0[1])) pts[0] = p0[1];

            var p1 = pts[1].getConnectedPipes(grid);
            if (!visited.contains(p1[0])) pts[1] = p1[0];
            if (!visited.contains(p1[1])) pts[1] = p1[1];

            steps++;
        }

        out.println("steps = " + steps);
    }

    out.println();
}

String ptToStr(Point pt, char[][] grid) {
    return "(%d, %d) -> %c".formatted(pt.i(), pt.j(), grid[pt.i()][pt.j()]);
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
