package io.sclera.it;

import io.sclera.Repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/asset-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-asset-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class AssetRepositoryIT extends PostgresJpaIT {

    @Autowired
    AssetRepository assetRepository;

    @Test
    void contextLoadsAndRepositoryAutowires() {
        assertThat(assetRepository).isNotNull();
        assertThat(assetRepository.count()).isEqualTo(3);
    }

    @Test
    void getTotalAssetCount_countsAllRows() {
        assertThat(assetRepository.getTotalAssetCount()).isEqualTo(3);
    }

    @Test
    void getParentAssetSubsystemCount_countsChildren() {
        assertThat(assetRepository.getParentAssetSubsystemCount("a1")).isEqualTo(1);
    }

    @Test
    void getSubsystemParentIdByAssetId_returnsParent() {
        assertThat(assetRepository.getSubsystemParentIdByAssetId("a2")).isEqualTo("a1");
    }

    @Test
    void getSubAssetIdByParentId_returnsChildIds() {
        assertThat(assetRepository.getSubAssetIdByParentId("a1")).containsExactly("a2");
    }

    @Test
    void getUniqueDeviceTypes_returnsDistinct() {
        assertThat(assetRepository.getUniqueDeviceTypes())
            .containsExactlyInAnyOrder("pump", "valve", "meter");
    }

    @Test
    void checkImportExists_trueWhenRowsPresent() {
        assertThat(assetRepository.checkImportExists()).isTrue();
    }
}
