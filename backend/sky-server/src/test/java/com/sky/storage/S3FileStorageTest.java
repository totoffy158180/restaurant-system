package com.sky.storage;

import com.sky.config.StorageConfiguration;
import com.sky.properties.StorageProperties;
import org.junit.jupiter.api.Test;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.core.exception.SdkException;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
class S3FileStorageTest {

    private static final String BUCKET = "test-bucket";

    @Container
    static final GenericContainer<?> S3_MOCK = new GenericContainer<>(DockerImageName.parse("adobe/s3mock:5.2.3"))
            .withEnv("COM_ADOBE_TESTING_S3MOCK_STORE_INITIAL_BUCKETS", BUCKET)
            .withExposedPorts(9090)
            .waitingFor(Wait.forHttp("/").forPort(9090).forStatusCode(200));

    private static String endpoint() {
        return "http://" + S3_MOCK.getHost() + ":" + S3_MOCK.getMappedPort(9090);
    }

    private static FileStorage storage(String bucket) {
        StorageProperties properties = new StorageProperties();
        properties.setType("s3");
        StorageProperties.S3 s3 = properties.getS3();
        s3.setEndpoint(endpoint());
        s3.setBucket(bucket);
        s3.setAccessKeyId("test");
        s3.setAccessKeySecret("test");
        s3.setPathStyleAccess(true);
        return new StorageConfiguration().s3FileStorage(properties);
    }

    @Test
    void uploadedObjectIsReadableFromReturnedUrl() throws Exception {
        byte[] bytes = "fake-image".getBytes(StandardCharsets.UTF_8);

        String url = storage(BUCKET).upload(bytes, "a.png", "image/png");

        assertThat(url).isEqualTo(endpoint() + "/" + BUCKET + "/a.png");
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        assertThat(connection.getResponseCode()).isEqualTo(200);
        assertThat(connection.getContentType()).isEqualTo("image/png");
        try (InputStream in = connection.getInputStream()) {
            assertThat(StreamUtils.copyToByteArray(in)).isEqualTo(bytes);
        }
    }

    @Test
    void uploadToMissingBucketThrows() {
        FileStorage storage = storage("missing-bucket");

        assertThatThrownBy(() -> storage.upload(new byte[]{1}, "a.png", "image/png"))
                .isInstanceOf(SdkException.class);
    }
}
