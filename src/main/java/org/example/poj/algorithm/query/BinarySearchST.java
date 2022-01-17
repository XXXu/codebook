package org.example.poj.algorithm.query;

public class BinarySearchST {
    private String[] keys;
    private String[] vals;
    private int n;

    public BinarySearchST(int capacity) {
        keys = new String[capacity];
        vals = new String[capacity];
    }

    public int size() {
        return n;
    }

    public boolean isEmpty() {
        return n == 0;
    }

    public String get(String key) {
        if (isEmpty()) {
            return null;
        }
        int i = rank(key);
        if (i < n && keys[i].compareTo(key) == 0) {
            return vals[i];
        } else {
            return null;
        }

    }

    public void put(String key, String val) {
        int i = rank(key);
        if (i < n && keys[i].compareTo(key) == 0) {
            vals[i] = val;
            return;
        }
        for (int j = n; j > i; j--) {
            keys[j] = keys[j - 1];
            vals[j] = vals[j - 1];
        }
        keys[i] = key;
        vals[i] = val;
        n++;
    }

    private int rank(String key) {
        int lo = 0;
        int hi = n - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int cmp = key.compareTo(keys[mid]);
            if (cmp < 0) {
                hi = mid - 1;
            } else if (cmp > 0) {
                lo = mid + 1;
            } else {
                return mid;
            }
        }
        return lo;
    }

    public void showKeys() {
        for (int i = 0; i < n; i++) {
            System.out.print(keys[i] + "\t");
        }
        System.out.println();
    }

    public String min() {
        return keys[0];
    }

    public String max() {
        return keys[n - 1];
    }

    public String select(int k) {
        return keys[k];
    }

    public static void main(String[] args) {
        BinarySearchST searchST = new BinarySearchST(9);
        searchST.put("5", "5");
        searchST.put("3", "3");
        searchST.put("7", "7");
        searchST.put("2", "2");
        searchST.put("4", "4");
        searchST.showKeys();

        System.out.println(searchST.get("6"));
        System.out.println(searchST.get("3"));

    }
}
