package io.sclera.it;

import io.sclera.Repository.DeviceTechnicianAISuggestionRepository;
import io.sclera.dto.DeviceTechnicianAISuggestionDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-technician-ai-suggestion-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-technician-ai-suggestion-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceTechnicianAISuggestionRepositoryIT extends PostgresJpaIT {

    @Autowired
    DeviceTechnicianAISuggestionRepository repo;

    @PersistenceContext
    EntityManager em;

    @Test
    void contextLoadsAndRepositoryAutowires() {
        assertThat(repo).isNotNull();
        assertThat(repo.count()).isEqualTo(2);
    }

    @Test
    void getdevicetechnicianbyid_returnsCorrectProjection() {
        DeviceTechnicianAISuggestionDTO dto = repo.getdevicetechnicianbyid("dtas-001");
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("dtas-001");
        assertThat(dto.getDeviceType()).isEqualTo("Camera");
        assertThat(dto.getVdmsId()).isEqualTo("vdms-ai-01");
        // technicians is a jsonb column returned as a String; just assert it is non-null
        assertThat(dto.getTechnicians()).isNotNull();
    }

    @Test
    void getdevicetechnicianbyid_unknownId_returnsNull() {
        DeviceTechnicianAISuggestionDTO dto = repo.getdevicetechnicianbyid("no-such-id");
        assertThat(dto).isNull();
    }

    @Test
    void getAlldevicetechnician_returnsAllRows() {
        List<DeviceTechnicianAISuggestionDTO> all = repo.getAlldevicetechnician();
        assertThat(all).hasSize(2);
        assertThat(all).extracting(DeviceTechnicianAISuggestionDTO::getId)
                .containsExactlyInAnyOrder("dtas-001", "dtas-002");
    }

    @Test
    void getDeviceTechnicianAISuggestionByDeviceType_returnsPayload() {
        String technicians = repo.getDeviceTechnicianAISuggestionByDeviceType("Camera", "vdms-ai-01");
        assertThat(technicians).isNotNull();
        assertThat(technicians).contains("tech-1");
    }

    @Test
    void deletedevicetechnicianById_removesRow() {
        repo.deletedevicetechnicianById("dtas-001");
        em.flush();
        em.clear();
        Long count = (Long) em.createQuery(
                "SELECT COUNT(s) FROM DeviceTechnicianAISuggestion s WHERE s.id = 'dtas-001'")
                .getSingleResult();
        assertThat(count).isEqualTo(0L);
        // other row must still exist
        assertThat(repo.count()).isEqualTo(1);
    }
}
