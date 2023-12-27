import java.nio.file.*;
import java.util.*;
import static java.lang.Integer.parseInt;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.lang.System.*;
import static java.util.Comparator.comparingLong;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;

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
    try (var fileLines = Files.lines(Paths.get(uri))) {
        var instr = fileLines.filter(not(String::isBlank))
                .map(String::trim)
                .toArray(String[]::new);

        var lines = new ArrayList<Line>();
        var ci = 0L;
        var cj = 0L;
        for (String s : instr) {
            var newLine = Line.of(s, ci, cj);
            ci = newLine.i2();
            cj = newLine.j2();
            lines.add(newLine);
        }

        if (ci != 0 || cj != 0) throw new AssertionError();

        var solution = solve(lines);

        out.println("solution = " + solution);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}


record Line(long i1, long j1, long i2, long j2) {
    static final String DIRS = "RDLU";

    Line {
        if (i1 != i2 && j1 != j2) throw new AssertionError();
        if (i1 == i2 && j1 == j2) throw new AssertionError();
    }

    static Line of(String s, long ci, long cj) {
        var dist = parseInt(s.substring(s.length() - 7, s.length() - 2), 16);
        var dir = DIRS.charAt(s.charAt(s.length() - 2) - '0');

        var ni = switch (dir) {
            case 'U' -> ci - dist;
            case 'D' -> ci + dist;
            case 'L', 'R' -> ci;
            default -> throw new IllegalStateException("Unexpected value: " + s.charAt(0));
        };

        var nj = switch (dir) {
            case 'U', 'D' -> cj;
            case 'L' -> cj - dist;
            case 'R' -> cj + dist;
            default -> throw new IllegalStateException("Unexpected value: " + s.charAt(0));
        };

        var line = new Line(ci, cj, ni, nj);
        //out.printf("%s -> %c %d %s %d%n", s, dir, dist, line, line.len());

        return line;
    }

    Line align() {
        if (i1 < i2) return this;
        if (i1 == i2 && j1 < j2) return this;
        return new Line(i2, j2, i1, j1);
    }

    long len() {
        return abs(i2 - i1) + abs(j2 - j1) + 1;
    }

    boolean isHorizontal() {
        return i1 == i2;
    }

    boolean hasEndpoint(long i, long j) {
        return (i1 == i && j1 == j) || (i2 == i && j2 == j);
    }

    boolean connects(Line o) {
        return hasEndpoint(o.i1, o.j1) || hasEndpoint(o.i2, o.j2);
    }

    public String toString() {
        if (isHorizontal()) return format("<H i=%d, j=%d..%d, len=%d>", i1, j1, j2, len());
        return format("<V i=%d..%d, j=%d, len=%d>", i1, i2, j1, len());
    }
}

long solve(List<Line> lines) {
    var q = lines.stream()
            .map(Line::align)
            .filter(Objects::nonNull)
            .collect(toCollection(() -> new PriorityQueue<>(
                    comparingLong(Line::i1)
                    .thenComparingLong(Line::j1)
                    .thenComparingLong(Line::i2)
                    .thenComparingLong(Line::j2))));

    //for (Line line : q) {
    //    out.println(line);
    //}

    // j2 == j1 for vertical lines, and j2 > j1 for horizontal lines, so this gives vertical lines an edge
    var activeLines = new TreeSet<>(comparingLong(Line::j1)
            .thenComparingLong(Line::j2));

    var curI = 0L;
    var surface = 0L;
    while (!q.isEmpty() || !activeLines.isEmpty()) {
        if (activeLines.isEmpty()) {
            activeLines.add(q.remove());
            curI = activeLines.getFirst().i1();
        }

        makeAllActiveOnLine(curI, activeLines, q);

        var nextI = q.isEmpty() ? Long.MAX_VALUE : q.peek().i1;
        var in = false;
        var inJ = 0L;
        var toAdd = 0L;
        for (Line line : activeLines) {
            nextI = Math.min(nextI, line.i2 + 1);

            //if (test) out.printf(",- Line %s - in? %b - inJ: %d%n", line, in, inJ);

            if (line.isHorizontal()) {
                var cLines = connectingLines(line, activeLines);

                //if (test) out.printf("|  cLines for %s: %s%n", line, Arrays.toString(cLines));

                if (cLines[0].i1 < line.i1 == cLines[1].i1 < line.i1) {
                    // They go the same way. The first vertical line flipped in/out, and the second will flip it again.
                    // So the in/out remains the same, which is correct. Only adjust for the length of the horizontal
                    // line.
                    if (!in) toAdd += line.len() <= 2 ? 0 : (line.len() - 2);
                } else {
                    // They go different ways, so there should be a flip of in/out. The first vertical took care of one,
                    // the second will do another, so the horizontal should do a third.

                    if (in) {
                        // If we started out, we are now in. Flip to out, and adjust for the horizontal line and the
                        // width of the first vertical line. As of the second vertical line, everything works as normal.

                        in = false;
                        toAdd += line.len() - 1;
                    } else {
                        // If we started in, we are now out. The first vertical was counted properly. We should adjust
                        // for the lenght of the horizontal line, and flip out to in. The second line will flip back to
                        // out. When doing so, it will add something to toAdd. Make sure it's just the width of the
                        // vertical line.

                        in = true;
                        toAdd += line.len() <= 2 ? 0 : (line.len() - 2);
                        inJ = line.j2;
                        //if (test) out.printf("|  H Set inJ to %d%n", inJ);
                    }
                }
            } else if (in) {
                //if (test) out.printf("|  toAdd += %d - %d + 1%n", line.j1, inJ);
                toAdd += line.j1 - inJ + 1;
                in = false;
            } else {
                in = true;
                inJ = line.j1;
                //if (test) out.printf("|  V Set inJ to %d%n", inJ);
            }

            //if (test) out.printf("`- toAdd %d%n", toAdd);
        }

        if (in) throw new AssertionError(activeLines);
        if (nextI == Long.MAX_VALUE) throw new AssertionError();
        if (nextI <= curI) throw new AssertionError(nextI + " < " + curI);

        //if (test)
        //    out.printf("--- curI: %7d - nextI: %7d - toAdd: %7d - activeLines: %s%n", curI, nextI, toAdd, activeLines);
        surface += (nextI - curI) * toAdd;
        curI = nextI;

        var curICopy = curI; // Stupid anon function hack
        activeLines.removeIf(l -> l.i2 < curICopy);
    }

    return surface;
}

void makeAllActiveOnLine(long curI, Collection<Line> activeLines, PriorityQueue<Line> q) {
    while(!q.isEmpty() && q.peek().i1 <= curI) {
        var l = q.remove();
        if (l.i1 < curI) throw new AssertionError("Line should've been active already (curI=" + curI + "): " + l);
        activeLines.add(l);
    }
}

Line[] connectingLines(Line line, Collection<Line> activeLines) {
    var cLines = activeLines.stream()
            .filter(l -> l != line)
            .filter(line::connects)
            .toArray(Line[]::new);

    if (cLines.length != 2) throw new AssertionError(line + " connects " + Arrays.toString(cLines));

    return cLines;
}
