import java.nio.file.*;
import java.util.*;
import static java.lang.Long.parseLong;
import static java.lang.System.out;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;

void main() throws Exception {
    doFile("input-test.txt");
    doFile("input.txt");
}

void doFile(String filename) throws Exception {
    out.println(STR."*** input file: \{filename} ***");

    var uri = getClass().getResource(filename).toURI();
    try (var lines = Files.lines(Paths.get(uri))) {
        var rounds = lines.filter(not(String::isBlank))
                .map(String::trim)
                .map(Round::of)
                .toArray(Round[]::new);

        Arrays.sort(rounds, this::lowToHigh);

        out.println("Arrays.toString(rounds) = " + Arrays.toString(rounds));

        long winnings = 0;
        for (int i = 0; i < rounds.length; i++) winnings += ((long) i + 1) * rounds[i].bid;

        out.println("winnings = " + winnings);
    }

    out.println();
}

static final List<Character> CARD_ORDER = List.of('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2');
enum HandType {
    HIGH_CARD, PAIR, TWO_PAIR, TRICE, FULL_HOUSE, CARRE, POKER;

    static HandType fromHand(String hand) {
        var histogram = hand.chars()
                .boxed()
                .collect(groupingBy(c -> c, counting()));

        //out.println(hand + " = " + histogram);

        var pairs = 0;
        var trices = 0;
        for (long value : histogram.values()) {
            if (value > 5) throw new AssertionError(hand + ' ' + value);
            if (value == 5) return POKER;
            if (value == 4) return CARRE;
            if (value == 3) trices++;
            else if (value == 2) pairs++;
            else if (value != 1) throw new AssertionError(hand + ' ' + value);
        }

        if (trices > 1 || pairs > 2) throw new AssertionError(hand + ' ' + trices + ' ' + pairs);

        if (trices == 1 && pairs == 1) return FULL_HOUSE;
        if (trices == 1) return TRICE;
        if (pairs == 2) return TWO_PAIR;
        if (pairs == 1) return PAIR;
        return HIGH_CARD;
    }
}

int lowToHigh(Round r1, Round r2) {
    return lowToHigh(r1.hand(), r2.hand());
}

int lowToHigh(String h1, String h2) {
    var t1 = HandType.fromHand(h1);
    //out.println(h1 + " = " + t1);
    var t2 = HandType.fromHand(h2);
    //out.println(h2 + " = " + t2);

    if (t1 == t2) return cardCmpL2H(h1, h2);

    return Integer.compare(t1.ordinal(), t2.ordinal());
}

int cardCmpL2H(String h1, String h2) {
    for (int i = 0; i < h1.length(); i++) {
        var c1 = h1.charAt(i);
        var c2 = h2.charAt(i);
        var i1 = CARD_ORDER.indexOf(c1);
        var i2 = CARD_ORDER.indexOf(c2);
        var cmp = Integer.compare(i2, i1);
        if (cmp != 0) return cmp;
    }

    return 0;
}

record Round(String hand, long bid) {
    static Round of(String line) {
        var split = line.split(" ");
        return new Round(split[0], parseLong(split[1]));
    }
}