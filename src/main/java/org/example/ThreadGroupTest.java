package org.example;

public class ThreadGroupTest {
    public static void main(String[] args) {
        /*ThreadGroup rootThreadGroup = new ThreadGroup("root线程组");
        Thread thread0 = new Thread(rootThreadGroup, new MRunnable(), "线程A");
        Thread thread1 = new Thread(rootThreadGroup, new MRunnable(), "线程B");
        thread0.start();
        thread1.start();*/
        /*ThreadGroup rootThreadGroup = new ThreadGroup("root线程组");
        Thread thread0 = new Thread(rootThreadGroup, new MRunnable(), "线程A");
        Thread thread1 = new Thread(rootThreadGroup, new MRunnable(), "线程B");
        thread0.start();
        thread1.start();
        ThreadGroup threadGroup1 = new ThreadGroup(rootThreadGroup, "子线程组");
        Thread thread2 = new Thread(threadGroup1, new MRunnable(), "线程C");
        Thread thread3 = new Thread(threadGroup1, new MRunnable(), "线程D");
        thread2.start();
        thread3.start();
        rootThreadGroup.interrupt();
        System.out.println("批量中断组内线程");*/
        ThreadGroup rootThreadGroup = new ThreadGroup("root线程组");
        Thread thread0 = new Thread(rootThreadGroup, new MRunnable(), "线程A");
        Thread thread1 = new Thread(rootThreadGroup, new MRunnable(), "线程B");
        thread0.start();
        thread1.start();
        ThreadGroup threadGroup1 = new ThreadGroup(rootThreadGroup, "子线程组");
        Thread thread2 = new Thread(threadGroup1, new MRunnable(), "线程C");
        Thread thread3 = new Thread(threadGroup1, new MRunnable(), "线程D");
        thread2.start();
        thread3.start();
        rootThreadGroup.interrupt();
        System.out.println("批量中断组内线程");
    }
}

class MRunnable implements Runnable {
    @Override
    public void run() {
        /*while (!Thread.currentThread().isInterrupted()) {
            System.out.println("线程名: " + Thread.currentThread().getName()
                    + ", 所在线程组: " + Thread.currentThread().getThreadGroup().getName()) ;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        /*while (!Thread.currentThread().isInterrupted()) {
            System.out.println("线程名: " + Thread.currentThread().getName()
                    + ", 所在线程组: " + Thread.currentThread().getThreadGroup().getName()
                    + ", 父线程组: " + Thread.currentThread().getThreadGroup().getParent().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("线程名: " + Thread.currentThread().getName()
                    + ", 所在线程组: " + Thread.currentThread().getThreadGroup().getName()
                    + ", 父线程组: " + Thread.currentThread().getThreadGroup().getParent().getName());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        System.out.println(Thread.currentThread().getName() + "执行结束");
    }
}
