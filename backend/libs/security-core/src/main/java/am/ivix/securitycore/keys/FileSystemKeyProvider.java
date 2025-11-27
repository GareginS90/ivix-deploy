package am.ivix.securitycore.keys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

public final class FileSystemKeyProvider implements KeyProvider {

    private final Path privateKeyPath;
    private final Path publicKeyPath;

    public FileSystemKeyProvider(Path privateKeyPath, Path publicKeyPath) {
        this.privateKeyPath = Objects.requireNonNull(privateKeyPath);
        this.publicKeyPath = Objects.requireNonNull(publicKeyPath);
    }

    @Override
    public PrivateKey getPrivateKey() {
        try {
            byte[] bytes = Files.readAllBytes(privateKeyPath);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load private key from " + privateKeyPath, e);
        }
    }

    @Override
    public PublicKey getPublicKey() {
        try {
            byte[] bytes = Files.readAllBytes(publicKeyPath);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load public key from " + publicKeyPath, e);
        }
    }
}
