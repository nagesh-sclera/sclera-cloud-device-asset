package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.uuid.Generators;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.FeatureDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.FeatureRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class FeatureService {

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private VdmsFeatureService vdmsFeatureService;

    @Autowired
    private AwsService awsService;

    @Autowired
    private ResourceUrlConfig resourceUrlConfig;
    @Autowired
    private UserActionLogService userActionLogService;

    public ResponseEntity<ResponseDTO> getFeatureList(String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: LoggedInUser: {}", loggedInUser);
        List<FeatureDTO> featureDTOList = featureRepository.getFeatureList();
        ResponseDTO responseDTO = ScleraUtils.generatePayload(featureDTOList, 200, true);
        log.info("Fetching Feature List. Endpoint: {}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getFeatureDetailsById(String featureId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: FeatureId: {}, LoggedInUser: {}", featureId, loggedInUser);
        if (featureId != null) {
            FeatureDTO featureDTO = featureRepository.getFeatureDetailsById(featureId);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(featureDTO, 200, true);
            log.info("Fetching Feature Details. Endpoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> addFeature(String body, MultipartFile image, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: Body: {}, LoggedInUser: {}", body, loggedInUser);
        if (body != null){
            FeatureDTO featureDTO = JSON.parseObject(body, FeatureDTO.class);
            String featureId = Generators.timeBasedGenerator().generate().toString();

            if (image != null) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(image.getOriginalFilename()), httpServletRequest);
                String url = String.format(resourceUrlConfig.getFeatureImageUrl());
                String directory = String.format(resourceUrlConfig.getFeatureImageDirectory());
                String imageLink = awsService.addFileToAWSS3(image.getBytes(), directory, url, extension, featureId, httpServletRequest);
                featureDTO.setImageUrl(imageLink);
            }

            featureRepository.addFeature(featureId, featureDTO.getName(), featureDTO.getImageUrl(), featureDTO.getDeepLinkUrl());
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Feature Added Successfully", 200, true);
            userActionLogService.addUserActionLog(loggedInUser, "Feature", "ADD", "Feature With Name:" + featureDTO.getName() + " Added Successfully", "success");
            log.info("Feature Added Successfully. Endpoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Feature", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public List<FeatureDTO> getVdmsFeatureByVdmsId(String vdmsId) {
        log.info("Payload: VdmsId: {}", vdmsId);
        return featureRepository.getVdmsFeatureByVdmsId(vdmsId);
    }

    public ResponseEntity<ResponseDTO> updateFeatureDetailsById(String featureId, String body, String imageUrl, MultipartFile image, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: FeatureId:{}, Body: {}, ImageUrl: {}, LoggedInUser: {}", featureId, body, imageUrl, loggedInUser);
        if (featureId != null) {
            FeatureDTO featureDTO = JSON.parseObject(body, FeatureDTO.class);
            if (imageUrl != null) {
                String fileName = awsService.getFileNameByImageUrl(imageUrl, httpServletRequest);
                String directory = String.format(resourceUrlConfig.getFeatureImageDirectory());
                awsService.removeFileFromAWSS3(directory, fileName, httpServletRequest);
                featureDTO.setImageUrl(null);
            }

            if (image != null) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(image.getOriginalFilename()), httpServletRequest);
                String url = String.format(resourceUrlConfig.getFeatureImageUrl());
                String directory = String.format(resourceUrlConfig.getFeatureImageDirectory());
                String logoLink = awsService.addFileToAWSS3(image.getBytes(), directory, url, extension, featureId, httpServletRequest);
                featureDTO.setImageUrl(logoLink);
            }

            featureRepository.updateFeatureDetailsById(featureDTO.getName(), featureDTO.getImageUrl(), featureDTO.getDeepLinkUrl(), featureId);
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Feature Updated Successfully", 200, true);
            userActionLogService.addUserActionLog(loggedInUser, "Feature", "UPDATE", "Feature With Name:" + featureDTO.getName() + " Is Updated Successfully", "success");
            log.info("Feature Updated Successfully. Endpoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Feature", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> deleteFeatureById(String featureId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: FeatureId:{}, LoggedInUser: {}", featureId, loggedInUser);
        if (featureId != null) {
            String url = featureRepository.getImageUrlById(featureId);
            String fileName = awsService.getFileNameByImageUrl(url, httpServletRequest);
            String directory = String.format(resourceUrlConfig.getFeatureImageDirectory());
            awsService.removeFileFromAWSS3(directory, fileName, httpServletRequest);

            vdmsFeatureService.deleteVdmsFeatureByFeatureId(featureId);
            featureRepository.deleteFeatureById(featureId);
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Feature Deleted Successfully", 200, true);
            userActionLogService.addUserActionLog(loggedInUser, "Feature", "DELETE", "Feature With Id:" + featureId + " Deleted Successfully", "success");
            log.info("Feature Deleted Successfully. Endpoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Feature", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public List<FeatureDTO> getVdmsFeatureByOrgId(String orgId) {
        log.info("Payload: OrgId: {}", orgId);
        log.info("Fetching Vdms Features By OrgId: {}", orgId);
        return featureRepository.getVdmsFeatureByOrgId(orgId);
    }
}
