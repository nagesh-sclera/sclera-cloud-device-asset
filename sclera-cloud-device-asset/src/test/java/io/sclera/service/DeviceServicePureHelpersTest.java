package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Coverage for the pure, side-effect-free string/parse helpers of DeviceService. These touch none of
 * the @Autowired collaborators, so they run on a bare instance; the private ones are reached via
 * ReflectionTestUtils.
 */
class DeviceServicePureHelpersTest {

    private final DeviceService service = new DeviceService();

    // ---- public helpers ----

    @Test
    void getImageExtensionByImageUrl_returnsExtension() {
        assertThat(service.getImageExtensionByImageUrl("http://host/path/asset.png")).isEqualTo("png");
    }

    @Test
    void removeUnderscores_stripsAllUnderscores() {
        assertThat(service.removeUnderscores("a_b__c")).isEqualTo("abc");
    }

    @Test
    void getValueOrDefault_blankOrNull_returnsDash() {
        assertThat(service.getValueOrDefault(null)).isEqualTo(" - ");
        assertThat(service.getValueOrDefault("")).isEqualTo(" - ");
        assertThat(service.getValueOrDefault("x")).isEqualTo("x");
    }

    @Test
    void convertUnderscoresToSpaces_handlesNullAndReplaces() {
        assertThat(service.convertUnderscoresToSpaces(null)).isNull();
        assertThat(service.convertUnderscoresToSpaces("a_b_c")).isEqualTo("a b c");
    }

    // ---- private helpers via reflection ----

    private Object invoke(String name, Object... args) {
        return ReflectionTestUtils.invokeMethod(service, name, args);
    }

    @Test
    void parseObjectSafe_validAndInvalid() {
        JSONObject ok = (JSONObject) invoke("parseObjectSafe", "{\"k\":\"v\"}");
        assertThat(ok.getString("k")).isEqualTo("v");
        JSONObject bad = (JSONObject) invoke("parseObjectSafe", "not-json");
        assertThat(bad).isEmpty();
    }

    @Test
    void parseLastSeenOn_parsesOnlyValidNumbers() {
        assertThat((BigInteger) invoke("parseLastSeenOn", new Object[]{(String) null})).isNull();
        assertThat((BigInteger) invoke("parseLastSeenOn", "")).isNull();
        assertThat((BigInteger) invoke("parseLastSeenOn", "null")).isNull();
        assertThat((BigInteger) invoke("parseLastSeenOn", "abc")).isNull();
        assertThat((BigInteger) invoke("parseLastSeenOn", " 123 ")).isEqualTo(BigInteger.valueOf(123));
    }

    @Test
    void flagLabel_mapsTruthyFalsyAndPassthrough() {
        assertThat(invoke("flagLabel", new Object[]{(String) null})).isEqualTo("");
        assertThat(invoke("flagLabel", "1")).isEqualTo("Yes");
        assertThat(invoke("flagLabel", "true")).isEqualTo("Yes");
        assertThat(invoke("flagLabel", "0")).isEqualTo("No");
        assertThat(invoke("flagLabel", "false")).isEqualTo("No");
        assertThat(invoke("flagLabel", "maybe")).isEqualTo("maybe");
    }

    @Test
    void imageExtensionForMime_mapsMimeToExtension() {
        assertThat(invoke("imageExtensionForMime", "image/jpeg;base64")).isEqualTo("jpg");
        assertThat(invoke("imageExtensionForMime", "image/webp")).isEqualTo("webp");
        assertThat(invoke("imageExtensionForMime", "image/gif")).isEqualTo("gif");
        assertThat(invoke("imageExtensionForMime", "image/svg+xml")).isEqualTo("svg");
        assertThat(invoke("imageExtensionForMime", "image/png")).isEqualTo("png");
        assertThat(invoke("imageExtensionForMime", new Object[]{(String) null})).isEqualTo("png");
    }
}
