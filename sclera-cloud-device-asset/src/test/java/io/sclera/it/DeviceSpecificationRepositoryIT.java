package io.sclera.it;

import io.sclera.Repository.DeviceSpecificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql",                           executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-specification-pilot.sql",     executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-specification-pilot.sql",  executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceSpecificationRepositoryIT extends PostgresJpaIT {

    @Autowired
    DeviceSpecificationRepository repo;

    /**
     * findDistinctEmail: returns exactly the two distinct non-null emails from seed rows.
     */
    @Test
    void findDistinctEmail_returnsDistinctNonNullEmails() {
        List<String> emails = repo.findDistinctEmail();
        assertThat(emails).hasSize(2)
                .containsExactlyInAnyOrder("alice@example.com", "bob@example.com");
    }

    /**
     * findDistinctOsType: returns exactly the two distinct non-null OS types from seed rows.
     */
    @Test
    void findDistinctOsType_returnsDistinctNonNullOsTypes() {
        List<String> osTypes = repo.findDistinctOsType();
        assertThat(osTypes).hasSize(2)
                .containsExactlyInAnyOrder("Windows", "Linux");
    }

    /**
     * getChildDeviceByDeviceId: returns the child_devices value for the given device.
     */
    @Test
    void getChildDeviceByDeviceId_returnsChildDevices() {
        String childDevices = repo.getChildDeviceByDeviceId("spec-dev1");
        assertThat(childDevices).isEqualTo("child-a");
    }

    /**
     * getChildDeviceByDeviceId: returns null when no specification exists for the device.
     */
    @Test
    void getChildDeviceByDeviceId_unknownDevice_returnsNull() {
        String childDevices = repo.getChildDeviceByDeviceId("no-such-device");
        assertThat(childDevices).isNull();
    }

}
