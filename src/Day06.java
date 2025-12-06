import java.util.ArrayList;
import java.util.List;

public class Day06 {
    private static final String TEST_INPUT = """
            123 328  51 64\s
             45 64  387 23\s
              6 98  215 314
            *   +   *   + \s""";

    public static void main(String... args) {
        AocUtils.waitForStartTime(6);
        String realInput = AocUtils.download(6);

        for (String input : List.of(TEST_INPUT, realInput)) {
            System.out.println(input);
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        long res = 0;
        List<String[]> cells = new ArrayList<>();
        input.lines().forEach(line -> cells.add(line.trim().split("[ ]+")));

        for (int x = 0; x < cells.getFirst().length; x++) {
            long[] values = new long[cells.size() - 1];

            for (int y = 0; y < cells.size() - 1; y++) {
                values[y] = Long.parseLong(cells.get(y)[x]);
            }

            res += cells.getLast()[x].equals("+") ? sum(values) : product(values);
        }

        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        long res = 0;
        char[][] grid = AocUtils.grid(input);
        List<Long> values = new ArrayList<>();

        for (int x = grid[0].length - 1; x >= 0; x--) {
            long value = 0;

            for (int y = 0; y < grid.length - 1; y++) {
                char c = grid[y][x];

                if (c >= '0' && c <= '9') {
                    value = value * 10 + (c - '0');
                }
            }

            values.add(value);

            char c = grid[grid.length - 1][x];

            if (c == '+' || c == '*') {
                long[] array = values.stream().mapToLong(l -> l).toArray();
                res += (c == '+') ? sum(array) : product(array);
                values.clear();
                x--;
            }
        }

        System.out.println("Part II: " + res);
    }

    private static long product(long[] vals) {
        long total = 1;
        for (long v : vals) {
            total *= v;
        }
        return total;
    }

    private static long sum(long[] vals) {
        long total = 0;
        for (long v : vals) {
            total += v;
        }
        return total;
    }
}
