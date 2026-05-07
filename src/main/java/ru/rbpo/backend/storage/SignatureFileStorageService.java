package ru.rbpo.backend.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
public class SignatureFileStorageService {

    private final MinioClient minioClient;
    private final MinioProperties props;

    public SignatureFileStorageService(MinioClient minioClient, MinioProperties props) {
        this.minioClient = minioClient;
        this.props = props;
    }

    public void uploadBytes(String objectKey, byte[] data, String contentType) throws Exception {
        try (InputStream in = new ByteArrayInputStream(data)) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .stream(in, data.length, -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build());
        }
    }

    public void uploadStream(String objectKey, InputStream stream, long size, String contentType) throws Exception {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(props.getBucket())
                .object(objectKey)
                .stream(stream, size, -1)
                .contentType(contentType != null ? contentType : "application/octet-stream")
                .build());
    }

    public String presignedGetUrl(String objectKey) throws Exception {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(props.getBucket())
                .object(objectKey)
                .expiry(props.getPresignedExpirySeconds(), TimeUnit.SECONDS)
                .build());
    }

    /** Лучший-effort откат при ошибке после загрузки. */
    public void deleteQuietly(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception ignored) {
            // ignore
        }
    }
}
