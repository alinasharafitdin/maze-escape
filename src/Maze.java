/**
 * Missions 1 and 2. Building the maze out of the walls.
 * A wall k joins the cells w.a()[k] and w.b()[k] and costs w.cost()[k]. Indexes start from 0,
 * the line number in walls.txt is the index + 1. The cells are connected by DisjointSet.
 */
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Maze {

    // Mission 1: process walls in file order.
    public static int[] breakWalls(Input.Walls w) {
        DisjointSet dsu = new DisjointSet(w.cells());

        int[] broken = new int[w.cells() - 1];
        int used = 0;

        for (int k = 0; k < w.count(); k++) {
            int a = w.a()[k];
            int b = w.b()[k];

            if (dsu.union(a, b)) {
                broken[used] = k;
                used++;
            }
        }

        return Arrays.copyOf(broken, used);
    }

    // Mission 2: Kruskal.
    public static int[] cheapestMaze(Input.Walls w) {
        Integer[] order = new Integer[w.count()];

        for (int k = 0; k < w.count(); k++) {
            order[k] = k;
        }

        Arrays.sort(
                order,
                Comparator.comparingInt(k -> w.cost()[k])
        );

        DisjointSet dsu = new DisjointSet(w.cells());

        int[] broken = new int[w.cells() - 1];
        int used = 0;

        for (int k : order) {
            if (dsu.union(w.a()[k], w.b()[k])) {
                broken[used] = k;
                used++;

                if (used == w.cells() - 1) {
                    break;
                }
            }
        }

        return Arrays.copyOf(broken, used);
    }

    // Mission 2: Prim with a priority queue.
    public static long primCost(Input.Walls w) {
        int n = w.cells();

        // Count walls touching each cell.
        int[] degree = new int[n];

        for (int k = 0; k < w.count(); k++) {
            degree[w.a()[k]]++;
            degree[w.b()[k]]++;
        }

        // Store incident wall indexes for every cell.
        int[][] incident = new int[n][];

        for (int cell = 0; cell < n; cell++) {
            incident[cell] = new int[degree[cell]];
        }

        int[] filled = new int[n];

        for (int k = 0; k < w.count(); k++) {
            int a = w.a()[k];
            int b = w.b()[k];

            incident[a][filled[a]++] = k;
            incident[b][filled[b]++] = k;
        }

        boolean[] inTree = new boolean[n];

        PriorityQueue<Integer> queue = new PriorityQueue<>(
                Comparator.comparingInt(k -> w.cost()[k])
        );

        // Start from cell 0.
        inTree[0] = true;
        int reached = 1;
        long total = 0;

        for (int k : incident[0]) {
            queue.add(k);
        }

        while (!queue.isEmpty() && reached < n) {
            int k = queue.remove();

            int a = w.a()[k];
            int b = w.b()[k];

            // Both cells are already connected to the tree.
            if (inTree[a] && inTree[b]) {
                continue;
            }

            int next = inTree[a] ? b : a;

            inTree[next] = true;
            reached++;
            total += w.cost()[k];

            // Add walls leading from the new cell
            // to cells outside the tree.
            for (int edge : incident[next]) {
                int other;

                if (w.a()[edge] == next) {
                    other = w.b()[edge];
                } else {
                    other = w.a()[edge];
                }

                if (!inTree[other]) {
                    queue.add(edge);
                }
            }
        }

        if (reached != n) {
            throw new IllegalArgumentException(
                    "Wall graph is disconnected"
            );
        }

        return total;
    }
}
