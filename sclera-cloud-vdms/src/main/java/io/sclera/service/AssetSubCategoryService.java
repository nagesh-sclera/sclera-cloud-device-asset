package io.sclera.service;


import com.alibaba.fastjson2.JSON;
import com.fasterxml.uuid.Generators;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.CategoryDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.SubCategoryDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.AssetSubCategoryRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class AssetSubCategoryService {

    @Autowired
    private AssetSubCategoryRepository assetSubCategoryRepository;

    @Autowired
    private AwsService awsService;
    @Autowired
    private ResourceUrlConfig resourceUrlConfig;
    @Autowired
    private UserActionLogService userActionLogService;

    @Autowired
    private DigitalTwinTemplateService digitalTwinTemplateService;

    public ResponseEntity<?> getSubCategoryByCategoryId(String categoryId, String key, String sort, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: CategoryId:{}, Key: {}, Sort: {}, LoggedInUser: {}", categoryId, key, sort, loggedInUser);
        if (categoryId != null) {
            //replace '+' with '-' in search key
            String searchKey = key.replace(" ", "_");
            log.info("searchKey: {}", searchKey);

            List<SubCategoryDTO> subCategoryDTO = assetSubCategoryRepository.getSubCategoryByCategoryId(categoryId, searchKey, sort);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(subCategoryDTO, 200, true);
            log.info("Fetching List of Subcategory by CategoryId: {}. EndPoint:{}", categoryId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> addSubCategoryByCategoryId(String categoryId, String body, MultipartFile icon, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: CategoryId: {}, Body:{}, LoggedInUser: {}", categoryId, body, loggedInUser);
        if (categoryId != null) {
            SubCategoryDTO subCategoryDTO = JSON.parseObject(body, SubCategoryDTO.class);
            log.info("SubCategoryDTO:{}", subCategoryDTO);
            Integer isExist = assetSubCategoryRepository.checkSubCategoryByNameAndCategoryId(subCategoryDTO.getName(), categoryId);
            if (isExist == 1) {
                log.error("SubCategory Name: {} Already Exists. EndPoint:{}", subCategoryDTO.getName(), httpServletRequest.getRequestURI());
                throw new ClientException("SubCategory Already Exists", 745, httpServletRequest.getRequestURI());
            } else {
                BigInteger creationTimestamp = BigInteger.valueOf(System.currentTimeMillis());
                String subCategoryId = Generators.timeBasedGenerator().generate().toString();
                if (icon != null) {
                    String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                    String url = String.format(resourceUrlConfig.getSubCategoryImageUrl(), categoryId);
                    String directory = String.format(resourceUrlConfig.getSubCategoryImageDirectory(), categoryId);
                    String iconLink = awsService.addFileToAWSS3(icon.getBytes(), directory, url, extension, subCategoryId, httpServletRequest);
                    subCategoryDTO.setIconUrl(iconLink);
                }
                assetSubCategoryRepository.addSubCategoryByCategoryId(subCategoryId, subCategoryDTO.getName(),
                        subCategoryDTO.getIconUrl(), subCategoryDTO.getDisplayName(), creationTimestamp, categoryId);
                userActionLogService.addUserActionLog(loggedInUser, "SubCategory", "ADD", "SubCategory With Name:" + subCategoryDTO.getName() + "Is Added For Category: " + categoryId, "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload("SubCategory Added Successfully", 200, true);
                log.info("SubCategory With Name: {} Is Added Successfully For CategoryId: {}. Endpoint: {}", subCategoryDTO.getName(), categoryId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateSubCategoryByCategoryAndSubcategoryId(String categoryId, String subCategoryId, String body, MultipartFile icon,
                                                                         String loggedInUser, String iconUrl, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: CategoryId: {}, SubCategoryId: {}, SubCategoryDTO:{}, LoggedInUser: {},iconUrl:{}"
                , categoryId, subCategoryId, body, loggedInUser, iconUrl);
        if (categoryId != null && subCategoryId != null) {
            SubCategoryDTO subCategoryDTO = JSON.parseObject(body, SubCategoryDTO.class);
            log.info("SubCategoryDTO:{}", subCategoryDTO);
            Integer isExist = assetSubCategoryRepository.checkSubCategoryByIdAndName(subCategoryId, subCategoryDTO.getName(), categoryId);
            if (isExist == 1) {
                log.error("SubCategory Name: {} Already Exists. EndPoint:{}", subCategoryDTO.getName(), httpServletRequest.getRequestURI());
                throw new ClientException("SubCategory Already Exists", 745, httpServletRequest.getRequestURI());
            } else {
                String name = assetSubCategoryRepository.getNameById(subCategoryId);
                if (iconUrl != null) {
                    String fileName = awsService.getFileNameByImageUrl(iconUrl, httpServletRequest);
                    String directory = String.format(resourceUrlConfig.getSubCategoryImageDirectory(), categoryId);
                    awsService.removeFileFromAWSS3(directory, fileName, httpServletRequest);
                    subCategoryDTO.setIconUrl(null);
                }
                if (icon != null) {
                    String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                    String url = String.format(resourceUrlConfig.getSubCategoryImageUrl(), categoryId);
                    String directory = String.format(resourceUrlConfig.getSubCategoryImageDirectory(), categoryId);
                    String dbImageURL = awsService.addFileToAWSS3(icon.getBytes(), directory, url, extension, subCategoryId, httpServletRequest);
                    subCategoryDTO.setIconUrl(dbImageURL);
                }
                assetSubCategoryRepository.updateSubCategoryByCategoryAndSubcategoryId(subCategoryDTO.getName(), subCategoryDTO.getIconUrl(),
                        subCategoryDTO.getDisplayName(), subCategoryId, categoryId);
                ResponseDTO responseDTO = ScleraUtils.generatePayload("SubCategory Updated Successfully", 200, true);
                log.info("SubCategory With Name: {} Is Updated Successfully For CategoryId: {}. Endpoint: {}", name, categoryId, httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "SubCategory", "UPDATE", "SubCategory With Name:" + name + "Is Updated For Category: " + categoryId, "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateSubCategoryIconByCategoryAndSubcategoryId(String categoryId, String subCategoryId, String iconUrl, MultipartFile icon, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: CategoryId: {}, SubCategoryId: {}, IconUrl:{}, LoggedInUser: {}", categoryId, subCategoryId, iconUrl, loggedInUser);
        if (categoryId != null && subCategoryId != null) {
            if (iconUrl != null) {
                String fileName = awsService.getFileNameByImageUrl(iconUrl, httpServletRequest);
                String directory = String.format(resourceUrlConfig.getSubCategoryImageDirectory(), categoryId);
                awsService.removeFileFromAWSS3(directory, fileName, httpServletRequest);
                assetSubCategoryRepository.updateSubCategoryIconById(null, subCategoryId, categoryId);
            }

            if (icon != null) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                String url = String.format(resourceUrlConfig.getSubCategoryImageUrl(), categoryId);
                String directory = String.format(resourceUrlConfig.getSubCategoryImageDirectory(), categoryId);
                String iconLink = awsService.addFileToAWSS3(icon.getBytes(), directory, url, extension, categoryId, httpServletRequest);
                assetSubCategoryRepository.updateSubCategoryIconById(iconLink, subCategoryId, categoryId);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("SubCategory Icon Updated Successfully", 200, true);
            log.info("SubCategory Icon Updated Successfully For Category Id: {}, Endpoint: {}", categoryId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteSubcategoryByCategoryAndSubcategoryId(String categoryId, List<String> subCategoryIds, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: CategoryId: {}, SubCategoryIds: {}, LoggedInUser: {}", categoryId, subCategoryIds, loggedInUser);
        if (categoryId != null && subCategoryIds != null) {
            Integer isTagged = digitalTwinTemplateService.checkTaggedSubCategory(subCategoryIds);
            if (isTagged == 0) {
                List<String> imageUrls = assetSubCategoryRepository.getImageUrlsByIds(subCategoryIds);
                List<String> urls = new ArrayList<>();
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    for (String imageUrl : imageUrls) {
                        String fileName = awsService.getFileNameByImageUrl(imageUrl, httpServletRequest);
                        String directory = String.format(resourceUrlConfig.getSubCategoryImageDirectory(), categoryId);
                        urls.add(directory + fileName);
                    }
                    if (!urls.isEmpty()) {
                        awsService.removeFilesFromAWSS3(urls);
                    }
                }
                assetSubCategoryRepository.deleteSubcategoryByCategoryAndSubcategoryIds(categoryId, subCategoryIds);
                ResponseDTO responseDTO = ScleraUtils.generatePayload("SubCategory(s) Deleted Successfully", 200, true);
                log.info("SubCategory(s) Deleted Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "SubCategory", "DELETE", "SubCategory(s) Deleted Successfully", "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                userActionLogService.addUserActionLog(loggedInUser, "SubCategory", "DELETE", "Unable To Delete SubCategory(s) As It Is Tagged to Digital Twin", "failed");
                log.error("Unable To Delete SubCategory(s) As It Is Tagged to Digital Twin. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unable To Delete SubCategory(s) As It Is Tagged to Digital Twin", 791, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public void deleteSubcategoryByCategoryId(List<String> categoryIds, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: CategoryIds:{}", categoryIds);
        List<String> subCategoryIds = assetSubCategoryRepository.getSubCategoryIdsByCategoryIds(categoryIds);
        Integer isTagged = digitalTwinTemplateService.checkTaggedSubCategory(subCategoryIds);
        if (isTagged == 0) {
            for (String categoryId : categoryIds) {
                List<String> filenames = assetSubCategoryRepository.getImageUrlByCategoryId(categoryId);
                if (filenames != null && !filenames.isEmpty()) {
                    List<String> urls = new ArrayList<>();
                    for (String filename : filenames) {
                        String directory = String.format(resourceUrlConfig.getSubCategoryImageDirectory(), categoryId);
                        urls.add(directory + filename);
                    }
                    assetSubCategoryRepository.deleteSubcategoryByCategoryId(categoryId);
                    if (!urls.isEmpty()) {
                        awsService.removeFilesFromAWSS3(urls);
                    }
                }
            }
        } else {
            userActionLogService.addUserActionLog(loggedInUser, "SubCategory", "DELETE", "Unable To Delete SubCategory(s) of Category(s) As It Is Tagged to Digital Twin", "failed");
            log.error("Unable To Delete SubCategory(s) of Category(s) As It Is Tagged to Digital Twin. Endpoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Unable To Delete SubCategory(s) of Category(s) As It Is Tagged to Digital Twin", 790, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateBulkSubCategoryIcon(String categoryId, List<MultipartFile> icons, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: CategoryId:{}, LoggedInUser: {}", categoryId, loggedInUser);
        List<SubCategoryDTO> subCategoryDTOS = assetSubCategoryRepository.getAllSubCategories();
        for (SubCategoryDTO subCategoryDTO : subCategoryDTOS) {
            for (MultipartFile icon : icons) {
                String fileName = icon.getOriginalFilename().substring(0, icon.getOriginalFilename().lastIndexOf("."));
                if (subCategoryDTO.getName().equals(fileName)) {
                    String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                    String url = String.format(resourceUrlConfig.getSubCategoryImageUrl(), categoryId);
                    String directory = String.format(resourceUrlConfig.getSubCategoryImageDirectory(), categoryId);
                    String iconURL = awsService.addFileToAWSS3(icon.getBytes(), directory, url, extension, subCategoryDTO.getId(), httpServletRequest);
                    assetSubCategoryRepository.updateSubCategoryIconUrl(iconURL, subCategoryDTO.getId());
                }
            }
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload("SubCategory Icon Updated Successfully", 200, true);
        log.info("Successfully Updated SubCategory Icon.EndPoint:{}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> updateSubCategoryDisplayName(HttpServletRequest httpServletRequest) {
        List<SubCategoryDTO> categoryDTOS = assetSubCategoryRepository.getAllSubCategories();
        log.info("categoryDTOS:{}", categoryDTOS);
        for (SubCategoryDTO categoryDTO : categoryDTOS) {
            String displayName = categoryDTO.getName().replace("_", " ");
            displayName = capitalizeWords(displayName);
            String assetCategoryId = assetSubCategoryRepository.getAssetCategoryIdById(categoryDTO.getId());
            assetSubCategoryRepository.updateSubCategoryByCategoryAndSubcategoryId(categoryDTO.getName(), categoryDTO.getIconUrl(), displayName, categoryDTO.getId(),assetCategoryId);
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Sub-Category Display Name Added Successfully", 200, true);
        log.info("Sub-Category Display Name Added Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
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
