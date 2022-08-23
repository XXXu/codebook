package org.example.poj.ac.two;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main003 {
    public static int job(int[][] jobs) {
        if (jobs.length == 1) {
            return 1;
        }
        int[] sortByMoney = new int[jobs.length];
        for (int i = 0; i < jobs.length; i++) {
            sortByMoney[i] = jobs[i][1];
        }
        sortByMoney = Arrays.stream(sortByMoney).distinct().sorted().toArray();

        Map<Integer, Integer> indexByMoney = new HashMap<>();
        for (int i = 0; i < sortByMoney.length; i++) {
            indexByMoney.put(sortByMoney[i], i);
        }

        Map<Integer, Integer> loadIndexByPay = new HashMap<>();
        for (int[] i : jobs) {
            Integer v = loadIndexByPay.get(i[1]);
            if (v == null || v > i[0]) {
                loadIndexByPay.put(i[1], i[0]);
            }
        }

        int total = 0;
        for (int[] job : jobs) {
            int load = job[0];
            int pay = job[1];

            int minLoad = loadIndexByPay.get(pay);
            if (load > minLoad){
                continue;
            }

            int payIndex = indexByMoney.get(pay);
            boolean skip = false;
            for (int m = payIndex + 1; m < jobs.length; m++) {
                int nextMoney = sortByMoney[m];
                int nextLoad = loadIndexByPay.get(nextMoney);
                if (nextLoad <= load) {
                    skip = true;
                    break;
                }
            }

            if (!skip) {
                total++;
            }
        }
        return total;
    }

    public static int readInt(StreamTokenizer st) throws IOException {
        st.nextToken();
        return (int) st.nval;
    }

    public static void main(String[] args) throws IOException {
        StreamTokenizer st =new StreamTokenizer(new BufferedReader(new InputStreamReader(System.in)));
        int a = readInt(st);
        int[][] jobs = new int[a][2];
        for (int i = 0; i < a; i++) {
            jobs[i][0] = readInt(st);
            jobs[i][1] = readInt(st);
        }
        int res = job(jobs);
        System.out.println(res);
    }
}
