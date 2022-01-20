package org.example.poj.leetcode.queuestack;

public class MyCircularLinkQueue {
    /**
     * MyCircularQueue(k): 构造器，设置队列长度为 k 。
     * Front: 从队首获取元素。如果队列为空，返回 -1 。
     * Rear: 获取队尾元素。如果队列为空，返回 -1 。
     * enQueue(value): 向循环队列插入一个元素。如果成功插入则返回真。
     * deQueue(): 从循环队列中删除一个元素。如果成功删除则返回真。
     * isEmpty(): 检查循环队列是否为空。
     * isFull(): 检查循环队列是否已满。
     */
    private class Node {
        Integer item;
        Node next;
    }

    private Node head;
    private Node tail;
    private int queueMaxSize = 0;
    private int size = 0;

    public MyCircularLinkQueue(int k) {
        queueMaxSize = k;
    }

    public int Front() {
        if (isEmpty()) {
            return -1;
        }
        return head.item;
    }

    public int Rear() {
        if (isEmpty()) {
            return -1;
        }
        return tail.item;
    }

    public boolean enQueue(int val) {
        if (isFull()) {
            return false;
        }
        Node oldtail = tail;
        tail = new Node();
        tail.item = val;
        tail.next = null;
        if (isEmpty()) {
            head = tail;
        } else {
            oldtail.next = tail;
        }
        size++;
        return true;
    }

    public boolean deQueue() {
        if (isEmpty()) {
            return false;
        }
        head = head.next;
        size--;
        return true;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == queueMaxSize;
    }

    public static void main(String[] args) {
        MyCircularLinkQueue circularQueue = new MyCircularLinkQueue(3); // 设置长度为 3
        // 返回 true
        System.out.println(circularQueue.enQueue(1));

        // 返回 true
        System.out.println(circularQueue.enQueue(2));

        // 返回 true
        System.out.println(circularQueue.enQueue(3));

        // 返回 false，队列已满
        System.out.println(circularQueue.enQueue(4));

        // 返回 3
        System.out.println(circularQueue.Rear());

        // 返回 true
        System.out.println(circularQueue.isFull());

        // 返回 true
        System.out.println(circularQueue.deQueue());

        // 返回 true
        System.out.println(circularQueue.enQueue(4));
        // 返回 4
        System.out.println(circularQueue.Rear());

    }

}
