package org.example.poj.algorithm.query;

public class BST {
    private Node root;

    class Node {
        private int key;
        private String val;
        private Node left;
        private Node right;
        private int n;

        public Node(int key, String val, int n) {
            this.key = key;
            this.val = val;
            this.n = n;
        }
    }

    public String get(int key) {
        return get(root, key);
    }

    private String get(Node x, int key) {
        if (x == null) {
            return null;
        }
        int cmp = key - x.key;
        if (cmp < 0) {
            return get(x.left, key);
        } else if (cmp > 0) {
            return get(x.right, key);
        } else {
            return x.val;
        }
    }

    public void put(int key, String val) {
        root = put(root, key, val);
    }

    private Node put(Node x, int key, String val) {
        if (x == null) {
            return new Node(key, val, 1);
        }
        int cmp = key - x.key;
        if (cmp < 0) {
            x.left = put(x.left, key, val);
        } else if (cmp > 0) {
            x.right = put(x.right, key, val);
        } else {
            x.val = val;
        }
        x.n = size(x.left) + size(x.right) + 1;
        return x;
    }

    public int size() {
        return size(root);
    }

    private int size(Node x) {
        if (x == null) {
            return 0;
        }
        return x.n;
    }

    public int min() {
        return min(root).key;
    }

    private Node min(Node x) {
        if (x.left == null) {
            return x;
        }
        return min(x.left);
    }

    public int select(int k) {
        return select(root, k).key;
    }

    private Node select(Node x, int k) {
        if (x == null) {
            return null;
        }
        int t = size(x.left);
        if (t > k) {
            return select(x.left, k);
        } else if (t < k) {
            return select(x.right, k - t - 1);
        } else {
            return x;
        }
    }

    public int rank(int key) {
        return rank(root, key);
    }

    private int rank(Node x, int key) {
        if (x == null) {
            return 0;
        }
        int cmp = key - x.key;
        if (cmp < 0) {
            return rank(x.left, key);
        } else if (cmp > 0) {
            return rank(x.right, key) + size(x.left) + 1;
        } else {
            return size(x.left);
        }
    }

    public static void main(String[] args) {
        BST bst = new BST();
        bst.put(9, "9");
        System.out.println(bst.root.key);
        bst.put(5, "5");
        System.out.println(bst.root.key);
        bst.put(15, "15");
        bst.put(3, "3");
        bst.put(8, "8");
        bst.put(10, "10");
        bst.put(18, "18");
        System.out.println(bst.root.key);
    }
}
