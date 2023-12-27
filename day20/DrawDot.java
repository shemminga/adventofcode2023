import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import static java.lang.String.join;
import static java.lang.System.*;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;

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

        draw(types, inputs, outputs);

        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

void draw(Map<String, Character> types, Map<String, List<String>> inputs, Map<String, List<String>> outputs) {
    var clusters = clusterize(types, inputs, outputs);
    var allClustered = Stream.concat(clusters.values()
                            .stream()
                            .flatMap(List::stream),
                    clusters.keySet()
                            .stream())
            .toList();

    out.println("digraph {");
    out.println("  node [shape=oval, style=filled, fillcolor=plum1]");
    out.println("  edge [color=darkgreen, penwidth=3]");
    out.println("  peripheries=0");
    out.println();

    out.println("  subgraph cluster_grouping {");
    out.println("    edge [color=black, penwidth=1]");

    clusters.forEach((cBase, cluster) -> {
        out.println();
        out.printf("    subgraph cluster_%s {%n", cBase);
        out.printf("      node [shape=box, style=filled, fillcolor=skyblue]%n");
        out.println();
        out.printf("      %s [shape=oval, style=filled, fillcolor=plum1]%n", cBase);
        out.println();

        cluster.forEach(i -> {
            var outs = outputs.get(i)
                    .stream()
                    .filter(n -> cluster.contains(n) || cBase.equals(n))
                    .collect(joining(" "));

            out.printf("      %s -> { %s }%n", i, outs);
        });

        var cbOuts = outputs.get(cBase)
                .stream()
                .filter(cluster::contains)
                .collect(joining(" "));
        out.printf("      %s -> { %s } [color=red, penwidth=3, constraint=false]%n", cBase, cbOuts);
        out.println("    }");
    });

    out.println("  }");
    out.println();

    out.println("  subgraph cluster_roadcaster {");
    out.println("    node [shape=box, style=rounded]");
    out.println();
    out.println("    roadcaster;");
    out.println("  }");
    out.println();

    out.println("  subgraph cluster_rx {");
    out.println("    node [shape=oval, style=filled, fillcolor=plum1]");
    out.println("    edge [color=black, penwidth=1]");
    out.println();
    out.println("    rx [shape=box, style=rounded]");

    outputs.forEach((n, outs) -> {
        if ("roadcaster".equals(n)) return;
        if (allClustered.contains(n)) return;

        var sOuts = outs.stream()
                .filter(not(allClustered::contains))
                .filter(not("roadcaster"::equals))
                .collect(joining(" "));
        out.printf("    %s -> { %s }%n", n, sOuts);
    });

    out.println("  }");
    out.println();


    outputs.forEach((n, outs) -> {
        if (allClustered.contains(n)) {
            var ncOuts = outs.stream()
                    .filter(not(allClustered::contains))
                    .toList();
            if (!ncOuts.isEmpty()) {
                out.printf("  %s -> { %s }%n", n, join(" ", ncOuts));
            }
        } else {
            var cOuts = outs.stream()
                    .filter(allClustered::contains)
                    .toList();
            out.printf("  %s -> { %s }%n", n, join(" ", cOuts));
        }
    });

    out.println("}");
}

HashMap<String, List<String>> clusterize(Map<String, Character> types, Map<String, List<String>> inputs, Map<String, List<String>> outputs) {
    var cBases = List.of("zp", "rg", "sj", "pp");

    var clusters = new HashMap<String, List<String>>();

    for (String cBase : cBases) {
        var cluster = new ArrayList<String>();
        outputs.get(cBase)
                .stream()
                .filter(n -> types.get(n) == '%')
                .forEach(cluster::add);
        inputs.get(cBase)
                .stream()
                .filter(n -> types.get(n) == '%')
                .filter(not(cluster::contains))
                .forEach(cluster::add);
        clusters.put(cBase, cluster);
    }

    return clusters;
}

private static final Pattern ARROW = Pattern.compile(" -> ");
private static final Pattern COMMA = Pattern.compile(", ");
