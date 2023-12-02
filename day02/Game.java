import java.util.*;
import java.util.regex.Pattern;
import static java.lang.Integer.parseInt;

record Game(int id, List<Grab> grabs) {
    private static final Pattern GRABS_PATTERN = Pattern.compile("; ");

    static Game of(String line) {
        int cIdx = line.indexOf(':');

        var id = parseInt(line.substring(5, cIdx));
        var sGrabs = GRABS_PATTERN.split(line.substring(cIdx + 2));

        var grabs = Arrays.stream(sGrabs)
                .map(Grab::of)
                .toList();

        return new Game(id, grabs);
    }
}
