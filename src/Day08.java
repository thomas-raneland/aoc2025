import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Day08 {
    private static final String TEST_INPUT = """
            162,817,812
            57,618,57
            906,360,560
            592,479,940
            352,342,300
            466,668,158
            542,29,236
            431,825,988
            739,650,466
            52,470,668
            216,146,977
            819,987,18
            117,168,530
            805,96,715
            346,949,466
            970,615,88
            941,993,340
            862,61,35
            984,92,344
            425,690,689
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(8);
        String realInput = AocUtils.download(8);

        for (String input : List.of(TEST_INPUT, realInput)) {
            System.out.println(input);
            partI(input, input.equals(TEST_INPUT) ? 10 : 1000);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input, int pairs) {
        List<Point> points = parsePoints(input);
        List<Pair> sortedPairs = allPairsSorted(points);
        Set<Set<Point>> circuits = new HashSet<>();
        points.forEach(p -> circuits.add(Set.of(p)));

        for (var pair : sortedPairs.stream().limit(pairs).toList()) {
            mergeCircuits(pair, circuits);
        }

        int[] sizes = circuits.stream().mapToInt(Set::size).sorted().toArray();
        long res = (long) sizes[sizes.length - 3] * sizes[sizes.length - 2] * sizes[sizes.length - 1];
        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        List<Point> points = parsePoints(input);
        Set<Set<Point>> circuits = new HashSet<>();
        points.forEach(p -> circuits.add(Set.of(p)));
        long res = 0;

        for (var pair : allPairsSorted(points)) {
            mergeCircuits(pair, circuits);

            if (circuits.size() == 1) {
                res = pair.a().x() * pair.b().x();
                break;
            }
        }

        System.out.println("Part II: " + res);
    }

    private static List<Point> parsePoints(String input) {
        List<Point> points = new ArrayList<>();

        for (String line : input.lines().toList()) {
            long[] coords = Stream.of(line.split(",")).mapToLong(Long::parseLong).toArray();
            points.add(new Point(coords[0], coords[1], coords[2]));
        }
        return points;
    }

    private static List<Pair> allPairsSorted(List<Point> points) {
        Set<Pair> pairs = new HashSet<>();

        points.forEach(a -> points.forEach(b -> {
            if (a != b && !pairs.contains(new Pair(b, a))) {
                pairs.add(new Pair(a, b));
            }
        }));

        List<Pair> sortedPairs = new ArrayList<>(pairs);
        sortedPairs.sort(Comparator.naturalOrder());
        return sortedPairs;
    }

    private static void mergeCircuits(Pair pair, Set<Set<Point>> circuits) {
        Set<Point> newCircuit = new HashSet<>();
        List<Set<Point>> oldCircuits = new ArrayList<>();

        for (var c : circuits) {
            if (c.contains(pair.a()) || c.contains(pair.b())) {
                newCircuit.addAll(c);
                oldCircuits.add(c);
            }
        }

        oldCircuits.forEach(circuits::remove);
        circuits.add(newCircuit);
    }

    private record Point(long x, long y, long z) {
        double distance(Point that) {
            return Math.sqrt((x - that.x) * (x - that.x) + (y - that.y) * (y - that.y) + (z - that.z) * (z - that.z));
        }
    }

    private record Pair(Point a, Point b, double distance) implements Comparable<Pair> {
        Pair(Point a, Point b) {
            this(a, b, a.distance(b));
        }

        @Override
        public int compareTo(Pair o) {
            return Double.compare(distance(), o.distance());
        }
    }
}
