package io.sclera.it;

import io.sclera.Repository.AssetFieldRepository;
import io.sclera.dto.AssetFieldDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/asset-field-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-asset-field-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class AssetFieldRepositoryIT extends PostgresJpaIT {

    @Autowired
    AssetFieldRepository assetFieldRepository;

    @PersistenceContext
    EntityManager em;

    // ---- getGlobalAssetFields ----

    @Test
    void getGlobalAssetFields_matchingNames_returnsIdAndName() {
        List<AssetFieldDTO> result = assetFieldRepository.getGlobalAssetFields(
                List.of("Location", "Vendor"));
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AssetFieldDTO::getId)
                .containsExactlyInAnyOrder("af-001", "af-002");
        assertThat(result).extracting(AssetFieldDTO::getName)
                .containsExactlyInAnyOrder("Location", "Vendor");
        // Confirm only id/name populated (2-arg ctor)
        assertThat(result.get(0).getType()).isNull();
    }

    @Test
    void getGlobalAssetFields_noMatch_returnsEmpty() {
        List<AssetFieldDTO> result = assetFieldRepository.getGlobalAssetFields(
                List.of("NonExistentField"));
        assertThat(result).isEmpty();
    }

    // ---- getAllAssetFields ----

    @Test
    void getAllAssetFields_returnsOnlyNonDeleted() {
        List<AssetFieldDTO> result = assetFieldRepository.getAllAssetFields();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AssetFieldDTO::getId)
                .containsExactlyInAnyOrder("af-001", "af-002");
        // Deleted row (af-003) must not appear
        assertThat(result).extracting(AssetFieldDTO::getId).doesNotContain("af-003");
    }

    @Test
    void getAllAssetFields_fullFieldsPopulated() {
        List<AssetFieldDTO> result = assetFieldRepository.getAllAssetFields();
        AssetFieldDTO loc = result.stream()
                .filter(d -> "af-001".equals(d.getId()))
                .findFirst().orElseThrow();
        assertThat(loc.getName()).isEqualTo("Location");
        assertThat(loc.getType()).isEqualTo("text");
        assertThat(loc.getToolTip()).isEqualTo("Physical location");
        assertThat(loc.getDefaultValue()).isEqualTo("Unknown");
        assertThat(loc.getIsActive()).isTrue();
        assertThat(loc.getIsDeleted()).isFalse();
        assertThat(loc.getShowInSection()).isEqualTo(1);
        assertThat(loc.getCreatedAt()).isEqualTo(BigInteger.valueOf(1700000001000L));
    }

    // ---- deleteAssetFieldsByIds ----

    @Test
    void deleteAssetFieldsByIds_marksRowAsDeleted() {
        assetFieldRepository.deleteAssetFieldsByIds(Set.of("af-001"));
        em.flush();
        em.clear();

        // Read back via scalar JPQL — not findById (avoids full entity load ambiguity)
        Boolean isDeleted = (Boolean) em.createQuery(
                "SELECT af.isDeleted FROM AssetField af WHERE af.id = 'af-001'")
                .getSingleResult();
        assertThat(isDeleted).isTrue();
    }

    @Test
    void deleteAssetFieldsByIds_doesNotAffectOtherRows() {
        assetFieldRepository.deleteAssetFieldsByIds(Set.of("af-001"));
        em.flush();
        em.clear();

        Boolean isDeletedOther = (Boolean) em.createQuery(
                "SELECT af.isDeleted FROM AssetField af WHERE af.id = 'af-002'")
                .getSingleResult();
        assertThat(isDeletedOther).isFalse();
    }
}
