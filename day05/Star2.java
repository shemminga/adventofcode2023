import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import static java.lang.Long.parseLong;
import static java.lang.Math.*;
import static java.lang.System.out;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test.txt");
    doFile("input.txt");
}

void doFile(String filename) throws Exception {
    out.println(STR."*** input file: \{filename} ***");

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var q = lines.filter(not(String::isBlank))
                .map(String::trim)
                .collect(Collectors.toCollection(ArrayDeque::new));

        var min = solve(q);
        out.println("min = " + min);
    }

    out.println();
}

record Range(long start, long len, long last) {

    Range {
        if (start + len - 1 != last) throw new AssertionError(start + " " + len + " " + last);
    }

    static Range firstLen(long s, long l) {
        return new Range(s, l, s + l - 1);
    }

    static Range firstLast(long s, long l) {
        return new Range(s, l - s + 1, l);
    }

    boolean intersects(Range o) {
        if (last() < o.start()) return false;
        if (start() > o.last()) return false;
        return true;
    }

    boolean canBeMappedFullyBy(Range o) {
        return o.start() <= start() && o.last() >= last();
    }

    Range mappedPart(Range o) {
        if (!intersects(o)) throw new AssertionError (this + " " + o);
        return firstLast(max(start(), o.start()), min(last(), o.last()));
    }

    Range unmappedPartBefore(Range o) {
        if (!intersects(o)) throw new AssertionError(this + " " + o);
        if (start() < o.start()) return firstLast(start(), o.start() - 1);
        return null;
    }

    Range unmappedPartAfter(Range o) {
        if (!intersects(o)) throw new AssertionError(this + " " + o);
        if (last() > o.last() + 1) return firstLast(o.last() + 1, last());
        return null;
    }
}

long solve(Deque<String> q) {
    var first = q.removeFirst();
    var seeds = Arrays.stream(first.split(" "))
            .skip(1)
            .mapToLong(Long::parseLong)
            .toArray();

    var seedRanges = createPairs(seeds);


    var locations = mapSeeds(seedRanges, q);

    return locations.stream()
            .mapToLong(Range::start)
            .min()
            .orElseThrow();
}

List<Range> createPairs(long[] inp) {
    var ranges = new LinkedList<Range>();
    for (int i = 0; i < inp.length; i += 2) ranges.add(Range.firstLen(inp[i], inp[i + 1]));
    return ranges;
}

List<Range> mapSeeds(List<Range> items, Deque<String> q) {
    var nextCat = new LinkedList<Range>();

    while (!q.isEmpty()) {
        var s = q.removeFirst();

        if (s.contains(":")) {
            nextCat.addAll(items);
            items = nextCat;
            nextCat = new LinkedList<>();

            out.printf("Before %32s %s%n", s, items);

            continue;
        }

        var parts = s.split(" ");
        var dest = parseLong(parts[0]);
        var src = parseLong(parts[1]);
        var len = parseLong(parts[2]);
        var shift = dest - src;
        var sRange = Range.firstLen(src, len);

        var remnants = new ArrayList<Range>();

        var iterator = items.iterator();
        while(iterator.hasNext()) {
            var item = iterator.next();

            if (item.intersects(sRange)) {
                iterator.remove();



                if (item.canBeMappedFullyBy(sRange)) {
                    nextCat.add(Range.firstLen(item.start() + shift, item.len));
                } else {
                    var mapped = item.mappedPart(sRange);
                    var unmappedBefore = item.unmappedPartBefore(sRange);
                    var unmappedAfter = item.unmappedPartAfter(sRange);

                    //out.printf("%s %s -> %s + %s + %s%n", item, sRange, unmappedBefore, mapped, unmappedAfter);

                    nextCat.add(Range.firstLen(mapped.start() + shift, mapped.len()));
                    if (unmappedBefore != null) remnants.add(unmappedBefore);
                    if (unmappedAfter != null) remnants.add(unmappedAfter);
                }
            }
        }

        items.addAll(remnants);
    }

    nextCat.addAll(items);

    out.printf("%-39s %s%n", "End:", items);

    return nextCat;
}
