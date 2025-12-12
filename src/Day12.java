import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Day12 {
    private static final String TEST_INPUT = """
            0:
            ###
            ##.
            ##.
            
            1:
            ###
            ##.
            .##
            
            2:
            .##
            ###
            ##.
            
            3:
            ##.
            ###
            ##.
            
            4:
            ###
            #..
            ###
            
            5:
            ###
            .#.
            ###
            
            4x4: 0 0 0 0 2 0
            12x5: 1 0 1 0 2 2
            12x5: 1 0 1 0 3 2
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(12);
        String realInput = AocUtils.download(12);

        for (String input : List.of(TEST_INPUT, realInput)) {
            System.out.println(input);
            partI(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        ParsedInput parsedInput = parse(input);
        BrailleCounter counter = new BrailleCounter();
        long res = 0;

        for (Region r : parsedInput.regions()) {
            Board board = new Board(r.width(), r.height(), new BitSet());
            boolean fits = fits(board, parsedInput.shapes(), r.counts(), new HashSet<>(), new HashMap<>());

            if (fits) {
                res++;
            }

            System.out.print(counter.add(fits));
        }

        System.out.println(counter);
        System.out.println("Part I: " + res);
    }

    private static ParsedInput parse(String input) {
        List<Shape> shapes = new ArrayList<>();
        List<Region> regions = new ArrayList<>();
        List<boolean[]> matrix = new ArrayList<>();

        for (String l : input.lines().toList()) {
            if (l.isBlank()) {
                if (!matrix.isEmpty()) {
                    shapes.add(new Shape(matrix.toArray(boolean[][]::new)));
                }
            } else if (l.endsWith(":")) {
                matrix.clear();
            } else if (l.startsWith("#") || l.startsWith(".")) {
                boolean[] row = new boolean[l.length()];
                for (int i = 0; i < l.length(); i++) {
                    row[i] = l.charAt(i) == '#';
                }
                matrix.add(row);
            } else {
                String[] parts = l.split(" ");
                String[] dims = parts[0].replace(":", "").split("x");
                int width = Integer.parseInt(dims[0]);
                int height = Integer.parseInt(dims[1]);
                List<Integer> items = new ArrayList<>();
                for (int i = 1; i < parts.length; i++) {
                    items.add(Integer.parseInt(parts[i]));
                }
                regions.add(new Region(width, height, items));
            }
        }

        return new ParsedInput(shapes, regions);
    }

    private static boolean fits(Board board, List<Shape> shapes, List<Integer> counts,
                                Set<FitsCacheKey> fitsCache, Map<DrawCacheKey, Board> drawCache) {

        int shapesLeft = counts.stream().mapToInt(i -> i).sum();

        if (shapesLeft == 0) {
            return true;
        }

        if (!fitsCache.add(new FitsCacheKey(board, counts))) {
            return false;
        }

        int cellsLeft = 0;

        for (int i = 0; i < counts.size(); i++) {
            cellsLeft += counts.get(i) * shapes.get(i).cells();
        }

        if (board.spaceLeft() < cellsLeft) {
            return false;
        }

        for (int ix = 0; ix < counts.size(); ix++) {
            if (counts.get(ix) > 0) {
                List<Integer> newCounts = new ArrayList<>(counts);
                newCounts.set(ix, newCounts.get(ix) - 1);

                for (Shape v : shapes.get(ix).variants()) {
                    for (int x = 0; x < board.width - v.width + 1; x++) {
                        for (int y = 0; y < board.height - v.height + 1; y++) {
                            Board newBoard = drawCache.computeIfAbsent(new DrawCacheKey(v, board, x, y),
                                    k -> drawShape(k.shape(), k.board(), k.x(), k.y()));

                            if (newBoard != null && fits(newBoard, shapes, newCounts, fitsCache, drawCache)) {
                                return true;
                            }
                        }
                    }
                }

                return false;
            }
        }

        return false;
    }

    private static Board drawShape(Shape shape, Board board, int x, int y) {
        if (x + shape.width() > board.width() || y + shape.height() > board.height()) {
            return null;
        }

        for (int dy = 0; dy < shape.height(); dy++) {
            for (int dx = 0; dx < shape.width(); dx++) {
                if (shape.matrix()[dy][dx] && board.get(x + dx, y + dy)) {
                    return null;
                }
            }
        }

        Board newBoard = board.copy();

        for (int dy = 0; dy < shape.height(); dy++) {
            for (int dx = 0; dx < shape.width(); dx++) {
                if (shape.matrix()[dy][dx]) {
                    newBoard.set(x + dx, y + dy);
                }
            }
        }

        return newBoard;
    }

    private record Shape(int width, int height, boolean[][] matrix, int cells) {
        private static final Map<Shape, Set<Shape>> variantsCache = new IdentityHashMap<>();

        Shape(boolean[][] matrix) {
            this(matrix[0].length, matrix.length, matrix, countCells(matrix));
        }

        private static int countCells(boolean[][] matrix) {
            int sum = 0;
            for (boolean[] row : matrix) {
                for (boolean cell : row) {
                    if (cell) {
                        sum++;
                    }
                }
            }
            return sum;
        }

        Set<Shape> variants() {
            return variantsCache.computeIfAbsent(this, k -> {
                Set<Shape> variants = new LinkedHashSet<>();
                variants.add(this);
                variants.add(this.rotate());
                variants.add(this.rotate().rotate());
                variants.add(this.rotate().rotate().rotate());
                variants.add(this.flip());
                variants.add(this.flip().rotate());
                variants.add(this.flip().rotate().rotate());
                variants.add(this.flip().rotate().rotate().rotate());
                return variants;
            });
        }

        private Shape rotate() {
            boolean[][] rotatedMatrix = new boolean[width][height];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    rotatedMatrix[x][y] = matrix[y][width - x - 1];
                }
            }

            return new Shape(rotatedMatrix);
        }

        private Shape flip() {
            boolean[][] flippedMatrix = new boolean[height][];

            for (int y = 0; y < height; y++) {
                flippedMatrix[height - y - 1] = matrix[y];
            }

            return new Shape(flippedMatrix);
        }

        @Override
        public int hashCode() {
            return Objects.hash(width, height, Arrays.deepHashCode(matrix), cells);
        }

        @SuppressWarnings("DeconstructionCanBeUsed")
        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof Shape s && width == s.width && height == s.height && cells == s.cells &&
                                  Arrays.deepEquals(matrix, s.matrix);
        }
    }

    private record Region(int width, int height, List<Integer> counts) {}

    private record ParsedInput(List<Shape> shapes, List<Region> regions) {}

    private record Board(int width, int height, BitSet bits) {
        boolean get(int x, int y) {
            return bits.get(y * width + x);
        }

        void set(int x, int y) {
            bits.set(y * width + x);
        }

        Board copy() {
            return new Board(width, height, (BitSet) bits.clone());
        }

        public int spaceLeft() {
            return width * height - bits.cardinality();
        }
    }

    private record FitsCacheKey(Board board, List<Integer> counts) {}

    private record DrawCacheKey(Shape shape, Board board, int x, int y) {}

    private static class BrailleCounter {
        private int bits = 0;
        private int size = 0;

        String add(boolean on) {
            String s = "";
            bits = bits | ((on ? 1 : 0) << size);

            if (++size == 6) {
                s = toString();
                bits = 0;
                size = 0;
            }

            return s;
        }

        @Override
        public String toString() {
            return String.valueOf((char) (10240 + bits));
        }
    }
}
