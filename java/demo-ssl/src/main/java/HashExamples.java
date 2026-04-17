import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash examples for SSL/TLS context - MD5 and SHA demonstrations
 */
public class HashExamples {

    // Convert bytes to lowercase hex (common digest display)
    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Generic helpers so new algorithms (e.g. SHA-384) are trivial to add
    public static String hashString(String algorithm, String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return toHex(digest);
    }

    public static String hashFile(String algorithm, Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        // Stream the file in chunks to avoid loading large files into memory
        try (InputStream in = Files.newInputStream(path)) {
            byte[] buf = new byte[8192];
            int read;
            while ((read = in.read(buf)) != -1) {
                md.update(buf, 0, read);
            }
        }
        return toHex(md.digest());
    }

    /**
     * Calculate MD5 hash of a string
     */
    public static String md5Hash(String input) throws Exception {
        return hashString("MD5", input);
    }

    /**
     * Calculate SHA-256 hash of a string
     */
    public static String sha256Hash(String input) throws Exception {
        return hashString("SHA-256", input);
    }

    /**
     * Calculate SHA-1 hash of a string
     */
    public static String sha1Hash(String input) throws Exception {
        return hashString("SHA-1", input);
    }

    /**
     * Calculate SHA-512 hash of a string
     */
    public static String sha512Hash(String input) throws Exception {
        return hashString("SHA-512", input);
    }

    public static void main(String[] args) throws Exception {
        // CLI usage (optional):
        //   java HashExamples md5 string "hello"
        //   java HashExamples sha-256 file ./path/to/file
        if (args.length >= 3) {
            String alg = args[0].toUpperCase().replace("SHA-", "SHA-"); // normalize
            String mode = args[1].toLowerCase();
            switch (mode) {
                case "string":
                    System.out.println(hashString(alg, args[2]));
                    return;
                case "file":
                    System.out.println(hashFile(alg, Path.of(args[2])));
                    return;
                default:
                    System.err.println("Unknown mode: " + mode + " (use 'string' or 'file')");
                    return;
            }
        }

        String testString = "hello-ssl-world";

        System.out.println("Hash Examples for: " + testString);
        System.out.println("=========================================");

        System.out.println("MD5:     " + md5Hash(testString));
        System.out.println("SHA-1:   " + sha1Hash(testString));
        System.out.println("SHA-256: " + sha256Hash(testString));
        System.out.println("SHA-512: " + sha512Hash(testString));

        System.out.println("\n=========================================");
        System.out.println("Additional String Examples:");
        System.out.println("=========================================");

        String[] examples = {
            "ssl-certificate",
            "tls-handshake",
            "encryption-key",
            "digital-signature",
            "abc",           // classic test vector
            "hello world"    // common sample
        };

        for (String example : examples) {
            System.out.println("\nString: " + example);
            System.out.println("  MD5:     " + md5Hash(example));
            System.out.println("  SHA-1:   " + sha1Hash(example));
            System.out.println("  SHA-256: " + sha256Hash(example));
        }

        // Optional file-hash demo: hash this source file if readable
        Path self = Path.of("demo-ssl/src/main/java/HashExamples.java");
        if (Files.isReadable(self)) {
            System.out.println("\n=========================================");
            System.out.println("File Hash Example: " + self);
            System.out.println("=========================================");
            System.out.println("  MD5:     " + hashFile("MD5", self));
            System.out.println("  SHA-256: " + hashFile("SHA-256", self));
        }
    }
}
