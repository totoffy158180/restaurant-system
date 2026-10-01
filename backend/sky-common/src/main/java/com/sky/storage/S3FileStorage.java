package com.sky.storage;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
public class S3FileStorage implements FileStorage {

    private final S3Client s3Client;
    private final String bucket;
    private final String publicUrlPrefix;

    public S3FileStorage(S3Client s3Client, String bucket, String publicUrlPrefix) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicUrlPrefix = publicUrlPrefix.endsWith("/")
                ? publicUrlPrefix.substring(0, publicUrlPrefix.length() - 1)
                : publicUrlPrefix;
    }

    @Override
    public String upload(byte[] bytes, String objectName, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectName)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(bytes));
        String url = publicUrlPrefix + "/" + objectName;
        log.info("文件上传到:{}", url);
        return url;
    }
}
