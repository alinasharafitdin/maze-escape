/**
 * Missions 4 and 5. The slides: a directed graph. Cells are 0 .. n-1, slide i goes from s.from()[i] to s.to()[i]
 * and takes s.time()[i]. There are no slides from a cell to itself, and the exit can always be reached from the start.
 *
 * Ready for you: Graphs.slidesFrom(s)[u] is the list of the slides that leave cell u, and Graphs.slidesInto(s)[v] is the list
 * of the slides that enter cell v (reversed graph). The lists hold slide numbers i; the cell at the other end is s.to()[i]
 * or s.from()[i].
 */
public class Zones {

    /**
     * Mission 4. Kosaraju. Returns zoneOf: zoneOf[cell] = number of the zone, numbers 0 .. zones-1.
     * Number the zones in the order in which the second pass finds them. That order is a topological order
     * of the zone graph: every slide between two zones goes from a smaller number to a larger number.
     *
     * Plan: 1) DFS on the slides, write the cells in the order in which they are finished (use a stack, not recursion);
     * 2) go through the cells from the last finished to the first; if a cell has no zone yet, DFS from it on the reversed
     * slides (slidesInto) and give every cell it reaches the next zone number.
     * Is DAG in Main is 1 when the number of zones equals the number of cells (no cycle), so you do not need a separate check.
     */
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();

        int[][] from = Graphs.slidesFrom(s);
        int[][] into = Graphs.slidesInto(s);

        boolean[] seen = new boolean[n];

        int[] stack = new int[n];
        int[] nextEdge = new int[n];
        int[] finish = new int[n];

        int finished = 0;

        // First pass: DFS on the original graph.
        for (int start = 0; start < n; start++) {
            if (seen[start]) {
                continue;
            }

            int top = 0;
            stack[0] = start;
            nextEdge[0] = 0;
            seen[start] = true;

            while (top >= 0) {
                int current = stack[top];

                if (nextEdge[top] == from[current].length) {
                    finish[finished++] = current;
                    top--;
                } else {
                    int edge = from[current][nextEdge[top]++];
                    int next = s.to()[edge];

                    if (!seen[next]) {
                        seen[next] = true;
                        top++;
                        stack[top] = next;
                        nextEdge[top] = 0;
                    }
                }
            }
        }

        int[] zoneOf = new int[n];
        java.util.Arrays.fill(zoneOf, -1);

        int zone = 0;

        // Second pass: reversed graph, reverse finishing order.
        for (int i = finished - 1; i >= 0; i--) {
            int start = finish[i];

            if (zoneOf[start] != -1) {
                continue;
            }

            int stackSize = 0;
            stack[stackSize++] = start;
            zoneOf[start] = zone;

            while (stackSize > 0) {
                int current = stack[--stackSize];

                for (int edge : into[current]) {
                    int next = s.from()[edge];

                    if (zoneOf[next] == -1) {
                        zoneOf[next] = zone;
                        stack[stackSize++] = next;
                    }
                }
            }

            zone++;
        }

        return zoneOf;
    }
    /**
     * Mission 5. The graph of zones: the edge X to Y costs the cheapest slide from X to Y, walking inside a zone is free.
     * Go through the zones in the order of their numbers (no priority queue). Returns two numbers:
     * [0] flow stages: the number of zones on the longest chain of zones;
     * [1] the cost of the cheapest route from the zone of cell 0 to the zone of the last cell.
     */
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        // Count slides between different zones.
        int[] degree = new int[zoneCount];

        for (int i = 0; i < s.count(); i++) {
            int from = zoneOf[s.from()[i]];
            int to = zoneOf[s.to()[i]];

            if (from != to) {
                degree[from]++;
            }
        }

        // Group cross-zone slides by their source zone.
        int[][] outgoing = new int[zoneCount][];

        for (int zone = 0; zone < zoneCount; zone++) {
            outgoing[zone] = new int[degree[zone]];
        }

        int[] filled = new int[zoneCount];

        for (int i = 0; i < s.count(); i++) {
            int from = zoneOf[s.from()[i]];
            int to = zoneOf[s.to()[i]];

            if (from != to) {
                outgoing[from][filled[from]++] = i;
            }
        }

        long[] distance = new long[zoneCount];
        java.util.Arrays.fill(distance, Long.MAX_VALUE);
        distance[zoneOf[0]] = 0;

        // A chain consisting of one zone has length 1.
        int[] stages = new int[zoneCount];
        java.util.Arrays.fill(stages, 1);

        // Temporary arrays for choosing the cheapest edge X -> Y.
        int[] stamp = new int[zoneCount];
        int[] cheapest = new int[zoneCount];
        int[] targets = new int[zoneCount];

        java.util.Arrays.fill(stamp, -1);

        long longest = 1;

        // Zone numbers are already in topological order.
        for (int from = 0; from < zoneCount; from++) {
            int targetCount = 0;

            for (int edge : outgoing[from]) {
                int to = zoneOf[s.to()[edge]];

                if (stamp[to] != from) {
                    stamp[to] = from;
                    cheapest[to] = s.time()[edge];
                    targets[targetCount++] = to;
                } else {
                    cheapest[to] = Math.min(
                            cheapest[to],
                            s.time()[edge]
                    );
                }
            }

            for (int i = 0; i < targetCount; i++) {
                int to = targets[i];

                stages[to] = Math.max(
                        stages[to],
                        stages[from] + 1
                );

                if (distance[from] != Long.MAX_VALUE) {
                    distance[to] = Math.min(
                            distance[to],
                            distance[from] + cheapest[to]
                    );
                }
            }

            longest = Math.max(longest, stages[from]);
        }

        int exitZone = zoneOf[s.n() - 1];

        return new long[]{longest, distance[exitZone]};
    }
}
