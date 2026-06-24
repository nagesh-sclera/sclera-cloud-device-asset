package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.amazonaws.HttpMethod;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
public class InventoryAlertService {

    @Autowired
    private ResourceUrlConfig resourceUrlConfig;
    @Autowired
    private AwsService awsService;
    @Autowired
    private WebClientAlertService webClientAlertService;


    public ResponseEntity<?> sendInventoryReport(List<MultipartFile> files, String fileType, String body, HttpServletRequest httpServletRequest) throws IOException {
        if (files != null && fileType != null) {
            for (MultipartFile file : files) {
                log.info("Payload:FileName:{},FileType:{},body:{}", file.getOriginalFilename(), fileType, body);
                String decodedBody = URLDecoder.decode(body, StandardCharsets.UTF_8.toString());
                JSONObject inventoryFileData = JSON.parseObject(decodedBody);
                log.info("PayLoad: InventoryFileData{}", inventoryFileData);
                log.info("Export Inventory Data Alert Triggered");

                String extension = awsService.getFileExtensionByImageUrl(file.getOriginalFilename(), httpServletRequest);
                String fileName = inventoryFileData.getString("file_name") + "_" + System.nanoTime() + "." + extension;
                String key = awsService.addExportFileToAWSS3(file.getBytes(), resourceUrlConfig.getInventoryFileUrl(), resourceUrlConfig.getInventoryFileDirectory(),
                        null, fileName, httpServletRequest);
                log.info("Key:" + key);
                int expirationMinutes = 60 * 24; //valid for 24hrs
                String url = awsService.getPreSignedUrlForFileUpload(key, expirationMinutes, HttpMethod.GET);
                String encodedUrl = Base64.getEncoder().encodeToString(url.getBytes());
                String link = resourceUrlConfig.getEmailLink() + "?redirectUrl=" + encodedUrl;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("to", inventoryFileData.getString("email"));
                jsonObject.put("link", link);
                jsonObject.putAll(inventoryFileData);
                webClientAlertService.sendInventoryReport(jsonObject);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Inventory Alert sent successfully", 200, true);
            log.info("Inventory Alert sent successfully. Endpoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } else {
            log.info("Invalid client params. Endpoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }


}
