package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.storage")
@Data
public class StorageProperties {

    private String type = "local";
    private Local local = new Local();
    private S3 s3 = new S3();

    @Data
    public static class Local {
        private String dir = "uploads";
        private String urlPrefix = "/uploads";
    }

    @Data
    public static class S3 {
        private String endpoint;
        private String region = "us-east-1";
        private String bucket;
        private String accessKeyId;
        private String accessKeySecret;
        private String publicUrlPrefix;
        private boolean pathStyleAccess;
    }
}
