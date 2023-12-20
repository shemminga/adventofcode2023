import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import static java.lang.Integer.parseInt;
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
        var input = lines.filter(not(String::isBlank))
                .map(String::trim)
                .filter(s -> s.charAt(0) != '{')
                .toList();

        Map<String, Set<MinMax>> positions = new HashMap<>();
        positions.put("A", new HashSet<>());
        positions.put("R", new HashSet<>());

        Map<String, String> rules = new HashMap<>();

        for (String is : input) {
            var accIdx = is.indexOf('{');
            var name = is.substring(0, accIdx);
            var rule = is.substring(accIdx + 1, is.length() - 1);
            rules.put(name, rule);
            positions.put(name, new HashSet<>());
        }

        positions.get("in").add(MinMax.of());

        var solution = solve(rules, positions);

        out.println("solution = " + solution);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

long solve(Map<String, String> rules, Map<String, Set<MinMax>> positions) {
    var box = box(positions);
    while (box.isPresent()) {
        var name = box.get().getKey();
        var ranges = box.get().getValue();
        applyRule(rules.get(name), ranges, positions);

        box = box(positions);
    }

    return positions.get("A").stream()
            .mapToLong(MinMax::allows)
            .sum();
}

void applyRule(String rule, Set<MinMax> parts, Map<String, Set<MinMax>> positions) {
    for (MinMax minMax : parts) {
        applyRule(rule, minMax, positions);
    }
    parts.clear();
}

void applyRule(String rule, MinMax minMax, Map<String, Set<MinMax>> positions) {
    var split = rule.split(",");

    var cur = minMax;
    for (String s : split) {
        if (!s.contains(":")) {
            positions.get(s).add(cur);
            break;
        }

        var split1 = s.split(":");
        var clause = split1[0];
        var target = split1[1];

        positions.get(target).add(cur.matchClause(clause));
        cur = cur.notMatchClause(clause);
    }
}

Optional<Entry<String, Set<MinMax>>> box(Map<String, Set<MinMax>> positions) {
    return positions.entrySet()
            .stream()
            .filter(e -> !"A".equals(e.getKey()))
            .filter(e -> !"R".equals(e.getKey()))
            .filter(e -> !e.getValue().isEmpty())
            .findAny();
}

record MinMax(Map<Character, Integer> min, Map<Character, Integer> max) {
    static final char[] TYPES = {'x', 'm', 'a', 's'};

    static MinMax of() {
        var min = new HashMap<Character, Integer>();
        var max = new HashMap<Character, Integer>();

        for (char type : TYPES) {
            min.put(type, 1);
            max.put(type, 4000);
        }

        return new MinMax(min, max);
    }

    MinMax copy() {
        return new MinMax(new HashMap<>(min), new HashMap<>(max));
    }

    MinMax matchClause(String clause) {
        var n = copy();

        var c = clause.charAt(0);
        var op = clause.charAt(1);
        var val = parseInt(clause.substring(2));

        if (op == '>') n.min.put(c, Math.max(min.get(c), val + 1));
        else if (op == '<') n.max.put(c, Math.min(max.get(c), val - 1));
        else throw new AssertionError();

        return n;
    }

    MinMax notMatchClause(String clause) {
        var n = copy();

        var c = clause.charAt(0);
        var op = clause.charAt(1);
        var val = parseInt(clause.substring(2));

        if (op == '<') n.min.put(c, Math.max(min.get(c), val));
        else if (op == '>') n.max.put(c, Math.min(max.get(c), val));
        else throw new AssertionError();

        return n;
    }

    long allows() {
        long x = 1;
        for (char type : TYPES) {
            var mn = min.get(type);
            var mx = max.get(type);

            if (mx < mn) x = 0;

            x *= mx - mn + 1;
        }

        return x;
    }
}