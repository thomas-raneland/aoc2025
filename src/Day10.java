import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class Day10 {
    private static final String TEST_INPUT = """
            [.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
            [...#.] (0,2,3,4) (2,3) (0,4) (0,1,2) (1,2,3,4) {7,5,12,7,2}
            [.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}
            """;

    private static final int MAX_DISTS = Integer.MAX_VALUE;

    private static List<Machine> parse(String input) {
        List<Machine> machines = new ArrayList<>();
        for (String line : input.lines().toList()) {
            String[] parts = line.split(" ");
            List<Boolean> lights = new ArrayList<>();
            for (int i = 1; i < parts[0].length() - 1; i++) {
                lights.add(parts[0].charAt(i) == '#');
            }
            List<Button> buttons = new ArrayList<>();
            for (int i = 1; i < parts.length - 1; i++) {
                String[] indexes = parts[i].substring(1, parts[i].length() - 1).split(",");
                List<Integer> ixs = new ArrayList<>();
                for (String ix : indexes) {
                    ixs.add(Integer.parseInt(ix));
                }
                buttons.add(new Button(ixs));
            }
            List<Integer> levels = new ArrayList<>();
            String[] levelsAsStrings = parts[parts.length - 1].substring(1, parts[parts.length - 1].length() - 1).split(",");
            for (String l : levelsAsStrings) {
                levels.add(Integer.parseInt(l));
            }
            machines.add(new Machine(lights, buttons, levels));
        }
        return machines;
    }

    public static void main(String... args) {
        AocUtils.waitForStartTime(10);
        String realInput = AocUtils.download(10);
        Loader.loadNativeLibraries();

        for (String input : List.of(TEST_INPUT, realInput)) {
            System.out.println(input);
            partI(input);
            partII(input);
            System.out.println();
        }
    }

    private static void partI(String input) {
        long res = 0;
        List<Machine> machines = parse(input);
        for (Machine m : machines) {
            res += bfs(m);
        }
        System.out.println("Part I: " + res);
    }

    private static void partII(String input) {
        AtomicLong res = new AtomicLong(0);
        List<Machine> machines = parse(input);

        machines.forEach(m -> {
            int index = machines.indexOf(m);
            long v1 = solve(m);
            long v2 = slow(index) ? Integer.MAX_VALUE : divideAndConquer(m, new int[m.levelList().size()]);
            System.out.println(index + ": " + v1 + " vs " + v2 + ((v1 != v2 ? " \33[31mDIFF\33[0m" : "")));
            res.addAndGet(Math.min(v1, v2));
        });

        System.out.println("Part II: " + res.get());
    }

    private static boolean slow(int i) {
        return Set.of(20, 23, 41, 54, 56, 78, 97, 131).contains(i);
    }

    private static long bfs(Machine machine) {
        List<Boolean> initialState = new ArrayList<>();
        for (int i = 0; i < machine.lights().size(); i++) {
            initialState.add(false);
        }

        record State(List<Boolean> lights, long presses) {}

        Queue<State> queue = new ArrayDeque<>();
        queue.add(new State(initialState, 0));

        while (!queue.isEmpty()) {
            State state = queue.poll();

            for (Button b : machine.buttons()) {
                State nextState = new State(b.toggle(state.lights), state.presses + 1);
                if (nextState.lights.equals(machine.lights())) {
                    return nextState.presses;
                }

                queue.add(nextState);
            }
        }

        throw new IllegalStateException();
    }

    private static int solve(Machine m) {
        int[] levels = m.levels();
        int counters = levels.length;
        int buttons = m.buttons().size();
        int[][] matrix = new int[counters][buttons];

        for (int b = 0; b < buttons; b++) {
            for (int c = 0; c < counters; c++) {
                if (m.buttons().get(b).lightIndexes().contains(c)) {
                    matrix[c][b] = 1;
                }
            }
        }

        MPSolver solver = new MPSolver("Solver", MPSolver.OptimizationProblemType.CBC_MIXED_INTEGER_PROGRAMMING);
        MPVariable[] variables = new MPVariable[buttons];

        for (int b = 0; b < buttons; b++) {
            variables[b] = solver.makeIntVar(0, Integer.MAX_VALUE, "x" + (b + 1));
        }

        for (int c = 0; c < counters; c++) {
            MPConstraint constraint = solver.makeConstraint(levels[c], levels[c]);

            for (int b = 0; b < buttons; b++) {
                if (matrix[c][b] != 0) {
                    constraint.setCoefficient(variables[b], matrix[c][b]);
                }
            }
        }

        MPObjective objective = solver.objective();

        for (MPVariable v : variables) {
            objective.setCoefficient(v, 1);
        }

        objective.setMinimization();

        if (solver.solve() == MPSolver.ResultStatus.OPTIMAL) {
            return Stream.of(variables).mapToInt(v -> (int) v.solutionValue()).sum();
        }

        throw new IllegalStateException("No solution for machine " + m);
    }

    private static int divideAndConquer(Machine m, int[] incomingLevels) {
        int[] levels = m.levels();

        if (Arrays.equals(incomingLevels, levels)) {
            return 0;
        }

        if (!isValid(incomingLevels, levels)) {
            return Integer.MAX_VALUE;
        }

        Map<Integer, Integer> buttonsPerLight = new HashMap<>();

        for (Button b : m.buttons()) {
            b.lightIndexes().forEach(i -> buttonsPerLight.merge(i, 1, Integer::sum));
        }

        for (int i : buttonsPerLight.keySet().stream().toList()) {
            buttonsPerLight.put(i, buttonsPerLight.getOrDefault(i, 0) * levels[i]);
        }

        if (buttonsPerLight.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        int lIx = buttonsPerLight.keySet().stream().min(Comparator.comparingInt(a -> buttonsPerLight.getOrDefault(a, 0)))
                                 .orElseThrow();
        List<Button> buttonsForLight = m.buttons().stream().filter(b -> b.lightIndexes().contains(lIx)).toList();
        List<Button> buttonsNotForLight = m.buttons().stream().filter(b -> !b.lightIndexes().contains(lIx)).toList();
        int presses = levels[lIx] - incomingLevels[lIx];
        List<int[]> outgoingLevels = allLevels(incomingLevels, buttonsForLight, presses, levels);
        Machine outM = new Machine(m.lights(), buttonsNotForLight, m.levelList());

        int best = Integer.MAX_VALUE;

        for (var outLvls : outgoingLevels) {
            int v = divideAndConquer(outM, outLvls);
            if (v < best) {
                best = presses + v;
            }
        }
        return best;
    }

    private static List<int[]> allLevels(int[] incomingLevels, List<Button> buttons, int presses, int[] mLevels) {
        List<int[]> all = new ArrayList<>();
        dist(new int[0], buttons.size(), presses, all, incomingLevels, mLevels, buttons);
        return all;
    }

    private static boolean isValid(int[] levels, int[] maxLevels) {
        for (int i = 0; i < levels.length; i++) {
            if (levels[i] > maxLevels[i]) {
                return false;
            }
        }

        return true;
    }

    private static void dist(int[] buttonPresses, int size, int presses, List<int[]> collector, int[] incomingLevels,
                             int[] mLevels,
                             List<Button> buttons) {

        if (collector.size() > MAX_DISTS) {
            return;
        }

        int pressesSoFar = 0;
        for (int i : buttonPresses) {
            pressesSoFar += i;
        }

        if (buttonPresses.length < size - 1) {
            for (int i = 0; i <= presses - pressesSoFar; i++) {
                if (collector.size() > MAX_DISTS) {
                    return;
                }
                int[] withMe = Arrays.copyOf(buttonPresses, buttonPresses.length + 1);
                withMe[withMe.length - 1] = i;
                int[] out = incomingLevels;
                for (int j = 0; j < withMe.length; j++) {
                    out = buttons.get(j).inc(out, withMe[j]);
                }
                if (isValid(out, mLevels)) {
                    dist(withMe, size, presses, collector, incomingLevels, mLevels, buttons);
                } else {
                    break;
                }
            }
        } else {
            int[] withMe = Arrays.copyOf(buttonPresses, buttonPresses.length + 1);
            withMe[withMe.length - 1] = presses - pressesSoFar;
            int[] out = incomingLevels;
            for (int i = 0; i < buttons.size(); i++) {
                out = buttons.get(i).inc(out, withMe[i]);
            }
            if (isValid(out, mLevels)) {
                collector.add(out);
            }
        }
    }

    private record Button(List<Integer> lightIndexes) {
        List<Boolean> toggle(List<Boolean> lights) {
            List<Boolean> newLights = new ArrayList<>(lights);
            for (int ix : lightIndexes) {
                newLights.set(ix, !newLights.get(ix));
            }
            return newLights;
        }

        int[] inc(int[] levels, int times) {
            int[] newLevels = Arrays.copyOf(levels, levels.length);
            for (int ix : lightIndexes) {
                newLevels[ix] += times;
            }
            return newLevels;
        }
    }

    private record Machine(List<Boolean> lights, List<Button> buttons, List<Integer> levelList) {
        int[] levels() {
            return levelList.stream().mapToInt(i -> i).toArray();
        }
    }
}
