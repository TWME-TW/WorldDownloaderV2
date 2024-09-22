package dev.twme.worldDownloaderV2;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class S3Manager {
    private final MinioClient minioClient;
    private final ConfigManager config;

    public S3Manager(ConfigManager config) {
        this.config = config;
        minioClient = MinioClient.builder()
                .endpoint(config.getS3Endpoint())
                .credentials(config.getS3AccessKey(), config.getS3SecretKey())
                .build();
    }

    public CompletableFuture<String> uploadFile(File file, String objectName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PutObjectArgs putArgs = PutObjectArgs.builder()
                        .bucket(config.getS3Bucket())
                        .object(objectName)
                        .stream(new java.io.FileInputStream(file), file.length(), -1)
                        .contentType("application/zip")
                        .build();

                minioClient.putObject(putArgs);

                GetPresignedObjectUrlArgs urlArgs = GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(config.getS3Bucket())
                        .object(objectName)
                        .expiry(config.getLinkExpiry())
                        .build();

                return minioClient.getPresignedObjectUrl(urlArgs);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}
