package io.sclera.scheduler;

import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.VdmsDTO;
import io.sclera.repository.QrCodeRepository;
import io.sclera.service.AwsService;
import io.sclera.service.UserActionLogService;
import io.sclera.service.VdmsService;
import io.sclera.service.WebClientAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.Tag;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SchedulerService {
  @Autowired private ResourceUrlConfig resourceUrlConfig;
  @Autowired private AwsService awsService;
  @Autowired private QrCodeRepository qrCodeRepository;
  @Autowired private UserActionLogService userActionLogService;
  @Autowired private VdmsService vdmsService;
  @Autowired private WebClientAlertService webClientAlertService;
  private static final int BATCH_SIZE = 10000;

  public void deleteQrCodeFromAwsAndDb() {
    log.info("Payload: directory: {}", resourceUrlConfig.getQrCodeFileDirectory());
    List<String> objectKey = new ArrayList<>();
    List<String> dbData = new ArrayList<>();
    ListObjectsV2Response listObjectsV2Response =
        awsService.objectListing(resourceUrlConfig.getQrCodeFileDirectory());
    Instant twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS);
    for (S3Object s3Object : listObjectsV2Response.contents()) {
      List<Tag> tags = awsService.getTags(s3Object);
      String accessTagValue = getTagValue(tags, "access");
      if (accessTagValue != null) {
        log.info(
            "*************************************************************************************");
        log.info("Object Key: {}", s3Object.key());
        log.info("Access Tag Value: {}", accessTagValue);
        log.info("Last Modified: {}", s3Object.lastModified());
        log.info(
            "*************************************************************************************");
        if (Objects.equals(accessTagValue, "true")
            && s3Object.lastModified().isBefore(twentyFourHoursAgo)) {
          log.info("Successfully Deleting (zip,pdf,txt) File From AWS S3:" + s3Object.key());
          objectKey.add(s3Object.key());
        } else if (Objects.equals(accessTagValue, "false")
            && s3Object.lastModified().isBefore(twentyFourHoursAgo)) {
          String batchId = getBatchId(s3Object);
          log.info("batchId:" + batchId);
          log.info(
              "Successfully Deleting (zip,pdf,txt,png) File: "
                  + s3Object.key()
                  + " From AWS S3 In Which Tag is false and exceeds 24 Hours.");

          // delete qr code data from db
          dbData.add(batchId);

          // delete qr code images from s3 bucket
          List<String> imageUrl = qrCodeRepository.getQrCodeImageByBatchId(batchId);
          log.info("Successfully Deleting QR Code Image URL From DB");
          objectKey.addAll(imageUrl);

          // delete qr code images from s3 bucket
          objectKey.add(s3Object.key());
        }
      }
    }
    log.info(
        "*************************************************************************************");
    log.info("Object_Key:-" + objectKey);
    log.info(
        "*************************************************************************************");
    log.info("Batch_Id:-" + dbData);
    log.info(
        "*************************************************************************************");
    qrCodeRepository.deleteQrCodesByBatchIds(dbData);
    awsService.removeFilesFromAWSS3(objectKey);
  }

  private String getTagValue(List<Tag> tags, String key) {
    for (Tag tag : tags) {
      if (tag.key().equals(key)) {
        return tag.value();
      }
    }
    return null;
  }

  private String getBatchId(S3Object s3Object) {
    return s3Object
        .key()
        .substring(s3Object.key().lastIndexOf("_") + 1, s3Object.key().lastIndexOf("."));
  }

  public void deleteExportFileFromAws() {
    log.info("Payload: directory: {}", resourceUrlConfig.getDownloadFileDirectory());
    List<String> objectKey = new ArrayList<>();
    ListObjectsV2Response listObjectsV2Response =
        awsService.objectListing(resourceUrlConfig.getDownloadFileDirectory());
    Instant twentyFourHoursAgo = Instant.now().minus(24, ChronoUnit.HOURS);

    for (S3Object s3Object : listObjectsV2Response.contents()) {
      if (s3Object.lastModified().isBefore(twentyFourHoursAgo)) {
        objectKey.add(s3Object.key());
      }
    }
    log.info("Files: " + objectKey + " Removed From AWS");
    awsService.removeFilesFromAWSS3(objectKey);
  }


  public void scheduleTaskForOfflineVdmsAlerts() {
    List<VdmsDTO> vdmsData = vdmsService.getVdmsAlertData();
    for (VdmsDTO vdmsDTO : vdmsData) {
      if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(vdmsDTO.getLast_seen().longValueExact()) > 10) {
        if(vdmsDTO.getEmail_alert() != 1){
          webClientAlertService.offlineVdmsemailAlert(vdmsDTO);
          vdmsService.updaateVdmsEmailAlertByVdmsId(vdmsDTO.getId(),1);
        }
      } else {
        vdmsService.updaateVdmsEmailAlertByVdmsId(vdmsDTO.getId(),0);
      }
    }
  }
}
