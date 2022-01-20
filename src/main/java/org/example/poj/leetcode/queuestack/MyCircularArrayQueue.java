package org.example.poj.leetcode.queuestack;

public class MyCircularArrayQueue {
    /**
     * MyCircularQueue(k): 构造器，设置队列长度为 k 。
     * Front: 从队首获取元素。如果队列为空，返回 -1 。
     * Rear: 获取队尾元素。如果队列为空，返回 -1 。
     * enQueue(value): 向循环队列插入一个元素。如果成功插入则返回真。
     * deQueue(): 从循环队列中删除一个元素。如果成功删除则返回真。
     * isEmpty(): 检查循环队列是否为空。
     * isFull(): 检查循环队列是否已满。
     */
    private int[] data;
    private int head;
    private int tail;
    private int size;

    public MyCircularArrayQueue(int k) {
        data = new int[k];
        head = -1;
        tail = -1;
        size = k;
    }

    public int Front() {
        if (isEmpty()) {
            return -1;
        }
        return data[head];
    }

    public int Rear() {
        if (isEmpty()) {
            return -1;
        }
        return data[tail];
    }

    public boolean enQueue(int val) {
        if (isFull()) {
            return false;
        }
        if (isEmpty()) {
            head = 0;
        }
        tail = (tail + 1) % size;
        data[tail] = val;
        return true;
    }

    public boolean deQueue() {
        if (isEmpty()) {
            return false;
        }
        if (head == tail) {
            head = -1;
            tail = -1;
        } else {
            data[head] = 0;
            head = (head + 1) % size;
        }
        return true;
    }

    public boolean isEmpty() {
        return head == -1;
    }

    public boolean isFull() {
        return (tail + 1) % size == head;
    }

}
