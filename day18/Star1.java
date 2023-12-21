import java.nio.file.*;
import java.util.*;
import static java.lang.Integer.parseInt;
import static java.lang.System.*;
import static java.util.Arrays.fill;
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
        var instr = lines.filter(not(String::isBlank))
                .map(String::trim)
                .toArray(String[]::new);

        //var idxs = "LRUD";
        //var dists = new int[4];
        //
        //for (String s : instr) {
        //    var split = s.split(" ");
        //    var idx = idxs.indexOf(s.charAt(0));
        //    dists[idx] += parseInt(split[1]);
        //}
        //
        //out.println("Arrays.toString(dists) = " + Arrays.toString(dists));

        //// Output:
        //// - for input-test1.txt: Arrays.toString(dists) = [10, 10, 9, 9]
        //// - for input.txt: Arrays.toString(dists) = [1236, 1236, 1171, 1171]


        var solution = solve(instr);

        out.println("solution = " + solution);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

long solve(String[] ins) {
    var grid = drawGrid(ins);

    dumpGrid(grid);
    fillGrid(grid);

    dumpGrid(grid);

    return countGrid(grid);
}

long countGrid(char[][] grid) {
    var count = 0L;

    for (char[] row : grid) for (char c : row) if (c == '#') count++;

    return count;
}

void fillGrid(char[][] grid) {
    var fillI = -1;
    var fillJ = -1;
    outer: for (int i = 0; i < grid.length; i++)
        for (int j = 0; j < grid[i].length; j++)
            if (grid[i][j] == '#') {
                // Top-left corner of polygon
                fillI = i + 1;
                fillJ = j + 1;
                break outer;
            }

    var q = new ArrayDeque<int[]>();
    q.push(new int[]{fillI, fillJ});

    while (!q.isEmpty()) {
        var p = q.removeFirst();
        int i = p[0];
        int j = p[1];

        //out.println("Arrays.toString(p) = " + Arrays.toString(p));

        if (grid[i][j] == '#') continue;
        grid[i][j] = '#';

        addIfNotFilled(q, grid, i - 1, j);
        addIfNotFilled(q, grid, i + 1, j);
        addIfNotFilled(q, grid, i, j - 1);
        addIfNotFilled(q, grid, i, j + 1);
    }
}

void addIfNotFilled(Deque<int[]> q, char[][] grid, int i, int j) {
    if (grid[i][j] == '.') q.addLast(new int[]{i, j});
}

char[][] drawGrid(String[] ins) {
    var grid = new char[2500][2500];
    for (char[] row : grid) fill(row, '.');

    var ci = 1250;
    var cj = 1250;
    grid[ci][cj] = '#';

    for (String in : ins) {
        var split = in.split(" ");
        var d = parseInt(split[1]);

        while (d > 0) {
            switch (in.charAt(0)) {
                case 'U' -> ci--;
                case 'D' -> ci++;
                case 'L' -> cj--;
                case 'R' -> cj++;
                default -> throw new IllegalStateException("Unexpected value: " + in.charAt(0));
            }

            grid[ci][cj] = '#';
            d--;
        }
    }
    return grid;
}

void dumpGrid(char[][] grid) {
    if (!test) return;

    for (int i = 1249; i < 1261; i++) {
        for (int j = 1249; j < 1258; j++) out.print(grid[i][j]);
        out.println();
    }
}
