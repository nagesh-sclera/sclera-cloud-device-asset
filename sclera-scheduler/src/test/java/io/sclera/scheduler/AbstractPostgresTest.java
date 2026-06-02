package io.sclera.scheduler;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Testcontainers base. {@code @ServiceConnection} wires the datasource to the container;
 * the Hibernate {@code default_schema} comes from application.yml.
 */
@Testcontainers
public abstract class AbstractPostgresTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine");
}
