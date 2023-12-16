import java.nio.file.*;
import java.util.*;
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

        var count = markEnergized(grid);

        out.println("count = " + count);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

enum Dir {
    U, D, L, R;

    int nextI(int i) {
        return switch (this) {
        case U -> i - 1;
        case D -> i + 1;
        case L, R -> i;
        };
    }

    int nextJ(int j) {
        return switch (this) {
            case U, D -> j;
            case L -> j - 1;
            case R -> j + 1;
        };
    }

    Dir mirrorFwdSlash() {
        return switch (this) {
            case U -> R;
            case D -> L;
            case L -> D;
            case R -> U;
        };
    }

    Dir mirrorBwSlash() {
        return switch (this) {
            case U -> L;
            case D -> R;
            case L -> U;
            case R -> D;
        };
    }

    boolean ignoresSplitter(char c) {
        return switch (this) {
            case U, D -> c == '|';
            case L, R -> c == '-';
        };
    }
};

long markEnergized(char[][] grid) {
    var energized = new boolean[grid.length][grid[0].length];

    markEnergized(grid, energized);

    var count = 0;
    for (boolean[] er : energized) for (boolean e : er) if (e) count++;

    return count;
}

record MovePoint(int i, int j, Dir d) {}

void markEnergized(char[][] grid, boolean[][] energized) {
    var q = new ArrayDeque<MovePoint>();
    var visited = new ArrayList<MovePoint>();
    addNext(q, 0, -1, Dir.R);

    while (!q.isEmpty()) {
        //out.println("q.size() = " + q.size());
        var p = q.removeFirst();
        var i = p.i();
        var j = p.j();
        var d = p.d();

        if (i < 0 || i >= grid.length) continue;
        if (j < 0 || j >= grid[0].length) continue;
        if (visited.contains(p)) continue;
        visited.add(p);

        energized[i][j] = true;

        switch (grid[i][j]) {
            case '.' -> addNext(q, i, j, d);
            case '/' -> addNext(q, i, j, d.mirrorFwdSlash());
            case '\\' -> addNext(q, i, j, d.mirrorBwSlash());
            case '-', '|' -> {
                if (d.ignoresSplitter(grid[i][j])) addNext(q, i, j, d);
                else {
                    addNext(q, i, j, d.mirrorFwdSlash());
                    addNext(q, i, j, d.mirrorBwSlash());
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + grid[i][j]);
        };
    }
}

private static void addNext(ArrayDeque<MovePoint> q, int curI, int curJ, Dir newDir) {
    q.addLast(new MovePoint(newDir.nextI(curI), newDir.nextJ(curJ), newDir));
}
