package ru.rbpo.backend.signature;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Конфиг ЭЦП: signature.* в application.properties (keystore path, password, alias). */
@ConfigurationProperties(prefix = "signature")
public class SignatureProperties {

    private String keyStorePath = "classpath:signing.jks";
    private String keyStoreType = "JKS";
    private String keyStorePassword = "changeit";
    private String keyAlias = "app-signing";
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

    /** Если keyPassword не задан — берётся пароль хранилища. */
    public String getEffectiveKeyPassword() {
        return keyPassword != null && !keyPassword.isEmpty() ? keyPassword : keyStorePassword;
    }
}
