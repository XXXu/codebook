package org.example.poj.algorithm.sort;

public class MaxPQ {
    private int[] pq;
    private int n = 0;

    public MaxPQ(int max) {
        pq = new int[max + 1];
    }

    public boolean isEmpty() {
        return n == 0;
    }

    public int size() {
        return n;
    }

    public void insert(int v) {
        pq[++n] = v;
        swim(n);
    }

    public int delMax() {
        int del = pq[1];
        exch(1, n--);
        pq[n + 1] = 0;
        sink(1);
        return del;
    }

    private void sink(int k) {
        while (2 * k < (pq.length - 1)) {
            int i = 2 * k;
            if (less(i, i + 1)) {
                i++;
            }
            if (less(i, k)) {
                break;
            }
            exch(k, i);
            k = i;
        }
    }

    private void swim(int k) {
        while ((k / 2) >= 1 && less(k / 2, k)) {
            exch(k / 2, k);
            k = k / 2;
        }
    }

    private boolean less(int i, int j) {
        return pq[i] < pq[j];
    }

    private static void show(int[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i]+"\t");
        }
        System.out.println();
    }


    private void exch(int i, int j) {
        int temp = pq[i];
        pq[i] = pq[j];
        pq[j] = temp;
    }

    public static void main(String[] args) {
        MaxPQ maxPQ = new MaxPQ(7);
        maxPQ.insert(5);
        maxPQ.insert(6);
        maxPQ.insert(7);
        maxPQ.insert(8);
        maxPQ.insert(9);
        System.out.println(maxPQ.delMax());
        maxPQ.insert(10);
        maxPQ.insert(11);
        maxPQ.insert(12);
        show(maxPQ.pq);
    }
}
