package io.sclera.config;
import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
class S3ConfigTest {
    @Test void buildsClient() {
        AmazonS3 s3 = new S3Config().amazonS3("us-east-1");
        assertThat(s3).isNotNull();
    }
}
