package org.example.jni;

public class HelloWorld {
    static {
        System.loadLibrary("HelloWorld");
    }

    private native void greeting();

    public static void main(String[] args) {
        new HelloWorld().greeting();
    }

}
