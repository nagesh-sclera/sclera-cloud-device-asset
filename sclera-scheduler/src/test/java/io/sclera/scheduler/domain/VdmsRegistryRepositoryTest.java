package io.sclera.scheduler.domain;

import io.sclera.scheduler.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class VdmsRegistryRepositoryTest extends AbstractPostgresTest {

    @Autowired VdmsRegistryRepository registry;

    @Test
    void findsOnlyActiveVdms() {
        registry.save(new VdmsRegistryEntity("vdms-1", "America/New_York", true));
        registry.save(new VdmsRegistryEntity("vdms-2", "Europe/London", false));

        assertThat(registry.findByActiveTrue())
                .extracting(VdmsRegistryEntity::getVdmsId).containsExactly("vdms-1");
    }
}
