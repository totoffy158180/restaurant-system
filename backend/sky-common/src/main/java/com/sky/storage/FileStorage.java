package com.sky.storage;

public interface FileStorage {

    String upload(byte[] bytes, String objectName, String contentType);
}
