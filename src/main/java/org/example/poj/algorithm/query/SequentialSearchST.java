package org.example.poj.algorithm.query;

public class SequentialSearchST {
    private Node first;
    private int n;

    public int size() {
        return n;
    }

    private class Node {
        String key;
        String val;
        Node next;
        public Node(String key, String val, Node next) {
            this.key = key;
            this.val = val;
            this.next = next;
        }
    }

    public void delete(String key) {
        Node temp = null;
        for (Node x = first; x != null; x = x.next) {
            if (key.equals(x.key)) {
                if (temp != null) {
                    temp.next = x.next;
                } else {
                    first = first.next;
                }
                n--;
            } else {
                temp = x;
            }
        }
    }

    public void show() {
        for (Node x = first; x != null; x = x.next) {
            System.out.println(x.key + ":" + x.val);
        }
    }

    public String get(String key) {
        for (Node x = first; x != null; x = x.next) {
            if (key.equals(x.key)) {
                return x.val;
            }
        }
        return null;
    }

    public void put(String key, String val) {
        for (Node x = first; x != null; x = x.next) {
            if (key.equals(x.key)) {
                x.val = val;
                return;
            }
        }
        Node oldfirst = first;
        first = new Node(key, val, oldfirst);
        n++;
    }

    public static void main(String[] args) {
        SequentialSearchST sequentialSearchST = new SequentialSearchST();
        sequentialSearchST.put("1", "1");
        sequentialSearchST.put("2", "2");
        sequentialSearchST.put("3", "3");
        sequentialSearchST.put("4", "4");
        sequentialSearchST.put("5", "5");
        System.out.println(sequentialSearchST.size());
        sequentialSearchST.show();
        System.out.println(sequentialSearchST.get("2"));
        System.out.println("---------------");
        sequentialSearchST.delete("4");
        sequentialSearchST.delete("5");
        System.out.println(sequentialSearchST.size());
        System.out.println(sequentialSearchST.get("2"));
        sequentialSearchST.show();
    }
}
