import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

public class Day09 {
    private static final String TEST_INPUT = """
            7,1
            11,1
            11,7
            9,7
            9,5
            2,5
            2,3
            7,3
            """;

    public static void main(String... args) {
        AocUtils.waitForStartTime(9);
        String realInput = AocUtils.download(9);

        for (String input : List.of(TEST_INPUT, realInput)) {
            System.out.println(input);
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        List<Point> points = parsePoints(input);
        long res = 0;

        for (Point a : points) {
            for (Point b : points) {
                if (a.x < b.x) {
                    res = Math.max(res, new Rectangle(a, b).area());
                }
            }
        }

        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        List<Point> points = parsePoints(input);
        Loop loop = createLoop(points);
        List<Rectangle> rectangles = new ArrayList<>();

        for (Point a : points) {
            for (Point b : points) {
                if (a.x < b.x) {
                    rectangles.add(new Rectangle(a, b));
                }
            }
        }

        rectangles.sort(Comparator.comparingLong(Rectangle::area).reversed());
        long res = 0;

        for (Rectangle r : rectangles) {
            if (loop.containsRectangle(r.a, r.b)) {
                res = r.area();
                break;
            }
        }

        System.out.println("Part II: " + res);
    }

    private static List<Point> parsePoints(String input) {
        List<Point> points = new ArrayList<>();

        for (String line : input.lines().toList()) {
            String[] coords = line.split(",");
            points.add(new Point(Long.parseLong(coords[0]), Long.parseLong(coords[1])));
        }
        return points;
    }

    private static Loop createLoop(List<Point> points) {
        List<Line> lines = new ArrayList<>();
        Point from = points.getLast();

        for (Point to : points) {
            lines.add(Line.excl(from, to));
            from = to;
        }

        return new Loop(lines, new HashMap<>());
    }

    private record Point(long x, long y) {}

    private interface Line {
        boolean contains(Point p);

        boolean crosses(Line l);

        List<Point> points();

        static Line excl(Point from, Point to) {
            if (from.x == to.x) {
                return new VLine(from.x, Math.min(from.y, to.y), Math.max(from.y, to.y) - 1);
            } else {
                return new HLine(from.y, Math.min(from.x, to.x), Math.max(from.x, to.x) - 1);
            }
        }

        static Line incl(Point from, Point to) {
            if (from.x == to.x) {
                return new VLine(from.x, Math.min(from.y, to.y), Math.max(from.y, to.y));
            } else {
                return new HLine(from.y, Math.min(from.x, to.x), Math.max(from.x, to.x));
            }
        }
    }

    private record VLine(long x, long loY, long hiY) implements Line {
        @Override
        public boolean contains(Point p) {
            return x == p.x && loY <= p.y && hiY >= p.y;
        }

        @Override
        public boolean crosses(Line l) {
            return l instanceof HLine(long y, long loX, long hiX) && loY <= y && hiY >= y && loX <= x && hiX >= x;
        }

        @Override
        public List<Point> points() {
            return LongStream.rangeClosed(loY, hiY).mapToObj(y -> new Point(x, y)).toList();
        }
    }

    private record HLine(long y, long loX, long hiX) implements Line {
        @Override
        public boolean contains(Point p) {
            return y == p.y && loX <= p.x && hiX >= p.x;
        }

        @Override
        public boolean crosses(Line l) {
            return l instanceof VLine(long x, long loY, long hiY) && loY <= y && hiY >= y && loX <= x && hiX >= x;
        }

        @Override
        public List<Point> points() {
            return LongStream.rangeClosed(loX, hiX).mapToObj(x -> new Point(x, y)).toList();
        }
    }

    private record Rectangle(Point a, Point b) {
        long area() {
            return (Math.abs(a.x - b.x) + 1) * (Math.abs(a.y - b.y) + 1);
        }
    }

    private record Loop(List<Line> lines, Map<Point, Boolean> pointInsideCache) {
        public boolean containsRectangle(Point a, Point b) {
            Point c = new Point(a.x, b.y);
            Point d = new Point(b.x, a.y);

            // optimization
            if (!containsPoint(c) || !containsPoint(d)) {
                return false;
            }

            Line l1 = Line.incl(a, c);
            Line l2 = Line.incl(a, d);
            Line l3 = Line.incl(b, c);
            Line l4 = Line.incl(b, d);

            // must theoretically check all points inside the rectangle (or something smarter), but the border did it for my input
            for (Line l : List.of(l1, l2, l3, l4)) {
                for (Point p : l.points()) {
                    if (!containsPoint(p)) {
                        return false;
                    }
                }
            }

            return true;
        }

        private boolean containsPoint(Point p) {
            return pointInsideCache.computeIfAbsent(p, v -> {
                VLine vl = new VLine(v.x, 0, v.y);
                HLine hl = new HLine(v.y, 0, v.x);
                int cx = 0;
                int cy = 0;

                for (Line l : lines) {
                    if (l.contains(v)) {
                        return true;
                    }
                    if (l.crosses(vl)) {
                        cy++;
                    }
                    if (l.crosses(hl)) {
                        cx++;
                    }
                }

                return cx % 2 == 1 && cy % 2 == 1;
            });
        }
    }
}
