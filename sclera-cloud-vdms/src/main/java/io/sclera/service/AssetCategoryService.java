package io.sclera.service;


import com.alibaba.fastjson2.JSON;
import com.fasterxml.uuid.Generators;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.CategoryDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.AssetCategoryRepository;
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
public class AssetCategoryService {

    @Autowired
    private AssetCategoryRepository assetCategoryRepository;
    @Autowired
    private AssetSubCategoryService subCategoryService;
    @Autowired
    private AwsService awsService;
    @Autowired
    private ResourceUrlConfig resourceUrlConfig;
    @Autowired
    private UserActionLogService userActionLogService;

    public ResponseEntity<?> getAllCategory(String key, String sort, int pageNo, int pageSize, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: Key: {}, Sort: {}, PageNo: {}, PageSize: {}, LoggedInUser: {}", key, sort, pageNo, pageSize, loggedInUser);
        //replace '+' with '-' in search key
        String searchKey = key.replace(" ", "_");
        log.info("searchKey: {}", searchKey);
        int offset = pageSize * (pageNo - 1);
        List<CategoryDTO> categoryDTOS = assetCategoryRepository.getAllCategory(searchKey, sort, pageSize, offset);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(categoryDTOS, 200, true);
        log.info("Fetching List of Categories. EndPoint:{}" + httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> addCategory(String body, MultipartFile icon, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: Body: {}, LoggedInUser: {}", body, loggedInUser);
        CategoryDTO categoryDTO = JSON.parseObject(body, CategoryDTO.class);
        log.info("CategoryDTO:{}", categoryDTO);
        Integer isExist = assetCategoryRepository.checkCategoryByName(categoryDTO.getName());
        if (isExist == 1) {
            log.error("Category Name: {} Already Exists. EndPoint:{}", categoryDTO.getName(), httpServletRequest.getRequestURI());
            throw new ClientException("Category Already Exists", 744, httpServletRequest.getRequestURI());
        } else {
            BigInteger creationTimestamp = BigInteger.valueOf(System.currentTimeMillis());
            String id = Generators.timeBasedGenerator().generate().toString();
            if (icon != null) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                String iconLink = awsService.addFileToAWSS3(icon.getBytes(), resourceUrlConfig.getCategoryImageDirectory(),
                        resourceUrlConfig.getCategoryImageUrl(), extension, id, httpServletRequest);
                categoryDTO.setIconUrl(iconLink);
            }
            assetCategoryRepository.addCategory(id, categoryDTO.getName(), categoryDTO.getIconUrl(), categoryDTO.getDisplayName(), creationTimestamp);
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Category Added Successfully", 200, true);
            log.info("Category Added Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Category", "ADD", "Category With Name:" + categoryDTO.getName() + "Is Added", "success");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
    }

    public ResponseEntity<?> updateCategoryById(String categoryId, String body, MultipartFile icon, String loggedInUser, String iconUrl, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: CategoryId:{}, Body:{}, LoggedInUser: {}, ,iconUrl:{}", categoryId, body, loggedInUser, iconUrl);
        if (categoryId != null) {
            CategoryDTO categoryDTO = JSON.parseObject(body, CategoryDTO.class);
            log.info("CategoryDTO:{}", categoryDTO);
            Integer isExist = assetCategoryRepository.checkCategoryByIdAndName(categoryId, categoryDTO.getName());
            if (isExist == 1) {
                log.error("Category Name: {} Already Exists. EndPoint:{}", categoryDTO.getName(), httpServletRequest.getRequestURI());
                throw new ClientException("Category Already Exists", 744, httpServletRequest.getRequestURI());
            } else {
                String name = assetCategoryRepository.getNameById(categoryId);
                if (iconUrl != null) {
                    String fileName = awsService.getFileNameByImageUrl(iconUrl, httpServletRequest);
                    awsService.removeFileFromAWSS3(resourceUrlConfig.getCategoryImageDirectory(), fileName, httpServletRequest);
                    categoryDTO.setIconUrl(null);
                }
                if (icon != null) {
                    String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                    String iconLink = awsService.addFileToAWSS3(icon.getBytes(), resourceUrlConfig.getCategoryImageDirectory(),
                            resourceUrlConfig.getCategoryImageUrl(), extension, categoryId, httpServletRequest);
                    categoryDTO.setIconUrl(iconLink);
                }
                assetCategoryRepository.updateCategoryById(categoryDTO.getName(), categoryDTO.getIconUrl(), categoryDTO.getDisplayName(), categoryId);
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Category Updated Successfully", 200, true);
                log.info("Category Updated Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "Category", "UPDATE", "Category With Name:" + name + "Is updated", "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateCategoryIconById(String categoryId, String iconUrl, MultipartFile icon, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: CategoryId:{}, IconUrl:{}, LoggedInUser: {}", categoryId, iconUrl, loggedInUser);
        if (categoryId != null) {
            if (iconUrl != null) {
                String fileName = awsService.getFileNameByImageUrl(iconUrl, httpServletRequest);
                awsService.removeFileFromAWSS3(resourceUrlConfig.getCategoryImageDirectory(), fileName, httpServletRequest);
                assetCategoryRepository.updateCategoryIconById(null, categoryId);
            }

            if (icon != null) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                String iconLink = awsService.addFileToAWSS3(icon.getBytes(), resourceUrlConfig.getCategoryImageDirectory(),
                        resourceUrlConfig.getCategoryImageUrl(), extension, categoryId, httpServletRequest);
                assetCategoryRepository.updateCategoryIconById(iconLink, categoryId);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Category Icon Updated Successfully", 200, true);
            log.info("Category Icon Updated Successfully For Category Id: {}, Endpoint: {}", categoryId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteCategory(List<String> categoryIds, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: CategoryIds:{}, LoggedInUser: {}", categoryIds, loggedInUser);
        if (categoryIds != null) {
            subCategoryService.deleteSubcategoryByCategoryId(categoryIds, loggedInUser, httpServletRequest);
            List<String> urls = new ArrayList<>();
            List<String> imageUrls = assetCategoryRepository.getImageUrlsByIds(categoryIds);
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (String imageUrl : imageUrls) {
                    String fileName = awsService.getFileNameByImageUrl(imageUrl, httpServletRequest);
                    String directory = resourceUrlConfig.getCategoryImageDirectory();
                    urls.add(directory + fileName);
                }
                if (!urls.isEmpty()) {
                    awsService.removeFilesFromAWSS3(urls);
                }
            }
            assetCategoryRepository.deleteCategoryByIds(categoryIds);
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Category(s) Deleted Successfully", 200, true);
            log.info("Category(s) Deleted Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Category", "DELETE", "Category(s) Deleted Successfully", "success");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateCategoryDisplayName(HttpServletRequest httpServletRequest) {
        int offset = 1000 * (1 - 1);
        List<CategoryDTO> categoryDTOS = assetCategoryRepository.getAllCategory("all", "creation_timestamp", 1000, offset);
        log.info("categoryDTOS:{}", categoryDTOS);
        for (CategoryDTO categoryDTO : categoryDTOS) {
            String displayName = categoryDTO.getName().replace("_", " ");
            displayName = capitalizeWords(displayName);
            assetCategoryRepository.updateCategoryById(categoryDTO.getName(), categoryDTO.getIconUrl(), displayName, categoryDTO.getId());
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Category Display Name Added Successfully", 200, true);
        log.info("Category Display Name Added Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        String[] words = str.split(" ");
        StringBuilder capitalizedString = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                capitalizedString.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase());
            }
            capitalizedString.append(" ");
        }
        // Remove the trailing space
        return capitalizedString.toString().trim();
    }
}
