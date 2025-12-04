import java.util.List;

public class Day04 {
    private static final String TEST_INPUT = """
            ..@@.@@@@.
            @@@.@.@.@@
            @@@@@.@.@@
            @.@@@@..@.
            @@.@@@@.@@
            .@@@@@@@.@
            .@.@.@.@@@
            @.@@@.@@@@
            .@@@@@@@@.
            @.@.@@@.@.""";

    public static void main(String... args) {
        AocUtils.waitForStartTime(4);
        String realInput = AocUtils.download(4);

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

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (check(x, y, grid)) {
                    res++;
                }
            }
        }

        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        long res = 0;
        char[][] grid = AocUtils.grid(input);
        boolean removed;

        do {
            removed = false;

            for (int y = 0; y < grid.length; y++) {
                for (int x = 0; x < grid[y].length; x++) {
                    if (check(x, y, grid)) {
                        res++;
                        grid[y][x] = '.';
                        removed = true;
                    }
                }
            }
        } while (removed);

        System.out.println("Part II: " + res);
    }

    private static boolean check(int x, int y, char[][] grid) {
        if (grid[y][x] == '.') {
            return false;
        }

        int count = 0;

        for (int y1 = y - 1; y1 < y + 2; y1++) {
            for (int x1 = x - 1; x1 < x + 2; x1++) {
                if (x1 == x && y1 == y) {
                    continue;
                }

                if (y1 < 0 || y1 >= grid.length || x1 < 0 || x1 >= grid[y1].length || grid[y1][x1] == '.') {
                    count++;
                }
            }
        }

        return count > 4;
    }
}
