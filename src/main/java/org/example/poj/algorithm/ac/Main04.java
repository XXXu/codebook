package org.example.poj.algorithm.ac;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main04 {
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        Integer l = Integer.parseInt(bufferedReader.readLine());
        List<Integer> list = new ArrayList<>();
        for(int i=0;i<l;i++){
            list.add(Integer.parseInt(bufferedReader.readLine()));
        }

        int sec = getSec(list, l);
        List<List<Integer>> secList = new ArrayList<>();
        List<Integer> inList = new ArrayList<>();
        for (Integer integer : list) {
            inList.add(integer);
            if (check(inList, sec)) {
                secList.add(inList);
                inList = new ArrayList<>();
            }
        }
        System.out.println(get(secList));
    }

    public static int getSec(List<Integer> integers, int l) {
        int all = 0;
        for (Integer integer : integers) {
            all += integer;
        }
        return all / l;
    }

    public static int get(List<List<Integer>> lists) {
        int ret = 0;
        for (List<Integer> i : lists) {
            ret += i.size() - 1;
        }
        return ret;
    }

    public static boolean check(List<Integer> list, int se) {
        Integer a = 0;
        for (Integer integer : list) {
            a += integer;
        }
        return a != 0 && a % se == 0;
    }
}
