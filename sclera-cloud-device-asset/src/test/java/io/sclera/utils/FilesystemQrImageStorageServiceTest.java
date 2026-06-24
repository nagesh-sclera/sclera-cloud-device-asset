package io.sclera.utils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.assertj.core.api.Assertions.assertThat;

class FilesystemQrImageStorageServiceTest {
    @Test
    void storeWritesFileAndReturnsUrl(@TempDir Path tmp) throws Exception {
        FilesystemQrImageStorageService svc =
            new FilesystemQrImageStorageService(tmp.toString() + "/", "http://host/images/qrcodes/");
        String url = svc.store(new byte[]{1,2,3}, "qr-1", "png");
        assertThat(url).isEqualTo("http://host/images/qrcodes/qr-1.png");
        assertThat(Files.readAllBytes(tmp.resolve("qr-1.png"))).containsExactly(1,2,3);
        assertThat(svc.fetch("qr-1")).containsExactly(1,2,3);
        svc.delete("qr-1");
        assertThat(Files.exists(tmp.resolve("qr-1.png"))).isFalse();
    }
}
