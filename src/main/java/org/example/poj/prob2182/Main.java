package org.example.poj.prob2182;

import java.util.Scanner;

public class Main {
    public static final int maxn = 8005;
    public static int n;
    public static int f[] = new int[maxn];
    public static int ar[] = new int[maxn];
    public static int ans[] = new int[maxn];

    public static int lowb(int t) {
        return t &(-t);
    }

    public static void add(int i, int v)
    {
        for (; i < maxn; ar[i] += v, i += lowb(i));
    }

    public static int sum(int i) {
        int s =0;
        for (; i >0; s += ar[i], i -= lowb(i));
        return s;
    }

    public static int calspace(int index) {
        return index - sum(index);
    }

    public static int binarysearch(int a) {
        int l =1;
        int r = n;
        int mid;
        while (l < r) {
            mid = (l + r) /2;
            int temp = calspace(mid);
            if (temp < a) {
                l = mid +1;
            }

            else {
                r = mid;
            }

        }
        return l;
    }

    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        n = scanner.nextInt();
        f[0] =0;
        for (int i =1; i < n; i++) {
            f[i] = scanner.nextInt();
        }
        for (int i = n -1; i >=0; i--) {
            int a = binarysearch(f[i] +1);
            ans[i] = a;
            add(a, 1);
        }
        for (int i =0; i < n; i++) {
            System.out.println(ans[i]);
        }
    }

}
