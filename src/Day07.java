import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day07 {
    private static final String TEST_INPUT = """
            .......S.......
            ...............
            .......^.......
            ...............
            ......^.^......
            ...............
            .....^.^.^.....
            ...............
            ....^.^...^....
            ...............
            ...^.^...^.^...
            ...............
            ..^...^.....^..
            ...............
            .^.^.^.^.^...^.
            ...............
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(7);
        String realInput = AocUtils.download(7);

        for (String input : List.of(TEST_INPUT, realInput)) {
            System.out.println(input);
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        long res = 0;
        char[][] grid = AocUtils.grid(input);
        int start = new String(grid[0]).indexOf('S');
        Set<Integer> beams = Set.of(start);

        for (int y = 1; y < grid.length; y++) {
            Set<Integer> newBeams = new HashSet<>();

            for (int x : beams) {
                if (grid[y][x] == '^') {
                    res++;
                    newBeams.add(x - 1);
                    newBeams.add(x + 1);
                } else {
                    newBeams.add(x);
                }
            }

            beams = newBeams;
        }

        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        long res = 1;
        char[][] grid = AocUtils.grid(input);
        int start = new String(grid[0]).indexOf('S');
        Map<Integer, Long> beams = Map.of(start, 1L);

        for (int y = 1; y < grid.length; y++) {
            Map<Integer, Long> newBeams = new HashMap<>();

            for (int x : beams.keySet()) {
                if (grid[y][x] == '^') {
                    res += beams.get(x);
                    newBeams.merge(x - 1, beams.get(x), Long::sum);
                    newBeams.merge(x + 1, beams.get(x), Long::sum);
                } else {
                    newBeams.merge(x, beams.get(x), Long::sum);
                }
            }

            beams = newBeams;
        }

        System.out.println("Part II: " + res);
    }
}
