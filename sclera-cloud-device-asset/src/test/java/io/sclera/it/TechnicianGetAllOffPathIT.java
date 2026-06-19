package io.sclera.it;

import io.sclera.Repository.TechnicianRepository;
import io.sclera.dto.TechnicianDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Transactional
class TechnicianGetAllOffPathIT extends PostgresJpaIT {

    @Autowired
    TechnicianRepository technicianRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void getAllTechnician_offPath_returnsSeededRows() {
        em.createNativeQuery("INSERT INTO technician(id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at, vdms_id) " +
                "VALUES ('t1','a@x.com','111','+1','Alice','dept','desig','UTC','admin',1000, NULL)").executeUpdate();
        em.createNativeQuery("INSERT INTO technician(id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at, vdms_id) " +
                "VALUES ('t2','b@x.com','222','+1','Bob','dept','desig','UTC','admin',2000, NULL)").executeUpdate();
        em.flush();
        em.clear();

        // sort a COPY — the on-path returns an unmodifiable list (Stream.toList()); the off-path list is
        // modifiable, but copying is safe for both and keeps this assertion identical across branches
        List<TechnicianDTO> result = technicianRepository.getAllTechnician().stream()
                .sorted(Comparator.comparing(TechnicianDTO::getId)).toList();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("t1");
        assertThat(result.get(0).getName()).isEqualTo("Alice");
        assertThat(result.get(0).getCreatedAt()).isEqualTo(1000L);
        assertThat(result.get(1).getId()).isEqualTo("t2");
        assertThat(result.get(1).getName()).isEqualTo("Bob");
    }
}
