import java.nio.file.*;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Pattern;
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
        var input = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map (SPLIT::split)
                .toArray(String[][]::new);

        List<Edge> edges = new ArrayList<>();
        for (String[] strings : input) {
            for (int i = 1; i < strings.length; i++) {
                edges.add(new Edge(strings[0], strings[i]));
            }
        }

        var solution = solve(edges);

        out.println("solution = " + solution);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

// Karger's algorithm https://en.wikipedia.org/wiki/Karger%27s_algorithm
long solve(List<Edge> edges) {
    while (true) {
        var copy = new ArrayList<Edge>(edges);
        contract(copy);

        if (copy.size() == 3) {
            var sz1 = copy.get(0).v1.length() / 3L;
            var sz2 = copy.get(0).v2.length() / 3L;
            return sz1 * sz2;
        }
    }
}

void contract(List<Edge> edges) {
    var vertices = new HashSet<String>();
    edges.forEach(e -> {
                vertices.add(e.v1);
                vertices.add(e.v2);
            });

    while (vertices.size() > 2) {
        var idx = RANDOM.nextInt(edges.size());

        var ce = edges.get(idx);
        var newName = ce.v1 + ce.v2;

        //out.printf("Removing %s%n", ce);

        for (int i = 0; i < edges.size(); i++) {
            var ee = edges.get(i);

            if (ce.sharesVert(ee)) {
                //out.printf("%d: %s shares vertex with %s%n", i, ce, ee);
                var nv1 = (ee.v1.equals(ce.v1) || ee.v1.equals(ce.v2)) ? newName : ee.v1;
                var nv2 = (ee.v2.equals(ce.v1) || ee.v2.equals(ce.v2)) ? newName : ee.v2;
                edges.set(i, new Edge(nv1, nv2));
            }
        }

        // Remove self-loops
        edges.removeIf(e -> e.v1.equals(newName) && e.v2.equals(newName));

        vertices.remove(ce.v1);
        vertices.remove(ce.v2);
        vertices.add(newName);
    }

    //out.println("vertices = " + vertices);
    //out.println("edges.size() = " + edges.size());
    //out.println("edges = " + edges);
}

static final Pattern SPLIT = Pattern.compile(":? ");
static final Random RANDOM = new Random();

record Edge(String v1, String v2) {
    boolean sharesVert(Edge o) {
        return v1.equals(o.v1) || v2.equals(o.v1) || v1.equals(o.v2) || v2.equals(o.v2);
    }
}
