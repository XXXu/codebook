package org.example.poj.algorithm.base;

import java.util.*;

public class Star {

    interface Strategy {
        int calcu(Pos current, Pos next);
    }

    static final int[][] dirs = {{-1,0}, {1,0}, {0, -1}, {0, 1}};

    static class Pos {
        int x;
        int y;
        public int priority = 0;

        public Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int hashCode() {
            return x << 16 & y;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Pos)) return false;
            Pos other = (Pos) obj;
            return other.x == this.x && other.y == this.y;
        }

        public String pos() {
            return String.format("x: %d, y: %d", x, y);
        }
    }

    public List<Pos> astar(Pos start, Pos end, Strategy cost, Strategy heuristic) throws Exception {
        PriorityQueue<Pos> pq = new PriorityQueue<>(Comparator.comparingInt(o -> -(o.priority)));
        Map<Pos, Integer> costSoFar = new HashMap<>();
        Map<Pos, Pos> comeFrom = new HashMap<>();

        pq.add(start);
        costSoFar.put(start, 0);

        while (!pq.isEmpty()) {
            Pos cur = pq.poll();
            for (Pos neighbor : neighbor(cur, start, end)) {
                Integer cost_current = costSoFar.get(cur);
                if (cost_current == null) continue;
                int attempt_cost = cost.calcu(cur, neighbor);
                int latest_cost = attempt_cost + cost_current;
                if (!costSoFar.containsKey(neighbor) || costSoFar.get(neighbor) > latest_cost) {
                    costSoFar.put(neighbor, latest_cost);
                    neighbor.priority = -(latest_cost + heuristic.calcu(cur, end));
                    pq.add(neighbor);
                    comeFrom.put(neighbor, cur);
                }
            }
        }

        List<Pos> coords = new ArrayList<Pos>();
        Pos dest = end;
        while (comeFrom.get(dest) != null) {
            coords.add(dest);
            dest = comeFrom.remove(dest);
        }

        return coords;
    }

    List<Pos> neighbor(Pos pos, Pos start, Pos end) {
        List<Pos> res = new ArrayList<Pos>();
        int cx = pos.x, cy = pos.y;
        for(int[] dir : dirs) {
            int x = cx + dir[0], y = cy + dir[1];
            if (x < start.x || x > end.x || y < start.y || y > end.y) continue;
            res.add(new Pos(x, y));
        }
        return res;
    }

    public static void skyboarding() throws Exception {
        int[][] map = {
                {1, 2, 3, 4, 5},
                {16,17,18,19,6},
                {15,24,25,20,7},
                {14,23,22,21,8},
                {13,12,11,10,9}
        };
        Star s = new Star();
        Pos start = new Pos(2, 2);
        Pos end = new Pos(0, 0);
        List<Pos> path = s.astar(start, end, (current, next) -> {
            int i = current.x, j = current.y;
            int ni = next.x, nj = current.y;
            return map[j][i] - map[nj][ni];
        }, (current, dest) -> Math.abs(current.x - dest.x) + Math.abs(current.y - dest.y));
        System.out.println("paths: " + path);
    }

    public static void main(String[] args) throws Exception {
//        star s = new star();
//        Pos start = new Pos(0, 0);
//        Pos end = new Pos(5, 5);
//        List<Pos> path = s.astar(start, end, (current, next) -> {
//            if (next.x >= 1 && next.y == 1) return 55;
//            else if (next.x <= 3 && next.y == 3) return 55;
//            else {
//                return 0;
//            }
//        }, (current, dest) -> {
//            int x = dest.x - current.x;
//            int y = dest.y - current.y;
//            return Math.abs(x) + Math.abs(y);
//        });
//
//        System.out.println(start.pos());
//        for (int i = path.size() - 1; i >= 0; --i) {
//            System.out.println(path.get(i).pos());
//        }

        Star.skyboarding();
    }
}
