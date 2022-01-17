package org.example.poj.algorithm.base;

public class Deque {
    private class Node {
        String item;
        Node next;
        Node pre;
    }

    private int n;
    private Node first;
    private Node last;

    public boolean isEmpty() {
        return n == 0;
    }

    public int size() {
        return n;
    }

    public void pushLeft(String item) {
        Node oldfirst = first;
        first = new Node();
        first.item = item;
        first.pre = null;
        first.next = oldfirst;
        if (isEmpty()) {
            last = first ;
        } else {
            oldfirst.pre = first;
        }
        n++;
    }

    public void pushRight(String item) {
        Node oldlast = last;
        last = new Node();
        last.item = item;
        last.next = null;
        last.pre = oldlast;
        if (isEmpty()) {
            first = last;
        } else {
            oldlast.next = last;
        }
        n++;
    }

    public String popLeft() {
        Node oldfirst = first;
        first = first.next;
        first.pre = null;
        if (isEmpty()) {
            last = null;
        }
        oldfirst.next = null;
        oldfirst.pre = null;
        n--;
        return oldfirst.item;
    }

    public String popRight() {
        Node oldlast = last;
        last = last.pre;
        last.next = null;
        if (isEmpty()) {
            first = null;
        }
        oldlast.next = null;
        oldlast.pre = null;
        n--;
        return oldlast.item;
    }

    public void foreachLeft() {
        for (Node x = first; x != null; x = x.next) {
            System.out.println(x.item);
        }
    }

    public void foreachRight() {
        for (Node x = last; x != null; x = x.pre) {
            System.out.println(x.item);
        }
    }

    public static void main(String[] args) {
        Deque deque = new Deque();
        deque.pushLeft("1");
        deque.pushLeft("2");
        deque.pushLeft("a");
        deque.pushLeft("3");
        deque.pushRight("4");
        deque.pushRight("5");
        deque.pushRight("6");
        deque.foreachLeft();
        System.out.println("==========");
        deque.foreachRight();

        deque.popLeft();
        deque.popRight();
        System.out.println("--------");

        deque.foreachLeft();
        System.out.println("*******");
        deque.foreachRight();
    }



}
