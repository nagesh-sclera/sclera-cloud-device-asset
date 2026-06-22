package io.sclera.utils;

import com.alibaba.fastjson.JSONArray;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.net.InetAddress;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit coverage for the pure (infra-free) helpers of Utils: network-address and macvlan
 * naming, netmask/CIDR conversion, string/filename normalisation, JSON (de)serialisation,
 * timezone-aware date formatting, percentage and timestamp helpers.
 */
class UtilsTest {

    private final Utils utils = new Utils();

    @Test
    void generateNetworkAddress_zeroesHostAndAppendsCidr() {
        assertEquals("192.168.1.0/24", utils.generateNetworkAddress("192.168.1.50", 24));
    }

    @Test
    void generateMacvlanName_joinsWithVlanOrReturnsInterface() {
        assertEquals("eth0_10", utils.generateMacvlanName("eth0", 10));
        assertEquals("eth0", utils.generateMacvlanName("eth0", null));
    }

    @Test
    void generateInternalIpSlave_returnsFirstFreeAddress() {
        List<Integer> used = new ArrayList<>(List.of(200));
        // loop starts at 131; 131 != 200 so the first free address is returned
        assertEquals("10.255.255.131", utils.generateInternalIpSlave(used));
    }

    @Test
    void convertNetmaskToCIDR_countsPrefixBits() throws Exception {
        assertEquals(24, utils.convertNetmaskToCIDR(InetAddress.getByName("255.255.255.0")));
        assertEquals(16, utils.convertNetmaskToCIDR(InetAddress.getByName("255.255.0.0")));
    }

    @Test
    void convertNetmaskToCIDR_rejectsNonContiguousMask() throws Exception {
        InetAddress bad = InetAddress.getByName("255.0.255.0");
        assertThrows(IllegalArgumentException.class, () -> utils.convertNetmaskToCIDR(bad));
    }

    @Test
    void fileExtensionHelpers_extractAfterLastDot() {
        assertEquals("png", utils.getFileExtensionByFileUrl("http://x/a/b.png"));
        assertEquals("PDF", utils.getExtensionByUrl("http://x/y.PDF"));
    }

    @Test
    void getFileNameHelpers_splitOnSlashAndDot() {
        assertEquals("file.png", utils.getFileNameByImageUrl("http://x/y/file.png"));
        assertEquals("photo", utils.getFileNameByImageName("photo.png"));
    }

    @Test
    void convertNumericValToString_stripsTrailingZeros() {
        assertEquals("5.5", utils.convertNumericValToString(5.50));
        assertEquals("3", utils.convertNumericValToString(3.0));
    }

    @Test
    void convertDegreeCelciusToFarenheit_convertsAndRounds() {
        assertEquals(212.0, utils.convertDegreeCelciusToFarenheit(100.0), 0.001);
        assertEquals(32.0, utils.convertDegreeCelciusToFarenheit(0.0), 0.001);
    }

    @Test
    void replaceSpecialCharactersWithUnderscore_collapsesAndTrims() {
        assertEquals("a_b_c", utils.replaceSpecialCharactersWithUnderscore("a b@c!"));
    }

    @Test
    void replaceSlashCharactersWithHyphen_replacesSlashes() {
        assertEquals("a-b-c", utils.replaceSlashCharactersWithHyphen("a/b/c"));
    }

    @Test
    void replaceSpecialCasesInFilename_replacesDisallowedChars() {
        assertEquals("Report_2024_pdf", utils.replaceSpecialCasesInFilename("Report@2024.pdf"));
    }

    @Test
    void compareIds_isCaseInsensitiveMembership() {
        assertTrue(utils.compareIds(Set.of("AbC", "xyz"), "abc"));
        assertFalse(utils.compareIds(Set.of("AbC", "xyz"), "nope"));
    }

    @Test
    void getJSONArrayFromJSONString_parsesListOrReturnsNull() {
        List<String> parsed = utils.getJSONArrayFromJSONString("[\"a\",\"b\"]", String.class);
        assertEquals(List.of("a", "b"), parsed);
        assertNull(utils.getJSONArrayFromJSONString("not-json", String.class));
    }

    @Test
    void getCurrentDateByTimezone_formatsOrDash() {
        assertEquals("-", utils.getCurrentDateByTimezone(null, "UTC"));
        assertEquals("1970-01-01", utils.getCurrentDateByTimezone(BigInteger.ZERO, "UTC"));
    }

    @Test
    void extractLastFourDigits_stripsAndLowercases() {
        assertEquals("eeff", utils.extractLastFourDigitsAndConvertToLowercase("AA:BB:CC:DD:EE:FF"));
        assertThrows(IllegalArgumentException.class,
                () -> utils.extractLastFourDigitsAndConvertToLowercase("12"));
    }

    @Test
    void convertSnakeCaseToTitleCase_titleCasesAndUppercasesId() {
        assertEquals("Device ID", utils.convertSnakeCaseToTitleCase("device_id"));
        assertEquals("Hello World", utils.convertSnakeCaseToTitleCase("hello_world"));
    }

    @Test
    void replaceUnderscoresAndCapitalize_capitalisesEachWord() {
        assertEquals("Hello World", utils.replaceUnderscoresAndCapitalize("hello_world"));
    }

    @Test
    void getRestrictedRoles_listsTheFourRoles() {
        List<String> roles = utils.getRestrictedRoles();
        assertEquals(4, roles.size());
        assertTrue(roles.contains("admin"));
        assertTrue(roles.contains("super-admin"));
    }

    @Test
    void convertToKeyValuePairs_splitsOnFirstSpace() {
        var map = utils.convertToKeyValuePairs("Asia/Kolkata (UTC+05:30)\nUTC (UTC+00:00)");
        assertEquals("(UTC+05:30)", map.get("Asia/Kolkata"));
        assertEquals("(UTC+00:00)", map.get("UTC"));
    }

    @Test
    void getPercentage_clampsToZeroHundred() {
        assertEquals("50", utils.getPercentage(50, 0, 100));
        assertEquals("100", utils.getPercentage(200, 0, 100));
        assertEquals("0", utils.getPercentage(0, 10, 20));
    }

    @Test
    void getMaxTimeStamp_handlesNullsAndPicksMax() {
        assertNull(Utils.getMaxTimeStamp(null, null));
        assertEquals(BigInteger.TEN, Utils.getMaxTimeStamp(null, BigInteger.TEN));
        assertEquals(BigInteger.TEN, Utils.getMaxTimeStamp(BigInteger.TEN, null));
        assertEquals(BigInteger.valueOf(9), Utils.getMaxTimeStamp(BigInteger.valueOf(5), BigInteger.valueOf(9)));
    }

    @Test
    void isLessThan10Minutes_comparesAgainstNow() {
        assertTrue(Utils.isLessThan10Minutes(BigInteger.valueOf(System.currentTimeMillis())));
        assertFalse(Utils.isLessThan10Minutes(BigInteger.ZERO));
    }

    @Test
    void combineJSONArrays_unionsAsSet() {
        JSONArray a = new JSONArray();
        a.add("1");
        a.add("2");
        JSONArray b = new JSONArray();
        b.add("2");
        b.add("3");
        Set<String> combined = utils.combineJSONArrays(a, b);
        assertEquals(Set.of("1", "2", "3"), combined);
    }

    @Test
    void generateTimestampOfStartOftheDay_isMidnightInZone() {
        BigInteger result = utils.generateTimestampOfStartOftheDay("UTC", "January 15, 2024");
        long expected = LocalDate.of(2024, 1, 15)
                .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();
        assertEquals(BigInteger.valueOf(expected), result);
    }
}
