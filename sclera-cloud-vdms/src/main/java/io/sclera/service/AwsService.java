package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.QrCodeDTO;
import io.sclera.util.ScleraUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;


import jakarta.servlet.http.HttpServletRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AwsService {

    @Autowired
    public S3Client s3Client;
    @Autowired
    private ResourceUrlConfig resourceUrlConfig;
    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${sclera.floor.image.tilesDirectory}")
    private String floorTilesDirectory;

    @Value("${sclera.floor.lambda-function-name}")
    private String lambdaFunctionName;

    @Autowired
    public AWSLambda awsLambda;

    @Autowired
    private S3Presigner s3Presigner;


    public String addFileToAWSS3(byte[] image, String absolutePath, String directory, String extension, String filename, HttpServletRequest httpServletRequest) {
        log.info("Payload:absolutePath:{},directory:{},filename:{},extension:{}", absolutePath, directory, filename, extension);
        if (image != null) {
            String id = String.valueOf(System.nanoTime());
            String file = absolutePath + filename + "_" + id + "." + extension;

            software.amazon.awssdk.services.s3.model.PutObjectRequest putObjectRequest = software.amazon.awssdk.services.s3.model.PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(image));
//            log.info("Successfully Added File in path {} To AWS S3.EndPoint:{}", absolutePath + fileName, httpServletRequest.getRequestURI());

            return directory + filename + "_" + id + "." + extension;
        } else {
            log.error("File could not be uploaded. Endpoint: {}", httpServletRequest.getRequestURI());
            return null;
        }
    }


    public String addFileWithoutTimestampToAWSS3(byte[] image, String absolutePath, String directory, String extension, String filename, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload:absolutePath:{},directory:{},filename:{},extension:{}", absolutePath, directory, filename, extension);
        if (image != null) {
            String file = absolutePath + filename + "." + extension;
            software.amazon.awssdk.services.s3.model.PutObjectRequest putObjectRequest = software.amazon.awssdk.services.s3.model.PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(image));
//            log.info("Successfully Added File in path {} To AWS S3.EndPoint:{}", absolutePath + fileName, httpServletRequest.getRequestURI());

            return directory + filename + "_" + "." + extension;
        } else {
            log.error("File could not be uploaded. Endpoint: {}", httpServletRequest.getRequestURI());
            return null;
        }
    }



    public void removeFileFromAWSS3(String absolutePath, String fileName, HttpServletRequest httpServletRequest) {
        log.info("Payload:FileName:{},AbsolutPath:{}", fileName, absolutePath);

        software.amazon.awssdk.services.s3.model.DeleteObjectRequest deleteObjectRequest = software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(absolutePath + fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        log.info("Successfully Removed File in path {} From AWS S3.EndPoint:{}", absolutePath + fileName, httpServletRequest.getRequestURI());

    }

    public String getFileNameByImageUrl(String image_url, HttpServletRequest httpServletRequest) {
        log.info("Fetching File_Name By Image Url:{}.EndPoint:{}", image_url, httpServletRequest.getRequestURI());
        return image_url.substring(image_url.lastIndexOf("/") + 1);
    }

    public String getFileExtensionByImageUrl(String image_url, HttpServletRequest httpServletRequest) {
        log.info("Fetching File Extension By Image Url:{}.EndPoint:{}", image_url, httpServletRequest.getRequestURI());
        return image_url.substring(image_url.lastIndexOf(".") + 1);
    }


    public Boolean checkFileExist(String filename, HttpServletRequest httpServletRequest) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }


    public void removeFolderFromAWSS3(String folderPath, HttpServletRequest httpServletRequest) {
        log.info("Payload: folderPath: {}", folderPath);
        List<ObjectIdentifier> keys = new ArrayList<>();

        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(folderPath)
                .build();

        ListObjectsV2Iterable listObjectsResponses = s3Client.listObjectsV2Paginator(listObjectsRequest);

        for (ListObjectsV2Response page : listObjectsResponses) {
            page.contents().forEach(object ->
                    keys.add(ObjectIdentifier.builder().key(object.key()).build())
            );
        }

        if (!keys.isEmpty()) {
            int batchSize = 1000;
            for (int i = 0; i < keys.size(); i += batchSize) {
                List<ObjectIdentifier> batch = keys.subList(i, Math.min(keys.size(), i + batchSize));

                DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(Delete.builder().objects(batch).build())
                        .build();

                DeleteObjectsResponse deleteObjectsResponse = s3Client.deleteObjects(deleteObjectsRequest);

                int successfulDeletes = deleteObjectsResponse.deleted().size();
                log.info("{} objects successfully deleted.", successfulDeletes);
            }
            log.info("Removing Folder From AWS S3. EndPoint: {}", httpServletRequest.getRequestURI());
        } else {
            log.info("No objects found in the specified folder.");
        }
    }


//    public String getPreSignedUrlForFileUpload(String objectKey, int expTime, HttpMethod httpMethod) {
//        GetObjectRequest objectRequest = GetObjectRequest.builder()
//                .bucket(bucketName)
//                .key(objectKey)
////                .responseContentType(determineContentType(extension))
//                .build();
//
//        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
//                .signatureDuration(Duration.ofMinutes(expTime))
//                .getObjectRequest(objectRequest)
//                .build();
//
//
//        PresignedGetObjectRequest preSignedUrl = s3Presigner.presignGetObject(presignRequest);
//
//        return preSignedUrl.url().toString();
//
//    }


    public String getPreSignedUrlForFileUpload(String objectKey, int expTime, HttpMethod httpMethod) {
        if (httpMethod == HttpMethod.PUT) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expTime))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest preSignedUrl = s3Presigner.presignPutObject(presignRequest);
            return preSignedUrl.url().toString();
        } else if (httpMethod == HttpMethod.GET) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expTime))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest preSignedUrl = s3Presigner.presignGetObject(presignRequest);
            return preSignedUrl.url().toString();
        } else {
            throw new IllegalArgumentException("Unsupported HTTP method: " + httpMethod);
        }
    }



    public String addQrCodeFileToAWSS3(byte[] content, String absolutePath, String directory, String extension, String filename, HttpServletRequest httpServletRequest) {
        log.info("Payload: absolutePath:{}, directory:{}, filename:{}, extension:{}", absolutePath, directory, filename, extension);
        if (content != null) {
            String file = directory + filename + extension;
            log.info("AWS File: {}", file);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file)
                    .acl(ObjectCannedACL.PRIVATE)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));

            List<Tag> tags = new ArrayList<>();
            tags.add(Tag.builder().key("access").value("false").build());

            PutObjectTaggingRequest taggingRequest = PutObjectTaggingRequest.builder()
                    .bucket(bucketName)
                    .key(file)
                    .tagging(Tagging.builder().tagSet(tags).build())
                    .build();

            s3Client.putObjectTagging(taggingRequest);
            return file;
        } else {
            log.error("Unable To Upload File. EndPoint: {}", httpServletRequest.getRequestURI());
            return null;
        }
    }


    public ListObjectsV2Response objectListing(String directory) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(directory)
                .build();
        return s3Client.listObjectsV2(listRequest);
    }

    public List<Tag> getTags(S3Object s3Object) {
        GetObjectTaggingRequest getObjectTaggingRequest = GetObjectTaggingRequest.builder()
                .bucket(bucketName)
                .key(s3Object.key())
                .build();

        GetObjectTaggingResponse taggingResponse = s3Client.getObjectTagging(getObjectTaggingRequest);
        return taggingResponse.tagSet();
    }


    public void removeFilesFromAWSS3(List<String> fileNames) {
        if (!fileNames.isEmpty()) {
            log.info("Payload: fileNames: {}", fileNames);
            List<ObjectIdentifier> keys = fileNames.stream()
                    .map(fileName -> ObjectIdentifier.builder().key(fileName).build())
                    .collect(Collectors.toList());

            int batchSize = 1000;
            for (int i = 0; i < keys.size(); i += batchSize) {
                List<ObjectIdentifier> batch = keys.subList(i, Math.min(keys.size(), i + batchSize));

                DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                        .bucket(bucketName)
                        .delete(Delete.builder().objects(batch).quiet(false).build())
                        .build();

                DeleteObjectsResponse deleteObjectsResponse = s3Client.deleteObjects(deleteObjectsRequest);

                int successfulDeletes = deleteObjectsResponse.deleted().size();
                log.info("{} objects successfully deleted.", successfulDeletes);
            }
            log.info("Removing Files From AWS S3");
        } else {
            log.info("Cannot Delete AWS Files: No files specified");
        }
    }


    public String addExportFileToAWSS3(byte[] image, String absolutePath, String directory, String extension, String filename, HttpServletRequest httpServletRequest) {
        log.info("Payload: absolutePath:{}, directory:{}, filename:{}, extension:{}", absolutePath, directory, filename, extension);
        if (image != null) {
            String file = extension == null ? directory + filename : directory + filename + extension;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(image));
            return file;
        } else {
            log.error("Unable To Upload File. EndPoint: {}", httpServletRequest.getRequestURI());
            return null;
        }
    }

    public String getFilePathByImageUrl(String image_url, HttpServletRequest httpServletRequest) throws MalformedURLException {
        log.info("Fetching File_Name By Image Url:{}.EndPoint:{}", image_url, httpServletRequest.getRequestURI());
        URL url = new URL(image_url);
        String path = url.getPath();

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        path = path.replaceFirst("^vdms/", "");
        return path;
    }

    public String getFilePathByImageUrl(String image_url ) throws MalformedURLException {
        log.info("Fetching File_Name By Image Url:{}.", image_url);
        URL url = new URL(image_url);
        String path = url.getPath();

        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        path = path.replaceFirst("^vdms/", "");
        return path;
    }

    public String copyFileToAWSS3(String sourceKey, String absolutePath, String directory, String extension, String deviceId, HttpServletRequest httpServletRequest) {
        log.info("Payload: sourceKey:{}, absolutePath:{}, directory:{}, extension:{}, filename:{}", sourceKey, absolutePath, directory, extension, deviceId);
        if (sourceKey != null) {
            String id = String.valueOf(System.nanoTime());
            String filename = absolutePath + deviceId + "_" + id + "." + extension;
            log.info("FileName: {}", filename);

            CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                    .sourceBucket(bucketName)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucketName)
                    .destinationKey(filename)
                    .build();

            s3Client.copyObject(copyObjectRequest);
            return directory + deviceId + "_" + id + "." + extension;
        } else {
            log.error("Unable To Upload File. EndPoint: {}", httpServletRequest.getRequestURI());
            return null;
        }
    }

    public JSONObject invokeScleraTiles(String imageUrl, String floorDirectoryUrl, String fileName,
                                        String ulx, String uly, String llx, String lly) {

        JSONObject lambdaRequestData = new JSONObject();

        lambdaRequestData.put("s3_url", imageUrl);
        lambdaRequestData.put("ulx", ulx);
        lambdaRequestData.put("uly", uly);
        lambdaRequestData.put("llx", llx);
        lambdaRequestData.put("lly", lly);
        lambdaRequestData.put("fileName", fileName);
        lambdaRequestData.put("s3folderPath", floorDirectoryUrl);
        lambdaRequestData.put("bucketName", bucketName);

        log.info("LAMBDA PAYLOAD : {}", lambdaRequestData);

        String json = lambdaRequestData.toString();


        byte[] payload = json.getBytes(StandardCharsets.UTF_8);

        // Setup an InvokeRequest.
        InvokeRequest request = new InvokeRequest()
                .withFunctionName(lambdaFunctionName)
                .withPayload(ByteBuffer.wrap(payload));

        InvokeResult res = awsLambda.invoke(request);


        String lambdaResponse = StandardCharsets.UTF_8.decode(res.getPayload()).toString();

        log.info("Lambda response : {}", lambdaResponse);

        return JSON.parseObject(lambdaResponse);
    }

    public String constructS3URL(String vdms_id, String imageUrl) {
        log.info("VDMS Id :{}, Image Url :{}", vdms_id, imageUrl);
        String s3FileId = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        String x = "https://" + bucketName + ".s3.amazonaws.com" + "/" + String.format(floorTilesDirectory, vdms_id, s3FileId);
        String s3Url = x.substring(0, x.length() - 1);
        log.info("S3 URL: {}", s3Url);
        return s3Url;
    }

    public HttpHeaders getQrCodeRedirectionLink(String key, String url) {
        byte[] decodedBytes = Base64.getDecoder().decode(url);
        String decodedUrl = new String(decodedBytes);
        boolean isValid = isValidPresignedUrl(decodedUrl);
        HttpHeaders headers = new HttpHeaders();

        if (isValid) {
            headers.setLocation(URI.create(decodedUrl));

            // Create a list of tags
            List<Tag> tags = new ArrayList<>();
            tags.add(Tag.builder().key("access").value("true").build());

            // Build the request to set object tagging
            PutObjectTaggingRequest taggingRequest = PutObjectTaggingRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .tagging(Tagging.builder().tagSet(tags).build())
                    .build();

            // Set the object tagging
            s3Client.putObjectTagging(taggingRequest);
        } else {
            headers.setLocation(URI.create(resourceUrlConfig.getErrorPage()));
        }

        return headers;
    }

    public HttpHeaders getQrCodeRedirectionLink(String redirectUrl) {

        byte[] decodedBytes = Base64.getDecoder().decode(redirectUrl);
        String decodedUrl = new String(decodedBytes);
        boolean isValid = isValidPresignedUrl(decodedUrl);
        HttpHeaders headers = new HttpHeaders();

        if (isValid) {
            headers.setLocation(URI.create(decodedUrl));
        } else {
            headers.setLocation(URI.create(resourceUrlConfig.getErrorPage()));
        }
        return headers;
    }

    public boolean isValidPresignedUrl(String preSignedUrl) {
        try {
            URL url = new URL(preSignedUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


    public void uploadQRCodesToS3(List<QrCodeDTO> qrCodeDTOS, String batchId) {
        for (QrCodeDTO qrCodeDTO : qrCodeDTOS) {
            byte[] image = qrCodeDTO.getQrCodeContent();
            String key = String.format(resourceUrlConfig.getQrCodeDirectory(), batchId, qrCodeDTO.getId());
            File tempFile = null;

            try {
                tempFile = File.createTempFile("image", ".png");
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(image);
                }

                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                s3Client.putObject(request, RequestBody.fromFile(tempFile));
                log.info("Uploaded QrCode Image with key: {} to S3 bucket.", key);

            } catch (S3Exception | IOException e) {
                log.error("Error: {}", e.getMessage());
                Thread.currentThread().interrupt();
            } finally {
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }
    }

    public byte[] downloadTemplateFromS3( String key) {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.getObject(request, ResponseTransformer.toBytes()).asByteArray();
    }
}