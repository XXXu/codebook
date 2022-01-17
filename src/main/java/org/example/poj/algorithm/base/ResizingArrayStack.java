package org.example.poj.algorithm.base;

import java.util.Iterator;

public class ResizingArrayStack<T> implements Iterable<T>{
    private T[] a;
    private int N;

    public ResizingArrayStack(int cap) {
        a = (T[]) new Object[cap];
    }

    public void resize(int max) {
        T[] temp = (T[]) new Object[max];
        for (int i = 0; i < N; i++) {
            temp[i] = a[i];
        }
        a = temp;
    }

    public boolean isEmpty() {
        return N==0;
    }

    public int size() {
        return N;
    }

    public void push(T item) {
        if (N == a.length) {
            resize(2 * a.length);
        }
        a[N++] = item;
    }

    public T pop() {
        T item = a[--N];
        a[N] = null;
        if (N > 0 && N == a.length / 4) {
            resize(a.length / 2);
        }
        return item;
    }

    @Override
    public Iterator<T> iterator() {
        return new ReverseArrayIter();
    }

    private class ReverseArrayIter implements Iterator<T> {
        private int i = N;
        @Override
        public void remove() {
        }

        @Override
        public boolean hasNext() {
            return i > 0;
        }

        @Override
        public T next() {
            return a[--i];
        }
    }

    public static void main(String[] args) {
        ResizingArrayStack<String> stringFixedCapacityStack = new ResizingArrayStack<String>(100);
        stringFixedCapacityStack.push("asd");
        stringFixedCapacityStack.push("asd1");
        stringFixedCapacityStack.push("asd2");
        stringFixedCapacityStack.push("asd3");
        System.out.println(stringFixedCapacityStack.size());

        for (String s : stringFixedCapacityStack) {
            System.out.println(s);
        }
    }
}
