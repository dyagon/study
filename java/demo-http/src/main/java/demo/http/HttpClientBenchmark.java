package demo.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * HTTP客户端性能测试
 */
public class HttpClientBenchmark {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpClientBenchmark.class);
    // private static final String TARGET_URL = "http://www.baidu.com";
    private static final String TARGET_URL = "https://httpbin.org/get";
    private static final int REQUEST_COUNT = 10;
    
    public static void main(String[] args) {
        String separator = "=".repeat(80);
        logger.info(separator);
        logger.info("HTTP Client Benchmark - Comparing Short vs Long Connections");
        logger.info("Target URL: {}", TARGET_URL);
        logger.info("Request Count: {}", REQUEST_COUNT);
        logger.info(separator);
        
        // 1. HttpURLConnection - Short Connection
        logger.info("\n[1/4] Testing HttpURLConnection with Short Connection...");
        testClient(new HttpUrlConnectionClient(false), "HttpURLConnection (Short)");
        
        // 2. HttpURLConnection - Long Connection
        logger.info("\n[2/4] Testing HttpURLConnection with Long Connection...");
        testClient(new HttpUrlConnectionClient(true), "HttpURLConnection (Long)");
        
        // 3. Apache HttpClient - Short Connection
        logger.info("\n[3/4] Testing Apache HttpClient with Short Connection...");
        testClient(new ApacheHttpClientWrapper(false), "Apache HttpClient (Short)");
        
        // 4. Apache HttpClient - Long Connection
        logger.info("\n[4/4] Testing Apache HttpClient with Long Connection...");
        testClient(new ApacheHttpClientWrapper(true), "Apache HttpClient (Long)");
        
        logger.info("\n{}", separator);
        logger.info("Benchmark completed!");
        logger.info(separator);
    }
    
    private static void testClient(HttpClient client, String clientName) {
        try {
            // Warm up
            logger.debug("Warming up...");
            client.get(TARGET_URL);
            
            // Benchmark
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < REQUEST_COUNT; i++) {
                executeRequest(client, i);
            }
            
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            double avgTime = (double) totalTime / REQUEST_COUNT;
            String avgTimeStr = String.format("%.2f", avgTime);
            
            logger.info("✓ {} - Total: {} ms, Average: {} ms/request", 
                    clientName, totalTime, avgTimeStr);
            
        } catch (IOException e) {
            logger.error("❌ {} - Failed: {}", clientName, e.getMessage());
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.warn("Failed to close client: {}", e.getMessage());
            }
        }
    }
    
    private static void executeRequest(HttpClient client, int requestIndex) {
        try {
            String response = client.get(TARGET_URL);
            if (requestIndex == 0) {
                logger.debug("First response length: {} bytes", response.length());
            }
        } catch (IOException e) {
            logger.error("Request {} failed: {}", requestIndex, e.getMessage());
        }
    }
}
