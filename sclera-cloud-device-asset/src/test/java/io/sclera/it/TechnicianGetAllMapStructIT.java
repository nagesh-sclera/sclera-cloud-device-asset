package io.sclera.it;

import io.sclera.Repository.TechnicianRepository;
import io.sclera.dto.TechnicianDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Parity gate for the {@code getAllTechnician} MapStruct on-path: forces the
 * {@code mapstruct.read.technician.get-all} flag true and asserts the same golden values as
 * {@code TechnicianGetAllOffPathIT}. Both ITs passing proves the on path is byte-identical to the
 * native off path. Requires Docker/Testcontainers (currently blocked by Docker Desktop 29 vs the
 * project's Testcontainers version) — runs in CI; the Docker-free {@code TechnicianDtoMapperTest}
 * covers the mapping logic in the meantime.
 */
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@TestPropertySource(properties = "mapstruct.read.technician.get-all=true")
@Transactional
class TechnicianGetAllMapStructIT extends PostgresJpaIT {

    @Autowired
    TechnicianRepository technicianRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void getAllTechnician_mapStructPath_matchesGoldenValues() {
        em.createNativeQuery("INSERT INTO technician(id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at, vdms_id) " +
                "VALUES ('t1','a@x.com','111','+1','Alice','dept','desig','UTC','admin',1000, NULL)").executeUpdate();
        em.createNativeQuery("INSERT INTO technician(id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at, vdms_id) " +
                "VALUES ('t2','b@x.com','222','+1','Bob','dept','desig','UTC','admin',2000, NULL)").executeUpdate();
        em.flush();
        em.clear();

        // sort a COPY — the on-path returns Stream.toList() (unmodifiable)
        List<TechnicianDTO> result = technicianRepository.getAllTechnician().stream()
                .sorted(Comparator.comparing(TechnicianDTO::getId)).toList();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("t1");
        assertThat(result.get(0).getEmail()).isEqualTo("a@x.com");
        assertThat(result.get(0).getCountryCode()).isEqualTo("+1");
        assertThat(result.get(0).getName()).isEqualTo("Alice");
        assertThat(result.get(0).getCreatedAt()).isEqualTo(1000L);
        assertThat(result.get(0).getVdmsId()).isNull();
        // fields NOT in the 11-column projection must stay null (ignoreByDefault parity)
        assertThat(result.get(0).getCost()).isNull();
        assertThat(result.get(0).getSync()).isNull();
        assertThat(result.get(1).getId()).isEqualTo("t2");
        assertThat(result.get(1).getName()).isEqualTo("Bob");
    }
}
