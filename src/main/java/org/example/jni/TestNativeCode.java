package org.example.jni;

public class TestNativeCode {
    /*static {
        System.loadLibrary("/root/jnitest/libjni_test.so");
    }*/
    public native void sayHello();

    public static void main(String[] args) {
        System.loadLibrary("jni_test");
        TestNativeCode nativeCode = new TestNativeCode();
        nativeCode.sayHello();
    }
}
