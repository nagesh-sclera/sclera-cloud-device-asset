package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.uuid.Generators;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.*;
import io.sclera.exception.ClientException;
import io.sclera.repository.DigitalTwinTemplateRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DigitalTwinTemplateService {

    @Autowired
    private DigitalTwinTemplateRepository digitalTwinTemplateRepository;

    @Autowired
    private DigitalTwinMeasuringInstrumentService digitalTwinMeasuringInstrumentService;

    @Autowired
    private AwsService awsService;

    @Autowired
    private ResourceUrlConfig resourceUrlConfig;

    @Autowired
    private WebClientService webClientService;

    @Autowired
    private UserActionLogService userActionLogService;


    public ResponseEntity<ResponseDTO> addDigitalTwinTemplateByVdmsAndSubCategoryId(String orgId, String email, String vdmsId,
                                                                                    String body, MultipartFile image, String loggedInUser,
                                                                                    HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: OrgId: {}, Email: {}, VdmsId: {}, Body: {}, LoggedInUser: {}", orgId, email, vdmsId, body, loggedInUser);
        if (orgId != null && email != null && vdmsId != null) {
            DigitalTwinTemplateDTO digitalTwinTemplateDTO = JSON.parseObject(body, DigitalTwinTemplateDTO.class);
            log.info("DigitalTwinTemplateDTO: {}", digitalTwinTemplateDTO);
            Integer isExist = digitalTwinTemplateRepository.checkDigitalTwinTemplateByNameAndVdmsId(digitalTwinTemplateDTO.getName(), vdmsId);
            if (isExist == 1) {
                log.error("Digital Twin Template: {} Already Exists. EndPoint:{}", digitalTwinTemplateDTO.getName(), httpServletRequest.getRequestURI());
                throw new ClientException("Digital Twin Template Already Exists", 785, httpServletRequest.getRequestURI());
            } else {
                String digitalTwinTemplateId = Generators.timeBasedGenerator().generate().toString();
                if (image != null) {
                    String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(image.getOriginalFilename()), httpServletRequest);
                    String imageLink = awsService.addFileToAWSS3(image.getBytes(), resourceUrlConfig.getDigitalTwinTemplateDirectory(), resourceUrlConfig.getDigitalTwinTemplateUrl(), extension, digitalTwinTemplateId, httpServletRequest);
                    digitalTwinTemplateDTO.setImageUrl(imageLink);
                }

                digitalTwinTemplateRepository.addDigitalTwinTemplateByVdmsAndSubCategoryId(digitalTwinTemplateId, digitalTwinTemplateDTO.getName(),
                        digitalTwinTemplateDTO.getDescription(), digitalTwinTemplateDTO.getImageUrl(), digitalTwinTemplateDTO.getSubCategoryId(),
                        vdmsId);
                digitalTwinMeasuringInstrumentService.addDigitalTwinMeasuringInstrumentByDigitalTwinTemplateId(digitalTwinTemplateId,
                        digitalTwinTemplateDTO.getDigitalTwinMeasuringInstrumentList());
                ResponseDTO responseDTO = ScleraUtils.generatePayload(digitalTwinTemplateDTO.getImageUrl(), 200, true);
                log.info("Digital Twin Template Added Successfully For VdmsId: {} And SubCategoryId: {}, Endpoint: {}", vdmsId,
                        digitalTwinTemplateDTO.getSubCategoryId(), httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "DigitalTwinTemplate", "ADD",
                        "A Digital Twin Template With Name:" + digitalTwinTemplateDTO.getName() + " Is Added", "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Error! Invalid client parameters:EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> getDigitalTwinTemplateListByVdmsId(String orgId, String email, String vdmsId, String key,
                                                                          String categoryId, String subCategoryId, int pageNo, int pageSize,
                                                                          String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId: {}, Email: {}, VdmsId: {}, Key: {}, CategoryId: {}, SubCategoryId: {}, PageNo: {}, PageSize: {}, LoggedInUser: {}",
                orgId, email, vdmsId, key, categoryId, subCategoryId, pageNo, pageSize, loggedInUser);
        if (orgId != null && email != null && vdmsId != null) {
            int offset = pageSize * (pageNo - 1);
            List<DigitalTwinTemplateDTO> digitalTwinTemplateDTOS = digitalTwinTemplateRepository.getDigitalTwinTemplateListByVdmsId(vdmsId, key,
                    categoryId, subCategoryId, pageSize, offset);

            ResponseDTO responseDTO = ScleraUtils.generatePayload(digitalTwinTemplateDTOS, 200, true);
            log.info("Fetching List of Digital Twin Templates By VdmsId: {}, Endpoint: {}", vdmsId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Error! Invalid client parameters:EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> getDigitalTwinTemplateDetailsByVdmsAndDigitalTwinTemplateId(String orgId, String email, String vdmsId, String digitalTwinTemplateId,
                                                                                                   String loggedInUser,
                                                                                                   HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId: {}, Email: {}, VdmsId: {}, DigitalTwinTemplateId: {}, LoggedInUser: {}",
                orgId, email, vdmsId, digitalTwinTemplateId, loggedInUser);
        if (orgId != null && email != null && vdmsId != null) {
            DigitalTwinTemplateDTO digitalTwinTemplateDTO = digitalTwinTemplateRepository.getDigitalTwinTemplateDetailsByVdmsId(vdmsId, digitalTwinTemplateId);
            // get unique MI
            List<String> measuringInstrumentsTypes = digitalTwinMeasuringInstrumentService.getDigitalTwinMeasuringInstrumentTypesByDigitalTwinTemplateId(digitalTwinTemplateDTO.getId());
            //get all grouped measuring instruments
            List<String> groupedMeasuringInstrumentIds = digitalTwinMeasuringInstrumentService.getDigitalTwinGroupedMeasuringInstrumentIdsByDigitalTwinTemplateId(digitalTwinTemplateDTO.getId());
            // get Stores Data
            ResponseEntity<ResponseDTO> storesServerResponse = webClientService.getMeasuringInstrumentsByTypes(email, measuringInstrumentsTypes, groupedMeasuringInstrumentIds, loggedInUser, httpServletRequest);
            log.info("Response from Stores:{}", storesServerResponse);

            // Construct Hashmap
            JSONObject hashMap = this.constructMap(Objects.requireNonNull(storesServerResponse.getBody()).getData());

            // get all Measuring Instrument references
            List<DigitalTwinMeasuringInstrumentDTO> digitalTwinMeasuringInstrumentDTOList = digitalTwinMeasuringInstrumentService.getDigitalTwinMeasuringInstrumentsByDigitalTwinTemplateId(digitalTwinTemplateDTO.getId());

            for (DigitalTwinMeasuringInstrumentDTO digitalTwinMeasuringInstrumentDTO : digitalTwinMeasuringInstrumentDTOList) {
                // get Cached Data
                DigitalTwinMeasuringInstrumentDTO mapGroupedMeasuringInstruments = hashMap.getObject(digitalTwinMeasuringInstrumentDTO.getGroupedMeasuringInstrumentId(), DigitalTwinMeasuringInstrumentDTO.class);
                DigitalTwinMeasuringInstrumentDTO mapMeasuringInstruments = hashMap.getObject(digitalTwinMeasuringInstrumentDTO.getType(), DigitalTwinMeasuringInstrumentDTO.class);

                // copy Bean properties
                if (mapGroupedMeasuringInstruments != null) {
                    BeanUtils.copyProperties(mapGroupedMeasuringInstruments, digitalTwinMeasuringInstrumentDTO, "id", "name", "digital_twin_position");
                }
                if (mapMeasuringInstruments != null) {
                    BeanUtils.copyProperties(mapMeasuringInstruments, digitalTwinMeasuringInstrumentDTO, "id", "name", "digital_twin_position");
                }
            }
            digitalTwinTemplateDTO.setDigitalTwinMeasuringInstrumentList(digitalTwinMeasuringInstrumentDTOList);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(digitalTwinTemplateDTO, 200, true);
            log.info("Fetching List of Digital Twin Templates By VdmsId: {}, Endpoint: {}", vdmsId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Error! Invalid client parameters:EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    private JSONObject constructMap(Object storesServerResponse) {
        JSONObject jsonObject = new JSONObject();
        String jsonString = JSON.toJSONString(storesServerResponse);
        List<DigitalTwinMeasuringInstrumentDTO> measuringInstrumentsDTOList = JSON.parseArray(jsonString, DigitalTwinMeasuringInstrumentDTO.class);
        for (DigitalTwinMeasuringInstrumentDTO measuringInstrumentsDTO : measuringInstrumentsDTOList) {
            if (measuringInstrumentsDTO.getGroupedMeasuringInstrumentId() != null) {
                jsonObject.put(measuringInstrumentsDTO.getGroupedMeasuringInstrumentId(), measuringInstrumentsDTO);
            }
            if (measuringInstrumentsDTO.getGroupedMeasuringInstrumentId() == null && measuringInstrumentsDTO.getType() != null) {
                jsonObject.put(measuringInstrumentsDTO.getType(), measuringInstrumentsDTO);
            }
        }
        return jsonObject;
    }

    public ResponseEntity<ResponseDTO> tagDigitalTwinToDevice(String orgId, String email, String vdmsId, MultipartFile image, String imageUrl, String deviceId, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: OrgId: {}, Email: {}, VdmsId: {}, DeviceId: {}, ImageUrl: {}, LoggedInUser: {}", orgId, email, vdmsId, deviceId, imageUrl, loggedInUser);
        if (orgId != null && email != null && vdmsId != null && deviceId != null && loggedInUser != null) {
            if (image != null && !image.isEmpty()) {
                String extension = FilenameUtils.getExtension(image.getOriginalFilename());
                String url = String.format(resourceUrlConfig.getDeviceImageUrl(), vdmsId);
                String directory = String.format(resourceUrlConfig.getDeviceImageDirectory(), vdmsId);
                String data = awsService.addFileToAWSS3(image.getBytes(), directory, url, extension, deviceId, httpServletRequest);
                log.info("Tagged Digital Twin To Device By VdmsId: {} And DeviceId: {}, Endpoint: {}", vdmsId, deviceId, httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "DigitalTwinTemplate", "ADD", "Tagged Digital Twin To Device: " + deviceId + " Successfully", "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }

            if (imageUrl != null) {
                String extension = getFileExtensionByImageUrl(imageUrl);
                String sourceKey = awsService.getFilePathByImageUrl(imageUrl, httpServletRequest);
                String url = String.format(resourceUrlConfig.getDeviceImageUrl(), vdmsId);
                String directory = String.format(resourceUrlConfig.getDeviceImageDirectory(), vdmsId);
                String data = awsService.copyFileToAWSS3(sourceKey, directory, url, extension, deviceId, httpServletRequest);
                log.info("Tagged Digital Twin To Device By VdmsId: {} And DeviceId: {}, Endpoint: {}", vdmsId, deviceId, httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "DigitalTwinTemplate", "ADD", "Tagged Digital Twin To Device: " + deviceId + " Successfully", "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
            log.info("Digital Twin Image Is Not Present To Tag to DeviceId: {}, Endpoint: {}", deviceId, httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "DigitalTwinTemplate", "ADD", "Digital Twin Image Is Not Present To Tag to Device: " + deviceId, "failed");
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public String getFileExtensionByImageUrl(String image_url) {
        return image_url.substring(image_url.lastIndexOf(".") + 1);
    }

    public ResponseEntity<ResponseDTO> updateDigitalTwinTemplateDetailsByVdmsAndDigitalTwinTemplateId(String orgId, String email, String vdmsId,
                                                                                                      String digitalTwinTemplateId, String body, MultipartFile image,
                                                                                                      String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: OrgId: {}, Email: {}, VdmsId: {}, DigitalTwinTemplateId: {}, Body: {}, LoggedInUser: {}", orgId, email, vdmsId, digitalTwinTemplateId, body, loggedInUser);
        if (orgId != null && email != null && vdmsId != null && digitalTwinTemplateId != null && loggedInUser != null) {
            DigitalTwinTemplateDTO digitalTwinTemplateDTO = JSON.parseObject(body, DigitalTwinTemplateDTO.class);

            if (image != null) {
                String imageName = digitalTwinTemplateRepository.getImageNameByDigitalTwinTemplateId(digitalTwinTemplateId);
                if (imageName != null) {
                    awsService.removeFileFromAWSS3(resourceUrlConfig.getDigitalTwinTemplateDirectory(), imageName, httpServletRequest);
                }
                String extension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(image.getOriginalFilename()), httpServletRequest);
                String imageLink = awsService.addFileToAWSS3(image.getBytes(), resourceUrlConfig.getDigitalTwinTemplateDirectory(), resourceUrlConfig.getDigitalTwinTemplateUrl(), extension, digitalTwinTemplateId, httpServletRequest);
                digitalTwinTemplateDTO.setImageUrl(imageLink);
            }

            digitalTwinTemplateRepository.updateDigitalTwinTemplateDetailsByVdmsAndDigitalTwinTemplateId(digitalTwinTemplateDTO.getName(),
                    digitalTwinTemplateDTO.getDescription(), digitalTwinTemplateDTO.getSubCategoryId(), digitalTwinTemplateDTO.getImageUrl(), vdmsId, digitalTwinTemplateId);
            digitalTwinMeasuringInstrumentService.updateDigitalTwinMeasuringInstrumentByDigitalTwinTemplateId(digitalTwinTemplateDTO.getDigitalTwinMeasuringInstrumentList(), digitalTwinTemplateId);

            log.info("Digital Twin Template Updated Successfully For DigitalTwinTemplateId: {}, Endpoint: {}", digitalTwinTemplateId, httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "DigitalTwinTemplate", "UPDATE", "Digital Twin Template Updated Successfully For DigitalTwinTemplateId: " + digitalTwinTemplateId, "success");
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Digital Twin Template Updated Successfully", 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<ResponseDTO> deleteDigitalTwinTemplatesByVdmsAndDigitalTwinTemplateIds(String orgId, String email, String vdmsId, List<String> digitalTwinTemplateIds, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId: {}, Email: {}, VdmsId: {}, Size of DigitalTwinTemplateIds: {}, DigitalTwinTemplateIds: {}, LoggedInUser: {}", orgId, email, vdmsId, digitalTwinTemplateIds.size(), digitalTwinTemplateIds, loggedInUser);
        if (orgId != null && email != null && vdmsId != null && loggedInUser != null) {
            List<String> url = new ArrayList<>();
            List<String> imageNames = digitalTwinTemplateRepository.getImageNameByDigitalTwinTemplateIds(digitalTwinTemplateIds);
            for (String imageName : imageNames) {
                String directory = String.format(resourceUrlConfig.getDigitalTwinTemplateDirectory(), vdmsId);
                url.add(directory + imageName);
            }
            digitalTwinMeasuringInstrumentService.deleteDigitalTwinMeasuringInstrumentsDigitalTwinTemplateIds(digitalTwinTemplateIds);
            digitalTwinTemplateRepository.deleteDigitalTwinTemplatesByVdmsAndDigitalTwinTemplateIds(vdmsId, digitalTwinTemplateIds);
            if (!url.isEmpty()) {
                awsService.removeFilesFromAWSS3(url);
            }
            log.info("Digital Twin Template(s) Deleted Successfully For VdmsId: {}, Endpoint: {}", vdmsId, httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "DigitalTwinTemplate", "DELETE", "Digital Twin Template(s) Deleted Successfully For VdmsId: " + vdmsId, "success");
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Digital Twin Template(s) Deleted Successfully", 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid Client Parameters", 700, httpServletRequest.getRequestURI());
        }
    }

    public Integer checkTaggedSubCategory(List<String> subCategoryIds) {
        log.info("Payload: SubCategoryIds: {}", subCategoryIds);
        return digitalTwinTemplateRepository.checkTaggedSubCategory(subCategoryIds);
    }

    public void deleteDigitalTwinTemplatesByVdmsId(String vdmsId) {
        log.info("VdmsId:{}",vdmsId);
       List<String> templateIds = digitalTwinTemplateRepository.getDigitalTwinTemplateIdsByVdmsId(vdmsId);
       digitalTwinMeasuringInstrumentService.deleteDigitalTwinMeasuringInstrumentsDigitalTwinTemplateIds(templateIds);
    }
}
