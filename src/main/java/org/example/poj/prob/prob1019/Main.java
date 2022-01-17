package org.example.poj.prob1019;

import java.util.Scanner;

public class Main {
    public static long[] digit = new long[70000];
    public static int t,n,num;
    public static void main(String[] args) {
        Scanner scanner=new Scanner(System.in);
        t = scanner.nextInt();
        while (t > 0) {
            n = scanner.nextInt();
            solve(n);
            t--;
        }
    }

    public static long solve(int x) {
        int add=0;
        long len=0;
        digit[0]=0;
        num=1;
        while(true){
            if(num/10==0)add=1;
            if(num/10!=0&&num/100==0)add=2;
            if(num/100!=0&&num/1000==0)add=3;
            if(num/1000!=0&&num/10000==0)add=4;
            if(num/10000!=0&&num/100000==0)add=5;
            if(num/100000!=0)add=6;
            digit[num]=digit[num-1]+add;
            if(len+digit[num]<x)len+=digit[num];
            else{
                long tmp=x-len;
                int i;
                for(i=1; i<=num; i++)if(digit[i]>=tmp)break;
                int[] tt= new int[6];
                int m=i;
                for(int j=0; j<6; j++){
                    if(m>0){
                        tt[j]=m%10;
                        m/=10;
                    }
                }
                System.out.println(tt[(int) (digit[i]-tmp)]);
                break;
            }
            num++;
        }
        return 0;
    }
}
