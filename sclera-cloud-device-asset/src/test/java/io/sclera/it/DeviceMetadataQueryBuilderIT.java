package io.sclera.it;

import io.sclera.queryrepository.DeviceMetadataQueryBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Result-asserting integration test for DeviceMetadataQueryBuilder (the JPA Criteria replacement
 * for the dynamic native SQL behind DeviceMetadataController's distinct device-column lookups),
 * driven against the committed fixture on a PostgreSQL 16 Testcontainer.
 */
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-metadata-it.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-metadata-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceMetadataQueryBuilderIT extends PostgresJpaIT {

    @PersistenceContext
    EntityManager em;

    DeviceMetadataQueryBuilder qb;

    @BeforeEach
    void init() {
        qb = new DeviceMetadataQueryBuilder(em);
    }

    @Test
    void distinctType_scopedToVdms_excludesArchivedAndForeign() {
        // Camera (dmx3) is archived -> excluded; Gateway (dmx5) is v2 -> excluded; Router de-duped.
        assertThat(qb.distinctColumnValues("type", "v1")).containsExactly("Router", "Switch");
    }

    @Test
    void distinctType_unscoped_includesAllVdms() {
        assertThat(qb.distinctColumnValues("type", null)).containsExactly("Gateway", "Router", "Switch");
    }

    @Test
    void distinctType_blankVdms_treatedAsUnscoped() {
        assertThat(qb.distinctColumnValues("type", "  ")).containsExactly("Gateway", "Router", "Switch");
    }

    @Test
    void distinctAssetGroup_excludesEmptyString() {
        // dmx4 has asset_group '' -> excluded by the <> '' predicate.
        assertThat(qb.distinctColumnValues("asset_group", "v1")).containsExactly("GA", "GB");
    }

    @Test
    void distinctCategory_dedupesAndExcludesNull() {
        assertThat(qb.distinctColumnValues("category", "v1")).containsExactly("Cat1");
    }

    @Test
    void unknownColumn_rejectedByWhitelist() {
        assertThat(qb.distinctColumnValues("docker_vdms_id", "v1")).isEmpty();
    }
}
