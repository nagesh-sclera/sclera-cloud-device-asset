package io.sclera.service;


import com.alibaba.fastjson2.JSON;
import com.fasterxml.uuid.Generators;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.CategoryDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.ProcedureRepository;
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
public class ProcedureService {

    @Autowired
    private ProcedureRepository procedureRepository;
    @Autowired
    private AwsService awsService;
    @Autowired
    private ResourceUrlConfig resourceUrlConfig;
    @Autowired
    private UserActionLogService userActionLogService;

    public ResponseEntity<?> getAllProcedure(String key, String sort, int pageNo, int pageSize, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: Key: {}, Sort: {}, PageNo: {}, PageSize: {}, LoggedInUser: {}", key, sort, pageNo, pageSize, loggedInUser);
        //replace '+' with '-' in search key
        int offset = pageSize * (pageNo - 1);
        List<CategoryDTO> categoryDTOS = procedureRepository.getAllProcedure(key, sort, pageSize, offset);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(categoryDTOS, 200, true);
        log.info("Fetching List of Procedure. EndPoint:{}" + httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> addProcedure(String body, MultipartFile icon, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: Body: {}, LoggedInUser: {}", body, loggedInUser);
        CategoryDTO procedureDTO = JSON.parseObject(body, CategoryDTO.class);
        log.info("Procedure DTO:{}", procedureDTO);
        Integer isExist = procedureRepository.checkProcedureByName(procedureDTO.getName());
        if (isExist == 1) {
            log.error("Procedure Name: {} Already Exists. EndPoint:{}", procedureDTO.getName(), httpServletRequest.getRequestURI());
            throw new ClientException("Procedure Already Exists", 796, httpServletRequest.getRequestURI());
        } else {
            BigInteger creationTimestamp = BigInteger.valueOf(System.currentTimeMillis());
            String id = Generators.timeBasedGenerator().generate().toString();
            if (icon != null) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                String iconLink = awsService.addFileToAWSS3(icon.getBytes(), resourceUrlConfig.getProcedureImageDirectory(),
                        resourceUrlConfig.getProcedureImageUrl(), extension, id, httpServletRequest);
                procedureDTO.setIconUrl(iconLink);
            }
            procedureRepository.addProcedure(id, procedureDTO.getName(), procedureDTO.getIconUrl(),
                    procedureDTO.getDisplayName(), creationTimestamp);
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Procedure Added Successfully", 200, true);
            log.info("Procedure Added Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Procedure", "ADD", "Procedure With Name:" + procedureDTO.getName() + "Is Added", "success");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
    }

    public ResponseEntity<?> updateProcedureById(String procedureId, String body, MultipartFile icon, String loggedInUser, String iconUrl, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: ProcedureId:{}, Body:{}, LoggedInUser: {},iconUrl:{}", procedureId, body, loggedInUser, iconUrl);
        if (procedureId != null) {
            CategoryDTO procedureDTO = JSON.parseObject(body, CategoryDTO.class);
            log.info("procedureDTO:{}", procedureDTO);
            Integer isExist = procedureRepository.checkProcedureByIdAndName(procedureId, procedureDTO.getName());
            if (isExist == 1) {
                log.error("Procedure Name: {} Already Exists. EndPoint:{}", procedureDTO.getName(), httpServletRequest.getRequestURI());
                throw new ClientException("Procedure Already Exists", 796, httpServletRequest.getRequestURI());
            } else {
                String name = procedureRepository.getNameById(procedureId);
                if (iconUrl != null) {
                    String fileName = awsService.getFileNameByImageUrl(iconUrl, httpServletRequest);
                    awsService.removeFileFromAWSS3(resourceUrlConfig.getProcedureImageDirectory(), fileName, httpServletRequest);
                    procedureDTO.setIconUrl(null);
                }
                if (icon != null) {
                    String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                    String iconLink = awsService.addFileToAWSS3(icon.getBytes(), resourceUrlConfig.getProcedureImageDirectory(),
                            resourceUrlConfig.getProcedureImageUrl(), extension, procedureId, httpServletRequest);
                    procedureDTO.setIconUrl(iconLink);
                }

                procedureRepository.updateProcedureById(procedureDTO.getName(), procedureDTO.getIconUrl(),
                        procedureDTO.getDisplayName(), procedureId);
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Procedure Updated Successfully", 200, true);
                log.info("Procedure Updated Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "Procedure", "UPDATE", "Procedure With Name:" + name + "Is updated", "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateProcedureIconById(String procedureId, String iconUrl, MultipartFile icon, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: ProcedureId:{}, IconUrl:{}, LoggedInUser: {}", procedureId, iconUrl, loggedInUser);
        if (procedureId != null) {
            if (iconUrl != null) {
                String fileName = awsService.getFileNameByImageUrl(iconUrl, httpServletRequest);
                awsService.removeFileFromAWSS3(resourceUrlConfig.getProcedureImageDirectory(), fileName, httpServletRequest);
                procedureRepository.updateProcedureIconById(null, procedureId);
            }

            if (icon != null) {
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(icon.getOriginalFilename()), httpServletRequest);
                String iconLink = awsService.addFileToAWSS3(icon.getBytes(), resourceUrlConfig.getProcedureImageDirectory(),
                        resourceUrlConfig.getProcedureImageUrl(), extension, procedureId, httpServletRequest);
                procedureRepository.updateProcedureIconById(iconLink, procedureId);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Procedure Icon Updated Successfully", 200, true);
            log.info("Procedure Icon Updated Successfully For Procedure Id: {}, Endpoint: {}", procedureId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteProcedure(List<String> procedureIds, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: Procedure Ids:{}, LoggedInUser: {}", procedureIds, loggedInUser);
        if (procedureIds != null) {
            List<String> urls = new ArrayList<>();
            List<String> imageUrls = procedureRepository.getImageUrlsByIds(procedureIds);
            if (imageUrls != null && !imageUrls.isEmpty()) {
                for (String imageUrl : imageUrls) {
                    String fileName = awsService.getFileNameByImageUrl(imageUrl, httpServletRequest);
                    String directory = resourceUrlConfig.getProcedureImageDirectory();
                    urls.add(directory + fileName);
                }
                if (!urls.isEmpty()) {
                    awsService.removeFilesFromAWSS3(urls);
                }
            }
            procedureRepository.deleteProcedureByIds(procedureIds);
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Procedure(s) Deleted Successfully", 200, true);
            log.info("Procedure(s) Deleted Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Procedure", "DELETE", "Procedure(s) Deleted Successfully", "success");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> addBulkProcedure(List<CategoryDTO> procedureDTOS, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: procedureDTOS: {}, LoggedInUser: {}", procedureDTOS, loggedInUser);
        for (CategoryDTO procedureDTO : procedureDTOS) {
            Integer isExist = procedureRepository.checkProcedureByName(procedureDTO.getName());
            if (isExist == 1) {
                log.error("Procedure Name: {} Already Exists. EndPoint:{}", procedureDTO.getName(), httpServletRequest.getRequestURI());
                throw new ClientException("Procedure Already Exists", 796, httpServletRequest.getRequestURI());
            } else {
                BigInteger creationTimestamp = BigInteger.valueOf(System.currentTimeMillis());
                String id = Generators.timeBasedGenerator().generate().toString();
                procedureRepository.addProcedure(id, procedureDTO.getName(), procedureDTO.getIconUrl(),
                        procedureDTO.getDisplayName(), creationTimestamp);
                userActionLogService.addUserActionLog(loggedInUser, "Procedure", "ADD", "Procedure With Name:" + procedureDTO.getName() + "Is Added", "success");

            }
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Procedure Added Successfully", 200, true);
        log.info("Procedure Added Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
