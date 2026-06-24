package io.sclera.service;

import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.AssetTypeGroupRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Service
@Slf4j
public class AssetTypeGroupService {
    @Autowired
    private AssetTypeGroupRepository assetTypeGroupRepository;
    @Autowired
    private AwsService awsService;
    @Autowired
    private UserActionLogService userActionLogService;

    @Autowired
    private AssetTypeService assetTypeService;

    public ResponseEntity<?> getAllAssetTypeGroup(String key, String sort, int pageNo, int pageSize, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: Key: {}, Sort: {}, PageNo: {}, PageSize: {}, LoggedInUser: {}", key, sort, pageNo, pageSize, loggedInUser);
        //replace '+' with '-' in search key
        List<String> name = assetTypeGroupRepository.getAllAssetTypeGroup(key, sort);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(name, 200, true);
        log.info("Fetching List of Asset Type Group. EndPoint:{}" + httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> addAssetTypeGroup(List<String> body, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: Body: {}, LoggedInUser: {}", body, loggedInUser);
        for (String name : body) {
            Integer isExist = assetTypeGroupRepository.checkAssetTypeGroupByName(name);
            if (isExist == 1) {
                log.error("AssetTypeGroup Name: {} Already Exists. EndPoint:{}", name, httpServletRequest.getRequestURI());
                throw new ClientException("Asset Type Group Already Exists", 794, httpServletRequest.getRequestURI());
            } else {
                assetTypeGroupRepository.addAssetTypeGroup(name);
            }
            userActionLogService.addUserActionLog(loggedInUser, "AssetTypeGroup", "ADD",
                    "Asset Type Group With Name:" + name + "Is Added", "success");
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Asset Type Group Added Successfully", 200, true);
        log.info("Asset Type Group Added Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }


    public ResponseEntity<?> deleteAssetTypeGroup(List<String> assetTypeGroupNames, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: assetTypeGroupNames:{}, LoggedInUser: {}", assetTypeGroupNames, loggedInUser);
        if (assetTypeGroupNames != null) {
            assetTypeService.updateAssetTypeGroupName("Generic",assetTypeGroupNames, loggedInUser, httpServletRequest);
            assetTypeGroupRepository.deleteAssetTypeGroupByIds(assetTypeGroupNames);
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Asset Type Group(s) Deleted Successfully", 200, true);
            log.info("Asset Type Group(s) Deleted Successfully. EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "AssetTypeGroup", "DELETE", "Asset Type Group(s) Deleted Successfully", "success");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public void addAssetTypeGroups(String assetTypeGroupName, HttpServletRequest httpServletRequest) {
        Integer isExist = assetTypeGroupRepository.checkAssetTypeGroupByName(assetTypeGroupName);
        if (isExist == 0) {
            assetTypeGroupRepository.addAssetTypeGroup(assetTypeGroupName);
        }
    }
}
