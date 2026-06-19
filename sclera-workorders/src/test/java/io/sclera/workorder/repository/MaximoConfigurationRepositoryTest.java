package io.sclera.workorder.repository;

import io.sclera.workorder.dto.MaximoConfigurationDTO;
import io.sclera.workorder.entity.MaximoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository slice test against H2 in PostgreSQL-compatibility mode.
 *
 * Covers the read paths (the two NamedNativeQuery methods that drive the
 * configuration GET endpoints). The custom ON CONFLICT upsert is covered by
 * integration tests against real Postgres — H2 supports ON CONFLICT only
 * partially, so we exercise persistence through {@link #save} here.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MaximoConfigurationRepositoryTest {

    @Autowired
    private MaximoConfigurationRepository repository;

    private MaximoConfiguration buildConfig(String id, String vdmsId, String clientId, String clientSecret) {
        MaximoConfiguration cfg = new MaximoConfiguration();
        cfg.setId(id);
        cfg.setName("test-config");
        cfg.setServerUrl("https://maximo.example.com/oslc");
        cfg.setAuthUrl("https://maximo.example.com/oauth/token");
        cfg.setClientId(clientId);
        cfg.setClientSecret(clientSecret);
        cfg.setSites("[{\"siteid\":\"AA\"}]");
        cfg.setVdmsId(vdmsId);
        return cfg;
    }

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void getMaximoConfigurationByVdmsId_returnsDtoWithoutSecrets() {
        repository.save(buildConfig("id-1", "vdms-A", "the-client-id", "the-secret"));

        MaximoConfigurationDTO dto = repository.getMaximoConfigurationByVdmsId("vdms-A");

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("id-1");
        assertThat(dto.getName()).isEqualTo("test-config");
        assertThat(dto.getServerUrl()).isEqualTo("https://maximo.example.com/oslc");
        assertThat(dto.getAuthUrl()).isEqualTo("https://maximo.example.com/oauth/token");
        assertThat(dto.getSites()).contains("AA");
        // 5-column mapping does NOT carry credentials
        assertThat(dto.getClientId()).isNull();
        assertThat(dto.getClientSecret()).isNull();
    }

    @Test
    void getMaximoConfigByVdmsId_returnsDtoIncludingSecrets() {
        repository.save(buildConfig("id-2", "vdms-B", "client-X", "secret-Y"));

        MaximoConfigurationDTO dto = repository.getMaximoConfigByVdmsId("vdms-B");

        assertThat(dto).isNotNull();
        assertThat(dto.getClientId()).isEqualTo("client-X");
        assertThat(dto.getClientSecret()).isEqualTo("secret-Y");
    }

    @Test
    void getMaximoConfigurationByVdmsId_returnsNullForUnknownVdms() {
        assertThat(repository.getMaximoConfigurationByVdmsId("does-not-exist")).isNull();
    }

    @Test
    void deleteById_removesRow() {
        repository.save(buildConfig("id-3", "vdms-C", "c", "s"));
        assertThat(repository.findById("id-3")).isPresent();

        repository.deleteById("id-3");

        assertThat(repository.findById("id-3")).isEmpty();
    }

    // ── upsertMaximoConfiguration (ORM load-then-apply) ───────────────────────

    @Test
    void upsert_insertsNewRow_withSecrets() {
        repository.upsertMaximoConfiguration("m-1", "cfg", "https://s", "https://a",
                "cid", "csecret", "[{\"siteid\":\"AA\"}]", "vd-1");

        MaximoConfigurationDTO dto = repository.getMaximoConfigByVdmsId("vd-1");
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("m-1");
        assertThat(dto.getName()).isEqualTo("cfg");
        assertThat(dto.getServerUrl()).isEqualTo("https://s");
        assertThat(dto.getAuthUrl()).isEqualTo("https://a");
        assertThat(dto.getClientId()).isEqualTo("cid");
        assertThat(dto.getClientSecret()).isEqualTo("csecret");
        assertThat(dto.getSites()).contains("AA");
    }

    @Test
    void upsert_existingRow_nullSecrets_preservedWhileOthersOverwritten() {
        repository.upsertMaximoConfiguration("m-2", "cfg", "https://s", "https://a",
                "cid", "secretV1", "sites1", "vd-2");

        // Update with null clientId / clientSecret and changed name + sites.
        repository.upsertMaximoConfiguration("m-2", "cfg-renamed", "https://s2", "https://a2",
                null, null, "sites2", "vd-2");

        MaximoConfigurationDTO dto = repository.getMaximoConfigByVdmsId("vd-2");
        assertThat(dto.getClientId()).isEqualTo("cid");          // preserved (COALESCE)
        assertThat(dto.getClientSecret()).isEqualTo("secretV1"); // preserved (COALESCE)
        assertThat(dto.getName()).isEqualTo("cfg-renamed");      // overwritten
        assertThat(dto.getServerUrl()).isEqualTo("https://s2");  // overwritten
        assertThat(dto.getSites()).isEqualTo("sites2");          // overwritten
    }

    @Test
    void upsert_existingRow_newSecrets_overwritten() {
        repository.upsertMaximoConfiguration("m-3", "cfg", "https://s", "https://a",
                "cid", "secretV1", "sites", "vd-3");

        repository.upsertMaximoConfiguration("m-3", "cfg", "https://s", "https://a",
                "cid2", "secretV2", "sites", "vd-3");

        MaximoConfigurationDTO dto = repository.getMaximoConfigByVdmsId("vd-3");
        assertThat(dto.getClientId()).isEqualTo("cid2");
        assertThat(dto.getClientSecret()).isEqualTo("secretV2");
    }

    @Test
    void upsert_existingRow_nullNonSecretColumn_overwrittenToNull() {
        repository.upsertMaximoConfiguration("m-4", "cfg", "https://s", "https://a",
                "cid", "sec", "sites", "vd-4");

        // sites is a non-secret column → overwritten even when null (not COALESCE-preserved).
        repository.upsertMaximoConfiguration("m-4", "cfg", "https://s", "https://a",
                "cid", "sec", null, "vd-4");

        MaximoConfigurationDTO dto = repository.getMaximoConfigByVdmsId("vd-4");
        assertThat(dto.getSites()).isNull();
    }
}
