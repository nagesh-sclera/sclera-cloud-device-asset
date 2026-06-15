package io.sclera.it;

import io.sclera.Repository.TechnicianSkillRepository;
import io.sclera.dto.TechnicianSkillDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql",                     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/technician-skill-pilot.sql",   executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-technician-skill-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class TechnicianSkillRepositoryIT extends PostgresJpaIT {

    @Autowired
    TechnicianSkillRepository repo;

    @PersistenceContext
    EntityManager em;

    @Test
    void contextLoads() {
        assertThat(repo).isNotNull();
        assertThat(repo.count()).isEqualTo(3);
    }

    @Test
    void getAllTechnicianSkill_returnsAllRows() {
        List<TechnicianSkillDTO> all = repo.getAllTechnicianSkill();
        assertThat(all).hasSize(3);
        assertThat(all).extracting(TechnicianSkillDTO::getId)
                .containsExactlyInAnyOrder("skill-1", "skill-2", "skill-3");
        // sync field is null (CAST(NULL AS integer))
        assertThat(all).allSatisfy(dto -> assertThat(dto.getSync()).isNull());
    }

    @Test
    void getTechnicianSkillById_returnsCorrectRow() {
        TechnicianSkillDTO dto = repo.getTechnicianSkillById("skill-1");
        assertThat(dto).isNotNull();
        assertThat(dto.getName()).isEqualTo("Electrical");
        assertThat(dto.getType()).isEqualTo("primary");
        assertThat(dto.getRating()).isEqualByComparingTo(new BigDecimal("4.5"));
        assertThat(dto.getRanking()).isEqualTo(1);
        assertThat(dto.getCreatedBy()).isEqualTo("admin");
        assertThat(dto.getCreatedAt()).isEqualTo(1700000000000L);
        assertThat(dto.getTechnicianId()).isEqualTo("tech-skill-1");
        assertThat(dto.getSync()).isNull();
    }

    @Test
    void getSkillsByTechnicianId_returnsCorrectSubset() {
        List<TechnicianSkillDTO> skills = repo.getSkillsByTechnicianId("tech-skill-1");
        assertThat(skills).hasSize(3);
        assertThat(skills).allSatisfy(dto ->
                assertThat(dto.getTechnicianId()).isEqualTo("tech-skill-1"));
    }

    @Test
    void deleteTechnicianSkillById_removesRow() {
        repo.deleteTechnicianSkillById("skill-1");
        em.flush();
        assertThat(repo.count()).isEqualTo(2);
        List<String> ids = em.createQuery(
                "SELECT ts.id FROM TechnicianSkill ts", String.class).getResultList();
        assertThat(ids).containsExactlyInAnyOrder("skill-2", "skill-3");
    }

    @Test
    void findExistingTechnicianSkillsByIds_returnsOnlyExisting() {
        Set<String> found = repo.findExistingTechnicianSkillsByIds(
                List.of("skill-1", "skill-3", "skill-nonexistent"));
        assertThat(found).containsExactlyInAnyOrder("skill-1", "skill-3");
    }

    @Test
    void deleteTechnicianSkillsByIds_removesMatchingRows() {
        int deleted = repo.deleteTechnicianSkillsByIds(Set.of("skill-1", "skill-2"));
        assertThat(deleted).isEqualTo(2);
        em.flush();
        assertThat(repo.count()).isEqualTo(1);
    }

    @Test
    void deleteTechnicianSkillsByTechnicianIds_removesAllForTechnician() {
        int deleted = repo.deleteTechnicianSkillsByTechnicianIds(Set.of("tech-skill-1"));
        assertThat(deleted).isEqualTo(3);
        em.flush();
        assertThat(repo.count()).isEqualTo(0);
    }
}
