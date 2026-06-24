package io.sclera.service;


import com.alibaba.fastjson2.JSON;
import com.fasterxml.uuid.Generators;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.CategoryDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.AssetTypeRepository;
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

@Slf4j
@Service
public class AssetTypeService {

    @Autowired
    private AssetTypeRepository assetTypeRepository;
    @Autowired
    private AwsService awsService;
    @Autowired
    private ResourceUrlConfig resourceUrlConfig;
    @Autowired
    private UserActionLogService userActionLogService;

    @Autowired
    private AssetTypeGroupService assetTypeGroupService;

    public ResponseEntity<?> getAllAssetTypes(String key, String sort, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Key: {}, Sort: {}, LoggedInUser: {}", key, sort, loggedInUser);
        String searchKey = key.replace(" ", "_");
        log.info("searchKey: {}", searchKey);
        List<CategoryDTO> assetTypeDTO = assetTypeRepository.getAllAssetTypes(searchKey, sort);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(assetTypeDTO, 200, true);
        log.info("Fetching List of AssetType. EndPoint:{}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getAssetType(String assetTypeGroupName, String key, String sort, int pageNo, int pageSize, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:assetTypeGroupName:{},Key: {}, Sort: {}, LoggedInUser: {},pageNo:{},pageSize:{}", assetTypeGroupName, key, sort, loggedInUser, pageNo, pageSize);
        if (assetTypeGroupName != null) {
            //replace '+' with '-' in search key
            int offset = pageSize * (pageNo - 1);
            List<CategoryDTO> assetTypeDTO = assetTypeRepository.getAssetType(assetTypeGroupName, key, sort, pageSize, offset);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(assetTypeDTO, 200, true);
            log.info("Fetching List of AssetType. EndPoint:{}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> addAssetType(String body, MultipartFile icon, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: Body:{}, LoggedInUser: {}", body, loggedInUser);
        if (body != null) {
            CategoryDTO assetTypeDTO = JSON.parseObject(body, CategoryDTO.class);
            log.info("AssetTypeDTO:{}", assetTypeDTO);
            Integer isExist = assetTypeRepository.checkAssetTypeByNameAndAssetTypeGroupName(assetTypeDTO.getName(), assetTypeDTO.getAssetTypeGroupName());
            if (isExist == 1) {
                log.error("Asset Type Name: {} Already Exists. EndPoint:{}", assetTypeDTO.getName(), httpServletRequest.getRequestURI());
                throw new ClientException("Asset Type Already Exists", 795, httpServletRequest.getRequestURI());
            } else {
                BigInteger creationTimestamp = BigInteger.valueOf(System.currentTimeMillis());
                String assetTypeId = Generators.timeBasedGenerator().generate().toString();
                if (icon != null) {
                    String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                    String url = String.format(resourceUrlConfig.getAssetTypeImageUrl(), assetTypeDTO.getAssetTypeGroupName());
                    String directory = String.format(resourceUrlConfig.getAssetTypeImageDirectory(), assetTypeDTO.getAssetTypeGroupName());
                    String iconLink = awsService.addFileToAWSS3(icon.getBytes(), directory, url, extension, assetTypeId, httpServletRequest);
                    assetTypeDTO.setIconUrl(iconLink);
                }
                assetTypeRepository.addAssetTypeByAssetTypeGroupName(assetTypeId, assetTypeDTO.getName(),
                        assetTypeDTO.getIconUrl(), assetTypeDTO.getDisplayName(), creationTimestamp, assetTypeDTO.getAssetTypeGroupName());
                userActionLogService.addUserActionLog(loggedInUser, "AssetType", "ADD", "AssetType With Name:" + assetTypeDTO.getName() + "Is Added For Asset Type Group: " + assetTypeDTO.getAssetTypeGroupName(), "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Asset Type Added Successfully", 200, true);
                log.info("Asset Type With Name: {} Is Added Successfully For Category Name: {}. Endpoint: {}", assetTypeDTO.getName(), assetTypeDTO.getAssetTypeGroupName(), httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }


    public ResponseEntity<?> updateAssetType(String assetTypeId, String body, MultipartFile icon, String loggedInUser, String iconUrl, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: AssetTypeId: {}, Asset Type DTO:{}, LoggedInUser: {}", assetTypeId, body, loggedInUser);
        if (body != null && assetTypeId != null) {
            CategoryDTO assetTypeDTO = JSON.parseObject(body, CategoryDTO.class);
            log.info("Asset Type DTO:{}", assetTypeDTO);
            Integer isExist = assetTypeRepository.checkAssetTypeByIdAndName(assetTypeId, assetTypeDTO.getName(), assetTypeDTO.getAssetTypeGroupName());
            if (isExist == 1) {
                log.error("Asset Type Name: {} Already Exists. EndPoint:{}", assetTypeDTO.getName(), httpServletRequest.getRequestURI());
                throw new ClientException("Asset Type Already Exists", 795, httpServletRequest.getRequestURI());
            } else {
                String name = assetTypeRepository.getNameById(assetTypeId);
                if (iconUrl != null) {
                    String fileName = awsService.getFileNameByImageUrl(iconUrl, httpServletRequest);
                    String directory = String.format(resourceUrlConfig.getAssetTypeImageDirectory(), assetTypeDTO.getAssetTypeGroupName());
                    awsService.removeFileFromAWSS3(directory, fileName, httpServletRequest);
                    assetTypeDTO.setIconUrl(null);
                }
                if (icon != null) {
                    String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                    String url = String.format(resourceUrlConfig.getAssetTypeImageUrl(), assetTypeDTO.getAssetTypeGroupName());
                    String directory = String.format(resourceUrlConfig.getAssetTypeImageDirectory(), assetTypeDTO.getAssetTypeGroupName());
                    String dbImageURL = awsService.addFileToAWSS3(icon.getBytes(), directory, url, extension, assetTypeId, httpServletRequest);
                    assetTypeDTO.setIconUrl(dbImageURL);
                }
                BigInteger updatedTimestamp = BigInteger.valueOf(System.currentTimeMillis());
                assetTypeRepository.updateAssetTypeByAssetTypeAndAssetTypeGroupName(assetTypeDTO.getName(), assetTypeDTO.getIconUrl(),
                        assetTypeDTO.getDisplayName(), assetTypeDTO.getAssetTypeGroupName(), updatedTimestamp, assetTypeId);
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Asset Type Updated Successfully", 200, true);
                log.info("Asset Type With Name: {} Is Updated Successfully For AssetTypeGroupName: {}. Endpoint: {}", name, assetTypeDTO.getAssetTypeGroupName(), httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "AssetType", "UPDATE",
                        "Asset Type With Name:" + name + "Is Updated For Asset Type Group: " + assetTypeDTO.getAssetTypeGroupName(), "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateAssetTypeIconByAssetTypeGroupAndAssetTypeId(String assetTypeGroupName, String assetTypeId, String iconUrl, MultipartFile icon, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: assetTypeGroupName: {}, AssetTypeId: {}, IconUrl:{}, LoggedInUser: {}", assetTypeGroupName, assetTypeId, iconUrl, loggedInUser);
        if (assetTypeGroupName != null && assetTypeId != null) {
            if (iconUrl != null) {
                String fileName = awsService.getFileNameByImageUrl(iconUrl, httpServletRequest);
                String directory = String.format(resourceUrlConfig.getAssetTypeImageDirectory(), assetTypeGroupName);
                awsService.removeFileFromAWSS3(directory, fileName, httpServletRequest);
                assetTypeRepository.updateAssetTypeIconById(null, assetTypeId, assetTypeGroupName);
            }

            if (icon != null) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                String url = String.format(resourceUrlConfig.getAssetTypeImageUrl(), assetTypeGroupName);
                String directory = String.format(resourceUrlConfig.getAssetTypeImageDirectory(), assetTypeGroupName);
                String iconLink = awsService.addFileToAWSS3(icon.getBytes(), directory, url, extension, assetTypeGroupName, httpServletRequest);
                assetTypeRepository.updateAssetTypeIconById(iconLink, assetTypeId, assetTypeGroupName);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Asset Type Icon Updated Successfully", 200, true);
            log.info("AssetType Icon Updated Successfully For AssetTypeGroup Id: {}, Endpoint: {}", assetTypeGroupName, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteAssetType(List<String> assetTypeIds, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:  AssetTypeIds: {}, LoggedInUser: {}", assetTypeIds, loggedInUser);
        if (assetTypeIds != null) {
            List<CategoryDTO> categoryDTOS = assetTypeRepository.getAssetTypeByIds(assetTypeIds);
            List<String> urls = new ArrayList<>();
            if (categoryDTOS != null && !categoryDTOS.isEmpty()) {
                for (CategoryDTO categoryDTO : categoryDTOS) {
                    if (categoryDTO.getIconUrl() != null) {
                        String fileName = awsService.getFileNameByImageUrl(categoryDTO.getIconUrl(), httpServletRequest);
                        String directory = String.format(resourceUrlConfig.getAssetTypeImageDirectory(), categoryDTO.getAssetTypeGroupName());
                        urls.add(directory + fileName);
                    }
                }
                if (!urls.isEmpty()) {
                    awsService.removeFilesFromAWSS3(urls);
                }
            }
            assetTypeRepository.deleteAssetTypeByAssetTypeIds(assetTypeIds);
            ResponseDTO responseDTO = ScleraUtils.generatePayload("AssetType(s) Deleted Successfully", 200, true);
            log.info("AssetType(s) Deleted Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "AssetType", "DELETE", "Asset Type(s) Deleted Successfully", "success");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }


    public void deleteAssetTypeByAssetTypeGroupName(List<String> AssetTypeGroupNames, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: AssetTypeGroupNames:{}", AssetTypeGroupNames);

        for (String categoryId : AssetTypeGroupNames) {
            List<String> filenames = assetTypeRepository.getImageUrlByAssetTypeGroupName(categoryId);
            if (filenames != null && !filenames.isEmpty()) {
                List<String> urls = new ArrayList<>();
                for (String filename : filenames) {
                    String directory = String.format(resourceUrlConfig.getAssetTypeImageDirectory(), categoryId);
                    urls.add(directory + filename);
                }
                assetTypeRepository.deleteAssetTypeByAssetTypeGroupName(categoryId);
                if (!urls.isEmpty()) {
                    awsService.removeFilesFromAWSS3(urls);
                }
            }
        }
    }

    public ResponseEntity<?> addBulkAssetType(List<CategoryDTO> categoryDTOS, String loggedInUser, HttpServletRequest httpServletRequest) {
        if (categoryDTOS != null) {
            for (CategoryDTO assetTypeDTO : categoryDTOS) {
                assetTypeGroupService.addAssetTypeGroups(assetTypeDTO.getAssetTypeGroupName(), httpServletRequest);
                BigInteger creationTimestamp = BigInteger.valueOf(System.currentTimeMillis());
                String assetTypeId = Generators.timeBasedGenerator().generate().toString();
                assetTypeRepository.addAssetTypeByAssetTypeGroupName(assetTypeId, assetTypeDTO.getName(),
                        assetTypeDTO.getIconUrl(), assetTypeDTO.getDisplayName(), creationTimestamp, assetTypeDTO.getAssetTypeGroupName());
                userActionLogService.addUserActionLog(loggedInUser, "AssetType", "ADD", "AssetType With Name:" + assetTypeDTO.getName() + "Is Added For Asset Type Group: " + assetTypeDTO.getAssetTypeGroupName(), "success");
                log.info("Asset Type With Name: {} Is Added Successfully For Category Name: {}. Endpoint: {}", assetTypeDTO.getName(), assetTypeDTO.getAssetTypeGroupName(), httpServletRequest.getRequestURI());
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Asset Type Added Successfully", 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }


    public void updateAssetTypeGroupName(String generic, List<String> assetTypeGroupNames, String loggedInUser, HttpServletRequest httpServletRequest) {
        assetTypeRepository.updateAssetTypeGroupName(generic, assetTypeGroupNames);
    }

    public String getIconUrlByAssetTypeName(String type) {
        log.info("Fetching Icon Url By Asset Type Name:{}", type);
        return assetTypeRepository.getIconUrlByAssetTypeName(type);
    }

    public String getIconUrlByAssetTypeDisplayName(String type) {
        log.info("Fetching Icon Url By Asset Type Display Name:{}", type);
        return assetTypeRepository.getIconUrlByAssetTypeDisplayName(type);
    }

    public List<CategoryDTO> getUpdatedAssetType(String assetTypeGroupName, String key, String sort, int pageNo, int pageSize, String updatedTimestamp, HttpServletRequest httpServletRequest) {
        log.info("Payload:assetTypeGroupName:{},Key: {}, Sort: {},pageNo:{},pageSize:{}", assetTypeGroupName, key, sort, pageNo, pageSize);
        if (assetTypeGroupName != null) {
            int offset = pageSize * (pageNo - 1);
            if (updatedTimestamp.isEmpty()) {
                return assetTypeRepository.getAssetType(assetTypeGroupName, key, sort, pageSize, offset);
            }
            return assetTypeRepository.getUpdatedAssetType(assetTypeGroupName, key, sort, pageSize, offset, updatedTimestamp);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getAllAssetTypeNames(HttpServletRequest httpServletRequest) {
        List<String> assetTypes = assetTypeRepository.getAllAssetTypeNames();
        ResponseDTO responseDTO = ScleraUtils.generatePayload(assetTypes, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
