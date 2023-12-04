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

    var uri = getClass().getResource(filename)
            .toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var scores = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(SPACES::split)
                .map(Arrays::asList)
                .map(l -> l.subList(2, l.size()))
                .mapToInt(this::matches)
                .toArray();

        var nrOfCards = new long[scores.length];
        Arrays.fill(nrOfCards, 1L);

        for (int i = 0; i < scores.length; i++)
            for (int j = 1; j <= scores[i] && i + j < nrOfCards.length; j++)
                nrOfCards[i + j] += nrOfCards[i];

        var totalCardsCount = Arrays.stream(nrOfCards)
                .sum();

        out.println("totalCardsCount = " + totalCardsCount);
    }

    out.println();
}

int matches(List<String> card) {
    var winners = new HashSet<Integer>();

    var matches = 0;
    var storing = true;

    for (String nr : card)
        if ("|".equals(nr)) storing = false;
        else if (storing) winners.add(parseInt(nr));
        else if (winners.contains(parseInt(nr))) matches++;

    return matches;
}
