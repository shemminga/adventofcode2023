import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import static java.lang.Integer.parseInt;
import static java.lang.System.*;
import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

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
                .toList();

        Map<String, List<Map<Character, Integer>>> positions = new HashMap<>();
        Map<String, String> rules = new HashMap<>();

        positions.put("in", new ArrayList<>());
        positions.put("A", new ArrayList<>());
        positions.put("R", new ArrayList<>());

        for (String is : input) {
            if (is.charAt(0) == '{') positions.get("in").add(parsePart(is));
            else {
                var accIdx = is.indexOf('{');
                var name = is.substring(0, accIdx);
                var rule = is.substring(accIdx + 1, is.length() - 1);
                rules.put(name, rule);
                positions.put(name, new ArrayList<>());
            }
        }

        var solution = solve(rules, positions);

        out.println("solution = " + solution);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

long solve(Map<String, String> rules, Map<String, List<Map<Character, Integer>>> positions) {
    var totalPartsCount = positions.get("in").size();

    var box = box(positions);
    while (box.isPresent()) {
        var name = box.get().getKey();
        var parts = box.get().getValue();
        applyRule(rules.get(name), parts, positions);

        box = box(positions);
    }

    var acceptedPartsCount = positions.get("A").size();
    var rejectedPartsCount = positions.get("R").size();

    if (acceptedPartsCount + rejectedPartsCount != totalPartsCount) throw new AssertionError();

    return positions.get("A")
            .stream()
            .flatMap(p -> p.values()
                    .stream())
            .mapToLong(x -> x)
            .sum();
}

void applyRule(String rule, List<Map<Character, Integer>> parts, Map<String, List<Map<Character, Integer>>> positions) {
    var split = rule.split(",");

    for (String s : split) {
        if (!s.contains(":")) {
            positions.get(s).addAll(parts);
            parts.clear();
            break;
        }

        var split1 = s.split(":");
        var clause = split1[0];
        var target = split1[1];

        moveMatchesTo(clause, target, parts, positions);
    }
}

void moveMatchesTo(String clause, String target, List<Map<Character, Integer>> parts,
        Map<String, List<Map<Character, Integer>>> positions) {
    var c = clause.charAt(0);
    var op = clause.charAt(1);
    var val = parseInt(clause.substring(2));

    var iterator = parts.iterator();
    while (iterator.hasNext()) {
        var p = iterator.next();

        boolean match;
        if (op == '>') match = p.get(c) > val;
        else if (op == '<') match = p.get(c) < val;
        else throw new AssertionError();

        if (match) {
            iterator.remove();
            positions.get(target).add(p);
        }
    }
}

Optional<Entry<String, List<Map<Character, Integer>>>> box(Map<String, List<Map<Character, Integer>>> positions) {
    return positions.entrySet()
            .stream()
            .filter(e -> !"A".equals(e.getKey()))
            .filter(e -> !"R".equals(e.getKey()))
            .filter(e -> !e.getValue().isEmpty())
            .findAny();
}

Map<Character, Integer> parsePart(String s) {
    var ratings = s.substring(1, s.length() - 1)
            .split(",");
    return stream(ratings)
            .map(r -> r.split("="))
            .collect(toMap(r -> r[0].charAt(0), r -> parseInt(r[1])));
}
