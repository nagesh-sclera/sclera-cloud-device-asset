package io.sclera.config;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Configuration
public class AwsConfig {

    @Value("${aws.s3.region}")
    private String region;

    @Bean
    public S3Client createS3Client(AwsBasicCredentials awsCredentials) {

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

    }

    @Bean
    public S3Presigner s3Presigner(AwsBasicCredentials awsCredentials) {

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();

    }

    @Bean
    public AWSLambda generateAwsLambdaClient(AwsBasicCredentials awsCredentials) {

        AWSCredentials credentials = new BasicAWSCredentials(
                awsCredentials.accessKeyId(),
                awsCredentials.secretAccessKey()
        );
        ClientConfiguration clientConfig = new ClientConfiguration()
                .withConnectionTimeout(15 * 60 * 1000)
                .withRequestTimeout(15 * 60 * 1000)
                .withSocketTimeout(15 * 60 * 1000);

        return AWSLambdaClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .withClientConfiguration(clientConfig)
                .build();
    }
}