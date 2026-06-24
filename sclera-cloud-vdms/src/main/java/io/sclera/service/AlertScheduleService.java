package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.dto.AlertScheduleDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.AlertScheduleRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@Service
@Slf4j
public class AlertScheduleService {

    @Autowired
    private AlertScheduleRepository alertScheduleRepository;
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;




    public void deleteAlertScheduleByEmail(String email, HttpServletRequest httpServletRequest) {
        log.info("Deleting Alert Schedule By Email:{}.EndPoint:{}", email, httpServletRequest.getRequestURI());
        alertScheduleRepository.deleteAlertScheduleByEmail(email);
    }

    public void deleteAlertScheduleByOrgId(String OrgId, HttpServletRequest httpServletRequest) {
        log.info("Deleting Alert Schedule By OrgId:{}.EndPoint:{}", OrgId, httpServletRequest.getRequestURI());
        alertScheduleRepository.deleteAlertScheduleByOrgId(OrgId);
    }


    public ResponseEntity<?> getAllAlertScheduler(HttpServletRequest httpServletRequest) {
        List<AlertScheduleDTO> alertScheduleDTOS =  alertScheduleRepository.getAllAlertScheduler();
        ResponseDTO responseDTO = ScleraUtils.generatePayload(alertScheduleDTOS,200,true);
        return new ResponseEntity<>(responseDTO,HttpStatus.OK);
    }
}
