package ru.rbpo.backend.signature;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;

/** Загрузка приватного ключа и сертификата из keystore (путь из конфига). Кэш после первой загрузки. */
@Component
public class SignatureKeyStoreLoader {

    private final SignatureProperties properties;
    private final ResourceLoader resourceLoader;

    private PrivateKey privateKey;
    private Certificate certificate;

    public SignatureKeyStoreLoader(SignatureProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    public synchronized PrivateKey getPrivateKey() {
        if (privateKey == null) {
            loadKeyStore();
        }
        return privateKey;
    }

    public synchronized Certificate getCertificate() {
        if (certificate == null) {
            loadKeyStore();
        }
        return certificate;
    }

    private void loadKeyStore() {
        String path = properties.getKeyStorePath();
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("signature.key-store-path не задан");
        }
        Resource resource = resourceLoader.getResource(path);
        try (InputStream is = resource.getInputStream()) {
            KeyStore ks = KeyStore.getInstance(properties.getKeyStoreType());
            ks.load(is, properties.getKeyStorePassword().toCharArray());
            String alias = properties.getKeyAlias();
            Key key = ks.getKey(alias, properties.getEffectiveKeyPassword().toCharArray());
            if (!(key instanceof PrivateKey)) {
                throw new IllegalStateException("Запись " + alias + " — не приватный ключ");
            }
            this.privateKey = (PrivateKey) key;
            this.certificate = ks.getCertificate(alias);
            if (certificate == null) {
                throw new IllegalStateException("Сертификат для алиаса " + alias + " не найден");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось загрузить keystore из " + path + ": " + e.getMessage(), e);
        }
    }
}
