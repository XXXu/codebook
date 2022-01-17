package org.example.poj.algorithm.base;

public class Node {
    String item;
    Node next;

    public static void main(String[] args) {
        Node first = new Node();
        first.item = "to";
        Node second = new Node();
        second.item = "be";
        Node three = new Node();
        three.item = "or";
        first.next = second;
        second.next = three;

        System.out.println(first.item);
        System.out.println(first.next.item);
        System.out.println(first.next.next.item);

    }
}
