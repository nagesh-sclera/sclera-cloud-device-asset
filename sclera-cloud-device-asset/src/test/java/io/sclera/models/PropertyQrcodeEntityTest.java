package io.sclera.models;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * POJO round-trip for the Property-QR entity graph.
 * No Spring context, no DB — verifies field wiring and intra-graph relations compile and hold values.
 */
class PropertyQrcodeEntityTest {

    @Test
    void propertyQrcodeRoundTrip() {
        // --- PropertyService (tenant-scoped, vdmsId is plain String) ---
        PropertyService ps = new PropertyService();
        ps.setId("svc-1");
        ps.setName("Cleaning");
        ps.setVdms_id("vdms-abc");

        assertThat(ps.getId()).isEqualTo("svc-1");
        assertThat(ps.getName()).isEqualTo("Cleaning");
        assertThat(ps.getVdms_id()).isEqualTo("vdms-abc");

        // --- PropertyServiceRequest (a form field) ---
        PropertyServiceRequest req = new PropertyServiceRequest();
        req.setId("req-1");
        req.setLabel("Cleanliness rating");
        req.setType("select");
        req.setOptions("[\"Good\",\"Average\",\"Poor\"]");
        req.setProperty_service(ps);

        assertThat(req.getId()).isEqualTo("req-1");
        assertThat(req.getProperty_service()).isSameAs(ps);

        // --- PropertyQrcode ---
        PropertyQrcode pq = new PropertyQrcode();
        pq.setId("qr-1");
        pq.setImage_url("https://cdn.example.com/qr/qr-1.png");
        pq.setProperty_service(ps);
        // location left null — testing without a Location stub

        assertThat(pq.getId()).isEqualTo("qr-1");
        assertThat(pq.getImage_url()).isEqualTo("https://cdn.example.com/qr/qr-1.png");
        assertThat(pq.getProperty_service()).isSameAs(ps);

        // --- PropertyServiceResponse (answer submitted via scan) ---
        PropertyServiceResponse resp = new PropertyServiceResponse();
        resp.setId("resp-1");
        resp.setValue("Good");
        resp.setAlert(false);
        resp.setTimestamp(BigInteger.valueOf(1_700_000_000L));
        resp.setProperty_qrcode(pq);
        resp.setProperty_service_request(req);

        assertThat(resp.getId()).isEqualTo("resp-1");
        assertThat(resp.getValue()).isEqualTo("Good");
        assertThat(resp.getAlert()).isFalse();
        assertThat(resp.getTimestamp()).isEqualTo(BigInteger.valueOf(1_700_000_000L));
        assertThat(resp.getProperty_qrcode()).isSameAs(pq);
        assertThat(resp.getProperty_service_request()).isSameAs(req);

        // --- Wire the cascade set back onto PropertyQrcode ---
        pq.setProperty_service_response(Set.of(resp));
        assertThat(pq.getProperty_service_response()).containsExactly(resp);
    }
}
