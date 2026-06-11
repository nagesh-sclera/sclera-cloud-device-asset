package io.sclera.it;

import io.sclera.Repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JpaTestConfig.class)
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/asset-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-asset-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class AssetRepositoryIT extends PostgresJpaIT {

    @Autowired
    AssetRepository assetRepository;

    @Test
    void contextLoadsAndRepositoryAutowires() {
        assertThat(assetRepository).isNotNull();
        assertThat(assetRepository.count()).isEqualTo(3);
    }
}
