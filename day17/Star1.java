import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.lang.System.*;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparingLong;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test1.txt");
    doFile("input-test2.txt");
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
                .map(s -> s.chars()
                        .map(c -> c - '0')
                        .toArray())
                .toArray(int[][]::new);

        var solution = solve(grid);

        out.println("solution = " + solution);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

enum Dir {L, R, U, D}

record MvPt(int i, int j, Dir d) {
    Stream<MvPt> next() {
        return switch (d) {
            case L -> Stream.of(
                    new MvPt(i - 1, j, Dir.U),
                    new MvPt(i - 1, j - 1, Dir.U),
                    new MvPt(i - 1, j - 2, Dir.U),
                    new MvPt(i + 1, j, Dir.D),
                    new MvPt(i + 1, j - 1, Dir.D),
                    new MvPt(i + 1, j - 2, Dir.D));
            case R -> Stream.of(new MvPt(i - 1, j, Dir.U),
                    new MvPt(i - 1, j + 1, Dir.U),
                    new MvPt(i - 1, j + 2, Dir.U),
                    new MvPt(i + 1, j, Dir.D),
                    new MvPt(i + 1, j + 1, Dir.D),
                    new MvPt(i + 1, j + 2, Dir.D));
            case U -> Stream.of(new MvPt(i, j - 1, Dir.L),
                    new MvPt(i - 1, j - 1, Dir.L),
                    new MvPt(i - 2, j - 1, Dir.L),
                    new MvPt(i, j + 1, Dir.R),
                    new MvPt(i - 1, j + 1, Dir.R),
                    new MvPt(i - 2, j + 1, Dir.R));
            case D -> Stream.of(new MvPt(i, j - 1, Dir.L),
                    new MvPt(i + 1, j - 1, Dir.L),
                    new MvPt(i + 2, j - 1, Dir.L),
                    new MvPt(i, j + 1, Dir.R),
                    new MvPt(i + 1, j + 1, Dir.R),
                    new MvPt(i + 2, j + 1, Dir.R));
        };
    }

    MvPt[] skipPts() {
        return switch (d) {
            case L -> new MvPt[]{new MvPt(i, j - 1, Dir.L), new MvPt(i, j - 2, Dir.L)};
            case R -> new MvPt[]{new MvPt(i, j + 1, Dir.R), new MvPt(i, j + 2, Dir.R)};
            case U -> new MvPt[]{new MvPt(i - 1, j, Dir.U), new MvPt(i - 2, j, Dir.U)};
            case D -> new MvPt[]{new MvPt(i + 1, j, Dir.D), new MvPt(i + 2, j, Dir.D)};
        };
    }
}

long solve(int[][] grid) {
    var hLoss = new long[grid.length][grid[0].length][Dir.values().length];
    for (long[][] dim1 : hLoss) for (long[] dim2 : dim1) Arrays.fill(dim2, Long.MAX_VALUE);
    hLoss[0][1][Dir.R.ordinal()] = grid[0][1];
    hLoss[1][0][Dir.D.ordinal()] = grid[1][0];

    var q = new PriorityQueue<MvPt>(comparingLong(p -> hLoss[p.i()][p.j()][p.d().ordinal()]));
    q.add(new MvPt(0, 1, Dir.R));
    q.add(new MvPt(1, 0, Dir.D));

    while (!q.isEmpty()) {
        var mp = q.remove();
        var sp = mp.skipPts();
        var curHL = hLoss[mp.i()][mp.j()][mp.d().ordinal()];

        mp.next()
                .filter(n -> isInRange(grid, n.i(), n.j()))
                .forEach(n -> {
                    int mhDist = mhDist(n, mp);

                    var newHL = curHL;
                    if (mhDist >= 2) newHL += grid[sp[0].i()][sp[0].j()];
                    if (mhDist >= 3) newHL += grid[sp[1].i()][sp[1].j()];
                    newHL += grid[n.i()][n.j()];

                    var set = setHLoss(hLoss, n, newHL);
                    if (set) q.add(n);
                });

        // End point might be reachable directly
        if (sp[0].i() == grid.length - 1 && sp[0].j() == grid[0].length - 1) {
            setHLoss(hLoss, sp[0], curHL + grid[sp[0].i()][sp[0].j()]);
        }

        if (sp[1].i() == grid.length - 1 && sp[1].j() == grid[0].length - 1) {
            setHLoss(hLoss, sp[1], curHL + grid[sp[0].i()][sp[0].j()] + grid[sp[1].i()][sp[1].j()]);
        }
    }

    dumpHLoss(hLoss);

    return stream(Dir.values())
            .mapToInt(Dir::ordinal)
            .mapToLong(d -> hLoss[grid.length - 1][grid[0].length - 1][d])
            .min()
            .orElseThrow();
}

void dumpHLoss(long[][][] hLoss) {
    if (!test) return;
    for (long[][] dim1 : hLoss) {
        for (long[] dim2 : dim1) {
            var sb = new StringBuilder();
            for (long dim3 : dim2) {
                if (dim3 != Long.MAX_VALUE) sb.append(format("%2d", dim3));
                else sb.append("..");
                sb.append('/');
            }
            sb.deleteCharAt(sb.length() - 1);
            out.printf("| %15s  ", sb);
        }
        out.println();
    }
    out.println();
}

boolean setHLoss(long[][][] hLoss, MvPt p, long newHL) {
    var i = p.i();
    var j = p.j();
    var d = p.d().ordinal();
    if (newHL >= hLoss[i][j][d]) return false;
    hLoss[i][j][d] = newHL;
    return true;
}

int mhDist(MvPt p1, MvPt p2) {
    var mhDist = abs(p2.i() - p1.i()) + abs(p2.j() - p1.j());

    if (mhDist < 1 || mhDist > 3)
        throw new AssertionError(p1 + " " + p2 + " " + mhDist);

    return mhDist;
}

boolean isInRange(int[][] grid, int i, int j) {
    if (i < 0 || i >= grid.length) return false;
    if (j < 0 || j >= grid[i].length) return false;
    return true;
}