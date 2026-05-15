//package io.sclera.service;
//
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import io.sclera.Repository.ManagedSoftwareRepository;
//import io.sclera.Repository.SystemInterfaceRepository;
//import io.sclera.Repository.VendorOrganisationRepository;
//import io.sclera.dto.*;
//import io.sclera.dto.touchscreen.settings.InterfaceDTO;
//import io.sclera.dto.touchscreen.settings.UserDTO;
//import io.sclera.enums.SyncType;
//import io.sclera.models.ManagedSoftware;
//import io.sclera.proxy.ProxyService;
//import io.sclera.service.touchscreen.CustomerOrganisationService;
//import io.sclera.service.touchscreen.SettingsService;
//import io.sclera.service.touchscreen.VdmsService;
//import io.sclera.utils.CorrigoUserUtils;
//import io.sclera.utils.UserRoleUtils;
//import io.sclera.utils.Utils;
//import io.sclera.websocket.client.WebSocketClient_Application;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//
//@Service
//public class EssentialService {
//    private static final Logger log = LoggerFactory.getLogger(EssentialService.class);
//
//    private final AtomicInteger qrcodeFlag = new AtomicInteger(0);
//    private final AtomicInteger nfcFlag = new AtomicInteger(0);
//    private final AtomicInteger barcodeFlag = new AtomicInteger(0);
//    @Autowired
//    DeviceService deviceService;
//
//    @Autowired
//    APICallService apiCallService;
//
//    @Autowired
//    Utils utils;
//
//    @Autowired
//    DockerService dockerService;
//
//    @Autowired
//    VendorAdminService vendorAdminService;
//
//    @Autowired
//    UserService userService;
//
//    @Autowired
//    BuildingService buildingService;
//
//
//    @Autowired
//    VendorOrganisationRepository vendorOrganisationRepository;
//
//
//    @Autowired
//    WebSocketClient_Application webSocketClient_Application;
//
//    @Autowired
//    SettingsService settingsService;
//
//    @Autowired
//    SystemInterfaceService systemInterfaceService;
//
//    @Autowired
//    ProxyService proxyService;
//
//    @Autowired
//    VdmsService vdmsService;
//
//    @Autowired
//    PropertyQrcodeService propertyQrcodeService;
//
//    @Autowired
//    GlobalInspectionRecordService globalInspectionRecordService;
//
//    @Autowired
//    RecordChecklistService recordChecklistService;
//
//    @Autowired
//    InspectionRecordService inspectionRecordService;
//
//    @Autowired
//    CustomerOrganisationService customerOrganisationService;
//
//    @Autowired
//    IntegrationService integrationService;
//
//    @Autowired
//    UserRoleUtils userRoleUtils;
//
//    @Autowired
//    CorrigoService corrigoService;
//
//    @Autowired
//    CorrigoUserUtils corrigoUserUtils;
//
//    @Autowired
//    QrCodeService qrCodeService;
//    @Autowired
//    ClientQrCodeService clientQrCodeService;
//
//    @Autowired
//    NfcService nfcService;
//
//    @Autowired
//    ClientNfcService clientNfcService;
//
//    @Autowired
//    ClientBarCodeService clientBarCodeService;
//
//    @Autowired
//    TechnicianService technicianService;
//
//    @Autowired
//    TechnicianSkillService technicianSkillService;
//
//    @Autowired
//    TechnicianCertificateService technicianCertificateService;
//
//    @Autowired
//    TechnicianAvailabilityService technicianAvailabilityService;
//
//    @Autowired
//    DeviceTechnicianAISuggestionService deviceTechnicianAISuggestionService;
//
//    AtomicBoolean runMainThread = new AtomicBoolean(false);
//
//    @Autowired
//    TicketService ticketService;
//
//    @Autowired
//    InventoryDeviceService inventoryDeviceService;
//
//    @Autowired
//    ApplicationUserService applicationUserService;
//
//    @Autowired
//    ManagedSoftwareService managedSoftwareService;
//
//    @Autowired
//    ManagedSoftwareRepository managedSoftwareRepository;
//
//    @Autowired
//    SystemInterfaceRepository systemInterfaceRepository;
//
//
//    public void runMainThread(String vdmsId) {
//        log.info("runMain {}", runMainThread.get());
//        if (!runMainThread.get()) {
//            log.info("Inside {}", runMainThread);
//            Thread thread = new Thread() {
//                public void run() {
//                    runMainThread.set(true);
//                    while (true) {
//                        log.info("START VDMS UDPDATE API");
//
//                        try {
//                            // API call made to cloud every 5 mins to sync the VDMS status
//                            updateVdmsStatus(vdmsId);
//                            log.info("END VDMS UPDATE API");
//                        } catch (Exception e) {
//                            // TODO Auto-generated catch block
//                            log.error("Error in runMainThread method while updating vdms status", e);
//                        }
//
//                        try {
//                            proxyService.verifyAndRestartProxyClient(vdmsId);
//                        } catch (Exception e) {
//                            log.error("Unable to connect proxy...", e);
//                        }
//
//                        try {
//                            Thread.sleep(300000);
////							Thread.sleep(3000);
//                        } catch (Exception e) {
//                            // TODO: handle exception
//                        }
//                        try {
//                            Thread thread = new Thread() {
//
//                                public void run() {
//                                    webSocketClient_Application.connectP2PSocket();
//                                    log.info("P2P starteddddd");
//
//                                    webSocketClient_Application.connectIntegrationSocket();
//                                    log.info("Integration starteddddd");
//
//                                }
//                            };
//                            thread.start();
//                        } catch (Exception e) {
//                            // TODO: handle exception
//                        }
//                    }
//                }
//            };
//
//            thread.start();
//        }
//    }
//
//
////
////	public void updateVdmsDetailCloud(String vdms_id)
////	{
////
////		String uuid[] = {"bash", "-c", "ifconfig | grep 'ether' | sed 's/://g'| sha256sum | awk {'print $1'}"};
//////		String uuid[] = {"bash", "-c", "echo $(sudo dmidecode -t 4 | grep ID | sed 's/.*ID://;s/ //g') \\ $(ifconfig | grep -oP 'HWaddr \\K.*' | sed 's/://g') | sha256sum | awk '{print $1}'\n"
//////				+ ""};
////		String uuidResult = utils.execPipedCmd(uuid).get("output");
////		System.out.println("UUID " + uuidResult);
////		VdmsSyncDTO vdmsSync = new VdmsSyncDTO();
////		vdmsSync.setDevuid(uuidResult);
////
////		apiCallService.updateVdmsDetailCloud(vdms_id, vdmsSync);
////
////
////	}
//
//    // Update Docker Network Origin from NULL to 1 (if any) on startup
//    public void updateDockerNetworkOrigin(String vdms_id) {
//        log.info("Updating docker network origin from NULL to 1 (if any)...");
//        dockerService.updateDockerNetworkOrigin(vdms_id);
//    }
//
//    public void updatePropertyDetails(String vdms_id) {
//        PropertyAddressDTO propertyAddress = apiCallService.updatePropertyDetails(vdms_id);
//        log.info("{} Details", propertyAddress);
//        vdmsService.updatePropertyDetails(propertyAddress);
//    }
//
//
//    public void updateVdmsDetailCloud(String vdms_id) {
//        String uuidResult = utils.readUUID();
//        log.info("UUID : {}", uuidResult);
//        VdmsSyncDTO vdmsSync = new VdmsSyncDTO();
//        vdmsSync.setDevuid(uuidResult);
//        apiCallService.updateVdmsDetailCloud(vdms_id, vdmsSync);
//    }
//
//    public void syncDeviceData() {
//        try {
//            List<DeviceDTO> deviceList = deviceService.listAlldevices();
//
//            log.info("DeviceList count : {}", deviceList.size());
//
//            for (DeviceDTO device : deviceList) {
//                Thread.sleep(2000);
//                try {
//                    //						System.out.println(device.toString());
//                    String vendor = null;
//                    String display_name = null;
//
//                    if (device.getVendor() == null || device.getVendor().equals("")) {
//                        vendor = apiCallService.getVendorByMacAddress(device.getMac_address());
//                        if (vendor != null) {
//                            deviceService.updateDeviceVendorById(device.getId(), vendor);
//                        }
//                    }
//
////					if(device.getModel() == null || device.getModel().isEmpty()) {
//                    if (vendor != null) {
//                        this.getProductDetailsByScript(device.getId(), device.getDocker_name(), device.getIp_address(), vendor);
//                    } else {
//                        this.getProductDetailsByScript(device.getId(), device.getDocker_name(), device.getIp_address(), device.getVendor());
//                    }
////					}
//
//                    if (device.getDisplay_name() == null || device.getDisplay_name().equals("") || device.getDisplay_name().equals("Generic") || device.getDisplay_name().equals("runtime")) {
//                        display_name = getDeviceDetails(device.getMac_address(), device.getIp_address(), device.getDocker_name()).replace(".local", "");
//                        deviceService.updateDevicesDisplayNameById(device.getId(), display_name);
//                    }
//
//                    //						System.out.println("vendor: " + vendor);
//                    //						System.out.println("display_name: " + display_name);
//
//                } catch (Exception e) {
//                    log.error("Error in syncDeviceData method while syncing device data", e);
//                }
//            }
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//    }
//
//
//    public String getDeviceDetails(String mac_address, String ip_address, String docker_name) {
//        String[] avahiCmd = {"bash", "-c", "docker exec " + docker_name + " avahi-resolve -a " + ip_address + " | awk {'print $2'} "};
//        //		String[] avahiCmd = {"bash", "-c", "avahi-resolve -a " + ip_address + " | awk {'print $2'} "};
//        String avahiResult = utils.execPipedCmd(avahiCmd).get("output");
//
//        if (!(avahiResult.equals(""))) {
//            return avahiResult;
//        } else {
//            String[] nmbCmd = {"bash", "-c", "docker exec " + docker_name + " nmblookup -A " + ip_address + " | grep '<00' | grep -v GROUP | awk {'print $1'}"};
//            //			String[] nmbCmd = {"bash", "-c", " nmblookup -A " + ip_address + " | head -n 2 | tail -n 1 | awk {'print $1'}"};
//            String nmbResult = utils.execPipedCmd(nmbCmd).get("output");
//            if (!nmbResult.equals("") && !nmbResult.equals("No")) {
//                return nmbResult;
//            } else {
//                return "Generic";
//            }
//        }
//    }
//
//    public void syncData(String vdms_id, String docker_name, String attribute) {
//        String internal_Ip_address = dockerService.getInternalIPbyDockername(vdms_id, docker_name);
//
//        switch (attribute) {
//            case "all":
//                apiCallService.syncAllAttribute(internal_Ip_address);
//                break;
//            case "bacnet":
//                apiCallService.syncBacnet(internal_Ip_address);
//                break;
//            case "snmpwalk":
//                apiCallService.syncSnmpWalk(internal_Ip_address);
//                break;
//            case "snmpinterface":
//                apiCallService.snmpInterface(internal_Ip_address);
//                break;
//            case "snmptopology":
//                apiCallService.snmpTopology(internal_Ip_address);
//                break;
//            case "connectivity":
//                apiCallService.internetConnectivity(internal_Ip_address);
//                break;
//            default:
//                break;
//        }
//
//        try {
//            this.syncDeviceData();
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//
//        try {
//
//            deviceService.updateAllVirtualDeviceStatus();
//        } catch (Exception e) {
//            // TODO: handle exception
//        }
//    }
//
//    public void syncUserData(String vdms_id) {
//        try {
//            log.info("Entered syncUserData method as user_sync = 1");
//            String customer_org_id = userService.getCustomerOrgIdByVdmsId(vdms_id);
//
//            List<UserDTO> users = apiCallService.getUsersByOrgId(customer_org_id, vdms_id);
//
//            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^" + users);
//
//            Set<String> users_db = userService.getAllUsersEmail();
//
//            Set<String> cloud_emails = new HashSet<>();
//
//            for (UserDTO user : users) {
//                try {
//                    log.info("Processing user sync for email: {}", user != null ? user.getEmail() : "null");
//                    if (utils.compareIds(users_db, user.getEmail())) {
//                        //update
//                        log.info("User exists in DB. Updating user: {}", user.getEmail());
//                        userRoleUtils.setRoles(user.getEmail(), user.getRole());
//                        this.checkCorrigoUserCredentials(user);
//                        userService.updateAllUser(user);
//
//                    } else {
//                        // insert
//                        log.info("User does not exist in DB. Inserting user: {}", user.getEmail());
//                        userRoleUtils.setRoles(user.getEmail(), user.getRole());
//                        this.checkCorrigoUserCredentials(user);
//                        userService.insertUsers(user);
//
//                    }
//                    cloud_emails.add(user.getEmail());
//                } catch (Exception e) {
//                    log.error("Error processing user with email for add/update of users: {}. Exception: {}", user != null ? user.getEmail() : "null", e);
//                }
//            }
//            // new changes
//            List<UserDTO> db_users = userService.getAllUsersByOrganisationId(customer_org_id);
//
//            for (UserDTO db_user : db_users) {
//                try {
//                    if (!utils.compareIds(cloud_emails, db_user.getEmail())) {
//                        // delete
//                        log.info("Deleting user not found in cloud: {}", db_user.getEmail());
//                        userRoleUtils.deleteRole(db_user.getEmail());
//                        log.info("Deleting Corrigo credentials for user: {}", db_user.getEmail());
//                        corrigoUserUtils.deleteCorrigoCredentials(db_user.getEmail());
//                        this.deleteUser(db_user);
//                    }
//                } catch (Exception e) {
//                    log.error("Error processing DB user with email for deletion of users: {}. Exception: {}", db_user != null ? db_user.getEmail() : "null", e);
//                }
//            }
//            log.info("User sync is successfully completed all the credentials are loaded into the hashmap");
//        } catch (Exception e) {
//            log.error("Error in syncUserData method, some issue with corrigo credentials being pushed into the hashmap", e);
//        }
//    }
//
//
//    // to be removed after testing
//    public List<UserDTO> testUserSync() {
//        try {
//            String vdms_id = vdmsService.getVDMSId();
//            String customer_org_id = userService.getCustomerOrgIdByVdmsId(vdms_id);
//
//            List<UserDTO> users = apiCallService.getUsersByOrgId(customer_org_id, vdms_id);
//
//            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^" + users);
//            return users;
//        } catch (Exception e) {
//            log.error("Error in testUserSync method", e);
//        }
//
//        return null;
//    }
//
//    public void checkCorrigoUserCredentials(UserDTO user) {
//        try {
//            log.info("Entered checkCorrigoUserCredentials for user: {}", user != null ? user.getEmail() : "null");
//            if (user.getCorrigoUserSettings() != null) {
//                CorrigoUserSettingsDTO credentials = user.getCorrigoUserSettings();
//                if (credentials.getCredential_type().equalsIgnoreCase("custom")) {
//                    log.info("Setting custom credentials for user: {}", user.getEmail());
//                    corrigoUserUtils.setCorrigoCredentials(user.getEmail(), credentials);
//
//                }
//                if (credentials.getCredential_type().equalsIgnoreCase("default")) {
//                    log.info("Setting default credentials for user: {}", user.getEmail());
//                    CorrigoUserSettingsDTO corrigoUserSettingsDTO = this.setCorrigoCredentials(user.getCorrigoUserSettings());
//                    corrigoUserUtils.setCorrigoCredentials(user.getEmail(), corrigoUserSettingsDTO);
//
//                }
//            } else {
//                log.info("Setting default credentials for user: {}", user.getEmail());
//                CorrigoUserSettingsDTO configurationDTO = new CorrigoUserSettingsDTO();
//                CorrigoUserSettingsDTO defaultCredentials = this.setCorrigoCredentials(configurationDTO);
//                defaultCredentials.setCredential_type("default");
//                corrigoUserUtils.setCorrigoCredentials(user.getEmail(), defaultCredentials);
//
//            }
//            log.info("Completed checkCorrigoUserCredentials for user: {}", user.getEmail());
//        } catch (Exception e) {
//            log.error("Error in checkCorrigoUserCredentials while setting details into hashmap", e);
//        }
//    }
//
//    public CorrigoUserSettingsDTO setCorrigoCredentials(CorrigoUserSettingsDTO corrigoUserSettingsDTO) {
//        CorrigoConfigurationDTO corrigoConfigurationDTO = corrigoService.getCorrigoConfigurationDetails();
//
//        if (corrigoConfigurationDTO != null) {
//            log.info("Corrigo configuration found. Setting client details");
//            corrigoUserSettingsDTO.setClient_id(corrigoConfigurationDTO.getClient_id());
//            corrigoUserSettingsDTO.setClient_secret(corrigoConfigurationDTO.getClient_secret());
//        } else {
//            log.error("Corrigo configuration is NULL while setting default credentials");
//        }
//        return corrigoUserSettingsDTO;
//
//    }
//
//
//    public void deleteUser(UserDTO user) {
//        try {
//            globalInspectionRecordService.updateGlobalInspectionRecord(user.getEmail());
//            inspectionRecordService.updateInspectionRecord(user.getEmail());
//            recordChecklistService.updateRecordChecklist(user.getEmail());
//            ticketService.updateTicketAssigneeByUserEmail(user.getEmail());
//            userService.deleteUserByEmailId(user.getEmail());
//        } catch (Exception e) {
//            System.out.println("Error while deleting user: " + e.getMessage());
//        }
//    }
//
//
//    public void syncVendorData(String vdms_id, String docker_name) {
//
//        String vendor_org_id = dockerService.getVendorOrgIdByNetworkName(docker_name);
//
//        List<VendorDTO> vendors = apiCallService.getAllVendorsByOrganisationId(vendor_org_id, vdms_id, docker_name);
//
//        if (vendors != null && vendors.size() > 0) {
//            vendorAdminService.deleteVendorsByOrganisationId(vendor_org_id);
//            log.info("vendors {}", vendors);
//            for (VendorDTO vendor : vendors) {
//                try {
//                    vendorAdminService.insertVendors(vendor);
//                } catch (Exception e) {
//                    // TODO: handle exception
//                }
//
//            }
//        }
//    }
//
//
//    public void syncVendorTransfer(String vdms_id, String docker_name) {
//        VendorTransferDTO vendorTransfer = apiCallService.getTransferVendor(vdms_id, docker_name);
//
//        String vendor_org_id = vendorTransfer.getVendor_organisation_id();
//
//        if (vendor_org_id != null) {
//            try {
//                vendorOrganisationRepository.addVendor(vendor_org_id, null);
//            } catch (Exception e) {
//                // TODO: handle exception
//            }
//
//            dockerService.updateVendorOrgIdbydocker(vendor_org_id, vdms_id, docker_name);
//        }
//
//
//        if (vendorTransfer.getVendors() != null && vendorTransfer.getVendors().size() > 0) {
//            for (VendorDTO vendor : vendorTransfer.getVendors()) {
//                try {
//                    vendorAdminService.insertVendors(vendor);
//                } catch (Exception e) {
//                    // TODO: handle exception
//                }
//            }
//        }
//    }
//
//    public void syncVdmsTransferData(String vdms_id) {
//
//        try {
//            String existing_customer_org_id = userService.getCustomerOrgIdByVdmsId(vdms_id);
//            String new_customer_org_id = apiCallService.getCustomerOrgIdByVdmsId(vdms_id);
//
//            log.info("new_customer_org_id {}", new_customer_org_id);
//
//            if (new_customer_org_id != null) {
//                customerOrganisationService.upsertCustomerByOrganisationIdSync(new_customer_org_id);
//                vdmsService.updateCustomerOrgIdByVdmsId(vdms_id, new_customer_org_id);
//                integrationService.updateCustomerOrgByIntegrationId(new_customer_org_id);
//                userService.updateCustomerOrgIdForUsers(existing_customer_org_id, new_customer_org_id);
//
//                this.syncUserData(vdms_id);
//
//                if (userService.getCustomerOrgIdByVdmsId(vdms_id).equals(new_customer_org_id)) {
//                    customerOrganisationService.deleteCustomerOrgById(existing_customer_org_id);
//                    apiCallService.updateVdmsTranfer(vdms_id);
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//
////	public void syncLocations(String vdms_id) {
////
////
////		List<BuildingDTO> Buildings = apiCallService.getAllLocations(vdms_id);
////
////		if(Buildings != null && Buildings.size() >0)
////
////		{
////			for( BuildingDTO building : Buildings)
////			{
////				buildingService.upsertBuildingByVdmsId(building, vdms_id);
////			}
////		}
////
////	}
//
//
//    public void updateVdmsStatusApi(String vdmsId, VdmsSyncDTO VdmsSync) {
//        log.info("VdmsSyncDTO: {}", VdmsSync);
//        try{
//            if(VdmsSync.getCorrigo_sync() != null && VdmsSync.getCorrigo_sync() == 2){
//                log.info("!!! <---------------- Migration Initiated ------------------> !!!");
//                corrigoService.updateCorrigoCredentialsMigration(vdmsId);
//            }
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//        try {
//            Integer corrigoSync = VdmsSync.getCorrigo_sync();
//            Integer userSync = VdmsSync.getUser_sync();
//
//            if (corrigoSync != null && corrigoSync == 1) {
//                if (userSync == null) {
//                    log.info("!!! Initiating Corrigo URL SYNC !!!");
//                    corrigoService.corrigoUrlSync(null, vdmsId);
//                } else if (userSync == 1) {
//                    log.info("!!! Initiating Credential SYNC !!!");
//                    corrigoService.updateCorrigoCredentialsFromCloud(vdmsId);
//                }
//            }
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//
//        try {
//            if (VdmsSync.getUser_sync() != null && VdmsSync.getUser_sync() == 1) {
//                this.syncUserData(vdmsId);
//            }
//        } catch (Exception e) {
//            log.error("Error in updateVdmsStatusApi method while syncing user data", e);
//        }
//
//        // Skill Profiles start
//        try {
//            if (VdmsSync.getSkill_profiles_sync() != null && VdmsSync.getSkill_profiles_sync() == 1) {
//                this.syncSkillProfiles(vdmsId);
//            }
//        } catch (Exception e) {
//            log.error("Error in syncSkillProfiles method", e);
//        }
//        // Skill Profiles end
//
//        // Managed Software start
//        try {
//            if (VdmsSync.getManaged_software_user_sync() != null && VdmsSync.getManaged_software_user_sync() == 1) {
//                this.syncManagedSoftwareUsersFromInventory(vdmsId);
//            }
//        } catch (Exception e) {
//            log.error("Error in syncManagedSoftwareUsersFromInventory method", e);
//        }
//
//        try {
//            if (VdmsSync.getManaged_software_sync() != null && VdmsSync.getManaged_software_sync() == 1) {
//                this.syncManagedSoftwaresFromInventory(vdmsId);
//            }
//        } catch (Exception e) {
//            log.error("Error in syncManagedSoftwaresFromInventory method", e);
//        }
//        // Managed Software end
//
//        this.syncQrCodeNfcBarCode(vdmsId, VdmsSync, "on sync");
//
//        if (VdmsSync.getDockers() != null) {
//
//            for (dockerSyncDTO docker : VdmsSync.getDockers()) {
//                try {
//                    if (docker.getVendor_sync() != null && docker.getVendor_sync() == 1) {
//                        this.syncVendorData(vdmsId, docker.getName());
//                    }
//                } catch (Exception e) {
//                    log.error("Error in updateVdmsStatusApi method while syncing vendor data", e);
//                }
//
//                try {
//                    if (docker.getVendor_transfer() != null && docker.getVendor_transfer() == 1) {
//                        this.syncVendorTransfer(vdmsId, docker.getName());
//                    }
//                } catch (Exception e) {
//                    log.error("Error in updateVdmsStatusApi method while syncing vendor transfer data", e);
//                }
//            }
//        }
//
//        try {
//            if (VdmsSync.getVdms_transfer() != null && VdmsSync.getVdms_transfer() == 1) {
//                this.syncVdmsTransferData(vdmsId);
//            }
//        } catch (Exception e) {
//            log.error("Error in updateVdmsStatusApi method while syncing vdms transfer data", e);
//        }
//
//
////	if(VdmsSync.getImage_sync() != null &&  VdmsSync.getImage_sync() == 1)
////	{
////		this.syncLocations(vdmsId);
////	}
//
//        try {
//            if (VdmsSync.getProxy_server_host_sync() != null && VdmsSync.getProxy_server_host_sync() == 1) {
//                this.syncProxyServer(vdmsId);
//            }
//        } catch (Exception e) {
//            log.error("Error in updateVdmsStatusApi method while syncing proxy server data", e);
//        }
//
//        try {
//            if (VdmsSync.getProxy_client_sync() != null && VdmsSync.getProxy_client_sync() == 1) {
//                this.syncProxyClient(vdmsId);
//            }
//        } catch (Exception e) {
//            log.error("Error in updateVdmsStatusApi method while syncing proxy client data", e);
//        }
//
//        /*************** Service value sync removed ********************/
////        try {
////            if (VdmsSync.getService_value_sync() != null && VdmsSync.getService_value_sync() == 1) {
////                this.syncServiceValue(vdmsId);
////            }
////        } catch (Exception e) {
////            System.out.println(e);
////        }
//
//
//        try {
//            if (VdmsSync.getInventory_device_sync() != null) {
//                log.info("Inventory Device Sync...........");
//                this.syncInventoryDevice(vdmsId, VdmsSync.getInventory_device_sync());
//            }
//        } catch (Exception e) {
//            log.error("Error in Inventory Device Sync ", e);
//        }
//
//        try {
//            if (VdmsSync.getSclera_agent_permission_sync() != null && VdmsSync.getSclera_agent_permission_sync() == 1) {
//                this.syncScleraAgentPermission(vdmsId);
//            }
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//
//    }
//
//    private void syncServiceValue(String vdmsId) {
//        propertyQrcodeService.syncServiceValue(vdmsId);
//    }
//
//    //Update Vdms Status
//    public void updateVdmsStatus(String vdmsId) {
//
//        VdmsSyncDTO VdmsSync = apiCallService.updateVdmsStatus(vdmsId);
//
//        log.info("VdmsSync {}", VdmsSync);
//
//        if (VdmsSync != null) {
//            updateVdmsStatusApi(vdmsId, VdmsSync);
//        }
//    }
//
//
//    public void interfaceStatusCheck() {
//        String cmd = "interfaceStatusMonitor";
//        Boolean isWorking = utils.execRemoteAccessCmd(cmd);
//        log.info("Interface STATUS Working? {}", isWorking);
//
////		  String[] cmd = {"bash", "-c", "cd scripts && ./interfaceStatusMonitor &"};
////	        utils.execPipedCmd(cmd).get("output");
//    }
//
//
//    public void refreshAllInterfaceStatus() {
//        try {
//            List<InterfaceDTO> allInterface = settingsService.getSystemInterfaces();
//            System.out.println("Interface List: " + allInterface);
//
//            if (allInterface != null) {
//                for (InterfaceDTO oneInterface : allInterface) {
//                    String status;
//
//                    if (oneInterface.getIsActive()) {
//                        status = "up";
//                    } else {
//                        status = "down";
//                    }
//                    System.out.println("Interface Name: " + oneInterface.getInterface_name() + " Status: " + status);
////                    systemInterfaceService.interfaceStatus(oneInterface.getInterface_name(), status);
//                    systemInterfaceRepository.upsertInterfaceStatus(oneInterface.getInterface_name(), status);
//                }
//            } else {
//                log.info("No interfaces found to refresh status.");
//            }
//        } catch (Exception e) {
//            log.error("Error in refreshAllInterfaceStatus method while refreshing interface status {}", e.getMessage(), e);
//        }
//    }
//
//    public void syncProxyServer(String vdmsId) {
//        proxyService.syncProxyServer(vdmsId);
//    }
//
//    public void syncProxyClient(String vdmsId) {
//        proxyService.syncProxyClient(vdmsId);
//    }
//
//
//    /***********************************Update Product Details By Model and Mac Vendor - Model Detection Script*************************************/
//    public void updateDeviceProductModel(String device_id, String mac_by_vendor, String model) {
//        deviceService.updateDeviceProductModel(device_id, mac_by_vendor, model);
//    }
//
//    public String getModelByScript(String docker_name, String ip_address, String script_name) {
//        String[] scriptCmd = {"bash", "-c", "docker exec " + docker_name + " ./model_scripts/" + script_name + " -i " + ip_address + " -uj "};
//        switch (script_name) {
//            case "cdp":
//                String interface_in = dockerService.getInternalInterfaceByDockerName(docker_name);
//                scriptCmd[2] = "docker exec " + docker_name + " ./model_scripts/" + script_name + " -I " + interface_in + " -i " + ip_address + " -j ";
//                break;
//            default:
//                break;
//        }
//
//        String scriptResult = utils.execPipedCmd(scriptCmd).get("output");
//        log.info("Script Result: {}", scriptResult);
//
//        try {
//            JSONObject jsonObject = JSONObject.parseObject(scriptResult);
//            String model = jsonObject.getString("model_number");
//            log.info("model {}", model);
//            return model;
//        } catch (Exception e) {
//            log.error("Error in getModelByScript method while parsing json", e);
//        }
//        return null;
//    }
//
//
//    public String getScriptNameByMacVendor(String mac_by_vendor) {
//        try {
//            //Read from file
//            StringBuilder stringBuilder = new StringBuilder();
//            InputStream inputStreamReader = getClass().getResourceAsStream("/vendor_scripts.json");
//            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamReader))) {
//                stringBuilder.append(bufferedReader.lines().collect(Collectors.joining("\n")));
//            }
//            String fileOutput = stringBuilder.toString();
//            JSONArray fileOutputArray = JSONArray.parseArray(fileOutput);
//
//            for (int i = 0; i < fileOutputArray.size(); i++) {
//                JSONObject fileOutputObject = fileOutputArray.getJSONObject(i);
//                if (mac_by_vendor.contains(fileOutputObject.getString("mac_by_vendor"))) {
//                    return fileOutputObject.getString("script_name");
//                }
//            }
//            return null;
//        } catch (Exception e) {
//            log.error("Error in getScriptNameByMacVendor method while reading vendor_scripts.json", e);
//        }
//        return null;
//    }
//
//    public void getProductDetailsByScript(String device_id, String docker_name, String ip_address, String mac_by_vendor) {
//        String scriptByMacByVendor = getScriptNameByMacVendor(mac_by_vendor);
//        if (scriptByMacByVendor != null || !scriptByMacByVendor.isEmpty()) {
//            String modelByScript = getModelByScript(docker_name, ip_address, scriptByMacByVendor);
//            if (modelByScript != null || !modelByScript.isEmpty()) {
//                updateDeviceProductModel(device_id, mac_by_vendor, modelByScript);
//            }
//        }
//    }
//
//
//    public void modelReset(String user_name, String vdms_id, String docker_name) {
//        deviceService.modelResetbyDockerName(docker_name);
//        try {
//            Set<DeviceDTO> devices = deviceService.getDeviceNamesByVdmsIdAndDockerName(user_name, vdms_id, docker_name);
//            if (devices != null && devices.size() > 0) {
//                for (DeviceDTO device : devices) {
//                    try {
//                        deviceService.deleteDeviceGlobalProductDetails(device.getId());
//                    } catch (Exception e) {
//                        log.error("Error in modelReset method while deleting product details for device id: {}", device.getId(), e);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error in modelReset method while fetching devices for vdms_id: {} and docker_name: {}", vdms_id, docker_name, e);
//        }
//    }
//
//    /***********************************Update Product Details By Model and Mac Vendor - Model Detection Script**************************************/
//
//    public void syncQrCodeNfcBarCode(String vdmsId, VdmsSyncDTO vdmsSyncDTO, String message) {
//        log.info("syncQrCodeNfcBarCode on {}", message);
//        if (vdmsSyncDTO.getQr_sync() != null) {
//            if (vdmsSyncDTO.getQr_sync() == 2) {
//                this.syncAllQrCode(vdmsId);
//            } else if (vdmsSyncDTO.getQr_sync() == 1) {
//                this.syncQrCode(vdmsId);
//            }
//        }
//        if (vdmsSyncDTO.getNfc_sync() != null) {
//            if (vdmsSyncDTO.getNfc_sync() == 2) {
//                this.syncAllNfc(vdmsId);
//            } else if (vdmsSyncDTO.getNfc_sync() == 1) {
//                this.syncNfc(vdmsId);
//            }
//        }
//        if (vdmsSyncDTO.getBarcode_sync() != null) {
//            if (vdmsSyncDTO.getBarcode_sync() == 2) {
//                this.syncAllBarCode(vdmsId);
//            } else if (vdmsSyncDTO.getBarcode_sync() == 1) {
//                this.syncBarCode(vdmsId);
//            }
//        }
//    }
//
//    public void syncQrCode(String vdmsId) {
//        if (qrcodeFlag.compareAndSet(0, 1)) { // Atomically set the flag to 1 if it was 0
//            ExecutorService executorService = Executors.newSingleThreadExecutor();
//            executorService.execute(() -> {
//                try {
//                    log.info("QrCode Sync started...flag..{}", qrcodeFlag.get());
//
//                    //Get changed qr codes from cloud.
//                    Set<QrCodeDTO> qrCodeDTOS = qrCodeService.syncQrCodes(vdmsId);
//
//                    //Get qr code details before sync.
//                    Set<String> qrcodeIds = qrCodeDTOS.stream()
//                            .filter(dto -> dto.getDeviceId() != null)
//                            .map(QrCodeDTO::getId)
//                            .collect(Collectors.toSet());
//
//                    Set<QrCodeDTO> existingQrCodes = qrCodeService.getQrCodeDetailsByIds(qrcodeIds);
//                    log.info("Existing qrcode size:{}", existingQrCodes.size());
//
//                    qrCodeService.upsertQrCodesInBatch(qrCodeDTOS);
//
//                    Set<QrCodeDTO> updatedQrCodes = qrCodeService.getQrCodeDetailsByIds(qrcodeIds);
//                    log.info("Updated qrcode size:{}", updatedQrCodes.size());
//
//                    Set<ClientQrCodeDTO> clientQrCodeDTOS = clientQrCodeService.syncClientQrCodes(vdmsId);
//
//                    Set<String> clientQrcodeIds = clientQrCodeDTOS.stream()
//                            .filter(dto -> dto.getDeviceId() != null)
//                            .map(ClientQrCodeDTO::getId)
//                            .collect(Collectors.toSet());
//
//                    Set<QrCodeDTO> existingClientQrCodes = qrCodeService.getClientQrCodeDetailsByIds(clientQrcodeIds);
//                    log.info("Existing client qrcode size:{}", existingClientQrCodes.size());
//
//                    clientQrCodeService.upsertClientQrCodesInBatch(clientQrCodeDTOS);
//
//                    Set<QrCodeDTO> updatedClientQrCodes = qrCodeService.getClientQrCodeDetailsByIds(clientQrcodeIds);
//                    log.info("Updated client qrcode size:{}", updatedClientQrCodes.size());
//
//                    apiCallService.updateQrCodeSyncByVdmsId(vdmsId);
//
//                    deviceService.syncDeviceOnboardStatusForQrSync(vdmsId, existingQrCodes, updatedQrCodes, existingClientQrCodes, updatedClientQrCodes);
//                    log.info("QrCode Sync Ended...flag..{}", qrcodeFlag.get());
//                } catch (Exception e) {
//                    log.error("Error in Qrcode sync for vdmsId {}: ", vdmsId, e);
//                } finally {
//                    qrcodeFlag.set(0); // Reset the flag in a thread-safe manner
//                    log.info("Qrcode Sync Shutdown...flag..{}", qrcodeFlag.get());
//                }
//            });
//
//            executorService.shutdown(); // Initiate an orderly shutdown
//        } else {
//            log.info("Sync QrCodes in progress");
//        }
//    }
//
//    public void syncAllQrCode(String vdmsId) {
//        if (qrcodeFlag.compareAndSet(0, 1)) { // Atomically set the flag to 1 if it was 0
//            ExecutorService executorService = Executors.newSingleThreadExecutor();
//            executorService.execute(() -> {
//                try {
//                    log.info("All QrCode Sync started...flag..{}", qrcodeFlag.get());
//                    qrCodeService.syncAlQrCodes(vdmsId);
//                    clientQrCodeService.syncAllClientQrCodes(vdmsId);
//                    apiCallService.updateQrCodeSyncByVdmsId(vdmsId);
//                    deviceService.syncAllDeviceOnboardStatusForQrSync(vdmsId);
//                    log.info("All QrCode Sync Ended...flag..{}", qrcodeFlag.get());
//                } catch (Exception e) {
//                    log.error("Error in All Qrcode sync for vdmsId {}: ", vdmsId, e);
//                } finally {
//                    qrcodeFlag.set(0); // Reset the flag in a thread-safe manner
//                    log.info("All Qrcode Sync Shutdown...flag..{}", qrcodeFlag.get());
//                }
//            });
//
//            executorService.shutdown(); // Initiate an orderly shutdown
//        } else {
//            log.info("Sync All  QrCodes in progress");
//        }
//    }
//
//    public void syncAllNfc(String vdmsId) {
//        if (nfcFlag.compareAndSet(0, 1)) { // Atomically set the flag to 1 if it was 0
//            ExecutorService executorService = Executors.newSingleThreadExecutor();
//            executorService.execute(() -> {
//                try {
//                    log.info("All Nfc Sync started...flag..{}", nfcFlag.get());
//                    nfcService.syncAllNfc(vdmsId);
//                    clientNfcService.syncAllClientNfc(vdmsId);
//                    apiCallService.updateNfcSyncByVdmsId(vdmsId);
//                    log.info("All Nfc Sync Ended...flag..{}", nfcFlag.get());
//                } catch (Exception e) {
//                    log.error("Error in All  Nfc sync for vdmsId {}: ", vdmsId, e);
//                } finally {
//                    nfcFlag.set(0); // Reset the flag in a thread-safe manner
//                    log.info("All Nfc Sync Shutdown...flag..{}", nfcFlag.get());
//                }
//            });
//            executorService.shutdown(); // Initiate an orderly shutdown
//        } else {
//            log.info("Sync All NFC in progress");
//        }
//    }
//
//    public void syncAllBarCode(String vdmsId) {
//        if (barcodeFlag.compareAndSet(0, 1)) { // Atomically set the flag to 1 if it was 0
//            ExecutorService executorService = Executors.newSingleThreadExecutor();
//            executorService.execute(() -> {
//                try {
//                    log.info("All BarCode Sync started...flag..{}", barcodeFlag.get());
//                    clientBarCodeService.syncAllClientBarCode(vdmsId);
//                    apiCallService.updateBarCodeSyncByVdmsId(vdmsId);
//                    log.info("All BarCode Sync Ended...flag..{}", barcodeFlag.get());
//                } catch (Exception e) {
//                    log.error("Error in All BarCode sync for vdmsId {}: ", vdmsId, e);
//                } finally {
//                    barcodeFlag.set(0); // Reset the flag in a thread-safe manner
//                    log.info("All BarCode Sync Shutdown...flag..{}", barcodeFlag.get());
//                }
//            });
//            executorService.shutdown(); // Initiate an orderly shutdown
//        } else {
//            log.info("Sync All BarCode in progress");
//        }
//    }
//
//    public void syncNfc(String vdmsId) {
//        if (nfcFlag.compareAndSet(0, 1)) { // Atomically set the flag to 1 if it was 0
//            ExecutorService executorService = Executors.newSingleThreadExecutor();
//            executorService.execute(() -> {
//                try {
//                    log.info("Nfc Sync started...flag..{}", nfcFlag.get());
//                    nfcService.syncNfc(vdmsId);
//                    clientNfcService.syncClientNfc(vdmsId);
//                    apiCallService.updateNfcSyncByVdmsId(vdmsId);
//                    log.info("Nfc Sync Ended...flag..{}", nfcFlag.get());
//                } catch (Exception e) {
//                    log.error("Error in Nfc sync for vdmsId {}: ", vdmsId, e);
//                } finally {
//                    nfcFlag.set(0); // Reset the flag in a thread-safe manner
//                    log.info("Nfc Sync Shutdown...flag..{}", nfcFlag.get());
//                }
//            });
//            executorService.shutdown(); // Initiate an orderly shutdown
//        } else {
//            log.info("Sync NFC in progress");
//        }
//    }
//
//    public void syncBarCode(String vdmsId) {
//        if (barcodeFlag.compareAndSet(0, 1)) { // Atomically set the flag to 1 if it was 0
//            ExecutorService executorService = Executors.newSingleThreadExecutor();
//            executorService.execute(() -> {
//                try {
//                    log.info("BarCode Sync started...flag..{}", barcodeFlag.get());
//                    clientBarCodeService.syncClientBarCode(vdmsId);
//                    apiCallService.updateBarCodeSyncByVdmsId(vdmsId);
//                    log.info("BarCode Sync Ended...flag..{}", barcodeFlag.get());
//                } catch (Exception e) {
//                    log.error("Error in BarCode sync for vdmsId {}: ", vdmsId, e);
//                } finally {
//                    barcodeFlag.set(0); // Reset the flag in a thread-safe manner
//                    log.info("BarCode Sync Shutdown...flag..{}", barcodeFlag.get());
//                }
//            });
//            executorService.shutdown(); // Initiate an orderly shutdown
//        } else {
//            log.info("Sync BarCode in progress");
//        }
//    }
//
//    public void syncSkillProfiles(String vdmsId) {
//        log.info("VDMS ID: {}", vdmsId);
//        try {
//            log.info("Entered syncSkillProfiles method as skillProfilesSync = 1");
//
//            // 1. --------------- Technicians UPSERT Sync --------------->
//            List<TechnicianDTO> technicians = apiCallService.getAllTechnicians(vdmsId);
//            log.info("technicians.. {}", technicians);
//
//            if (technicians != null && !technicians.isEmpty()) {
//                Map<Integer, List<TechnicianDTO>> filteredTechnicianData = technicians.stream()
//                        .filter(technician -> technician.getSync() != null)
//                        .collect(Collectors.groupingBy(TechnicianDTO::getSync));
//
//                log.info("SyncType.UPSERT: {} ", SyncType.UPSERT.getCode());
//                Set<String> upsertedTechnicianIds = technicianService.upsertTechnician(filteredTechnicianData.getOrDefault(SyncType.UPSERT.getCode(), Collections.emptyList()));
//                log.info("upsertedTechnicianIds: {} ", upsertedTechnicianIds);
//
//                if (!upsertedTechnicianIds.isEmpty()) {
//                    log.info("Resetting UPSERT technicians sync...");
//                    apiCallService.resetSyncStatusByTechnicianIds(upsertedTechnicianIds);
//                } else {
//                    log.info("No technicians to UPSERT sync...");
//                }
//                log.info("Sync Technicians UPSERT completed!");
//            } else {
//                log.info("No technicians data found for VDMS ID: {}", vdmsId);
//            }
//
//            // 2. --------------- Technician Skills Sync --------------->
//            List<TechnicianSkillDTO> technicianSkills = apiCallService.getAllTechnicianSkills(vdmsId);
//            log.info("technicianSkills.. {}", technicianSkills);
//
//            if (technicianSkills != null && !technicianSkills.isEmpty()) {
//                Map<Integer, List<TechnicianSkillDTO>> filteredTechnicianSkillData = technicianSkills.stream()
//                        .filter(skill -> skill.getSync() != null)
//                        .collect(Collectors.groupingBy(TechnicianSkillDTO::getSync));
//
//                log.info("SyncType.UPSERT: {} ", SyncType.UPSERT.getCode());
//                Set<String> upsertedTechnicianSkillIds = technicianSkillService.upsertTechnicianSkill(filteredTechnicianSkillData.getOrDefault(SyncType.UPSERT.getCode(), Collections.emptyList()));
//                log.info("upsertedTechnicianSkillIds: {} ", upsertedTechnicianSkillIds);
//
//                if (!upsertedTechnicianSkillIds.isEmpty()) {
//                    log.info("Resetting UPSERT TechnicianSkillIds sync...");
//                    apiCallService.resetSyncByTechnicianSkillIds(upsertedTechnicianSkillIds);
//                } else {
//                    log.info("No TechnicianSkillIds to UPSERT sync...");
//                }
//
//                log.info("SyncType.DELETE: {} ", SyncType.DELETE.getCode());
//                Set<String> deletedTechnicianSkillIds = technicianSkillService.deleteTechnicianSkillsById(filteredTechnicianSkillData.getOrDefault(SyncType.DELETE.getCode(), Collections.emptyList()));
//                log.info("deletedTechnicianSkillIds: {} ", deletedTechnicianSkillIds);
//
//                if (!deletedTechnicianSkillIds.isEmpty()) {
//                    log.info("Resetting DELETE TechnicianSkillIds sync...");
//                    apiCallService.resetSyncByTechnicianSkillIds(deletedTechnicianSkillIds);
//                } else {
//                    log.info("No TechnicianSkillIds to DELETE sync...");
//                }
//
//                log.info("Sync TechnicianSkills completed!");
//            } else {
//                log.info("No technician skills data found for VDMS ID: {}", vdmsId);
//            }
//
//            // 3. --------------- Technician Availability Sync --------------->
//            List<TechnicianAvailabilityDTO> techniciansAvailability = apiCallService.getAllTechniciansAvailability(vdmsId);
//            log.info("techniciansAvailability.. {}", techniciansAvailability);
//
//            if (techniciansAvailability != null && !techniciansAvailability.isEmpty()) {
//                Map<Integer, List<TechnicianAvailabilityDTO>> filteredTechnicianAvailabilityData = techniciansAvailability.stream()
//                        .filter(availability -> availability.getSync() != null)
//                        .collect(Collectors.groupingBy(TechnicianAvailabilityDTO::getSync));
//
//                log.info("SyncType.UPSERT: {} ", SyncType.UPSERT.getCode());
//                Set<String> upsertedTechnicianAvailabilityIds = technicianAvailabilityService.upsertTechnicianAvailability(filteredTechnicianAvailabilityData.getOrDefault(SyncType.UPSERT.getCode(), Collections.emptyList()));
//                log.info("upsertedTechnicianAvailabilityIds: {} ", upsertedTechnicianAvailabilityIds);
//
//                if (!upsertedTechnicianAvailabilityIds.isEmpty()) {
//                    log.info("Resetting UPSERT TechnicianAvailabilityIds sync...");
//                    apiCallService.resetSyncByTechnicianAvailabilityIds(upsertedTechnicianAvailabilityIds);
//                } else {
//                    log.info("No TechnicianAvailabilityIds to UPSERT sync...");
//                }
//
//                log.info("SyncType.DELETE: {} ", SyncType.DELETE.getCode());
//                Set<String> deletedTechnicianAvailabilityIds = technicianAvailabilityService.deleteTechnicianAvailabilityById(filteredTechnicianAvailabilityData.getOrDefault(SyncType.DELETE.getCode(), Collections.emptyList()));
//                log.info("deletedTechnicianAvailabilityIds: {} ", deletedTechnicianAvailabilityIds);
//
//                if (!deletedTechnicianAvailabilityIds.isEmpty()) {
//                    log.info("Resetting DELETE TechnicianAvailabilityIds sync...");
//                    apiCallService.resetSyncByTechnicianAvailabilityIds(deletedTechnicianAvailabilityIds);
//                } else {
//                    log.info("No TechnicianAvailabilityIds to DELETE sync...");
//                }
//
//                log.info("Sync TechnicianAvailability completed!");
//            } else {
//                log.info("No technician availability data found for VDMS ID: {}", vdmsId);
//            }
//
//            // 4. --------------- Technician Certificates Sync --------------->
//            List<TechnicianCertificateDTO> techniciansCertificates = apiCallService.getAllTechniciansCertificates(vdmsId);
//            log.info("techniciansCertificates.. {}", techniciansCertificates);
//
//            if (techniciansCertificates != null && !techniciansCertificates.isEmpty()) {
//                Map<Integer, List<TechnicianCertificateDTO>> filteredTechnicianCertificateData = techniciansCertificates.stream()
//                        .filter(certificate -> certificate.getSync() != null)
//                        .collect(Collectors.groupingBy(TechnicianCertificateDTO::getSync));
//
//                log.info("SyncType.UPSERT: {} ", SyncType.UPSERT.getCode());
//                Set<String> upsertedTechnicianCertificateIds = technicianCertificateService.upsertTechnicianCertificate(filteredTechnicianCertificateData.getOrDefault(SyncType.UPSERT.getCode(), Collections.emptyList()));
//                log.info("upsertedTechnicianCertificateIds: {} ", upsertedTechnicianCertificateIds);
//
//                if (!upsertedTechnicianCertificateIds.isEmpty()) {
//                    log.info("Resetting UPSERT techniciansCertificateIds sync...");
//                    apiCallService.resetSyncByTechnicianCertificateIds(upsertedTechnicianCertificateIds);
//                } else {
//                    log.info("No techniciansCertificateIds to UPSERT sync...");
//                }
//
//                log.info("SyncType.DELETE: {} ", SyncType.DELETE.getCode());
//                Set<String> deletedTechnicianCertificateIds = technicianCertificateService.deleteTechnicianCertificatesById(filteredTechnicianCertificateData.getOrDefault(SyncType.DELETE.getCode(), Collections.emptyList()));
//                log.info("deletedTechnicianCertificateIds: {} ", deletedTechnicianCertificateIds);
//
//                if (!deletedTechnicianCertificateIds.isEmpty()) {
//                    log.info("Resetting DELETE techniciansCertificateIds sync...");
//                    apiCallService.resetSyncByTechnicianCertificateIds(deletedTechnicianCertificateIds);
//                } else {
//                    log.info("No techniciansCertificateIds to DELETE sync...");
//                }
//
//                log.info("Sync TechnicianCertificates completed!");
//            } else {
//                log.info("No technician certificates data found for VDMS ID: {}", vdmsId);
//            }
//
//            // 5. --------------- Technician AI Suggestions Sync --------------->
//            List<DeviceTechnicianAISuggestionDTO> deviceTechnicianAISuggestions = apiCallService.getAllDeviceTechnicianAISuggestions(vdmsId);
//            log.info("deviceTechnicianAISuggestions.. {}", deviceTechnicianAISuggestions);
//
//            if (deviceTechnicianAISuggestions != null && !deviceTechnicianAISuggestions.isEmpty()) {
//                Set<String> upsertedDeviceTechnicianSuggestionIds = deviceTechnicianAISuggestionService.upsertTechnicianSuggestion(deviceTechnicianAISuggestions);
//                log.info("upsertedDeviceTechnicianSuggestionIds: {} ", upsertedDeviceTechnicianSuggestionIds);
//
//                if (!upsertedDeviceTechnicianSuggestionIds.isEmpty()) {
//                    log.info("Resetting UPSERT deviceTechnicianAISuggestions sync...");
//                    apiCallService.resetSyncByDeviceTechnicianAiSuggestionIds(upsertedDeviceTechnicianSuggestionIds);
//                } else {
//                    log.info("No deviceTechnicianAISuggestions to UPSERT sync...");
//                }
//
//                /* Currently there is no DELETE sync for Technician AI Suggestions */
//                log.info("Sync TechnicianAISuggestions completed!");
//            } else {
//                log.info("No device technician AI suggestions data found for VDMS ID: {}", vdmsId);
//            }
//
//            // 6. --------------- Technicians DELETE Sync --------------->
//            log.info("technicians.. {}", technicians);
//
//            if (technicians != null && !technicians.isEmpty()) {
//                Map<Integer, List<TechnicianDTO>> filteredTechnicianData = technicians.stream()
//                        .filter(technician -> technician.getSync() != null)
//                        .collect(Collectors.groupingBy(TechnicianDTO::getSync));
//
//                log.info("SyncType.DELETE: {} ", SyncType.DELETE.getCode());
//                Set<String> deletedTechnicianIds = technicianService.deleteTechniciansById(filteredTechnicianData.getOrDefault(SyncType.DELETE.getCode(), Collections.emptyList()));
//                log.info("deletedTechnicianIds: {} ", deletedTechnicianIds);
//
//                if (!deletedTechnicianIds.isEmpty()) {
//                    log.info("Resetting DELETE technicians sync...");
//                    apiCallService.resetSyncStatusByTechnicianIds(deletedTechnicianIds);
//                } else {
//                    log.info("No technicians to DELETE sync...");
//                }
//                log.info("Sync Technicians DELETE completed!");
//            } else {
//                log.info("No technicians data found for VDMS ID: {}", vdmsId);
//            }
//
//        } catch (Exception e) {
//            log.error("Error in syncSkillProfiles method", e);
//        }
//        log.info("Exited syncSkillProfiles method as skillProfilesSync = 1");
//    }
//
//    private void syncInventoryDevice(String vdmsId, InventoryDeviceSyncDTO inventoryDeviceSyncDTO) {
//        log.info("Received sync for Inventory Device...");
//        try {
//            if (inventoryDeviceSyncDTO != null) {
//                log.info("Received Data for Inventory Device...{}", inventoryDeviceSyncDTO.toString());
//                JSONObject stocked_out_items = apiCallService.getInventoryItemsByStockOutId(inventoryDeviceSyncDTO);
//                if (stocked_out_items != null) {
//                    Set<DeviceDTO> taggedDeviceDTOS = inventoryDeviceService.upsertInventoryDevices(stocked_out_items, vdmsId, inventoryDeviceSyncDTO.getEmail(), inventoryDeviceSyncDTO);
//                    apiCallService.updateTaggedInventoryItems(taggedDeviceDTOS);
//                    log.info("Successfully moved assets from inventory...");
//                } else {
//                    log.info("No Stocked out items found for Inventory Device Sync...");
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error in syncInventoryDevice method for VDMS ID: {}", vdmsId, e);
//        }
//
//    }
//    // sclera agent permission sync start
//    public void syncScleraAgentPermission(String vdmsId) {
//        log.info("Entered syncScleraAgentPermission method as sclera_agent_permission_sync = 1");
//        String scleraAgentPermission = apiCallService.getAgentPermissionsByVdmsId(vdmsId);
//        if(scleraAgentPermission!=null){
//            log.info("scleraAgentPermission {}",scleraAgentPermission);
//            vdmsService.setScleraAgentPermission(scleraAgentPermission);
//        }
//    }
//
//    // Application users sync start
//    public void syncManagedSoftwareUsersFromInventory(String vdmsId) throws JsonProcessingException {
//        log.info("Entered syncManagedSoftwareUsersFromInventory method");
//        try {
//            JSONArray applicationUsersJson = apiCallService.getAllApplicationUsersFromInventory(vdmsId);
//
//            if (applicationUsersJson == null || applicationUsersJson.isEmpty()) {
//                log.warn("No application users data returned from the inventory API for vdmsId: {}", vdmsId);
//                return;
//            }
//
//            List<InventoryApplicationUserDTO> managedSoftwareUsers = JSON.parseArray(
//                    applicationUsersJson.toJSONString(),
//                    InventoryApplicationUserDTO.class
//            );
//            log.info("applicationUsersList.. {}", managedSoftwareUsers);
//
//            // Step 1: Collect all application IDs from users
//            List<String> applicationIds = managedSoftwareUsers.stream()
//                    .map(InventoryApplicationUserDTO::getApplicationId)
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//
//            // Step 2: Fetch managed software for these applications
//            List<ManagedSoftware> taggedManagedSoftwares = managedSoftwareRepository.findByApplicationIds(applicationIds);
//
//            // Step 3: Collect IDs of applications that are tagged to managed software
//            Set<String> taggedApplicationIds = taggedManagedSoftwares.stream()
//                    .map(ManagedSoftware::getApplicationId)
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toSet());
//
//            // Step 4: Filter and group only the users whose applicationId is tagged to managed software
//            Map<Integer, List<InventoryApplicationUserDTO>> filteredApplicationUserData = managedSoftwareUsers.stream()
//                    .filter(applicationUser -> applicationUser.getSyncStatus() != null)
//                    .filter(applicationUser -> taggedApplicationIds.contains(applicationUser.getApplicationId()))
//                    .collect(Collectors.groupingBy(InventoryApplicationUserDTO::getSyncStatus));
//
//            // SyncType == 1 (upsert)
//            log.info("SyncType UPSERT ApplicationUsers: {} ", SyncType.UPSERT.getCode());
//            Set<String> failedToUpsertIds = applicationUserService.upsertApplicationUsersSync(filteredApplicationUserData.getOrDefault(SyncType.UPSERT.getCode(), Collections.emptyList()));
//
//            Set<String> upsertedApplicationUserIds = managedSoftwareUsers.stream()
//                    .filter(user -> user.getSyncStatus() != null && user.getSyncStatus() == 1) // only UPSERT users
//                    .map(InventoryApplicationUserDTO::getId)
//                    .filter(id -> !failedToUpsertIds.contains(id)) // exclude failed ones
//                    .collect(Collectors.toSet());
//            log.info("upsertedApplicationUserIds: {} ", upsertedApplicationUserIds);
//
//            if (!upsertedApplicationUserIds.isEmpty()) {
//                log.info("*********** Reset UPSERT ApplicationUsers sync call ***********");
//                apiCallService.syncApplicationUsers(upsertedApplicationUserIds, "0");
//                log.info("upsertApplicationUsers completed...");
//            } else {
//                log.info("Skipping RESET ApplicationUsers UPSERT sync as there are no upserted application users...");
//            }
//
//            // SyncType == 2 (delete)
//            log.info("SyncType DELETE ApplicationUsers: {} ", SyncType.DELETE.getCode());
//            Set<String> failedToDeleteIds = applicationUserService.deleteApplicationUsersSync(filteredApplicationUserData.getOrDefault(SyncType.DELETE.getCode(), Collections.emptyList()));
//
//            Set<String> deletedApplicationUserIds = managedSoftwareUsers.stream()
//                    .filter(user -> user.getSyncStatus() != null && user.getSyncStatus() == 2) // only DELETE users
//                    .map(InventoryApplicationUserDTO::getId)
//                    .filter(id -> !failedToDeleteIds.contains(id)) // exclude failed ones
//                    .collect(Collectors.toSet());
//            log.info("deletedApplicationUserIds: {} ", deletedApplicationUserIds);
//
//            if (!deletedApplicationUserIds.isEmpty()) {
//                log.info("*********** Reset DELETE ApplicationUsers sync call ***********");
//                apiCallService.syncApplicationUsers(deletedApplicationUserIds, "1");
//                log.info("deleteApplicationUsers completed...");
//            } else {
//                log.info("Skipping RESET ApplicationUsers DELETE sync as there are no deleted application users...");
//            }
//
//            log.info("syncManagedSoftwareUsersFromInventory completed...");
//        } catch (Exception e) {
//            log.error("Error in syncManagedSoftwareUsersFromInventory for vdmsId {}: ", vdmsId, e);
//        }
//    }
//
//    // Application details sync start
//    public void syncManagedSoftwaresFromInventory(String vdmsId) throws JsonProcessingException {
//        log.info("Entered syncManagedSoftwaresFromInventory method");
//        try {
//            JSONArray applicationDetailsJson = apiCallService.getAllInventoryApplications(vdmsId);
//
//            if (applicationDetailsJson == null || applicationDetailsJson.isEmpty()) {
//                log.warn("No applications data returned from the inventory API for vdmsId: {}", vdmsId);
//                return;
//            }
//
//            List<InventoryApplicationDTO> applicationDetails = JSON.parseArray(
//                    applicationDetailsJson.toJSONString(),
//                    InventoryApplicationDTO.class
//            );
//            log.info("applicationDetailsList.. {}", applicationDetails);
//
//            // Step 1: Collect all application IDs
//            List<String> applicationIds = applicationDetails.stream()
//                    .map(InventoryApplicationDTO::getId)
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//
//            // Step 2: Fetch managed software for these applications
//            List<ManagedSoftware> taggedManagedSoftwares = managedSoftwareRepository.findByApplicationIds(applicationIds);
//
//            // Step 3: Collect IDs of applications that are tagged to managed software
//            Set<String> taggedApplicationIds = taggedManagedSoftwares.stream()
//                    .map(ManagedSoftware::getApplicationId)
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toSet());
//
//            // Step 4: Filter and group only the applications whose applicationId is tagged to managed software
//            Map<Integer, List<InventoryApplicationDTO>> filteredApplicationDetails = applicationDetails.stream()
//                    .filter(application -> application.getSyncStatus() != null)
//                    .filter(applicationUser -> taggedApplicationIds.contains(applicationUser.getId()))
//                    .collect(Collectors.groupingBy(InventoryApplicationDTO::getSyncStatus));
//
//            // SyncType == 1 (upsert)
//            log.info("SyncType UPSERT Applications: {}", SyncType.UPSERT.getCode());
//            Set<String> failedToUpdateIds = managedSoftwareService.updateManagedSoftwareDetailsSync(filteredApplicationDetails.getOrDefault(SyncType.UPSERT.getCode(), Collections.emptyList()));
//
//            Set<String> upsertedApplicationIds = applicationDetails.stream()
//                    .filter(app -> app.getSyncStatus() != null && app.getSyncStatus() == 1) // only UPSERT applications
//                    .map(InventoryApplicationDTO::getId)
//                    .filter(id -> !failedToUpdateIds.contains(id)) // exclude failed ones
//                    .collect(Collectors.toSet());
//            log.info("upsertedApplicationIds: {} ", upsertedApplicationIds);
//
//            if (!upsertedApplicationIds.isEmpty()) {
//                log.info("*********** Reset UPSERT Applications sync call ***********");
//                apiCallService.syncApplication(upsertedApplicationIds, "0");
//                log.info("upsertApplications completed...");
//            } else {
//                log.info("Skipping RESET Applications UPSERT sync as there are no upserted applications...");
//            }
//
//            // SyncType == 2 (delete)
//            log.info("SyncType DELETE Applications: {}", SyncType.DELETE.getCode());
//            Set<String> failedToClearIds = managedSoftwareService.clearManagedSoftwareDetailsSync(filteredApplicationDetails.getOrDefault(SyncType.DELETE.getCode(), Collections.emptyList()));
//
//            Set<String> deletedApplicationIds = applicationDetails.stream()
//                    .filter(app -> app.getSyncStatus() != null && app.getSyncStatus() == 2) // only DELETE applications
//                    .map(InventoryApplicationDTO::getId)
//                    .filter(id -> !failedToClearIds.contains(id)) // exclude failed ones
//                    .collect(Collectors.toSet());
//            log.info("deletedApplicationIds: {} ", deletedApplicationIds);
//
//            if (!deletedApplicationIds.isEmpty()) {
//                log.info("*********** Reset DELETE Applications sync call ***********");
//                apiCallService.syncApplication(deletedApplicationIds, "1");
//                log.info("deleteApplications completed...");
//            } else {
//                log.info("Skipping RESET Applications DELETE sync as there are no deleted applications...");
//            }
//
//            log.info("syncManagedSoftwaresFromInventory completed...");
//        } catch (Exception e) {
//            log.error("Error in syncManagedSoftwaresFromInventory for vdmsId {}: ", vdmsId, e);
//        }
//    }
//}
