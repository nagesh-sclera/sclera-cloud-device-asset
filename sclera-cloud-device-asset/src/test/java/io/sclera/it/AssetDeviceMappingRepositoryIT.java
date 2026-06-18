package io.sclera.it;

import io.sclera.Repository.AssetDeviceMappingRepository;
import io.sclera.dto.touchscreen.assetmapper.AssetDeviceMappingDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Result-asserting IT for the JPA-converted AssetDeviceMappingRepository, run against real PostgreSQL.
 */
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/asset-device-mapping-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-asset-device-mapping-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class AssetDeviceMappingRepositoryIT extends PostgresJpaIT {

    @Autowired
    AssetDeviceMappingRepository repo;

    @PersistenceContext
    EntityManager em;

    @Test
    void findByDeviceIds_returnsAssetIdsForDevices() {
        assertThat(repo.findByDeviceIds(List.of("d1"))).containsExactly("a1");
        assertThat(repo.findByDeviceIds(List.of("d1", "d2"))).containsExactlyInAnyOrder("a1", "a2");
        assertThat(repo.findByDeviceIds(List.of("ghost"))).isEmpty();
    }

    @Test
    void findByAssetId_returnsTrueFalseString() {
        assertThat(repo.findByAssetId("a1")).isEqualTo("true");
        assertThat(repo.findByAssetId("a9")).isEqualTo("false");
    }

    @Test
    void findMappings_returnsAssetDeviceProjections() {
        List<AssetDeviceMappingDTO> mappings = repo.findMappings();
        assertThat(mappings).hasSize(2);
        assertThat(mappings).extracting(AssetDeviceMappingDTO::getAsset_id)
                .containsExactlyInAnyOrder("a1", "a2");
        assertThat(mappings).extracting(AssetDeviceMappingDTO::getDevice_id)
                .containsExactlyInAnyOrder("d1", "d2");
    }

    @Test
    void deleteByDeviceId_removesOnlyThatDevicesMapping() {
        repo.deleteByDeviceId("d1");
        assertThat(repo.findByDeviceIds(List.of("d1"))).isEmpty();
        assertThat(repo.findByDeviceIds(List.of("d2"))).containsExactly("a2");
    }

    @Test
    void deleteAllRecords_removesEverything() {
        repo.deleteAllRecords();
        assertThat(repo.count()).isZero();
    }

    @Test
    void saveNewAssetMapping_insertPath_persistsAllFields() {
        repo.saveNewAssetMapping("m3", 55, "a1", "d2");
        em.flush();
        em.clear();

        assertThat((Integer) em.createQuery(
                "SELECT m.matchScore FROM AssetDeviceMapping m WHERE m.id = 'm3'").getSingleResult())
                .isEqualTo(55);
        assertThat((String) em.createQuery(
                "SELECT m.asset.id FROM AssetDeviceMapping m WHERE m.id = 'm3'").getSingleResult())
                .isEqualTo("a1");
        assertThat((String) em.createQuery(
                "SELECT m.device.id FROM AssetDeviceMapping m WHERE m.id = 'm3'").getSingleResult())
                .isEqualTo("d2");
    }

    @Test
    void saveNewAssetMapping_conflictPath_updatesOnlyMatchScore() {
        // m1 exists: asset a1, device d1, match_score 80. Call with DIFFERENT asset/device + new score.
        repo.saveNewAssetMapping("m1", 99, "a2", "d2");
        em.flush();
        em.clear();

        // only match_score is updated on conflict; asset/device are left untouched
        assertThat((Integer) em.createQuery(
                "SELECT m.matchScore FROM AssetDeviceMapping m WHERE m.id = 'm1'").getSingleResult())
                .isEqualTo(99);
        assertThat((String) em.createQuery(
                "SELECT m.asset.id FROM AssetDeviceMapping m WHERE m.id = 'm1'").getSingleResult())
                .isEqualTo("a1"); // unchanged
        assertThat((String) em.createQuery(
                "SELECT m.device.id FROM AssetDeviceMapping m WHERE m.id = 'm1'").getSingleResult())
                .isEqualTo("d1"); // unchanged
    }
}
