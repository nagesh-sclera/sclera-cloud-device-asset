package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import io.sclera.dto.*;
import io.sclera.util.ScleraWebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

@Service
@Slf4j
public class WebClientService {

    @Autowired
    public ScleraWebClient scleraWebClient;

    @Value("${sclera.server.alert.url}")
    private String ALERT_SERVER_URL;

    @Value("${sclera.server.stores.url}")
    private String STORES_SERVER_URL;

    @Value("${sclera.server.mongodb.url}")
    private String MONGODB_SERVER_URL;
    @Value("${sclera.server.health.url}")
    private String HEALTH_SERVER_URL;
    @Value("${sclera.server.login.url}")
    private String LOGIN_SERVER_URL;
    @Value("${sclera.server.vendor.url}")
    private String VENDOR_SERVER_URL;
    @Value("${sclera.server.bff.url}")
    private String BFF_SERVER_URL;
    @Autowired
    private UserActionLogService userActionLogService;

    @Value("${sclera.server.multitenancy.londonUrl}")
    private String MULTITENANCY_LONDON_SERVER_URL;

    @Value("${sclera.server.multitenancy.usUrl}")
    private String MULTITENANCY_US_SERVER_URL;

    private String ipApiKey;

    @Autowired
    public WebClientService(String ipApiKey) {
        this.ipApiKey = ipApiKey;
    }

    @Value("${run-pod.url}")
    private String RUNPOD_SERVER_URL;

    public Map<String, String> constructToken(HttpServletRequest request) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", request.getHeader("Authorization"));
        return header;
    }

    public Map<String, String> constructRunPodToken(String token) {
        Map<String, String> header = new HashMap<>();
        header.put("Authorization", "Bearer " + token);
        return header;
    }


    public void remoteAccessAlert(JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/remoteAccessAlert",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                null,
                null,
                null);
    }

    public void ticketAlert(JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        ResponseEntity<?> response = scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/ticketAlert",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                30000,
                30000,
                null);
    }

    public String getProductImagesByOrgIdAndProductId(String vendor_org_id, String product_id, HttpServletRequest httpServletRequest) {
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                STORES_SERVER_URL + "/api/user/vendorProduct/" + product_id + "/getProductImageByProductId",
                null,
                null,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        ResponseDTO responseDTO = response.getBody();
        if (responseDTO.getData() != null) {
            return responseDTO.getData().toString();
        } else {
            return null;
        }
    }


    public ResponseEntity<ResponseDTO> inviteRegisteredVendor(JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/inviteRegisteredVendor",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
    }

    public ResponseEntity<ResponseDTO> inviteUnregisteredVendorByEmail(JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/inviteUnregisteredVendorByEmail",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
    }

    public ResponseEntity<ResponseDTO> networkRemovedEmail(JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/networkRemovedEmail",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
    }

    public ResponseEntity<ResponseDTO> registeredVendorTransfer(JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/registeredVendorTransfer",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
    }

    public ResponseEntity<ResponseDTO> unregisteredVendorTransfer(JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return scleraWebClient.httpRequest(
                HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/unregisteredVendorTransfer",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
    }

    public ResponseEntity<ResponseDTO> sendActivationEmail(JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/sendActivationEmail",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
    }

    public ResponseEntity<ResponseDTO> sendWelcomeEmail(JSONObject jsonObject, HttpServletRequest httpServletRequest) {

        return scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/sendWelcomeEmail",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
    }

    public ResponseEntity<ResponseDTO> vdmsRemovedEmail(JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/vdmsRemoved",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                0);
    }

    public ResponseEntity<ResponseDTO> vdmsTranferEmail(JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return scleraWebClient.httpRequest(HttpMethod.POST,
                ALERT_SERVER_URL + "/vdms/vdmsTransfer",
                null,
                null,
                jsonObject.toJSONString(),
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                0);
    }


    public List<IocDto> getAllIocDetails(String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/user/" + email + "/getAllIocDetails",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Response:{}", response);
        ResponseDTO responseDTO = response.getBody();

        String jsonString = JSON.toJSONString(responseDTO.getData());
        return JSON.parseArray(jsonString, IocDto.class);
    }

    public IocDto getIocDetailsByIocId(String email, String iocId, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        params.put("iocId", iocId);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/user/" + email + "/getIocDetailsByIocId",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Response:{}", response);
        ResponseDTO responseDTO = response.getBody();

        String jsonString = JSON.toJSONString(responseDTO.getData());
        return JSON.parseObject(jsonString, IocDto.class);

    }

    public ResponseEntity<ResponseDTO> addIocData(String orgId, String email, IocDto iocData, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        return scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/organisation/" + orgId + "/user/" + email + "/addIocData",
                params,
                headers,
                iocData,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                0);
    }

    public ResponseEntity<ResponseDTO> deleteIocData(String orgId, String email, String iocId, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        return scleraWebClient.httpRequest(HttpMethod.DELETE,
                BFF_SERVER_URL + "/api/organisation/" + orgId + "/user/" + email + "/iocId/" + iocId + "/deleteIocData",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                0);
    }

    public List<IocDto> getAllIocDetailsByOrgId(String email, String orgId, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/organisation/" + orgId + "/user/" + email + "/getAllIocDetailsByOrgId",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Response:{}", response);
        ResponseDTO responseDTO = response.getBody();

        String jsonString = JSON.toJSONString(responseDTO.getData());
        return JSON.parseArray(jsonString, IocDto.class);

    }


    public String authenticateUser(TouchscreenDTO userdto, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userdto);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/vdms/authenticate/user",
                null,
                headers,
                JSONString,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        JSONObject jsonObject = JSON.parseObject(response.getBody());
        if (jsonObject != null) {
            if (jsonObject.getBoolean("success")) {
                return jsonObject.getString("data");
            } else {
                return jsonObject.getString("errorMessage");
            }
        } else {
            return response.getBody();
        }
    }

    public ResponseDTO validateRoleByEmail(String email, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                VENDOR_SERVER_URL + "/api/vdms/organisation/vendor/" + email + "/validateMasterVendorRoleByVendorEmail",
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        return response.getBody();

    }

    public UserDTO getVendorDetailsByEmail(String email, String vendorOrgId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: Email: {}, VendorOrgId: {}, LoggedInUser: {}", email, vendorOrgId, loggedInUser);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                VENDOR_SERVER_URL + "/api/vdms/organisation/" + vendorOrgId + "/vendor/" + email + "/getVendorDetailsByVendorEmail",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Response: {}", response);
        ResponseDTO responseDTO = response.getBody();
        if (responseDTO.getData() != null) {
            String jsonString = JSON.toJSONString(responseDTO.getData());
            return JSON.parseObject(jsonString, UserDTO.class);
        } else {
            return null;
        }
    }

    public ResponseDTO getVendorDetailsByEmailForTouchScreen(String email, String vendorOrgId, String vdmsId, HttpServletRequest httpServletRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", vdmsId);
        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                VENDOR_SERVER_URL + "/api/vdms/organisation/" + vendorOrgId + "/vendor/" + email + "/getVendorDetailsByVendorEmail",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        return response.getBody();

    }

    public ResponseDTO getVendorDetailsForAlertServer(String vdmsId, String email, String vendorOrgId, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", vdmsId);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                VENDOR_SERVER_URL + "/api/vdms/organisation/" + vendorOrgId + "/vendor/" + email + "/getVendorDetailsByVendorEmail",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        return response.getBody();

    }

    public ResponseDTO getMasterVendorDetailsByOrganisationId(String vendor_org_id, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                VENDOR_SERVER_URL + "/api/vdms/organisation/" + vendor_org_id + "/vendor/getMasterVendorDetailsByOrganisationId",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        return response.getBody();

    }

    public ResponseDTO getRoleByUserEmail(String organisation_id, String user_email, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/user/" + user_email + "/role",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);

        System.out.println("response ===>" + response);
        log.info("Fetching Role By User Email:{},EndPoint:{}", user_email, httpServletRequest.getRequestURI());
        String jsonString = JSON.toJSONString(response.getBody());
        System.out.println("-->" + jsonString);
        ResponseDTO responseDTO = JSON.parseObject(jsonString, ResponseDTO.class);
        System.out.println("response dto ===>" + responseDTO.getData().toString());
        return responseDTO;

    }


    public UserDTO getRoleAndOrganisationIdByVendorEmail(String user_email, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/user/" + user_email + "/data",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("RESPONSE :" + response);
        JSONObject jsonObject1 = JSON.parseObject(response.getBody());
        JSONObject jsonObject2 = jsonObject1.getJSONObject("data");
        log.info("Fetching Role And Organisation_Id By Vendor_Email:{},EndPoint:{}", user_email, httpServletRequest.getRequestURI());
        return JSON.parseObject(jsonObject2.toJSONString(), UserDTO.class);
    }

    public ResponseDTO getRoleAndOrganisationIdByVendorEmail2(String user_email, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/user/" + user_email + "/data",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Fetching Role And Organisation_Id By Vendor_Email:{},EndPoint:{}", user_email, httpServletRequest.getRequestURI());
        return JSON.parseObject(response.getBody(), ResponseDTO.class);
    }

    public ResponseDTO createUserByPrivilegedUserInLoginDB(String customer_org_id, String email, UserDTO userdto, boolean ipAclEnabled, boolean geoAclEnabled, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:customer_org_id:{},email:{},UserDTO:{},ipAclEnabled:{},geoAclEnabled:{}", customer_org_id, email, userdto, ipAclEnabled, geoAclEnabled);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        params.put("ipAclEnabled", String.valueOf(ipAclEnabled));
        params.put("geoAclEnabled", String.valueOf(geoAclEnabled));

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userdto);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/vdms/organisation/" + customer_org_id + "/user/" + email + "/createUserByPrivilegedUser",
                params,
                headers,
                userdto,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Creating User By Privileged_User In Login_DB,EndPoint:{}", httpServletRequest.getRequestURI());
        return response.getBody();
    }


    public ResponseDTO createUserInLoginDB(String customer_org_id, String email, UserDTO userdto, boolean ipAclEnabled, boolean geoAclEnabled, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:customer_org_id:{},email:{},UserDTO:{}", customer_org_id, email, userdto);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        params.put("ipAclEnabled", String.valueOf(ipAclEnabled));
        params.put("geoAclEnabled", String.valueOf(geoAclEnabled));
        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userdto);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/vdms/organisation/" + customer_org_id + "/user/" + email + "/createUserByMasterUser",
                params,
                headers,
                userdto,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Create User In Login DB ,EndPoint:{}", httpServletRequest.getRequestURI());
        return response.getBody();

    }

    public ResponseDTO createPropertyAdminInLoginDB(String customer_org_id, String email, UserDTO userdto, boolean ipAclEnabled, boolean geoAclEnabled, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:customer_org_id:{},email:{},UserDTO:{}", customer_org_id, email, userdto);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        params.put("ipAclEnabled", String.valueOf(ipAclEnabled));
        params.put("geoAclEnabled", String.valueOf(geoAclEnabled));

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userdto);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/vdms/organisation/" + customer_org_id + "/user/" + email + "/createPropertyAdminByMasterUser",
                params,
                headers,
                JSONString,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Create User In Login DB ,EndPoint:{}", httpServletRequest.getRequestURI());
        return response.getBody();

    }

    public ResponseDTO createSuperAdminInLoginDB(UserDTO userDTO, String loggedInUser, String email) {
        log.info("Payload:,email:{},UserDTO:{}", email, userDTO);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/vdms/organisation/superAdmin/" + email,
                params,
                null,
                userDTO,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Create Super Admin In Login DB (createSuperAdminInLoginDB)");

        return response.getBody();
    }

    public ResponseDTO updateMasterUserInLoginDB(UserDTO userdto, String customer_org_id, String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:customer_org_id:{},email:{},UserDTO:{}", customer_org_id, email, userdto);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userdto);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.PUT,
                BFF_SERVER_URL + "/api/vdms/organisation/" + customer_org_id + "/masterUser/" + email + "/editMasterUserByEmail",
                params,
                headers,
                userdto,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Update Master User In Login DB ,EndPoint:{}", httpServletRequest.getRequestURI());

        return response.getBody();
    }

    public ResponseDTO updateAdminInLoginDB(UserDTO userdto, String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:email:{},UserDTO:{}", email, userdto);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userdto);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.PUT,
                BFF_SERVER_URL + "/api/vdms/organisation/admin/" + email + "/editAdminByEmail",
                params,
                headers,
                userdto,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Update Admin In Login DB,EndPoint:{}", httpServletRequest.getRequestURI());
        return response.getBody();
    }


    public ResponseDTO updateSuperAdminInLoginDB(UserDTO userdto, String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:email:{},UserDTO:{}", email, userdto);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userdto);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.PUT,
                BFF_SERVER_URL + "/api/vdms/organisation/superAdmin/" + email,
                params,
                headers,
                userdto,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Update Super Admin In Login DB,EndPoint:{}", httpServletRequest.getRequestURI());
        return response.getBody();

    }

    public ResponseDTO deleteUserByMasterUser(String orgId, String email, UserDTO userDTO, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userDTO);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.DELETE,
                BFF_SERVER_URL + "/api/vdms/organisation/" + orgId + "/user/" + email + "/deleteUserByEmail",
                params,
                headers,
                userDTO,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Delete User By Master User.EndPoint:{}", httpServletRequest.getRequestURI());
        return response.getBody();

    }

    public ResponseDTO deletePropertyAdminByMasterUser(String orgId, String email, UserDTO userDTO, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userDTO);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.DELETE,
                BFF_SERVER_URL + "/api/vdms/organisation/" + orgId + "/user/" + email + "/deletePropertyAdminByMasterUser",
                params,
                headers,
                userDTO,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Delete User By Master User.EndPoint:{}", httpServletRequest.getRequestURI());
        return response.getBody();

    }


    public ResponseDTO deleteUserByAdmin(String orgId, String email, UserDTO userDTO, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:org_id:{},email:{},UserDTO:{}", orgId, email, userDTO);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userDTO);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.DELETE,
                BFF_SERVER_URL + "/api/vdms/organisation/" + orgId + "/user/" + email + "/deleteUserByAdmin",
                params,
                headers,
                userDTO,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Delete User By Admin.EndPoint:{}", httpServletRequest.getRequestURI());
        return JSON.parseObject(response.getBody(), ResponseDTO.class);

    }

    public ResponseDTO deleteUserInStoresDB(String vendor_org_id, String user_email, HttpServletRequest httpServletRequest) {
        log.info("Payload:vendor_org_id:{},email:{}", vendor_org_id, user_email);
        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.DELETE,
                STORES_SERVER_URL + "/api/user/organisation/" + vendor_org_id + "/user/" + user_email,
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Delete User In Stores DB.EndPoint:{}", httpServletRequest.getRequestURI());
        return JSON.parseObject(response.getBody(), ResponseDTO.class);
    }

    public ResponseDTO deleteAdminBySuperAdmin(String email, UserDTO userDTO, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:email:{},UserDTO:{}", email, userDTO);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userDTO);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.DELETE,
                BFF_SERVER_URL + "/api/vdms/organisation/superAdmin/" + email + "/deleteAdminBySuperAdmin",
                params,
                headers,
                userDTO,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Delete Admin By Super Admin.EndPoint:{}", httpServletRequest.getRequestURI());
        return JSON.parseObject(response.getBody(), ResponseDTO.class);
    }


    public UserDTO getUserDetailsByEmailFromLoginDB(String user_email, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<String> response = scleraWebClient.httpRequest(
                HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/user/" + user_email + "/details",
                null,
                null,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        JSONObject jsonObject1 = JSON.parseObject(response.getBody());
        JSONObject jsonObject2 = jsonObject1.getJSONObject("data");
        log.info("Fetching User Details By Email:{}, From Login DB.EndPoint:{}", user_email, httpServletRequest.getRequestURI());
        return JSON.parseObject(jsonObject2.toJSONString(), UserDTO.class);
    }

    public List<String> getVendorEmailsByOrganisationId(String organisation_id, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                VENDOR_SERVER_URL + "/api/vdms/organisation/" + organisation_id + "/getAllVendorEmailListByOrgId",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Fetching Vendor Emails By Organisation_Id:{},EndPoint:{}", organisation_id, httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = response.getBody();
        List<String> vendorEmails = new ArrayList<>();
        if (responseDTO != null && responseDTO.getData() != null) {
            String jsonString = JSON.toJSONString(responseDTO.getData());
            vendorEmails = JSON.parseObject(jsonString, new TypeReference<>() {
            });
        }
        return vendorEmails;
    }

    public List<UserDTO> getAllVendorDetailsByOrganisationId(String organisation_id, String vdms_id, String name, HttpServletRequest httpServletRequest) {
        log.info("Payload:organisation_id:{},vdms_id:{},name:{}", organisation_id, vdms_id, name);
        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                VENDOR_SERVER_URL + "/api/vdms/organisation/" + organisation_id + "/vdms/" + vdms_id + "/docker/" + name + "/vendors",
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);

        ResponseDTO responseDTO = response.getBody();
        List<UserDTO> vendorEmails = new ArrayList<>();
        if (responseDTO != null && responseDTO.getData() != null) {
            String jsonString = JSON.toJSONString(responseDTO.getData());
            vendorEmails = JSON.parseObject(jsonString, new TypeReference<>() {
            });
        }
        return vendorEmails;
    }


    public void updateScleraFXVersionByEmailAndOS(String email, String os, String db_version, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.PUT,
                BFF_SERVER_URL + "/api/vdms/user/" + email + "/os/" + os + "/fx/" + db_version,
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
    }

    public String getPublicKeyFromLoginServer() {
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.GET,
                LOGIN_SERVER_URL + "/getPublicKey",
                null,
                null,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        return response.getBody();
    }

    public ResponseDTO updateDataEntryInLoginDB(UserDTO userdto, String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserDTO:{},Email:{}", userdto, email);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userdto);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.PUT,
                BFF_SERVER_URL + "/api/vdms/organisation/dataEntry/" + email + "/editDataEntryByEmail",
                params,
                headers,
                JSONString,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Update Data Entry In Login DB .Endpoint:{}", httpServletRequest.getRequestURI());

        return JSON.parseObject(response.getBody(), ResponseDTO.class);
    }

    public ResponseDTO deleteDataEntryInLoginDB(String email, UserDTO userDTO, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserDTO:{},Email:{}", userDTO, email);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userDTO);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.DELETE,
                BFF_SERVER_URL + "/api/vdms/organisation/admin/" + email + "/deleteDataEntryByAdmin",
                params,
                headers,
                JSONString,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Delete Data Entry In Login DB.Endpoint:{}", httpServletRequest.getRequestURI());

        return JSON.parseObject(response.getBody(), ResponseDTO.class);
    }

    public Map<String, String> constructParams(String key, List<String> id, HttpServletRequest httpServletRequest) {
        Map<String, String> param = new HashMap<>();
        String x = id.toString();
        x = x.replaceAll("\\[", "").replaceAll("\\]", "");
        param.put(key, x);
        return param;
    }

    public JSONObject getAllHelperDetailsByHelperIds(List<String> helperId, Integer type, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = this.constructParams("helperIds", helperId, httpServletRequest);
        params.put("type", type.toString());
        params.put("loggedInUser", loggedInUser);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.GET,
                STORES_SERVER_URL + "/api/user/helper/getAllHelperDetailsByHelperIdsAndType",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        JSONObject jsonObject = JSON.parseObject(response.getBody());
        return jsonObject;

    }

    public ResponseDTO createParentUserInLoginDB(UserDTO userDTO, HttpServletRequest httpServletRequest) {
        String JSONString = JSON.toJSONString(userDTO);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/vdms/organisation/user/createMasterUser",
                null,
                null,
                JSONString,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);

        return JSON.parseObject(response.getBody(), ResponseDTO.class);

    }

    public ResponseDTO createParentUserInStoresDB(UserDTO userDTO, String org_id, HttpServletRequest
            httpServletRequest) {
        String JSONString = JSON.toJSONString(userDTO);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.POST,
                STORES_SERVER_URL + "/api/user/organisation/" + org_id + "/user",
                null,
                null,
                JSONString,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);

        return JSON.parseObject(response.getBody(), ResponseDTO.class);
    }

    public void deleteParentUserInLoginDB(String vendor_org_id, String user_email, HttpServletRequest httpServletRequest) {
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.DELETE,
                BFF_SERVER_URL + "/api/vdms/organisation/" + vendor_org_id + "/user/" + user_email + "/deleteParentUserByEmail",
                null,
                null,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Delete Parent User In Login DB.vendor_org_id:{}.user_email:{}.Endpoint:{}", vendor_org_id, user_email, httpServletRequest.getRequestURI());
    }


    public ResponseDTO createAdminInLoginDB(UserDTO userDTO, String email, boolean ipAclEnabled, boolean geoAclEnabled, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserDTO:{},email:{}", userDTO, email);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        params.put("ipAclEnabled", String.valueOf(ipAclEnabled));
        params.put("geoAclEnabled", String.valueOf(geoAclEnabled));

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userDTO);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/vdms/organisation/superAdmin/" + email + "/createAdminBySuperAdmin",
                params,
                headers,
                JSONString,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Create Admin In Login DB.EndPoint:{}", httpServletRequest.getRequestURI());
        return response.getBody();
    }

    public ResponseDTO updateUserInLoginDB(UserDTO userdto, String customer_org_id, String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserDTO:{},customer_org_id:{},email:{}", userdto, customer_org_id, email);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
//        String JSONString = JSON.toJSONString(userdto);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.PUT,
                BFF_SERVER_URL + "/api/vdms/organisation/" + customer_org_id + "/user/" + email + "/editUserByEmail",
                params,
                headers,
                userdto,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Update User In Login DB.EndPoint:{}", httpServletRequest.getRequestURI());
        return response.getBody();
    }


    public ResponseDTO updatePropertyAdminInLoginDB(UserDTO userdto, String customer_org_id, String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserDTO:{},customer_org_id:{},email:{}", userdto, customer_org_id, email);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userdto);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.PUT,
                BFF_SERVER_URL + "/api/vdms/organisation/" + customer_org_id + "/user/" + email + "/editPropertyAdminByEmail",
                params,
                headers,
                JSONString,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Update User In Login DB.EndPoint:{}", httpServletRequest.getRequestURI());
        return JSON.parseObject(response.getBody(), ResponseDTO.class);
    }

    public ResponseDTO createDataEntryInLoginDB(UserDTO userDTO, String email, boolean ipAclEnabled, boolean geoAclEnabled, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserDTO:{},email:{},ipAclEnabled:{},geoAclEnabled:{}", userDTO, email, ipAclEnabled, geoAclEnabled);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        params.put("ipAclEnabled", String.valueOf(ipAclEnabled));
        params.put("geoAclEnabled", String.valueOf(geoAclEnabled));

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userDTO);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/vdms/organisation/admin/" + email + "/createDataEntryByAdmin",
                params,
                headers,
                JSONString,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Create Data Entry In Login DB:EndPoint:{}", httpServletRequest.getRequestURI());
        return response.getBody();
    }

    public ResponseDTO createOrgAdminInLoginDB(String customerOrgId, String email, UserDTO userdto, boolean ipAclEnabled, boolean geoAclEnabled, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        params.put("ipAclEnabled", String.valueOf(ipAclEnabled));
        params.put("geoAclEnabled", String.valueOf(geoAclEnabled));
        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userdto);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/vdms/organisation/" + customerOrgId + "/masterUser/" + email + "/createOrgAdminByMasterUser",
                params,
                headers,
                userdto,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Create Organisation admin In Login DB (createOrgAdminInLoginDB)");
        return response.getBody();
    }

    public ResponseDTO updateOrgAdminInLoginDB(UserDTO userdto, String customerOrgId, String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserDTO:{},customer_org_id:{},email:{}", userdto, customerOrgId, email);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userdto);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.PUT,
                BFF_SERVER_URL + "/api/vdms/organisation/" + customerOrgId + "/masterUser/" + email + "/editOrgAdminByEmail",
                params,
                headers,
                JSONString,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Update User In Login DB:EndPoint:{}", httpServletRequest.getRequestURI());
        return JSON.parseObject(response.getBody(), ResponseDTO.class);
    }

    public ResponseDTO deleteOrgAdminByMasterUserFromLogin(String orgId, String email, UserDTO userDTO, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserDTO:{},customer_org_id:{},email:{}", userDTO, orgId, email);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userDTO);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.DELETE,
                BFF_SERVER_URL + "/api/vdms/organisation/" + orgId + "/masterUser/" + email + "/deleteOrgAdminByMasterUser",
                params,
                headers,
                JSONString,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Delete Organisation Admin By Master User:EndPoint:{}", httpServletRequest.getRequestURI());
        return JSON.parseObject(response.getBody(), ResponseDTO.class);
    }

    public ResponseDTO deleteVdmsHealthByVdmsId(String vdmsId, String loggedInUser, HttpServletRequest
            httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        ResponseEntity<String> res = scleraWebClient.httpRequest(HttpMethod.DELETE,
                HEALTH_SERVER_URL + "/api/vdms/" + vdmsId + "/deleteVdmsByVdmsId",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);

        return JSON.parseObject(res.getBody(), ResponseDTO.class);
    }

    public ResponseDTO addVdmsInHealth(String vdmsId, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.POST,
                HEALTH_SERVER_URL + "/api/vdms/" + vdmsId + "/addVdms",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                0,
                0,
                0);

        return response.getBody();
    }

    public void deleteVdmsTokenInLoginDB(String vdmsId, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        scleraWebClient.httpRequest(HttpMethod.DELETE,
                BFF_SERVER_URL + "/api/vdms/" + vdmsId + "/deleteVdmsTokenByVdmsId",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);

    }

    public ResponseDTO addVdmsOneTimePassword(String vdmsId, String vdmsOneTimePassword, String email, String deploymentType, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        params.put("deploymentType", deploymentType);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/vdms/" + vdmsId + "/user/" + email + "/addVdmsOneTimePassword",
                params,
                headers,
                vdmsOneTimePassword,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("response" + response);
        return JSON.parseObject(response.getBody().toString(), ResponseDTO.class);

    }

    public ResponseDTO checkVdmsTokenCredentials(String vdmsId, TouchscreenDTO touchscreenDTO, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        headers.put("x-real-ip", httpServletRequest.getHeader("x-real-ip").toString());
        String JSONString = JSON.toJSONString(touchscreenDTO);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/vdms/" + vdmsId + "/checkVdmsCredentials",
                null,
                headers,
                JSONString,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("response" + response);

        if (response.getStatusCode().is4xxClientError()) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        }
        return JSON.parseObject(response.getBody(), ResponseDTO.class);
    }

    public ResponseDTO getVdmsAccessTokenByRefreshToken(String vdmsId, TouchscreenDTO touchscreenDTO, HttpServletRequest httpServletRequest) {
        String JSONString = JSON.toJSONString(touchscreenDTO);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/vdms/" + vdmsId + "/getvdmsAccessTokenByRefreshToken",
                null,
                null,
                JSONString,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("response" + response);
        return JSON.parseObject(response.getBody(), ResponseDTO.class);
    }

    public ResponseDTO getVdmsAccessTokenByVdmsIdAndPassword(String vdmsId, String password, HttpServletRequest httpServletRequest) {

        Map<String, String> headers = this.constructToken(httpServletRequest);
        headers.put("x-real-ip", httpServletRequest.getHeader("x-real-ip").toString());
        Map<String, String> params = new HashMap<>();
        params.put("password", password);
        ResponseEntity<String> res = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/" + vdmsId + "/getVdmsAccessTokenByVdmsIdAndPassword",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        ResponseDTO responseDTO = JSON.parseObject(res.getBody(), ResponseDTO.class);
        log.info("response" + res);
        return JSON.parseObject(res.getBody(), ResponseDTO.class);

    }

    public ResponseDTO getVdmsTokenPasswordByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/" + vdmsId + "/getVdmsPasswordByVdmsId",
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("response" + response);
        return JSON.parseObject(response.getBody().toString(), ResponseDTO.class);
    }

    public ResponseDTO getUserDetailsAndProxyProfileDetailsByEmail(String username, String orgId) {
        Map<String, String> params = new HashMap<>();
        params.put("orgId", orgId);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/user/" + username + "/getUserDetailsAndProxyProfileDetailsByEmail",
                params,
                null,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Response==>" + response);
        return JSON.parseObject(response.getBody().toString(), ResponseDTO.class);
    }

    public List<UserDTO> getUserDetailsByAdminEmail(String admin_email, String key, String org_id, int limit, int offset, List<String> userProfiles,
                                                    boolean withoutProfile, String active, HttpServletRequest httpServletRequest) {
        log.info("Payload:User_Email:{},Key:{},org_id:{},limit:{},offset:{},userProfiles:{},withoutProfile:{}",
                admin_email, key, org_id, limit, offset, userProfiles, withoutProfile);
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("limit", String.valueOf(limit));
        params.put("offset", String.valueOf(offset));
        params.put("org_id", String.valueOf(org_id));
        params.put("userProfiles", JSON.toJSONString(userProfiles));
        params.put("withoutProfile", String.valueOf(withoutProfile));
        params.put("active", active);
        log.info("params:{}", params);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/user/" + admin_email + "/getUserDetailsByAdminEmail",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        JSONObject jsonObject = JSON.parseObject(response.getBody());
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        return JSON.parseArray(jsonArray.toString(), UserDTO.class);
    }

    public ResponseDTO deleteMasterUserInStoresDB(String vendor_org_id, String user_email, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);

        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.DELETE,
                STORES_SERVER_URL + "/api/user/organisation/" + vendor_org_id + "/user/" + user_email + "/deleteProductByMasterUser",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);

        return JSON.parseObject(response.getBody(), ResponseDTO.class);
    }

    public JSONObject getLoginCountByEmail(List<String> userList, String orgId, HttpServletRequest
            httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<JSONObject> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/organisation/" + orgId + "/getLoginCountByEmail",
                null,
                headers,
                userList,
                MediaType.APPLICATION_JSON,
                JSONObject.class,
                null,
                null,
                null,
                null,
                null);

        return JSONObject.from(response.getBody().get("data"));
    }


// BFF changes

    public ResponseDTO getOrganisationDetailsByOrgId(List<String> orgIds) {

        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                LOGIN_SERVER_URL + "/api/getOrganisationDetailsByOrgId",
                null,
                null,
                orgIds,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                0);
        System.out.println("===>" + response.getBody());
        return response.getBody();
    }

    public ResponseDTO getAllUserDetailsByOrgId(List<String> orgIds) {


        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                LOGIN_SERVER_URL + "/api/getAllUserDetailsByOrgId",
                null,
                null,
                orgIds,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                0
        );
        return response.getBody();
    }

    public ResponseDTO getAllUserDetails(HttpServletRequest httpServletRequest) {

        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                LOGIN_SERVER_URL + "/api/migrateData/getAllUserDetails",
                null,
                null,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                0);
//        ResponseDTO responseDTO = response.getBody();
        System.out.println("===>" + response.getBody());
        return response.getBody();
    }

    public List<UserProfileDTO> getUserProfileDetailsByOrgId(List<String> orgIds) {

        ResponseDTO responseDTO = scleraWebClient.httpRequest(HttpMethod.GET,
                LOGIN_SERVER_URL + "/api/migrateData/getAllUerProfilesByOrgIds",
                null,
                null,
                orgIds,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null).getBody();
        String userProfiles = JSON.toJSONString(responseDTO.getData());
        List<UserProfileDTO> userProfileDTO = JSON.parseArray(userProfiles, UserProfileDTO.class);
        return userProfileDTO;

    }


    public List<UserDTO> getUsersLastLoggedIntimeByEmailIds(List<String> emailIds, HttpServletRequest
            httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/getUsersLastLoginTimeByEmailIds",
                null,
                headers,
                emailIds,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                20000,
                20000,
                10000);

        return JSON.parseArray(JSON.toJSONString(response.getBody().getData()), UserDTO.class);
    }

    public IocDto getIocDetailsByIocIdAndOrgId(String email, String iocId, String orgId, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        params.put("iocId", iocId);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/organisation/" + orgId + "/user/" + email + "/getIocDetailsByIocIdAndOrgId",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Response:{}", response);
        ResponseDTO responseDTO = response.getBody();

        String jsonString = JSON.toJSONString(responseDTO.getData());
        return JSON.parseObject(jsonString, IocDto.class);
    }

    public List<UserDTO> getAllUserInfo(HttpServletRequest httpServletRequest) {

        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseDTO responseDTO = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/getAllUserDetails",
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null).getBody();
        if (responseDTO.getData() != null) {
            List<UserDTO> userDTOS = JSON.parseArray(JSON.toJSONString(responseDTO.getData()), UserDTO.class);
            return userDTOS;
        } else {
            return null;
        }

    }

    public String getImageUrlByEmail(String email, HttpServletRequest httpServletRequest) {

        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/user/getImageUrlsByEmail",
                null,
                headers,
                email,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);

        return response.getBody();
    }

    public List<UserDTO> getAllUserInfoByOrganisationId(String organisationId, String key, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {

        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("pageNo", String.valueOf(pageNo));
        params.put("pageSize", String.valueOf(pageSize));
        ResponseDTO responseDTO = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/organisation/" + organisationId + "/getAllUserInfoByOrganisationId",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null).getBody();

        if (responseDTO.getData() != null) {
            List<UserDTO> userDTOS = JSON.parseArray(JSON.toJSONString(responseDTO.getData()), UserDTO.class);
            return userDTOS;
        } else {
            return null;
        }
    }

    public List<UserDTO> getAllUserDetailsByOrganisationIdAndUserEmail(String organisationId, List<String> emails, HttpServletRequest httpServletRequest) {

        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        ResponseDTO responseDTO = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/organisation/" + organisationId + "/getUserDetailsByOrganisationIdAndUserEmails",
                null,
                headers,
                emails,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null).getBody();

        if (responseDTO.getData() != null) {
            List<UserDTO> userDTOS = JSON.parseArray(JSON.toJSONString(responseDTO.getData()), UserDTO.class);
            return userDTOS;
        } else {
            return null;
        }
    }


    public List<UserDTO> getAllUserDetailsByOrganisationId(String orgId, HttpServletRequest httpServletRequest) {

        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        ResponseDTO responseDTO = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/organisation/" + orgId + "/getUserDetailsByOrganisationId",
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null).getBody();

        if (responseDTO.getData() != null) {
            List<UserDTO> userDTOS = JSON.parseArray(JSON.toJSONString(responseDTO.getData()), UserDTO.class);
            return userDTOS;
        } else {
            return null;
        }
    }

    public String getAlertSchedule(String email, JSONObject dateTimeData, HttpServletRequest httpServletRequest) {

        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseDTO responseDTO = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/organisation/user/" + email + "/getAlertSchedule",
                null,
                headers,
                dateTimeData,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null).getBody();
        log.info("Fetching AlertSchedule By email :{},EndPoint:{}", email, httpServletRequest.getRequestURI());
        if (responseDTO.getData() != null) {
            return responseDTO.getData().toString();
        } else {
            return null;
        }
    }

    public String getLanguageByEmail(String email, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/organisation/user/" + email + "/getLanguageByEmail",
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);

        return response.getBody();

    }


    public List<CustomerOrganisationDto> getAllOrganisationDetailsFromVendorServer(HttpServletRequest httpServletRequest) {

        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                VENDOR_SERVER_URL + "/api/migrateData/getAllOrganisationDetails",
                null,
                null,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                0,
                0,
                0);
//        ResponseDTO responseDTO = response.getBody();
        System.out.println("vendor ===>" + response.getBody().getData());

        String org = JSON.toJSONString(response.getBody().getData());
        List<CustomerOrganisationDto> vendorOrg = JSON.parseArray(org, CustomerOrganisationDto.class);
        System.out.println("json ===>" + JSON.toJSONString(vendorOrg));
        return vendorOrg;
    }

    public List<UserDTO> getAllVendorUserDetailsByOrgId(List<String> orgIds) {

        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                VENDOR_SERVER_URL + "/api/migrateData/getAllVendorUserDetailsByOrgId",
                null,
                null,
                orgIds,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                0);
//        ResponseDTO responseDTO = response.getBody();
        System.out.println("vendor user ===>" + response.getBody().getData());

        String users = JSON.toJSONString(response.getBody().getData());
        List<UserDTO> userDTOS = JSON.parseArray(users, UserDTO.class);
        System.out.println("json ===>" + JSON.toJSONString(userDTOS));
        return userDTOS;
    }

    public TenantDTO getAllTenants(String issuer) {
        log.info("issuer:{}", issuer);

        Map<String, String> params = new HashMap<>();
        params.put("issuer", issuer);

        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/getTenantByIssuer",
                params,
                null,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                20000,
                20000,
                10000);

        ResponseDTO responseBody = response.getBody();
        if (responseBody != null && responseBody.getData() != null) {
            String jsonString = JSON.toJSONString(responseBody.getData());
            return JSON.parseObject(jsonString, TenantDTO.class);
        } else {
            return new TenantDTO(); // Return an empty TenantDTO
        }
    }

    public ResponseEntity<ResponseDTO> getMeasuringInstrumentsByTypes(String email, List<String> measuringInstrumentsTypes, List<String> groupedMeasuringInstrumentIds, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = this.constructParams("measuringInstrumentsTypes", measuringInstrumentsTypes, httpServletRequest);
        Map<String, String> groupedMeasuringInstrumentIdsParams = this.constructParams("groupedMeasuringInstrumentIds", groupedMeasuringInstrumentIds, httpServletRequest);
        params.putAll(groupedMeasuringInstrumentIdsParams);
        params.put("loggedInUser", loggedInUser);
        log.info("Params: {}", params);
        return scleraWebClient.httpRequest(HttpMethod.GET,
                STORES_SERVER_URL + "/api/user/" + email + "/getMeasuringInstrumentDetails",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
    }

    public ResponseEntity<byte[]> getImageByRedirectUrl(String url, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseEntity<byte[]> response = scleraWebClient.httpRequest(HttpMethod.GET,
                url,
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                byte[].class,
                null,
                null,
                0,
                0,
                0);
        return response;
    }


    public JSONObject sendIpApi(String ip) {
        Map<String, String> params = new HashMap<>();
        params.put("key", ipApiKey);
        params.put("fields", "timezone");
        JSONObject response = scleraWebClient.httpRequest(HttpMethod.GET,
                "http://pro.ip-api.com/json/" + ip,
                params,
                null,
                null,
                MediaType.APPLICATION_JSON,
                JSONObject.class,
                null,
                null,
                20000,
                20000,
                10000).getBody();
        return response;
    }

    public JSONObject getAllUserCount(String email, String key, String loggedInUser, List<String> userProfiles, String orgId,
                                      String active, boolean withoutProfile, HttpServletRequest httpServletRequest) {
        log.info("Payload:Key:{},org_id:{},userProfiles:{}", key, orgId, userProfiles);
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("org_id", String.valueOf(orgId));
        params.put("userProfiles", JSON.toJSONString(userProfiles));
        params.put("active", active);
        params.put("withoutProfile", String.valueOf(withoutProfile));
        params.put("loggedInUser", loggedInUser);
        log.info("params:{}", params);
        JSONObject response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/user/" + email + "/getAllUserCount",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                JSONObject.class,
                null,
                null,
                null,
                null,
                null).getBody();
        return response;
    }

    public List<UserDTO> getUserListByPropertyAdmin(String userEmail, List<String> visibleEmails, String key, String customerOrgId, int limit,
                                                    int offset, List<String> userProfiles, String active, HttpServletRequest httpServletRequest) {
        log.info("Payload:userEmail:{},visibleEmails:{},key:{}customerOrgId:{},limit:{},offset:{},userProfiles:{},active:{}", userEmail, visibleEmails, key, customerOrgId, limit, offset, userProfiles, active);

        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        params.put("limit", String.valueOf(limit));
        params.put("offset", String.valueOf(offset));
        params.put("org_id", String.valueOf(customerOrgId));
        params.put("userProfiles", JSON.toJSONString(userProfiles));
        params.put("active", active);
        log.info("params:{}", params);
        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/user/" + userEmail + "/getUserDetailsByPropertyAdminEmail",
                params,
                headers,
                visibleEmails,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        JSONObject jsonObject = JSON.parseObject(response.getBody());
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        return JSON.parseArray(jsonArray.toString(), UserDTO.class);

    }

    public ResponseEntity<JSONObject> getRegionCoordinatesByFloorMap(JSONObject runPodPayload, String runPodAPIKey, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructRunPodToken(runPodAPIKey);

        log.info("API CALL URL : {}", RUNPOD_SERVER_URL);
        log.info("API CALL BODY : {}", runPodPayload);

        return scleraWebClient.httpRequest(HttpMethod.POST,
                RUNPOD_SERVER_URL + "/runsync",
                null,
                headers,
                runPodPayload,
                MediaType.APPLICATION_JSON,
                JSONObject.class,
                null,
                null,
                0,
                0,
                0
        );
    }

    public ResponseEntity<JSONObject> checkRunPodRequestStatus(String runPodAPIKey, String requestId, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructRunPodToken(runPodAPIKey);
        log.info("API CALL URL : {}  , REQUEST-ID : {}", RUNPOD_SERVER_URL, requestId);

        return scleraWebClient.httpRequest(HttpMethod.GET,
                RUNPOD_SERVER_URL + "/status/" + requestId,
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                JSONObject.class,
                null,
                null,
                0,
                0,
                0
        );
    }

    public String getVdmsClientIdByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("VDMS Id :{}", vdmsId);
        Map<String, String> headers = this.constructToken(httpServletRequest);

        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/" + vdmsId + "/getVdmsClientIdByVdmsId",
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        JSONObject jsonObject = JSON.parseObject(response.getBody());
        return jsonObject.get("data").toString();
    }

    public ResponseDTO deleteVdmsClientInBffServer(String vdmsId, String loggedInUser, HttpServletRequest httpServletRequest) {

        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        ResponseEntity<String> res = scleraWebClient.httpRequest(HttpMethod.DELETE,
                BFF_SERVER_URL + "/api/vdms/" + vdmsId + "/deleteVdmsClientVdmsId",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);

        return JSON.parseObject(res.getBody(), ResponseDTO.class);
    }

    public AlertScheduleDTO getEmailAndSmsMuteByEmail(String email, HttpServletRequest httpServletRequest) {

        Map<String, String> headers = this.constructToken(httpServletRequest);
        ResponseDTO responseDTO = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/organisation/user/" + email + "/getEmailAndSmsMuteByEmail",
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null).getBody();
        log.info("Fetching AlertSchedule By email :{},EndPoint:{}", email, httpServletRequest.getRequestURI());
        if (responseDTO.getData() != null) {
            return JSON.parseObject(JSON.toJSONString(responseDTO.getData()), AlertScheduleDTO.class);
        } else {
            return null;
        }
    }

    public String getDiskKeyByUniqueID(String vdmsId, String uniqueId, HttpServletRequest httpServletRequest) {
        log.info("Getting Keys By vdms-id : {} , uniqueId : {}", vdmsId, uniqueId);
        Map<String, String> headers = new HashMap<>();
        headers.put("x-real-ip", httpServletRequest.getHeader("x-real-ip"));

        Map<String, String> params = new HashMap<>();
        params.put("uniqueId", uniqueId);

        ResponseEntity<String> responseDTO = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/" + vdmsId + "/getDiskKeyByUniqueID",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Response from BFF server for vdms-id : {} ,uniqueID : {} , response : {}", vdmsId, uniqueId, responseDTO);
        return responseDTO.getBody();
    }

    public String getMySQLKeyByUniqueID(String vdmsId, String uniqueId, HttpServletRequest httpServletRequest) {
        log.info("Getting Keys By vdms-id : {} , uniqueId : {}", vdmsId, uniqueId);
        Map<String, String> headers = new HashMap<>();
        headers.put("x-real-ip", httpServletRequest.getHeader("x-real-ip"));

        Map<String, String> params = new HashMap<>();
        params.put("uniqueId", uniqueId);

        ResponseEntity<String> responseDTO = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/" + vdmsId + "/getMySQLKeyByUniqueID",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Response from BFF server for vdms-id : {} ,uniqueID : {} , response : {}", vdmsId, uniqueId, responseDTO);
        return responseDTO.getBody();
    }

    public String getSQLiteKeyByUniqueID(String vdmsId, String uniqueId, HttpServletRequest httpServletRequest) {
        log.info("Getting Keys By vdms-id : {} , uniqueId : {}", vdmsId, uniqueId);
        Map<String, String> headers = new HashMap<>();
        headers.put("x-real-ip", httpServletRequest.getHeader("x-real-ip"));

        Map<String, String> params = new HashMap<>();
        params.put("uniqueId", uniqueId);

        ResponseEntity<String> responseDTO = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/" + vdmsId + "/getSQLiteKeyByUniqueID",
                params,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);
        log.info("Response from BFF server for vdms-id : {} ,uniqueID : {} , response : {}", vdmsId, uniqueId, responseDTO);
        return responseDTO.getBody();
    }

    public ResponseDTO migrateCreationTimeStamp(List<UserDTO> userDTOS, HttpServletRequest httpServletRequest) {

        Map<String, String> headers = this.constructToken(httpServletRequest);
        String JSONString = JSON.toJSONString(userDTOS);

        ResponseEntity<String> response = scleraWebClient.httpRequest2(HttpMethod.PUT,
                BFF_SERVER_URL + "/migrateTimeStamp",
                null,
                headers,
                JSONString,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                50000,
                50000,
                50000);
        log.info("response" + response);
        return JSON.parseObject(response.getBody(), ResponseDTO.class);
    }

    public List<OrganisationDTO> getOrganisationNameAndIconUrlByOrgIds(List<String> orgIds, String loggedInUser, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        Map<String, String> params = new HashMap<>();
        params.put("loggedInUser", loggedInUser);
        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.POST,
                BFF_SERVER_URL + "/api/getOrganisationNameAndIconUrlByOrgIds",
                params,
                headers,
                orgIds,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                0);

        ResponseDTO responseDTO = response.getBody();

        if (responseDTO == null || responseDTO.getData() == null) {
            return Collections.emptyList();
        }
        String jsonString = JSON.toJSONString(responseDTO.getData());
        return JSON.parseArray(jsonString, OrganisationDTO.class);
    }


    public ResponseEntity<ResponseDTO> addAgentPermissions(String vdmsId, String data, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);
        return scleraWebClient.httpRequest(HttpMethod.PUT,
                BFF_SERVER_URL + "/api/vdms/" + vdmsId + "/updateAgentPermissions",
                null,
                headers,
                data,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);
    }

    public String getAgentPermissionsByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);

        ResponseEntity<String> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/" + vdmsId + "/getAgentPermissionsByVdmsId",
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                String.class,
                null,
                null,
                null,
                null,
                null);

        JSONObject jsonObject = JSON.parseObject(response.getBody());
        return Optional.ofNullable(jsonObject)
                .map(obj -> obj.getString("data"))
                .orElse(null);
    }

    public Integer checkAppCredentialsExistOrNot(String orgId, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);

        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/organisation/" + orgId + "/checkAppCredentialsExistOrNot",
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);

        ResponseDTO responseDTO = response.getBody();
        if (responseDTO != null && responseDTO.getData() != null) {
            return Integer.valueOf(responseDTO.getData().toString());
        } else {
            return null;
        }
    }


    public Integer getUserRemoteDesktopAuthByUserId(String email, HttpServletRequest httpServletRequest) {
        Map<String, String> headers = this.constructToken(httpServletRequest);

        ResponseEntity<ResponseDTO> response = scleraWebClient.httpRequest(HttpMethod.GET,
                BFF_SERVER_URL + "/api/vdms/organisation/user/" + email + "/getUserRemoteDesktopAuth",
                null,
                headers,
                null,
                MediaType.APPLICATION_JSON,
                ResponseDTO.class,
                null,
                null,
                null,
                null,
                null);

        ResponseDTO responseDTO = response.getBody();
        if (responseDTO != null && responseDTO.getData() != null) {
            return Integer.valueOf(responseDTO.getData().toString());
        } else {
            return null;
        }
    }

    public String sendMultiTenantData(JSONObject jsonObject, String vdmsId, String awsRegion, HttpServletRequest httpServletRequest) {

        log.info("Multitenancy Payload: VdmsId: {}", vdmsId);
        String serverUrl = getMultiTenancyServerUrlByRegion(awsRegion);
        log.info("ServerUrl:" + serverUrl);
        try {
            userActionLogService.addUserActionLog(vdmsId, "MultiTenancy", "ADD", "ServerUrl:" + serverUrl + "/tenants/" + vdmsId + "/vdms/activate", "success");
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Tenant-ID", vdmsId);

            ResponseEntity<String> response = scleraWebClient.httpRequest(
                    HttpMethod.POST,
                    serverUrl + "/tenants/" + vdmsId + "/vdms/activate",
                    null,
                    headers,
                    jsonObject,
                    MediaType.APPLICATION_JSON,
                    String.class,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            log.info("Multitenancy Response Body: {}", response.getBody());
            return response.getBody();
        } catch (Exception ex) {
            userActionLogService.addUserActionLog(vdmsId, "MultiTenancy", "ADD",
                    "Error while sending multitenancy data for vdmsId:" + vdmsId + " message: " + ex.getMessage(), "failes");
            log.error("Error while sending multitenancy data for vdmsId: {}", vdmsId, ex);
            return null;
        }
    }

//    public void sendMultiTenantData(JSONObject jsonObject, String vdmsId, String awsRegion, HttpServletRequest httpServletRequest) {
//        log.info("Multitenancy Payload:VdmsId:{}", vdmsId);
//        String serverUrl = getMultiTenancyServerUrlByRegion(awsRegion);
//        log.info("ServerUrl:" + serverUrl);
//        userActionLogService.addUserActionLog(vdmsId, "MultiTenancy", "ADD", "ServerUrl:" + serverUrl + "/tenants/" + vdmsId + "/vdms/activate", "success");
//        Map<String, String> headers = new HashMap<>();
//        headers.put("X-Tenant-ID", vdmsId);
//        scleraWebClient.httpRequest(
//                HttpMethod.POST,
//                serverUrl + "/tenants/" + vdmsId + "/vdms/activate",
//                null,
//                headers,
//                jsonObject,
//                MediaType.APPLICATION_JSON,
//                String.class,
//                null,
//                null,
//                null,
//                null,
//                null);
//    }

    public void multiTenantSyncApiCall(String vdmsId, VdmsSyncDTO vdmsSyncDTO, String awsRegion, HttpServletRequest httpServletRequest) {
        log.info("Multitenancy Sync Payload:VdmsId:{}", vdmsId);
        String serverUrl = getMultiTenancyServerUrlByRegion(awsRegion);
        log.info("ServerUrl:" + serverUrl);
        userActionLogService.addUserActionLog(vdmsId, "MultiTenancy", "ADD", "ServerUrl:" + serverUrl + "/cloud/vdms/" + vdmsId + "/sync", "success");
        Map<String, String> headers;
        headers = this.constructToken(httpServletRequest);
        headers.put("X-Tenant-ID", vdmsId);
        Map<String, String> params = new HashMap<>();
        params.put("vdms_id", vdmsId);
        scleraWebClient.httpRequest(HttpMethod.POST,
                serverUrl + "/cloud/vdms/" + vdmsId + "/sync",
                params,
                headers,
                vdmsSyncDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                50000,
                50000,
                50000);
    }

    public void multiTenantSyncApiCall(String vdmsId, JSONObject jsonObject, String awsRegion, HttpServletRequest httpServletRequest) {
        String serverUrl = getMultiTenancyServerUrlByRegion(awsRegion);
        log.info("ServerUrl:" + serverUrl);
        userActionLogService.addUserActionLog(vdmsId, "MultiTenancy", "ADD", "ServerUrl:" + serverUrl + "/cloud/vdms/" + vdmsId + "/sync", "success");
        Map<String, String> headers;
        headers = this.constructToken(httpServletRequest);
        headers.put("X-Tenant-ID", vdmsId);
        Map<String, String> params = new HashMap<>();
        params.put("vdms_id", vdmsId);
        scleraWebClient.httpRequest(HttpMethod.POST,
                serverUrl + "/cloud/vdms/" + vdmsId + "/sync",
                params,
                headers,
                jsonObject,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                50000,
                50000,
                50000);
    }


    public void multiTenantSyncApiCall(String vdmsId, VdmsSyncDTO vdmsSyncDTO, String awsRegion, String token) {
        log.info("Multitenancy Sync Payload:VdmsId:{}", vdmsId);
        String serverUrl = getMultiTenancyServerUrlByRegion(awsRegion);
        log.info("ServerUrl:" + serverUrl);
        userActionLogService.addUserActionLog(vdmsId, "MultiTenancy", "ADD", "ServerUrl:" + serverUrl + "/cloud/vdms/" + vdmsId + "/sync", "success");
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", token);
        headers.put("X-Tenant-ID", vdmsId);
        Map<String, String> params = new HashMap<>();
        params.put("vdms_id", vdmsId);
        scleraWebClient.httpRequest(HttpMethod.POST,
                serverUrl + "/cloud/vdms/" + vdmsId + "/sync",
                params,
                headers,
                vdmsSyncDTO,
                MediaType.APPLICATION_JSON,
                Void.class,
                null,
                null,
                50000,
                50000,
                50000);
    }

    public String getMultiTenancyServerUrlByRegion(String awsRegion) {
        log.info("Payload:Region:" + awsRegion);
        if ("us-east-1".equalsIgnoreCase(awsRegion)) {
            return MULTITENANCY_US_SERVER_URL;
        } else if ("eu-west-2".equalsIgnoreCase(awsRegion)) {
            return MULTITENANCY_LONDON_SERVER_URL;
        }
        return null;
    }
}