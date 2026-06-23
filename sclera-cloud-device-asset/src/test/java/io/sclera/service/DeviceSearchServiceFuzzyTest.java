package io.sclera.service;

import io.sclera.dto.DeviceDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coverage for DeviceSearchService.getFuzzyValueByDeviceAndSearchString - reflective fuzzy scoring
 * of a device's default fields plus its custom fields, writing the best match back onto the device.
 */
class DeviceSearchServiceFuzzyTest {

    private final DeviceSearchService service = new DeviceSearchService();

    @Test
    void getFuzzyValueByDeviceAndSearchString_exactCustomFieldMatch_scores100() {
        DeviceDTO device = new DeviceDTO();
        device.setCustom_fields("[{\"AssetTag\":\"PRINTER01\"}]");

        service.getFuzzyValueByDeviceAndSearchString(device, "PRINTER01");

        assertThat(device.getMatched_score()).isEqualTo(100);
        assertThat(device.getMatched_column()).isEqualTo("AssetTag");
        assertThat(device.getMatched_info()).contains("AssetTag").contains("100");
    }

    @Test
    void getFuzzyValueByDeviceAndSearchString_noMatch_emptyInfo() {
        DeviceDTO device = new DeviceDTO(); // all fields null, no custom fields

        service.getFuzzyValueByDeviceAndSearchString(device, "anything");

        assertThat(device.getMatched_score()).isEqualTo(0);
        assertThat(device.getMatched_info()).isEmpty();
    }
}
