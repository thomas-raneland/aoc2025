import java.util.List;

public class Day00 {
    private static final String TEST_INPUT = """
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(0);
        String realInput = AocUtils.download(0);

        for (String input : List.of(TEST_INPUT, realInput)) {
            System.out.println(input);
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        var res = 0;
        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        // TODO
    }
}
