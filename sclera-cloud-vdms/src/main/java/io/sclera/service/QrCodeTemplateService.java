package io.sclera.service;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.uuid.Generators;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.QrCodeTemplateDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.repository.QrCodeTemplateRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class QrCodeTemplateService {

    private final QrCodeTemplateRepository qrCodeTemplateRepository;

    private final ResourceUrlConfig resourceUrlConfig;

    private final AwsService awsService;

    public QrCodeTemplateService(QrCodeTemplateRepository qrCodeTemplateRepository, ResourceUrlConfig resourceUrlConfig, AwsService awsService) {
        this.qrCodeTemplateRepository = qrCodeTemplateRepository;
        this.resourceUrlConfig = resourceUrlConfig;
        this.awsService = awsService;
    }

    public ResponseEntity<ResponseDTO> getAllQrCodeTemplateByOrgId(String orgId, int pageNo, int pageSize, String key, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:OrgId:{},PageNo:{},PageSize:{},Key:{}", orgId, pageNo, pageSize, key);
        int offset = pageSize * (pageNo - 1);
        List<QrCodeTemplateDTO> qrCodeTemplates = qrCodeTemplateRepository.getAllQrCodeTemplateByOrgId(key, orgId, pageSize, offset);
        if (pageNo == 1) {
            QrCodeTemplateDTO defaultTemplate = qrCodeTemplateRepository.getDefaultTemplate();
            int count = qrCodeTemplateRepository.getTemplateInUseCount(orgId);
            if (count == 0) {
                defaultTemplate.setInUse(1);
            }
            qrCodeTemplates.add(0, defaultTemplate);
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload(qrCodeTemplates, 200, true);
        log.info("Successfully fetching Qr-Code Templates By OrgId:{},EndPoint:{}", orgId, httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getAllQrCodeTemplate(int pageNo, int pageSize, String key, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:PageNo:{},PageSize:{},Key:{}", pageNo, pageSize, key);
        int offset = pageSize * (pageNo - 1);
        List<QrCodeTemplateDTO> qrCodeTemplates = qrCodeTemplateRepository.getAllQrCodeTemplate(key, pageSize, offset);
        log.info("Successfully fetching Qr-Code Templates,EndPoint:{}", httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload(qrCodeTemplates, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> addQrCodeTemplate(String orgId, String body, MultipartFile multipartFile,
                                                         MultipartFile logo, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload:body:{}", body);
        QrCodeTemplateDTO qrCodeTemplateDTO = JSONObject.parseObject(body, QrCodeTemplateDTO.class);
        BigInteger creationTimestamp = BigInteger.valueOf(System.currentTimeMillis());
        String id = Generators.timeBasedGenerator().generate().toString();
        if (qrCodeTemplateDTO != null) {
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(multipartFile.getOriginalFilename()), httpServletRequest);

                String filePathUrl = String.format(resourceUrlConfig.getQrCodeTemplateUrl(), orgId);
                String directory = String.format(resourceUrlConfig.getQrCodeTemplateDirectory(), orgId);

                String url = awsService.addFileToAWSS3(multipartFile.getBytes(), directory, filePathUrl, extension, id, httpServletRequest);
                qrCodeTemplateDTO.setQrCodeTemplateUrl(url);
            }
            if (logo != null && !logo.isEmpty()) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(logo.getOriginalFilename()), httpServletRequest);

                String filePathUrl = String.format(resourceUrlConfig.getQrCodeTemplateLogoUrl(), orgId);
                String directory = String.format(resourceUrlConfig.getQrCodeTemplateLogoDirectory(), orgId);

                String url = awsService.addFileToAWSS3(logo.getBytes(), directory, filePathUrl, extension, id, httpServletRequest);
                qrCodeTemplateDTO.setQrCodeLogoUrl(url);
            }
            qrCodeTemplateRepository.addQrCodeTemplate(id, qrCodeTemplateDTO.getTemplateName(), qrCodeTemplateDTO.getQrCodeTemplateUrl(), qrCodeTemplateDTO.getQrCodeLogoUrl(),
                    orgId, body, creationTimestamp, loggedInUser);
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload(qrCodeTemplateDTO.getQrCodeTemplateUrl(), 200, true);
        log.info("Successfully Added Qr-Code Template,EndPoint:" + httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> deleteQrCodeTemplate(String orgId, List<String> ids, HttpServletRequest httpServletRequest) {
        log.info("Payload:Ids:{}", ids);

        List<QrCodeTemplateDTO> qrCodeTemplateDTOS = qrCodeTemplateRepository.getDataByIds(ids);
        if (qrCodeTemplateDTOS != null) {
            List<String> urls = new ArrayList<>();
            for (QrCodeTemplateDTO qrCodeTemplateDTO:qrCodeTemplateDTOS) {
                //template
                String templateFileName = awsService.getFileNameByImageUrl(qrCodeTemplateDTO.getQrCodeTemplateUrl(), httpServletRequest);
                String templateDirectory = String.format(resourceUrlConfig.getQrCodeTemplateDirectory(), orgId);
                urls.add(templateDirectory + templateFileName);

                //logo
                String logoFileName = awsService.getFileNameByImageUrl(qrCodeTemplateDTO.getQrCodeLogoUrl(), httpServletRequest);
                String logoDirectory = String.format(resourceUrlConfig.getQrCodeTemplateLogoDirectory(), orgId);
                urls.add(logoDirectory + logoFileName);
            }
            awsService.removeFilesFromAWSS3(urls);
        }
        qrCodeTemplateRepository.removeQrCodeTemplateByIds(ids);
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Successfully Deleted Qr-Code Template", 200, true);
        log.info("Successfully Deleted Qr-Code Template,EndPoint:" + httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> updateQrCodeTemplate(String orgId, String body, MultipartFile multipartFile,
                                                            MultipartFile logo, String url, String logoUrl, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload:body:{},TemplateUrl:{},LogoUrl:{}", body, url, logoUrl);
        QrCodeTemplateDTO qrCodeTemplateDTO = JSONObject.parseObject(body, QrCodeTemplateDTO.class);
        if (qrCodeTemplateDTO != null) {
            BigInteger updatedTimeStamp = BigInteger.valueOf(System.currentTimeMillis());
            String id = Generators.timeBasedGenerator().generate().toString();
            if (url != null && !url.isBlank()) {
                String fileName = awsService.getFileNameByImageUrl(url, httpServletRequest);
                log.info("Template:" + fileName);
                String directory = String.format(resourceUrlConfig.getQrCodeTemplateDirectory(), orgId);
                awsService.removeFileFromAWSS3(directory, fileName, httpServletRequest);
                qrCodeTemplateDTO.setQrCodeTemplateUrl(null);
            }
            if (logoUrl != null && (logo == null || logo.isEmpty())) {
                String fileName = awsService.getFileNameByImageUrl(logoUrl, httpServletRequest);
                log.info("Logo:" + fileName);
                String directory = String.format(resourceUrlConfig.getQrCodeTemplateLogoDirectory(), orgId);
                awsService.removeFileFromAWSS3(directory, fileName, httpServletRequest);
                qrCodeTemplateDTO.setQrCodeLogoUrl(null);
            }
            if (multipartFile != null && !multipartFile.isEmpty()) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(multipartFile.getOriginalFilename()), httpServletRequest);

                String filePathUrl = String.format(resourceUrlConfig.getQrCodeTemplateUrl(), orgId);
                String directory = String.format(resourceUrlConfig.getQrCodeTemplateDirectory(), orgId);

                String templateUrl = awsService.addFileToAWSS3(multipartFile.getBytes(),
                        directory,
                        filePathUrl,
                        extension,
                        id, httpServletRequest);
                qrCodeTemplateDTO.setQrCodeTemplateUrl(templateUrl);
                qrCodeTemplateDTO.setQrCodeLogoUrl(logoUrl);
            }
            if (logo != null && !logo.isEmpty()) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(logo.getOriginalFilename()), httpServletRequest);

                String filePathUrl = String.format(resourceUrlConfig.getQrCodeTemplateLogoUrl(), orgId);
                String directory = String.format(resourceUrlConfig.getQrCodeTemplateLogoDirectory(), orgId);

                String awsLogoUrl = awsService.addFileToAWSS3(logo.getBytes(),
                        directory,
                        filePathUrl,
                        extension,
                        id, httpServletRequest);
                qrCodeTemplateDTO.setQrCodeLogoUrl(awsLogoUrl);
            }
            if (logoUrl == null) {
                logoUrl = qrCodeTemplateRepository.getQrCodeLogoUrlById(qrCodeTemplateDTO.getId());
                qrCodeTemplateDTO.setQrCodeLogoUrl(logoUrl);
            }
            qrCodeTemplateRepository.updateQrCodeTemplateById(qrCodeTemplateDTO.getTemplateName(), qrCodeTemplateDTO.getQrCodeTemplateUrl(), qrCodeTemplateDTO.getQrCodeLogoUrl(), orgId, body, updatedTimeStamp, loggedInUser, qrCodeTemplateDTO.getId());
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload(qrCodeTemplateDTO.getQrCodeTemplateUrl(), 200, true);
        log.info("Successfully Updated Qr-Code Template,EndPoint:" + httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getInUseUrlByOrgId(String orgId, HttpServletRequest httpServletRequest) {
        log.info("Payload:OrgId:{}", orgId);
        QrCodeTemplateDTO qrCodeTemplateDTO;
        qrCodeTemplateDTO = qrCodeTemplateRepository.getInUseUrlByOrgId(1, orgId);
        if (qrCodeTemplateDTO == null) {
            qrCodeTemplateDTO = qrCodeTemplateRepository.getDefaultTemplate();
        }        ResponseDTO responseDTO = ScleraUtils.generatePayload(qrCodeTemplateDTO, 200, true);
        log.info("Successfully fetching Qr-Code Template InUse Url,EndPoint:" + httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> updateInUseByUrl(String orgId, String templateUrl, boolean isDefault, HttpServletRequest httpServletRequest) {
        log.info("Payload:templateUrl:{}", templateUrl);
        if (isDefault) {
            qrCodeTemplateRepository.updateInUseByOrgId(0, orgId);
        } else {
            qrCodeTemplateRepository.updateInUseByOrgId(0, orgId);
            qrCodeTemplateRepository.updateInUseByUrl(1, templateUrl);
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Successfully Updating Qr-Code Template", 200, true);
        log.info("Successfully Updating Qr-Code Template InUse Url,EndPoint:" + httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
