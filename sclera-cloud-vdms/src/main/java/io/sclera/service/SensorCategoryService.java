package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.uuid.Generators;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.CategoryDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.SensorCategoryRepository;
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
public class SensorCategoryService {

    @Autowired
    private SensorCategoryRepository sensorCategoryRepository;

    @Autowired
    private SensorSubcategoryService sensorSubcategoryService;

    @Autowired
    private AwsService awsService;
    @Autowired
    private ResourceUrlConfig resourceUrlConfig;
    @Autowired
    private UserActionLogService userActionLogService;

    public ResponseEntity<?> getAllSensorCategory(String key, String sort, int pageNo, int pageSize, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: Key: {}, Sort: {}, PageNo: {}, PageSize: {}, LoggedInUser: {}", key, sort, pageNo, pageSize, loggedInUser);
        //replace '+' with '-' in search key
        int offset = pageSize * (pageNo - 1);
        List<CategoryDTO> categoryDTOS = sensorCategoryRepository.getAllSensorCategory(key, sort, pageSize, offset);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(categoryDTOS, 200, true);
        log.info("Fetching List of Sensor Categories. EndPoint:{}" + httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> addSensorCategory(String body, MultipartFile icon, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: Body: {}, LoggedInUser: {}", body, loggedInUser);
        CategoryDTO sensorCategoryDTO = JSON.parseObject(body, CategoryDTO.class);
        log.info("sensorCategoryDTO:{}", sensorCategoryDTO);
        Integer isExist = sensorCategoryRepository.checkSensorCategoryByName(sensorCategoryDTO.getName());
        if (isExist == 1) {
            log.error("Sensor Category Name: {} Already Exists. EndPoint:{}", sensorCategoryDTO.getName(), httpServletRequest.getRequestURI());
            throw new ClientException("Sensor Category Already Exists", 797, httpServletRequest.getRequestURI());
        } else {
            BigInteger creationTimestamp = BigInteger.valueOf(System.currentTimeMillis());
            String id = Generators.timeBasedGenerator().generate().toString();
            if (icon != null) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                String iconLink = awsService.addFileToAWSS3(icon.getBytes(), resourceUrlConfig.getSensorCategoryImageDirectory(),
                        resourceUrlConfig.getSensorCategoryImageUrl(), extension, id, httpServletRequest);
                sensorCategoryDTO.setIconUrl(iconLink);
            }
            sensorCategoryRepository.addSensorCategory(id, sensorCategoryDTO.getName(), sensorCategoryDTO.getIconUrl(),
                    sensorCategoryDTO.getDisplayName(), creationTimestamp);
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Sensor Category Added Successfully", 200, true);
            log.info("Sensor Category Added Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "SensorCategory", "ADD", "Sensor Category With Name:" + sensorCategoryDTO.getName() + "Is Added", "success");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
    }

    public ResponseEntity<?> updateSensorCategoryById(String sensorCategoryId, String body, MultipartFile icon, String loggedInUser, String iconUrl, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: SensorCategoryId:{}, Body:{}, LoggedInUser: {},iconUrl:{}", sensorCategoryId, body, loggedInUser, iconUrl);
        if (sensorCategoryId != null) {
            CategoryDTO categoryDTO = JSON.parseObject(body, CategoryDTO.class);
            log.info("CategoryDTO:{}", categoryDTO);
            Integer isExist = sensorCategoryRepository.checkSensorCategoryByIdAndName(sensorCategoryId, categoryDTO.getName());
            if (isExist == 1) {
                log.error("Sensor Category Name: {} Already Exists. EndPoint:{}", categoryDTO.getName(), httpServletRequest.getRequestURI());
                throw new ClientException("Sensor Category Already Exists", 797, httpServletRequest.getRequestURI());
            } else {
                String name = sensorCategoryRepository.getNameById(sensorCategoryId);
                if (iconUrl != null) {
                    String fileName = awsService.getFileNameByImageUrl(iconUrl, httpServletRequest);
                    log.info("fileName2:" + fileName);
                    awsService.removeFileFromAWSS3(resourceUrlConfig.getSensorCategoryImageDirectory(), fileName, httpServletRequest);
                    categoryDTO.setIconUrl(null);
                }
                if (icon != null) {
                    String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                    String iconLink = awsService.addFileToAWSS3(icon.getBytes(), resourceUrlConfig.getSensorCategoryImageDirectory(),
                            resourceUrlConfig.getSensorCategoryImageUrl(), extension, sensorCategoryId, httpServletRequest);
                    categoryDTO.setIconUrl(iconLink);
                }
                sensorCategoryRepository.updateSensorCategoryById(categoryDTO.getName(), categoryDTO.getIconUrl(),
                        categoryDTO.getDisplayName(), sensorCategoryId);
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Sensor Category Updated Successfully", 200, true);
                log.info("Sensor Category Updated Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "SensorCategory", "UPDATE", "Sensor Category With Name:" + name + "Is updated", "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateSensorCategoryIconById(String sensorCategoryId, String iconUrl, MultipartFile icon, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: SensorCategoryId:{}, IconUrl:{}, LoggedInUser: {}", sensorCategoryId, iconUrl, loggedInUser);
        if (sensorCategoryId != null) {
            if (iconUrl != null) {
                String fileName = awsService.getFileNameByImageUrl(iconUrl, httpServletRequest);
                awsService.removeFileFromAWSS3(resourceUrlConfig.getSensorCategoryImageDirectory(), fileName, httpServletRequest);
                sensorCategoryRepository.updateSensorCategoryIconById(null, sensorCategoryId);
            }

            if (icon != null) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                String iconLink = awsService.addFileToAWSS3(icon.getBytes(), resourceUrlConfig.getSensorCategoryImageDirectory(),
                        resourceUrlConfig.getSensorCategoryImageUrl(), extension, sensorCategoryId, httpServletRequest);
                sensorCategoryRepository.updateSensorCategoryIconById(iconLink, sensorCategoryId);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Sensor Category Icon Updated Successfully", 200, true);
            log.info("Sensor Category Icon Updated Successfully For Category Id: {}, Endpoint: {}", sensorCategoryId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteSensorCategory(List<String> sensorCategoryIds, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: SensorCategoryIds:{}, LoggedInUser: {}", sensorCategoryIds, loggedInUser);
        if (sensorCategoryIds != null) {
            sensorSubcategoryService.deleteSensorSubcategoryBySensorCategoryId(sensorCategoryIds, loggedInUser, httpServletRequest);
            List<String> urls = new ArrayList<>();
            List<String> imageUrls = sensorCategoryRepository.getImageUrlsByIds(sensorCategoryIds);
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (String imageUrl : imageUrls) {
                    String fileName = awsService.getFileNameByImageUrl(imageUrl, httpServletRequest);
                    String directory = resourceUrlConfig.getSensorCategoryImageDirectory();
                    urls.add(directory + fileName);
                }
                if (!urls.isEmpty()) {
                    awsService.removeFilesFromAWSS3(urls);
                }
            }
            sensorCategoryRepository.deleteSensorCategoryByIds(sensorCategoryIds);
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Sensor Category(s) Deleted Successfully", 200, true);
            log.info("Sensor Category(s) Deleted Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "SensorCategory", "DELETE", "Sensor Category(s) Deleted Successfully", "success");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> addBulkSensorCategory(List<CategoryDTO> categoryDTOS, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: categoryDTOS: {}, LoggedInUser: {}", categoryDTOS, loggedInUser);
        for (CategoryDTO sensorCategoryDTO : categoryDTOS) {
            Integer isExist = sensorCategoryRepository.checkSensorCategoryByName(sensorCategoryDTO.getName());
            if (isExist == 1) {
                log.error("Sensor Category Name: {} Already Exists. EndPoint:{}", sensorCategoryDTO.getName(), httpServletRequest.getRequestURI());
                throw new ClientException("Sensor Category Already Exists", 797, httpServletRequest.getRequestURI());
            } else {
                BigInteger creationTimestamp = BigInteger.valueOf(System.currentTimeMillis());
                String id = Generators.timeBasedGenerator().generate().toString();
                sensorCategoryRepository.addSensorCategory(id, sensorCategoryDTO.getName(), sensorCategoryDTO.getIconUrl(),
                        sensorCategoryDTO.getDisplayName(), creationTimestamp);
                userActionLogService.addUserActionLog(loggedInUser, "SensorCategory", "ADD", "Sensor Category With Name:" + sensorCategoryDTO.getName() + "Is Added", "success");
            }
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Sensor Category Added Successfully", 200, true);
        log.info("Sensor Category Added Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public String getSensorCategoryIdByDisplayName(String sensorCategoryGroupName) {
        return sensorCategoryRepository.getSensorCategoryIdByDisplayName(sensorCategoryGroupName);
    }


    public String getIconUrlByCategoryName(String category) {
        log.info("Fetching Icon Url by Category Name:{}",category);
        return sensorCategoryRepository.getIconUrlByCategoryName(category);
    }
}
