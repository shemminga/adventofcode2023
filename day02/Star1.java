import java.nio.file.*;
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
        final int totalIds = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(Game::of)
                .filter(this::isPossible)
                .mapToInt(Game::id)
                .sum();

        out.println("totalIds = " + totalIds);
    }

    out.println();
}

static final int REQ_R = 12, REQ_G = 13, REQ_B = 14;

boolean isPossible(Game g) {
    for (var grab : g.grabs()) {
        if (grab.r() > REQ_R) return false;
        if (grab.g() > REQ_G) return false;
        if (grab.b() > REQ_B) return false;
    }
    return true;
}
