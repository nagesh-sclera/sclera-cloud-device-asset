package io.sclera.it;

import io.sclera.Repository.MeasuringInstrumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigInteger;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the JPQL-converted methods in MeasuringInstrumentRepository.
 * Native-kept methods (JSON, multi-join projections, join-table DML) are not tested here.
 */
@Sql(scripts = "/schema-pg.sql",                          executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/measuring-instrument-pilot.sql",    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-measuring-instrument-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class MeasuringInstrumentRepositoryIT extends PostgresJpaIT {

    @Autowired
    MeasuringInstrumentRepository repo;

    @PersistenceContext
    EntityManager em;

    // ---- deleteInstrumentById ----

    @Test
    void deleteInstrumentById_removesRow() {
        // mi-no-device has no child attributes — safe to delete directly
        repo.deleteInstrumentById("mi-no-device");
        em.flush();
        em.clear();

        Long count = em.createQuery(
                "SELECT COUNT(mi) FROM MeasuringInstrument mi WHERE mi.id = 'mi-no-device'", Long.class)
                .getSingleResult();
        assertThat(count).isZero();
    }

    // ---- updateInstrumentValueById ----

    @Test
    void updateInstrumentValueById_updatesValueAndTimestamp() {
        repo.updateInstrumentValueById("mi-001", "99.9", BigInteger.valueOf(9999999L));
        em.flush();
        em.clear();

        Object[] row = (Object[]) em.createQuery(
                "SELECT mi.value, mi.timestamp FROM MeasuringInstrument mi WHERE mi.id = 'mi-001'")
                .getSingleResult();
        assertThat(row[0]).isEqualTo("99.9");
        assertThat(row[1]).isEqualTo(BigInteger.valueOf(9999999L));
    }

    // ---- syncMeasuringInstrument ----

    @Test
    void syncMeasuringInstrument_updatesAllFields() {
        Integer affected = repo.syncMeasuringInstrument(
                "NewName", "NewDesc", "median", "{\"x\":1}", "{\"p\":2}",
                "newcategory", "55.0", "F", "tagX", BigInteger.valueOf(8888888L),
                "temperature", "mi-001");
        em.flush();
        em.clear();

        assertThat(affected).isEqualTo(1);
        Object[] row = (Object[]) em.createQuery(
                "SELECT mi.name, mi.description, mi.calculation_type, mi.category, mi.value, mi.unit, mi.tags FROM MeasuringInstrument mi WHERE mi.id = 'mi-001'")
                .getSingleResult();
        assertThat(row[0]).isEqualTo("NewName");
        assertThat(row[1]).isEqualTo("NewDesc");
        assertThat(row[2]).isEqualTo("median");
        assertThat(row[3]).isEqualTo("newcategory");
        assertThat(row[4]).isEqualTo("55.0");
        assertThat(row[5]).isEqualTo("F");
        assertThat(row[6]).isEqualTo("tagX");
    }

    // ---- syncMeasuringInstrumentExceptAttributes ----

    @Test
    void syncMeasuringInstrumentExceptAttributes_updatesSubsetOfFields() {
        Integer affected = repo.syncMeasuringInstrumentExceptAttributes(
                "sum", "{\"p\":3}", "newcat2", "K", "tagY",
                "temperature", "mi-001");
        em.flush();
        em.clear();

        assertThat(affected).isEqualTo(1);
        Object[] row = (Object[]) em.createQuery(
                "SELECT mi.calculation_type, mi.category, mi.unit, mi.tags FROM MeasuringInstrument mi WHERE mi.id = 'mi-001'")
                .getSingleResult();
        assertThat(row[0]).isEqualTo("sum");
        assertThat(row[1]).isEqualTo("newcat2");
        assertThat(row[2]).isEqualTo("K");
        assertThat(row[3]).isEqualTo("tagY");
        // attribute should NOT have changed (this method skips it)
        String attr = (String) em.createQuery(
                "SELECT mi.attribute FROM MeasuringInstrument mi WHERE mi.id = 'mi-001'")
                .getSingleResult();
        assertThat(attr).isEqualTo("{}");
    }

    // ---- updateMeasuringInstrumentSensorAlert ----

    @Test
    void updateMeasuringInstrumentSensorAlert_setsAlertTrue() {
        repo.updateMeasuringInstrumentSensorAlert("mi-001", true);
        em.flush();
        em.clear();

        Boolean alert = (Boolean) em.createQuery(
                "SELECT mi.alert FROM MeasuringInstrument mi WHERE mi.id = 'mi-001'")
                .getSingleResult();
        assertThat(alert).isTrue();
    }

    @Test
    void updateMeasuringInstrumentSensorAlert_setsAlertFalse() {
        repo.updateMeasuringInstrumentSensorAlert("mi-003", false);
        em.flush();
        em.clear();

        Boolean alert = (Boolean) em.createQuery(
                "SELECT mi.alert FROM MeasuringInstrument mi WHERE mi.id = 'mi-003'")
                .getSingleResult();
        assertThat(alert).isFalse();
    }

    // ---- getMeasuringInstrumentAlertSensorCount ----

    @Test
    void getMeasuringInstrumentAlertSensorCount_countsAlertingWithMonitoredDevice() {
        // mi-003 has alert=true and device dev-mi-2 (monitor=0) => NOT counted
        // mi-001, mi-002 have alert=false => not counted for alert=true
        // mi-no-device has no device => not counted
        Integer count = repo.getMeasuringInstrumentAlertSensorCount(true);
        assertThat(count).isZero();
    }

    @Test
    void getMeasuringInstrumentAlertSensorCount_countsMonitoredDeviceAlert() {
        // Set mi-001 alert = true; its device has monitor=1 => should be counted
        repo.updateMeasuringInstrumentSensorAlert("mi-001", true);
        em.flush();
        em.clear();

        Integer count = repo.getMeasuringInstrumentAlertSensorCount(true);
        assertThat(count).isEqualTo(1);
    }

    // ---- updateMeasuringinstrumentSensorUserDataValue ----

    @Test
    void updateMeasuringinstrumentSensorUserDataValue_setsValue() {
        repo.updateMeasuringinstrumentSensorUserDataValue("mi-001", "custom-value");
        em.flush();
        em.clear();

        String val = (String) em.createQuery(
                "SELECT mi.user_data_value FROM MeasuringInstrument mi WHERE mi.id = 'mi-001'")
                .getSingleResult();
        assertThat(val).isEqualTo("custom-value");
    }

    // ---- getDeviceIdByMeasuringInstrumentSensorId ----

    @Test
    void getDeviceIdByMeasuringInstrumentSensorId_returnsDeviceId() {
        String deviceId = repo.getDeviceIdByMeasuringInstrumentSensorId("mi-001");
        assertThat(deviceId).isEqualTo("dev-mi-1");
    }

    @Test
    void getDeviceIdByMeasuringInstrumentSensorId_returnsNullWhenNoDevice() {
        String deviceId = repo.getDeviceIdByMeasuringInstrumentSensorId("mi-no-device");
        assertThat(deviceId).isNull();
    }

    // ---- getMeasuringInstrumentSensorCurrentValue ----

    @Test
    void getMeasuringInstrumentSensorCurrentValue_returnsValue() {
        String val = repo.getMeasuringInstrumentSensorCurrentValue("mi-002");
        assertThat(val).isEqualTo("60");
    }

    // ---- updateInstrumentValueAndAttributeById ----

    @Test
    void updateInstrumentValueAndAttributeById_updatesThreeFields() {
        repo.updateInstrumentValueAndAttributeById("mi-001", "33.0", BigInteger.valueOf(7777777L), "{\"k\":\"v\"}");
        em.flush();
        em.clear();

        Object[] row = (Object[]) em.createQuery(
                "SELECT mi.value, mi.timestamp, mi.attribute FROM MeasuringInstrument mi WHERE mi.id = 'mi-001'")
                .getSingleResult();
        assertThat(row[0]).isEqualTo("33.0");
        assertThat(row[1]).isEqualTo(BigInteger.valueOf(7777777L));
        assertThat(row[2]).isEqualTo("{\"k\":\"v\"}");
    }

    // ---- updateInstrumentAttributeById ----

    @Test
    void updateInstrumentAttributeById_updatesAttributeField() {
        repo.updateInstrumentAttributeById("mi-001", "{\"new\":\"attr\"}");
        em.flush();
        em.clear();

        String attr = (String) em.createQuery(
                "SELECT mi.attribute FROM MeasuringInstrument mi WHERE mi.id = 'mi-001'")
                .getSingleResult();
        assertThat(attr).isEqualTo("{\"new\":\"attr\"}");
    }

    // ---- deleteDigitalTwinPositions ----

    @Test
    void deleteDigitalTwinPositions_setsNullForDevice() {
        // First set a position
        em.createNativeQuery("UPDATE measuring_instrument SET digital_twin_position = 'pos-1' WHERE id = 'mi-001'")
                .executeUpdate();
        em.flush();
        em.clear();

        repo.deleteDigitalTwinPositions("dev-mi-1");
        em.flush();
        em.clear();

        String pos = (String) em.createQuery(
                "SELECT mi.digital_twin_position FROM MeasuringInstrument mi WHERE mi.id = 'mi-001'")
                .getSingleResult();
        assertThat(pos).isNull();
    }

    // ---- updateScaleTypeAndSensorTypeById ----

    @Test
    void updateScaleTypeAndSensorTypeById_updatesThreeFields() {
        repo.updateScaleTypeAndSensorTypeById("logarithmic", "vibration", "structural", "mi-001");
        em.flush();
        em.clear();

        Object[] row = (Object[]) em.createQuery(
                "SELECT mi.scale_type, mi.sensor_type, mi.sub_category FROM MeasuringInstrument mi WHERE mi.id = 'mi-001'")
                .getSingleResult();
        assertThat(row[0]).isEqualTo("logarithmic");
        assertThat(row[1]).isEqualTo("vibration");
        assertThat(row[2]).isEqualTo("structural");
    }

    // ---- getMeasuringInstrumentCountByType ----

    @Test
    void getMeasuringInstrumentCountByType_countsByType() {
        Integer count = repo.getMeasuringInstrumentCountByType("temperature");
        assertThat(count).isEqualTo(1);
    }

    @Test
    void getMeasuringInstrumentCountByType_allReturnsTotal() {
        Integer count = repo.getMeasuringInstrumentCountByType("all");
        assertThat(count).isEqualTo(4);
    }

    // ---- getTotalManualAttributesCountofMeasuringInstruments ----

    @Test
    void getTotalManualAttributesCountofMeasuringInstruments_countsManualType() {
        int count = repo.getTotalManualAttributesCountofMeasuringInstruments();
        // mia-001 and mia-002 have type='manual'; mia-003 is 'auto'
        assertThat(count).isEqualTo(2);
    }

    // ---- getMeasuringInstrumentIdsByProtocolAndPrimaryIds ----

    @Test
    void getMeasuringInstrumentIdsByProtocolAndPrimaryIds_returnsMatchingIds() {
        Set<String> ids = repo.getMeasuringInstrumentIdsByProtocolAndPrimaryIds(
                "bacnet", Set.of("pid-1", "pid-2", "nonexistent"));
        // mia-001 and mia-002 are bacnet; mia-003 is lorawan
        assertThat(ids).containsExactlyInAnyOrder("mi-001", "mi-002");
    }

    @Test
    void getMeasuringInstrumentIdsByProtocolAndPrimaryIds_returnsEmptyForUnknownProtocol() {
        Set<String> ids = repo.getMeasuringInstrumentIdsByProtocolAndPrimaryIds(
                "zigbee", Set.of("pid-1"));
        assertThat(ids).isEmpty();
    }
}
