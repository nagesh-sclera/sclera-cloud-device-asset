package io.sclera.it;

import io.sclera.Repository.DeviceConditionsRepository;
import io.sclera.dto.DeviceConditionsDTO;
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
@Sql(scripts = "/seed/device-conditions-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-conditions-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceConditionsRepositoryIT extends PostgresJpaIT {

    @Autowired
    DeviceConditionsRepository deviceConditionsRepository;

    @PersistenceContext
    EntityManager em;

    // ---- getDeviceConditions ----

    @Test
    void getDeviceConditions_excludesAiCallAlert() {
        Set<DeviceConditionsDTO> result = deviceConditionsRepository.getDeviceConditions("dev-dc-001");
        assertThat(result).hasSize(1);
        DeviceConditionsDTO dto = result.iterator().next();
        assertThat(dto.getId()).isEqualTo("dc-001");
        assertThat(dto.getAlert_condition()).isEqualTo("device_offline");
        assertThat(dto.getDevice_id()).isEqualTo("dev-dc-001");
        assertThat(dto.getPriority()).isEqualTo("high");
        assertThat(dto.getAlert_count_enabled()).isEqualTo(0);
    }

    // ---- getDeviceConditionsById ----

    @Test
    void getDeviceConditionsById_excludesAiCallAndReturnsDto() {
        DeviceConditionsDTO dto = deviceConditionsRepository.getDeviceConditionsById("dc-001");
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("dc-001");
        assertThat(dto.getAlert_condition()).isEqualTo("device_offline");
    }

    @Test
    void getDeviceConditionsById_returnsNullForAiCallRow() {
        // dc-002 is an AI-call alert — getDeviceConditionsById filters it out
        DeviceConditionsDTO dto = deviceConditionsRepository.getDeviceConditionsById("dc-002");
        assertThat(dto).isNull();
    }

    // ---- getDeviceConditionsForAiCall ----

    @Test
    void getDeviceConditionsForAiCall_returnsAiCallRow() {
        Set<DeviceConditionsDTO> result = deviceConditionsRepository.getDeviceConditionsForAiCall("dev-dc-001");
        assertThat(result).hasSize(1);
        DeviceConditionsDTO dto = result.iterator().next();
        assertThat(dto.getId()).isEqualTo("dc-002");
        assertThat(dto.getAlert_condition()).isEqualTo("device_offline_ai_call_alert");
        assertThat(dto.getAlert_count()).isEqualTo(1);
    }

    // ---- getDeviceConditionsByIdForAiCall ----

    @Test
    void getDeviceConditionsByIdForAiCall_returnsAiCallRow() {
        DeviceConditionsDTO dto = deviceConditionsRepository.getDeviceConditionsByIdForAiCall("dc-002");
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo("dc-002");
        assertThat(dto.getAlert_condition()).isEqualTo("device_offline_ai_call_alert");
    }

    // ---- getAlertCount ----

    @Test
    void getAlertCount_returnsAlertCountForAiCallCondition() {
        Integer count = deviceConditionsRepository.getAlertCount("dev-dc-001");
        assertThat(count).isEqualTo(1);
    }

    // ---- getDeviceConditionIdByDeviceId ----

    @Test
    void getDeviceConditionIdByDeviceId_returnsAiCallConditionId() {
        String id = deviceConditionsRepository.getDeviceConditionIdByDeviceId("dev-dc-001");
        assertThat(id).isEqualTo("dc-002");
    }

    // ---- getLastAlertedTimeByDeviceId ----

    @Test
    void getLastAlertedTimeByDeviceId_returnsNullWhenNotSet() {
        BigInteger ts = deviceConditionsRepository.getLastAlertedTimeByDeviceId("dev-dc-001");
        assertThat(ts).isNull();
    }

    // ---- updateLastAlertedDetails ----

    @Test
    void updateLastAlertedDetails_updatesFields() {
        deviceConditionsRepository.updateLastAlertedDetails(
                "dc-002", BigInteger.valueOf(9988776655L), true, 3);
        em.flush();
        em.clear();

        Object[] row = (Object[]) em.createQuery(
                "SELECT dc.last_alerted_time, dc.last_alerted, dc.alert_count FROM DeviceConditions dc WHERE dc.id = 'dc-002'")
                .getSingleResult();
        assertThat(row[0]).isEqualTo(BigInteger.valueOf(9988776655L));
        assertThat((Boolean) row[1]).isTrue();
        assertThat((Integer) row[2]).isEqualTo(3);
    }

    // ---- updateLastAlertedTimestamp ----

    @Test
    void updateLastAlertedTimestamp_clearsTimestamp() {
        // First set a value
        em.createNativeQuery("UPDATE device_conditions SET last_alerted_time = 1234 WHERE id = 'dc-002'").executeUpdate();
        em.flush();
        em.clear();

        deviceConditionsRepository.updateLastAlertedTimestamp("dc-002");
        em.flush();
        em.clear();

        Object ts = em.createQuery(
                "SELECT dc.last_alerted_time FROM DeviceConditions dc WHERE dc.id = 'dc-002'")
                .getSingleResult();
        assertThat(ts).isNull();
    }

    // ---- updateAlertProfileId ----

    @Test
    void updateAlertProfileId_clearsProfileId() {
        // Set an alert_profile_id first
        em.createNativeQuery("UPDATE device_conditions SET alert_profile_id = 'ap-x' WHERE id = 'dc-001'").executeUpdate();
        em.flush();
        em.clear();

        deviceConditionsRepository.updateAlertProfileId("ap-x");
        em.flush();
        em.clear();

        String apId = (String) em.createQuery(
                "SELECT dc.alert_profile_id FROM DeviceConditions dc WHERE dc.id = 'dc-001'")
                .getSingleResult();
        assertThat(apId).isNull();
    }

    // ---- resetDeviceConditions ----

    @Test
    void resetDeviceConditions_resetsCountAndClearsTime() {
        // Set last_alerted_time first
        em.createNativeQuery("UPDATE device_conditions SET last_alerted_time = 555 WHERE id = 'dc-002'").executeUpdate();
        em.flush();
        em.clear();

        deviceConditionsRepository.resetDeviceConditions("dc-002", 0, false);
        em.flush();
        em.clear();

        Object[] row = (Object[]) em.createQuery(
                "SELECT dc.alert_count, dc.last_alerted, dc.last_alerted_time FROM DeviceConditions dc WHERE dc.id = 'dc-002'")
                .getSingleResult();
        assertThat((Integer) row[0]).isZero();
        assertThat((Boolean) row[1]).isFalse();
        assertThat(row[2]).isNull();
    }

    // ---- updateAlertCountByConditionId ----

    @Test
    void updateAlertCountByConditionId_updatesOnlyAiCallRow() {
        deviceConditionsRepository.updateAlertCountByConditionId("dc-002", 5);
        em.flush();
        em.clear();

        Integer count = (Integer) em.createQuery(
                "SELECT dc.alert_count FROM DeviceConditions dc WHERE dc.id = 'dc-002'")
                .getSingleResult();
        assertThat(count).isEqualTo(5);

        // dc-001 is not an AI-call condition — alert_count must not change
        Integer otherCount = (Integer) em.createQuery(
                "SELECT dc.alert_count FROM DeviceConditions dc WHERE dc.id = 'dc-001'")
                .getSingleResult();
        assertThat(otherCount).isZero();
    }
}
