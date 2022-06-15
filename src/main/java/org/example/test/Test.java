package org.example.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    static  class Directory implements Runnable{
        private static volatile AtomicInteger atomicInteger = new AtomicInteger(0);
        private static volatile int i=0;
        public void atomicI() {
            atomicInteger.addAndGet(1);
        }

        public void setI() {
            i++;
        }

        @Override
        public void run() {
//            atomicI();
            setI();
        }

        public int getAtomic() {
            return atomicInteger.get();
        }

        public int getI() {
            return i;
        }
    }

    public static void main(String[] args) throws IOException {
        /*Directory directory = new Directory();
        int count = 0;
        for (int i = 0; i < 10000; i++) {
            count++;
            Thread thread = new Thread(directory);
            thread.start();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("count is: "+count +", directory is :"+directory.getAtomic() +", i is: "+directory.getI());*/

//        String str = "18446744073709551615";
//        System.out.println("max: "+Long.MAX_VALUE);
//        System.out.println("str: "+str);
//        System.out.println(Long.valueOf(str));

        /*Configuration conf = new Configuration();
        conf.addResource(new Path("./conf/core-site.xml"));
        conf.addResource(new Path("./conf/hdfs-site.xml"));
        conf.set("dfs.ha.automatic-failover.enabled.nameservice1", "true");

        boolean aBoolean = conf.getBoolean("dfs.ha.automatic-failover.enabled", false);
        System.out.println("automatic is: " + aBoolean);*/

//        AtomicInteger atomicInteger = new AtomicInteger(100);
//        System.out.println(atomicInteger.getAndIncrement());
        /*StringBuilder builder = new StringBuilder("asd");
        StringBuilder b1 = builder;
        b1.append("ko");
        StringBuilder b2 = builder;
        b2.append("qwe");
        System.out.println("build: " + builder + ", b1: " + b1 + ", b2: " + b2);*/

        /*Map<String, Boolean> map = new HashMap();
        Boolean qwe = map.put("qwe", Boolean.TRUE);
        System.out.println(qwe);
        System.out.println(map.get("qwe"));
        Boolean absent = map.putIfAbsent("asd", Boolean.FALSE);
        System.out.println(absent);
        System.out.println(map.putIfAbsent("asd3", Boolean.FALSE));
        System.out.println(map.putIfAbsent("asd", Boolean.TRUE));*/

        

    }
}
