package io.sclera.models;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class VdmsColumnsTest {

    @Test
    void serverUrl_roundTrips() {
        Vdms v = new Vdms();
        v.setServer_url("http://vdms-a:8089/vdms");
        assertThat(v.getServer_url()).isEqualTo("http://vdms-a:8089/vdms");
    }

    @Test
    void credentialRef_roundTrips() {
        Vdms v = new Vdms();
        v.setCredential_ref("sclera/dev/edge/vdms-a/credential/");
        assertThat(v.getCredential_ref()).isEqualTo("sclera/dev/edge/vdms-a/credential/");
    }
}
