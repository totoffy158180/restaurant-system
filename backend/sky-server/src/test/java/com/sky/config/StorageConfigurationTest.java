package com.sky.config;

import com.sky.properties.StorageProperties;
import com.sky.storage.FileStorage;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class StorageConfigurationTest {

    private static String publicUrlPrefix(StorageProperties.S3 s3) {
        StorageProperties properties = new StorageProperties();
        properties.setS3(s3);
        FileStorage storage = new StorageConfiguration().s3FileStorage(properties);
        return (String) ReflectionTestUtils.getField(storage, "publicUrlPrefix");
    }

    private static StorageProperties.S3 s3(String bucket, String region) {
        StorageProperties.S3 s3 = new StorageProperties.S3();
        s3.setBucket(bucket);
        s3.setRegion(region);
        s3.setAccessKeyId("test");
        s3.setAccessKeySecret("test");
        return s3;
    }

    @Test
    void awsUrlUsesBucketRegion() {
        assertThat(publicUrlPrefix(s3("my-bucket", "ap-northeast-1")))
                .isEqualTo("https://my-bucket.s3.ap-northeast-1.amazonaws.com");
    }

    @Test
    void customEndpointUsesVirtualHostStyle() {
        StorageProperties.S3 s3 = s3("my-bucket", "auto");
        s3.setEndpoint("https://account.r2.cloudflarestorage.com/");

        assertThat(publicUrlPrefix(s3)).isEqualTo("https://my-bucket.account.r2.cloudflarestorage.com");
    }

    @Test
    void customEndpointWithPathStyle() {
        StorageProperties.S3 s3 = s3("my-bucket", "us-east-1");
        s3.setEndpoint("http://minio:9000");
        s3.setPathStyleAccess(true);

        assertThat(publicUrlPrefix(s3)).isEqualTo("http://minio:9000/my-bucket");
    }

    @Test
    void explicitPublicUrlPrefixWins() {
        StorageProperties.S3 s3 = s3("my-bucket", "us-east-1");
        s3.setPublicUrlPrefix("https://cdn.example.com/images/");

        assertThat(publicUrlPrefix(s3)).isEqualTo("https://cdn.example.com/images");
    }
}
