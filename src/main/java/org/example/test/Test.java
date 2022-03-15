package org.example.test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test {
    static  class Directory {
        private int parentid;
        private List<Integer> inodeid;

        public int getParentid() {
            return parentid;
        }

        public void setParentid(int parentid) {
            this.parentid = parentid;
        }

        public List<Integer> getInodeid() {
            return inodeid;
        }

        public void setInodeid(List<Integer> inodeid) {
            this.inodeid = inodeid;
        }

        @Override
        public String toString() {
            return "Directory{" +
                    "parentid=" + parentid +
                    ", inodeid=" + inodeid +
                    '}';
        }
    }

    public static void main(String[] args) throws IOException {
        /*BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("/root/workspace/transwarp/tdfs/target/debug/asdasd.txt")));
        String line = null;
        Map<Integer, Directory> map = new HashMap<>();
        while (!((line = bufferedReader.readLine()) == null)) {
            String[] split = line.split(" ");
            int key = Integer.parseInt(split[0].substring(1, split[0].length() - 1));
            int value = Integer.parseInt(split[1]);

            if (map.get(value) == null) {
                Directory directory = new Directory();
                directory.setParentid(value);
                List<Integer> integerList = new ArrayList<>();
                integerList.add(key);
                directory.setInodeid(integerList);
                map.put(value, directory);
            } else {
                Directory directory = map.get(value);
                directory.getInodeid().add(key);
            }
        }
        for (Map.Entry entry : map.entrySet()) {
            System.out.println(entry.getValue());
            System.out.println("========");
        }
        bufferedReader.close();*/
        System.out.println(2 << 14 - 1);
    }
}
