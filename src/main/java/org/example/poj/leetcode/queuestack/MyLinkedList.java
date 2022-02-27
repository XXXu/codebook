package org.example.poj.leetcode.queuestack;

public class MyLinkedList {
    private class Node{
        int value;
        Node next;
    }

    private int count = 0;
    private Node head;
    private Node tail;

    public MyLinkedList() {

    }

    public int get(int index) {
        if (index >= count) {
            return -1;
        }
        Node node = head;
        for (int i = 0; i < index; i++) {
            node = node.next;
        }
        return node.value;
    }

    public void addAtHead(int val) {
        Node node = new Node();
        node.value = val;
        if (head == null) {
            head = node;
            tail = node;
        } else {
            Node old = head;
            node.next = old;
            head = node;
        }
        count++;
    }

    public void addAtTail(int val) {
        Node node = new Node();
        node.value = val;
        if (tail == null) {
            tail = node;
            head = node;
        } else {
            Node old = tail;
            old.next = node;
            tail = node;
        }
        count++;
    }
    public void addAtIndex(int index, int val) {
        if (index == count) {
            addAtTail(val);
        } else if (index > count) {

        } else if (index < 0) {
            addAtHead(val);
        } else {
            Node node = new Node();
            node.value = val;

            Node temp = head;
            for (int i = 0; i < index - 1; i++) {
                temp = temp.next;
            }
            Node tempbe = temp;
            Node tempaf = temp.next;
            node.next = tempaf;
            tempbe.next = node;
            count++;
        }
    }

    public void deleteAtIndex(int index) {
        if (0 <= index && index < count) {
            if (index == 0) {
                head = head.next;
            } else {
                Node x = head;
                for (int i = 0; i < index - 1; i++) {
                    x = x.next;
                }
                if (x.next != null) {
                    Node af = x.next.next;
                    x.next = af;
                } else {
                    x.next = null;
                }
            }
            count--;
        }
    }

    public void print() {
        Node x = head;
        while (x != null) {
            System.out.print(x.value + " ");
            x = x.next;
        }
    }

    public static void main(String[] args) {
        MyLinkedList linkedList = new MyLinkedList();
        linkedList.addAtHead(1);
        linkedList.addAtTail(3);
        System.out.println("count: " + linkedList.count);

        linkedList.addAtIndex(1,2);   //链表变为1-> 2-> 3

        System.out.println(linkedList.get(1));
        linkedList.deleteAtIndex(0);
        System.out.println("=========");
        linkedList.print();
        System.out.println("============");
        System.out.println(linkedList.get(0));
    }
}

