import java.util.List;

public class Day03 {
    private static final String TEST_INPUT = """
            987654321111111
            811111111111119
            234234234234278
            818181911112111
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(3);
        String realInput = AocUtils.download(3);

        for (String input : List.of(TEST_INPUT, realInput)) {
            System.out.println(input);
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        long res = 0;

        for (String bank : input.lines().toList()) {
            res += joltage(bank, 2);
        }

        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        long res = 0;

        for (String bank : input.lines().toList()) {
            res += joltage(bank, 12);
        }

        System.out.println("Part II: " + res);
    }

    private static long joltage(String bank, int digits) {
        StringBuilder sb = new StringBuilder();
        int start = 0;

        for (int digit = 0; digit < digits; digit++) {
            String range = bank.substring(start, bank.length() - digits + 1 + digit);
            int ix = maxIndex(range);
            sb.append(range.charAt(ix));
            start += ix + 1;
        }

        return Long.parseLong(sb.toString());
    }

    private static int maxIndex(String s) {
        int maxIndex = 0;
        char maxChar = 0;

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > maxChar) {
                maxIndex = i;
                maxChar = s.charAt(i);

                if (maxChar == '9') {
                    break;
                }
            }
        }

        return maxIndex;
    }
}
