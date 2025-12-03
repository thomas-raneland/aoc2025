import java.util.List;

public class Day01 {
    private static final String TEST_INPUT = """
            L68
            L30
            R48
            L5
            R60
            L55
            L1
            L99
            R14
            L82""";

    public static void main(String... args) {
        AocUtils.waitForStartTime(1);
        String realInput = AocUtils.download(1);

        for (String input : List.of(TEST_INPUT, realInput)) {
            System.out.println(input);
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        int zeroes = 0;
        int val = 50;

        for (String line : input.lines().toList()) {
            int distance = Integer.parseInt(line.substring(1)) % 100;

            if (line.startsWith("R")) {
                val = (val + distance) % 100;
            } else {
                val = (val + 100 - distance) % 100;
            }

            if (val == 0) {
                zeroes++;
            }
        }

        var res = zeroes;
        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        int zeroes = 0;
        int val = 50;

        for (String line : input.lines().toList()) {
            int distance = Integer.parseInt(line.substring(1));
            zeroes += distance / 100;
            distance = distance % 100;

            if (line.startsWith("R")) {
                for (int i = 0; i < distance; i++) {
                    val = (val + 1) % 100;

                    if (val == 0) {
                        zeroes++;
                    }
                }
            } else {
                for (int i = 0; i < distance; i++) {
                    val = (val + 99) % 100;

                    if (val == 0) {
                        zeroes++;
                    }
                }
            }

        }

        var res = zeroes;
        System.out.println("Part II: " + res);
    }
}
