package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.sclera.dto.*;
import io.sclera.exception.ClientException;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class AlertService {

    @Autowired
    private UserService userService;

    @Autowired
    private VdmsService vdmsService;

    @Autowired
    private VdmsProfileService vdmsprofileService;

    @Autowired
    private ProfileUserService profileuserService;

    @Autowired
    private WebClientService webClientService;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;


    public ResponseEntity<?> alertUser(String vdmsId, AlertDTO alertDTO, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        log.info("Payload: vdmsId: {}, AlertDTO:{}", vdmsId, alertDTO);
        if (alertDTO != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                UserDTO userDTO = userService.getMasterUserInfoByOrganisationId(alertDTO.getCustomer_org_id(), httpServletRequest);
                log.info("UserDTO: {}", userDTO);
                if (userDTO != null) {
                    VdmsDTO vdmsDTO = vdmsService.getVdmsInfoByVdmsId(alertDTO.getVdms_id(), httpServletRequest);
                    log.info("VdmsDTO: {}", vdmsDTO);
                    if (vdmsDTO != null) {
                        String vendorProfileId = vdmsprofileService.getProfileIdByVendorOrganisationIdAndVdmsId(alertDTO.getVdms_id(), alertDTO.getVendor_org_id(), httpServletRequest);
                        log.info("vendorProfileId: {}", vendorProfileId);

                        switch (alertDTO.getAlert_type()) {
                            case 2: //Vendor requested Remote Access
                                log.info("Vendor requested Remote Access.EndPoint:{}", httpServletRequest.getRequestURI());
                                this.remoteAccessEmail(vdmsId, userDTO, alertDTO, vdmsDTO, httpServletRequest);
                                break;
                            case 8: //Ticket Alert
                                log.info("Ticket Alert.EndPoint:{}", httpServletRequest.getRequestURI());
                                this.ticketAlert(userDTO, alertDTO, vdmsDTO, vendorProfileId, httpServletRequest);
                                break;
                            default:
                                log.error("Alert type does not exist.EndPoint:{}", httpServletRequest.getRequestURI());
                                throw new ClientException("Alert type does not exist", 718, httpServletRequest.getRequestURI());
                        }
                        ResponseDTO responseDTO = ScleraUtils.generatePayload("Alert sent successfully", 200, true);
                        log.info("Alert sent successfully. Endpoint: {}", httpServletRequest.getRequestURI());
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        log.error("VDMS does not exist.EndPoint:{}", httpServletRequest.getRequestURI());
                        throw new ClientException("VDMS does not exist", 728, httpServletRequest.getRequestURI());
                    }
                } else {
                    log.error("Master User not found.EndPoint:{}", httpServletRequest.getRequestURI());
                    throw new ClientException("Master User not found", 701, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }


    public void remoteAccessEmail(String vdmsId, UserDTO userDTO, AlertDTO alertDTO, VdmsDTO vdmsDTO, HttpServletRequest httpServletRequest) {
        log.info("Remote Access Alert");
        log.info("Payload:UserDTO:{},AlertDTO:{},VdmsDTO:{}", userDTO, alertDTO, vdmsDTO);
        ResponseDTO response = webClientService.getVendorDetailsForAlertServer(vdmsId, alertDTO.getVendor_email(), vdmsDTO.getVendor_org_id(), httpServletRequest);
        log.info("Response from Vendor: {}", response);
        String jsonString = JSON.toJSONString(response.getData());
        log.info("jsonString:{}", jsonString);
        UserDTO vendorDto = JSON.parseObject(jsonString, UserDTO.class);
        log.info("vendorDto:{}", vendorDto);
        if (vendorDto != null) {
            if (alertDTO.getEmail_alert() != 0 || alertDTO.getSms_alert() != 0) {

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("emailAlert", alertDTO.getEmail_alert());
                jsonObject.put("smsAlert", alertDTO.getSms_alert());
                jsonObject.put("to", userDTO.getEmail());
                jsonObject.put("propertyOwnerName", userDTO.getName());
                jsonObject.put("vendorName", vendorDto.getName());
                jsonObject.put("networkName", alertDTO.getDocker_name());
                jsonObject.put("vdmsId", alertDTO.getVdms_id());
                jsonObject.put("otp", alertDTO.getRemote_access_otp());
                jsonObject.put("propertyName", vdmsDTO.getProperty_name());
                jsonObject.put("vdmsAddress", getVdmsAddress(vdmsDTO, httpServletRequest));
                jsonObject.put("vendorEmail", vendorDto.getEmail());
                jsonObject.put("phone", vendorDto.getPhone());
                jsonObject.put("userPhone", userDTO.getPhone());
                jsonObject.put("userExtension", userDTO.getValue());
                webClientService.remoteAccessAlert(jsonObject, httpServletRequest);
                log.info("Remote Access Email.EndPoint:{}", httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Vendor Not Found.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Vendor not found", 735, httpServletRequest.getRequestURI());
        }
    }

    public void ticketAlert(UserDTO userDTO, AlertDTO alertDTO, VdmsDTO vdmsDTO, String vendorProfileId, HttpServletRequest httpServletRequest) {
        log.info("Ticket Alert");
        log.info("Payload:UserDTO:{},AlertDTO:{},VdmsDTO:{},vendorProfileId:{}", userDTO, alertDTO, vdmsDTO, vendorProfileId);
        JSONObject jsonObject = new JSONObject();
        String productImageURL = webClientService.getProductImagesByOrgIdAndProductId(alertDTO.getVendor_org_id(), alertDTO.getProduct_id(), httpServletRequest);
        log.info("Response from Stores Server: ProductImageURL: {}", productImageURL);
        if (productImageURL == null) {
            productImageURL = "https://app.sclera.com/alert/sclera-resource/static/images/common/generic.png";
        }


        if (vendorProfileId != null) {
            Set<ProfileUserDTO> vendorProfiles = profileuserService.getProfileUsersByProfileId(null, null, vendorProfileId, httpServletRequest);
            jsonObject.put("vendorProfiles", vendorProfiles);
        } else {
            jsonObject.put("vendorProfiles", new HashSet<>());
        }

        jsonObject.put("emailAlert", alertDTO.getEmail_alert());
        jsonObject.put("smsAlert", alertDTO.getSms_alert());
        jsonObject.put("localVendorEmailAlert", alertDTO.getLocal_vendor_email_alert());
        jsonObject.put("localVendorSmsAlert", alertDTO.getLocal_vendor_sms_alert());
        jsonObject.put("ticketType", alertDTO.getTicket_type());
        jsonObject.put("userMessage", alertDTO.getTicket_user_message());
        jsonObject.put("ticketNumber", alertDTO.getTicket_number());
        jsonObject.put("propertyOwnerName", userDTO.getName());
        jsonObject.put("deviceName", alertDTO.getDevice_name());
        jsonObject.put("networkName", alertDTO.getDocker_name());
        jsonObject.put("location", alertDTO.getLocation() != null ? alertDTO.getLocation() : "-");
        jsonObject.put("floor", alertDTO.getFloor() != null ? alertDTO.getFloor() : "-");
        jsonObject.put("building", alertDTO.getBuilding() != null ? alertDTO.getBuilding() : "-");
        jsonObject.put("propertyName", vdmsDTO.getProperty_name());
        jsonObject.put("vdmsId", vdmsDTO.getVdms_id());
        jsonObject.put("systemType", alertDTO.getDocker_system_type());
        jsonObject.put("vdmsAddress", getVdmsAddress(vdmsDTO, httpServletRequest));
        jsonObject.put("userEmail", userDTO.getEmail());
        jsonObject.put("userExtension", userDTO.getValue());
        jsonObject.put("userPhone", userDTO.getPhone());
        jsonObject.put("productId", alertDTO.getProduct_id());
        jsonObject.put("productImage", productImageURL);
        jsonObject.put("to", alertDTO.getLocal_vendor_email());
        jsonObject.put("name", alertDTO.getLocal_vendor_name());
        jsonObject.put("localVendorPhone", alertDTO.getLocal_vendor_phone());
        jsonObject.put("localVendorExtension", alertDTO.getLocal_vendor_extension());

        if ((alertDTO.getEmail_alert() != 0 || alertDTO.getSms_alert() != 0) ||
                (alertDTO.getLocal_vendor_email_alert() != 0 || alertDTO.getLocal_vendor_sms_alert() != 0)) {
            webClientService.ticketAlert(jsonObject, httpServletRequest);
            log.info("Ticket Alert Sent Successfully.EndPoint:{}", httpServletRequest.getRequestURI());
        }
    }

    public String getVdmsAddress(VdmsDTO vdmsDTO, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsDTO:{}", vdmsDTO);
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
        log.info("Fetching VDMS Address.EndPoint:{}", httpServletRequest.getRequestURI());
        return address;
    }

}
