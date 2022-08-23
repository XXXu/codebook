package org.example.poj.ac.two;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.*;

public class Main001 {
    public static StreamTokenizer st = new StreamTokenizer(new BufferedReader(new InputStreamReader(System.in)));
    public static Map<String, Boolean> v;
    public static Map<String, Boolean> path;
    public static Map<String, List<String>> g;
    public static boolean isFlg = false;

    public static void main(String[] args) throws IOException {
        st.ordinaryChar('-');
        int numCourses = nextTokenAndReadInt();
        int numberRelation = nextTokenAndReadInt();
        String[][] prerequisites = new String[numberRelation][2];
        v = new HashMap<>(numCourses);
        path = new HashMap<>(numCourses);
        g = new HashMap<>(numCourses);
        for (int i = 0; i < numberRelation; i++) {
            prerequisites[i] = readString();
            v.putIfAbsent(prerequisites[i][0], false);
            path.putIfAbsent(prerequisites[i][0], false);
            g.computeIfAbsent(prerequisites[i][0], k -> new LinkedList<>());
            if (prerequisites[i].length == 2) {
                v.putIfAbsent(prerequisites[i][1], false);
                path.putIfAbsent(prerequisites[i][1], false);
                g.computeIfAbsent(prerequisites[i][1], k -> new LinkedList<>());
            }
        }
        Map<String, List<String>> graph = build(prerequisites);
        for (String s : v.keySet()) {
            trav(graph, s);
        }
        System.out.println(isFlg ? "NO" : "YES");
    }

    public static void trav(Map<String, List<String>> graph, String s) {
        if (path.get(s)) {
            isFlg = true;
        }
        if (v.get(s) || isFlg) {
            return;
        }
        v.put(s, true);
        path.put(s, true);
        for (String t : graph.get(s)) {
            trav(graph, t);
        }
        path.put(s, false);
    }

    public static String[] readString() throws IOException {
        st.nextToken();
        String to = st.sval;
        st.nextToken();
        st.nextToken();
        st.nextToken();
        String from = st.sval;

        return new String[]{to, from};
    }

    public static int readI() {
        return (int) st.nval;
    }

    public static int nextTokenAndReadInt() {
        nextToken();
        return readI();
    }

    public static int nextToken() {
        try {
            return st.nextToken();
        } catch (Exception e) {
            return StreamTokenizer.TT_EOF;
        }
    }

    public static Map<String, List<String>> build(String[][] prerequisites) {
        for (String[] edge : prerequisites) {
            String f = edge[1], to = edge[0];
            g.get(f).add(to);
        }
        return g;
    }

}

