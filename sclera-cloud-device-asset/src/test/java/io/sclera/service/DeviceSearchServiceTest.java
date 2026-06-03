package io.sclera.service;

import io.sclera.client.APICallClient;
import io.sclera.dto.DeviceDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the pure/algorithmic methods of DeviceSearchService: search-column
 * name mapping, fuzzy-match scoring, and matched-score sorting. The large SQL/JSON query
 * generation and JdbcTemplate query methods are deferred.
 */
@ExtendWith(MockitoExtension.class)
class DeviceSearchServiceTest {

    @Mock JdbcTemplate jdbcTemplate;
    @Mock DeviceService deviceService;
    @Mock APICallClient apiCallService;

    @InjectMocks DeviceSearchService service;

    // ---- updateDeviceSearchColumnName ------------------------------------

    @Test
    void updateDeviceSearchColumnName_mapsKnownColumns() {
        assertThat(service.updateDeviceSearchColumnName("id")).isEqualTo("d.id");
        assertThat(service.updateDeviceSearchColumnName("type")).isEqualTo("d.type");
        assertThat(service.updateDeviceSearchColumnName("location")).isEqualTo("l.name");
        assertThat(service.updateDeviceSearchColumnName("building")).isEqualTo("b.name");
        assertThat(service.updateDeviceSearchColumnName("serial_number")).isEqualTo("d.serial_number");
    }

    @Test
    void updateDeviceSearchColumnName_displayName_returnsCaseExpression() {
        assertThat(service.updateDeviceSearchColumnName("display_name"))
                .contains("user_data_name").contains("CASE WHEN");
    }

    @Test
    void updateDeviceSearchColumnName_unknownColumn_returnsDefaultNameExpression() {
        assertThat(service.updateDeviceSearchColumnName("totally_unknown"))
                .contains("user_data_name").contains("display_name");
    }

    // ---- getFuzzyValueByBaseStringAndSearchString ------------------------

    @Test
    void getFuzzyValue_identicalStrings_returns100() {
        assertThat(service.getFuzzyValueByBaseStringAndSearchString("printer", "printer")).isEqualTo(100);
    }

    @Test
    void getFuzzyValue_nullBase_returnsZero() {
        assertThat(service.getFuzzyValueByBaseStringAndSearchString(null, "printer")).isEqualTo(0);
    }

    @Test
    void getFuzzyValue_caseAndWhitespaceInsensitive() {
        assertThat(service.getFuzzyValueByBaseStringAndSearchString("  Printer ", "printer")).isEqualTo(100);
    }

    // ---- sortFilteredDevicesByMatchedScore -------------------------------

    @Test
    void sortFilteredDevicesByMatchedScore_sortsDescendingByScore() {
        DeviceDTO low = mock(DeviceDTO.class);
        lenient().when(low.getMatched_score()).thenReturn(30);
        DeviceDTO high = mock(DeviceDTO.class);
        lenient().when(high.getMatched_score()).thenReturn(70);
        List<DeviceDTO> devices = new ArrayList<>(List.of(low, high));

        List<DeviceDTO> sorted = service.sortFilteredDevicesByMatchedScore(devices);

        assertThat(sorted).containsExactly(high, low);
    }

    @Test
    void sortFilteredDevicesByMatchedScore_empty_returnsEmpty() {
        assertThat(service.sortFilteredDevicesByMatchedScore(new ArrayList<>())).isEmpty();
    }
}
