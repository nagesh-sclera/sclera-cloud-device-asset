package io.sclera.it;

import io.sclera.Repository.TechnicianCertificateRepository;
import io.sclera.dto.TechnicianCertificateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql",                    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/technician-cert-pilot.sql",   executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-technician-cert-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class TechnicianCertificateRepositoryIT extends PostgresJpaIT {

    @Autowired
    TechnicianCertificateRepository repo;

    @PersistenceContext
    EntityManager em;

    @Test
    void contextLoads() {
        assertThat(repo).isNotNull();
        assertThat(repo.count()).isEqualTo(3);
    }

    @Test
    void getAllTechnicianCertificates_returnsAllRows() {
        List<TechnicianCertificateDTO> all = repo.getAllTechnicianCertificates();
        assertThat(all).hasSize(3);
        assertThat(all).extracting(TechnicianCertificateDTO::getId)
                .containsExactlyInAnyOrder("cert-1", "cert-2", "cert-3");
        // sync field is null (CAST(NULL AS integer))
        assertThat(all).allSatisfy(dto -> assertThat(dto.getSync()).isNull());
    }

    @Test
    void getTechnicianCertificateById_returnsCorrectRow() {
        TechnicianCertificateDTO dto = repo.getTechnicianCertificateById("cert-1");
        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("AWS Cert");
        assertThat(dto.getType()).isEqualTo("cloud");
        assertThat(dto.getUrl()).isEqualTo("https://example.com/cert1");
        assertThat(dto.getTechnicianId()).isEqualTo("tech-cert-1");
        assertThat(dto.getSync()).isNull();
    }

    @Test
    void getCertificatesByTechnicianId_returnsCorrectSubset() {
        List<TechnicianCertificateDTO> certs = repo.getCertificatesByTechnicianId("tech-cert-1");
        assertThat(certs).hasSize(3);
        assertThat(certs).allSatisfy(dto ->
                assertThat(dto.getTechnicianId()).isEqualTo("tech-cert-1"));
    }

    @Test
    void deleteTechnicianCertificateById_removesRow() {
        repo.deleteTechnicianCertificateById("cert-1");
        em.flush();
        assertThat(repo.count()).isEqualTo(2);
        // read-back via scalar JPQL
        List<String> ids = em.createQuery(
                "SELECT tc.id FROM TechnicianCertificate tc", String.class).getResultList();
        assertThat(ids).containsExactlyInAnyOrder("cert-2", "cert-3");
    }

    @Test
    void findExistingTechnicianCertificatesByIds_returnsOnlyExisting() {
        Set<String> found = repo.findExistingTechnicianCertificatesByIds(
                List.of("cert-1", "cert-2", "cert-nonexistent"));
        assertThat(found).containsExactlyInAnyOrder("cert-1", "cert-2");
    }

    @Test
    void deleteTechnicianCertificatesByIds_removesMatchingRows() {
        int deleted = repo.deleteTechnicianCertificatesByIds(Set.of("cert-1", "cert-2"));
        assertThat(deleted).isEqualTo(2);
        em.flush();
        assertThat(repo.count()).isEqualTo(1);
    }

    @Test
    void deleteTechnicianCertificatesByTechnicianIds_removesAllForTechnician() {
        int deleted = repo.deleteTechnicianCertificatesByTechnicianIds(Set.of("tech-cert-1"));
        assertThat(deleted).isEqualTo(3);
        em.flush();
        assertThat(repo.count()).isEqualTo(0);
    }
}
