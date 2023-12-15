import java.nio.file.*;
import java.util.*;
import static java.lang.System.*;
import static java.util.function.Predicate.not;

void main() throws Exception {
    doFile("input-test1.txt");
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
        var instr = lines.filter(not(String::isBlank))
                .map(String::trim)
                .findFirst()
                .orElseThrow()
                .split(",");

        var sum = solve(instr);

        out.println("sum = " + sum);
        out.printf("%,5d ns%n", nanoTime() - fileStartTime);
    }

    out.println();
}

long solve(String[] instr) {
    var boxes = new ArrayList<LinkedList<Slot>>(NBOXES);
    for (int i = 0; i < NBOXES; i++) boxes.add(new LinkedList<>());

    for (String i : instr) {
        var boxId = hash(i);

        if (i.endsWith("-")) remove(boxes.get(boxId), i);
        else add(boxes.get(boxId), i);

        dumpBoxes(i, boxes);
    }

    return calcFocPower(boxes);
}

long calcFocPower(List<LinkedList<Slot>> boxes) {
    var totPower = 0L;

    for (int i = 0; i < boxes.size(); i++)
        for (int j = 0; j < boxes.get(i).size(); j++) {
            var slotPower = i + 1;
            slotPower *= j + 1;
            slotPower *= boxes.get(i).get(j).focalLength();
            totPower += slotPower;
        }

    return totPower;
}

void remove(LinkedList<Slot> box, String instr) {
    var label = instr.substring(0, instr.length() - 1);
    for (int i = 0; i < box.size(); i++)
        if (label.equals(box.get(i).label())) {
            box.remove(i);
            break;
        }
}

void add(LinkedList<Slot> box, String instr) {
    var label = instr.substring(0, instr.length() - 2);
    int focLen = instr.charAt(instr.length() - 1) - '0';
    var slot = new Slot(label, focLen);

    for (int i = 0; i < box.size(); i++) {
        if (label.equals(box.get(i).label())) {
            box.set(i, slot);
            return;
        }
    }

    box.add(slot);
}

int hash(String instr) {
    var cv = 0;

    for (char c : instr.toCharArray()) {
        if (c == '=' || c == '-') break;
        cv += c;
        cv *= 17;
        cv %= NBOXES;
    }

    return cv;
}

void dumpBoxes(String instr, List<LinkedList<Slot>> boxes) {
    if (!test) return;

    out.printf("After \"%s\":%n", instr);

    for (int i = 0; i < boxes.size(); i++)
        if (!boxes.get(i).isEmpty())
            out.printf("Box %3d: %s%n", i, boxes.get(i));

    out.println();
}


record Slot(String label, int focalLength) {
    public String toString() {
        return label + ' ' + focalLength;
    }
}

public static final int NBOXES = 256;
