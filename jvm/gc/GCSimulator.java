import java.util.ArrayList;
import java.util.List;

public class GCSimulator {
    // Define 1 Megabyte
    private static final int MB = 1024 * 1024;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting GC log generation experiment...");
        
        // This list will hold references to objects, preventing them from being garbage collected.
        // This simulates a growing cache or a memory leak.
        List<byte[]> longLivedObjects = new ArrayList<>();

        for (int i = 1; i <= 500; i++) {
            // 1. Allocate Short-Lived Objects
            // These arrays are created and immediately lose their reference. 
            // They will quickly fill up the Young Generation and trigger Minor GCs.
            byte[] shortLived1 = new byte[2 * MB];
            byte[] shortLived2 = new byte[2 * MB];

            // 2. Allocate Long-Lived Objects
            // Every 10 iterations, we save a 1MB object into our list.
            // These cannot be collected, will promote to the Old Generation, and will eventually trigger a Full GC.
            if (i % 10 == 0) {
                longLivedObjects.add(new byte[1 * MB]);
                System.out.println("Iteration " + i + ": Added 1MB to long-lived list. Total list size: " + longLivedObjects.size() + " MB");
            }

            // Sleep for 20 milliseconds to slow down the loop and make logs easier to read chronologically
            Thread.sleep(20);
        }

        System.out.println("Experiment finished successfully.");
    }
}
