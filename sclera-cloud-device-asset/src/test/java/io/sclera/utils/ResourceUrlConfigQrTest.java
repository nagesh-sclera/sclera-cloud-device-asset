package io.sclera.utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ResourceUrlConfigQrTest {
    @Test void qrGettersWork() {
        ResourceUrlConfig c = new ResourceUrlConfig();
        c.setServer_qrcode_images_url("u/"); c.setServices_cloud_server_url("s");
        assertThat(c.getServer_qrcode_images_url()).isEqualTo("u/");
        assertThat(c.getServices_cloud_server_url()).isEqualTo("s");
    }
}
