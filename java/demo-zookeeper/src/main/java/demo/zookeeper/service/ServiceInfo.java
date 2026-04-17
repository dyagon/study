package demo.zookeeper.service;

import java.util.Objects;

/**
 * 服务信息类
 * 包含服务的基本信息：名称、主机地址、端口等
 */
public class ServiceInfo {
    
    private final String serviceName;
    private final String host;
    private final int port;
    private final String serviceId;
    
    /**
     * 构造函数
     * @param serviceName 服务名称
     * @param host 服务主机地址
     * @param port 服务端口
     */
    public ServiceInfo(String serviceName, String host, int port) {
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.serviceId = serviceName + "-" + host + ":" + port;
    }
    
    /**
     * 从字符串解析服务信息
     * 格式：host:port 或 serviceName:host:port
     */
    public static ServiceInfo fromString(String serviceName, String data) {
        String[] parts = data.split(":");
        if (parts.length == 2) {
            return new ServiceInfo(serviceName, parts[0], Integer.parseInt(parts[1]));
        } else if (parts.length == 3) {
            return new ServiceInfo(parts[0], parts[1], Integer.parseInt(parts[2]));
        } else {
            throw new IllegalArgumentException("Invalid service data format: " + data);
        }
    }
    
    /**
     * 转换为字符串（用于存储到 ZooKeeper）
     * 格式：host:port
     */
    public String toDataString() {
        return host + ":" + port;
    }
    
    /**
     * 获取服务地址（host:port）
     */
    public String getAddress() {
        return host + ":" + port;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getHost() {
        return host;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getServiceId() {
        return serviceId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceInfo that = (ServiceInfo) o;
        return port == that.port &&
               Objects.equals(serviceName, that.serviceName) &&
               Objects.equals(host, that.host);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(serviceName, host, port);
    }
    
    @Override
    public String toString() {
        return "ServiceInfo{" +
               "serviceName='" + serviceName + '\'' +
               ", host='" + host + '\'' +
               ", port=" + port +
               ", serviceId='" + serviceId + '\'' +
               '}';
    }
}
