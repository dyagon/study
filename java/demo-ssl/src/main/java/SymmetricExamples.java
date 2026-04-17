import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Symmetric cryptography examples using the JDK (AES-GCM and AES-CBC).
 * Notes:
 *  - Prefer AES-GCM for new code (provides confidentiality + integrity).
 *  - AES-CBC shown for completeness; pair with an HMAC for integrity in real systems.
 */
public class SymmetricExamples {

    // -------------------- Common helpers --------------------
    private static final SecureRandom RNG = new SecureRandom();

    private static String b64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] randomBytes(int len) {
        byte[] b = new byte[len];
        RNG.nextBytes(b);
        return b;
    }

    public static SecretKey generateAesKey(int bits) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(bits, RNG);
        return kg.generateKey();
    }

    public static SecretKey deriveAesKeyFromPassword(char[] password, byte[] salt, int bits, int iterations) throws Exception {
        // PBKDF2 with HMAC-SHA-256
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bits);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] raw = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(raw, "AES");
    }

    // -------------------- AES-GCM --------------------
    public static class AesGcmResult {
        public final byte[] iv;           // 12-byte nonce
        public final byte[] ciphertext;   // includes authentication tag (appended by JCE)
        public AesGcmResult(byte[] iv, byte[] ciphertext) { this.iv = iv; this.ciphertext = ciphertext; }
    }

    public static AesGcmResult aesGcmEncrypt(byte[] plaintext, SecretKey key, byte[] aad) throws Exception {
        byte[] iv = randomBytes(12); // 96-bit nonce recommended for GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv); // 128-bit auth tag
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        if (aad != null && aad.length > 0) cipher.updateAAD(aad);
        byte[] ct = cipher.doFinal(plaintext);
        return new AesGcmResult(iv, ct);
    }

    public static byte[] aesGcmDecrypt(AesGcmResult enc, SecretKey key, byte[] aad) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, enc.iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        if (aad != null && aad.length > 0) cipher.updateAAD(aad);
        return cipher.doFinal(enc.ciphertext);
    }

    // -------------------- AES-CBC (PKCS#5 padding) --------------------
    public static class AesCbcResult {
        public final byte[] iv;           // 16-byte IV
        public final byte[] ciphertext;
        public AesCbcResult(byte[] iv, byte[] ciphertext) { this.iv = iv; this.ciphertext = ciphertext; }
    }

    public static AesCbcResult aesCbcEncrypt(byte[] plaintext, SecretKey key) throws Exception {
        byte[] iv = randomBytes(16); // 128-bit IV for AES block size
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] ct = cipher.doFinal(plaintext);
        return new AesCbcResult(iv, ct);
    }

    public static byte[] aesCbcDecrypt(AesCbcResult enc, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(enc.iv));
        return cipher.doFinal(enc.ciphertext);
    }

    // -------------------- Demo --------------------
    public static void main(String[] args) throws Exception {
        String plaintext = "hello-symmetric-worldkkkkkkkkkkkkkkkkkkkkkkkkkk";
        byte[] pt = plaintext.getBytes(StandardCharsets.UTF_8);

        // 1) AES-GCM with random 256-bit key and AAD
        SecretKey gcmKey = generateAesKey(256);
        byte[] aad = "demo-ssl".getBytes(StandardCharsets.UTF_8);
        AesGcmResult gcm = aesGcmEncrypt(pt, gcmKey, aad);
        byte[] decryptedGcm = aesGcmDecrypt(gcm, gcmKey, aad);

        System.out.println("AES-GCM Example");
        System.out.println("  key(256):   " + b64(gcmKey.getEncoded()));
        System.out.println("  iv(12):     " + b64(gcm.iv));
        System.out.println("  ct+tag:     " + b64(gcm.ciphertext));
        System.out.println("  decrypted:  " + new String(decryptedGcm, StandardCharsets.UTF_8));

        // 2) AES from password via PBKDF2 (salt + iterations)
        char[] password = "p@ssw0rd!".toCharArray();
        byte[] salt = randomBytes(16);
        int iterations = 150_000; // tune for your environment
        SecretKey pwKey = deriveAesKeyFromPassword(password, salt, 256, iterations);
        AesGcmResult gcm2 = aesGcmEncrypt(pt, pwKey, null);
        byte[] dec2 = aesGcmDecrypt(gcm2, pwKey, null);

        System.out.println();
        System.out.println("AES-GCM From Password (PBKDF2)");
        System.out.println("  salt:       " + b64(salt));
        System.out.println("  key(256):   " + b64(pwKey.getEncoded()));
        System.out.println("  iv(12):     " + b64(gcm2.iv));
        System.out.println("  ct+tag:     " + b64(gcm2.ciphertext));
        System.out.println("  decrypted:  " + new String(dec2, StandardCharsets.UTF_8));

        // 3) AES-CBC (legacy) example
        SecretKey cbcKey = generateAesKey(256);
        AesCbcResult cbc = aesCbcEncrypt(pt, cbcKey);
        byte[] decCbc = aesCbcDecrypt(cbc, cbcKey);

        System.out.println();
        System.out.println("AES-CBC Example (PKCS5Padding)");
        System.out.println("  key(256):   " + b64(cbcKey.getEncoded()));
        System.out.println("  iv(16):     " + b64(cbc.iv));
        System.out.println("  ct:         " + b64(cbc.ciphertext));
        System.out.println("  decrypted:  " + new String(decCbc, StandardCharsets.UTF_8));
    }
}
