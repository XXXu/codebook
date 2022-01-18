package org.example.test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class JstackCase {
    public static Executor executor = Executors.newFixedThreadPool(5);
    public static Object lock = new Object();
    public static void main(String[] args) {
        boolean close = true;
        int i = 1;
        do {
            i++;
            System.out.println("as"+i);
        } while (false);



        /*Task task1 = new Task();
        Task task2 = new Task();
        executor.execute(task1);
        executor.execute(task2);*/

    }
    static class Task implements Runnable {
        @Override
        public void run() {
            synchronized (lock) {
                try {
                    lock.wait();
                    //TimeUnit.SECONDS.sleep(100000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /*static class Task implements Runnable {

        @Override
        public void run() {
            synchronized (lock) {
                calculate();
            }
        }

        public void calculate() {
            long i = 0;
            while (true) {
                i++;
            }
        }
    }*/
}

