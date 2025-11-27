package am.ivix.securitycore.keys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Production-level KeyProvider:
 *
 * - Работает с RSA ключами
 * - Приватный ключ должен быть в PKCS#8 (BEGIN PRIVATE KEY)
 * - Публичный ключ должен быть в X.509 (BEGIN PUBLIC KEY)
 * - Один раз загружает и кеширует ключи в памяти
 * - Даёт понятные Error Details, если формат повреждён
 */
public class RSAFileKeyProvider implements KeyProvider {

    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    /**
     * @param publicKeyPath  путь к public.pem
     * @param privateKeyPath путь к private.pkcs8.pem
     */
    public RSAFileKeyProvider(String publicKeyPath, String privateKeyPath) {
        this.publicKey = loadPublicKey(Path.of(publicKeyPath));
        this.privateKey = loadPrivateKey(Path.of(privateKeyPath));
    }

    @Override
    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    // ------------------------------
    //  Private Helpers
    // ------------------------------

    private PublicKey loadPublicKey(Path path) {
        try {
            String pem = Files.readString(path);
            String base64 = extractPem(pem, "PUBLIC KEY");

            byte[] decoded = Base64.getDecoder().decode(base64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);

            return KeyFactory.getInstance("RSA").generatePublic(spec);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load PUBLIC KEY from " + path, e);
        }
    }

    private PrivateKey loadPrivateKey(Path path) {
        try {
            String pem = Files.readString(path);
            String base64 = extractPem(pem, "PRIVATE KEY");

            byte[] decoded = Base64.getDecoder().decode(base64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);

            return KeyFactory.getInstance("RSA").generatePrivate(spec);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to load PRIVATE KEY from " + path + ". Must be PKCS#8.", e);
        }
    }

    /**
     * Извлекает Base64 тело PEM-файла.
     * Защита:
     * - если формат неверный — явная ошибка
     * - если ключ пустой — ошибка
     */
    private String extractPem(String pem, String keyType) {
        String begin = "-----BEGIN " + keyType + "-----";
        String end = "-----END " + keyType + "-----";

        int start = pem.indexOf(begin);
        int stop = pem.indexOf(end);

        if (start == -1 || stop == -1) {
            throw new IllegalArgumentException("Invalid PEM structure: missing " + keyType + " markers");
        }

        String body = pem.substring(start + begin.length(), stop)
                .replaceAll("\\s+", "");

        if (body.isBlank()) {
            throw new IllegalArgumentException("PEM " + keyType + " body is empty");
        }

        return body;
    }
}
