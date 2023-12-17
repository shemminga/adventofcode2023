import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import static java.lang.Math.*;
import static java.lang.String.format;
import static java.lang.System.*;
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

//static final int MINMV = 0;
//static final int MAXMV = 2;

static final int MINMV = 3;
static final int MAXMV = 9;

record MvPt(int i, int j, Dir d) {
    Stream<MvPt> next() {
        return switch (d) {
            case L -> IntStream.rangeClosed(j - MAXMV, j - MINMV)
                    .mapToObj(nj -> Stream.of(new MvPt(i - 1, nj, Dir.U), new MvPt(i + 1, nj, Dir.D)))
                    .flatMap(x -> x);
            case R -> IntStream.rangeClosed(j + MINMV, j + MAXMV)
                    .mapToObj(nj -> Stream.of(new MvPt(i - 1, nj, Dir.U), new MvPt(i + 1, nj, Dir.D)))
                    .flatMap(x -> x);
            case U -> IntStream.rangeClosed(i - MAXMV, i - MINMV)
                    .mapToObj(ni -> Stream.of(new MvPt(ni, j - 1, Dir.L), new MvPt(ni, j + 1, Dir.R)))
                    .flatMap(x -> x);
            case D -> IntStream.rangeClosed(i + MINMV, i + MAXMV)
                    .mapToObj(ni -> Stream.of(new MvPt(ni, j - 1, Dir.L), new MvPt(ni, j + 1, Dir.R)))
                    .flatMap(x -> x);
        };
    }

    Stream<MvPt> skipPts() {
        return switch (d) {
            case L -> IntStream.rangeClosed(j - MAXMV, j - 1)
                    .mapToObj(nj -> new MvPt(i, nj, d));
            case R -> IntStream.rangeClosed(j + 1, j + MAXMV)
                    .mapToObj(nj -> new MvPt(i, nj, d));
            case U -> IntStream.rangeClosed(i - MAXMV, i - 1)
                    .mapToObj(ni -> new MvPt(ni, j, d));
            case D -> IntStream.rangeClosed(i + 1, i + MAXMV)
                    .mapToObj(ni -> new MvPt(ni, j, d));
        };
    }
}

long solve(int[][] grid) {
    var hLoss = new long[grid.length][grid[0].length][Dir.values().length];
    for (long[][] dim1 : hLoss) for (long[] dim2 : dim1) Arrays.fill(dim2, Long.MAX_VALUE);
    var q = new PriorityQueue<MvPt>(comparingLong(p -> hLoss[p.i()][p.j()][p.d().ordinal()]));

    var hl = 0L;
    for (int i = 1; i <= MAXMV + 1; i++) {
        if (!isInRange(grid, i, 1)) continue;
        hl += grid[i][0];
        if (i > MINMV) {
            hLoss[i][1][Dir.R.ordinal()] = hl + grid[i][1];
            q.add(new MvPt(i, 1, Dir.R));
        }
    }

    hl = 0L;
    for (int j = 1; j <= MAXMV + 1; j++) {
        if (!isInRange(grid, 1, j)) continue;
        hl += grid[0][j];
        if (j > MINMV) {
            hLoss[1][j][Dir.D.ordinal()] = hl + grid[1][j];
            q.add(new MvPt(1, j, Dir.D));
        }
    }

    var epHL = new long[]{Long.MAX_VALUE};
    while (!q.isEmpty()) {
        var mp = q.remove();
        var curHL = hLoss[mp.i()][mp.j()][mp.d().ordinal()];

        mp.next()
                .filter(n -> isInRange(grid, n.i(), n.j()))
                .forEach(n -> {
                    var newHL = curHL + grid[n.i()][n.j()] + mp.skipPts()
                            .filter(p -> isInRange(grid, p.i(), p.j()))
                            .filter(p -> mhDist(mp, p) < mhDist(mp, n))
                            .mapToLong(p -> grid[p.i()][p.j()])
                            .sum();

                    var set = setHLoss(hLoss, n, newHL);
                    if (set) q.add(n);
                });

        // End point might be reachable directly
        mp.skipPts()
                .filter(p -> p.i() == grid.length - 1 && p.j() == grid[0].length - 1)
                .filter(p -> mhDist(mp, p) >= MINMV)
                .forEach(ep -> {
                    var newHL = curHL + mp.skipPts()
                            .filter(p -> mhDist(mp, p) <= mhDist(mp, ep))
                            .mapToLong(p -> grid[p.i()][p.j()])
                            .sum();

                    epHL[0] = min(epHL[0], newHL);
                });
    }

    dumpHLoss(hLoss);

    return epHL[0];
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

    if (mhDist < 1 || mhDist > MAXMV + 1)
        throw new AssertionError(p1 + " " + p2 + " " + mhDist);

    return mhDist;
}

boolean isInRange(int[][] grid, int i, int j) {
    if (i < 0 || i >= grid.length) return false;
    if (j < 0 || j >= grid[i].length) return false;
    return true;
}