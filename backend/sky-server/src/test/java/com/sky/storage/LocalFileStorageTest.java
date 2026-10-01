package com.sky.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageTest {

    @TempDir
    Path dir;

    @Test
    void uploadWritesFileAndReturnsUrl() throws Exception {
        LocalFileStorage storage = new LocalFileStorage(dir.toString(), "/uploads/");
        byte[] bytes = {1, 2, 3};

        String url = storage.upload(bytes, "a.png", "image/png");

        assertThat(url).isEqualTo("/uploads/a.png");
        assertThat(Files.readAllBytes(dir.resolve("a.png"))).isEqualTo(bytes);
    }

    @Test
    void uploadCreatesMissingDirectory() {
        Path store = dir.resolve("store");
        LocalFileStorage storage = new LocalFileStorage(store.toString(), "/uploads");

        storage.upload(new byte[]{1}, "a.png", "image/png");

        assertThat(store.resolve("a.png")).exists();
    }

    @Test
    void uploadRejectsPathTraversal() {
        LocalFileStorage storage = new LocalFileStorage(dir.resolve("store").toString(), "/uploads");

        assertThatThrownBy(() -> storage.upload(new byte[]{1}, "../evil.png", "image/png"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(dir.resolve("evil.png")).doesNotExist();
    }
}
