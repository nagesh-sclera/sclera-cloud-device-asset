package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coverage for the pure SQL-fragment generators in DeviceSearchService that build feature/device-id
 * filter clauses. These are private and side-effect free (no JdbcTemplate / collaborators), so they
 * are exercised directly on a bare instance via ReflectionTestUtils.
 */
class DeviceSearchServiceFeatureFilterTest {

    private final DeviceSearchService service = new DeviceSearchService();

    private String featureFilter(JSONArray features) {
        return (String) ReflectionTestUtils.invokeMethod(
                service, "generateFeatureFilterQuery", features, "v1");
    }

    private JSONArray feature(String name, String condition) {
        JSONObject f = new JSONObject();
        f.put("name", name);
        f.put("condition", condition);
        JSONArray arr = new JSONArray();
        arr.add(f);
        return arr;
    }

    @Test
    void qrcode_present_and_notPresent() {
        assertThat(featureFilter(feature("qrcode", "is_present")))
                .contains("qc.device_id IS NOT NULL OR cqc.device_id IS NOT NULL");
        assertThat(featureFilter(feature("qrcode", "is_not_present")))
                .contains("qc.device_id IS NULL AND cqc.device_id IS NULL");
    }

    @Test
    void barcode_present_and_notPresent() {
        assertThat(featureFilter(feature("barcode", "is_present"))).contains("cbc.device_id IS NOT NULL");
        assertThat(featureFilter(feature("barcode", "is_not_present"))).contains("cbc.device_id IS NULL");
    }

    @Test
    void adc_present_and_notPresent() {
        assertThat(featureFilter(feature("adc", "is_present"))).contains("d.source_type = 'adc'");
        assertThat(featureFilter(feature("adc", "is_not_present"))).contains("d.source_type = 'vdms'");
    }

    @Test
    void nfc_present_and_notPresent() {
        assertThat(featureFilter(feature("nfc", "is_present")))
                .contains("nfc.device_id IS NOT NULL OR cnfc.device_id IS NOT NULL");
        assertThat(featureFilter(feature("nfc", "is_not_present")))
                .contains("nfc.device_id IS NULL AND cnfc.device_id IS NULL");
    }

    @Test
    void countBackedFeatures_present_and_notPresent() {
        assertThat(featureFilter(feature("record_checklist", "is_present"))).contains("d.record_checklist_count>0");
        assertThat(featureFilter(feature("record_checklist", "is_not_present"))).contains("record_checklist_count=0");
        assertThat(featureFilter(feature("document", "is_present"))).contains("d.document_count>0");
        assertThat(featureFilter(feature("document", "is_not_present"))).contains("document_count=0");
        assertThat(featureFilter(feature("measuring_instrument", "is_present"))).contains("d.measuring_instrument_count>0");
        assertThat(featureFilter(feature("measuring_instrument", "is_not_present"))).contains("measuring_instrument_count=0");
    }

    @Test
    void assetImageUrl_present_and_notPresent() {
        assertThat(featureFilter(feature("asset_image_url", "is_present")))
                .contains("d.asset_image_url IS NOT NULL AND d.asset_image_url <> '[]'");
        assertThat(featureFilter(feature("asset_image_url", "is_not_present")))
                .contains("d.asset_image_url IS NULL OR d.asset_image_url = '[]'");
    }

    @Test
    void sensorAlert_present_and_notPresent() {
        assertThat(featureFilter(feature("sensor_alert", "is_present"))).contains("d.monnit_status='alert'");
        assertThat(featureFilter(feature("sensor_alert", "is_not_present")))
                .contains("d.monnit_status IS NULL OR d.monnit_status<>'alert'");
    }

    @Test
    void statusFeatures_mapAllFourCodes() {
        for (String name : new String[]{"geolocation_status", "image_status", "field_status", "tag_status"}) {
            String col = name; // dos.<name>
            assertThat(featureFilter(feature(name, "is_not_present"))).contains("dos." + col + " = 0");
            assertThat(featureFilter(feature(name, "is_present"))).contains("dos." + col + " = 1");
            assertThat(featureFilter(feature(name, "retag"))).contains("dos." + col + " = 2");
            assertThat(featureFilter(feature(name, "not_added_exception"))).contains("dos." + col + " = 3");
        }
    }

    @Test
    void multipleFeatures_concatenated() {
        JSONArray features = new JSONArray();
        JSONObject a = new JSONObject(); a.put("name", "qrcode"); a.put("condition", "is_present");
        JSONObject b = new JSONObject(); b.put("name", "adc"); b.put("condition", "is_present");
        features.add(a);
        features.add(b);
        String q = featureFilter(features);
        assertThat(q).contains("qc.device_id IS NOT NULL").contains("d.source_type = 'adc'");
    }

    @Test
    void emptyFeatures_returnsEmpty() {
        assertThat(featureFilter(new JSONArray())).isEmpty();
    }

    // ---- generateDeviceIdsFilterCustomQuery ----

    private String deviceIdsFilter(JSONObject details) {
        return (String) ReflectionTestUtils.invokeMethod(
                service, "generateDeviceIdsFilterCustomQuery", details);
    }

    @Test
    void deviceIdsFilter_buildsInClause() {
        JSONObject details = new JSONObject();
        JSONArray ids = new JSONArray();
        ids.add("d1");
        ids.add("d2");
        details.put("device_ids", ids);
        assertThat(deviceIdsFilter(details)).isEqualTo(" AND (d.id IN (\"d1\",\"d2\"))");
    }

    @Test
    void deviceIdsFilter_noIds_returnsEmpty() {
        assertThat(deviceIdsFilter(new JSONObject())).isEmpty();
    }
}
