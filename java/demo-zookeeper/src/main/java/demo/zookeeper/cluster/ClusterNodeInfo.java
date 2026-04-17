package demo.zookeeper.cluster;

import java.util.Objects;

/**
 * 集群节点信息类
 * 包含节点的 ID、序号、路径等信息
 */
public class ClusterNodeInfo {
    
    private final String nodeId;
    private final int sequence;
    private final String path;
    private final String data;
    
    /**
     * 构造函数
     * @param nodeId 节点 ID（从路径中提取）
     * @param sequence 节点序号（从路径中提取）
     * @param path 节点完整路径
     * @param data 节点数据
     */
    public ClusterNodeInfo(String nodeId, int sequence, String path, String data) {
        this.nodeId = nodeId;
        this.sequence = sequence;
        this.path = path;
        this.data = data;
    }
    
    /**
     * 从 ZooKeeper 路径解析节点信息
     * 路径格式：/cluster/nodes/node-{sequence}
     * @param path 节点路径
     * @param data 节点数据
     * @return 节点信息
     */
    public static ClusterNodeInfo fromPath(String path, String data) {
        // 提取节点名称（路径的最后一部分）
        String nodeName = path.substring(path.lastIndexOf('/') + 1);
        
        // 提取序号（假设格式为 node-{sequence}）
        int sequence = -1;
        String nodeId = nodeName;
        
        if (nodeName.contains("-")) {
            try {
                String[] parts = nodeName.split("-");
                if (parts.length > 1) {
                    sequence = Integer.parseInt(parts[parts.length - 1]);
                    // 重新组合 nodeId（去掉序号部分）
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        if (i > 0) sb.append("-");
                        sb.append(parts[i]);
                    }
                    nodeId = sb.toString();
                }
            } catch (NumberFormatException e) {
                // 如果解析失败，使用原始名称
            }
        }
        
        return new ClusterNodeInfo(nodeId, sequence, path, data);
    }
    
    public String getNodeId() {
        return nodeId;
    }
    
    public int getSequence() {
        return sequence;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getData() {
        return data;
    }
    
    /**
     * 获取节点的显示名称（包含序号）
     */
    public String getDisplayName() {
        if (sequence >= 0) {
            return nodeId + "-" + sequence;
        }
        return nodeId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusterNodeInfo that = (ClusterNodeInfo) o;
        return sequence == that.sequence &&
               Objects.equals(path, that.path);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sequence, path);
    }
    
    @Override
    public String toString() {
        return "ClusterNodeInfo{" +
               "nodeId='" + nodeId + '\'' +
               ", sequence=" + sequence +
               ", path='" + path + '\'' +
               ", data='" + data + '\'' +
               '}';
    }
}
