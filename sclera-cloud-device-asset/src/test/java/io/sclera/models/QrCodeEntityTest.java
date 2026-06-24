package io.sclera.models;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class QrCodeEntityTest {
    @Test void newFieldsRoundTrip() {
        QrCode q = new QrCode();
        q.setCustomerOrgId("org-1"); q.setAdcQrCodeCheck(1);
        assertThat(q.getCustomerOrgId()).isEqualTo("org-1");
        assertThat(q.getAdcQrCodeCheck()).isEqualTo(1);
        ClientQrCode c = new ClientQrCode(); c.setAdcClientQrCodeCheck(1);
        assertThat(c.getAdcClientQrCodeCheck()).isEqualTo(1);
    }
}
