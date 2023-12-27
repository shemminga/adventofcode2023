import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import static java.lang.System.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

void main() throws Exception {
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
    //types.forEach((k, v) -> out.printf("T: %s -> %s%n", k, v));
    //inputs.forEach((k, v) -> out.printf("I: %s -> %s%n", k, v));
    //outputs.forEach((k, v) -> out.printf("O: %s -> %s%n", k, v));

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

    var sentPulses = new ArrayDeque<Pulse>();
    var done = false;
    var buttonPresses = 0L;
    var arrayOuts = List.of("xp", "gp", "ln", "xl");
    var arrayIntervals = new long[arrayOuts.size()];

    while (!done) {
        sentPulses.add(new Pulse("button", "roadcaster", -1));
        buttonPresses++;

        //out.println("After button press " + buttonPresses);

        while (!sentPulses.isEmpty()) {
            var wip = sentPulses.removeFirst();
            //out.println("wip = " + wip);
            //if ("rx".equals(wip.to()) && wip.hiLo() == -1) done = true;

            if (arrayOuts.contains(wip.to()) && wip.hiLo == -1) {
                out.printf("After button press %d: %s got %d%n", buttonPresses, wip.to(), wip.hiLo);
                arrayIntervals[arrayOuts.indexOf(wip.to())] = buttonPresses;
            }

            var nextHiLo = !types.containsKey(wip.to()) ? 0 : switch (types.get(wip.to())) {
                case '%' -> {
                    if (wip.hiLo() == -1) {
                        yield states.get(wip.to())
                                .computeIfPresent(wip.to(), (_, v) -> v == -1 ? 1 : -1);
                    }
                    yield 0;
                }
                case '&' -> {
                    states.get(wip.to()).put(wip.from(), wip.hiLo());
                    var allHigh = states.get(wip.to()).values()
                            .stream()
                            .allMatch(v -> v == 1);
                    if (allHigh) yield -1;
                    yield 1;
                }
                case 'b' -> wip.hiLo();
                default -> throw new IllegalStateException("Unexpected value: " + types.get(wip.to()));
            };

            if (nextHiLo != 0) outputs.get(wip.to())
                    .forEach(o -> sentPulses.addLast(new Pulse(wip.to(), o, nextHiLo)));
        }

        if (buttonPresses > 4096) done = true;
    }

    // These are all prime...
    out.println("arrayIntervals = " + Arrays.toString(arrayIntervals));

    var minPresses = Arrays.stream(arrayIntervals)
            .reduce(1, (a, b) -> a * b);

    return minPresses;
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
