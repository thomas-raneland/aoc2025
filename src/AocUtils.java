import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.LongStream;

@SuppressWarnings({"unused", "SameParameterValue", "UseOfSystemOutOrSystemErr"})
class AocUtils {
    static char[][] grid(String input) {
        List<String> lines = input.lines().toList();
        char[][] grid = new char[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            grid[i] = lines.get(i).toCharArray();
        }
        return grid;
    }

    static int modulo(int a, int b) {
        int remainder = a % b;
        return remainder < 0 ? remainder + b : remainder;
    }

    static long modulo(long a, long b) {
        long remainder = a % b;
        return remainder < 0 ? remainder + b : remainder;
    }

    static long lcm(LongStream stream) {
        return stream.reduce(1, AocUtils::lcm);
    }

    static long lcm(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }

        return Math.abs(a) * (Math.abs(b) / gcd(a, b));
    }

    static long gcd(LongStream stream) {
        return stream.reduce(1, AocUtils::gcd);
    }

    static long gcd(long a, long b) {
        if (a == 0 || b == 0) {
            return a + b;
        }

        var min = Math.min(Math.abs(a), Math.abs(b));
        var max = Math.max(Math.abs(a), Math.abs(b));
        return gcd(max % min, min);
    }

    static <V> Set<V> union(Set<? extends V> a, Set<? extends V> b) {
        var u = new HashSet<V>();
        u.addAll(a);
        u.addAll(b);
        return u;
    }

    public static <T> Set<T> diff(Set<T> a, Set<T> b) {
        Set<T> d = new HashSet<>(a);
        d.removeAll(b);
        return d;
    }

    public static <T> List<List<T>> product(List<T> values, int length) {
        List<List<T>> combinations = new ArrayList<>();
        collectPermutations(values, length, new ArrayList<>(), combinations);
        return combinations;
    }

    private static <T> void collectPermutations(List<T> values, int length, List<T> prefix, List<List<T>> combinations) {
        if (prefix.size() == length) {
            combinations.add(List.copyOf(prefix));
        } else {
            for (T value : values) {
                prefix.addLast(value);
                collectPermutations(values, length, prefix, combinations);
                prefix.removeLast();
            }
        }
    }

    static class Graph<T> {
        private final Map<T, Set<NodeDistance<T>>> neighbors;

        public Graph() {
            neighbors = new HashMap<>();
        }

        void addEdge(T source, T destination, long weight) {
            neighbors.computeIfAbsent(source, k -> new HashSet<>()).add(new NodeDistance<>(destination, weight));
            neighbors.computeIfAbsent(destination, k -> new HashSet<>());
        }

        long longestPath(T start, T end) {
            return longestPath(start, end, new HashSet<>());
        }

        private long longestPath(T pos, T end, Set<T> visited) {
            if (pos.equals(end)) {
                return 0;
            }

            visited.add(pos);
            long max = Long.MIN_VALUE;

            for (var next : neighbors.get(pos)) {
                if (!visited.contains(next.node())) {
                    long distance = next.distance() + longestPath(next.node(), end, visited);

                    if (distance > max) {
                        max = distance;
                    }
                }
            }

            visited.remove(pos);
            return max;
        }

        long dijkstra(T start, T end) {
            return dijkstra(start, t -> Objects.equals(t, end));
        }

        long dijkstra(T start, Predicate<T> isEnd) {
            var distances = new HashMap<T, Long>();
            var queue = new PriorityQueue<NodeDistance<T>>(neighbors.size(), Comparator.comparingLong(NodeDistance::distance));
            var visited = new HashSet<T>();

            distances.put(start, 0L);
            queue.add(new NodeDistance<>(start, 0));

            while (!queue.isEmpty()) {
                var u = queue.poll();

                if (visited.add(u.node())) {
                    for (var v : neighbors.getOrDefault(u.node(), Set.of())) {
                        if (!visited.contains(v.node())) {
                            var newDistance = distances.get(u.node()) + v.distance();

                            if (newDistance < distances.getOrDefault(v.node(), Long.MAX_VALUE)) {
                                distances.put(v.node(), newDistance);
                                queue.add(new NodeDistance<>(v.node, newDistance));
                            }
                        }
                    }
                }
            }

            return distances.keySet()
                            .stream()
                            .filter(isEnd)
                            .mapToLong(distances::get)
                            .min()
                            .orElse(Long.MAX_VALUE);
        }

        private record NodeDistance<T>(T node, long distance) {}
    }

    static String download(int day) {
        if (day < 1 || day > 25) {
            throw new IllegalArgumentException("Day must be 1-25");
        }

        try (HttpClient client = HttpClient.newHttpClient()) {
            Path path = Path.of("input/" + day + ".txt");

            if (!Files.exists(path)) {
                String cookies = Files.readString(Path.of("cookies.txt")).replace("\r", "").replace("\n", "");
                String content = client.send(HttpRequest.newBuilder()
                                                        .uri(URI.create("https://adventofcode.com/2025/day/" + day + "/input"))
                                                        .setHeader("Cookie", cookies)
                                                        .build(), HttpResponse.BodyHandlers.ofString())
                                       .body();
                Files.writeString(path, content);
            }

            return Files.readString(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String downloadLeaderboard(boolean force) {
        long id = 2897173;

        try (HttpClient client = HttpClient.newHttpClient()) {
            Path path = Path.of("leaderboard/" + 2897173 + ".json");

            if (force || !Files.exists(path)) {
                String cookies = Files.readString(Path.of("cookies.txt")).replace("\r", "").replace("\n", "");
                String content = client.send(HttpRequest.newBuilder()
                                                        .uri(URI.create(
                                                                "https://adventofcode.com/2025/leaderboard/private/view/" + id
                                                                + ".json"))
                                                        .setHeader("Cookie", cookies)
                                                        .build(), HttpResponse.BodyHandlers.ofString())
                                       .body();

                Files.createDirectories(path.getParent());
                Files.writeString(path, content);
            }

            return Files.readString(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static void waitForStartTime(int day) {
        if (day < 1 || day > 25) {
            throw new IllegalArgumentException("Day must be 1-25");
        }

        Instant start = Instant.parse("2025-12-XXT05:00:01Z".replace("XX", String.format("%02d", day)));

        while (Instant.now().isBefore(start)) {
            System.out.print(".");

            try {
                //noinspection BusyWait
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println();
    }
}
