package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.properties.StorageProperties;
import com.sky.storage.FileStorage;
import com.sky.storage.LocalFileStorage;
import com.sky.storage.S3FileStorage;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@Slf4j
public class StorageConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "sky.storage", name = "type", havingValue = "local", matchIfMissing = true)
    public FileStorage localFileStorage(StorageProperties storageProperties) {
        StorageProperties.Local local = storageProperties.getLocal();
        LocalFileStorage storage = new LocalFileStorage(local.getDir(), local.getUrlPrefix());
        log.info("file storage: local, dir={}", storage.getDir());
        return storage;
    }

    @Bean
    @ConditionalOnProperty(prefix = "sky.storage", name = "type", havingValue = "s3")
    public FileStorage s3FileStorage(StorageProperties storageProperties) {
        StorageProperties.S3 s3 = storageProperties.getS3();
        S3ClientBuilder builder = S3Client.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .region(Region.of(s3.getRegion()))
                .credentialsProvider(credentialsProvider(s3))
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(s3.isPathStyleAccess())
                        .build());
        if (StringUtils.hasText(s3.getEndpoint())) {
            builder.endpointOverride(URI.create(s3.getEndpoint()));
        }
        String publicUrlPrefix = publicUrlPrefix(s3);
        log.info("file storage: s3, bucket={}, publicUrlPrefix={}", s3.getBucket(), publicUrlPrefix);
        return new S3FileStorage(builder.build(), s3.getBucket(), publicUrlPrefix);
    }

    @Bean
    @ConditionalOnProperty(prefix = "sky.storage", name = "type", havingValue = "aliyun")
    public FileStorage aliOssFileStorage(AliOssProperties aliOssProperties) {
        log.info("file storage: aliyun, bucket={}", aliOssProperties.getBucketName());
        return new AliOssUtil(aliOssProperties.getEndpoint(), aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(), aliOssProperties.getBucketName());
    }

    private AwsCredentialsProvider credentialsProvider(StorageProperties.S3 s3) {
        if (StringUtils.hasText(s3.getAccessKeyId()) && StringUtils.hasText(s3.getAccessKeySecret())) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(s3.getAccessKeyId(), s3.getAccessKeySecret()));
        }
        return DefaultCredentialsProvider.builder().build();
    }

    private String publicUrlPrefix(StorageProperties.S3 s3) {
        if (StringUtils.hasText(s3.getPublicUrlPrefix())) {
            return s3.getPublicUrlPrefix();
        }
        if (!StringUtils.hasText(s3.getEndpoint())) {
            return "https://" + s3.getBucket() + ".s3." + s3.getRegion() + ".amazonaws.com";
        }
        String endpoint = s3.getEndpoint().replaceAll("/+$", "");
        if (s3.isPathStyleAccess()) {
            return endpoint + "/" + s3.getBucket();
        }
        URI uri = URI.create(endpoint);
        return uri.getScheme() + "://" + s3.getBucket() + "." + uri.getAuthority();
    }
}
