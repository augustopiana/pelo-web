package com.peloweb.vinilos.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageService implements StorageService {

    private final S3Client s3;
    private final String bucket;
    private final String publicUrl;

    public S3StorageService(S3Client s3,
                            @Value("${app.storage.bucket}") String bucket,
                            @Value("${app.storage.public-url}") String publicUrl) {
        this.s3 = s3;
        this.bucket = bucket;
        // sin barra final, para no duplicarla al armar la URL
        this.publicUrl = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
    }

    @Override
    public String upload(byte[] content, String key, String contentType) {
        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(content));
        return urlFor(key);
    }

    @Override
    public void delete(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    @Override
    public String urlFor(String key) {
        return publicUrl + "/" + bucket + "/" + key;
    }

    @Override
    public String keyFromUrl(String url) {
        String prefix = publicUrl + "/" + bucket + "/";
        return url.startsWith(prefix) ? url.substring(prefix.length()) : url;
    }
}

