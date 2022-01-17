package org.example.btrace;

import java.util.Random;

public class BtraceDemo {
    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        Random random = new Random();
        while (true) {
            System.out.println(calculator.add(random.nextInt(10), random.nextInt(10)));
        }
    }
}
