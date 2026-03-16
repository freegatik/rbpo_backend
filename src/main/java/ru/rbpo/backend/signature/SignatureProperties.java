package ru.rbpo.backend.signature;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурация модуля ЭЦП (хранилище ключей, пароли, алиас).
 * См. методичку: signature.key-store-path, key-store-password, key-alias, key-password.
 */
@ConfigurationProperties(prefix = "signature")
public class SignatureProperties {

    /** Путь к keystore: classpath:signing.jks или file:/path/to/keystore.jks */
    private String keyStorePath = "classpath:signing.jks";
    /** Тип хранилища (JKS, PKCS12) */
    private String keyStoreType = "JKS";
    /** Пароль хранилища */
    private String keyStorePassword = "changeit";
    /** Алиас записи с приватным ключом */
    private String keyAlias = "app-signing";
    /** Пароль ключа (если не задан, используется keyStorePassword) */
    private String keyPassword;

    public String getKeyStorePath() { return keyStorePath; }
    public void setKeyStorePath(String keyStorePath) { this.keyStorePath = keyStorePath; }
    public String getKeyStoreType() { return keyStoreType; }
    public void setKeyStoreType(String keyStoreType) { this.keyStoreType = keyStoreType; }
    public String getKeyStorePassword() { return keyStorePassword; }
    public void setKeyStorePassword(String keyStorePassword) { this.keyStorePassword = keyStorePassword; }
    public String getKeyAlias() { return keyAlias; }
    public void setKeyAlias(String keyAlias) { this.keyAlias = keyAlias; }
    public String getKeyPassword() { return keyPassword; }
    public void setKeyPassword(String keyPassword) { this.keyPassword = keyPassword; }

    /** Пароль ключа: если задан отдельно — его, иначе пароль хранилища. */
    public String getEffectiveKeyPassword() {
        return keyPassword != null && !keyPassword.isEmpty() ? keyPassword : keyStorePassword;
    }
}
