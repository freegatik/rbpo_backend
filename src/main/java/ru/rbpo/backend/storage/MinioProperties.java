package ru.rbpo.backend.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /** Endpoint без слеша в конце, например http://localhost:9000 */
    private String endpoint = "http://localhost:9000";
    private String accessKey = "rbpoapp";
    private String secretKey = "rbpoappsecret";
    private String bucket = "rbpo-signature-files";
    private String region = "us-east-1";
    /** Срок жизни presigned GET (секунды). */
    private int presignedExpirySeconds = 3600;

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    public String getBucket() { return bucket; }
    public void setBucket(String bucket) { this.bucket = bucket; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public int getPresignedExpirySeconds() { return presignedExpirySeconds; }
    public void setPresignedExpirySeconds(int presignedExpirySeconds) { this.presignedExpirySeconds = presignedExpirySeconds; }
}
