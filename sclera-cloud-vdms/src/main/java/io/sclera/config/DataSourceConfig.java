package io.sclera.config;
import com.alibaba.fastjson2.JSONObject;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Configuration
@Slf4j
public class DataSourceConfig {

    @Value("${aws.secretsmanager.database}")
    private String dbSecretName;

    @Value("${spring.datasource.hikari.auto-commit}")
    private Boolean autoCommit;

    @Value("${spring.datasource.hikari.minimum-idle}")
    private Integer minimumIdle;

    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private Integer maximumPoolSize;

    @Value("${spring.datasource.hikari.idle-timeout}")
    private Long idleTimeout;

    @Value("${spring.datasource.hikari.pool-name}")
    private String poolName;

    @Value("${spring.datasource.hikari.max-lifetime}")
    private Long maxLifetime;

    @Value("${spring.datasource.hikari.connection-timeout}")
    private Long connectionTimeout;


    @Bean
    public HikariDataSource dataSource(SecretsManagerClient secretsManager) {
        try {
            log.info("Retrieving DB credentials from secret: {}", dbSecretName);

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(dbSecretName)
                    .build();
            GetSecretValueResponse response = secretsManager.getSecretValue(request);

            JSONObject secretJson = JSONObject.parseObject(response.secretString());

            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setJdbcUrl("jdbc:mysql://" + secretJson.getString("host") + ":" + secretJson.getString("port") + "/" +
                    secretJson.getString("dbname") + "?createDatabaseIfNotExist=true");
            dataSource.setUsername(secretJson.getString("username"));
            dataSource.setPassword(secretJson.getString("password"));
            dataSource.setAutoCommit(autoCommit);
            dataSource.setMinimumIdle(minimumIdle);
            dataSource.setMaximumPoolSize(maximumPoolSize);
            dataSource.setIdleTimeout(idleTimeout);
            dataSource.setPoolName(poolName);
            dataSource.setMaxLifetime(maxLifetime);
            dataSource.setConnectionTimeout(connectionTimeout);

            return dataSource;
        } catch (Exception e) {
            log.error("Failed to retrieve db credentials from Secrets Manager", e);
            return null;
        }
    }
}