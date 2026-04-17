package demo.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * HttpURLConnection实现的HTTP客户端
 */
public class HttpUrlConnectionClient implements HttpClient {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpUrlConnectionClient.class);
    private final boolean keepAlive;
    
    /**
     * 构造函数
     * @param keepAlive 是否使用长连接
     */
    public HttpUrlConnectionClient(boolean keepAlive) {
        this.keepAlive = keepAlive;
        logger.debug("HttpUrlConnectionClient created with keepAlive={}", keepAlive);
    }
    
    @Override
    public String get(String url) throws IOException {
        URL urlObj = URI.create(url).toURL();
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
        
        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            if (keepAlive) {
                conn.setRequestProperty("Connection", "keep-alive");
            } else {
                conn.setRequestProperty("Connection", "close");
            }
            
            int responseCode = conn.getResponseCode();
            
            try (InputStream is = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } finally {
            if (!keepAlive) {
                // 短连接：显式关闭物理连接
                conn.disconnect();
            }
            // 长连接：不调用 disconnect()。因为流已经 close 了，Socket 会自动回池。
            // 当你创建一个新的 ⁠HttpURLConnection 实例时，如果之前的请求（针对同一个域名端口）已正确关闭流且没调用 ⁠disconnect()，Java 底层会自动把旧的 Socket 拿给这个新的 ⁠HttpURLConnection 实例使用。
        }
    }
    
    @Override
    public void close() throws IOException {
        // HttpURLConnection doesn't need explicit cleanup
        logger.debug("HttpUrlConnectionClient closed");
    }
}
