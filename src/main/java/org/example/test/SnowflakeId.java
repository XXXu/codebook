package org.example.test;

public class SnowflakeId {
    /**
     * 起始的时间戳 2020-01-01 01:01:01
     */
    private final static long START_STMP = 1577811661000L;

    /**
     * 每一部分占用的位数
     */
    private final static long MACHINE_BIT = 5;   //机器标识占用的位数
    private final static long DATACENTER_BIT = 5;//数据中心占用的位数
    private final static long SEQUENCE_BIT = 12; //序列号占用的位数

    /**
     * 每一部分的最大值
     */
    private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT; //12
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT; // 12 + 5 = 17
    private final static long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT; // 17 + 5 = 22

    private final long datacenterId;  //数据中心
    private final long machineId;     //机器标识
    private long sequence = 0L; //序列号
    private long lastStmp = -1L;//上一次时间戳

    public SnowflakeId(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("数据中心的id不能小于0或者大于最大值" + MAX_DATACENTER_NUM);
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("机器标识的id不能小于0或者大于最大值" + MAX_MACHINE_NUM);
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     *
     * @return id
     */
    public synchronized long nextId() {
        long currStmp = System.currentTimeMillis();
        if (currStmp < lastStmp) {
            throw new RuntimeException("时间改变，不能再次生成id");
        }

        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStmp = currStmp;

        return (currStmp - START_STMP) << TIMESTMP_LEFT //时间戳部分
                | datacenterId << DATACENTER_LEFT       //数据中心部分
                | machineId << MACHINE_LEFT             //机器标识部分
                | sequence;                             //序列号部分
    }

    /**
     * 获取下一毫秒
     *
     * @return
     */
    private long getNextMill() {
        long mill = System.currentTimeMillis();
        while (mill <= lastStmp) {
            mill = System.currentTimeMillis();
        }
        return mill;
    }

    public static void main(String[] args) {
        /*SnowflakeId snowflakeId = new SnowflakeId(1, 1);
        System.out.println(snowflakeId.nextId());*/
        System.out.println(~(-1L << 8));
//        System.out.println(MAX_DATACENTER_NUM);
//        System.out.println(MAX_MACHINE_NUM);
//        System.out.println(MAX_SEQUENCE);
    }
}
