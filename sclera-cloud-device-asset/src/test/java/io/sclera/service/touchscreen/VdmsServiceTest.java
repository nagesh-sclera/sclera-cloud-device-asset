package io.sclera.service.touchscreen;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.Repository.VdmsDetailsRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.Repository.VdmsconfigurationRepository;
import io.sclera.dto.touchscreen.VdmsDetailsDTO;
import io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for VdmsService: repository delegates, the pure device-fields JSON merge,
 * id-generation upserts, and the device-custom-fields merge branches. File-IO, bash health,
 * and access-token startup methods are deferred.
 */
@ExtendWith(MockitoExtension.class)
class VdmsServiceTest {

    @Mock VdmsRepository vdmsRepository;
    @Mock VdmsDetailsRepository vdmsDetailsRepository;
    @Mock VdmsconfigurationRepository vdmsconfigurationRepository;

    @InjectMocks VdmsService service;

    // ---- delegates -------------------------------------------------------

    @Test
    void getVDMSId_delegates() {
        when(vdmsRepository.getVDMSId()).thenReturn("vdms1");
        assertThat(service.getVDMSId()).isEqualTo("vdms1");
    }

    @Test
    void getConfiguration_delegates() {
        VdmsConfigurationDTO cfg = mock(VdmsConfigurationDTO.class);
        when(vdmsconfigurationRepository.getConfiguration()).thenReturn(cfg);
        assertThat(service.getConfiguration()).isSameAs(cfg);
    }

    @Test
    void getVDMSDetails_delegates() {
        VdmsDTO dto = mock(VdmsDTO.class);
        when(vdmsRepository.getVdmsDetails()).thenReturn(dto);
        assertThat(service.getVDMSDetails()).isSameAs(dto);
    }

    @Test
    void getIsMaster_delegates() {
        when(vdmsRepository.getIsMaster()).thenReturn(1);
        assertThat(service.getIsMaster()).isEqualTo(1);
    }

    @Test
    void getCustomerOrgIdByVdmsId_delegates() {
        when(vdmsRepository.getCustomerOrgIdByVdmsId("v1")).thenReturn("org1");
        assertThat(service.getCustomerOrgIdByVdmsId("v1")).isEqualTo("org1");
    }

    @Test
    void getDeviceCustomFields_delegatesToDetailsRepo() {
        VdmsDetailsDTO d = mock(VdmsDetailsDTO.class);
        when(vdmsDetailsRepository.getVdmsDeviceCustomFields()).thenReturn(d);
        assertThat(service.getDeviceCustomFields("u", "v1")).isSameAs(d);
    }

    // ---- getMergedDeviceFieldsList ---------------------------------------

    @Test
    void getMergedDeviceFieldsList_nullDto_returnsBase() {
        StringBuilder base = new StringBuilder("[{\"column\":\"name\"}]");
        String merged = service.getMergedDeviceFieldsList(base, null);
        assertThat(merged).contains("name");
    }

    @Test
    void getMergedDeviceFieldsList_addsNewColumn() throws Exception {
        StringBuilder base = new StringBuilder("[{\"column\":\"name\"}]");
        VdmsDetailsDTO dto = mock(VdmsDetailsDTO.class);
        when(dto.getDevice_custom_fields()).thenReturn("[{\"column\":\"extra\"}]");

        String merged = service.getMergedDeviceFieldsList(base, dto);

        org.json.JSONArray result = new org.json.JSONArray(merged);
        assertThat(result.length()).isEqualTo(2);
        assertThat(merged).contains("extra");
    }

    @Test
    void getMergedDeviceFieldsList_skipsDuplicateColumn() throws Exception {
        StringBuilder base = new StringBuilder("[{\"column\":\"name\"}]");
        VdmsDetailsDTO dto = mock(VdmsDetailsDTO.class);
        when(dto.getDevice_custom_fields()).thenReturn("[{\"column\":\"name\"}]");

        String merged = service.getMergedDeviceFieldsList(base, dto);

        org.json.JSONArray result = new org.json.JSONArray(merged);
        assertThat(result.length()).isEqualTo(1);
    }

    // ---- id-generation upserts -------------------------------------------

    @Test
    void upsertVdmsDeviceCustomFields_nullId_generatesId() {
        VdmsDetailsDTO dto = mock(VdmsDetailsDTO.class);
        when(dto.getId()).thenReturn(null);

        service.upsertVdmsDeviceCustomFields(dto);

        verify(dto).setId(any());
        verify(vdmsDetailsRepository).upsertVdmsDeviceCustomFields(any(), any(), any());
    }

    @Test
    void updateVdmsLayoutData_noExistingId_generatesId() {
        when(vdmsDetailsRepository.getVdmsDetailsId()).thenReturn(null);
        VdmsDetailsDTO dto = mock(VdmsDetailsDTO.class);

        service.updateVdmsLayoutData("v1", dto);

        verify(dto).setId(any());
        verify(vdmsDetailsRepository).upsertVdmsLayoutData(any(), any(), eq("v1"));
    }

    @Test
    void updateVdmsLayoutData_existingId_reusesId() {
        when(vdmsDetailsRepository.getVdmsDetailsId()).thenReturn("x1");
        VdmsDetailsDTO dto = mock(VdmsDetailsDTO.class);

        service.updateVdmsLayoutData("v1", dto);

        verify(dto).setId("x1");
    }

    // ---- upsertDeviceCustomFields branches -------------------------------

    @Test
    void upsertDeviceCustomFields_nullList_doesNothing() {
        service.upsertDeviceCustomFields("u", "v1", null);
        verify(vdmsDetailsRepository, never()).upsertVdmsDeviceCustomFields(any(), any(), any());
    }

    @Test
    void upsertDeviceCustomFields_noExistingDetails_createsAndUpserts() {
        when(vdmsDetailsRepository.getVdmsDeviceCustomFields()).thenReturn(null);
        JSONArray list = new JSONArray();
        JSONObject col = new JSONObject();
        col.put("column", "extra");
        list.add(col);

        service.upsertDeviceCustomFields("u", "v1", list);

        verify(vdmsDetailsRepository).upsertVdmsDeviceCustomFields(any(), any(), eq("v1"));
    }

    @Test
    void upsertDeviceCustomFields_existingDetails_mergesAndUpserts() {
        VdmsDetailsDTO existing = mock(VdmsDetailsDTO.class);
        lenient().when(existing.getId()).thenReturn("d1");
        when(existing.getDevice_custom_fields()).thenReturn("[{\"column\":\"name\"}]");
        when(vdmsDetailsRepository.getVdmsDeviceCustomFields()).thenReturn(existing);

        JSONArray list = new JSONArray();
        JSONObject col = new JSONObject();
        col.put("column", "extra");
        list.add(col);

        service.upsertDeviceCustomFields("u", "v1", list);

        verify(existing).setDevice_custom_fields(any());
        verify(vdmsDetailsRepository).upsertVdmsDeviceCustomFields(any(), any(), any());
    }
}
