package demo.nio.echo.reactor2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多线程 Reactor 模式的 Echo 服务器
 * 
 * 架构：
 * 1. Boss Reactor：一个线程，专门处理连接接受（accept）
 * 2. Worker Reactor：一个或多个线程，处理数据读写（read/write）
 * 
 * 这种模式可以充分利用多核 CPU，提高并发处理能力
 */
public class MultiThreadReactorEchoServer {
    
    private final int port;
    private final int workerThreadCount;
    private BossReactor bossReactor;
    private WorkerReactor[] workerReactors;
    private final AtomicInteger workerIndex = new AtomicInteger(0);
    
    public MultiThreadReactorEchoServer(int port, int workerThreadCount) {
        this.port = port;
        this.workerThreadCount = workerThreadCount;
    }
    
    /**
     * 启动服务器
     */
    public void start() throws IOException {
        // 创建 Worker Reactor 池
        workerReactors = new WorkerReactor[workerThreadCount];
        for (int i = 0; i < workerThreadCount; i++) {
            workerReactors[i] = new WorkerReactor("WorkerReactor-" + i);
            new Thread(workerReactors[i], "WorkerReactor-Thread-" + i).start();
        }
        
        // 创建 Boss Reactor
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);
        
        bossReactor = new BossReactor(serverChannel, this);
        new Thread(bossReactor, "BossReactor-Thread").start();
        
        System.out.println("MultiThread Reactor Echo Server started on port " + port);
        System.out.println("Boss Reactor: 1 thread");
        System.out.println("Worker Reactors: " + workerThreadCount + " threads");
    }
    
    /**
     * 获取下一个 Worker Reactor（轮询方式）
     */
    public WorkerReactor getNextWorkerReactor() {
        int index = workerIndex.getAndIncrement() % workerThreadCount;
        return workerReactors[index];
    }
    
    /**
     * 关闭服务器
     */
    public void shutdown() {
        if (bossReactor != null) {
            bossReactor.shutdown();
        }
        if (workerReactors != null) {
            for (WorkerReactor worker : workerReactors) {
                worker.shutdown();
            }
        }
    }
    
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        int workerThreads = args.length > 1 ? Integer.parseInt(args[1]) : 2;
        
        MultiThreadReactorEchoServer server = new MultiThreadReactorEchoServer(port, workerThreads);
        server.start();
        
        // 等待服务器运行
        Thread.currentThread().join();
    }
}

