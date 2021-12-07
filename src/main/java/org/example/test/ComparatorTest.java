package org.example.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ComparatorTest {
    public static void main(String[] args) {
        List<String> list = Arrays.asList("nihaoujk", "hello", "world", "welcome");
        // 按照字母升序排序
//        Collections.sort(list);
        System.out.println(list);
        // 按照字符串的长度升序排序
        Collections.sort(list, (item1, item2) -> item1.length() - item2.length());
        System.out.println(list);
        // comparingInt 接收一个 ToIntFunction， 返回一个比较器（c1,c3） -> c1-c2
        // ToIntFunction 接收一个泛型，返回int类型
        Collections.sort(list, Comparator.comparingInt(String::length).reversed());
        System.out.println(list);

        // 按照字符串的长度降序排序
//        list.sort((item1, item2) -> item2.length() - item1.length());
//        list.sort(Comparator.comparingInt(String::length).reversed());
        // 下面例子没加参数类型时报错，加上 String 后不报错。
        // 原因 comparingInt 跟 list.sort 排序时的类型是不可以推断的
        // comparingInt 只是个工厂创建comparator， reversed 方法使类型推断出问题
//        list.sort(Comparator.comparingInt((String item) -> item.length()).reversed());

        // 按照字符串长度排序，再按字符串的字母排序
        list.sort(Comparator.comparingInt(String::length).thenComparing(String.CASE_INSENSITIVE_ORDER));
        System.out.println(list);
//        list.sort(Comparator
//                .comparingInt(String::length)
//                .thenComparing((item1 , item2) -> item1.toLowerCase().compareTo(item2.toLowerCase())));
//        list.sort(Comparator
//                .comparingInt(String::length)
//                .thenComparing(Comparator.comparing(String::toLowerCase)));

//        list.sort(Comparator.comparingInt(String::length)
//                .thenComparing(Comparator.comparing(String::toLowerCase, Comparator.reverseOrder())));
//        list.sort(Comparator.comparingInt(String::length)
//                .thenComparing(String::toLowerCase, Comparator.reverseOrder()));
//        System.out.println(list);
//        list.sort(Comparator.comparingInt(String::length).reversed()
//                .thenComparing(Comparator.comparing(String::toLowerCase, Comparator.reverseOrder())));
//
//        System.out.println(list);
    }
}
