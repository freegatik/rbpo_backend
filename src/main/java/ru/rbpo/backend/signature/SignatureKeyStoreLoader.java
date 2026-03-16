package ru.rbpo.backend.signature;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;

/**
 * Загрузка приватного ключа и сертификата из keystore.
 * Keystore загружается по пути из конфигурации (classpath:, file: или путь к файлу).
 * Ключи кэшируются в памяти после первой загрузки.
 */
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

    /**
     * Возвращает приватный ключ для подписи. Загружает keystore при первом вызове.
     */
    public synchronized PrivateKey getPrivateKey() {
        if (privateKey == null) {
            loadKeyStore();
        }
        return privateKey;
    }

    /**
     * Возвращает сертификат (публичный ключ) для проверки подписи.
     */
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
                throw new IllegalStateException("Запись " + alias + " не является приватным ключом");
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
