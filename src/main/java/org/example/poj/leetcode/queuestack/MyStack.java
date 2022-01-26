package org.example.poj.leetcode.queuestack;

import java.util.LinkedList;
import java.util.List;

public class MyStack {
    LinkedList<Integer> queue1 = new LinkedList<>();
    public MyStack() {

    }

    public void push(int x) {
        int size = queue1.size();
        queue1.offer(x);
        for (int i = 0; i < size; i++) {
            queue1.offer(queue1.poll());
        }
    }

    public int pop() {
        return queue1.poll();
    }

    public int top() {
        return queue1.peek();
    }

    public boolean empty() {
        return queue1.isEmpty();
    }
}
