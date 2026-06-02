package io.sclera.scheduler;

import io.sclera.scheduler.catalog.CatalogStartupRunner;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared Testcontainers base for @SpringBootTest classes.
 *
 * <p>ONE Postgres container is started for the whole test JVM (the singleton-container
 * pattern) and reused by every subclass. Starting a fresh container per class exhausted the
 * Docker host mid-suite and produced "Could not open JPA EntityManager" connection timeouts;
 * a single shared container removes that churn. The container is never stopped explicitly —
 * Testcontainers' Ryuk reaps it at JVM exit.
 *
 * <p>{@code @ServiceConnection} wires the datasource to the container; the Hibernate
 * {@code default_schema} comes from application.yml and Flyway creates the schema.
 *
 * <p>{@link CatalogStartupRunner} is mocked here so that no subclass's application context
 * auto-reconciles the 28-job catalog into the shared container at startup. Without this, the
 * committed catalog rows would leak across tests/classes and break assertions that expect a
 * clean job table. Tests that need the catalog exercise the reconciler directly.
 */
public abstract class AbstractPostgresTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @MockitoBean
    CatalogStartupRunner catalogStartupRunner;
}
