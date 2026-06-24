package io.sclera.config;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

@Slf4j
@Configuration
public class SecretsManagerConfig {

    @Value("${aws.secretsmanager.ip-api}")
    private String ipApiSecretName;

    @Value("${aws.secretsmanager.region}")
    private String region;

    @Value("${aws.secretsmanager.aws}")
    private String awsSecretName;


    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
                .region(Region.of(region))
                .build();
    }

    @Bean
    public AwsBasicCredentials awsCredentials(SecretsManagerClient secretsManager) {
        try {
            log.info("Retrieving AWS credentials from secret: {}", awsSecretName);

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(awsSecretName)
                    .build();
            GetSecretValueResponse response = secretsManager.getSecretValue(request);

            JSONObject secretJson = JSONObject.parseObject(response.secretString());


            return AwsBasicCredentials.create(
                    secretJson.getString("accessKeyId"),
                    secretJson.getString("secretAccessKey")
            );
        } catch (Exception e) {
            log.error("Failed to retrieve AWS credentials from Secrets Manager", e);
            return null;
        }
    }


    @Bean
    public String ipApiKey(SecretsManagerClient secretsManager) {
        try {
            log.info("Retrieving IpApi credentials from secret: {}", ipApiSecretName);

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(ipApiSecretName)
                    .build();
            GetSecretValueResponse response = secretsManager.getSecretValue(request);

            JSONObject secretJson = JSONObject.parseObject(response.secretString());
            return secretJson.getString("key");

        } catch (Exception e) {
            log.error("Failed to retrieve Ip-Api credentials from Secrets Manager", e);
            return null;
        }
    }



}
