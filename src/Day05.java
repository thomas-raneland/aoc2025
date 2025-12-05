import java.util.ArrayList;
import java.util.List;

public class Day05 {
    private static final String TEST_INPUT = """
            3-5
            10-14
            16-20
            12-18
            
            1
            5
            8
            11
            17
            32
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(5);
        String realInput = AocUtils.download(5);

        for (String input : List.of(TEST_INPUT, realInput)) {
            System.out.println(input);
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private record Invl(long from, long to) {
        long size() {
            return to - from + 1;
        }

        List<Invl> exclude(Invl toExclude) {
            if (to < toExclude.from || toExclude.to < from) {
                return List.of(this);
            }

            List<Invl> split = new ArrayList<>();
            split.add(new Invl(from, toExclude.from - 1));
            split.add(new Invl(toExclude.to + 1, to));
            split.removeIf(i -> i.size() <= 0);
            return split;
        }
    }

    private static void partI(String input) {
        long res = 0;
        List<Invl> freshInvls = new ArrayList<>();
        boolean isAvailable = false;

        for (String line : input.lines().toList()) {
            if (line.isEmpty()) {
                isAvailable = true;
            } else {
                if (isAvailable) {
                    long id = Long.parseLong(line);
                    for (Invl i : freshInvls) {
                        if (i.from() <= id && i.to() >= id) {
                            res++;
                            break;
                        }
                    }
                } else {
                    int dashIx = line.indexOf("-");
                    long from = Long.parseLong(line.substring(0, dashIx));
                    long to = Long.parseLong(line.substring(dashIx + 1));
                    freshInvls.add(new Invl(from, to));
                }
            }
        }

        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        List<Invl> disjointInvls = new ArrayList<>();

        for (String line : input.lines().toList()) {
            if (line.isEmpty()) {
                break;
            }

            int ix = line.indexOf("-");
            long from = Long.parseLong(line.substring(0, ix));
            long to = Long.parseLong(line.substring(ix + 1));
            List<Invl> newInvls = List.of(new Invl(from, to));

            for (Invl invl : disjointInvls) {
                newInvls = newInvls.stream().flatMap(i -> i.exclude(invl).stream()).toList();
            }

            disjointInvls.addAll(newInvls);
        }

        long res = disjointInvls.stream().mapToLong(Invl::size).sum();
        System.out.println("Part II: " + res);
    }
}
