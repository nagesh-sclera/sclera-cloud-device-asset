package io.sclera.queryrepository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QrCodeQueryRepositoryTest {

    @Test
    void upsertSqlIsPostgres() {
        String sql = new QrCodeQueryRepository().getQueryForUpsertQrCodesInBatch();
        assertThat(sql).contains("INSERT INTO qr_code").contains("ON CONFLICT (id) DO UPDATE SET")
            .contains("image_url = EXCLUDED.image_url").contains("is_deleted = false");
        assertThat(sql).doesNotContain("ON DUPLICATE KEY").doesNotContain("VALUES(");
    }

}
