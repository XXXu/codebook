package org.example.proxy;

public class CalculatorImpl implements Calculator {
    public int add(int a, int b) {
        return a + b;
    }

    @Override
    public Calculator getCal() {
        System.out.println("getcal");
        return this;
    }
}
