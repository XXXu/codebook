# JNI 实现最简单的JAVA调用C/C++代码
## 什么是JNI
JNI是Java Native Interface的简称，中文是"Java本地调用"。通过这种技术可以做到以下两点：
* Java程序中的函数可以调用Native语言写的函数，Native一般指的是C/C++编写的函数。
* Native程序中的函数可以调用Java层的函数，也就是说在C/C++程序中可以调用Java的函数。
JNI并不是什么特别神奇的东西，当初SUN推出它的目的是为了屏蔽不同操作系统平台的差异性，通过Java语言来调用Native语言的功能模块，避免重复制作车轮，最主要是这两个目的。

## 例子
1. 建立一个类，方法必须用native修饰
```
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
```
2. 将java文件编译成class文件，通过命令javac，如果需要引入额外的jar，请用javac -cp jar包
3. 生成.h头文件，用javah，请到这个类的最顶级包路径下，上述HelloWorld例子是cd 到 org 这个路径下执行javah -cp . -jni org.example.jni.HelloWorld，如果引入其他jar包的话，
那么加上jar：javah -cp xxx.jar:./ -jni org.example.jni.HelloWorld
4. 编写c++文件,HelloWorld.cpp
```
#include <iostream>
#include "org_example_jni_HelloWorld.h"

JNIEXPORT void JNICALL Java_org_example_jni_HelloWorld_greeting
  (JNIEnv *, jobject) 
{
    std::cout << "hello" << std::endl;
}
```
5. 生成so文件：g++ -fPIC -I /usr/lib/jvm/jdk1.8.0_291/include -I /usr/lib/jvm/jdk1.8.0_291/include/linux -shared -o libHelloWorld.so HelloWorld.cpp
6. 将libHelloWorld.so放到/usr/lib目录下，此路径为默认的运行时库路径，当然也可以通过将动态库路径直接添加到LD_LIBRARY_PATH路径下，我放到/usr/lib下仅仅是为了省事。
7. 运行HelloWorld的main方法

