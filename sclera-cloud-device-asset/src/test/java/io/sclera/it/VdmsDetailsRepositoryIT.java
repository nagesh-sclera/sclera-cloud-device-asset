package io.sclera.it;

import io.sclera.Repository.VdmsDetailsRepository;
import io.sclera.dto.touchscreen.VdmsDetailsDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/vdms-details-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-vdms-details-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class VdmsDetailsRepositoryIT extends PostgresJpaIT {

    @Autowired
    VdmsDetailsRepository repo;

    @PersistenceContext
    EntityManager em;

    @Test
    void contextLoadsAndRepositoryAutowires() {
        assertThat(repo).isNotNull();
        assertThat(repo.count()).isEqualTo(1);
    }

    @Test
    void getVdmsDetailsId_returnsId() {
        String id = repo.getVdmsDetailsId();
        assertThat(id).isEqualTo("vdd-001");
    }

    @Test
    void getWeatherData_returnsCorrectProjection() {
        VdmsDetailsDTO dto = repo.getWeatherData();
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("vdd-001");
        assertThat(dto.getWeather_city()).isEqualTo("London");
        assertThat(dto.getWeather_zip_code()).isEqualTo("EC1A");
        assertThat(dto.getWeather_country_code()).isEqualTo("GB");
        assertThat(dto.getWeather_latitude()).isEqualTo("51.5074");
        assertThat(dto.getWeather_longitude()).isEqualTo("-0.1278");
        assertThat(dto.getWeather_units()).isEqualTo("metric");
        assertThat(dto.getVdms_id()).isEqualTo("vdms-det-01");
        // weather_data is a TEXT column; assert non-null
        assertThat(dto.getWeather_data()).isNotNull();
    }

    @Test
    void getVdmsLayoutData_returnsCorrectProjection() {
        VdmsDetailsDTO dto = repo.getVdmsLayoutData();
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("vdd-001");
        assertThat(dto.getVdms_id()).isEqualTo("vdms-det-01");
        assertThat(dto.getLayout_data()).isNotNull();
        assertThat(dto.getCorrigo_layout_data()).isNotNull();
    }

    @Test
    void getVdmsDeviceCustomFields_returnsCorrectProjection() {
        VdmsDetailsDTO dto = repo.getVdmsDeviceCustomFields();
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("vdd-001");
        assertThat(dto.getDevice_custom_fields()).isNotNull();
        assertThat(dto.getDevice_custom_fields()).contains("fields");
    }
}
