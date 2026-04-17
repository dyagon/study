import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

/**
 * Asymmetric cryptography examples using the JDK:
 *  - RSA OAEP (SHA-256) encrypt/decrypt
 *  - RSA PSS (SHA-256) sign/verify
 *  - ECDSA P-256 sign/verify
 *  - ECDH P-256 key agreement + HKDF-SHA-256 -> AES-GCM
 */
public class AsymmetricExamples {

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

    // -------------------- RSA --------------------
    public static KeyPair generateRsaKey(int bits) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(bits, RNG);
        return kpg.generateKeyPair();
    }

    public static byte[] rsaOaepEncrypt(byte[] plaintext, PublicKey pub) throws Exception {
        // Use OAEP with SHA-256 and MGF1(SHA-256)
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, pub, RNG);
        return cipher.doFinal(plaintext);
    }

    public static byte[] rsaOaepDecrypt(byte[] ciphertext, PrivateKey priv) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, priv);
        return cipher.doFinal(ciphertext);
    }

    public static byte[] rsaPssSign(byte[] message, PrivateKey priv) throws Exception {
        // Use RSASSA-PSS with SHA-256 and MGF1(SHA-256)
        Signature sig = Signature.getInstance("RSASSA-PSS");
        sig.setParameter(new java.security.spec.PSSParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        sig.initSign(priv, RNG);
        sig.update(message);
        return sig.sign();
    }

    public static boolean rsaPssVerify(byte[] signature, byte[] message, PublicKey pub) throws Exception {
        Signature sig = Signature.getInstance("RSASSA-PSS");
        sig.setParameter(new java.security.spec.PSSParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
        sig.initVerify(pub);
        sig.update(message);
        return sig.verify(signature);
    }

    // -------------------- ECDSA P-256 --------------------
    public static KeyPair generateEcP256() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"), RNG);
        return kpg.generateKeyPair();
    }

    public static byte[] ecdsaSign(byte[] message, PrivateKey priv) throws Exception {
        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initSign(priv, RNG);
        sig.update(message);
        return sig.sign();
    }

    public static boolean ecdsaVerify(byte[] signature, byte[] message, PublicKey pub) throws Exception {
        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initVerify(pub);
        sig.update(message);
        return sig.verify(signature);
    }

    // -------------------- ECDH + HKDF -> AES-GCM --------------------
    public static class AesGcmOut {
        public final byte[] iv;           // 12-byte nonce
        public final byte[] ciphertext;   // ciphertext + tag
        AesGcmOut(byte[] iv, byte[] ciphertext) { this.iv = iv; this.ciphertext = ciphertext; }
    }

    private static byte[] hmacSha256(byte[] key, byte[] data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    // Minimal HKDF-SHA256 (RFC 5869)
    public static byte[] hkdfExtract(byte[] salt, byte[] ikm) throws Exception {
        byte[] zeroSalt = new byte[32];
        return hmacSha256(salt == null ? zeroSalt : salt, ikm);
    }

    public static byte[] hkdfExpand(byte[] prk, byte[] info, int outLen) throws Exception {
        int hLen = 32;
        int n = (int) Math.ceil(outLen / (double) hLen);
        byte[] t = new byte[0];
        byte[] okm = new byte[outLen];
        int pos = 0;
        for (int i = 1; i <= n; i++) {
            byte[] input = new byte[t.length + (info == null ? 0 : info.length) + 1];
            System.arraycopy(t, 0, input, 0, t.length);
            if (info != null) System.arraycopy(info, 0, input, t.length, info.length);
            input[input.length - 1] = (byte) i;
            t = hmacSha256(prk, input);
            int copy = Math.min(hLen, outLen - pos);
            System.arraycopy(t, 0, okm, pos, copy);
            pos += copy;
        }
        return okm;
    }

    public static byte[] ecdhSecret(PrivateKey myPriv, PublicKey theirPub) throws Exception {
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(myPriv);
        ka.doPhase(theirPub, true);
        return ka.generateSecret();
    }

    public static AesGcmOut aesGcmEncrypt(byte[] plaintext, byte[] key, byte[] aad) throws Exception {
        byte[] iv = randomBytes(12);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
        if (aad != null && aad.length > 0) cipher.updateAAD(aad);
        byte[] ct = cipher.doFinal(plaintext);
        return new AesGcmOut(iv, ct);
    }

    public static byte[] aesGcmDecrypt(AesGcmOut enc, byte[] key, byte[] aad) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, enc.iv));
        if (aad != null && aad.length > 0) cipher.updateAAD(aad);
        return cipher.doFinal(enc.ciphertext);
    }

    // -------------------- Demo --------------------
    public static void main(String[] args) throws Exception {
        String message = "hello-asymmetric-world";
        byte[] msg = message.getBytes(StandardCharsets.UTF_8);

        // 1) RSA OAEP encrypt/decrypt
        KeyPair rsa = generateRsaKey(2048);
        byte[] rsaCt = rsaOaepEncrypt(msg, rsa.getPublic());
        byte[] rsaPt = rsaOaepDecrypt(rsaCt, rsa.getPrivate());

        System.out.println("RSA-OAEP (SHA-256) Example");
        System.out.println("  pub:       " + b64(rsa.getPublic().getEncoded()));
        System.out.println("  priv:      " + b64(rsa.getPrivate().getEncoded()));
        System.out.println("  ct:        " + b64(rsaCt));
        System.out.println("  decrypted: " + new String(rsaPt, StandardCharsets.UTF_8));

        // 2) RSA-PSS sign/verify
        byte[] rsapssSig = rsaPssSign(msg, rsa.getPrivate());
        boolean rsapssOk = rsaPssVerify(rsapssSig, msg, rsa.getPublic());

        System.out.println();
        System.out.println("RSA-PSS (SHA-256) Example");
        System.out.println("  signature: " + b64(rsapssSig));
        System.out.println("  verify:    " + rsapssOk);

        // 3) ECDSA P-256 sign/verify
        KeyPair ec = generateEcP256();
        byte[] ecdsaSig = ecdsaSign(msg, ec.getPrivate());
        boolean ecdsaOk = ecdsaVerify(ecdsaSig, msg, ec.getPublic());

        System.out.println();
        System.out.println("ECDSA P-256 Example");
        System.out.println("  pub:       " + b64(ec.getPublic().getEncoded()));
        System.out.println("  signature: " + b64(ecdsaSig));
        System.out.println("  verify:    " + ecdsaOk);

        // 4) ECDH P-256 key agreement + HKDF -> AES-GCM between Alice and Bob
        KeyPair alice = generateEcP256();
        KeyPair bob   = generateEcP256();
        byte[] aliceSecret = ecdhSecret(alice.getPrivate(), bob.getPublic());
        byte[] bobSecret   = ecdhSecret(bob.getPrivate(), alice.getPublic());
        // Derive same AES-256 key via HKDF-SHA256
        byte[] salt = randomBytes(16);
        byte[] info = "demo-ssl-ecdh-aes-gcm".getBytes(StandardCharsets.UTF_8);
        byte[] prkA = hkdfExtract(salt, aliceSecret);
        byte[] prkB = hkdfExtract(salt, bobSecret);
        byte[] aesKeyA = hkdfExpand(prkA, info, 32);
        byte[] aesKeyB = hkdfExpand(prkB, info, 32);

        AesGcmOut enc = aesGcmEncrypt(msg, aesKeyA, "aad".getBytes(StandardCharsets.UTF_8));
        byte[] dec = aesGcmDecrypt(enc, aesKeyB, "aad".getBytes(StandardCharsets.UTF_8));

        System.out.println();
        System.out.println("ECDH P-256 + HKDF -> AES-GCM Example");
        System.out.println("  salt:      " + b64(salt));
        System.out.println("aliceSecret: " + b64(aliceSecret));
        System.out.println("bobSecret:   " + b64(bobSecret));
        System.out.println("  alicePub:  " + b64(alice.getPublic().getEncoded()));
        System.out.println("  bobPub:    " + b64(bob.getPublic().getEncoded()));
        System.out.println("  aesKeyA:   " + b64(aesKeyA));
        System.out.println("  aesKeyB:   " + b64(aesKeyB));
        System.out.println("  iv(12):    " + b64(enc.iv));
        System.out.println("  ct+tag:    " + b64(enc.ciphertext));
        System.out.println("  decrypted: " + new String(dec, StandardCharsets.UTF_8));
    }
}
