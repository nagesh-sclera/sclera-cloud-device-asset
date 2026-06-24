package io.sclera.utils;

public interface QrImageStorageService {
    String store(byte[] bytes, String key, String ext);
    byte[] fetch(String key);
    void delete(String key);
}
