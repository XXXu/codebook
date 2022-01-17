package org.example.poj.algorithm.base;

import java.util.Iterator;

public class Stack<T> implements Iterable<T> {
    private Node first;
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

    public void push(T item) {
        Node oldfirst = first;
        first = new Node();
        first.item = item;
        first.next = oldfirst;
        n++;
    }

    public T pop() {
        T item = first.item;
        first = first.next;
        n--;
        return item;
    }

    public void foreach() {
        for (Node x = first; x != null; x = x.next) {
            System.out.println(x.item);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new ListIter();
    }

    private class ListIter implements Iterator<T> {
        private Node cur = first;
        @Override
        public boolean hasNext() {
            return cur != null;
        }

        @Override
        public T next() {
            T item = first.item;
            first = first.next;
            return item;
        }
    }


    public static void main(String[] args) {
        Stack<String> stack = new Stack<String>();
        stack.push("a");
        stack.push("b");
        stack.push("c");
        System.out.println(stack.size());
        stack.foreach();

    }
}
