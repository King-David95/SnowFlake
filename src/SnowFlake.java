/**
 * twitter的snowflake算法 -- java实现
 *
 * @author song_wei
 * @date 2019-03-07
 */
public class SnowFlake {

    /**
     * 起始的时间戳
     */
    private static final long START_STMP = 1480166465631L;

    /**
     * 每一部分占用的位数
     */
    // 序列号占用的位数
    private static final long SEQUENCE_BIT = 12;
    // 机器标识占用的位数
    private static final long MACHINE_BIT = 5;
    // 数据中心占用的位数
    private static final long DATACENTER_BIT = 5;

    /**
     * 每一部分的最大值
     */
    private static final long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);

    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private static final long MACHINE_LEFT = SEQUENCE_BIT;

    private static final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private static final long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    // 数据中心
    private long datacenterId;
    // 机器标识
    private long machineId;
    // 序列号
    private long sequence = 0L;
    // 上一次时间戳
    private long lastStmp = -1L;

    /**
     * @param datacenterId 数据中心
     * @param machineId    机器标识
     */
    public SnowFlake(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException(
                    "datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException(
                    "machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * @return 产生下一个ID
     */
    private synchronized long nextId() {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            // 相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            // 不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStmp = currStmp;

        // 时间戳部分
        return (currStmp - START_STMP) << TIMESTMP_LEFT
                // 数据中心部分
                | datacenterId << DATACENTER_LEFT
                // 机器标识部分
                | machineId << MACHINE_LEFT
                // 序列号部分
                | sequence;
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }

    public static void main(String[] args) {
        SnowFlake snowFlake = new SnowFlake(2, 3);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            System.out.println(snowFlake.nextId());
        }

        System.out.println(System.currentTimeMillis() - start);
    }
}
