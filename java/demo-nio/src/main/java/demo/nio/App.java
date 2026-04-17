package demo.nio;

import java.nio.IntBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    static Logger logger = LoggerFactory.getLogger(App.class);


    static void testIntBuffer() {
        IntBuffer buffer = IntBuffer.allocate(10);
        
        logger.debug("buffer position: {}", buffer.position());
        logger.debug("buffer limit: {}", buffer.limit());
        logger.debug("buffer capacity: {}", buffer.capacity());
        logger.debug("buffer array: {}", buffer.array());
        logger.debug("buffer array offset: {}", buffer.arrayOffset());

        buffer.put(1);
        buffer.put(2);
        buffer.put(3);
        buffer.put(4);
        buffer.put(5);

        logger.debug("=====after put=====");
        logger.debug("buffer position: {}", buffer.position());
        logger.debug("buffer limit: {}", buffer.limit());
        logger.debug("buffer capacity: {}", buffer.capacity());
        logger.debug("buffer array: {}", buffer.array());
        logger.debug("buffer array offset: {}", buffer.arrayOffset());

        logger.debug("=====after flip=====");
        buffer.flip();
        logger.debug("buffer position: {}", buffer.position());
        logger.debug("buffer limit: {}", buffer.limit());
        logger.debug("buffer capacity: {}", buffer.capacity());
        logger.debug("buffer array: {}", buffer.array());
        logger.debug("buffer array offset: {}", buffer.arrayOffset());

        logger.debug("=====after get 2 =====");
        logger.debug("get 2 values: {}", buffer.get(), buffer.get());
        logger.debug("buffer position: {}", buffer.position());
        logger.debug("buffer limit: {}", buffer.limit());
        logger.debug("buffer capacity: {}", buffer.capacity());
        logger.debug("buffer array: {}", buffer.array());
        logger.debug("buffer array offset: {}", buffer.arrayOffset());

        logger.debug("=====after get 5 =====");
        logger.debug("get 3 values: {}", buffer.get(), buffer.get(), buffer.get());
        logger.debug("buffer position: {}", buffer.position());
        logger.debug("buffer limit: {}", buffer.limit());
        logger.debug("buffer capacity: {}", buffer.capacity());
        logger.debug("buffer array: {}", buffer.array());
        logger.debug("buffer array offset: {}", buffer.arrayOffset());

        logger.debug("=====after rewind =====");
        buffer.rewind();
        logger.debug("buffer position: {}", buffer.position());
        logger.debug("buffer limit: {}", buffer.limit());
        logger.debug("buffer capacity: {}", buffer.capacity());
        logger.debug("buffer array: {}", buffer.array());
        logger.debug("buffer array offset: {}", buffer.arrayOffset());

        logger.debug("=====after clear =====");
        buffer.clear();
        logger.debug("buffer position: {}", buffer.position());
        logger.debug("buffer limit: {}", buffer.limit());
        logger.debug("buffer capacity: {}", buffer.capacity());
        logger.debug("buffer array: {}", buffer.array());
        logger.debug("buffer array offset: {}", buffer.arrayOffset());

    }
    
    public static void main(String[] args) {
        
        testIntBuffer();
    }
}