import java.nio.file.*;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;
import static java.lang.Math.max;
import static java.lang.System.*;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test1.txt");
    doFile("input.txt");
    out.printf("*** %s *** DONE ***%n", LocalTime.now());
}

long fileStartTime = 0;
boolean test = false;

void doFile(String filename) throws Exception {
    out.printf("*** %s *** input file: %s ***%n", LocalTime.now(), filename);
    test = filename.contains("test");
    fileStartTime = nanoTime();

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var grid = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map (String::toCharArray)
                .toArray(char[][]::new);

        var solution = solve(grid);

        out.println("solution = " + solution);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

long solve(char[][] grid) {
    var q = new PriorityQueue<Route>(Comparator.<Route>comparingInt(r -> r.visiteds().size()).reversed());

    q.add(Route.of(0, 1));

    var longestRoute = -1;
    while (!q.isEmpty()) {
        var cr = q.remove();
        var cp = cr.cur;

        if (cp.i == grid.length - 1 && cp.j == grid[0].length - 2) {
            out.println("+++ Found route of length " + (cr.visiteds.size() - 1));
            longestRoute = max(longestRoute, cr.visiteds.size() - 1);
            continue;
        }

        //out.printf("Current longest unfinished route: %d, q size: %d%n", cr.visiteds.size(), q.size());

        movablePoints(cp, grid)
                .filter(p -> !cr.visiteds.contains(p))
                .forEach(p -> {
                    q.add(cr.addStep(p));
                });
    }

    return longestRoute;
}

Stream<Pt> movablePoints(Pt cp, char[][] grid) {
    return switch (grid[cp.i][cp.j]) {
        case '^' -> Stream.of(new Pt(cp.i - 1, cp.j));
        case '>' -> Stream.of(new Pt(cp.i, cp.j + 1));
        case 'v' -> Stream.of(new Pt(cp.i + 1, cp.j));
        case '<' -> Stream.of(new Pt(cp.i, cp.j - 1));
        case '.' -> Stream.of(new Pt(cp.i - 1, cp.j),
                        new Pt(cp.i + 1, cp.j),
                        new Pt(cp.i, cp.j - 1),
                        new Pt(cp.i, cp.j + 1))
                .filter(p -> inRange(p, grid))
                .filter(p -> canMove(p, grid));
        default -> throw new IllegalStateException("Unexpected value: " + grid[cp.i][cp.j] + ' ' + cp);
    };
}

boolean inRange(Pt p, char[][] grid) {
    if (p.i < 0 || p.i >= grid.length) return false;
    if (p.j < 0 || p.j >= grid[0].length) return false;
    return true;
}

boolean canMove(Pt p, char[][] grid) {
    return grid[p.i][p.j] != '#';
}

record Pt(int i, int j) {}
record Route(Pt cur, Set<Pt> visiteds) {
    Route {
        if (!visiteds.contains(cur)) throw new AssertionError();
    }

    static Route of(int i, int j) {
        Pt cur = new Pt(i, j);
        Set<Pt> visiteds = new HashSet<>();
        visiteds.add(cur);
        return new Route(cur, visiteds);
    }

    Route addStep(Pt pt) {
        var newV = new HashSet<Pt>(visiteds);
        newV.add(pt);
        return new Route(pt, newV);
    }
}