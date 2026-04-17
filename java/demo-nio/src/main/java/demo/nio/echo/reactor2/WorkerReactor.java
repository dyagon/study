package demo.nio.echo.reactor2;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Worker Reactor：从 Reactor 线程
 * 专门负责处理数据读写（read/write）事件
 */
public class WorkerReactor implements Runnable {
    
    private final String name;
    private Selector selector;
    private volatile boolean running = true;
    private final ConcurrentLinkedQueue<Runnable> pendingTasks = new ConcurrentLinkedQueue<>();
    
    public WorkerReactor(String name) throws IOException {
        this.name = name;
        this.selector = Selector.open();
    }
    
    @Override
    public void run() {
        System.out.println(name + " started");
        
        while (running) {
            try {
                // 处理待处理的任务（注册新的通道）
                processPendingTasks();
                
                // 阻塞等待事件发生
                if (selector.select() == 0) {
                    continue;
                }
                
                // 获取所有就绪的事件
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    
                    // 分发事件：从 SelectionKey 中获取附件（Runnable），并执行
                    Runnable handler = (Runnable) key.attachment();
                    if (handler != null) {
                        handler.run();
                    }
                    
                    // 移除已处理的事件
                    iterator.remove();
                }
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            }
        }
        
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println(name + " stopped");
    }
    
    /**
     * 处理待处理的任务（用于注册新的通道）
     */
    private void processPendingTasks() {
        Runnable task;
        while ((task = pendingTasks.poll()) != null) {
            task.run();
        }
    }
    
    /**
     * 注册新的客户端通道到当前 Worker Reactor
     * 这个方法可能从其他线程调用，所以需要线程安全
     */
    public void registerChannel(SocketChannel clientChannel) {
        // 将注册任务添加到队列，由 Worker Reactor 线程执行
        pendingTasks.offer(() -> {
            try {
                clientChannel.configureBlocking(false);
                SelectionKey selectionKey = clientChannel.register(selector, SelectionKey.OP_READ);
                selectionKey.attach(new MultiThreadEchoHandler(selector, clientChannel));
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    clientChannel.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        // 唤醒 Selector，以便处理待处理的任务
        selector.wakeup();
    }
    
    public void shutdown() {
        running = false;
        if (selector != null) {
            selector.wakeup();
        }
    }
}

