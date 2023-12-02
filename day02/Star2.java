import java.nio.file.*;
import java.util.function.ToLongFunction;
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
        var totalIds = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(Game::of)
                .map(this::toMinGrab)
                .mapToLong(Grab::power)
                .sum();

        out.println("totalIds = " + totalIds);
    }

    out.println();
}

Grab toMinGrab(Game game) {
    long minR = maxColor(game, Grab::r);
    long minG = maxColor(game, Grab::g);
    long minB = maxColor(game, Grab::b);

    return new Grab(minR, minG, minB);
}

long maxColor(final Game game, final ToLongFunction<Grab> color) {
    return game.grabs()
            .stream()
            .mapToLong(color)
            .max()
            .orElse(0);
}
