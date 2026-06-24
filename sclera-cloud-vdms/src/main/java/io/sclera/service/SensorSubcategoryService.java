package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.uuid.Generators;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.CategoryDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.SubCategoryDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.SensorSubcategoryRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SensorSubcategoryService {

    @Autowired
    private SensorSubcategoryRepository sensorSubcategoryRepository;

    @Autowired
    private AwsService awsService;

    @Autowired
    private ResourceUrlConfig resourceUrlConfig;

    @Autowired
    private UserActionLogService userActionLogService;

    @Autowired
    private SensorCategoryService sensorCategoryService;

    public ResponseEntity<?> getSensorSubCategoryBySensorCategoryId(String sensorCategoryId, String key, String sort, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: SensorCategoryId:{}, Key: {}, Sort: {}, LoggedInUser: {}", sensorCategoryId, key, sort, loggedInUser);
        if (sensorCategoryId != null) {
            //replace '+' with '-' in search key
            List<SubCategoryDTO> subCategoryDTO = sensorSubcategoryRepository.getSensorSubCategoryBySensorCategoryId(sensorCategoryId, key, sort);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(subCategoryDTO, 200, true);
            log.info("Fetching List of Sensor Subcategory by Sensor CategoryId: {}. EndPoint:{}", sensorCategoryId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> addSensorSubCategoryBySensorCategoryId(String sensorCategoryId, String body, MultipartFile icon, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: SensorCategoryId: {}, Body:{}, LoggedInUser: {}", sensorCategoryId, body, loggedInUser);
        if (sensorCategoryId != null) {
            SubCategoryDTO sensorSubCategoryDTO = JSON.parseObject(body, SubCategoryDTO.class);
            log.info("SensorSubCategoryDTO:{}", sensorSubCategoryDTO);
            Integer isExist = sensorSubcategoryRepository.checkSensorSubCategoryByNameAndCategoryId(sensorSubCategoryDTO.getName(), sensorCategoryId);
            if (isExist == 1) {
                log.error("SensorSubCategory Name: {} Already Exists. EndPoint:{}", sensorSubCategoryDTO.getName(), httpServletRequest.getRequestURI());
                throw new ClientException("Sensor SubCategory Already Exists", 798, httpServletRequest.getRequestURI());
            } else {
                BigInteger creationTimestamp = BigInteger.valueOf(System.currentTimeMillis());
                String sensorSubCategoryId = Generators.timeBasedGenerator().generate().toString();
                if (icon != null) {
                    String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                    String url = String.format(resourceUrlConfig.getSensorSubCategoryImageUrl(), sensorCategoryId);
                    String directory = String.format(resourceUrlConfig.getSensorSubCategoryImageDirectory(), sensorCategoryId);
                    String iconLink = awsService.addFileToAWSS3(icon.getBytes(), directory, url, extension, sensorSubCategoryId, httpServletRequest);
                    sensorSubCategoryDTO.setIconUrl(iconLink);
                }
                sensorSubcategoryRepository.addSensorSubCategoryByCategoryId(sensorSubCategoryId, sensorSubCategoryDTO.getName(),
                        sensorSubCategoryDTO.getIconUrl(), sensorSubCategoryDTO.getDisplayName(),
                        creationTimestamp, sensorCategoryId);
                userActionLogService.addUserActionLog(loggedInUser, "SensorSubCategory", "ADD",
                        "Sensor SubCategory With Name:" + sensorSubCategoryDTO.getName() + "Is Added For Sensor Category: " + sensorCategoryId, "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload("SensorSubCategory Added Successfully", 200, true);
                log.info("SensorSubCategory With Name: {} Is Added Successfully For" +
                        " SensorCategoryId: {}. Endpoint: {}", sensorSubCategoryDTO.getName(), sensorCategoryId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateSensorSubCategoryBySensorCategoryAndSensorSubcategoryId(String sensorCategoryId, String sensorSubCategoryId,
                                                                                           String body, MultipartFile icon, String loggedInUser,
                                                                                           String iconUrl, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: sensorCategoryId: {}, sensorSubCategoryId: {}, SensorSubCategoryDTO:{}, LoggedInUser: {},,iconUrl:{}",
                sensorCategoryId, sensorSubCategoryId, body, loggedInUser, iconUrl);
        if (sensorCategoryId != null && sensorSubCategoryId != null) {
            SubCategoryDTO sensorSubCategoryDTO = JSON.parseObject(body, SubCategoryDTO.class);
            log.info("SensorSubCategoryDTO:{}", sensorSubCategoryDTO);
            Integer isExist = sensorSubcategoryRepository.checkSensorSubCategoryByIdAndName(sensorSubCategoryId, sensorSubCategoryDTO.getName(), sensorCategoryId);
            if (isExist == 1) {
                log.error("SensorSubCategory Name: {} Already Exists. EndPoint:{}", sensorSubCategoryDTO.getName(), httpServletRequest.getRequestURI());
                throw new ClientException("Sensor SubCategory Already Exists", 798, httpServletRequest.getRequestURI());
            } else {
                String name = sensorSubcategoryRepository.getNameById(sensorSubCategoryId);
                if (iconUrl != null) {
                    String fileName = awsService.getFileNameByImageUrl(iconUrl, httpServletRequest);
                    String directory = String.format(resourceUrlConfig.getSensorSubCategoryImageDirectory(), sensorCategoryId);
                    awsService.removeFileFromAWSS3(directory, fileName, httpServletRequest);
                    sensorSubCategoryDTO.setIconUrl(null);
                }
                if (icon != null) {
                    String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                    String url = String.format(resourceUrlConfig.getSensorSubCategoryImageUrl(), sensorCategoryId);
                    String directory = String.format(resourceUrlConfig.getSensorSubCategoryImageDirectory(), sensorCategoryId);
                    String dbImageURL = awsService.addFileToAWSS3(icon.getBytes(), directory, url, extension, sensorSubCategoryId, httpServletRequest);
                    sensorSubCategoryDTO.setIconUrl(dbImageURL);
                }
                sensorSubcategoryRepository.updateSubCategoryByCategoryAndSubcategoryId(sensorSubCategoryDTO.getName(),
                        sensorSubCategoryDTO.getIconUrl(), sensorSubCategoryDTO.getDisplayName(), sensorSubCategoryId, sensorCategoryId);
                ResponseDTO responseDTO = ScleraUtils.generatePayload("SensorSubCategory Updated Successfully", 200, true);
                log.info("SensorSubCategory With Name: {} Is Updated Successfully For SensorCategoryId: {}. Endpoint: {}", name,
                        sensorCategoryId, httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "SensorSubCategory", "UPDATE",
                        "SensorSubCategory With Name:" + name + "Is Updated For SensorCategory: " + sensorCategoryId, "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }


    public ResponseEntity<?> updateSensorSubCategoryIconBySensorCategoryAndSensorSubcategoryId(String sensorCategoryId, String sensorSubCategoryId, String iconUrl, MultipartFile icon, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: sensorCategoryId: {}, sensorSubCategoryId: {}, IconUrl:{}, LoggedInUser: {}", sensorCategoryId, sensorSubCategoryId, iconUrl, loggedInUser);
        if (sensorCategoryId != null && sensorSubCategoryId != null) {
            if (iconUrl != null) {
                String fileName = awsService.getFileNameByImageUrl(iconUrl, httpServletRequest);
                String directory = String.format(resourceUrlConfig.getSensorSubCategoryImageDirectory(), sensorCategoryId);
                awsService.removeFileFromAWSS3(directory, fileName, httpServletRequest);
                sensorSubcategoryRepository.updateSensorSubCategoryIconById(null, sensorSubCategoryId, sensorCategoryId);
            }

            if (icon != null) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                String url = String.format(resourceUrlConfig.getSensorSubCategoryImageUrl(), sensorCategoryId);
                String directory = String.format(resourceUrlConfig.getSensorSubCategoryImageDirectory(), sensorCategoryId);
                String iconLink = awsService.addFileToAWSS3(icon.getBytes(), directory, url, extension, sensorCategoryId, httpServletRequest);
                sensorSubcategoryRepository.updateSensorSubCategoryIconById(iconLink, sensorSubCategoryId, sensorCategoryId);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("SensorSubCategory Icon Updated Successfully", 200, true);
            log.info("SensorSubCategory Icon Updated Successfully For SensorCategory Id: {}, Endpoint: {}", sensorCategoryId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteSensorSubcategoryBySensorCategoryAndSensorSubcategoryId(String sensorCategoryId, List<String> sensorSubCategoryIds, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: sensorCategoryId: {}, sensorSubCategoryIds: {}, LoggedInUser: {}", sensorCategoryId, sensorSubCategoryIds, loggedInUser);
        if (sensorCategoryId != null && sensorSubCategoryIds != null) {
            List<String> imageUrls = sensorSubcategoryRepository.getSensorImageUrlsByIds(sensorSubCategoryIds);
            List<String> urls = new ArrayList<>();
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (String imageUrl : imageUrls) {
                    String fileName = awsService.getFileNameByImageUrl(imageUrl, httpServletRequest);
                    String directory = String.format(resourceUrlConfig.getSensorSubCategoryImageDirectory(), sensorCategoryId);
                    urls.add(directory + fileName);
                }
                if (!urls.isEmpty()) {
                    awsService.removeFilesFromAWSS3(urls);
                }
            }
            sensorSubcategoryRepository.deleteSensorSubcategoryBySensorCategoryAndSensorSubcategoryIds(sensorCategoryId, sensorSubCategoryIds);
            ResponseDTO responseDTO = ScleraUtils.generatePayload("SensorSubCategory(s) Deleted Successfully", 200, true);
            log.info("SensorSubCategory(s) Deleted Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "SensorSubCategory", "DELETE", "SensorSubCategory(s) Deleted Successfully", "success");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public void deleteSensorSubcategoryBySensorCategoryId(List<String> sensorCategoryIds, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: sensorCategoryIds:{}", sensorCategoryIds);
        for (String sensorCategoryId : sensorCategoryIds) {
            List<String> filenames = sensorSubcategoryRepository.getImageUrlBySensorCategoryId(sensorCategoryId);
            if (filenames != null && !filenames.isEmpty()) {
                List<String> urls = new ArrayList<>();
                for (String filename : filenames) {
                    String directory = String.format(resourceUrlConfig.getSensorSubCategoryImageDirectory(), sensorCategoryId);
                    urls.add(directory + filename);
                }
                sensorSubcategoryRepository.deleteSensorSubcategoryByCategoryId(sensorCategoryId);
                if (!urls.isEmpty()) {
                    awsService.removeFilesFromAWSS3(urls);
                }
            }
        }
    }


    public ResponseEntity<?> addBulkSensorSubCategoryBySensorCategoryId(List<CategoryDTO> categoryDTOS, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: categoryDTOS:{}, LoggedInUser: {}", categoryDTOS, loggedInUser);
        if (categoryDTOS != null) {
            for (CategoryDTO sensorSubCategoryDTO : categoryDTOS) {
                String sensorCategoryId = sensorCategoryService.getSensorCategoryIdByDisplayName(sensorSubCategoryDTO.getSensorCategoryGroupName());
                Integer isExist = sensorSubcategoryRepository.checkSensorSubCategoryByNameAndCategoryId(sensorSubCategoryDTO.getName(), sensorCategoryId);
                if (isExist == 1) {
                    log.error("SensorSubCategory Name: {} Already Exists. EndPoint:{}", sensorSubCategoryDTO.getName(), httpServletRequest.getRequestURI());
                    throw new ClientException("Sensor SubCategory Already Exists", 798, httpServletRequest.getRequestURI());
                } else {
                    BigInteger creationTimestamp = BigInteger.valueOf(System.currentTimeMillis());
                    String sensorSubCategoryId = Generators.timeBasedGenerator().generate().toString();
                    sensorSubcategoryRepository.addSensorSubCategoryByCategoryId(sensorSubCategoryId, sensorSubCategoryDTO.getName(),
                            sensorSubCategoryDTO.getIconUrl(), sensorSubCategoryDTO.getDisplayName(),
                            creationTimestamp, sensorCategoryId);
                    userActionLogService.addUserActionLog(loggedInUser, "SensorSubCategory", "ADD",
                            "Sensor SubCategory With Name:" + sensorSubCategoryDTO.getName() + "Is Added For Sensor Category: " + sensorCategoryId, "success");
                    log.info("SensorSubCategory With Name: {} Is Added Successfully For" +
                            " SensorCategoryId: {}. Endpoint: {}", sensorSubCategoryDTO.getName(), sensorCategoryId, httpServletRequest.getRequestURI());
                }

            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("SensorSubCategory Added Successfully", 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getSensorSubCategoryBySensorCategoryName(String sensorCategoryName, String key, String sort, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: SensorCategoryName:{}, Key: {}, Sort: {}, LoggedInUser: {}", sensorCategoryName, key, sort, loggedInUser);
        if (sensorCategoryName != null) {
            //replace '+' with '-' in search key
            List<SubCategoryDTO> subCategoryDTO = sensorSubcategoryRepository.getSensorSubCategoryBySensorCategoryName(sensorCategoryName, key, sort);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(subCategoryDTO, 200, true);
            log.info("Fetching List of Sensor Subcategory by Sensor Category Name: {}. EndPoint:{}", sensorCategoryName, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }
}
