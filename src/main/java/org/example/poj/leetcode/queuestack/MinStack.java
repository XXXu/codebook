package org.example.poj.leetcode.queuestack;

public class MinStack {
    private Node head;

    private class Node {
        Integer item;
        Integer min = 0;
        Node next;
    }

    public MinStack() {

    }

    public void push(int val) {
        Node oldhead = head;
        head = new Node();
        head.item = val;
        head.next = oldhead;
        if (oldhead == null) {
            head.min = val;
        } else {
            if (oldhead.min > val) {
                head.min = val;
            } else {
                head.min = oldhead.min;
            }
        }
    }

    public void pop() {
        head = head.next;
    }

    public int top() {
        return head.item;
    }
    public int getMin() {
        return head.min;
    }

    public static void main(String[] args) {
        MinStack minStack = new MinStack();
        minStack.push(-2);
        minStack.push(0);
        minStack.push(-3);
        System.out.println(minStack.getMin());
        minStack.pop();
        System.out.println(minStack.top());
        System.out.println(minStack.getMin());

    }
}
