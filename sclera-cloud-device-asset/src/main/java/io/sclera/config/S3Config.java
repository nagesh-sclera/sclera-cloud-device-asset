package io.sclera.config;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
@Configuration @Profile({"docker","qa","uat","prod","development"})
public class S3Config {
    @Bean public AmazonS3 amazonS3(@Value("${sclera.aws.s3.region:us-east-1}") String region) {
        return AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(region)).build();
    }
}
