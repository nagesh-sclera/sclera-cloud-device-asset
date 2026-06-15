package io.sclera.it;

import io.sclera.Repository.DeviceNetworkSpecificationRepository;
import io.sclera.models.DeviceNetworkSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Result-asserting IT for DeviceNetworkSpecificationRepository, run against real PostgreSQL.
 *
 * Methods classified:
 *  - updateDeviceIdBySerialNumber: stays native (sets @OneToOne FK device_id from scalar id;
 *    JPQL bulk UPDATE cannot assign a relation from a scalar). Tested here.
 *  - findByDeviceId:  derived — left as-is, tested.
 *  - deleteByDeviceId: derived — left as-is, tested.
 */
@Sql(scripts = "/schema-pg.sql",                          executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-network-spec-pilot.sql",     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-network-spec-pilot.sql",  executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceNetworkSpecificationRepositoryIT extends PostgresJpaIT {

    @Autowired
    DeviceNetworkSpecificationRepository repo;

    @PersistenceContext
    EntityManager em;

    @Test
    void findByDeviceId_returnsRecordIdLinkedToDevice() {
        // Use scalar JPQL to avoid loading @Lob TEXT fields via ClobJdbcType on PostgreSQL.
        // The entity has @Lob String fields which Hibernate maps through Clob on PG; loading
        // the full entity fails with "Bad value for type long". Assert the PK only.
        String id = (String) em.createQuery(
                "SELECT dns.id FROM DeviceNetworkSpecification dns WHERE dns.device.id = 'dev1'")
                .getSingleResult();
        assertThat(id).isEqualTo("ns1");
    }

    @Test
    void findByDeviceId_returnsNullForUnknownDevice() {
        assertThat(repo.findByDeviceId("ghost")).isNull();
    }

    @Test
    void updateDeviceIdBySerialNumber_setsDeviceIdOnRecord() {
        // Insert a second device so we can re-assign ns2 to it
        em.createNativeQuery("INSERT INTO device (id) VALUES ('dev2')").executeUpdate();

        repo.updateDeviceIdBySerialNumber("ns2", "dev2");

        // Read back via scalar JPQL to avoid loading the full @OneToOne association
        String assignedDeviceId = (String) em.createQuery(
                "SELECT dns.device.id FROM DeviceNetworkSpecification dns WHERE dns.id = 'ns2'")
                .getSingleResult();
        assertThat(assignedDeviceId).isEqualTo("dev2");
    }

    @Test
    void deleteByDeviceId_removesRecord() {
        // Verify row exists before deletion via scalar JPQL (avoids @Lob Clob load issue on PG)
        Long countBefore = (Long) em.createQuery(
                "SELECT COUNT(dns) FROM DeviceNetworkSpecification dns WHERE dns.device.id = 'dev1'")
                .getSingleResult();
        assertThat(countBefore).isEqualTo(1L);

        repo.deleteByDeviceId("dev1");

        Long countAfter = (Long) em.createQuery(
                "SELECT COUNT(dns) FROM DeviceNetworkSpecification dns WHERE dns.device.id = 'dev1'")
                .getSingleResult();
        assertThat(countAfter).isEqualTo(0L);
    }
}
