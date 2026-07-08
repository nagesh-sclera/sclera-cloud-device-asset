package io.sclera.it;

import io.sclera.Repository.VdmsJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/vdms-jpa-repository-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-vdms-jpa-repository-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class VdmsJpaRepositoryIT extends PostgresJpaIT {

    @Autowired
    VdmsJpaRepository repo;

    @Test
    void contextLoads() {
        assertThat(repo).isNotNull();
    }

    @Test
    void findAllIds_returnsSavedIds() {
        assertThat(repo.findAllIds()).containsExactlyInAnyOrder("v-jpa-1", "v-jpa-2");
    }

    @Test
    void countBuildingsByVdmsId_countsLinkedBuildingsAndZeroWhenNone() {
        assertThat(repo.countBuildingsByVdmsId("v-jpa-1")).isEqualTo(2);
        assertThat(repo.countBuildingsByVdmsId("v-jpa-2")).isZero();
    }

    @Test
    void countAssetsByVdmsId_countsLinkedAssetsAndZeroWhenNone() {
        assertThat(repo.countAssetsByVdmsId("v-jpa-1")).isEqualTo(1);
        assertThat(repo.countAssetsByVdmsId("v-jpa-2")).isZero();
    }
}
