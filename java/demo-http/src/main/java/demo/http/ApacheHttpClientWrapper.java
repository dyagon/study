package demo.http;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Apache HttpClient实现的HTTP客户端
 */
public class ApacheHttpClientWrapper implements HttpClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ApacheHttpClientWrapper.class);
    private final CloseableHttpClient httpClient;
    private final boolean keepAlive;
    
    /**
     * 构造函数
     * @param keepAlive 是否使用长连接
     */
    public ApacheHttpClientWrapper(boolean keepAlive) {
        this.keepAlive = keepAlive;
        
        if (keepAlive) {
            // 配置连接池，支持长连接
            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
            connectionManager.setMaxTotal(100);
            connectionManager.setDefaultMaxPerRoute(20);
            
            ConnectionConfig connectionConfig = ConnectionConfig.custom()
                    .setConnectTimeout(Timeout.ofSeconds(5))
                    .setSocketTimeout(Timeout.ofSeconds(5))
                    .setTimeToLive(TimeValue.ofMinutes(1))
                    .build();
            connectionManager.setDefaultConnectionConfig(connectionConfig);
            
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(Timeout.ofSeconds(5))
                    .build();
            
            this.httpClient = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(requestConfig)
                    .build();
            
            logger.debug("ApacheHttpClientWrapper created with connection pooling (keepAlive=true)");
        } else {
            // 短连接，每次创建新的client（或在请求时设置Connection: close）
            this.httpClient = HttpClients.createDefault();
            logger.debug("ApacheHttpClientWrapper created without connection pooling (keepAlive=false)");
        }
    }
    
    @Override
    public String get(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        
        if (!keepAlive) {
            request.setHeader("Connection", "close");
        }
        
        return httpClient.execute(request, response -> {
            String content = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            EntityUtils.consume(response.getEntity());
            return content;
        });
    }
    
    @Override
    public void close() throws IOException {
        if (httpClient != null) {
            httpClient.close();
            logger.debug("ApacheHttpClientWrapper closed");
        }
    }
}
