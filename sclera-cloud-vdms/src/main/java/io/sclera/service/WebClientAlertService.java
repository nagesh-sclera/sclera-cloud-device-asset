package io.sclera.service;

import com.alibaba.fastjson2.JSONObject;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.TouchscreenAlertDTO;
import io.sclera.dto.VdmsDTO;
import io.sclera.util.ScleraWebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class WebClientAlertService {

    @Autowired
    public ScleraWebClient scleraWebClient;

    @Value("${sclera.server.alert.url}")
    private String ALERT_SERVER_URL;

    public void bacnetAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/bacnetAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void lorawanAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/lorawanAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void disruptiveAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/disruptiveAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void myDeviceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/myDeviceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void monnitAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/monnitAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void pelicanAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/pelicanAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void knxAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/knxAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void snmpObjectAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/snmpObjectAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void measuringInstrumentAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/measuringInstrumentAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void daintreeAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/daintreeAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void ecoBeeAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/ecoBeeAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);


    }

    public void modbusAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/modbusAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void deviceService(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/deviceService",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);


    }

    public void locationService(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/locationService",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void deviceAssigneeService(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/deviceAssigneeService",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void locationAssigneeService(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/locationAssigneeService",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void checklistAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/checklistAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void serviceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/serviceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }


    public void inspectionCompletedAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionCompletedAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void serviceCompletedAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/serviceCompletedAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void inspectionPrimaryAssigneeChecklistAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionPrimaryAssigneeChecklistAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void inspectionAssigneeDeviceChecklistAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionAssigneeDeviceChecklistAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void inspectionAssigneeLocationChecklistAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionAssigneeLocationChecklistAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);


    }

    public void inspectionPrimaryAssigneeServiceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionPrimaryAssigneeServiceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void inspectionAssigneeDeviceServiceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionAssigneeDeviceServiceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void inspectionAssigneeLocationServiceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionAssigneeLocationServiceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }


    public void inspectionTaskFailedDeviceServiceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionTaskFailedDeviceServiceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void inspectionTaskFailedLocationServiceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionTaskFailedLocationServiceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void inspectionTaskCompletedDeviceServiceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionTaskCompletedDeviceServiceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void inspectionTaskCompletedLocationServiceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionTaskCompletedLocationServiceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);


    }

    public void procedureAssigneeDeviceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/procedureAssigneeDeviceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void procedureAssigneeLocationAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/procedureAssigneeLocationAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void deviceOnlineAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/deviceOnlineAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void deviceOfflineAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/deviceOfflineAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void deviceStatusService(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/deviceStatusService",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void locationStatusService(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/locationStatusService",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void locationCreateService(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/locationCreateService",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void sensorReportAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/sensorReport",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void inspectionReportAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionReport",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void onboardedAssetReportAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/onboardedAssetReport",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void qrCodeAlert(JSONObject jsonObject) {
        log.info("Payload:JSONObject:{}", jsonObject.toJSONString());
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/sendQrCodeDownloadLink",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void scheduledTaskCompletedDeviceServiceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/scheduledTaskCompletedDeviceServiceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);


    }

    public void scheduledTaskCompletedLocationServiceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/scheduledTaskCompletedLocationServiceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);


    }

    public void scheduledTaskFailedDeviceServiceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/scheduledTaskFailedDeviceServiceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }

    public void scheduledTaskFailedLocationServiceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/scheduledTaskFailedLocationServiceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);


    }

    public void inspectionIncompleteAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionIncompleteAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);


    }

    public void scheduledServiceIncompleteAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/scheduledServiceIncompleteAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);

    }


    public void downloadFileAlert(JSONObject jsonObject) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/downloadFileAlert",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void nonAssociatedInspectionAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/nonAssociatedInspectionAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void nonAssociatedScheduledServiceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/nonAssociatedScheduledServiceAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void sendInventoryReport(JSONObject jsonObject) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/sendInventoryReport",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void inspectionExceptionDeviceChecklistAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionExceptionDeviceChecklistAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void inspectionExceptionLocationChecklistAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/inspectionExceptionLocationChecklistAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void scheduledServicesExceptionDeviceChecklistAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/scheduledServicesExceptionDeviceChecklistAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void scheduledServicesExceptionLocationChecklistAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/scheduledServicesExceptionLocationChecklistAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void vdmsBackupZipAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response=scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL +"/vdmsBackupZipAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void vdmsBackupUploadAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response=scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL +"/vdmsBackupUploadAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void offlineVdmsemailAlert(VdmsDTO jsonObject) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/offlineVdmsEmailAlert",
                null,
                null,
                jsonObject,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void openTicketAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/openTicketAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void closedTicketAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/closedTicketAlert",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void statusFailedEmailAlert(JSONObject jsonObject) {
        scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/api/statusFailedEmail",
                null,
                null,
                jsonObject,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void statusFailedSmsAlert(JSONObject jsonObject) {
        scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/api/statusFailedSms",
                null,
                null,
                jsonObject,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void sendBillingReminderAlert(JSONObject alertData) {
        scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/api/billingReminderAlert",
                null,
                null,
                alertData,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void deviceGuestService(TouchscreenAlertDTO touchscreenAlertDTO) {
        scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/deviceGuestService",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void locationGuestService(TouchscreenAlertDTO touchscreenAlertDTO) {
        scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/locationGuestService",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void deviceStatusGuestService(TouchscreenAlertDTO touchscreenAlertDTO) {
        scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/deviceStatusGuestService",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }

    public void locationStatusGuestService(TouchscreenAlertDTO touchscreenAlertDTO) {
        scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/locationStatusGuestService",
                null,
                null,
                touchscreenAlertDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                10000);
    }
}
