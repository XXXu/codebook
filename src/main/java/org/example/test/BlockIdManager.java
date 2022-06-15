package org.example.test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BlockIdManager {
    AtomicInteger currentid;
    AtomicLong lasttime;

    public BlockIdManager() {
        this.currentid = new AtomicInteger(1);
        this.lasttime = new AtomicLong(System.currentTimeMillis());
    }

    public Long nextBlockId() {
        long curtime = System.currentTimeMillis();
        if (curtime == lasttime.get()) {
            int bid = currentid.getAndAdd(1);
            return (curtime << 8) + bid;
        } else {
            lasttime.set(curtime);
            currentid.set(2);
            return (curtime << 8) + 1;
        }
    }

    public long currentBlockId() {
        int bid = currentid.get();
        return (lasttime.get() << 8) + bid - 1;
    }

    public static void main(String[] args) {
        // 0 0000
        // 1 0001
        // 2 0010
        // 3 0011
        // 4 0100
        // 5 0101
        // 6 0110
        // 7 0111 0011 1110 8+4+2
        // 8 1000
        int a = 6;
        System.out.println(a|1);
    }
}
