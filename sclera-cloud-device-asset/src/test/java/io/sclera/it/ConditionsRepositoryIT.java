package io.sclera.it;

import io.sclera.Repository.ConditionsRepository;
import io.sclera.dto.ConditionsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigInteger;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/conditions-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-conditions-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class ConditionsRepositoryIT extends PostgresJpaIT {

    @Autowired
    ConditionsRepository conditionsRepository;

    @PersistenceContext
    EntityManager em;

    // ---- getConditions ----

    @Test
    void getConditions_byMeasuringInstrumentId_returnsRow() {
        Set<ConditionsDTO> result = conditionsRepository.getConditions(
                null, null,       // bacnet_object_id, bacnet_device_id
                null,             // lorawan_sensor_id
                null,             // snmp_device_id
                null,             // disruptive_sensor_id
                null,             // my_devices_sensor_id
                null,             // monnit_sensor_id
                null,             // pelican_sensor_id
                null,             // knx_group_address
                null,             // knx_device_address
                null, null,       // snmp_device_configuration_id, snmp_object_oid
                "mi-001",         // measuring_instrument_id — matches cond-001
                null,             // daintree_device_id
                null,             // ecobee_sensor_id
                null              // modbus_register_id
        );
        assertThat(result).hasSize(1);
        ConditionsDTO dto = result.iterator().next();
        assertThat(dto.getId()).isEqualTo("cond-001");
        assertThat(dto.getName()).isEqualTo("TempHigh");
        assertThat(dto.getMeasuring_instrument_id()).isEqualTo("mi-001");
        assertThat(dto.getPriority()).isEqualTo("high");
        assertThat(dto.getAlert_count_enabled()).isEqualTo(1);
        assertThat(dto.getEnable_threshold_line_onchart()).isEqualTo(1);
        assertThat(dto.getColor_of_threshold_line_onchart()).isEqualTo("#FF0000");
    }

    @Test
    void getConditions_byLorawanSensorId_returnsRow() {
        Set<ConditionsDTO> result = conditionsRepository.getConditions(
                null, null,
                "lorawan-sensor-001", // lorawan_sensor_id — matches cond-002
                null, null, null, null, null, null, null,
                null, null,
                null, null, null, null
        );
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getId()).isEqualTo("cond-002");
    }

    // ---- getConditionsById ----

    @Test
    void getConditionsById_byLorawanIdAndName_returnsRow() {
        Set<ConditionsDTO> result = conditionsRepository.getConditionsById(
                null, null,                           // bacnet_device_id, bacnet_object_id
                "lorawan-sensor-001", "temp",         // lorawan + attr name — matches cond-002
                null, null, null, null,               // snmp, disruptive, mydevices id+name
                null,                                 // monnit
                null, null,                           // pelican id+name
                null, null,                           // knx address + device address
                null, null,                           // snmp config + oid
                null,                                 // measuring_instrument_id
                null, null,                           // daintree id + point
                null, null,                           // ecobee id + name
                null                                  // modbus
        );
        assertThat(result).hasSize(1);
        assertThat(result.iterator().next().getId()).isEqualTo("cond-002");
    }

    // ---- getConditionByConditionId ----

    @Test
    void getConditionByConditionId_returnsFullDto() {
        ConditionsDTO dto = conditionsRepository.getConditionByConditionId("cond-001");
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("cond-001");
        assertThat(dto.getValue()).isEqualTo("75");
        assertThat(dto.getAlert_profile_id()).isEqualTo("ap-001");
        assertThat(dto.getLast_alerted_timestamp()).isEqualTo(BigInteger.valueOf(999000000L));
    }

    // ---- getConditionAlertCountDetails ----

    @Test
    void getConditionAlertCountDetails_returnsCountFields() {
        ConditionsDTO dto = conditionsRepository.getConditionAlertCountDetails("cond-001");
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("cond-001");
        assertThat(dto.getAlert_count_enabled()).isEqualTo(1);
        assertThat(dto.getMax_alert_count()).isEqualTo(5);
        assertThat(dto.getAlert_count()).isEqualTo(2);
        assertThat(dto.getAlert_time()).isEqualTo(30);
    }

    // ---- updateConditionAlert ----

    @Test
    void updateConditionAlert_updatesAlertFields() {
        conditionsRepository.updateConditionAlert("cond-001", true, 3, BigInteger.valueOf(1234567890L), true);
        em.flush();
        em.clear();

        // Verify via scalar read to avoid loading associations
        Object[] row = (Object[]) em.createQuery(
                "SELECT c.alert, c.alert_count, c.last_alerted_timestamp, c.last_alerted FROM Conditions c WHERE c.id = 'cond-001'")
                .getSingleResult();
        assertThat((Boolean) row[0]).isTrue();
        assertThat((Integer) row[1]).isEqualTo(3);
        assertThat(row[2]).isEqualTo(BigInteger.valueOf(1234567890L));
        assertThat((Boolean) row[3]).isTrue();
    }

    // ---- updateConditionAlertCount ----

    @Test
    void updateConditionAlertCount_updatesCount() {
        conditionsRepository.updateConditionAlertCount("cond-001", 7);
        em.flush();
        em.clear();

        Integer count = (Integer) em.createQuery(
                "SELECT c.alert_count FROM Conditions c WHERE c.id = 'cond-001'")
                .getSingleResult();
        assertThat(count).isEqualTo(7);
    }

    // ---- updateAlertProfileId ----

    @Test
    void updateAlertProfileId_clearsProfileId() {
        conditionsRepository.updateAlertProfileId("ap-001");
        em.flush();
        em.clear();

        String apId = (String) em.createQuery(
                "SELECT c.alert_profile_id FROM Conditions c WHERE c.id = 'cond-001'")
                .getSingleResult();
        assertThat(apId).isNull();
    }

    // ---- resetLastAlertById ----

    @Test
    void resetLastAlertById_setsFlag() {
        conditionsRepository.resetLastAlertById("cond-001", true);
        em.flush();
        em.clear();

        Boolean val = (Boolean) em.createQuery(
                "SELECT c.last_alerted FROM Conditions c WHERE c.id = 'cond-001'")
                .getSingleResult();
        assertThat(val).isTrue();
    }

    // ---- updateLastAlertedTimestamp ----

    @Test
    void updateLastAlertedTimestamp_clearsTimestamp() {
        conditionsRepository.updateLastAlertedTimestamp("cond-001");
        em.flush();
        em.clear();

        Object ts = em.createQuery(
                "SELECT c.last_alerted_timestamp FROM Conditions c WHERE c.id = 'cond-001'")
                .getSingleResult();
        assertThat(ts).isNull();
    }

    // ---- deleteConditionById ----

    @Test
    void deleteConditionById_removesRow() {
        conditionsRepository.deleteConditionById("cond-002");
        em.flush();
        em.clear();

        Long count = (Long) em.createQuery(
                "SELECT COUNT(c) FROM Conditions c WHERE c.id = 'cond-002'")
                .getSingleResult();
        assertThat(count).isZero();
    }

    // ---- updateCondition (bulk JPQL UPDATE with 43 params) ----

    @Test
    void updateCondition_updatesAllFields() {
        conditionsRepository.updateCondition(
                "cond-001",      // id
                "TempUpdated",   // name
                "90",            // value
                "95",            // second_value
                "Updated msg",   // alert_message
                "07:00",         // start_time
                "21:00",         // end_time
                1,               // schedule
                null,            // schedule_conditions
                10,              // max_alert_count
                1,               // alert_count_enabled
                0,               // alert_count
                "above",         // alert_condition
                true,            // show_alert
                false,           // show_alert_message_as_value
                null, null,      // bacnet_device_id, bacnet_object_id
                null, null,      // lorawan_sensor_id, lorawan_sensor_attributes_name
                null,            // snmp_device_id
                null,            // disruptive_sensor_id
                null, null,      // my_devices_sensor_id, my_devices_sensor_attributes_name
                null,            // monnit_sensor_id
                null, null,      // pelican_sensor_id, pelican_sensor_attributes_name
                null, null,      // knx_group_address, knx_device_address
                null, null,      // snmp_device_configuration_id, snmp_object_oid
                "mi-002",        // measuring_instrument_id
                60,              // alert_time
                null, null,      // daintree_device_id, daintree_point_id
                "ap-002",        // alert_profile_id
                null, null,      // ecobee_sensor_id, ecobee_sensor_attributes_name
                null,            // modbus_register_id
                "medium",        // priority
                false,           // last_alerted
                120,             // alert_count_time
                0,               // enable_threshold_line_onchart
                null             // color_of_threshold_line_onchart
        );
        em.flush();
        em.clear();

        Object[] row = (Object[]) em.createQuery(
                "SELECT c.name, c.value, c.measuring_instrument_id, c.priority FROM Conditions c WHERE c.id = 'cond-001'")
                .getSingleResult();
        assertThat(row[0]).isEqualTo("TempUpdated");
        assertThat(row[1]).isEqualTo("90");
        assertThat(row[2]).isEqualTo("mi-002");
        assertThat(row[3]).isEqualTo("medium");
    }
}
