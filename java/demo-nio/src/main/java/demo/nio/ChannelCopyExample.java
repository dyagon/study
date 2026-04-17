package demo.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ChannelCopyExample {


    static void copyFile(String sourcePath, String destPath) {

        // 使用 try-with-resources 自动关闭资源
        try (FileInputStream fis = new FileInputStream(sourcePath);
                FileOutputStream fos = new FileOutputStream(destPath);
                FileChannel sourceChannel = fis.getChannel();
                FileChannel destChannel = fos.getChannel()) {

            // 1. 创建一个缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10240);

            long startTime = System.currentTimeMillis();
            System.out.println("Start time: " + startTime);

            // 2. 循环从源通道读取数据到缓冲区
            while (sourceChannel.read(buffer) != -1) {
                // 3. 翻转缓冲区，从写模式切换到读模式
                buffer.flip();

                // 4. 将缓冲区的数据写入到目标通道
                // 确保缓冲区的数据被完全写入
                while (buffer.hasRemaining()) {
                    destChannel.write(buffer);
                }

                // 5. 清空缓冲区，准备下一次读取
                buffer.clear();
            }

            destChannel.force(true);
            long endTime = System.currentTimeMillis();
            System.out.println("End time: " + endTime);
            System.out.println("Time taken: " + (endTime - startTime) + " milliseconds");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void copyFile2(String sourcePath, String destPath) {


        // 使用 try-with-resources 自动关闭资源
        try (FileInputStream fis = new FileInputStream(sourcePath);
                FileOutputStream fos = new FileOutputStream(destPath);
                FileChannel sourceChannel = fis.getChannel();
                FileChannel destChannel = fos.getChannel()) {

            System.out.println("Source channel size: " + sourceChannel.size());
            long startTime = System.currentTimeMillis();

            sourceChannel.transferTo(0, sourceChannel.size(), destChannel);
            long endTime = System.currentTimeMillis();
            System.out.println("End time: " + endTime);
            System.out.println("Time taken: " + (endTime - startTime) + " milliseconds");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String sourcePath =
                "/Users/mac-DYANG20/Workspace/offer/offer-pim-data/dev/data/base/16384/164112.2";
        String destPath = "test";

        copyFile(sourcePath, destPath);
        // copyFile2(sourcePath, destPath);


    }
}
