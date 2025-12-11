import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day11 {
    private static final String TEST_INPUT1 = """
            aaa: you hhh
            you: bbb ccc
            bbb: ddd eee
            ccc: ddd eee fff
            ddd: ggg
            eee: out
            fff: out
            ggg: out
            hhh: ccc fff iii
            iii: out
            """;

    private static final String TEST_INPUT2 = """
            svr: aaa bbb
            aaa: fft
            fft: ccc
            bbb: tty
            tty: ccc
            ccc: ddd eee
            ddd: hub
            hub: fff
            eee: dac
            dac: fff
            fff: ggg hhh
            ggg: out
            hhh: out
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(11);
        String realInput = AocUtils.download(11);

        for (String input : List.of(TEST_INPUT1, TEST_INPUT2, realInput)) {
            System.out.println(input);
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        Graph g = parse(input);
        long res = g.count("you", "out", Set.of());
        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        Graph g = parse(input);
        long svrDac = g.count("svr", "dac", Set.of("svr", "fft", "out"));
        long dacFft = g.count("dac", "fft", Set.of("svr", "dac", "out"));
        long fftOut = g.count("fft", "out", Set.of("svr", "dac", "fft"));
        long svrFft = g.count("svr", "fft", Set.of("svr", "dac", "out"));
        long fftDac = g.count("fft", "dac", Set.of("svr", "fft", "out"));
        long dacOut = g.count("dac", "out", Set.of("svr", "dac", "fft"));
        long res = svrDac * dacFft * fftOut + svrFft * fftDac * dacOut;
        System.out.println("Part II: " + res);
    }

    private static Graph parse(String input) {
        Map<String, Set<String>> links = new HashMap<>();

        for (String line : input.lines().toList()) {
            List<String> parts = Arrays.asList(line.split(" "));
            String from = parts.getFirst().replace(":", "");

            for (String to : parts.subList(1, parts.size())) {
                links.computeIfAbsent(from, k -> new HashSet<>()).add(to);
            }
        }

        return new Graph(links, new HashMap<>());
    }

    private record Graph(Map<String, Set<String>> links, Map<CacheKey, Long> cache) {
        private record CacheKey(String from, String to, Set<String> blocked) {}

        long count(String from, String to, Set<String> blocked) {
            if (from.equals(to)) {
                return 1;
            }

            CacheKey key = new CacheKey(from, to, blocked);

            if (!cache.containsKey(key)) {
                long total = 0;

                for (String via : links.getOrDefault(from, Set.of())) {
                    if (!blocked.contains(via)) {
                        total += count(via, to, blocked);
                    }
                }

                cache.put(key, total);
            }

            return cache.get(key);
        }
    }
}
