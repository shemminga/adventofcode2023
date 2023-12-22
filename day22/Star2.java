import java.nio.file.*;
import java.util.stream.IntStream;
import static java.lang.Integer.parseInt;
import static java.lang.Math.*;
import static java.lang.System.*;
import static java.util.Comparator.*;
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
        var bricks = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(Brick::of)
                .map(Brick::align)
                .sorted()
                .toArray(Brick[]::new);

        var solution = solve(bricks);

        out.println("solution = " + solution);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

long solve(Brick[] bricks) {
    drop(bricks);

    var sum = 0L;
    for (int i = 0; i < bricks.length; i++) {
        var changes = countChangesIfBrickRemoved(bricks, i);
        sum += changes;
        out.printf("| %2d: %d sum %d%n", i, changes, sum);
    }

    return sum;
}

long countChangesIfBrickRemoved(Brick[] bricks, int i) {
    var bricksWOI = copyWithout(bricks, i);
    var bricksTD = copyWithout(bricks, i);
    drop(bricksTD);
    return IntStream.range(0, bricksTD.length)
            .filter(n -> !bricksWOI[n].equals(bricksTD[n]))
            .count();
}

Brick[] copyWithout(Brick[] bricks, int i) {
    return IntStream.range(0, bricks.length)
            .filter(n -> n != i)
            .mapToObj(n -> bricks[n])
            .toArray(Brick[]::new);
}

void drop(Brick[] bricks) {
    var changed = true;

    while (changed) {
        changed = false;
        for (int i = 0; i < bricks.length; i++) {
            tryDown: while (!bricks[i].onGround()) {
                var down = bricks[i].moveDown();

                for (int j = 0; j < bricks.length; j++)
                    if (i != j && down.intersects(bricks[j]))
                        break tryDown;

                changed = true;
                bricks[i] = down;
            }
        }
    }
}

record Brick(Pt pt1, Pt pt2) implements Comparable<Brick> {
    static Brick of(String s) {
        var split = s.split("~");
        var pt1 = Pt.of(split[0]);
        var pt2 = Pt.of(split[1]);
        return new Brick(pt1, pt2);
    }

    Brick align() {
        if (pt1.compareTo(pt2) <= 0) return this;
        return new Brick(pt2, pt1);
    }

    boolean onGround() {
        return pt1.onGround() || pt2.onGround();
    }

    Brick moveDown() {
        return new Brick(pt1.moveDown(), pt2.moveDown());
    }

    boolean intersects(Brick o) {
        if (!intersects(pt1.x, pt2.x, o.pt1.x, o.pt2.x)) return false;
        if (!intersects(pt1.y, pt2.y, o.pt1.y, o.pt2.y)) return false;
        if (!intersects(pt1.z, pt2.z, o.pt1.z, o.pt2.z)) return false;
        return true;
    }

    boolean intersects(int n1, int m1, int n2, int m2) {
        if (n1 > m1 || n2 > m2) return intersects(min(n1, m1), max(n1, m1), min(n2, m2), max(n2, m2));
        if (m1 < n2 || n1 > m2) return false;
        return true;
    }

    @Override
    public int compareTo(Brick o) {
        return comparing(Brick::pt1)
                .thenComparing(Brick::pt2)
                .compare(this, o);
    }
}

record Pt(int x, int y, int z) implements Comparable<Pt> {
    static Pt of(String s) {
        var split = s.split(",");
        var x = parseInt(split[0]);
        var y = parseInt(split[1]);
        var z = parseInt(split[2]);
        return new Pt(x, y, z);
    }

    boolean onGround() {
        return z == 1;
    }

    Pt moveDown() {
        return new Pt(x, y, z - 1);
    }

    @Override
    public int compareTo(Pt o) {
        return comparingInt(Pt::z)
                .thenComparingInt(Pt::y)
                .thenComparingInt(Pt::z)
                .compare(this, o);

    }
}
