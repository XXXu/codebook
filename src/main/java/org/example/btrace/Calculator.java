package org.example.btrace;

public class Calculator {
    private int c = 1;

    public int add(int a, int b) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return a + b;
    }
}
