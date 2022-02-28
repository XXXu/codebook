package org.example.thread;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTest {

    public static void main(String[] args) {
        /*Object object = new Object();
        Thread2 thread2 = new Thread2(object);
        thread2.setName("thread2");

        Thread1 thread1 = new Thread1(object);
        thread1.setName("thread1");
        thread1.start();
        thread2.start();*/
        AtomicBoolean flag = new AtomicBoolean(false);
        AtomicInteger num = new AtomicInteger(1);

        Thread threada = new ThreadA(flag, num);
        threada.setName("a");

        Thread threadb = new ThreadB(flag, num);
        threadb.setName("b");

        threada.start();
        threadb.start();

    }

    static class ThreadA extends Thread {
        private AtomicBoolean flag;
        private AtomicInteger num;

        public ThreadA(AtomicBoolean flag, AtomicInteger num) {
            this.num = num;
            this.flag = flag;
        }

        @Override
        public void run() {
            while (num.get() < 100) {
                if (!flag.get()) {
                    System.out.println(Thread.currentThread().getName() + ": " + num);
                    num.incrementAndGet();
                    flag.set(true);
                }
            }

        }
    }

    static class ThreadB extends Thread {
        private AtomicBoolean flag;
        private AtomicInteger num;

        public ThreadB(AtomicBoolean flag, AtomicInteger num) {
            this.num = num;
            this.flag = flag;
        }

        @Override
        public void run() {
            while (num.get() < 100) {
                if (flag.get()) {
                    System.out.println(Thread.currentThread().getName() + ": " + num);
                    num.incrementAndGet();
                    flag.set(false);
                }
            }

        }
    }



    /*static class Thread2 extends Thread {
        private Object object;

        public Thread2(Object object) {
            this.object = object;
        }

        @Override
        public void run() {
            for (int i = 2; i < 100; i = i + 2) {
                synchronized (object) {
                    System.out.println(Thread2.currentThread().getName() +":"+ i);
                    object.notify();
                    try {
                        object.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    static class Thread1 extends Thread {
        private Object object;

        public Thread1(Object object) {
            this.object = object;
        }
        @Override
        public void run() {
            for (int i = 1; i < 99; i = i + 2) {
                synchronized (object) {
                    System.out.println(Thread1.currentThread().getName()+":"+i);
                    try {
                        object.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    object.notify();
                }
            }

        }
    }*/
}




