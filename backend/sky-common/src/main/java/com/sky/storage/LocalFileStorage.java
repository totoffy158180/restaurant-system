package com.sky.storage;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class LocalFileStorage implements FileStorage {

    private final Path dir;
    private final String urlPrefix;

    public LocalFileStorage(String dir, String urlPrefix) {
        this.dir = Paths.get(dir).toAbsolutePath().normalize();
        this.urlPrefix = urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
    }

    @Override
    public String upload(byte[] bytes, String objectName, String contentType) {
        Path target = dir.resolve(objectName).normalize();
        if (!target.getParent().equals(dir)) {
            throw new IllegalArgumentException("Invalid object name: " + objectName);
        }
        try {
            Files.createDirectories(dir);
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write file " + target, e);
        }
        log.info("文件上传到:{}", target);
        return urlPrefix + "/" + objectName;
    }

    public Path getDir() {
        return dir;
    }
}
