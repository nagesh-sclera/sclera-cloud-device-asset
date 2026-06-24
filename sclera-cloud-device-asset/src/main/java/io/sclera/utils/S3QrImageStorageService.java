package io.sclera.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

// NOTE: the local docker-compose stack has no AWS S3, so the "docker" profile uses the
// filesystem impl instead (QR images go to /home/sclera/images/qrcodes, served at /images/**,
// same as asset images). Real cloud envs (qa/uat/prod/development) use S3 here.
@Component @Profile({"qa","uat","prod","development"})
public class S3QrImageStorageService implements QrImageStorageService {
    private final AmazonS3 s3; private final String bucket; private final String prefix; private final String urlBase;
    public S3QrImageStorageService(AmazonS3 s3,
        @Value("${sclera.aws.s3.bucket}") String bucket,
        @Value("${sclera.aws.s3.qrcode-prefix:qrcodes/}") String prefix,
        @Value("${sclera.server-qrcode-images-url}") String urlBase) {
        this.s3 = s3; this.bucket = bucket; this.prefix = prefix; this.urlBase = urlBase;
    }
    private String objectKey(String key, String ext) { return prefix + key + "." + ext; }
    public String store(byte[] bytes, String key, String ext) {
        ObjectMetadata m = new ObjectMetadata(); m.setContentLength(bytes.length);
        s3.putObject(bucket, objectKey(key, ext), new ByteArrayInputStream(bytes), m);
        return urlBase + key + "." + ext;
    }
    public byte[] fetch(String key) {
        for (String e : new String[]{"png","jpeg","pdf","zip","txt"}) {
            String ok = objectKey(key, e);
            if (s3.doesObjectExist(bucket, ok)) {
                try (com.amazonaws.services.s3.model.S3Object obj = s3.getObject(bucket, ok)) {
                    return obj.getObjectContent().readAllBytes();
                } catch (IOException ex) { throw new UncheckedIOException(ex); }
            }
        }
        throw new IllegalArgumentException("QR object not found: " + key);
    }
    public void delete(String key) {
        for (String e : new String[]{"png","jpeg","pdf","zip","txt"}) {
            String ok = objectKey(key, e); if (s3.doesObjectExist(bucket, ok)) s3.deleteObject(bucket, ok);
        }
    }
}
