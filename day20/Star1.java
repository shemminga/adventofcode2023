import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import static java.lang.System.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

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
        Map<String, List<String>> outputs = new HashMap<>();
        Map<String, List<String>> inputs = new HashMap<>();
        Map<String, Character> types = new HashMap<>();

        lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(ARROW::split)
                .forEach(split -> {
                    var name = split[0].substring(1);

                    var type = split[0].charAt(0);
                    if (type == '%' || type == '&' || type == 'b') types.put(name, type);
                    else throw new AssertionError(type);

                    List<String> outs = List.of(COMMA.split(split[1]));
                    outputs.put(name, outs);

                    inputs.computeIfAbsent(name, s -> new ArrayList<>());

                    outs.forEach(o -> inputs.computeIfAbsent(o, k -> new ArrayList<>())
                            .add(name));
                });

        var solution = solve(types, inputs, outputs);

        out.println("solution = " + solution);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

long solve(Map<String, Character> types, Map<String, List<String>> inputs, Map<String, List<String>> outputs) {
    //if (test) {
    //    types.forEach((k, v) -> out.printf("T: %s -> %s%n", k, v));
    //    inputs.forEach((k, v) -> out.printf("I: %s -> %s%n", k, v));
    //    outputs.forEach((k, v) -> out.printf("O: %s -> %s%n", k, v));
    //}

    Map<String, Map<String, Integer>> states = new HashMap<>();

    types.forEach((name, type) -> {
        Map<String, Integer> state = switch (type) {
            case '%' -> new HashMap<>(Map.of(name, -1));
            case '&' -> inputs.get(name)
                    .stream()
                    .collect(toMap(k -> k, _ -> -1));
            default -> Map.of();
        };
        states.put(name, state);
    });

    var sentPulses = new ArrayList<Pulse>();
    var currentPulse = 0;

    for (int i = 0; i < 1000; i++) {
        var startSize = sentPulses.size();
        sentPulses.add(new Pulse("button", "roadcaster", -1));

        while (currentPulse < sentPulses.size()) {
            var wip = sentPulses.get(currentPulse);
            currentPulse++;

            var nextHiLo = !types.containsKey(wip.to()) ? 0 : switch (types.get(wip.to())) {
                case '%' -> {
                    if (wip.hiLo() == -1) {
                        yield states.get(wip.to())
                                .computeIfPresent(wip.to(), (_, v) -> v == -1 ? 1 : -1);
                    }
                    yield 0;
                }
                case '&' -> {
                    //out.println(wip);
                    //out.printf("S: %s%n", states.get(wip.to()));
                    states.get(wip.to()).put(wip.from(), wip.hiLo());
                    //out.printf("|: %s%n", states.get(wip.to()));
                    var allHigh = states.get(wip.to()).values()
                            .stream()
                            .allMatch(v -> v == 1);
                    //out.printf("`- %b%n", allHigh);
                    if (allHigh) yield -1;
                    yield 1;
                }
                case 'b' -> wip.hiLo();
                default -> throw new IllegalStateException("Unexpected value: " + types.get(wip.to()));
            };

            if (nextHiLo != 0) outputs.get(wip.to())
                    .forEach(o -> sentPulses.add(new Pulse(wip.to(), o, nextHiLo)));
        }

        //if (test) {
        //    out.printf("%nAfter run %2d:%n", i + 1);
        //    sentPulses
        //            .stream()
        //            .skip(startSize)
        //            .forEach(out::println);
        //}
    }

    var loCnt = sentPulses.stream()
            .filter(p -> p.hiLo() == -1)
            .count();
    var hiCnt = sentPulses.stream()
            .filter(p -> p.hiLo() == 1)
            .count();

    out.println("loCnt = " + loCnt);
    out.println("hiCnt = " + hiCnt);

    return loCnt * hiCnt;
}

record Pulse(String from, String to, int hiLo) {
    Pulse {
        requireNonNull(from);
        requireNonNull(to);
        if (hiLo != -1 && hiLo != 1) throw new AssertionError(hiLo);
    }

    public String toString() {
        return from + (hiLo == -1 ? " -low" : " -high") + "-> " + to;
    }
}

private static final Pattern ARROW = Pattern.compile(" -> ");
private static final Pattern COMMA = Pattern.compile(", ");
