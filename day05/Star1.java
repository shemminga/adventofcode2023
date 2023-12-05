import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import static java.lang.Long.parseLong;
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

        var first = q.removeFirst();
        var seeds = Arrays.stream(first.split(" "))
                .skip(1)
                .mapToLong(Long::parseLong)
                .toArray();

        var locations = mapSeeds(seeds, q);

        var min = Arrays.stream(locations)
                .min()
                .orElseThrow();
        out.println("min = " + min);
    }

    out.println();
}

long[] mapSeeds(long[] items, Deque<String> q) {
    var nextCat = newNextCat(items.length);


    while (!q.isEmpty()) {
        var s = q.removeFirst();

        if (s.contains(":")) {
            copyUnmapped(nextCat, items);
            items = nextCat;
            nextCat = newNextCat(items.length);

            out.printf("Before %32s %s%n", s, Arrays.toString(items));

            continue;
        }

        var parts = s.split(" ");
        var dest = parseLong(parts[0]);
        var src = parseLong(parts[1]);
        var len = parseLong(parts[2]);

        for (int i = 0; i < items.length; i++)
            if (items[i] >= src && items[i] < src + len) nextCat[i] = dest + (items[i] - src);
    }

    copyUnmapped(nextCat, items);

    out.printf("%-39s %s%n", "End:", Arrays.toString(nextCat));

    return nextCat;
}

long[] newNextCat(int size) {
    var nextCat = new long[size];
    Arrays.fill(nextCat, -1);
    return nextCat;
}

void copyUnmapped(long[] nextCat, long[] items) {
    for (int i = 0; i < nextCat.length; i++)
        if (nextCat[i] == -1)
            nextCat[i] = items[i];
}
