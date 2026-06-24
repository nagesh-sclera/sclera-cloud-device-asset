package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsFeatureDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.VdmsFeatureRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class VdmsFeatureService {

    @Autowired
    private VdmsFeatureRepository vdmsFeatureRepository;

    @Autowired
    private UserActionLogService userActionLogService;


    public ResponseEntity<ResponseDTO> getVdmsFeatureDetailsByVdmsId(String vdmsId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, LoggedInUser: {}", vdmsId, loggedInUser);
        if (vdmsId != null) {
            List<VdmsFeatureDTO> vdmsFeatureDTOs = vdmsFeatureRepository.getVdmsFeatureDetailsByVdmsId(vdmsId);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsFeatureDTOs, 200, true);
            log.info("Fetching Vdms Feature Details By VdmsId: {}. Endpoint: {}", vdmsId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> addVdmsFeatureByVdmsAndFeatureIds(String vdmsId, List<String> featureIds, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, FeatureIds: {}, LoggedInUser: {}", vdmsId, featureIds, loggedInUser);
        if (vdmsId != null) {
            for (String featureId : featureIds) {
                String id = Generators.timeBasedGenerator().generate().toString();
                vdmsFeatureRepository.addVdmsFeatureByVdmsAndFeatureId(id, featureId, vdmsId);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Vdms Feature Added Successfully", 200, true);
            userActionLogService.addUserActionLog(loggedInUser, "VdmsFeature", "ADD", "VDMS Feature Added Successfully", "success");
            log.info("Vdms Feature Added Successfully. Endpoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VdmsFeature", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> deleteVdmsFeatureByVdmsId(String vdmsId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, LoggedInUser: {}", vdmsId, loggedInUser);
        if (vdmsId != null) {
            vdmsFeatureRepository.deleteVdmsFeatureByVdmsId(vdmsId);
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Vdms Feature Deleted Successfully", 200, true);
            userActionLogService.addUserActionLog(loggedInUser, "VdmsFeature", "DELETE", "VDMS Feature Deleted Successfully", "success");
            log.info("Vdms Feature Deleted Successfully. Endpoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VdmsFeature", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public void deleteVdmsFeatureByFeatureId(String featureId) {
        log.info("Payload: FeatureId: {}", featureId);
        vdmsFeatureRepository.deleteVdmsFeatureByFeatureId(featureId);
        log.info("Vdms Feature Deleted Successfully");
    }
}
