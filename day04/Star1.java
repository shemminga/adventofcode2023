import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import static java.lang.Integer.parseInt;
import static java.lang.System.out;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test.txt");
    doFile("input.txt");
}

static final Pattern SPACES = Pattern.compile(" +");

void doFile(String filename) throws Exception {
    out.println(STR."*** input file: \{filename} ***");

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var score = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(SPACES::split)
                .map(Arrays::asList)
                .map(l -> l.subList(2, l.size()))
                .mapToLong(this::score)
                .sum();

        out.println("score = " + score);
    }

    out.println();
}

long score(List<String> card) {
    var winners = new HashSet<Integer>();

    var score = 0L;
    var storing = true;

    for (String nr : card)
        if ("|".equals(nr)) storing = false;
        else if (storing) winners.add(parseInt(nr));
        else if (winners.contains(parseInt(nr))) score += (score == 0) ? 1 : score;

    return score;
}
