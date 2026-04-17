package demo.zookeeper.snowflake;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantLock;

/**
 * SnowFlake ID 生成器
 * 
 * SnowFlake ID 结构（64 位）：
 * +--------------------------------------------------------------------------+
 * | 1 Bit Unused | 41 Bit Timestamp | 10 Bit Machine ID | 12 Bit Sequence |
 * +--------------------------------------------------------------------------+
 * 
 * - 1 位符号位（固定为 0，保证 ID 为正数）
 * - 41 位时间戳（毫秒级，可以使用约 69 年）
 * - 10 位机器 ID（支持 1024 个节点）
 * - 12 位序列号（同一毫秒内可生成 4096 个 ID）
 * 
 * 时间戳起始时间：2024-01-01 00:00:00（可自定义）
 */
public class SnowFlakeIdGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(SnowFlakeIdGenerator.class);
    
    // 时间戳起始时间：2024-01-01 00:00:00
    private static final long EPOCH = 1704067200000L;
    
    // 机器 ID 占用位数
    private static final long MACHINE_ID_BITS = 10L;
    
    // 序列号占用位数
    private static final long SEQUENCE_BITS = 12L;
    
    // 机器 ID 最大值（1023）
    private static final long MAX_MACHINE_ID = (1L << MACHINE_ID_BITS) - 1;
    
    // 序列号最大值（4095）
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;
    
    // 时间戳左移位数（机器 ID + 序列号）
    private static final long TIMESTAMP_SHIFT = MACHINE_ID_BITS + SEQUENCE_BITS;
    
    // 机器 ID 左移位数（序列号）
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    
    // 机器 ID（0-1023）
    private final long machineId;
    
    // 序列号（0-4095）
    private long sequence = 0L;
    
    // 上次生成 ID 的时间戳
    private long lastTimestamp = -1L;
    
    // 锁，保证线程安全
    private final ReentrantLock lock = new ReentrantLock();
    
    /**
     * 构造函数
     * @param machineId 机器 ID（0-1023）
     */
    public SnowFlakeIdGenerator(long machineId) {
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException(
                String.format("机器 ID 必须在 0 到 %d 之间，当前值: %d", MAX_MACHINE_ID, machineId));
        }
        this.machineId = machineId;
        logger.info("SnowFlake ID 生成器初始化，机器 ID: {}", machineId);
    }
    
    /**
     * 生成下一个 ID
     * @return 生成的 ID
     */
    public long nextId() {
        lock.lock();
        try {
            long timestamp = currentTimestamp();
            
            // 如果当前时间小于上次时间，说明时钟回拨
            if (timestamp < lastTimestamp) {
                throw new RuntimeException(
                    String.format("时钟回拨，拒绝生成 ID。上次时间戳: %d, 当前时间戳: %d", 
                                lastTimestamp, timestamp));
            }
            
            // 如果是同一毫秒内
            if (timestamp == lastTimestamp) {
                // 序列号自增
                sequence = (sequence + 1) & MAX_SEQUENCE;
                
                // 如果序列号溢出（同一毫秒内生成超过 4096 个 ID）
                if (sequence == 0) {
                    // 等待下一毫秒
                    timestamp = waitNextMillis(lastTimestamp);
                }
            } else {
                // 新的毫秒，序列号重置为 0
                sequence = 0L;
            }
            
            lastTimestamp = timestamp;
            
            // 组装 ID
            return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                 | (machineId << MACHINE_ID_SHIFT)
                 | sequence;
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * 获取当前时间戳（毫秒）
     */
    private long currentTimestamp() {
        return System.currentTimeMillis();
    }
    
    /**
     * 等待下一毫秒
     * @param lastTimestamp 上次时间戳
     * @return 新的时间戳
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = currentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTimestamp();
        }
        return timestamp;
    }
    
    /**
     * 解析 ID 的各个组成部分
     * @param id ID
     * @return ID 组成部分
     */
    public static IdParts parseId(long id) {
        // 提取序列号（低 12 位）
        long sequence = id & MAX_SEQUENCE;
        
        // 提取机器 ID（中间 10 位）
        long machineId = (id >> MACHINE_ID_SHIFT) & MAX_MACHINE_ID;
        
        // 提取时间戳（高 41 位）
        long timestamp = (id >> TIMESTAMP_SHIFT) + EPOCH;
        
        return new IdParts(timestamp, machineId, sequence);
    }
    
    /**
     * ID 组成部分
     */
    public static class IdParts {
        private final long timestamp;
        private final long machineId;
        private final long sequence;
        
        public IdParts(long timestamp, long machineId, long sequence) {
            this.timestamp = timestamp;
            this.machineId = machineId;
            this.sequence = sequence;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public long getMachineId() {
            return machineId;
        }
        
        public long getSequence() {
            return sequence;
        }
        
        @Override
        public String toString() {
            return String.format("IdParts{timestamp=%d, machineId=%d, sequence=%d}", 
                               timestamp, machineId, sequence);
        }
    }
    
    /**
     * 获取机器 ID
     */
    public long getMachineId() {
        return machineId;
    }
}
