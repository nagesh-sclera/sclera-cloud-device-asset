package io.sclera.it;

import io.sclera.Repository.ClientBarCodeRepository;
import io.sclera.dto.ClientBarCodeDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql",                  executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/client-bar-code-pilot.sql",  executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-client-bar-code-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class ClientBarCodeRepositoryIT extends PostgresJpaIT {

    @Autowired
    ClientBarCodeRepository repo;

    @PersistenceContext
    EntityManager em;

    @Test
    void contextLoads() {
        assertThat(repo).isNotNull();
        // 5 rows seeded
        assertThat(repo.count()).isEqualTo(5);
    }

    @Test
    void getClientBarCodeCountByDeviceId_countsByDevice() {
        // dev-cbc-1 has cbc-1 (not deleted) and cbc-5 (deleted) — 2 rows total
        assertThat(repo.getClientBarCodeCountByDeviceId("dev-cbc-1")).isEqualTo(2);
        // dev-cbc-2 has only cbc-2
        assertThat(repo.getClientBarCodeCountByDeviceId("dev-cbc-2")).isEqualTo(1);
    }

    @Test
    void getBarCodesByDeviceIds_returnsMatchingDTOs() {
        Set<ClientBarCodeDTO> result = repo.getBarCodesByDeviceIds(Set.of("dev-cbc-1"));
        // cbc-1 and cbc-5 are linked to dev-cbc-1
        assertThat(result).extracting(ClientBarCodeDTO::getId)
                .containsExactlyInAnyOrder("cbc-1", "cbc-5");
        result.forEach(dto -> assertThat(dto.getDeviceId()).isEqualTo("dev-cbc-1"));
    }

    @Test
    void getBarCodesByLocationIds_returnsMatchingDTOs() {
        Set<ClientBarCodeDTO> result = repo.getBarCodesByLocationIds(Set.of("loc-cbc-1", "loc-cbc-2"));
        assertThat(result).extracting(ClientBarCodeDTO::getId)
                .containsExactlyInAnyOrder("cbc-3", "cbc-4");
        assertThat(result).allSatisfy(dto -> assertThat(dto.getLocationId()).isNotNull());
    }

    @Test
    void updateIsDeletedForAllClientBarCode_setsAllTrue() {
        repo.updateIsDeletedForAllClientBarCode();
        em.flush();
        // verify via scalar JPQL read-back
        List<Boolean> flags = em.createQuery(
                "SELECT cbc.isDeleted FROM ClientBarCode cbc", Boolean.class).getResultList();
        assertThat(flags).allMatch(Boolean.TRUE::equals);
    }

    @Test
    void deleteOldClientBarCode_removesDeletedRows() {
        // cbc-5 is already is_deleted=true; mark all then delete
        repo.updateIsDeletedForAllClientBarCode();
        em.flush();
        repo.deleteOldClientBarCode();
        em.flush();
        assertThat(repo.count()).isEqualTo(0);
    }
}
