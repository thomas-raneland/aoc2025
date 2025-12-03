import java.util.List;

public class Day02 {
    private static final String TEST_INPUT = """
            11-22,95-115,998-1012,1188511880-1188511890,222220-222224,1698522-1698528,446443-446449,38593856-38593862,565653-565659,824824821-824824827,2121212118-2121212124
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(2);
        String realInput = AocUtils.download(2);

        for (String input : List.of(TEST_INPUT, realInput)) {
            System.out.println(input);
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        var res = 0L;

        for (String interval : input.replace("\n", "").split(",")) {
            long low = Long.parseLong(interval.substring(0, interval.indexOf("-")));
            long high = Long.parseLong(interval.substring(interval.indexOf("-") + 1));

            for (long nbr = low; nbr <= high; nbr++) {
                String figures = String.valueOf(nbr);
                int length = figures.length();

                if (length % 2 == 0 && figures.substring(0, length / 2).equals(figures.substring(length / 2))) {
                    res += nbr;
                }
            }
        }

        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        var res = 0L;

        for (String interval : input.replace("\n", "").split(",")) {
            long low = Long.parseLong(interval.substring(0, interval.indexOf("-")));
            long high = Long.parseLong(interval.substring(interval.indexOf("-") + 1));

            for (long nbr = low; nbr <= high; nbr++) {
                String figures = String.valueOf(nbr);
                int length = figures.length();

                for (int i = 2; i <= length; i++) {
                    if (length % i == 0) {
                        String repeated = figures.substring(0, length / i).repeat(i);

                        if (repeated.equals(figures)) {
                            res += nbr;
                            break;
                        }
                    }
                }
            }
        }

        System.out.println("Part II: " + res);
    }
}
