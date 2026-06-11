package io.sclera.it;

import io.sclera.Repository.AssetRepository;
import io.sclera.dto.touchscreen.assetmapper.AssetDTO;
import io.sclera.models.Asset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    // ── Task 2: JPQL constructor-expression tests ────────────────────────────

    @Test
    void getPaginatedAssets_filtersByImportTypeAndSearch() {
        List<AssetDTO> result = assetRepository.getPaginatedAssets("corrigo", "null",
                PageRequest.of(0, 10));
        assertThat(result).extracting(AssetDTO::getId)
                .containsExactly("a1", "a2"); // ORDER BY display_name
        assertThat(result).allSatisfy(a -> assertThat(a.getMatch_score()).isNull());
    }

    @Test
    void getPaginatedAssets_searchKeyFilters() {
        List<AssetDTO> result = assetRepository.getPaginatedAssets("corrigo", "Alpha",
                PageRequest.of(0, 10));
        assertThat(result).extracting(AssetDTO::getId).containsExactly("a1");
    }

    @Test
    void getLinkedAssets_joinsMappingForMatchScore() {
        List<AssetDTO> result = assetRepository.getLinkedAssets("d1");
        assertThat(result).extracting(AssetDTO::getId).contains("a1");
        assertThat(result).filteredOn(a -> a.getId().equals("a1")).first()
                .extracting(AssetDTO::getMatch_score).isEqualTo(88);
    }

    @Test
    void getUnmappedAssets_excludesMappedAndMatched() {
        // a1 is mapped (in asset_device_mapping), a2 is matched (is_matched=true)
        // only a3 is unmapped and unmatched
        List<AssetDTO> result = assetRepository.getUnmappedAssets(PageRequest.of(0, 10));
        assertThat(result).extracting(AssetDTO::getId)
                .contains("a3").doesNotContain("a1", "a2");
    }

    @Test
    void getFilteredAssets_returnsAll() {
        List<AssetDTO> result = assetRepository.getFilteredAssets("display_name", PageRequest.of(0, 10));
        assertThat(result).hasSize(3);
        assertThat(result).allSatisfy(a -> assertThat(a.getMatch_score()).isNull());
    }

    @Test
    void getAssetsById_returnsRequestedIds() {
        List<AssetDTO> result = assetRepository.getAssetsById(List.of("a1", "a3"));
        assertThat(result).extracting(AssetDTO::getId)
                .containsExactlyInAnyOrder("a1", "a3");
    }

    @Test
    void getUnmappedMatchedAssets_returnsMatchedAndUnmapped() {
        // a2 is matched but not in asset_device_mapping → should appear
        List<AssetDTO> result = assetRepository.getUnmappedMatchedAssets();
        assertThat(result).extracting(AssetDTO::getId).contains("a2");
        assertThat(result).extracting(AssetDTO::getId).doesNotContain("a1"); // a1 is mapped
    }

    @Test
    void getSubAssetsByParentId_returnsChildren() {
        List<AssetDTO> result = assetRepository.getSubAssetsByParentId("a1");
        assertThat(result).extracting(AssetDTO::getId).containsExactly("a2");
    }

    @Test
    void getUnmappedAssetsByIds_returnsUnmappedSubset() {
        // a1 is in asset_device_mapping so excluded; a3 is unmapped and unmatched
        List<AssetDTO> result = assetRepository.getUnmappedAssetsByIds(List.of("a1", "a3"));
        assertThat(result).extracting(AssetDTO::getId)
                .contains("a3").doesNotContain("a1");
    }

    @Test
    void getSubSystemParentAssets_returnsParentsForImportType() {
        // a1 has subsystem_parent_id IS NULL and import_type=corrigo → should appear
        List<AssetDTO> result = assetRepository.getSubSystemParentAssets("corrigo", PageRequest.of(0, 10));
        assertThat(result).extracting(AssetDTO::getId).contains("a1");
        assertThat(result).extracting(AssetDTO::getId).doesNotContain("a2"); // a2 has parent=a1
    }

    @Test
    void getSubSystemAssets_returnsChildren() {
        List<AssetDTO> result = assetRepository.getSubSystemAssets("a1", PageRequest.of(0, 10));
        assertThat(result).extracting(AssetDTO::getId).containsExactly("a2");
    }

    @Test
    void getUnmappedSubSystemParentAssets_excludesMapped() {
        // a1 is in asset_device_mapping → excluded. a3 has no parent and is unmapped/unmatched
        List<AssetDTO> result = assetRepository.getUnmappedSubSystemParentAssets(PageRequest.of(0, 10));
        assertThat(result).extracting(AssetDTO::getId)
                .contains("a3").doesNotContain("a1");
    }

    @Test
    void getUnmappedSubSystemParentAssetsByAssetIds_filtersById() {
        // a3 has subsystem_parent_id IS NULL, is_matched=false, not in mapping
        List<AssetDTO> result = assetRepository.getUnmappedSubSystemParentAssetsByAssetIds(List.of("a3", "a1"));
        assertThat(result).extracting(AssetDTO::getId)
                .contains("a3").doesNotContain("a1");
    }

    @Test
    void getAllAssets_byImportType() {
        List<AssetDTO> result = assetRepository.getAllAssets("corrigo");
        assertThat(result).extracting(AssetDTO::getId)
                .containsExactlyInAnyOrder("a1", "a2");
    }

    @Test
    void getAllAssets_bacnetImportType() {
        List<AssetDTO> result = assetRepository.getAllAssets("bacnet");
        assertThat(result).extracting(AssetDTO::getId).containsExactly("a3");
    }

    /**
     * Regression for CONCAT_WS null-skip semantics: a row with a NULL description must still
     * match a searchKey on its display_name. Plain JPQL CONCAT can propagate NULL (dropping the
     * row); the COALESCE wrapping preserves the original CONCAT_WS('', ...) behavior.
     */
    @Test
    void getPaginatedAssets_matchesRowWithNullDescription() {
        Asset nullDesc = new Asset();
        nullDesc.setId("a-null");
        nullDesc.setDisplay_name("Zeta Widget");
        nullDesc.setDescription(null);
        nullDesc.setImport_type("corrigo");
        nullDesc.setOriginalKeys("");
        nullDesc.setIsMatched(false);
        assetRepository.saveAndFlush(nullDesc);

        List<AssetDTO> result = assetRepository.getPaginatedAssets("corrigo", "Zeta", PageRequest.of(0, 10));
        assertThat(result).extracting(AssetDTO::getId).containsExactly("a-null");
    }

    // ── Task 3: @Modifying JPQL write tests ─────────────────────────────────

    @Test
    void setMatched_thenDeleteAllMatched_removesRows() {
        assetRepository.setMatched(true, "a3");
        assetRepository.deleteAllMatchedRecords(); // a2 (seed-matched) + a3 now matched; neither has a mapping
        assertThat(assetRepository.findById("a2")).isEmpty();
        assertThat(assetRepository.findById("a3")).isEmpty();
        assertThat(assetRepository.findById("a1")).isPresent();
    }

    @Test
    void updateSubsystemParentId_reparents() {
        assetRepository.updateSubsystemParentId("a3", "a1");
        assertThat(assetRepository.getSubAssetIdByParentId("a1")).contains("a2", "a3");
    }

    @Test
    void getAssetCount_filtersAndCounts() {
        assertThat(assetRepository.getAssetCount("corrigo", "null")).isEqualTo(2);
        assertThat(assetRepository.getAssetCount("corrigo", "Alpha")).isEqualTo(1);
    }

    @Test
    void setTypeGeneric_setsNullTypesToGeneric() {
        assetRepository.setAllAssetsToUnMatched();
        // set a1's type to null, then call setTypeGeneric to fill it with 'generic'
        assetRepository.updateDeviceType("pump", null);
        assetRepository.setTypeGeneric();
        assertThat(assetRepository.getUniqueDeviceTypes()).contains("generic");
    }
}
