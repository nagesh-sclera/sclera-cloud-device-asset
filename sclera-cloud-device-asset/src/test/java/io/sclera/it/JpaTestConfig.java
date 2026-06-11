package io.sclera.it;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.persistence.autoconfigure.PersistenceExceptionTranslationAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * Minimal Spring configuration for JPA integration tests in Spring Boot 4.
 *
 * Spring Boot 4.0 removed @DataJpaTest. This configuration provides the equivalent
 * minimal JPA context for @SpringBootTest(classes = JpaTestConfig.class).
 *
 * What it provides:
 *   - DataSource (from spring.datasource.* — wired by @ServiceConnection in PostgresJpaIT)
 *   - DataSourceTransactionManager
 *   - Hibernate EntityManagerFactory (with all entities from io.sclera.models)
 *   - PersistenceExceptionTranslation
 *
 * What it deliberately OMITS:
 *   - Spring Data JPA repositories (@EnableJpaRepositories) — to avoid the named-query
 *     bootstrap issue where repositories fail on @Query(name=...) for queries declared
 *     in MySQL-specific @NamedNativeQuery annotations (e.g. with ON DUPLICATE KEY UPDATE).
 *   - Web, Security, RabbitMQ, Redis, Flyway, Dapr — not needed for JPA SQL tests.
 *
 * Tests use EntityManager.createNativeQuery() directly to validate the ported SQL.
 *
 * Spring Boot 4 package mapping:
 *   DataSourceAutoConfiguration   -> org.springframework.boot.jdbc.autoconfigure
 *   HibernateJpaAutoConfiguration -> org.springframework.boot.hibernate.autoconfigure
 *   @EntityScan                   -> org.springframework.boot.persistence.autoconfigure
 */
@Configuration
@ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        PersistenceExceptionTranslationAutoConfiguration.class,
})
@EntityScan(basePackages = "io.sclera.models")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(
        basePackageClasses = io.sclera.Repository.AssetRepository.class,
        includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = { io.sclera.Repository.AssetRepository.class,
                            io.sclera.Repository.DeviceIPAddressRepository.class,
                            io.sclera.Repository.LocationRepository.class,
                            io.sclera.Repository.FloorRepository.class,
                            io.sclera.Repository.AssetDeviceMappingRepository.class,
                            io.sclera.Repository.BuildingRepository.class,
                            io.sclera.Repository.DeviceOnboardStatusRepository.class }))
public class JpaTestConfig {
    // Intentionally empty — all beans come from @ImportAutoConfiguration.
}
