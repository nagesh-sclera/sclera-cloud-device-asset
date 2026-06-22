package io.sclera.utils;

import io.sclera.client.WorkorderTemplateClient;
import io.sclera.dto.DeviceAlertDTO;
import io.sclera.dto.LocationAlertDTO;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for CorrigoUtils: date/time extraction and formatting, duration math,
 * work-order description/comment building, and address assembly.
 */
@ExtendWith(MockitoExtension.class)
class CorrigoUtilsTest {

    @Mock WorkorderTemplateClient workorderTemplateService;

    @InjectMocks CorrigoUtils corrigoUtils;

    @Test
    void getDatefromDateTime_extractsAndReformatsDate() {
        assertEquals("03/15/2024", corrigoUtils.getDatefromDateTime("2024-03-15T10:30:00"));
    }

    @Test
    void getDatefromDateTime_returnsNullOnBadInput() {
        assertNull(corrigoUtils.getDatefromDateTime("not-a-date"));
    }

    @Test
    void getTimefromDateTime_extractsTwelveHourTime() {
        assertEquals("02:05 PM", corrigoUtils.getTimefromDateTime("2024-03-15T14:05:00"));
    }

    @Test
    void formatHoursMins_formatsHoursAndZeroPadsMinutes() {
        assertEquals("1:30", corrigoUtils.formatHoursMins(90.0));
        assertEquals("0:05", corrigoUtils.formatHoursMins(5.0));
    }

    @Test
    void formatHoursMins_returnsNullWhenDurationNull() {
        assertNull(corrigoUtils.formatHoursMins(null));
    }

    @Test
    void getEndTime_addsDurationToStartTime() {
        // getEndTime does not normalise case, so the AM/PM marker may be locale-cased
        assertEquals("10:30 AM", corrigoUtils.getEndTime("10:00 AM", "30").toUpperCase());
    }

    @Test
    void getWorkOrderDescription_buildsAssetDescription() {
        // constructor order: (id, docker_name, docker_system_type, name, building, floor, location, device_monitor, product_id, image_url, type)
        DeviceAlertDTO device = new DeviceAlertDTO(
                null, null, null, "Pump", "B1", "F1", "Room1", null, null, null, null);
        String result = corrigoUtils.getWorkOrderDescription(device, null);
        assertEquals("Asset: Pump ( Room1, F1, B1 ) ", result);
    }

    @Test
    void getWorkOrderDescription_buildsLocationDescriptionWhenNoDevice() {
        LocationAlertDTO location = new LocationAlertDTO();
        location.setName("Lobby");
        location.setFloor_name("Ground");
        location.setBuilding_name("Tower");
        String result = corrigoUtils.getWorkOrderDescription(null, location);
        assertEquals("Location: Lobby, Ground, Tower", result);
    }

    @Test
    void getFormattedWorkorderComment_appendsTemplateComment() {
        when(workorderTemplateService.getWorkOrderTemplateComment("t1")).thenReturn("Check filters");
        assertEquals("Replace unit - Check filters",
                corrigoUtils.getFormattedWorkorderComment("Replace unit", "t1"));
    }

    @Test
    void getFormattedWorkorderComment_returnsDescriptionWhenNoComment() {
        when(workorderTemplateService.getWorkOrderTemplateComment("t1")).thenReturn(null);
        assertEquals("Replace unit",
                corrigoUtils.getFormattedWorkorderComment("Replace unit", "t1"));
    }

    @Test
    void deviceOnlineOfflineComment_mapsAlertTypes() {
        assertEquals("Device went offline at ", corrigoUtils.deviceOnlineOfflineComment("device_offline"));
        assertEquals("Device came online at ", corrigoUtils.deviceOnlineOfflineComment("device_online"));
        assertNull(corrigoUtils.deviceOnlineOfflineComment("something_else"));
    }

    @Test
    void formatAddressBuilder_joinsAddressParts() throws Exception {
        JSONObject address = new JSONObject();
        address.put("Street", "123 Main");
        address.put("City", "Springfield");
        address.put("State", "IL");
        address.put("Zip", "62701");
        JSONObject data = new JSONObject().put("Address", address);
        JSONObject payload = new JSONObject().put("Data", data);

        String result = corrigoUtils.formatAddressBuilder(payload);
        assertTrue(result.contains("123 Main"), result);
        assertTrue(result.contains("Springfield"), result);
        assertTrue(result.contains(", IL"), result);
        assertTrue(result.contains("62701"), result);
    }

    @Test
    void getDuplicateDateLimit_subtractsDays() {
        assertEquals("2024-03-10", corrigoUtils.getDuplicateDateLimit("2024-03-15", 5));
    }

    @Test
    void getDuplicateDateLimit_returnsNullOnBadInput() {
        assertNull(corrigoUtils.getDuplicateDateLimit("garbage", 5));
    }

    @Test
    void oauthToken_roundTrips() {
        corrigoUtils.setOauth_token("abc123");
        assertEquals("abc123", corrigoUtils.getOauth_token());
    }
}
