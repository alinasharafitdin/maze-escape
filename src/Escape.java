import java.util.Arrays;

public class Escape {

    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int n = w.cells();
        int[][] open = Graphs.open(w, broken);

        // Count all cells with exactly one open door.
        int deadEnds = 0;

        for (int[] neighbours : open) {
            if (neighbours.length == 1) {
                deadEnds++;
            }
        }

        boolean[] seen = new boolean[n];

        // Explicit DFS stack and next direction for each frame.
        int[] stack = new int[n];
        int[] nextDirection = new int[n];

        int top = 0;
        int visited = 1;

        stack[0] = 0;
        seen[0] = true;

        while (top >= 0) {
            int current = stack[top];

            // The stack contains the path from start to exit.
            if (current == n - 1) {
                int[] path = Arrays.copyOf(stack, top + 1);
                return new Result(path, visited, deadEnds);
            }

            // All four directions tried: return to the previous cell.
            if (nextDirection[top] == 4) {
                top--;
                continue;
            }

            int direction = nextDirection[top]++;
            int next;

            // Try up, right, down, left.
            switch (direction) {
                case 0:
                    next = current - w.cols();
                    break;

                case 1:
                    next = current % w.cols() < w.cols() - 1
                            ? current + 1 : -1;
                    break;

                case 2:
                    next = current + w.cols();
                    break;

                default:
                    next = current % w.cols() > 0
                            ? current - 1 : -1;
                    break;
            }

            if (next < 0 || next >= n || seen[next]) {
                continue;
            }

            boolean hasDoor = false;

            for (int neighbour : open[current]) {
                if (neighbour == next) {
                    hasDoor = true;
                    break;
                }
            }

            if (!hasDoor) {
                continue;
            }

            seen[next] = true;
            visited++;

            top++;
            stack[top] = next;
            nextDirection[top] = 0;
        }

        return new Result(new int[0], visited, deadEnds);
    }
}