package io.sclera.mapper;

import io.sclera.dto.TechnicianDTO;
import io.sclera.models.Technician;
import io.sclera.models.Vdms;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Docker-free unit test for {@link TechnicianDtoMapper} — the mapper used by the
 * {@code TechnicianRepositoryImpl} MapStruct on-path for {@code getAllTechnician}. Verifies parity
 * with the old {@code technicianMapping} @ConstructorResult: exactly the 11 projected fields are
 * populated (with {@code vdmsId} sourced null-safely from {@code vdms.id}) and every field outside
 * that projection stays null ({@code ignoreByDefault = true}).
 */
class TechnicianDtoMapperTest {

    private final TechnicianDtoMapper mapper = new TechnicianDtoMapperImpl();

    @Test
    void toDto_mapsTheElevenProjectedFields() {
        Vdms vdms = new Vdms();
        vdms.setId("vdms-9");

        Technician t = new Technician();
        t.setId("t1");
        t.setEmail("a@x.com");
        t.setPhone("111");
        t.setCountryCode("+1");
        t.setName("Alice");
        t.setDepartment("dept");
        t.setDesignation("desig");
        t.setTimeZone("UTC");
        t.setCreatedBy("admin");
        t.setCreatedAt(1000L);
        t.setVdms(vdms);

        TechnicianDTO dto = mapper.toDto(t);

        assertThat(dto.getId()).isEqualTo("t1");
        assertThat(dto.getEmail()).isEqualTo("a@x.com");
        assertThat(dto.getPhone()).isEqualTo("111");
        assertThat(dto.getCountryCode()).isEqualTo("+1");
        assertThat(dto.getName()).isEqualTo("Alice");
        assertThat(dto.getDepartment()).isEqualTo("dept");
        assertThat(dto.getDesignation()).isEqualTo("desig");
        assertThat(dto.getTimeZone()).isEqualTo("UTC");
        assertThat(dto.getCreatedBy()).isEqualTo("admin");
        assertThat(dto.getCreatedAt()).isEqualTo(1000L);
        assertThat(dto.getVdmsId()).isEqualTo("vdms-9");
    }

    @Test
    void toDto_nonProjectedFieldsStayNull() {
        Technician t = new Technician();
        t.setId("t1");

        TechnicianDTO dto = mapper.toDto(t);

        // not part of the technicianMapping projection — must remain null
        assertThat(dto.getCost()).isNull();
        assertThat(dto.getUnit()).isNull();
        assertThat(dto.getType()).isNull();
        assertThat(dto.getSync()).isNull();
        assertThat(dto.getDeviceId()).isNull();
        assertThat(dto.getPrimarySkill()).isNull();
        assertThat(dto.getAvailability()).isNull();
        assertThat(dto.getTechnicianAvailabilityDto()).isNull();
        assertThat(dto.getTechnicianSkillDto()).isNull();
        assertThat(dto.getTechnicianCertificateDtos()).isNull();
    }

    @Test
    void toDto_nullVdms_yieldsNullVdmsId() {
        Technician t = new Technician();
        t.setId("t1");
        t.setVdms(null);

        TechnicianDTO dto = mapper.toDto(t);

        assertThat(dto.getVdmsId()).isNull();
    }

    @Test
    void toDto_nullEntity_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
