package org.example.poj.algorithm.ac;

import java.io.IOException;
import java.io.StreamTokenizer;

public class Main1 {
    static class Edge {
        int num, dis;
        Edge next;

        Edge(int num, int dis) {
            this.num = num;
            this.dis = dis;
        }
    }
    static Edge[] distance;
    private static void handle(StreamTokenizer in) throws IOException {
        in.nextToken();
        int n = (int) in.nval;
        in.nextToken();
        int m = (int) in.nval;
        distance = new Edge[n];
        for (int i = 0; i < m; i++) {
            in.nextToken();
            int a = (int) in.nval;
            in.nextToken();
            int b = (int) in.nval;

            addEdge(a,b, 0);
        }

        int groupNum = 0;
        for (int i = 0; i < n; i++) {
            if (distance[i] == null) {
                groupNum++;
                continue;
            }
            Edge currentInfo = distance[i];
            if (currentInfo.dis == 0) {
                groupNum ++;
                render(i, groupNum);
            }

        }
        System.out.println(groupNum);
    }

    private static void render(int index, int groupNum) {
        Edge current = distance[index];
        if (current.dis != 0) {
            return;
        }
        current.dis = groupNum;

        render(current.num, groupNum);

        Edge temp = current.next;
        while (temp != null) {
            render(temp.num, groupNum);
            temp = temp.next;
        }
    }

    private static void addEdge(int a, int b, int d) {
        if (distance[a] == null) {
            distance[a] = new Edge(b, d);
        } else {
            Edge temp = new Edge(b, d);
            temp.next = distance[a];
            distance[a] = temp;
        }
        if (distance[b] == null) {
            distance[b] = new Edge(a, d);
        } else {
            Edge temp = new Edge(a, d);
            temp.next = distance[b];
            distance[b] = temp;
        }
    }

    public static void main(String[] args) throws IOException {
        handle(new StreamTokenizer(System.in));
    }
}
