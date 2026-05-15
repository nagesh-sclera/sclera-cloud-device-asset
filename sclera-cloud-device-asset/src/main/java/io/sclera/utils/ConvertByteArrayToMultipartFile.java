package io.sclera.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class ConvertByteArrayToMultipartFile implements MultipartFile {

    private final byte[] bytes;
    private final String originalFilename;
    private final String name;
    private final String contentType;

    public ConvertByteArrayToMultipartFile(byte[] bytes, String originalFilename, String name, String contentType) {
        this.bytes = bytes;
        this.originalFilename = originalFilename;
        this.name = name;
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return bytes == null || bytes.length == 0;
    }

    @Override
    public long getSize() {
        return bytes.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return bytes;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        try (FileOutputStream outputStream = new FileOutputStream(dest)) {
            outputStream.write(bytes);
        }
    }
}
