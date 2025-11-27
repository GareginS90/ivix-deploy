package am.ivix.api.security;

import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

public class KeyProvider {
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public KeyProvider(String publicClasspathLocation, String privateClasspathLocation) {
        try {
            this.publicKey = loadPublicKey(publicClasspathLocation);
            this.privateKey = loadPrivateKey(privateClasspathLocation);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load RSA keys", e);
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    private PublicKey loadPublicKey(String path) throws Exception {
        String pem = readClasspath(path);
        pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                 .replace("-----END PUBLIC KEY-----", "")
                 .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private PrivateKey loadPrivateKey(String path) throws Exception {
        String pem = readClasspath(path);
        pem = pem.replace("-----BEGIN RSA PRIVATE KEY-----", "")
                 .replace("-----END RSA PRIVATE KEY-----", "")
                 .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec;
        try {
            // Попытка PKCS#8
            spec = new PKCS8EncodedKeySpec(decoded);
        } catch (IllegalArgumentException e) {
            // Если openssl сгенерил PKCS#1, конвертируй заранее, но попробуем так:
            throw new IllegalArgumentException("Provide PKCS#8 private key (convert with: openssl pkcs8 -topk8 -nocrypt -in rsa-private.pem -out rsa-private-pkcs8.pem)");
        }
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private String readClasspath(String path) throws Exception {
        ClassPathResource res = new ClassPathResource(path);
        try (InputStream is = res.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
