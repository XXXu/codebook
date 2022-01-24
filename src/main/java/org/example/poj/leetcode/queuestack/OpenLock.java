package org.example.poj.leetcode.queuestack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class OpenLock {
    public int openLock(String[] deadends, String target) {
        Set<String> deadendSet = new HashSet<>(Arrays.asList(deadends));
        if (deadendSet.contains("0000")) {
            return -1;
        }
        LinkedList<String> queue = new LinkedList<>();
        HashSet<String> endSet = new HashSet<>();
        queue.offer("0000");
        int level = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();
            while (size-- > 0) {
                String str = queue.poll();
                for (int i = 0; i < 4; i++) {
                    char ch = str.charAt(i);
                    String strAdd = str.substring(0, i) + (ch == '9' ? 0 : ch - '0' + 1) + str.substring(i + 1);
                    String strSub = str.substring(0, i) + (ch == '0' ? 9 : ch - '0' - 1) + str.substring(i + 1);
                    if (str.equals(target)) {
                        return level;
                    }
                    if (!deadendSet.contains(strAdd) && !endSet.contains(strAdd)) {
                        queue.offer(strAdd);
                        endSet.add(strAdd);
                    }
                    if (!deadendSet.contains(strSub) && !endSet.contains(strSub)) {
                        queue.offer(strSub);
                        endSet.add(strSub);
                    }

                }
            }
            level++;
        }
        return -1;
    }
    public static void main(String[] args) {
//        String[] deadends = {"0201", "0101", "0102", "1212", "2002"};
//        String target = "0202";
        String[] deadends = {"8888"};
        String target = "0009";
        OpenLock openLock = new OpenLock();
        System.out.println(openLock.openLock(deadends, target));
    }
}
