package demo.http;

import java.io.IOException;

/**
 * HTTP客户端接口
 */
public interface HttpClient {
    
    /**
     * 发送GET请求
     * @param url 请求URL
     * @return 响应内容
     * @throws IOException IO异常
     */
    String get(String url) throws IOException;
    
    /**
     * 关闭客户端，释放资源
     */
    void close() throws IOException;
}
