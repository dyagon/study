package demo.zookeeper.snowflake;

import demo.zookeeper.cluster.ClusterNodeInfo;
import demo.zookeeper.cluster.ClusterNodeNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于集群节点命名服务的 SnowFlake ID 生成器
 * 
 * 使用 ZooKeeper 节点命名服务为每个节点分配唯一的机器 ID，
 * 然后使用该机器 ID 生成 SnowFlake ID。
 * 
 * 优势：
 * 1. 自动分配机器 ID，无需手动配置
 * 2. 节点下线后机器 ID 可以被回收（通过序号）
 * 3. 支持动态扩缩容
 */
public class ClusterSnowFlakeIdGenerator implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(ClusterSnowFlakeIdGenerator.class);
    
    private final ClusterNodeNaming nodeNaming;
    private SnowFlakeIdGenerator idGenerator;
    private ClusterNodeInfo currentNodeInfo;
    
    /**
     * 构造函数
     * @param nodeNaming 集群节点命名服务（必须已加入集群）
     */
    public ClusterSnowFlakeIdGenerator(ClusterNodeNaming nodeNaming) {
        this.nodeNaming = nodeNaming;
        initialize();
    }
    
    /**
     * 初始化 ID 生成器
     */
    private void initialize() {
        currentNodeInfo = nodeNaming.getCurrentNode();
        if (currentNodeInfo == null) {
            throw new IllegalStateException("节点尚未加入集群，请先调用 joinCluster()");
        }
        
        int sequence = currentNodeInfo.getSequence();
        if (sequence < 0) {
            throw new IllegalStateException("无法获取节点序号");
        }
        
        // 使用节点序号作为机器 ID（限制在 0-1023 范围内）
        long machineId = sequence & 0x3FF; // 限制在 10 位（0-1023）
        
        // 创建 SnowFlake ID 生成器
        this.idGenerator = new SnowFlakeIdGenerator(machineId);
        
        logger.info("集群 SnowFlake ID 生成器初始化成功，节点序号: {}, 机器 ID: {}", 
                   sequence, machineId);
    }
    
    /**
     * 生成下一个 ID
     * @return 生成的 ID
     */
    public long nextId() {
        if (idGenerator == null) {
            throw new IllegalStateException("ID 生成器尚未初始化");
        }
        return idGenerator.nextId();
    }
    
    /**
     * 解析 ID
     * @param id ID
     * @return ID 组成部分
     */
    public SnowFlakeIdGenerator.IdParts parseId(long id) {
        return SnowFlakeIdGenerator.parseId(id);
    }
    
    /**
     * 获取当前节点信息
     */
    public ClusterNodeInfo getCurrentNodeInfo() {
        return currentNodeInfo;
    }
    
    /**
     * 获取机器 ID
     */
    public long getMachineId() {
        if (idGenerator == null) {
            throw new IllegalStateException("ID 生成器尚未初始化");
        }
        return idGenerator.getMachineId();
    }
    
    @Override
    public void close() {
        // 节点命名服务会在外部关闭
        logger.info("集群 SnowFlake ID 生成器已关闭");
    }
}
