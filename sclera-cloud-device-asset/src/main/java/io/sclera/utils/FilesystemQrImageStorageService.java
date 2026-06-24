package io.sclera.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;

@Component @Profile({"local","test","dev","default","docker"})
public class FilesystemQrImageStorageService implements QrImageStorageService {
    private final String absolutePath;
    private final String urlBase;
    public FilesystemQrImageStorageService(
        @Value("${sclera.server-qrcode-images-absolute-path}") String absolutePath,
        @Value("${sclera.server-qrcode-images-url}") String urlBase) {
        this.absolutePath = absolutePath; this.urlBase = urlBase;
    }
    private Path path(String key, String ext) { return Paths.get(absolutePath, key + "." + ext); }
    private Path find(String key) {
        for (String e : new String[]{"png","jpeg","pdf","zip","txt"}) { Path p = path(key,e); if (Files.exists(p)) return p; }
        return path(key,"png");
    }
    public String store(byte[] bytes, String key, String ext) {
        // key may contain a subdirectory (e.g. "<batchId>/<qrId>"), so create the full
        // parent chain, not just the base dir.
        try { Path target = path(key, ext); Files.createDirectories(target.getParent()); Files.write(target, bytes); }
        catch (IOException e) { throw new UncheckedIOException(e); }
        return urlBase + key + "." + ext;
    }
    public byte[] fetch(String key) { try { return Files.readAllBytes(find(key)); } catch (IOException e) { throw new UncheckedIOException(e); } }
    public void delete(String key) { try { Files.deleteIfExists(find(key)); } catch (IOException e) { throw new UncheckedIOException(e); } }
}
