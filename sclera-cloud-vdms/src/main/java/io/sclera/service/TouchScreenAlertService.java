package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.amazonaws.HttpMethod;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.*;
import io.sclera.exception.ClientException;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class TouchScreenAlertService {

    @Autowired
    private WebClientAlertService webClientAlertService;
    @Autowired
    private WebClientService webClientService;

    @Autowired
    private AlertScheduleService alertScheduleService;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;

    @Autowired
    private ResourceUrlConfig resourceUrlConfig;
    @Autowired
    private AwsService awsService;

    @Autowired
    private SensorCategoryService sensorCategoryService;
    @Autowired
    private AssetTypeService assetTypeService;

    public ResponseEntity<?> alertUser(String vdmsId, TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) throws IOException {
        if (touchscreenAlertDTO != null) {
            log.info("Touchscreen Alert Triggered");
            log.info("Payload: VdmsId: {}, TouchscreenAlertDTO: {}", vdmsId, touchscreenAlertDTO);
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                CloudAlertDTO cloudAlertDTO = new CloudAlertDTO();

                if (touchscreenAlertDTO.getDevice() != null) {
                    String docker_name = capitalizeWords(touchscreenAlertDTO.getDevice().getDocker_name(), httpServletRequest);
                    if (touchscreenAlertDTO.getDevice().getName() == null) {
                        touchscreenAlertDTO.getDevice().setName("Generic");
                    }
                    touchscreenAlertDTO.getDevice().setDocker_name(docker_name);
                }

                if (touchscreenAlertDTO.getChecklist() != null) {
                    String category_image_url = "https://app.sclera.com/alert/sclera-resource/static/images/" + touchscreenAlertDTO.getChecklist().getTask_type() + "/" + touchscreenAlertDTO.getChecklist().getCategory() + ".png";
                    cloudAlertDTO.setCategory_image_url(category_image_url);

                    String category = capitalizeWords(touchscreenAlertDTO.getChecklist().getCategory(), httpServletRequest);
                    touchscreenAlertDTO.getChecklist().setCategory(category);

                    String status = capitalizeWords(touchscreenAlertDTO.getChecklist().getStatus(), httpServletRequest);
                    touchscreenAlertDTO.getChecklist().setStatus(status);
                }

                if (touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0) {
                    if (touchscreenAlertDTO.getAlert_type() != 7) {
                        this.checkAlertSchedule(touchscreenAlertDTO, httpServletRequest);
                    }

                    switch (touchscreenAlertDTO.getAlert_type()) {
                        case 1: //sensor alert
                            this.sensorAlert(touchscreenAlertDTO, httpServletRequest);
                            break;
                        case 2: //services alert
                            this.servicesAlert(touchscreenAlertDTO, cloudAlertDTO, httpServletRequest);
                            break;
                        case 3: //inspection alert
                            this.inspectionAlert(touchscreenAlertDTO, cloudAlertDTO, httpServletRequest);
                            break;
                        case 4: //procedure alert
                            this.procedureAlert(touchscreenAlertDTO, cloudAlertDTO, httpServletRequest);
                            break;
                        case 5: //device alert
                            this.deviceAlert(touchscreenAlertDTO, httpServletRequest);
                            break;
                        case 6: //report alert->sensor_report, inspection_report, and onboarding report
                            this.reportAlert(touchscreenAlertDTO, cloudAlertDTO, httpServletRequest);
                            break;
                        case 7: // vdms backup failed alert
                            this.vdmsBackupAlert(touchscreenAlertDTO, httpServletRequest);
                            break;
                        case 8: // ITAM ticket alert
                            this.ticketAlert(touchscreenAlertDTO,httpServletRequest);
                            break;
                        case 9: // Invalid QrCode Scan alert
                            this.invalidQrCodeScanAlert(touchscreenAlertDTO);
                            break;
                        case 10: // Guest Users (Teams) Alert
                            this.teamsGuestUserAlert(touchscreenAlertDTO, httpServletRequest);
                            break;
                        default:
                            log.error("Invalid alert type. Endpoint: {}", httpServletRequest.getRequestURI());
                            throw new ClientException("Invalid alert type", 756, httpServletRequest.getRequestURI());
                    }

                    ResponseDTO responseDTO = ScleraUtils.generatePayload("Alert sent successfully", 200, true);
                    log.info("Alert sent successfully. Endpoint: {}", httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);

                } else {
                    ResponseDTO responseDTO = ScleraUtils.generatePayload("Email and SMS not enabled", 200, true);
                    log.error("Email or SMS not enabled. Endpoint: {}", httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                }
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.info("Invalid client params. Endpoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    private void teamsGuestUserAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        log.info("Teams Guest User Alert Triggered");
        log.info("Payload:TouchscreenAlertDTO: {}", touchscreenAlertDTO);
        String categoryImageUrl = "https://app.sclera.com/alert/sclera-resource/static/alerts/images/jll-logo.png";
        CloudAlertDTO cloudAlertDTO = new CloudAlertDTO();
        if (touchscreenAlertDTO.getDevice() != null) {
            String image_url = touchscreenAlertDTO.getDevice().getImage_url() != null ? touchscreenAlertDTO.getDevice().getImage_url() : "https://app.sclera.com/alert/sclera-resource/static/images/common/generic.png";
            String location = touchscreenAlertDTO.getDevice().getLocation() != null ? touchscreenAlertDTO.getDevice().getLocation() : "-";
            String floor = touchscreenAlertDTO.getDevice().getFloor() != null ? touchscreenAlertDTO.getDevice().getFloor() : "-";
            String building = touchscreenAlertDTO.getDevice().getBuilding() != null ? touchscreenAlertDTO.getDevice().getBuilding() : "-";

            cloudAlertDTO.setImage_url(image_url);
            touchscreenAlertDTO.getDevice().setLocation(location);
            touchscreenAlertDTO.getDevice().setFloor(floor);
            touchscreenAlertDTO.getDevice().setBuilding(building);
        }
        cloudAlertDTO.setCategory_image_url(categoryImageUrl);

        String timestamp = getCurrentTimeForServices(touchscreenAlertDTO.getChecklist().getCreated_timestamp(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
        cloudAlertDTO.setTimestamp(timestamp);

        if (touchscreenAlertDTO.getChecklist().getUpdated_timestamp() != null) {
            String updatedTime = getCurrentTimeForServices(touchscreenAlertDTO.getChecklist().getUpdated_timestamp(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
            cloudAlertDTO.setUpdated_timestamp(updatedTime);
        }

        if (touchscreenAlertDTO.getChecklist().getDue_date() != null) {
            String due_date = getCurrentTimeForServices(touchscreenAlertDTO.getChecklist().getDue_date(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
            cloudAlertDTO.setDue_date(due_date);
        }
        touchscreenAlertDTO.setCloud_alert(cloudAlertDTO);

        switch (touchscreenAlertDTO.getChecklist().getTemplate_type()) {
            case "device_guest_service":
                webClientAlertService.deviceGuestService(touchscreenAlertDTO);
                log.info("Device Guest Service Alert Sent. Endpoint:{}", httpServletRequest.getRequestURI());
                break;
            case "location_guest_service":
                webClientAlertService.locationGuestService(touchscreenAlertDTO);
                log.info("Location Guest Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "device_status_guest_service":
                webClientAlertService.deviceStatusGuestService(touchscreenAlertDTO);
                log.info("Device Status Guest Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "location_status_guest_service":
                webClientAlertService.locationStatusGuestService(touchscreenAlertDTO);
                log.info("Location Status Guest Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            default:
                log.error("Invalid alert type. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid alert type", 756, httpServletRequest.getRequestURI());

        }
    }

    private void invalidQrCodeScanAlert(TouchscreenAlertDTO touchscreenAlertDTO) {
    }

    private void ticketAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        log.info("ITAM {} Alert Triggered", touchscreenAlertDTO.getTicket().getTemplate_type());
        log.info("ITAM Ticket Alert Payload:{}",touchscreenAlertDTO);
        JSONObject dateTimeData = getCurrentDateAndTimeByTimestampAndTimezone(touchscreenAlertDTO.getTimestamp(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
        CloudAlertDTO cloudAlertDTO = new CloudAlertDTO();
        cloudAlertDTO.setTicket_date(dateTimeData.getString("date"));
        cloudAlertDTO.setTicket_time(dateTimeData.getString("time"));
        touchscreenAlertDTO.setCloud_alert(cloudAlertDTO);
        switch (touchscreenAlertDTO.getTicket().getTemplate_type()) {
            case "open_ticket":
                webClientAlertService.openTicketAlert(touchscreenAlertDTO, httpServletRequest);
                break;
            case "closed_ticket":
                webClientAlertService.closedTicketAlert(touchscreenAlertDTO, httpServletRequest);
                break;
            default:
                log.error("Invalid alert type. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid alert type", 756, httpServletRequest.getRequestURI());
        }
    }

    private void vdmsBackupAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        log.info("Vdms Backup Alert Triggered");
        log.info("Payload:{}", touchscreenAlertDTO);
        String timestamp = getCurrentTimeForServices(touchscreenAlertDTO.getTimestamp(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
        CloudAlertDTO cloudAlertDTO = new CloudAlertDTO();
        cloudAlertDTO.setTimestamp(timestamp);
        if (touchscreenAlertDTO.getVdms_info().getTemplate_type().equals("vdms_backup_zip_alert")) {
            cloudAlertDTO.setCategory_image_url("https://app.sclera.com/alert/sclera-resource/static/alerts/images/Backup-zip-fail.png");
        } else {
            cloudAlertDTO.setCategory_image_url("https://app.sclera.com/alert/sclera-resource/static/alerts/images/Backup-upload-fail.png");
        }

        touchscreenAlertDTO.setCloud_alert(cloudAlertDTO);
        switch (touchscreenAlertDTO.getVdms_info().getTemplate_type()) {
            case "vdms_backup_zip_alert":
                webClientAlertService.vdmsBackupZipAlert(touchscreenAlertDTO, httpServletRequest);
                break;
            case "vdms_backup_upload_alert":
                webClientAlertService.vdmsBackupUploadAlert(touchscreenAlertDTO, httpServletRequest);
                break;
            default:
                log.error("Invalid alert type. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid alert type", 756, httpServletRequest.getRequestURI());

        }
    }


    private void sensorAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        log.info("Sensor Alert Triggered");
        log.info("Payload:Touchscreen_Alert_DTO{}", touchscreenAlertDTO);
        String vdms_address = getVdmsAddress(touchscreenAlertDTO.getVdms(), httpServletRequest);
        String timestamp = getCurrentTimeByTimestampAndTimezone(touchscreenAlertDTO.getTimestamp(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
        String unit = touchscreenAlertDTO.getSensor().getUnit() != null ? touchscreenAlertDTO.getSensor().getUnit() : "";
        String value = touchscreenAlertDTO.getSensor().getValue() + " " + unit;
        String location = touchscreenAlertDTO.getDevice().getLocation() != null ? touchscreenAlertDTO.getDevice().getLocation() : "-";
        String floor = touchscreenAlertDTO.getDevice().getFloor() != null ? touchscreenAlertDTO.getDevice().getFloor() : "-";
        String building = touchscreenAlertDTO.getDevice().getBuilding() != null ? touchscreenAlertDTO.getDevice().getBuilding() : "-";

        CloudAlertDTO cloudAlertDTO = new CloudAlertDTO();
        cloudAlertDTO.setVdms_address(vdms_address);
        cloudAlertDTO.setTimestamp(timestamp);
        cloudAlertDTO.setValue(value);

        if (touchscreenAlertDTO.getSensor().getProtocol().equals("measuring_instrument")) {
            String priority = capitalizeWords(touchscreenAlertDTO.getSensor().getPriority(), httpServletRequest);
            touchscreenAlertDTO.getSensor().setPriority(priority);
            String categoryImageUrl = sensorCategoryService.getIconUrlByCategoryName(touchscreenAlertDTO.getSensor().getCategory()) != null ? sensorCategoryService.getIconUrlByCategoryName(touchscreenAlertDTO.getSensor().getCategory()) : sensorCategoryService.getIconUrlByCategoryName("generic");
            cloudAlertDTO.setCategory_image_url(categoryImageUrl);
            String category = capitalizeWords(touchscreenAlertDTO.getSensor().getCategory(), httpServletRequest);
            touchscreenAlertDTO.getSensor().setCategory(category);
            String assetTypeUrl = assetTypeService.getIconUrlByAssetTypeName(touchscreenAlertDTO.getDevice().getType());

            if (assetTypeUrl == null) {
                assetTypeUrl = assetTypeService.getIconUrlByAssetTypeDisplayName(touchscreenAlertDTO.getDevice().getType());
            }

            if (assetTypeUrl == null) {
                assetTypeUrl = assetTypeService.getIconUrlByAssetTypeName("generic");
            }

            cloudAlertDTO.setImage_url(assetTypeUrl);


        } else {
            String image_url = touchscreenAlertDTO.getDevice().getImage_url() != null ? touchscreenAlertDTO.getDevice().getImage_url() : "https://app.sclera.com/alert/sclera-resource/static/images/common/generic.png";
            cloudAlertDTO.setImage_url(image_url);
            cloudAlertDTO.setCategory_image_url("https://app.sclera.com/alert/sclera-resource/static/images/sensors/images/" + touchscreenAlertDTO.getSensor().getProtocol() + "/" + touchscreenAlertDTO.getSensor().getCategory() + ".png");
        }

        touchscreenAlertDTO.setCloud_alert(cloudAlertDTO);
        touchscreenAlertDTO.getDevice().setLocation(location);
        touchscreenAlertDTO.getDevice().setFloor(floor);
        touchscreenAlertDTO.getDevice().setBuilding(building);

        switch (touchscreenAlertDTO.getSensor().getProtocol()) {
            case "bacnet": //bacnet alert
                webClientAlertService.bacnetAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("BackNet Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "lorawan": //lorawan alert
                webClientAlertService.lorawanAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Lorawan Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "disruptive": //disruptive alert
                webClientAlertService.disruptiveAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Disruptive Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "my_devices": //my_devices alert
                webClientAlertService.myDeviceAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("MyDevice Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "monnit": //monnit alert
                webClientAlertService.monnitAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Monnit Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "pelican": //pelican alert
                webClientAlertService.pelicanAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Pelican Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "knx": //knx alert
                webClientAlertService.knxAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("KNX Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "snmp_object": //snmp_object alert
                webClientAlertService.snmpObjectAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("SNMP Object Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "measuring_instrument": //measuring_instrument alert
                webClientAlertService.measuringInstrumentAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Measuring Instrument Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "daintree": //daintree alert
                webClientAlertService.daintreeAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Daintree Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "ecobee": //ecobee alert
                webClientAlertService.ecoBeeAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("EcoBee Sensor Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "modbus": //modbus alert
                webClientAlertService.modbusAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Modbus Sensor Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            default:
                log.error("Invalid alert type. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid alert type", 756, httpServletRequest.getRequestURI());
        }
    }

    private void servicesAlert(TouchscreenAlertDTO touchscreenAlertDTO, CloudAlertDTO cloudAlertDTO, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Service Alert Triggered");
        log.info("Payload:TouchscreenAlertDTO: {}, CloudAlertDTO: {}", touchscreenAlertDTO, cloudAlertDTO);
        String timestamp = getCurrentTimeForServices(touchscreenAlertDTO.getChecklist().getCreated_timestamp(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
        cloudAlertDTO.setTimestamp(timestamp);

        if (touchscreenAlertDTO.getDevice() != null) {
            String image_url = touchscreenAlertDTO.getDevice().getImage_url() != null ? touchscreenAlertDTO.getDevice().getImage_url() : "https://app.sclera.com/alert/sclera-resource/static/images/common/generic.png";
            String location = touchscreenAlertDTO.getDevice().getLocation() != null ? touchscreenAlertDTO.getDevice().getLocation() : "-";
            String floor = touchscreenAlertDTO.getDevice().getFloor() != null ? touchscreenAlertDTO.getDevice().getFloor() : "-";
            String building = touchscreenAlertDTO.getDevice().getBuilding() != null ? touchscreenAlertDTO.getDevice().getBuilding() : "-";

            cloudAlertDTO.setImage_url(image_url);
            touchscreenAlertDTO.getDevice().setLocation(location);
            touchscreenAlertDTO.getDevice().setFloor(floor);
            touchscreenAlertDTO.getDevice().setBuilding(building);
        }
        if (touchscreenAlertDTO.getChecklist().getDue_date() != null) {
            String due_date = getCurrentTimeForServices(touchscreenAlertDTO.getChecklist().getDue_date(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
            cloudAlertDTO.setDue_date(due_date);
        }

        if (touchscreenAlertDTO.getChecklist().getTemplate_type().equals("device_status_service") ||
                touchscreenAlertDTO.getChecklist().getTemplate_type().equals("location_status_service")) {
            String completed_date = getCurrentTimeForInspection(touchscreenAlertDTO.getChecklist().getCompleted_timestamp(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
            cloudAlertDTO.setCompleted_date(completed_date);
        }

        touchscreenAlertDTO.setCloud_alert(cloudAlertDTO);

        if (touchscreenAlertDTO.getChecklist().getTemplate_type().equals("location_service") &&
                touchscreenAlertDTO.getChecklist().getFile() != null) {
            String preSignUrl = this.generateS3PreSignUrl(touchscreenAlertDTO, httpServletRequest);
            touchscreenAlertDTO.getChecklist().setPreSignUrl(preSignUrl);
        }

        switch (touchscreenAlertDTO.getChecklist().getTemplate_type()) {
            case "device_service": //device service alert
                webClientAlertService.deviceService(touchscreenAlertDTO, httpServletRequest);
                log.info("Device Service Alert Sent. Endpoint:{}", httpServletRequest.getRequestURI());
                break;
            case "location_service": //location service alert
                webClientAlertService.locationService(touchscreenAlertDTO, httpServletRequest);
                log.info("Location Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "device_assignee_service": //device assignee service alert
                webClientAlertService.deviceAssigneeService(touchscreenAlertDTO, httpServletRequest);
                log.info("Device Assignee Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "location_assignee_service": //location assignee service alert
                webClientAlertService.locationAssigneeService(touchscreenAlertDTO, httpServletRequest);
                log.info("Location Assignee Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "device_status_service": //device status service alert
                webClientAlertService.deviceStatusService(touchscreenAlertDTO, httpServletRequest);
                log.info("Device Status Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "location_status_service": //location status service alert
                webClientAlertService.locationStatusService(touchscreenAlertDTO, httpServletRequest);
                log.info("Location Status Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "location_create_service": //location create service alert
                webClientAlertService.locationCreateService(touchscreenAlertDTO, httpServletRequest);
                log.info("Location Create Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            default:
                log.error("Invalid alert type. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid alert type", 756, httpServletRequest.getRequestURI());
        }
    }

    private void inspectionAlert(TouchscreenAlertDTO touchscreenAlertDTO, CloudAlertDTO cloudAlertDTO, HttpServletRequest httpServletRequest) {
        log.info("Inspection Alert Triggered");
        log.info("Payload:TouchscreenAlertDTO: {}, CloudAlertDTO: {}", touchscreenAlertDTO, cloudAlertDTO);
        String start_date = getCurrentTimeForInspection(touchscreenAlertDTO.getInspection().getStart_date(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
        String due_date = getCurrentTimeForInspection(touchscreenAlertDTO.getInspection().getDue_date(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);

        if (touchscreenAlertDTO.getInspection().getRecordchecklist() != null) {
            RecordChecklistAlertDTO recordChecklistAlertDTO = touchscreenAlertDTO.getInspection().getRecordchecklist().get(0);
            String category_image_url = "https://app.sclera.com/alert/sclera-resource/static/images/" + recordChecklistAlertDTO.getTask_type() + "/" + recordChecklistAlertDTO.getCategory() + ".png";
            cloudAlertDTO.setCategory_image_url(category_image_url);
            String remarks_date = getCurrentTimeForInspection(recordChecklistAlertDTO.getRemarks_timestamp(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
            cloudAlertDTO.setRemarks_date(remarks_date);

            if (touchscreenAlertDTO.getInspection().getTemplate_type().equals("inspection_failed_device_checklist") ||
                    touchscreenAlertDTO.getInspection().getTemplate_type().equals("inspection_failed_location_checklist") ||
                    touchscreenAlertDTO.getInspection().getTemplate_type().equals("scheduled_services_failed_device_checklist") ||
                    touchscreenAlertDTO.getInspection().getTemplate_type().equals("scheduled_services_failed_location_checklist")) {
                String completed_date = getCurrentTimeForInspection(recordChecklistAlertDTO.getCompleted_timestamp(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
                cloudAlertDTO.setCompleted_date(completed_date);
            }
        }

        cloudAlertDTO.setStart_date(start_date);
        cloudAlertDTO.setDue_date(due_date);

        if (touchscreenAlertDTO.getInspection().getTemplate_type().equals("inspection_completed_checklist") ||
                touchscreenAlertDTO.getInspection().getTemplate_type().equals("inspection_completed_service")) {
            String completed_date = getCurrentTimeForInspection(touchscreenAlertDTO.getInspection().getCompleted_date(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
            cloudAlertDTO.setCompleted_date(completed_date);
        }

        touchscreenAlertDTO.setCloud_alert(cloudAlertDTO);

        switch (touchscreenAlertDTO.getInspection().getTemplate_type()) {
            case "inspection_checklist": //inspection created alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.checklistAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Checklist Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_service": //scheduled service created alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.serviceAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_completed_checklist": //inspection completed alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionCompletedAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Complete Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_completed_service": //scheduled service completed alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.serviceCompletedAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Service Completed Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_primary_assignee_checklist": //inspection primary assignee alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionPrimaryAssigneeChecklistAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Primary Assignee Checklist Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_assignee_device_checklist": //inspection assignee device alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionAssigneeDeviceChecklistAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Assignee Device Checklist Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_assignee_location_checklist": //inspection assignee location alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionAssigneeLocationChecklistAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Assignee Location Checklist Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_primary_assignee_service": //scheduled service primary assignee alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionPrimaryAssigneeServiceAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Primary Assignee Service Checklist Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_assignee_device_service": //scheduled service assignee device alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionAssigneeDeviceServiceAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Assignee Device Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_assignee_location_service": //scheduled service assignee location alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionAssigneeLocationServiceAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Assignee Location Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_failed_device_checklist": //inspection failed device alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionTaskFailedDeviceServiceAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Task Failed Device Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_failed_location_checklist": //inspection failed location alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionTaskFailedLocationServiceAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Task Failed Location Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_completed_device_checklist": //inspection completed device alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionTaskCompletedDeviceServiceAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Task Completed Device Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_completed_location_checklist": //inspection completed location alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionTaskCompletedLocationServiceAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Task Completed Location Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "scheduled_services_completed_device_checklist": //scheduled service completed device alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.scheduledTaskCompletedDeviceServiceAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Task Completed Location Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "scheduled_services_completed_location_checklist": //scheduled service completed location alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.scheduledTaskCompletedLocationServiceAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Task Completed Location Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "scheduled_services_failed_device_checklist": //scheduled service failed device alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.scheduledTaskFailedDeviceServiceAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Task Completed Location Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "scheduled_services_failed_location_checklist": //scheduled service failed location alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.scheduledTaskFailedLocationServiceAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Task Completed Location Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "incomplete_inspection_checklist": //inspection incomplete alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionIncompleteAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Incomplete Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "incomplete_scheduled_services": //scheduled service incomplete alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.scheduledServiceIncompleteAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Scheduled Service Incomplete Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_assignee_non_associated_checklist": //non-associated inspection alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.nonAssociatedInspectionAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Non Associated Inspection Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_assignee_non_associated_service": //non-associated scheduled service alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.nonAssociatedScheduledServiceAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Non Associated Scheduled Service Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_exception_device_checklist": //inspection exception device checklist alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionExceptionDeviceChecklistAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Exception Device Checklist Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "inspection_exception_location_checklist": //inspection exception location checklist alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.inspectionExceptionLocationChecklistAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Inspection Exception Location Checklist Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "scheduled_services_exception_device_checklist": //scheduled services exception device checklist alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.scheduledServicesExceptionDeviceChecklistAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Scheduled Services Exception Device Checklist Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            case "scheduled_services_exception_location_checklist": //scheduled services exception location checklist alert
                if ((touchscreenAlertDTO.getEmail_alert() != 0 || touchscreenAlertDTO.getSms_alert() != 0)) {
                    webClientAlertService.scheduledServicesExceptionLocationChecklistAlert(touchscreenAlertDTO, httpServletRequest);
                    log.info("Scheduled Services Exception Location Checklist Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                }
                break;
            default:
                log.error("Invalid alert type. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid alert type", 756, httpServletRequest.getRequestURI());
        }
    }

    private void procedureAlert(TouchscreenAlertDTO touchscreenAlertDTO, CloudAlertDTO cloudAlertDTO, HttpServletRequest httpServletRequest) {
        log.info("Procedure Alert Triggered");
        log.info("Payload:TouchscreenAlertDTO: {}, CloudAlertDTO: {}", touchscreenAlertDTO, cloudAlertDTO);
        String due_date = getCurrentTimeForInspection(touchscreenAlertDTO.getChecklist().getDue_date(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
        cloudAlertDTO.setDue_date(due_date);

        if (touchscreenAlertDTO.getDevice() != null) {
            String location = (touchscreenAlertDTO.getDevice().getLocation() != null ? touchscreenAlertDTO.getDevice().getLocation() + ", " + touchscreenAlertDTO.getDevice().getFloor()
                    + ", " + touchscreenAlertDTO.getDevice().getBuilding() : "-");
            touchscreenAlertDTO.getDevice().setLocation(location);
        }

        if (touchscreenAlertDTO.getLocation() != null) {
            String location = (touchscreenAlertDTO.getLocation().getName() != null ? touchscreenAlertDTO.getLocation().getName() + ", " + touchscreenAlertDTO.getLocation().getFloor_name()
                    + ", " + touchscreenAlertDTO.getLocation().getBuilding_name() : "-");
            touchscreenAlertDTO.getLocation().setName(location);
        }

        touchscreenAlertDTO.setCloud_alert(cloudAlertDTO);

        switch (touchscreenAlertDTO.getChecklist().getTemplate_type()) {
            case "procedure_assignee_device": //procedure assignee device alert
                webClientAlertService.procedureAssigneeDeviceAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Procedure Assignee Device Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "procedure_assignee_location": //procedure assignee location alert
                webClientAlertService.procedureAssigneeLocationAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Procedure Assignee Location Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            default:
                log.error("Invalid alert type. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid alert type", 756, httpServletRequest.getRequestURI());
        }
    }

    private void deviceAlert(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        log.info("Device Alert Triggered");
        log.info("Payload:TouchscreenAlertDTO: {}", touchscreenAlertDTO);
        String timestamp = getCurrentTimeForServices(touchscreenAlertDTO.getDevice().getAlert_time(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);

        CloudAlertDTO cloudAlertDTO = new CloudAlertDTO();
        cloudAlertDTO.setTimestamp(timestamp);

        if (touchscreenAlertDTO.getDevice() != null) {
            String image_url = touchscreenAlertDTO.getDevice().getImage_url() != null ? touchscreenAlertDTO.getDevice().getImage_url() : "https://app.sclera.com/alert/sclera-resource/static/images/common/generic.png";
            String location = touchscreenAlertDTO.getDevice().getLocation() != null ? touchscreenAlertDTO.getDevice().getLocation() : "-";
            String floor = touchscreenAlertDTO.getDevice().getFloor() != null ? touchscreenAlertDTO.getDevice().getFloor() : "-";
            String building = touchscreenAlertDTO.getDevice().getBuilding() != null ? touchscreenAlertDTO.getDevice().getBuilding() : "-";

            cloudAlertDTO.setImage_url(image_url);

            touchscreenAlertDTO.getDevice().setLocation(location);
            touchscreenAlertDTO.getDevice().setFloor(floor);
            touchscreenAlertDTO.getDevice().setBuilding(building);
        }

        touchscreenAlertDTO.setCloud_alert(cloudAlertDTO);

        switch (touchscreenAlertDTO.getDevice().getTemplate_type()) {
            case "device_online": //device online alert
                webClientAlertService.deviceOnlineAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Device Online Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "device_offline": //device offline alert
                webClientAlertService.deviceOfflineAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Device Offline Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            default:
                log.error("Invalid alert type. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid alert type", 756, httpServletRequest.getRequestURI());
        }
    }

    private void reportAlert(TouchscreenAlertDTO touchscreenAlertDTO, CloudAlertDTO cloudAlertDTO, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Report Alert Triggered");
        log.info("Payload:TouchscreenAlertDTO: {}", touchscreenAlertDTO);
        String start_date = getCurrentTimeForInspection(touchscreenAlertDTO.getSensor_report_details().getFrom(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
        String due_date = getCurrentTimeForInspection(touchscreenAlertDTO.getSensor_report_details().getTo(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);

        if (touchscreenAlertDTO.getSensor_report_details().getCategory() != null) {
            cloudAlertDTO.setCategory_image_url("https://app.sclera.com/alert/sclera-resource/static/images/report/" + touchscreenAlertDTO.getSensor_report_details().getCategory() + ".png");

            String category = capitalizeWords(touchscreenAlertDTO.getSensor_report_details().getCategory(), httpServletRequest);
            touchscreenAlertDTO.getSensor_report_details().setCategory(category);
        }

        cloudAlertDTO.setStart_date(start_date);
        cloudAlertDTO.setDue_date(due_date);
        touchscreenAlertDTO.setCloud_alert(cloudAlertDTO);
        String preSignUrl = this.generateS3PreSignUrl(touchscreenAlertDTO, httpServletRequest);
        touchscreenAlertDTO.getSensor_report_details().setPreSignUrl(preSignUrl);

        switch (touchscreenAlertDTO.getSensor_report_details().getTemplate_type()) {
            case "sensor_report": //sensor report alert
                webClientAlertService.sensorReportAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Sensor Report Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "inspection_report","inspection_report_general": //inspection report alert
                webClientAlertService.inspectionReportAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Inspection Report Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            case "onboarded_asset_report": //onboarded asset report alert
                webClientAlertService.onboardedAssetReportAlert(touchscreenAlertDTO, httpServletRequest);
                log.info("Onboarded Asset Report Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
                break;
            default:
                log.error("Invalid alert type. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid alert type", 756, httpServletRequest.getRequestURI());
        }
    }

    public String generateS3PreSignUrl(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) throws IOException {
        if (touchscreenAlertDTO != null) {
            String encodedString = null;
            byte[] decodedBytes = null;
            String filUrl = null;
            String fileDirectory = null;
            String key = null;
            if (touchscreenAlertDTO.getSensor_report_details() != null &&
                    touchscreenAlertDTO.getSensor_report_details().getTemplate_type().equals("sensor_report")) {
                encodedString = touchscreenAlertDTO.getSensor_report_details().getFile();
                decodedBytes = Base64.getDecoder().decode(encodedString);
                filUrl = resourceUrlConfig.getSensorReportFileUrl();
                fileDirectory = resourceUrlConfig.getSensorReportDirectory();
                key = awsService.addExportFileToAWSS3(decodedBytes, filUrl, fileDirectory, null,
                        touchscreenAlertDTO.getSensor_report_details().getReport_template_name() + "_" + System.nanoTime() + ".pdf",
                        httpServletRequest);
            } else if (touchscreenAlertDTO.getSensor_report_details() != null &&
                    touchscreenAlertDTO.getSensor_report_details().getTemplate_type().equals("inspection_report")) {
                encodedString = touchscreenAlertDTO.getSensor_report_details().getFile();
                decodedBytes = Base64.getDecoder().decode(encodedString);
                filUrl = resourceUrlConfig.getInspectionReportFileUrl();
                fileDirectory = resourceUrlConfig.getInspectionReportFileDirectory();
                key = awsService.addExportFileToAWSS3(decodedBytes, filUrl, fileDirectory, null,
                        touchscreenAlertDTO.getSensor_report_details().getReport_template_name() + "_" + System.nanoTime() + ".pdf",
                        httpServletRequest);
            } else if (touchscreenAlertDTO.getSensor_report_details() != null &&
                    touchscreenAlertDTO.getSensor_report_details().getTemplate_type().equals("onboarded_asset_report")) {
                encodedString = touchscreenAlertDTO.getSensor_report_details().getFile();
                decodedBytes = Base64.getDecoder().decode(encodedString);
                filUrl = resourceUrlConfig.getOnboardedAssetReportFileUrl();
                fileDirectory = resourceUrlConfig.getOnboardedAssetReportFileDirectory();
                key = awsService.addExportFileToAWSS3(decodedBytes, filUrl, fileDirectory, null,
                        touchscreenAlertDTO.getSensor_report_details().getReport_template_name() + "_" + System.nanoTime() + ".pdf",
                        httpServletRequest);
            } else if (touchscreenAlertDTO.getChecklist() != null &&
                    touchscreenAlertDTO.getChecklist().getTemplate_type().equals("location_service")) {
                encodedString = touchscreenAlertDTO.getChecklist().getFile();
                decodedBytes = Base64.getDecoder().decode(encodedString);
                filUrl = resourceUrlConfig.getLocationCreateReportFileUrl();
                fileDirectory = resourceUrlConfig.getLocationCreateReportFileDirectory();
                key = awsService.addExportFileToAWSS3(decodedBytes, filUrl, fileDirectory, null,
                        touchscreenAlertDTO.getChecklist().getName() + "_" + System.nanoTime() + ".pdf",
                        httpServletRequest);
            }
            else if (touchscreenAlertDTO.getSensor_report_details() != null &&
                    touchscreenAlertDTO.getSensor_report_details().getTemplate_type().equals("inspection_report_general")) {
                encodedString = touchscreenAlertDTO.getSensor_report_details().getFile();
                decodedBytes = Base64.getDecoder().decode(encodedString);
                filUrl = resourceUrlConfig.getInspectionReportFileUrl();
                fileDirectory = resourceUrlConfig.getInspectionReportFileDirectory();
                String filename = touchscreenAlertDTO.getSensor_report_details().getReport_template_name().replace("/","-");
                key = awsService.addExportFileToAWSS3(decodedBytes, filUrl, fileDirectory, null, filename + "_" + System.nanoTime() + ".xlsx", httpServletRequest);
            }

            int expirationMinutes = 60 * 24 * 7; //valid for 1week
            String url = awsService.getPreSignedUrlForFileUpload(key, expirationMinutes, HttpMethod.GET);
            String encodedUrl = Base64.getEncoder().encodeToString(url.getBytes());
            String link = resourceUrlConfig.getEmailLink() + "?redirectUrl=" + encodedUrl;
            return link;
        } else {
            log.info("Invalid client params. Endpoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public String getVdmsAddress(VdmsDTO vdmsDTO, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsDTO{}", vdmsDTO);
        String address = "";
        if (vdmsDTO.getAddress() != null) {
            address = address + vdmsDTO.getAddress();
        }
        if (vdmsDTO.getCity() != null) {
            address = address + ", " + vdmsDTO.getCity();
        }
        if (vdmsDTO.getState() != null) {
            address = address + ", " + vdmsDTO.getState();
        }
        if (vdmsDTO.getCountry() != null) {
            address = address + ", " + vdmsDTO.getCountry();
        }
        if (vdmsDTO.getZip() != null) {
            address = address + ", " + vdmsDTO.getZip();
        }
        log.info("Fetching Vdms Address.EndPoint:{}", httpServletRequest.getRequestURI());
        address = address.isBlank() ? "-" : address;
        return address;
    }

    public String getCurrentTimeByTimestampAndTimezone(BigInteger timestamp, String timezone, HttpServletRequest httpServletRequest) {
        log.info("Payload:TimeStamp:{},TimeZone:{}", timestamp, timezone);
        DateFormat dateFormat = new SimpleDateFormat("hh:mm aa dd/MM/yyyy");
        if (timezone != null) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        Timestamp stamp = new Timestamp(timestamp.longValueExact());
        Date date = new Date(stamp.getTime());
        log.info("Fetching Current Time By TimeStamp And TimeZone.EndPoint:{}", httpServletRequest.getRequestURI());
        return dateFormat.format(date);

    }

    public String getCurrentTimeForServices(BigInteger timestamp, String timezone, HttpServletRequest httpServletRequest) {
        log.info("Payload:TimeStamp:{},TimeZone:{}", timestamp, timezone);
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm aa");
        if (timezone != null) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        Timestamp stamp = new Timestamp(timestamp.longValueExact());
        Date date = new Date(stamp.getTime());
        log.info("Fetching Current Time For Service.EndPoint:{}", httpServletRequest.getRequestURI());
        return dateFormat.format(date);
    }

    public String getCurrentTimeForInspection(BigInteger timestamp, String timezone, HttpServletRequest httpServletRequest) {
        log.info("Payload:TimeStamp:{},TimeZone:{}", timestamp, timezone);
        if (timestamp != null) {
            DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            if (timezone != null) {
                dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
            }
            Timestamp stamp = new Timestamp(timestamp.longValueExact());
            Date date = new Date(stamp.getTime());
            log.info("Fetching Current Time For Inspection.EndPoint:{}", httpServletRequest.getRequestURI());
            return dateFormat.format(date);
        } else {
            return "-";
        }
    }


    public JSONObject getCurrentDateAndTimeByTimestampAndTimezone(BigInteger timestamp, String timezone, HttpServletRequest httpServletRequest) {
        log.info("Payload:TimeStamp:{},TimeZone:{}", timestamp, timezone);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        if (timezone != null) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
            timeFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        Timestamp stamp = new Timestamp(timestamp.longValueExact());
        Date date = new Date(stamp.getTime());
        JSONObject data = new JSONObject();
        data.put("date", dateFormat.format(date));
        data.put("time", timeFormat.format(date));
        log.info("Fetching Current Date And Time By TimeStamp And TimeZone.EndPoint:{}", httpServletRequest.getRequestURI());
        return data;
    }


    private void checkAlertSchedule(TouchscreenAlertDTO touchscreenAlertDTO, HttpServletRequest httpServletRequest) {
        log.info("Payload:Touchscreen_Alert_DTO{}", touchscreenAlertDTO);
        JSONObject dateTimeData = getCurrentDateAndTimeByTimestampAndTimezone(touchscreenAlertDTO.getTimestamp(), touchscreenAlertDTO.getVdms().getTimezone(), httpServletRequest);
        for (int i = 0; i < touchscreenAlertDTO.getProfile_users().size(); i++) {
            String email = touchscreenAlertDTO.getProfile_users().get(i).getEmail();
            AlertScheduleDTO data = webClientService.getEmailAndSmsMuteByEmail(email, httpServletRequest);
            if (touchscreenAlertDTO.getProfile_users().get(i).getOrganisation_id() != null && data != null) {
                String schedule = webClientService.getAlertSchedule(email, dateTimeData, httpServletRequest);
                if (schedule != null && Integer.parseInt(schedule) == 1) {
                    touchscreenAlertDTO.getProfile_users().get(i).setEmailMute(data.getEmailMute());
                    touchscreenAlertDTO.getProfile_users().get(i).setSmsMute(data.getSmsMute());
                } else {
                    touchscreenAlertDTO.getProfile_users().remove(i);
                    i--;
                }
            } else {
                touchscreenAlertDTO.getProfile_users().get(i).setEmailMute(false);
                touchscreenAlertDTO.getProfile_users().get(i).setSmsMute(false);
            }
        }
    }


    public String capitalizeWords(String name, HttpServletRequest httpServletRequest) {
        if (name != null) {
            String str = name.replaceAll("_", " ");
            String[] words = str.split("\\s");
            StringBuilder capitalizeWord = new StringBuilder();

            for (String word : words) {
                String first = word.substring(0, 1);
                String second = word.substring(1);
                capitalizeWord.append(first.toUpperCase()).append(second).append(" ");
            }
            log.info("Capitalize Words,Name:{}.EndPoint:{}", name, httpServletRequest);
            return capitalizeWord.toString().trim();
        } else {
            return "-";
        }
    }


    //Global Inspection Report
    public ResponseEntity<?> sendDownloadEmail(String vdmsId, java.util.List<MultipartFile> files, String fileType, String body, HttpServletRequest httpServletRequest) throws IOException {
        String decodedJsonString = URLDecoder.decode(body, StandardCharsets.UTF_8);
        JSONObject downloadFileData = JSON.parseObject(decodedJsonString);
        log.info("PayLoad:vdmsId:{},fileType:{},template_type:{},email:{},subject:{},name:{}", vdmsId, fileType,
                downloadFileData.getString("template_type"), downloadFileData.getString("email"),
                downloadFileData.getString("subject"), downloadFileData.getString("name"));
        boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
        if (authorized) {
            if (files != null) {
                this.downloadFile(downloadFileData, files, httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Alert sent successfully", 200, true);
                log.info("Alert sent successfully. Endpoint: {}", httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.info("Invalid client params. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
        }
    }


    //Global Inspection Report
    private void downloadFile(JSONObject downloadFileData, List<MultipartFile> files, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Export Inspection Data Alert Triggered");
        if (files.size() == 1) {
            for (MultipartFile exportFile : files) {
                String extension = awsService.getFileExtensionByImageUrl(exportFile.getOriginalFilename(), httpServletRequest);
                String fileName = downloadFileData.getString("file_name") + "_" + System.nanoTime() + "." + extension;
                String key = awsService.addExportFileToAWSS3(exportFile.getBytes(), resourceUrlConfig.getDownloadFileUrl(), resourceUrlConfig.getDownloadFileDirectory(),
                        null, fileName, httpServletRequest);
                log.info("Key:" + key);
                int expirationMinutes = 60 * 24; //valid for 24hrs
                String url = awsService.getPreSignedUrlForFileUpload(key, expirationMinutes, HttpMethod.GET);
                String encodedUrl = Base64.getEncoder().encodeToString(url.getBytes());
                String link = resourceUrlConfig.getEmailLink() + "?redirectUrl=" + encodedUrl;
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("to", downloadFileData.getString("email"));
                jsonObject.put("link", link);
                jsonObject.putAll(downloadFileData);
                webClientAlertService.downloadFileAlert(jsonObject);
                log.info("Download File Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());
            }
        } else {
            String fileName = downloadFileData.getString("file_name") + "_" + System.nanoTime();
            ByteArrayOutputStream zipOutputStream = this.exportFilesToZIP(files, fileName, httpServletRequest);
            String key = awsService.addExportFileToAWSS3(zipOutputStream.toByteArray(), resourceUrlConfig.getDownloadFileUrl(),
                    resourceUrlConfig.getDownloadFileDirectory(), ".zip", fileName, httpServletRequest);

            log.info("Key:" + key);
            int expirationMinutes = 60 * 24; //valid for 24hrs
            String url = awsService.getPreSignedUrlForFileUpload(key, expirationMinutes, HttpMethod.GET);
            String encodedUrl = Base64.getEncoder().encodeToString(url.getBytes());
            String link = resourceUrlConfig.getEmailLink() + "?redirectUrl=" + encodedUrl;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("to", downloadFileData.getString("email"));
            jsonObject.put("link", link);
            jsonObject.putAll(downloadFileData);
            webClientAlertService.downloadFileAlert(jsonObject);
            log.info("Download File Alert Sent. EndPoint:{}", httpServletRequest.getRequestURI());

        }
    }

    private ByteArrayOutputStream exportFilesToZIP(List<MultipartFile> files, String zipFileName, HttpServletRequest httpServletRequest) throws IOException {
        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(zipOutputStream)) {
            for (MultipartFile file : files) {

                String fileName = file.getOriginalFilename();
                if (fileName == null) {
                    continue;
                }

                zip.putNextEntry(new ZipEntry(fileName));
                zip.write(file.getBytes());
                zip.closeEntry();
            }
        } catch (IOException e) {
            log.error("Error: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return zipOutputStream;
    }

    public HttpHeaders getRedirectionLink(String redirectUrl) {
        return awsService.getQrCodeRedirectionLink(redirectUrl);
    }
}
