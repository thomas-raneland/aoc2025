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
        long res = 0;

        List<Shape> shapes = new ArrayList<>();
        List<Region> regions = new ArrayList<>();

        List<boolean[]> matrix = new ArrayList<>();
        int nextNum = 0;

        for (String l : input.lines().toList()) {
            if (l.isBlank()) {
                if (!matrix.isEmpty()) {
                    shapes.add(new Shape(matrix.getFirst().length, matrix.size(), matrix.toArray(boolean[][]::new)));
                }
            } else if (l.equals(nextNum + ":")) {
                matrix.clear();
                nextNum++;
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

        for (Region r : regions) {
            drawCache.clear();
            visited.clear();

            if (fits(new Board(r.width, r.height, new BitSet()), shapes, r.counts)) {
                res++;
            }
        }

        System.out.println("Part I: " + res);
    }

    record Board(int width, int height, BitSet bits) {
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

    private record State(Board board, List<Integer> counts) {}

    private static final Set<State> visited = new HashSet<>();

    private static boolean fits(Board board, List<Shape> shapes, List<Integer> counts) {
        int shapesLeft = counts.stream().mapToInt(i -> i).sum();

        if (shapesLeft == 0) {
            return true;
        }

        if (!visited.add(new State(board, counts))) {
            return false;
        }

        int cellsLeft = 0;
        for (int i = 0; i < counts.size(); i++) {
            cellsLeft += counts.get(i) * shapes.get(i).countCells();
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
                            Board newBoard = drawShape(v, board, x, y);

                            if (newBoard != null && fits(newBoard, shapes, newCounts)) {
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

    private record DrawState(Shape s, Board board, int x, int y) {}

    private static final Map<DrawState, Board> drawCache = new HashMap<>();

    private static Board drawShape(Shape s, Board board, int x, int y) {
        return drawCache.computeIfAbsent(new DrawState(s, board, x, y), k -> {
            if (x + s.width > board.width || y + s.height > board.height) {
                return null;
            }

            for (int dx = 0; dx < s.width; dx++) {
                for (int dy = 0; dy < s.height; dy++) {
                    if (s.matrix[dy][dx] && board.get(x + dx, y + dy)) {
                        return null;
                    }
                }
            }

            Board newBoard = board.copy();

            for (int dx = 0; dx < s.width; dx++) {
                for (int dy = 0; dy < s.height; dy++) {
                    if (s.matrix[dy][dx]) {
                        newBoard.set(x + dx, y + dy);
                    }
                }
            }

            return newBoard;
        });
    }

    private static final Map<Shape, Set<Shape>> variantsCache = new IdentityHashMap<>();

    private record Shape(int width, int height, boolean[][] matrix) {
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
                variants.add(this.rotate().flip());
                variants.add(this.rotate().flip().rotate());
                variants.add(this.rotate().flip().rotate().rotate());
                variants.add(this.rotate().flip().rotate().rotate().rotate());
                return variants;
            });
        }

        Shape rotate() {
            boolean[][] rotatedMatrix = new boolean[width][height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    rotatedMatrix[x][y] = matrix[y][x];
                }
            }
            //noinspection SuspiciousNameCombination
            return new Shape(height, width, rotatedMatrix);
        }

        Shape flip() { // flips vertically
            boolean[][] flippedMatrix = new boolean[height][width];
            for (int y = 0; y < height; y++) {
                System.arraycopy(matrix[y], 0, flippedMatrix[height - y - 1], 0, width);
            }
            return new Shape(width, height, flippedMatrix);
        }

        @Override
        public int hashCode() {
            return Objects.hash(width, height, Arrays.deepHashCode(matrix));
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Shape that && toString().equals(that.toString());
        }

        @Override
        public String toString() {
            StringBuilder matrixAsString = new StringBuilder("[");
            for (var row : matrix) {
                for (var cell : row) {
                    matrixAsString.append(cell ? "#" : ".");
                }
                matrixAsString.append("|");
            }
            matrixAsString.deleteCharAt(matrixAsString.length() - 1);
            matrixAsString.append("]");
            return "Shape[width=" + width + ", height=" + height + ", matrix=" + matrixAsString + "]";
        }

        public int countCells() {
            int sum = 0;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (matrix[y][x]) {
                        sum++;
                    }
                }
            }
            return sum;
        }
    }

    private record Region(int width, int height, List<Integer> counts) {}
}
