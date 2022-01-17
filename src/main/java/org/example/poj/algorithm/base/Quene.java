package org.example.poj.algorithm.base;

public class Quene<T> {
    private Node first;
    private Node last;
    private int n;
    private class Node {
        T item;
        Node next;
    }

    public boolean isEmpty() {
        return n == 0;
    }

    public int size() {
        return n;
    }

    public void enqueue(T item) {
        Node oldlast = last;
        last = new Node();
        last.item = item;
        last.next = null;
        if (isEmpty()) {
            first = last;
        } else {
            oldlast.next = last;
        }
        n++;
    }

    public T dequeue() {
        T item = first.item;
        first = first.next;
        if (isEmpty()) {
            last = null;
        }
        n--;
        return item;
    }

    public static void main(String[] args) {
        Quene<String> quene = new Quene<String>();
        quene.enqueue("1");
        quene.enqueue("2");
        quene.enqueue("3");

        System.out.println(quene.size());

        System.out.println(quene.dequeue());
        System.out.println(quene.dequeue());
        System.out.println(quene.dequeue());
        System.out.println(quene.dequeue());
    }
}
