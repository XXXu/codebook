package org.example.btrace;

import org.openjdk.btrace.core.annotations.*;

import static org.openjdk.btrace.core.BTraceUtils.*;

@BTrace
public class BTraceTest {
    private static long count;
    @OnMethod(
            clazz = "org.example.transwarp.btrace.Calculator",
            method = "add",
            location = @Location(Kind.RETURN)
    )
    public static void trace1(int a, int b, @Return int sum) {
        println("trace1:a=" + a + ",b=" + b + ",sum=" + sum);
    }

    @OnMethod(
            clazz = "org.example.transwarp.btrace.Calculator",
            method = "add",
            location = @Location(Kind.RETURN)
    )
    public static void trace2(@Duration long duration) {
        println(strcat("duration(nanos): ", str(duration)));
        println(strcat("duration(s): ", str(duration / 1000000000)));
    }

    //@OnMethod(
      //      clazz = "org.example.transwarp.btrace.Calculator",
        //    method = "add",
          //  location = @Location(value = Kind.CALL, clazz = "/.*/", method = "sleep")
    //)
    /*public static void trace3(@ProbeClassName String pcm, @ProbeMethodName String pmn,
                              @TargetInstance Object instance, @TargetMethodOrField String method) {
        println(strcat("ProbeClassName: ", pcm));
        println(strcat("ProbeMethodName: ", pmn));
        println(strcat("TargetInstance: ", str(instance)));
        println(strcat("TargetMethodOrField : ", str(method)));
        println(strcat("count: ", str(++count)));
    }*/

    @OnMethod(
            clazz = "org.example.transwarp.btrace.Calculator",
            method = "add",
            location = @Location(Kind.RETURN)
    )
    public static void trace5(@Self Object calculator) {
        println(get(field("org.example.transwarp.btrace.Calculator", "c"), calculator));
    }

}
