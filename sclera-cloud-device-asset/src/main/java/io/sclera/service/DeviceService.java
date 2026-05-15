package io.sclera.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.Generators;
import com.itextpdf.text.Document;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import io.sclera.Repository.*;
import io.sclera.dto.*;
import io.sclera.dto.touchscreen.*;
import io.sclera.dto.touchscreen.assetmapper.AssetDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import io.sclera.enums.JacksCodeMapping;
import io.sclera.integration.dto.ResponseDTO;
import io.sclera.models.*;
import io.sclera.queryrepository.DeviceQueryRepository;
import io.sclera.rabbitmq.RabbitmqService;
//import io.sclera.service.touchscreen.VdmsService;
import io.sclera.service.touchscreen.VdmsService;
import io.sclera.service.touchscreen.assetmapper.AssetMapperService;
import io.sclera.sockets.SocketService;
import io.sclera.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.poi.ss.util.CellUtil.createCell;

/* Asset Match Status Values
	value 				Used for
	0					Unmatched
 	1					Matched
 	2					Verified
 	3					Archived
*/

/* Device Alarm Type Values
	value 				Used for
 	1					went offline
 	2					came online
 	3					no response
 	4					mac lost an ip
 	5					mac got an ip
 	6					ip conflict
 	7					ip conflict resolved
 	8					new device
 	9					rebooted
 	10					ip change
 	11					ip expired
*/

@Service
@ConfigurationProperties(prefix = "sclera")
public class DeviceService {

    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);

    @Autowired
    SocketService socketservice;

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    NotesService notesService;

    // @Autowired
    // SnmpConfigurationService snmpconfigurationService;

    @Autowired
    SnmpService snmpService;
    @Autowired
    VdmsService vdmsService;

    @Autowired
    InterfaceService interfaceService;

    // @Autowired
    // SnmpDeviceService snmpdeviceService;

    @Autowired
    PhonebookService phonebookService;

    @Autowired
    PortService portService;

    @Autowired
    APIRequest apiRequest;

    @Autowired
    APICallService apicallService;

    @Autowired
    BuildingService buildingService;

    @Autowired
    UtilsService utilsService;

    @Autowired
    Product_DetailsService product_detailsService;

    @Autowired
    HistoryService historyService;

    @Autowired
    Utils utils;

    @Autowired
    AlertService alertService;

    @Autowired
    TicketService ticketService;

    @Autowired
    BacnetService bacnetService;

    @Autowired
    LorawanService lorawanService;

    @Autowired
    DisruptiveService disruptiveService;

    @Autowired
    DeviceIPAddressRepository deviceIPAddressRepository;

    @Autowired
    MyDevicesService myDevicesService;

    @Autowired
    RabbitmqService rabbitmqService;

    @Autowired
    AsyncService asyncService;

    @Autowired
    DockerService dockerService;

    @Autowired
    MonnitService monnitService;

    @Autowired
    PelicanService pelicanService;

    @Autowired
    DataHoistService dataHoistService;

    @Autowired
    LocationService locationService;

    @Autowired
    KNXService knxService;

    @Autowired
    MeasuringInstrumentService measuringInstrumentService;

    @Autowired
    DocumentService documentService;

    @Autowired
    MediaService mediaService;

    @Autowired
    CheckListTemplateService checkListTemplateService;

    @Autowired
    RecordChecklistService recordChecklistService;

    @Autowired
    DaintreeService daintreeService;

    @Autowired
    GlobalQrcodeService globalQrcodeService;

    @Autowired
    DeviceConditionsService deviceConditionsService;

    @Autowired
    AlertProfileService alertProfileService;

    @Autowired
    EcobeeService ecobeeService;

    @Autowired
    ConnectedDevicesService connectedDevicesService;

    @Autowired
    SpecificationsService specificationsService;

    @Autowired
    UserActionLogService userActionLogService;

    @Autowired
    ModbusService modbusService;

    @Autowired
    IOCService iocService;

    @Autowired
    JobSchedulerService jobSchedulerService;

    @Autowired
    ScheduledJobRepository scheduledJobRepository;

    @Autowired
    ConditionUtils conditionUtils;

    @Autowired
    WebClientService webClientService;

    @Autowired
    AuthenticationUtils authenticationUtils;

    @Autowired
    AlertDowntimeScheduleService alertDowntimeScheduleService;

    @Autowired
    DeviceOnboardStatusRepository deviceOnboardStatusRepository;

//    @Autowired
//    VdmsService vdmsService;

    @Autowired
    DeviceSearchService deviceSearchService;

    @Autowired
    DeviceOnboardStatusAssigneeService deviceOnboardStatusAssigneeService;

    @Autowired
    GlobalInspectionRecordService globalInspectionRecordService;

    @Autowired
    GlobalChecklistService globalChecklistService;

    @Autowired
    SiemensService siemensService;

    @Autowired
    APICallService apiCallService;

    @Autowired
    PolyLensService polyLensService;

    @Autowired
    MqttService mqttService;

    @Autowired
    QrCodeService qrCodeService;

    @Autowired
    ClientQrCodeService clientQrCodeService;

    @Autowired
    NfcService nfcService;

    @Autowired
    ClientNfcService clientNfcService;

    @Autowired
    DataSource dataSource;

    @Autowired
    DeviceQueryRepository deviceQueryRepository;

    @Autowired
    ClientBarCodeService clientBarCodeService;

    @Autowired
    ArchivedRecordService archivedRecordService;

    @Autowired
    InspectionRecordService inspectionRecordService;

    @Autowired
    GlobalChecklistConditionsService globalChecklistConditionsService;

    @Autowired
    private QrCodeRepository qrCodeRepository;

    @Autowired
    private ClientQrCodeRepository clientQrCodeRepository;

    @Autowired
    private DeviceTypesRepository deviceTypesRepository;

    @Autowired
    private DeviceTypeService deviceTypeService;

    @Autowired
    private AssetMapperService assetMapperService;

    @Autowired
    DeviceLifecycleHistoryService deviceLifecycleHistoryService;

    @Autowired
    AiCallService aiCallService;

    @Autowired
    DeviceSpecificationRepository deviceSpecificationRepository;

    @Autowired
    DeviceInstalledAppsRepository deviceInstalledAppsRepository;

    @Autowired
    DeviceLifeCycleHistoryRepository deviceLifeCycleHistoryRepository;

    @Autowired
    DeviceSpecificationService deviceSpecificationService;

    @Autowired
    DeviceInstalledAppsService deviceInstalledAppsService;

    @Autowired
    HistoryRepository historyRepository;

    @Autowired
    InventoryDeviceRepository inventoryDeviceRepository;

    @Autowired
    DeviceNetworkSpecificationRepository deviceNetworkSpecificationRepository;

    @Autowired
    RemoteDesktopSessionRepository remoteDesktopSessionRepository;

    @Autowired
    private SyslogService syslogService;


    private String server_asset_images_absolute_path;
    private String server_asset_images_url;
    private String server_asset_ocr_images_absolute_path;
    private String server_asset_ocr_images_url;
    @Autowired
    private ConditionsService conditionsService;

    public String getServer_asset_images_absolute_path() {
        return server_asset_images_absolute_path;
    }

    public void setServer_asset_images_absolute_path(String server_asset_images_absolute_path) {
        this.server_asset_images_absolute_path = server_asset_images_absolute_path;
    }

    public String getServer_asset_images_url() {
        return server_asset_images_url;
    }

    public void setServer_asset_images_url(String server_asset_images_url) {
        this.server_asset_images_url = server_asset_images_url;
    }

    public String getServer_asset_ocr_images_absolute_path() {
        return server_asset_ocr_images_absolute_path;
    }

    public void setServer_asset_ocr_images_absolute_path(String server_asset_ocr_images_absolute_path) {
        this.server_asset_ocr_images_absolute_path = server_asset_ocr_images_absolute_path;
    }

    public String getServer_asset_ocr_images_url() {
        return server_asset_ocr_images_url;
    }

    public void setServer_asset_ocr_images_url(String server_asset_ocr_images_url) {
        this.server_asset_ocr_images_url = server_asset_ocr_images_url;
    }

    public Set<DeviceDTO> listAllDevicebyVdmsidAndDockerName(String username, String vdmsid, String dockername) {

        Set<DeviceDTO> devices = deviceRepository.listAllDevicebyVdmsidAndDockerName(vdmsid, dockername);
        for (DeviceDTO device : devices) {
            try {
                device.setIp_addresses(this.getDeviceIPAddressByDeviceId(device.getId()));
            } catch (Exception e) {
                System.out.println("Error getting device ip address " + e);
                System.out.println(e);
            }

        }
        return devices;
    }


    public Set<DeviceDTO> getfilterdevices(String username, String vdmsid, String dockername, String condition,
                                           String searchKey, Integer pageNo, Integer pageSize) {

        Integer virtual_device_type = null;
        Integer status = null;
        Integer monitor = 123;
        Integer asset_match_status = null;
        Integer offset = pageSize * (pageNo - 1);
        Integer assigned_status = null;

        System.out.println("outside all" + virtual_device_type + status + monitor + offset + pageNo);

        if (condition.equals("all")) {
            System.out.println("inside all" + virtual_device_type + status + monitor + offset + pageNo);
        } else if (condition.equals("unmonitored")) {
            System.out.println("outsidxse all" + virtual_device_type + status + monitor + offset + pageNo);
            monitor = 0;
        } else if (condition.equals("online")) {

            monitor = 1;
            status = 1;
            System.out.println("Inside Online" + monitor + status);

        } else if (condition.equals("offline")) {
            monitor = 1;
            status = 0;
        } else if (condition.equals("other")) {
            virtual_device_type = 123;
        } else if (condition.equals("matched")) {
            asset_match_status = 1;
        } else if (condition.equals("unmatched")) {
            asset_match_status = 0;
        } else if (condition.equals("verified")) {
            asset_match_status = 2;
        } else if (condition.equals("archived")) {
            asset_match_status = 3;
        } else if (condition.equals("assigned")) {
            assigned_status = 1;
        } else if (condition.equals("unassigned")) {
            assigned_status = 0;
        }


        Set<DeviceDTO> devices = deviceRepository.getfilterdevices(vdmsid, dockername, searchKey, virtual_device_type, status, monitor, asset_match_status, pageSize, offset,assigned_status);
        for (DeviceDTO device : devices) {
            try {
                device.setIp_addresses(this.getDeviceIPAddressByDeviceId(device.getId()));
                device.setSubsystems(new HashSet<>());
            } catch (Exception e) {
                System.out.println("Error getting device ip address " + e);
                System.out.println(e);
            }

        }
        return devices;


    }

    public void upsertDeviceListByVdmsIdAndDockerName(List<DeviceDTO> deviceList, String username, String vdmsid,
                                                      String dockername, String assignee) {
        try {
            for (DeviceDTO deviceDto : deviceList) {

                try {

                    if (deviceRepository.checkDeviceByDeviceId(deviceDto.getMac_address(), deviceDto.getVdms_id(),
                            deviceDto.getDocker_name()) != 0) {
                        try {
                            deviceRepository.updateDevice(deviceDto.getIp_address(), deviceDto.getStatus(),
                                    deviceDto.getLast_seen_on(), deviceDto.getDisplay_name(), deviceDto.getVendor(),
                                    deviceDto.getSnmp_parent(), deviceDto.getVdms_id(), deviceDto.getDocker_name(),
                                    deviceDto.getMac_address());
                            userActionLogService.addUserAction(username, "asset", "UPDATE", "A Device name:" + deviceDto.getDisplay_name() + " and id: " + deviceDto.getId() + " is updated for network " + deviceDto.getDocker_name(), "success", "asset_info", deviceDto.getId());

                        } catch (Exception e) {
                            userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to Update Device name: " + deviceDto.getDisplay_name() + " and id: " + deviceDto.getId() + " for network " + deviceDto.getDocker_name(), "failed", "asset_info", deviceDto.getId());
                        }
                    } else {
                        // if(deviceDto.getIs_virtual() == false)
                        deviceDto.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
                        if (deviceDto.getVirtual_device_type() == null || deviceDto.getVirtual_device_type() == 0) {
                            try {
                                deviceRepository.insertDevice(deviceDto.getId(), vdmsid,
                                        dockername, deviceDto.getIp_address(), deviceDto.getStatus(),
                                        deviceDto.getMac_address(), deviceDto.getLast_seen_on(),
                                        deviceDto.getDisplay_name(), deviceDto.getVendor(), deviceDto.getCreated_timestamp(),
                                        deviceDto.getUser_data_name(), deviceDto.getType(), deviceDto.getDescription(), deviceDto.getCustom_fields(), username, deviceDto.getAsset_group());
                                userActionLogService.addUserAction(username, "asset", "ADD", "A Device name: " + deviceDto.getUser_data_name() + " and id: " + deviceDto.getId() + " is added for network " + dockername, "success", "asset_info", deviceDto.getId());
                            } catch (Exception e) {
                                userActionLogService.addUserAction(username, "asset", "ADD", "Unable to Add Device name: " + deviceDto.getUser_data_name() + " and id: " + deviceDto.getId() + " for network " + dockername, "failed", "asset_info", deviceDto.getId());
                            }

                            // SnmpConfigurationDTO snmpconfigurationdto = new SnmpConfigurationDTO();
                            // //
                            // snmpconfigurationService.addSnmpConfigurationByDeviceId(snmpconfigurationdto,
                            // deviceDto.getId());
                            // snmpService.addSnmpConfigurationByDeviceId(snmpconfigurationdto,
                            // deviceDto.getId());
                        }
                    }

                } catch (Exception e) {
                    System.out.println(e.getStackTrace());
                }

            }

            try {
                socketservice.socketDeviceCount();
                socketservice.sockerDeviceCountByDocker(dockername,assignee);
            } catch (Exception e) {
                // TODO: handle exception
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }

    }

    private DeviceOnboardStatusDTO setDeviceOnboardStatusFields(DeviceOnboardStatusDTO deviceOnboardStatusDTO, DeviceOnboardStatusDTO onboard_data) {

        if (onboard_data.getField_status() != null) {
            deviceOnboardStatusDTO.setField_status(onboard_data.getField_status());
        }
        if (onboard_data.getGeolocation_status() != null) {
            deviceOnboardStatusDTO.setGeolocation_status(onboard_data.getGeolocation_status());
        }
        if (onboard_data.getTag_status() != null) {
            deviceOnboardStatusDTO.setTag_status(onboard_data.getTag_status());
        }
        if (onboard_data.getImage_status() != null) {
            deviceOnboardStatusDTO.setImage_status(onboard_data.getImage_status());
        }
        return deviceOnboardStatusDTO;
    }

    public DeviceDTO editDeviceByDeviceID(String username, String vdmsid, String dockername, String device_id, DeviceDTO devicedto, HttpServletRequest httpServletRequest, String assignee) throws JSONException, IOException {
        log.info("editDeviceByDeviceID, Params: devicedto: {}, endpoint : {}", devicedto, httpServletRequest.getRequestURI());
        DeviceDTO existingDevice = this.getDeviceByDeviceId(username, vdmsid, dockername, device_id);
        String previous_product_id = existingDevice.getProduct_id();
        String previous_subsystem_parent_id = existingDevice.getSubsystem_parent_id();
        String device_name = devicedto.getUser_data_name() == null || devicedto.getUser_data_name().equals("") ? devicedto.getDisplay_name() : devicedto.getUser_data_name();
        log.info("Existing device Dto : {}", existingDevice);
        try {
            if (devicedto.getOnboard_data() == null) {
                devicedto.setOnboard_data(new DeviceOnboardStatusDTO());
            }

            this.getProductDetails(username, vdmsid, dockername, device_id, devicedto.getProduct_id(), previous_product_id);

//        if (devicedto.getBuilding_object() != null) {
//            buildingService.upsertBuildingByVdmsId(devicedto.getBuilding_object(), vdmsid);
//        }

            if (devicedto.getLocation_id() != null) {
                String previous_location_id = existingDevice.getLocation_id();
                if (!devicedto.getLocation_id().equals(previous_location_id)) {
                    String position = locationService.getPositionByLocationId(devicedto.getLocation_id());
                    // Update latitude and longitude to null
                    this.updateDeviceCoordinates(null, null, position, device_id);
                    devicedto.getOnboard_data().setGeolocation_status(0);
                }
                //if location is tagged then match status is 2 - verified
                devicedto.setAsset_match_status(2);
                devicedto.setLocation_status(null);

            } else {
                // Update latitude and longitude to null
                this.updateDeviceCoordinates(null, null, null, device_id);

                //Update Asset Match Status
                if (devicedto.getAsset_match_status() == null) {

                    if (existingDevice.getAsset_match_status() != null) {
                        if (existingDevice.getAsset_match_status() == 2) {
                            devicedto.setAsset_match_status(1);
                        } else {
                            devicedto.setAsset_match_status(existingDevice.getAsset_match_status());
                        }
                    }
                }

                if (devicedto.getOnboard_data() != null && devicedto.getOnboard_data().getGeolocation_status() == null) {
//                    if (existingDevice.getOnboard_data().getGeolocation_status() == 1) {
//                        devicedto.getOnboard_data().setGeolocation_status(0);
//                    }
                    if (devicedto.getLocation_status() != null && devicedto.getLocation_status().equals("no_fixed_location")) {
                        devicedto.getOnboard_data().setGeolocation_status(1);
                    } else {
                        devicedto.getOnboard_data().setGeolocation_status(0);
                    }
                }
            }
            if (devicedto.getId() != null && devicedto.getId().equals(devicedto.getSubsystem_parent_id())) {
                devicedto.setSubsystem_parent_id(null);
            }

            log.info("location id: : {}", devicedto.getLocation_id());

            deviceRepository.editDeviceByDeviceID(device_id, vdmsid, devicedto.getDocker_name(), devicedto.getMonitor(), devicedto.getNetwork_layer(),
                    devicedto.getUser_data_model(), devicedto.getUser_data_name(), devicedto.getType(),
                    devicedto.getUser_data_vendor(), devicedto.getParent(), devicedto.getRemote_access(),
                    devicedto.getWarranty(), devicedto.getProduct_id(), devicedto.getLocation_id(),
                    devicedto.getEmail_alert(), devicedto.getSms_alert(), devicedto.getPopup_notification(),
                    devicedto.getSerial_number(), devicedto.getLocal_vendor_email_alert(), devicedto.getLocal_vendor_sms_alert(),
                    devicedto.getSubsystem_parent_id(), devicedto.getCustom_fields(), devicedto.getDescription(), devicedto.getAsset_match_status(), devicedto.getAsset_group(), devicedto.getCategory(), devicedto.getSub_category(), devicedto.getLocation_status(),
                    devicedto.getCost_value(), devicedto.getAssigned_user_email(), devicedto.getAi_call(), devicedto.getCost_unit(), devicedto.getIs_dnd_enabled(), devicedto.getOperational_status(), devicedto.getAdc_json() );

            if (devicedto.getAsset_group() != null)  {

                // CASE 1:  user is removed (A → null)
                if (existingDevice.getAssigned_user_email() != null &&
                        devicedto.getAssigned_user_email() == null) {
                    DeviceLifecycleHistoryDTO deviceLifecycleHistoryDTO = new DeviceLifecycleHistoryDTO();
                    deviceLifecycleHistoryDTO.setDevice_id(existingDevice.getId());

                    deviceLifecycleHistoryDTO.setOperational_status(devicedto.getOperational_status());

                    deviceLifecycleHistoryDTO.setAssigned_user_id(null);
                    deviceLifecycleHistoryDTO.setAssigned_by_user_id(username);
                    deviceLifecycleHistoryService.addDeviceHistory(username, vdmsid, deviceLifecycleHistoryDTO, null);

                    // CASE 2: user changed (A → B)
                } else if (!Objects.equals(devicedto.getAssigned_user_email(), existingDevice.getAssigned_user_email()) &&
                        existingDevice.getAssigned_user_email() != null &&
                        devicedto.getAssigned_user_email() != null) {

                    DeviceLifecycleHistoryDTO deviceLifecycleHistoryDTO = new DeviceLifecycleHistoryDTO();
                    deviceLifecycleHistoryDTO.setDevice_id(existingDevice.getId());
                    deviceLifecycleHistoryDTO.setOperational_status(devicedto.getOperational_status());
                    deviceLifecycleHistoryDTO.setAssigned_user_id(devicedto.getAssigned_user_email());
                    deviceLifecycleHistoryDTO.setAssigned_by_user_id(username);
                    deviceLifecycleHistoryService.addDeviceHistory(username, vdmsid, deviceLifecycleHistoryDTO,null);

                    // CASE 3: user assigned (null → A)
                } else if (existingDevice.getAssigned_user_email() == null &&
                        devicedto.getAssigned_user_email() != null) {

                    DeviceLifecycleHistoryDTO deviceLifecycleHistoryDTO = new DeviceLifecycleHistoryDTO();
                    deviceLifecycleHistoryDTO.setDevice_id(existingDevice.getId());
                    deviceLifecycleHistoryDTO.setOperational_status(devicedto.getOperational_status());
                    deviceLifecycleHistoryDTO.setAssigned_user_id(devicedto.getAssigned_user_email());
                    deviceLifecycleHistoryDTO.setAssigned_by_user_id(username);
                    deviceLifecycleHistoryService.addDeviceHistory(username, vdmsid, deviceLifecycleHistoryDTO,null);

                }

                // CASE 4: assignee same, but operational status changed
                else if (Objects.equals(devicedto.getAssigned_user_email(), existingDevice.getAssigned_user_email()) &&
                        !Objects.equals(devicedto.getOperational_status(), existingDevice.getOperational_status())) {

                    DeviceLifecycleHistoryDTO deviceLifecycleHistoryDTO = new DeviceLifecycleHistoryDTO();
                    deviceLifecycleHistoryDTO.setDevice_id(existingDevice.getId());
                    deviceLifecycleHistoryDTO.setOperational_status(devicedto.getOperational_status());
                    deviceLifecycleHistoryDTO.setAssigned_user_id(devicedto.getAssigned_user_email());
                    deviceLifecycleHistoryDTO.setAssigned_by_user_id(username);
                    deviceLifecycleHistoryService.addDeviceHistory(username, vdmsid, deviceLifecycleHistoryDTO, null);
                }
            }
            if (devicedto.getOnboard_data() != null) {
                if (((devicedto.getName() != null && !(devicedto.getName().isBlank())) || (devicedto.getUser_data_name() != null && !(devicedto.getUser_data_name().isBlank()))) &&
                        ((devicedto.getVendor() != null && !(devicedto.getVendor().isBlank())) || (devicedto.getUser_data_vendor() != null && !(devicedto.getUser_data_vendor().isBlank()))) &&
                        ((devicedto.getModel() != null && !(devicedto.getModel().isBlank())) || (devicedto.getUser_data_model() != null && !(devicedto.getUser_data_model().isBlank())))) {
                    devicedto.getOnboard_data().setField_status(1);
                } else {
                    // Only downgrade if previously complete
                    Integer existingStatus = existingDevice.getOnboard_data().getField_status();
                    if (existingStatus != null && existingStatus == 1) {
                        devicedto.getOnboard_data().setField_status(0);
                    }
                }
            }

            if (devicedto.getOnboard_data() != null) {
                log.info("edit onboard data: : {}", devicedto.getOnboard_data());
                this.updateAssetOnboardData(username, vdmsid, device_id, devicedto.getOnboard_data(), existingDevice.getOnboard_status());
            }
            log.info("device id : {}", devicedto.getId());
            log.info("device location : {}", devicedto.getLocation_id());
            userActionLogService.addUserAction(username, "asset", "UPDATE", "A Device name: " + device_name + " and id: " + devicedto.getId() + " is updated for network " + devicedto.getDocker_name() + (devicedto.getLocation_id() != null && devicedto.getLocation() != null ? ", Location id: " + devicedto.getLocation_id() + ", Location name: " + devicedto.getLocation() : ""), "success", "asset_info", devicedto.getId());

        } catch (Exception e) {
            log.error("Exception. Params: devicedto: {}, endpoint : {}", devicedto, httpServletRequest.getRequestURI(), e);
            userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to Update Device name: " + device_name + " and id: " + devicedto.getId() + " for network " + devicedto.getDocker_name() + (devicedto.getLocation_id() != null && devicedto.getLocation() != null ? ", Location id: " + devicedto.getLocation_id() + ", Location name: " + devicedto.getLocation() : ""), "failed", "asset_info", devicedto.getId());
        }
        try {
            socketservice.socketDeviceCount();
            socketservice.sockerDeviceCountByDocker(dockername,assignee);
        } catch (Exception e) {
            // TODO: handle exception
        }

        try {
            this.updateDeviceSnmpCount(device_id);
            this.updateDeviceNotesCount(device_id);
            this.updateDeviceSnmpStatusByDeviceId(device_id);
            this.updateParentSubsystemCount(devicedto.getSubsystem_parent_id());
            this.updateParentSubsystemCount(previous_subsystem_parent_id);
        } catch (Exception e) {
            // TODO: handle exception
        }

        try {
            Set<String> device_ids = Collections.singleton(device_id);
            this.updateDeviceProductPortStatus(vdmsid, device_ids);
        } catch (Exception e) {
            System.out.println(e);
        }

        return this.getDeviceByDeviceId(username, vdmsid, devicedto.getDocker_name(), device_id);
    }


    public void tagProductImages(ProductDTO productdto) throws IOException {
        Integer count = product_detailsService.checkProductId(productdto.getId());
        if (count == 0) {
            String image_url_1 = null, image_url_2 = null, image_url_3 = null;
            String modified_image_url_1 = null, modified_image_url_2 = null, modified_image_url_3 = null;
            byte[] image = null;
//			 String directory = "/home/rajath/Desktop/ts_images/";
            String directory = "/home/sclera/images/";
            if (productdto.getImage_url_1() != null) {
                image_url_1 = addImageByUrl(productdto.getImage_url_1(), image, directory, productdto.getId() + "_1");
                String extension = getImageExtensionByImageUrl(productdto.getImage_url_1());
                modified_image_url_1 = "http://localhost:8888/images/" + productdto.getId() + "_1." + extension;
            }
            if (productdto.getImage_url_2() != null) {
                image_url_2 = addImageByUrl(productdto.getImage_url_2(), image, directory, productdto.getId() + "_2");
                String extension = getImageExtensionByImageUrl(productdto.getImage_url_2());
                modified_image_url_2 = "http://localhost:8888/images/" + productdto.getId() + "_2." + extension;
            }
            if (productdto.getImage_url_3() != null) {
                image_url_3 = addImageByUrl(productdto.getImage_url_3(), image, directory, productdto.getId() + "_3");
                String extension = getImageExtensionByImageUrl(productdto.getImage_url_3());
                modified_image_url_3 = "http://localhost:8888/images/" + productdto.getId() + "_3." + extension;
            }
            System.out.println(productdto.getId());
            System.out.println(image_url_1);
            System.out.println(image_url_2);
            System.out.println(image_url_3);

            product_detailsService.addProductImages(productdto.getId(), modified_image_url_1, modified_image_url_2,
                    modified_image_url_3, productdto.getImage_url_1(), productdto.getImage_url_1(), productdto.getImage_url_3());
        }
    }

//	94b55ef5-9d0a-11eb-a3a7-19d1a4fc06a3

    public void retagProductImages(ProductDTO productdto) throws IOException {
        String image_url_1 = null, image_url_2 = null, image_url_3 = null;
        String modified_image_url_1 = null, modified_image_url_2 = null, modified_image_url_3 = null;
        byte[] image = null;
//		 String directory = "/home/rajath/Desktop/ts_images/";
        String directory = "/home/sclera/images/";
        if (productdto.getImage_url_1() != null) {
            image_url_1 = addImageByUrl(productdto.getImage_url_1(), image, directory, productdto.getId() + "_1");
            String extension = getImageExtensionByImageUrl(productdto.getImage_url_1());
            modified_image_url_1 = "http://localhost:8888/images/" + productdto.getId() + "_1." + extension;
        }
        if (productdto.getImage_url_2() != null) {
            image_url_2 = addImageByUrl(productdto.getImage_url_2(), image, directory, productdto.getId() + "_2");
            String extension = getImageExtensionByImageUrl(productdto.getImage_url_1());
            modified_image_url_2 = "http://localhost:8888/images/" + productdto.getId() + "_2." + extension;
        }
        if (productdto.getImage_url_3() != null) {
            image_url_3 = addImageByUrl(productdto.getImage_url_3(), image, directory, productdto.getId() + "_3");
            String extension = getImageExtensionByImageUrl(productdto.getImage_url_1());
            modified_image_url_3 = "http://localhost:8888/images/" + productdto.getId() + "_3." + extension;
        }
        System.out.println(productdto.getId());
        System.out.println(image_url_1);
        System.out.println(image_url_2);
        System.out.println(image_url_3);

        product_detailsService.addProductImages(productdto.getId(), modified_image_url_1, modified_image_url_2,
                modified_image_url_3, productdto.getImage_url_1(), productdto.getImage_url_1(), productdto.getImage_url_3());
    }

    public String addImageByUrl(String link, byte[] image, String directory, String id) throws IOException {
        image = webClientService.getImageBytesByUrl(link);
        String extension = getImageExtensionByImageUrl(link);
        return addFileToServer(image, directory, id, extension);
    }


    public String getImageExtensionByImageUrl(String image_url) {
        return image_url.substring(image_url.lastIndexOf(".") + 1, image_url.length());

    }

    public String addFileToServer(byte[] image, String directory, String file_name, String file_extension)
            throws IOException {
        // final String dir = "http://dbapi.sclera.com:/vendor_products/";
        final String dir = "file://" + directory;

        if (image != null) {
            Path path = Paths.get(directory + file_name + "." + file_extension);
            Files.write(path, image);
            return dir + file_name + "." + file_extension;
        } else {
            System.out.println("Error");
            return null;
        }
    }

    public void removeFileFromServer(String absolute_path, String file_name, String file_extension) {
        File file = new File(absolute_path + file_name + "." + file_extension);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("File deleted successfully");
            } else {
                System.out.println("Failed to delete the file");
            }
        }
    }

    public String linkVendorByVendorIdAndDeviceId(String username, String vdmsid, String dockername, String
            device_id, PhonebookAddressDto phonebookaddressdto, String vendor_type, HttpServletRequest httpServletRequest) {
        log.info("linkVendorByVendorIdAndDeviceId, Params: phonebookaddressdto: {}, device_id: {}, vendor_type: {}, endpoint : {}", phonebookaddressdto, device_id, vendor_type, httpServletRequest.getRequestURI());
        String id = utilsService.upsertPhoneAddressById(username, vdmsid, dockername, phonebookaddressdto);
        if (id != null) {
            try {
                log.info("vendor type : {}, phone address id : {}, device id : {}", vendor_type, id, device_id);
                deviceRepository.linkVendorByVendorIdAndDeviceId(vendor_type, id, device_id);
                userActionLogService.addUserAction(username, "asset", "UPDATE", "A " + vendor_type + " Vendor is linked for device id: " + device_id, "success", "vendor", device_id);
            } catch (Exception e) {
                log.error("Exception. Params: phoneaccount: {}, device_id: {}, vendor_type: {}, endpoint : {}", phonebookaddressdto, device_id, vendor_type, httpServletRequest.getRequestURI(), e);
                userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to link a " + vendor_type + " Vendor for device id: " + device_id, "failed", "vendor", device_id);
            }
            return id;

        } else {
            try {
                log.info("vendor type : {}, phone address id : {}, device id : {}", vendor_type, id, device_id);
                deviceRepository.linkVendorByVendorIdAndDeviceId(vendor_type, phonebookaddressdto.getId(), device_id);
                userActionLogService.addUserAction(username, "asset", "UPDATE", "A " + vendor_type + "Vendor is linked for device id: " + device_id, "success", "vendor", device_id);
            } catch (Exception e) {
                log.error("Exception. Params: phoneaccount: {}, device_id: {}, vendor_type: {}, endpoint : {}", phonebookaddressdto, device_id, vendor_type, httpServletRequest.getRequestURI(), e);
                userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to link a " + vendor_type + " Vendor for device id: " + device_id, "failed", "vendor", device_id);
            }
            return phonebookaddressdto.getId();
        }
    }

    public void unlinkVendorByVendorIdAndDeviceId(String username, String dockername, String phoneaccount,
                                                  String device_id, String vendor_type, HttpServletRequest httpServletRequest) {
        log.info("unlinkVendorByVendorIdAndDeviceId, Params: phoneaccount: {}, device_id: {}, vendor_type: {}, endpoint : {}", phoneaccount, device_id, vendor_type, httpServletRequest.getRequestURI());
        try {
            log.info("phoneaccount: {}, device_id: {}, vendor_type: {}", phoneaccount, device_id, vendor_type);
            deviceRepository.unlinkVendorByVendorIdAndDeviceId(phoneaccount, vendor_type, device_id);
            userActionLogService.addUserAction(username, "asset", "UPDATE", "A " + vendor_type + " Vendor is unlinked from device id: " + device_id, "success", "vendor", device_id);
        } catch (Exception e) {
            log.error("Exception. Params: phoneaccount: {}, device_id: {}, vendor_type: {}, endpoint : {}", phoneaccount, device_id, vendor_type, httpServletRequest.getRequestURI(), e);
            userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to unlink a " + vendor_type + " Vendor  from device id: " + device_id, "failed", "vendor", device_id);
        }
    }

    public void multiDeviceUpdate(String username, String vdmsid, String dockername,
                                  Set<MultiDeviceDTO> multidevicedtos, HttpServletRequest httpServletRequest, String assignee) throws JSONException, IOException {
        log.info("multiDeviceUpdate, Params: multidevicedtos: {}, endpoint : {}", multidevicedtos, httpServletRequest.getRequestURI());
        for (MultiDeviceDTO multidevicedto : multidevicedtos) {
            log.info("multidevicedto : {}", multidevicedto);
            DeviceDTO existingDevice = this.getDeviceByDeviceId(username, vdmsid, dockername, multidevicedto.getId());
            String previous_product_id = existingDevice.getProduct_id();
            String device_name = multidevicedto.getUser_data_name() == null || multidevicedto.getUser_data_name().equals("") ? existingDevice.getDisplay_name() : multidevicedto.getUser_data_name();
            LocationDTO locationDTO = null;
            try {

                if (multidevicedto.getOnboard_data() == null) {
                    multidevicedto.setOnboard_data(new DeviceOnboardStatusDTO());
                }

                this.updateVendorPhoneBookDetails(username, vdmsid, dockername, multidevicedto);


//            if (multidevicedto.getBuilding_object() != null) {
//                buildingService.upsertBuildingByVdmsId(multidevicedto.getBuilding_object(), vdmsid);
//            }

                if (multidevicedto.getLocation_id() != null) {

                    String previous_location_id = existingDevice.getLocation_id();

                    if (!multidevicedto.getLocation_id().equals(previous_location_id)) {

                        String position = locationService.getPositionByLocationId(multidevicedto.getLocation_id());

                        // Update latitude and longitude to null
                        this.updateDeviceCoordinates(null, null, position, multidevicedto.getId());
                        multidevicedto.getOnboard_data().setGeolocation_status(0);
                    }

                    //if location is tagged then match status is 2 - verified
                    multidevicedto.setAsset_match_status(2);
                } else {
                    // Update latitude and longitude to null
                    this.updateDeviceCoordinates(null, null, null, multidevicedto.getId());

                    //Update Asset Match Status
                    if (multidevicedto.getAsset_match_status() == null) {

                        if (existingDevice.getAsset_match_status() != null) {
                            if (existingDevice.getAsset_match_status() == 2) {
                                multidevicedto.setAsset_match_status(1);
                            } else {
                                multidevicedto.setAsset_match_status(existingDevice.getAsset_match_status());
                            }
                        }
                    }
                    multidevicedto.getOnboard_data().setGeolocation_status(0);

                }

                if (multidevicedto.getId() != null && multidevicedto.getId().equals(multidevicedto.getSubsystem_parent_id())) {
                    multidevicedto.setSubsystem_parent_id(null);
                }

                this.getProductDetails(username, vdmsid, dockername, multidevicedto.getId(), multidevicedto.getProduct_id(), previous_product_id);

                MultiDeviceDTO deviceVendors = this.setProductVendorsForDevices(multidevicedto, previous_product_id);

                log.info("device name : {}", multidevicedto.getUser_data_name());
                deviceRepository.multiDeviceUpdateByDeviceId(multidevicedto.getId(), multidevicedto.getUser_data_name(),
                        multidevicedto.getUser_data_model(), multidevicedto.getUser_data_vendor(), multidevicedto.getType(),
                        multidevicedto.getNetwork_layer(), multidevicedto.getLocation_id(), multidevicedto.getParent(),
                        multidevicedto.getWarranty(), multidevicedto.getMonitor(), multidevicedto.getRemote_access(),
                        multidevicedto.getProduct_id(), deviceVendors.getGlobal_vendor_id(),
                        deviceVendors.getLocal_vendor_id(), deviceVendors.getOther_vendor_1_id(),
                        deviceVendors.getOther_vendor_2_id(), deviceVendors.getOther_vendor_3_id(),
                        multidevicedto.getEmail_alert(), multidevicedto.getSms_alert(),
                        multidevicedto.getPopup_notification(), multidevicedto.getSerial_number(), multidevicedto.getLocal_vendor_email_alert(),
                        multidevicedto.getLocal_vendor_sms_alert(), multidevicedto.getDocker_name(), multidevicedto.getSubsystem_parent_id(),
                        multidevicedto.getCustom_fields(), multidevicedto.getDescription(), multidevicedto.getAsset_match_status(), multidevicedto.getAsset_group(), multidevicedto.getCategory(), multidevicedto.getSub_category());

                if (multidevicedto.getLocation_id() != null) {
                    locationDTO = locationService.getLocationByLocationId(multidevicedto.getLocation_id());
                }
                userActionLogService.addUserAction(username, "asset", "UPDATE", "A Device  name: " + device_name + " and id: " + multidevicedto.getId() + " is updated for network " + multidevicedto.getDocker_name() + (multidevicedto.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + multidevicedto.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_info", multidevicedto.getId());

                if (multidevicedto.getOnboard_data() != null && multidevicedto.getOnboard_data().getField_status() == null) {
                    if (((existingDevice.getName() != null && !(existingDevice.getName().isBlank())) || (multidevicedto.getUser_data_name() != null && !(multidevicedto.getUser_data_name().isBlank()))) &&
                            ((existingDevice.getVendor() != null && !(existingDevice.getVendor().isBlank())) || (multidevicedto.getUser_data_vendor() != null && !(multidevicedto.getUser_data_vendor().isBlank()))) &&
                            ((existingDevice.getModel() != null && !(existingDevice.getModel().isBlank())) || (multidevicedto.getUser_data_model() != null && !(multidevicedto.getUser_data_model().isBlank())))) {
                        multidevicedto.getOnboard_data().setField_status(1);
                    } else if (existingDevice.getOnboard_data().getField_status() == 1) {
                        multidevicedto.getOnboard_data().setField_status(0);

                    }
                }

                if (multidevicedto.getOnboard_data() != null) {
                    log.info("edit onboard data : {}", multidevicedto.getOnboard_data());
                    this.updateAssetOnboardData(username, vdmsid, multidevicedto.getId(), multidevicedto.getOnboard_data(), existingDevice.getOnboard_status());
                }

            } catch (Exception e) {
                log.error("Exception. Params: multidevicedtos: {}, endpoint : {}", multidevicedtos, httpServletRequest.getRequestURI(), e);
                userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to Update Device name: " + device_name + " and id: " + multidevicedto.getId() + " for network " + multidevicedto.getDocker_name() + (multidevicedto.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + multidevicedto.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_info", multidevicedto.getId());
            }
        }
        try {
            socketservice.socketDeviceCount();
            socketservice.sockerDeviceCountByDocker(dockername,assignee);
        } catch (Exception e) {
            // TODO: handle exception
        }

        Set<String> device_ids = new HashSet<>();

        // update snmp and notes count
        for (MultiDeviceDTO multidevicedto : multidevicedtos) {
            try {
                String device_id = multidevicedto.getId();
                device_ids.add(device_id);

                this.updateDeviceSnmpCount(device_id);
                this.updateDeviceNotesCount(device_id);
                this.updateDeviceSnmpStatusByDeviceId(device_id);
                this.updateParentSubsystemCount(multidevicedto.getSubsystem_parent_id());
                this.updateParentSubsystemCount(multidevicedto.getPrevious_subsystem_parent_id());
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        //update device product port status
        try {
            this.updateDeviceProductPortStatus(vdmsid, device_ids);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public Set<DeviceDTO> quickUpdate(String username, String vdmsid, String dockername, TagDeviceOrLocationDTO tagDeviceOrLocationDTO, HttpServletRequest httpServletRequest, String assignee) throws IOException {
        log.info("quickUpdate, Params: tagDeviceOrLocationDTO: {}, endpoint : {}", tagDeviceOrLocationDTO, httpServletRequest.getRequestURI());
        Integer select_all_status = tagDeviceOrLocationDTO.getSelect_all_status();
        JSONObject filter_object = tagDeviceOrLocationDTO.getFilter_object();
        JSONObject general_object = tagDeviceOrLocationDTO.getGeneral_object();
        Set<DeviceDTO> devices = new HashSet<>();

        try {
            if (select_all_status == 1) {
                if (filter_object != null && !filter_object.isEmpty()) {
                    String condition = filter_object.getString("condition");
                    Integer onboard_status = filter_object.getInteger("onboard_status");

                    if (onboard_status == null) {
                        onboard_status = 123;
                    }

                    JSONObject search_sort_filter_object = tagDeviceOrLocationDTO.getFilter_object().getJSONObject("search_sort_filter");
                    if (condition != null && search_sort_filter_object == null) {
                        devices = this.getAllSubsystemDevices(username, vdmsid, dockername, null, condition);

                    } else {
                        devices = deviceSearchService.multipleKeywordSearchSortFilterDevicesForAssetExport(username, vdmsid, dockername, condition, search_sort_filter_object, onboard_status);

                    }
                }

            } else if (select_all_status == 0) {
                if (general_object != null && !general_object.isEmpty()) {
                    Set<String> deviceIds = utils.getJSONArrayFromJSONStringForSet(general_object.getJSONArray("ids").toJSONString(), String.class);
                    devices = this.updateDeviceDetailsByIds(username, vdmsid, dockername, deviceIds);

                }
            }

            if (devices.size() > 0) {
                if (general_object != null && !general_object.isEmpty()) {
                    DevicesDTO updateDevice = utils.getJSONObjectFromString(general_object.getJSONObject("device").toJSONString(), DevicesDTO.class);
                    log.info("devices updated : {}", devices);
                    this.quickUpdateForDevices(username, vdmsid, dockername, updateDevice, devices, assignee);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Exception. Params: tagDeviceOrLocationDTO: {}, endpoint : {}", tagDeviceOrLocationDTO, httpServletRequest.getRequestURI(), e);
        }
        return devices;

    }

    private void quickUpdateForDevices(String username, String vdmsid, String dockername, DevicesDTO updateDevice, Set<DeviceDTO> devices, String assignee) {
        MultiDeviceDTO multiDeviceDTO = this.mapProductVendorsToMultideviceDTO(updateDevice);
        multiDeviceDTO = this.updateVendorPhoneBookDetails(username, vdmsid, dockername, multiDeviceDTO);
        LocationDTO locationDTO = null;

        for (DeviceDTO existingDevice : devices) {

            String previous_product_id = existingDevice.getProduct_id();
            String previous_subsystem_parent_id = existingDevice.getSubsystem_parent_id();
            String device_name = updateDevice.getUser_data_name() == null || updateDevice.getUser_data_name().equals("") ? existingDevice.getUser_data_name() : updateDevice.getUser_data_name();
            String network_name = updateDevice.getDocker_name();
            String location_id = updateDevice.getLocation_id() == null || updateDevice.getLocation_id().equals("") ? existingDevice.getLocation_id() : updateDevice.getLocation_id();

            String previous_subsystem_parent_id_new = null;
            DeviceDTO existingDeviceFromParent = this.getDeviceByDeviceId(username, vdmsid, dockername, existingDevice.getSubsystem_parent_id());
            if (existingDeviceFromParent != null) {
                previous_subsystem_parent_id_new = existingDeviceFromParent.getSubsystem_parent_id();
                String existingDeviceId = existingDeviceFromParent.getId();

                if (existingDevice != null) {
                    if (existingDevice.getId() != null && existingDevice.getId().equals(existingDevice.getSubsystem_parent_id())) {
                        deviceRepository.updateSubsystemParentDevice(existingDevice.getId(), null);
                    } else if (existingDevice.getId() != null && existingDevice.getId().equals(previous_subsystem_parent_id_new) && existingDevice.getSubsystem_parent_id().equals(existingDeviceId)) {
                        deviceRepository.updateSubsystemParentDevice(existingDevice.getId(), null);
                    }
                }
            }

            if (updateDevice.getOnboard_data() == null) {
                updateDevice.setOnboard_data(new DeviceOnboardStatusDTO());
            }

            try {
                if (updateDevice.getDocker_name() == null) {
                    network_name = existingDevice.getDocker_name();
                }
                this.getProductDetails(username, vdmsid, dockername, existingDevice.getId(), updateDevice.getProduct_id(), previous_product_id);


                if (updateDevice.getLocation_id() != null) {
                    if (!(updateDevice.getLocation_id().equalsIgnoreCase("null")) &&
                            !(updateDevice.getLocation_id().equalsIgnoreCase(""))) {

                        String previous_location_id = existingDevice.getLocation_id();

                        if (!updateDevice.getLocation_id().equals(previous_location_id)) {

                            String position = locationService.getPositionByLocationId(updateDevice.getLocation_id());

                            // Update latitude and longitude to null
                            this.updateDeviceCoordinates(null, null, position, existingDevice.getId());

                            updateDevice.getOnboard_data().setGeolocation_status(0);
                        }

                        //if location is tagged then match status is 2 - verified
                        updateDevice.setAsset_match_status(2);
                    } else if (updateDevice.getLocation_id().equalsIgnoreCase("null") ||
                            updateDevice.getLocation_id().equalsIgnoreCase("")) {
                        // Update latitude and longitude to null
                        this.updateDeviceCoordinates(null, null, null, existingDevice.getId());

                        //Update Asset Match Status
                        if (updateDevice.getAsset_match_status() == null) {

                            if (existingDevice.getAsset_match_status() != null) {
                                if (existingDevice.getAsset_match_status() == 2) {
                                    updateDevice.setAsset_match_status(1);
                                } else {
                                    updateDevice.setAsset_match_status(existingDevice.getAsset_match_status());
                                }
                            }
                        }

                        if (updateDevice.getOnboard_data() != null && updateDevice.getOnboard_data().getGeolocation_status() == null) {
                            if (existingDevice.getOnboard_data().getGeolocation_status() == 1) {
                                updateDevice.getOnboard_data().setGeolocation_status(0);
                            }
                        }

                    }
                }

                String custom_fields = null;
                if (updateDevice.getCustom_fields() != null) {
                    JSONArray customField = JSON.parseArray(updateDevice.getCustom_fields());// convert string to JSON
                    DeviceDTO custom = this.updateCustomFields(existingDevice.getId(), customField);
                    custom_fields = custom.getCustom_fields();
                }

                String docker_name = existingDevice.getDocker_name();
                if (existingDevice.getVirtual_device_type() != null && existingDevice.getVirtual_device_type() != 0) {

                    if (updateDevice.getDocker_name() != null) {
                        docker_name = updateDevice.getDocker_name();
                    }
                }

                deviceRepository.quickUpdate(existingDevice.getId(), updateDevice.getUser_data_name(), updateDevice.getUser_data_model(),
                        updateDevice.getUser_data_vendor(), updateDevice.getType(), updateDevice.getWarranty(),
                        updateDevice.getNetwork_layer(), updateDevice.getLocation_id(), updateDevice.getParent(),
                        updateDevice.getProduct_id(), updateDevice.getMonitor(), updateDevice.getRemote_access(),
                        multiDeviceDTO.getGlobal_vendor_id(), multiDeviceDTO.getLocal_vendor_id(), multiDeviceDTO.getOther_vendor_1_id(), multiDeviceDTO.getOther_vendor_2_id(), multiDeviceDTO.getOther_vendor_3_id(),
                        updateDevice.getEmail_alert(), updateDevice.getSms_alert(), updateDevice.getPopup_notification(), updateDevice.getLocal_vendor_email_alert(),
                        updateDevice.getLocal_vendor_sms_alert(), updateDevice.getSubsystem_parent_id(), updateDevice.getDescription(), updateDevice.getAsset_match_status(), custom_fields, docker_name,
                        updateDevice.getAsset_group(), updateDevice.getCategory(), updateDevice.getSub_category());

                if (updateDevice.getOnboard_data() != null && updateDevice.getOnboard_data().getField_status() == null) {
                    if (existingDevice != null) {
                        if (((existingDevice.getName() != null && !existingDevice.getName().isBlank()) || (existingDevice.getUser_data_name() != null && !existingDevice.getUser_data_name().isBlank()) ||
                                (updateDevice.getUser_data_name() != null && !updateDevice.getUser_data_name().isBlank())) && ((existingDevice.getVendor() != null && !existingDevice.getVendor().isBlank()) ||
                                (existingDevice.getUser_data_vendor() != null && !existingDevice.getUser_data_vendor().isBlank()) || (updateDevice.getUser_data_vendor() != null && !updateDevice.getUser_data_vendor().isBlank())) &&
                                ((existingDevice.getModel() != null && !existingDevice.getModel().isBlank()) || (updateDevice.getUser_data_model() != null && !updateDevice.getUser_data_model().isBlank()) ||
                                        (existingDevice.getUser_data_model() != null && !existingDevice.getUser_data_model().isBlank()))) {
                            updateDevice.getOnboard_data().setField_status(1);
                        }
                    }
                }

                if (updateDevice.getOnboard_data() != null) {
                    System.out.println("quick update onboard data : " + updateDevice.getOnboard_data());
                    this.updateAssetOnboardData(username, vdmsid, existingDevice.getId(), updateDevice.getOnboard_data(), existingDevice.getOnboard_status());
                }

                if (location_id != null) {
                    locationDTO = locationService.getLocationByLocationId(location_id);
                }
                userActionLogService.addUserAction(username, "asset", "UPDATE", "A Device  name: " + device_name + " and id: " + existingDevice.getId() + " is updated for network " + network_name + (location_id != null && locationDTO.getName() != null ? ", Location id: " + location_id + ", Location name: " + locationDTO.getName() : ""), "success", "asset_info", existingDevice.getId());

            } catch (Exception e) {
                userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to Update Device  name: " + device_name + " and id: " + existingDevice.getId() + " for network " + network_name + (updateDevice.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + updateDevice.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_info", existingDevice.getId());

            }
            if (existingDevice.getId() != null && existingDevice.getId().equals(updateDevice.getSubsystem_parent_id())) {
                deviceRepository.updateSubsystemParentDevice(existingDevice.getId(), null);
            }

            //update subsystem parent count of previous parent
            try {
                this.updateParentSubsystemCount(previous_subsystem_parent_id);
            } catch (Exception e) {
                // TODO: handle exception
            }

        }

        try {
            this.updateParentSubsystemCount(updateDevice.getSubsystem_parent_id());
        } catch (Exception e) {
            // TODO: handle exception
        }

        try {
            socketservice.socketDeviceCount();
            socketservice.sockerDeviceCountByDocker(dockername,assignee);
        } catch (Exception e) {
            // TODO: handle exception
        }

        // update snmp and notes count
        for (DeviceDTO deviceDTO : devices) {
            this.updateDeviceSnmpCount(deviceDTO.getId());
            this.updateDeviceNotesCount(deviceDTO.getId());
            this.updateDeviceSnmpStatusByDeviceId(deviceDTO.getId());
        }

        //update device port status
        try {
            this.updateMultipleDevicesProductPortStatus(vdmsid, devices);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public Set<DeviceDTO> getAllSubsystemDevices(String username, String vdmsid, String dockername, String device_id, String condition) {
        Set<DeviceDTO> devices;
        Integer virtual_device_type = null;
        Integer status = null;
        Integer monitor = 123;

        Integer asset_match_status = null;
        Integer onboard_status = 123;
        Integer assigned_status = null;

        System.out.println("outside all" + virtual_device_type + status + monitor);

        if (condition.equals("all")) {
            System.out.println("inside all" + virtual_device_type + status + monitor);
        } else if (condition.equals("unmonitored")) {
            System.out.println("outsidxse all" + virtual_device_type + status + monitor);
            monitor = 0;
        } else if (condition.equals("online")) {

            monitor = 1;
            status = 1;
            System.out.println("Inside Online" + monitor + status);

        } else if (condition.equals("offline")) {
            monitor = 1;
            status = 0;
        } else if (condition.equals("other")) {
            virtual_device_type = 123;
        } else if (condition.equals("matched")) {
            asset_match_status = 1;
        } else if (condition.equals("unmatched")) {
            asset_match_status = 0;
        } else if (condition.equals("verified")) {
            asset_match_status = 2;
        } else if (condition.equals("archived")) {
            asset_match_status = 3;
        } else if (condition.equals("onboarded")) {
            onboard_status = 3;
        } else if (condition.equals("notonboarded")) {
            onboard_status = 0;
        } else if (condition.equals("assigned")) {
            assigned_status = 1;
        } else if (condition.equals("unassigned")) {
            assigned_status = 0;
        }

//        if (device_id != null) {
//            devices = deviceRepository.getSubsystemDevicesByPagination(device_id);
//        } else {
        devices = deviceRepository.getSubsystemParentDevices(vdmsid, dockername, virtual_device_type, status, monitor, asset_match_status, onboard_status, assigned_status);
//        }

        for (DeviceDTO device : devices) {
            try {
                device.setIp_addresses(this.getDeviceIPAddressByDeviceId(device.getId()));
                device.setOnboard_data(new DeviceOnboardStatusDTO(device.getAssignee_email(), device.getImage_status(), device.getGeolocation_status(), device.getTag_status(), device.getField_status()));
                device.setSubsystems(new HashSet<>());
            } catch (Exception e) {
                System.out.println("Error getting device ip address " + e);
                System.out.println(e);
            }

        }

        Set<DeviceDTO> devicesWithQrCodeDetails = this.getDevicesWithQrCodeCount(vdmsid, devices);
        if (devicesWithQrCodeDetails != null) {
            return devicesWithQrCodeDetails;
        }
        return devices;
    }

    public Set<DeviceDTO> updateDeviceDetailsByIds(String username, String vdmsid, String dockername, Set<String> deviceIds) {
        Set<DeviceDTO> devices = new HashSet<>();
        DeviceDTO updateDevice;
        for (String id : deviceIds) {
            updateDevice = new DeviceDTO();
            updateDevice = this.getDeviceByDeviceId(username, vdmsid, dockername, id);
            devices.add(updateDevice);
        }
        return devices;
    }


    public Set<DeviceDTO> getDeviceNamesByVdmsIdAndDockerName(String username, String vdmsid, String dockername) {
        return deviceRepository.getDeviceNamesByVdmsIdAndDockerName(vdmsid, dockername);
    }

    public void addVirtualDeviceByVdmsIdAndDockerName(String username, String vdmsid, String dockername, String
            virtualDevicesDTO, List<MultipartFile> asset_images, HttpServletRequest httpServletRequest, String assignee) {
        /* (virtual_device_type: 1 - IP Device, 2 - Other Device
         *
         * dockername in path variable is dockername from which the device is updated,
         * dockername in the body is the dockername to be updated for the device
         * */
        log.info("addVirtualDeviceByVdmsIdAndDockerName, Params: virtualDeviceDto: {}, asset images: {}, endpoint : {}", virtualDevicesDTO, asset_images, httpServletRequest.getRequestURI());
        List<DeviceDTO> virtualDevices = utils.getJSONArrayFromJSONString(virtualDevicesDTO, DeviceDTO.class);
        List<String> device_ids_with_images = new ArrayList<>();
        Set<String> device_ids = new HashSet<>();
        Set<DeviceDTO> virtual_devices;
        LocationDTO locationDTO = null;

        for (DeviceDTO virtualDevice : virtualDevices) {
            String device_id = vdmsid + "_" + virtualDevice.getDocker_name() + "_" + virtualDevice.getUser_data_name() + "_"
                    + System.currentTimeMillis();
            String final_device_id = utils.replaceSpecialCharactersWithUnderscore(device_id);
            Integer device_status = null;
            try {
                if (virtualDevice.getVirtual_device_type() == 1) {
                    device_status = getVirtualDeviceStatus(virtualDevice.getDocker_name(), virtualDevice.getIp_address());
                }

//            if (virtualDevice.getBuilding_object() != null) {
//                buildingService.upsertBuildingByVdmsId(virtualDevice.getBuilding_object(), vdmsid);
//            }
                if (virtualDevice.getLocation_id() != null) {
                    //if location is tagged then match status is 2 - verified
                    virtualDevice.setAsset_match_status(2);
                } else {
                    virtualDevice.setAsset_match_status(0);
                }

                if (virtualDevice.getId() != null && virtualDevice.getId().equals(virtualDevice.getSubsystem_parent_id())) {
                    virtualDevice.setSubsystem_parent_id(null);
                }

                virtualDevice.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
                deviceRepository.addVirtualDevice(final_device_id, virtualDevice.getIp_address(), virtualDevice.getMac_address(),
                        virtualDevice.getUser_data_name(), virtualDevice.getUser_data_model(),
                        virtualDevice.getUser_data_vendor(), virtualDevice.getType(), virtualDevice.getLocation_id(),
                        virtualDevice.getNetwork_layer(), virtualDevice.getParent(), virtualDevice.getSnmp_parent(),
                        virtualDevice.getMonitor(), virtualDevice.getDocker_name(), vdmsid, virtualDevice.getLast_seen_on(),
                        virtualDevice.getWarranty(), device_status, virtualDevice.getEmail_alert(),
                        virtualDevice.getSms_alert(), virtualDevice.getPopup_notification(),
                        virtualDevice.getVirtual_device_type(), virtualDevice.getSerial_number(), virtualDevice.getLocal_vendor_email_alert(),
                        virtualDevice.getLocal_vendor_sms_alert(), virtualDevice.getSubsystem_parent_id(), virtualDevice.getDescription(),
                        virtualDevice.getAsset_match_status(), virtualDevice.getCreated_timestamp(), username, virtualDevice.getAsset_group(), virtualDevice.getCategory(), virtualDevice.getSub_category(),
                        virtualDevice.getCost_value(), virtualDevice.getAssigned_user_email(), virtualDevice.getAi_call(), virtualDevice.getCost_unit(), virtualDevice.getIs_dnd_enabled(), virtualDevice.getOperational_status());
                if (virtualDevice.getAsset_group() != null) {

                    DeviceLifecycleHistoryDTO dto = new DeviceLifecycleHistoryDTO();
                    dto.setDevice_id(final_device_id);
                    dto.setOperational_status("Working");
                    dto.setAssigned_user_id(virtualDevice.getAssigned_user_email());
                    dto.setAssigned_by_user_id(username);
                    deviceLifecycleHistoryService.addDeviceHistory(username, vdmsid, dto, null);

                }


                if (virtualDevice.getSpecifications() != null && virtualDevice.getSpecifications().size() > 0) {
                    for (SpecificationsDTO specificationsDTO : virtualDevice.getSpecifications()) {
                        int count = specificationsService.checkSpecificationByDeviceId(final_device_id, specificationsDTO.getKey_name());
                        if (count == 0) {

                            specificationsDTO.setDevice_id(final_device_id);
                            specificationsService.upsertDeviceSpecification(specificationsDTO);
                        }
                    }
                }

                // update virtual device history
                if (virtualDevice.getVirtual_device_type() == 1) {
                    Integer alarm = 1;
                    if (device_status == 1) {
                        alarm = 2;
                    }
                    historyService.insertDeviceStatusHistory(alarm, virtualDevice.getIp_address(), null, null,
                            final_device_id);
                }

                if (asset_images != null) {
                    device_ids_with_images.add(final_device_id);
                }
                device_ids.add(final_device_id);

                if (virtualDevice.getLocation_id() != null) {
                    locationDTO = locationService.getLocationByLocationId(virtualDevice.getLocation_id());
                }
                log.info("device id : {}", final_device_id);
                userActionLogService.addUserAction(username, "asset", "ADD", "A Virtual Device  name: " + virtualDevice.getUser_data_name() + " and id: " + final_device_id + " is added for network " + virtualDevice.getDocker_name() + (virtualDevice.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + virtualDevice.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_info", final_device_id);

            } catch (Exception e) {
                log.error("Exception. Params: virtualDeviceDto: {}, asset images: {}, endpoint : {}", virtualDevicesDTO, asset_images, httpServletRequest.getRequestURI(), e);
                userActionLogService.addUserAction(username, "asset", "ADD", "Unable to Add Virtual Device name: " + virtualDevice.getUser_data_name() + " and id: " + final_device_id + " for network " + virtualDevice.getDocker_name() + (virtualDevice.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + virtualDevice.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_info", final_device_id);

            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }

        if (device_ids_with_images.size() > 0) {
            this.upsertAssetImages(username, vdmsid, device_ids_with_images, asset_images, httpServletRequest);
        }

        virtual_devices = this.getDevicesByIdList(vdmsid, device_ids);
        this.updateVirtualDeviceOnboardStatus(virtual_devices, username);

        try {
            socketservice.sockerDeviceCountByDocker(dockername,assignee);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    // Get Virtual Device Status
    public Integer getVirtualDeviceStatus(String dockername, String ip_address) {
        Integer device_status = null;

        String[] cmd = {"bash", "-c", "docker exec " + dockername + " /tmp/sclera/virtual " + ip_address};

        String status = utils.execPipedCmd(cmd).get("output");
        if (!status.isBlank() && (status.equals("ONLINE") || status.contains("ONLINE"))) {
            device_status = 1;
        } else {
            device_status = 0;
        }

        System.out.println("virtual device status " + status);
        return device_status;
    }

    public void editVirtualDeviceByVirtualDeviceId(String username, String vdmsid, String dockername,
                                                   Set<DeviceDTO> virtualDevices, HttpServletRequest httpServletRequest) throws IOException {

        /* (virtual_device_type: 1 - IP Device, 2 - Other Device
         *
         * dockername in path variable is dockername from which the device is updated,
         * dockername in the body is the dockername to be updated for the device
         *
         * */
        log.info("editVirtualDeviceByVirtualDeviceId, Params: virtualDevices: {}, endpoint : {}", virtualDevices, httpServletRequest.getRequestURI());
        for (DeviceDTO virtualDevice : virtualDevices) {
            String virtual_device_id = virtualDevice.getId();

            DeviceDTO existingDevice = this.getDeviceByDeviceId(username, vdmsid, dockername, virtual_device_id);

            String previous_product_id = existingDevice.getProduct_id();
            String previous_subsystem_parent_id = existingDevice.getSubsystem_parent_id();
            try {

                if (virtualDevice.getOnboard_data() == null) {
                    virtualDevice.setOnboard_data(new DeviceOnboardStatusDTO());
                }

                this.getProductDetails(username, vdmsid, dockername, virtual_device_id, virtualDevice.getProduct_id(), previous_product_id);


//            if (virtualDevice.getBuilding_object() != null) {
//                buildingService.upsertBuildingByVdmsId(virtualDevice.getBuilding_object(), vdmsid);
//            }

                if (virtualDevice.getLocation_id() != null) {

                    String previous_location_id = existingDevice.getLocation_id();
                    if (!virtualDevice.getLocation_id().equals(previous_location_id)) {

                        String position = locationService.getPositionByLocationId(virtualDevice.getLocation_id());

                        // Update latitude and longitude to null
                        this.updateDeviceCoordinates(null, null, position, virtual_device_id);
                        virtualDevice.getOnboard_data().setGeolocation_status(0);
                    }

                    //if location is tagged then match status is 2 - verified
                    virtualDevice.setAsset_match_status(2);
                    virtualDevice.setLocation_status(null);
                } else {
                    // Update latitude and longitude to null
                    this.updateDeviceCoordinates(null, null, null, virtual_device_id);

                    //Update Asset Match Status
                    if (virtualDevice.getAsset_match_status() == null) {

                        if (existingDevice.getAsset_match_status() != null) {
                            if (existingDevice.getAsset_match_status() == 2) {
                                virtualDevice.setAsset_match_status(1);
                            } else {
                                virtualDevice.setAsset_match_status(existingDevice.getAsset_match_status());
                            }
                        }
                    }
                    if (virtualDevice.getOnboard_data() != null && virtualDevice.getOnboard_data().getGeolocation_status() == null) {
//                        if (existingDevice.getOnboard_data().getGeolocation_status() == 1) {
//                            virtualDevice.getOnboard_data().setGeolocation_status(0);
//                        }
                        if (virtualDevice.getLocation_status() != null && virtualDevice.getLocation_status().equals("no_fixed_location")) {
                            virtualDevice.getOnboard_data().setGeolocation_status(1);
                        } else {
                            virtualDevice.getOnboard_data().setGeolocation_status(0);
                        }
                    }
                }
                if (virtualDevice.getId() != null && virtualDevice.getId().equals(virtualDevice.getSubsystem_parent_id())) {
                    virtualDevice.setSubsystem_parent_id(null);
                }
//			deviceRepository.editVirtualDeviceByVirtualDeviceId(virtualDevice.getMonitor(),
//					virtualDevice.getLocation_id(), virtualDevice.getNetwork_layer(),
//					virtualDevice.getUser_data_model(), virtualDevice.getType(), virtualDevice.getUser_data_vendor(),
//					virtualDevice.getUser_data_name(), virtualDevice.getParent(), virtualDevice.getRemote_access(),
//					virtualDevice.getProduct_id(), virtualDevice.getWarranty(), virtualDevice.getIp_address(),
//					virtualDevice.getEmail_alert(), virtualDevice.getSms_alert(), virtualDevice.getPopup_notification(),
//					virtualDevice.getVirtual_device_type(), virtualDevice.getSerial_number(), virtual_device_id);


                deviceRepository.editVirtualDeviceByVirtualDeviceId(virtual_device_id, virtualDevice.getMonitor(),
                        virtualDevice.getLocation_id(), virtualDevice.getNetwork_layer(),
                        virtualDevice.getUser_data_model(), virtualDevice.getType(), virtualDevice.getUser_data_vendor(),
                        virtualDevice.getUser_data_name(), virtualDevice.getParent(), virtualDevice.getRemote_access(),
                        virtualDevice.getProduct_id(), virtualDevice.getWarranty(), virtualDevice.getIp_address(),
                        virtualDevice.getEmail_alert(), virtualDevice.getSms_alert(), virtualDevice.getPopup_notification(),
                        virtualDevice.getVirtual_device_type(), virtualDevice.getSerial_number(),
                        virtualDevice.getLocal_vendor_email_alert(), virtualDevice.getLocal_vendor_sms_alert(), virtualDevice.getDocker_name(),
                        virtualDevice.getSubsystem_parent_id(), virtualDevice.getCustom_fields(), virtualDevice.getDescription(),
                        virtualDevice.getAsset_match_status(), virtualDevice.getAsset_group(), virtualDevice.getCategory(), virtualDevice.getSub_category(), virtualDevice.getLocation_status(),
                        virtualDevice.getCost_value(), virtualDevice.getAssigned_user_email(), virtualDevice.getAi_call(), virtualDevice.getCost_unit(), virtualDevice.getIs_dnd_enabled(), virtualDevice.getOperational_status(), virtualDevice.getAdc_json());

                if (virtualDevice.getAsset_group() != null)  {

                    // CASE 1:  user is removed (A → null)
                    if (existingDevice.getAssigned_user_email() != null &&
                            virtualDevice.getAssigned_user_email() == null) {
                        DeviceLifecycleHistoryDTO deviceLifecycleHistoryDTO = new DeviceLifecycleHistoryDTO();
                        deviceLifecycleHistoryDTO.setDevice_id(existingDevice.getId());

                        deviceLifecycleHistoryDTO.setOperational_status(virtualDevice.getOperational_status());

                        deviceLifecycleHistoryDTO.setAssigned_user_id(null);
                        deviceLifecycleHistoryDTO.setAssigned_by_user_id(username);
                        deviceLifecycleHistoryService.addDeviceHistory(username, vdmsid, deviceLifecycleHistoryDTO, null);

                        // CASE 2: user changed (A → B)
                    } else if (!Objects.equals(virtualDevice.getAssigned_user_email(), existingDevice.getAssigned_user_email()) &&
                            existingDevice.getAssigned_user_email() != null &&
                            virtualDevice.getAssigned_user_email() != null) {

                        DeviceLifecycleHistoryDTO deviceLifecycleHistoryDTO = new DeviceLifecycleHistoryDTO();
                        deviceLifecycleHistoryDTO.setDevice_id(existingDevice.getId());
                        deviceLifecycleHistoryDTO.setOperational_status(virtualDevice.getOperational_status());
                        deviceLifecycleHistoryDTO.setAssigned_user_id(virtualDevice.getAssigned_user_email());
                        deviceLifecycleHistoryDTO.setAssigned_by_user_id(username);
                        deviceLifecycleHistoryService.addDeviceHistory(username, vdmsid, deviceLifecycleHistoryDTO,null);

                        // CASE 3: user assigned (null → A)
                    } else if (existingDevice.getAssigned_user_email() == null &&
                            virtualDevice.getAssigned_user_email() != null) {

                        DeviceLifecycleHistoryDTO deviceLifecycleHistoryDTO = new DeviceLifecycleHistoryDTO();
                        deviceLifecycleHistoryDTO.setDevice_id(existingDevice.getId());
                        deviceLifecycleHistoryDTO.setOperational_status(virtualDevice.getOperational_status());
                        deviceLifecycleHistoryDTO.setAssigned_user_id(virtualDevice.getAssigned_user_email());
                        deviceLifecycleHistoryDTO.setAssigned_by_user_id(username);
                        deviceLifecycleHistoryService.addDeviceHistory(username, vdmsid, deviceLifecycleHistoryDTO,null);

                    }

                        // CASE 4: assignee same, but operational status changed
                    else if (Objects.equals(virtualDevice.getAssigned_user_email(), existingDevice.getAssigned_user_email()) &&
                            !Objects.equals(virtualDevice.getOperational_status(), existingDevice.getOperational_status())) {

                        DeviceLifecycleHistoryDTO deviceLifecycleHistoryDTO = new DeviceLifecycleHistoryDTO();
                        deviceLifecycleHistoryDTO.setDevice_id(existingDevice.getId());
                        deviceLifecycleHistoryDTO.setOperational_status(virtualDevice.getOperational_status());
                        deviceLifecycleHistoryDTO.setAssigned_user_id(virtualDevice.getAssigned_user_email());
                        deviceLifecycleHistoryDTO.setAssigned_by_user_id(username);
                        deviceLifecycleHistoryService.addDeviceHistory(username, vdmsid, deviceLifecycleHistoryDTO, null);
                    }
                }
                if (virtualDevice.getOnboard_data() != null) {
                    if (((virtualDevice.getName() != null && !(virtualDevice.getName().isBlank())) || (virtualDevice.getUser_data_name() != null && !(virtualDevice.getUser_data_name().isBlank()))) &&
                            ((virtualDevice.getVendor() != null && !(virtualDevice.getVendor().isBlank())) || (virtualDevice.getUser_data_vendor() != null && !(virtualDevice.getUser_data_vendor().isBlank()))) &&
                            ((virtualDevice.getModel() != null && !(virtualDevice.getModel().isBlank())) || (virtualDevice.getUser_data_model() != null && !(virtualDevice.getUser_data_model().isBlank())))) {
                        virtualDevice.getOnboard_data().setField_status(1);
                    } else {
                        // Only downgrade if previously complete
                        Integer existingStatus = existingDevice.getOnboard_data().getField_status();
                        if (existingStatus != null && existingStatus == 1) {
                            virtualDevice.getOnboard_data().setField_status(0);
                        }
                    }
                }

                if (virtualDevice.getOnboard_data() != null) {
                    System.out.println("Virtual Onboard data: " + virtualDevice.getOnboard_data());
                    log.info("Virtual Onboard data: : {}", virtualDevice.getOnboard_data());
                    this.updateAssetOnboardData(username, vdmsid, virtual_device_id, virtualDevice.getOnboard_data(), existingDevice.getOnboard_status());
                }
                log.info("device id : {}", virtual_device_id);
                userActionLogService.addUserAction(username, "asset", "UPDATE", "A Virtual Device  name: " + virtualDevice.getUser_data_name() + " and id: " + virtualDevice.getId() + " is updated for network " + virtualDevice.getDocker_name() + (virtualDevice.getLocation_id() != null && virtualDevice.getLocation() != null ? ", Location id: " + virtualDevice.getLocation_id() + ", Location name: " + virtualDevice.getLocation() : ""), "success", "asset_info", virtualDevice.getId());

            } catch (Exception e) {
                log.error("Exception. Params: virtualDevices: {}, endpoint : {}", virtualDevices, httpServletRequest.getRequestURI(), e);
                userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to Update Virtual Device name: " + virtualDevice.getUser_data_name() + " and id: " + virtualDevice.getId() + " for network " + virtualDevice.getDocker_name() + (virtualDevice.getLocation_id() != null && virtualDevice.getLocation() != null ? ", Location id: " + virtualDevice.getLocation_id() + ", Location name: " + virtualDevice.getLocation() : ""), "failed", "asset_info", virtualDevice.getId());

            }
            try {
                this.updateParentSubsystemCount(virtualDevice.getSubsystem_parent_id());
                this.updateParentSubsystemCount(previous_subsystem_parent_id);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        Set<String> device_ids = new HashSet<>();

        // update snmp and notes count
        for (DeviceDTO virtualDevice : virtualDevices) {
            try {
                device_ids.add(virtualDevice.getId());
                this.updateDeviceSnmpCount(virtualDevice.getId());
                this.updateDeviceNotesCount(virtualDevice.getId());
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        try {
            this.updateDeviceProductPortStatus(vdmsid, device_ids);
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void deleteVirtualDeviceByVirtualDeviceId(String username, String vdmsid, String dockername,
                                                     String virtual_device_id, String assignee) {

        // portService.deletePortByDeviceId(virtual_device_id);
        // notesService.deleteNotesByDeviceId(virtual_device_id);
        // deviceRepository.deleteVirtualDeviceByDeviceId(virtual_device_id);
        DeviceDTO deviceDTO = deviceRepository.getDeviceByDeviceId(virtual_device_id);
        String device_name = deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().equals("") ? deviceDTO.getDisplay_name() : deviceDTO.getUser_data_name();

        try {
            connectedDevicesService.untagPowerSourceByDeviceId(virtual_device_id);
            deviceRepository.deleteById(virtual_device_id);
            userActionLogService.addUserAction(username, "asset", "DELETE", "A Virtual Device  name: " + device_name + " and id: " + deviceDTO.getId() + " is deleted from network " + deviceDTO.getDocker_name(), "success", "asset_info", deviceDTO.getId());
        } catch (Exception e) {
            userActionLogService.addUserAction(username, "asset", "DELETE", "Unable to delete Virtual Device name:" + device_name + " and id: " + deviceDTO.getId() + " from network " + deviceDTO.getDocker_name(), "failed", "asset_info", deviceDTO.getId());
        }
        try {
            socketservice.sockerDeviceCountByDocker(dockername,assignee);
        } catch (Exception e) {
            // TODO: handle exception
        }

        try {
            this.removeAssetImages(deviceDTO);
        } catch (Exception e) {
            System.out.println("Unable to remove asset images");
        }
        try {
            removeAssetOcrImages(deviceDTO);
        } catch (Exception e) {
            System.out.println("Unable to remove asset ocr images");
        }
    }

    // device is not soft deleted, bcz it had lot all changes, only inspections are soft deleted.
    @Transactional
    public void softDeleteDevicesById(String username, String vdmsid, String dockername, Set<String> deviceIds, HttpServletRequest httpServletRequest, String assignee) {
        log.info("deleteDevicesById, Params: deviceIds: {}, endpoint : {}", deviceIds, httpServletRequest.getRequestURI());
        List<UserActionLogDTO> userActionLogDTOS = new ArrayList<>();
        List<String> finalRecordChecklistIds = new ArrayList<>();
        List<String> finalGlobalInspectionRelationIds = new ArrayList<>();
        Set<String> finalInspectionrecordIds = new HashSet<>();
        LocationDTO locationDTO = null;
        for (String deviceId : deviceIds) {
            log.info("Deleting Device Id : " + deviceId);
            Device device = deviceRepository.findById(deviceId).orElse(null);
            DeviceDTO deviceDTO = deviceRepository.getDeviceByDeviceId(deviceId);
            String device_name = deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().equals("") ? deviceDTO.getDisplay_name() : deviceDTO.getUser_data_name();

            try {
                for (Bacnet_Object bacnetObject : device.getBacnet_object()) {
                    bacnetObject.setDevice(null);
                }
                log.info("Came here after bacnet");
                for (Lorawan_Sensor lorawanSensor : device.getLorawan_sensor()) {
                    lorawanSensor.setDevice(null);
                }
                log.info("Came here after lorawan");
                for (DisruptiveSensor disruptiveSensor : device.getDisruptive_sensor()) {
                    disruptiveSensor.setDevice(null);
                }

                log.info("Came here after disruptive");
                for (Datahoist datahoist : device.getDatahoist()) {
                    datahoist.setDevice(null);
                }
                log.info("Came here after data hoist");
                for (MyDevicesSensor myDevicesSensor : device.getMy_devices_sensor()) {
                    myDevicesSensor.setDevice(null);
                }
                log.info("Came here after my devices");
                for (Monnit_Sensor monnitSensor : device.getMonnit_sensor()) {
                    monnitSensor.setDevice(null);
                }
                log.info("Came here after monnit sensor");
                for (PelicanSensor pelicanSensor : device.getPelican_sensor()) {
                    pelicanSensor.setDevice(null);
                }
                log.info("Came here after pelican sensor");
                for (KNXGroup knxGroup : device.getKnx_group()) {
                    knxGroup.setDevice(null);
                }
                log.info("Came here after knx group");
                for (SnmpObject snmpObject : device.getSnmp_object()) {
                    snmpObject.setDevice(null);
                }
                log.info("Came here after snmp object");
                for (DaintreeDevice daintreeDevice : device.getDaintree_device()) {
                    daintreeDevice.setDevice(null);
                }
                log.info("Came here after daintree");

                for (EcobeeSensor ecobeeSensor : device.getEcobee_sensor()) {
                    ecobeeSensor.setDevice(null);
                }
                log.info("Came here after ecobee");

                for (ModbusRegister modbusRegister : device.getModbus_register()) {
                    modbusRegister.setDevice(null);
                }
                log.info("Came here after modbus");

                for (SiemensAsset siemensAsset : device.getSiemens_asset()) {
                    siemensAsset.setDevice(null);
                }
                log.info("Came here after siemens");

                if (device.getInventory_device() != null) {
                    device.getInventory_device().setDevice(null);
                    log.info("Set device to null...for inventory device");
                } else {
                    log.info("No inventory_device mapped for deviceId: {}", device.getId());
                }

                Set<String> inspectionRecordIds = device.getRecord_checklist()
                        .stream()
                        .map(RecordChecklist::getInspection_record)
                        .filter(Objects::nonNull)
                        .filter(record -> !true)
                        .map(InspectionRecord::getId)
                        .collect(Collectors.toSet());
                finalInspectionrecordIds.addAll(inspectionRecordIds);

                Set<String> recordChecklistIds = device.getRecord_checklist()
                        .stream()
                        .map(RecordChecklist::getId)
                        .collect(Collectors.toSet());
                finalRecordChecklistIds.addAll(recordChecklistIds);

                System.out.println("r size" + recordChecklistIds.size());

                Set<String> globalInspectionRelationIds = device.getGlobal_inspection_relation()
                        .stream()
                        .map(GlobalInspectionRelation::getId)
                        .collect(Collectors.toSet());
                finalGlobalInspectionRelationIds.addAll(globalInspectionRelationIds);

                System.out.println("g size" + globalInspectionRelationIds.size());

                Set<String> globalChecklistConditionIds = device.getGlobal_checklist_conditions()
                        .stream()
                        .map(GlobalChecklistConditions::getId)
                        .collect(Collectors.toSet());

                System.out.println("global checklist conditions size" + globalChecklistConditionIds.size());

                recordChecklistService.updateRecordChecklistDeviceAndIsRemoved(recordChecklistIds);
                globalInspectionRecordService.updateGlobalInspectionRelationDeviceAndIsRemoved(globalInspectionRelationIds);
                globalChecklistConditionsService.updateGlobalChecklistConditionsDeviceAndIsRemoved(globalChecklistConditionIds);

                for (RecordChecklist recordChecklist : device.getRecord_checklist()) {
                    UserActionLogDTO userActionLogDTO = new UserActionLogDTO();
                    if (recordChecklist.getRecord_type().equals("checklist") && recordChecklist.getInspection_record() == null) {
                        userActionLogDTO.setType("procedure");
                        userActionLogDTO.setSub_type("tagged_procedure");
                    } else if (recordChecklist.getRecord_type().equals("checklist") && recordChecklist.getInspection_record() != null) {
                        userActionLogDTO.setType("inspection");
                        userActionLogDTO.setSub_type("inspection_checklist");
                    } else if (recordChecklist.getRecord_type().equals("service") && recordChecklist.getInspection_record() == null) {
                        userActionLogDTO.setType("reactive_service");
                        userActionLogDTO.setSub_type("service_request");
                    } else if (recordChecklist.getRecord_type().equals("service") && recordChecklist.getInspection_record() != null) {
                        userActionLogDTO.setSub_type("scheduled_service");
                        userActionLogDTO.setSub_type("service_checklist");
                    }
                    userActionLogDTO.setStatus("success");
                    userActionLogDTO.setPrimary_id(recordChecklist.getId());
                    userActionLogDTO.setEmail(username);
                    userActionLogDTO.setSecondary_id(deviceId);
                    userActionLogDTO.setTable_name("record_checklist");
                    userActionLogDTO.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
                    userActionLogDTO.setMessage("Record Checklist " + recordChecklist.getId() + " tagged to device " + deviceId + " has been soft deleted");
                    userActionLogDTOS.add(userActionLogDTO);
                    log.info("Deleted record id " + recordChecklist.getId());
                }

                for (GlobalInspectionRelation globalInspectionRelation : device.getGlobal_inspection_relation()) {
                    UserActionLogDTO userActionLogDTO = new UserActionLogDTO();
                    userActionLogDTO.setPrimary_id(globalInspectionRelation.getId());
                    userActionLogDTO.setEmail(username);
                    userActionLogDTO.setStatus("success");
                    userActionLogDTO.setSecondary_id(deviceId);
                    userActionLogDTO.setTable_name("global_inspection_relation");
                    userActionLogDTO.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
                    userActionLogDTO.setMessage("Global Inspection Relation " + globalInspectionRelation.getId() + " associated with device " + deviceId + " has been soft deleted");
                    userActionLogDTOS.add(userActionLogDTO);
                    log.info("DELETED global relation id :" + globalInspectionRelation.getId());
                }

                for (GlobalChecklistConditions globalChecklistConditions : device.getGlobal_checklist_conditions()) {
                    UserActionLogDTO userActionLogDTO = new UserActionLogDTO();
                    userActionLogDTO.setPrimary_id(globalChecklistConditions.getId());
                    userActionLogDTO.setEmail(username);
                    userActionLogDTO.setStatus("success");
                    userActionLogDTO.setSecondary_id(deviceId);
                    userActionLogDTO.setTable_name("global_checklist_conditions");
                    userActionLogDTO.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
                    userActionLogDTO.setMessage("Global Inspection Relation " + globalChecklistConditions.getId() + " associated with device " + deviceId + " has been soft deleted");
                    userActionLogDTOS.add(userActionLogDTO);
                    log.info("DELETED global checklist condition id :" + globalChecklistConditions.getId());
                }
                connectedDevicesService.untagPowerSourceByDeviceId(deviceId);
                log.info("Untagged Power sources");

                if (deviceDTO.getLocation_id() != null) {
                    locationDTO = locationService.getLocationByLocationId(deviceDTO.getLocation_id());
                }


                try {
                    deviceSpecificationRepository.deleteByDeviceId(deviceId);
                    log.info("DeviceSpecification deleted successfully for deviceId: {}", deviceId);
                    deviceInstalledAppsRepository.deleteByDeviceId(deviceId);
                    log.info("DeviceInstalledApps deleted successfully for deviceId: {}", deviceId);
                    deviceLifeCycleHistoryRepository.deleteByDeviceId(deviceId);
                    log.info("DeviceLifecycleHistory deleted successfully for deviceId: {}", deviceId);
                    historyRepository.deleteByDeviceId(deviceId);
                    log.info("History deleted successfully for deviceId: {}", deviceId);
                    deviceOnboardStatusRepository.deleteByDeviceId(deviceId);
                    log.info("Device Onboard Status Repository deleted successfully for deviceId: {}", deviceId);
                    inventoryDeviceRepository.deleteByDeviceId(deviceId);
                    log.info("Inventory Device Repository deleted successfully for deviceId: {}", deviceId);
                    deviceNetworkSpecificationRepository.deleteByDeviceId(deviceId);
                    log.info("Device Network Specification Repository deleted successfully for deviceId: {}", deviceId);
                    remoteDesktopSessionRepository.deleteByDeviceId(deviceId);
                    log.info("Remote Desktop Session Repository deleted successfully for deviceId: {}", deviceId);
                } catch (Exception e) {
                    log.error("Error while deleting device for deviceId: {}", deviceId, e);
                }

                deviceRepository.deleteById(deviceId);
                log.info("Deleted device");
                userActionLogService.addUserAction(username, "asset", "DELETE", "A Device name: " + device_name + " and id: " + deviceId + " is deleted from network " + deviceDTO.getDocker_name() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_info", deviceId);
            } catch (Exception e) {
                userActionLogService.addUserAction(username, "asset", "DELETE", "Unable to delete Device name: " + device_name + " and id: " + deviceId + " from network " + deviceDTO.getDocker_name() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_info", deviceId);
                log.error("Exception. Params: deviceIds: {}, endpoint : {}", deviceIds, httpServletRequest.getRequestURI(), e);
            }

            log.info("Came here after all");
            //update device subsystem count
            try {
                this.updateParentSubsystemCount(device.getSubsystem_parent_id());

                List<String> device_ids = deviceRepository.getDevicesBySubSystemParentId(deviceId);
                for (String device_id : device_ids) {
                    deviceRepository.updateSubsystemParentDevice(device_id, null);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

            try {
                this.removeAssetImages(deviceDTO);
            } catch (Exception e) {
                log.error("Unable to remove asset images");
            }
            try {
                this.removeAssetOcrImages(deviceDTO);
            } catch (Exception e) {
                log.error("Unable to remove asset ocr images");
            }
        }

        try {
            socketservice.sockerDeviceCountByDocker(dockername,assignee);

        } catch (Exception e) {
            // TODO: handle exception
        }

        log.info("fi size " + finalInspectionrecordIds.size());
        log.info("fr size " + finalRecordChecklistIds.size());
        log.info("fg size " + finalGlobalInspectionRelationIds.size());

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            log.info("Entered executor service at " + System.currentTimeMillis());
            archivedRecordService.batchUpdateArchivedRecords(userActionLogDTOS);
            recordChecklistService.deleteRecordChecklistInBatch(finalRecordChecklistIds);
            globalInspectionRecordService.deleteGlobalInspectionRelationInBatch(finalGlobalInspectionRelationIds);
            for (String id : finalInspectionrecordIds) {
                inspectionRecordService.updateInspectionRecordStatus(username, vdmsid, id, false);
                log.info("Inspection record status updated while deleting device for id " + id);
            }
            log.info("Process completed at " + System.currentTimeMillis());
        });

    }


    public void deleteDevicesById(String username, String vdmsid, String dockername, Set<String> deviceIds, HttpServletRequest httpServletRequest, String assignee) {
        log.info("deleteDevicesById, Params: deviceIds: {}, endpoint : {}", deviceIds, httpServletRequest.getRequestURI());
        for (String deviceId : deviceIds) {
            log.info("Deleting Device Id : " + deviceId);
            Device device = deviceRepository.findById(deviceId).orElse(null);
            DeviceDTO deviceDTO = deviceRepository.getDeviceByDeviceId(deviceId);
            String device_name = deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().equals("") ? deviceDTO.getDisplay_name() : deviceDTO.getUser_data_name();
            LocationDTO locationDTO = null;

            try {
                for (Bacnet_Object bacnetObject : device.getBacnet_object()) {
                    bacnetObject.setDevice(null);
                }
                log.info("Came here after bacnet");
                for (Lorawan_Sensor lorawanSensor : device.getLorawan_sensor()) {
                    lorawanSensor.setDevice(null);
                }
                log.info("Came here after lorawan");
                for (DisruptiveSensor disruptiveSensor : device.getDisruptive_sensor()) {
                    disruptiveSensor.setDevice(null);
                }

                log.info("Came here after disruptive");
                for (Datahoist datahoist : device.getDatahoist()) {
                    datahoist.setDevice(null);
                }
                log.info("Came here after data hoist");
                for (MyDevicesSensor myDevicesSensor : device.getMy_devices_sensor()) {
                    myDevicesSensor.setDevice(null);
                }
                log.info("Came here after my devices");
                for (Monnit_Sensor monnitSensor : device.getMonnit_sensor()) {
                    monnitSensor.setDevice(null);
                }
                log.info("Came here after monnit sensor");
                for (PelicanSensor pelicanSensor : device.getPelican_sensor()) {
                    pelicanSensor.setDevice(null);
                }
                log.info("Came here after pelican sensor");
                for (KNXGroup knxGroup : device.getKnx_group()) {
                    knxGroup.setDevice(null);
                }
                log.info("Came here after knx group");
                for (SnmpObject snmpObject : device.getSnmp_object()) {
                    snmpObject.setDevice(null);
                }
                log.info("Came here after snmp object");
                for (DaintreeDevice daintreeDevice : device.getDaintree_device()) {
                    daintreeDevice.setDevice(null);
                }
                log.info("Came here after daintree");

                for (EcobeeSensor ecobeeSensor : device.getEcobee_sensor()) {
                    ecobeeSensor.setDevice(null);
                }
                log.info("Came here after ecobee");

                for (ModbusRegister modbusRegister : device.getModbus_register()) {
                    modbusRegister.setDevice(null);
                }
                log.info("Came here after modbus");

                for (SiemensAsset siemensAsset : device.getSiemens_asset()) {
                    siemensAsset.setDevice(null);
                }
                log.info("Came here after siemens");


                connectedDevicesService.untagPowerSourceByDeviceId(deviceId);
                log.info("Untagged Power sources");
                List<String> imageUrls = recordChecklistService.deleteAllRecordChecklistByDeviceId(deviceId);
                if (deviceDTO.getLocation_id() != null) {
                    locationDTO = locationService.getLocationByLocationId(deviceDTO.getLocation_id());
                }
                deviceRepository.deleteById(deviceId);
                log.info("Deleted device");
                recordChecklistService.deleteAllRecordChecklistImagesByUrls(imageUrls);
                log.info("Deleting checklist images");
                userActionLogService.addUserAction(username, "asset", "DELETE", "A Device name: " + device_name + " and id: " + deviceId + " is deleted from network " + deviceDTO.getDocker_name() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_info", deviceId);
            } catch (Exception e) {
                userActionLogService.addUserAction(username, "asset", "DELETE", "Unable to delete Device name: " + device_name + " and id: " + deviceId + " from network " + deviceDTO.getDocker_name() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_info", deviceId);
                log.error("Exception. Params: deviceIds: {}, endpoint : {}", deviceIds, httpServletRequest.getRequestURI(), e);
            }
            log.info("Came here after all");
            //update device subsystem count
            try {
                this.updateParentSubsystemCount(device.getSubsystem_parent_id());

                List<String> device_ids = deviceRepository.getDevicesBySubSystemParentId(deviceId);
                for (String device_id : device_ids) {
                    deviceRepository.updateSubsystemParentDevice(device_id, null);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

            try {
                this.removeAssetImages(deviceDTO);
            } catch (Exception e) {
                log.error("Unable to remove asset images");
            }
            try {
                this.removeAssetOcrImages(deviceDTO);
            } catch (Exception e) {
                log.error("Unable to remove asset ocr images");
            }
        }
        try {
            socketservice.sockerDeviceCountByDocker(dockername,assignee);

        } catch (Exception e) {
            // TODO: handle exception
        }
    }


    // Update Virtual device status and timestamp
    public DeviceDTO updateVirtualDeviceStatusByVirtualDeviceId(String username, String vdmsid, String dockername,
                                                                String virtual_device_id, DeviceDTO virtualdevicedto) {

        DeviceDTO virtualDevice = deviceRepository.getDeviceByDeviceId(virtual_device_id);

        Integer status = getVirtualDeviceStatus(dockername, virtualdevicedto.getIp_address());

        if (status != null) {
            BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
            virtualdevicedto.setLast_seen_on(timestamp.toString());
            virtualdevicedto.setStatus(status);
            deviceRepository.updateVirtualDeviceStatus(status, timestamp, virtual_device_id);

            // update virtual device history
            try {
                if (status != virtualDevice.getStatus()) {
                    Integer alarm = 1;
                    if (status == 1) {
                        alarm = 2;
                    }
                    historyService.insertDeviceStatusHistory(alarm, virtualdevicedto.getIp_address(), null, null,
                            virtual_device_id);
                }
            } catch (Exception e) {
                System.out.println("Virtual Device History Error " + e);
            }
        }

        return virtualdevicedto;
    }

    // Sync all Virtual device status
    public void updateAllVirtualDeviceStatus() {

        // get all virtual device
        List<DeviceDTO> virtualdeviceList = deviceRepository.listAllVirtualdevices();

        for (DeviceDTO deviceDTO : virtualdeviceList) {
            updateVirtualDeviceStatusByVirtualDeviceId(null, deviceDTO.getVdms_id(), deviceDTO.getDocker_name(),
                    deviceDTO.getId(), deviceDTO);
        }

    }

    // update snmp count
    public void updateDeviceSnmpCount(String device_id) {
        try {
            Integer snmp_count = snmpService.getSnmpDeviceCountByDeviceAndSnmpConfiguration(device_id);
            deviceRepository.updateDeviceSnmpCount(device_id, snmp_count);
        } catch (Exception e) {
            System.out.println("Error in snmp count update " + e);
        }
    }

    // update snmp status
    public void updateDeviceSnmpStatus(String snmp_device_id) {
        try {
            String device_id = snmpService.getDeviceIdBySnmpDeviceId(snmp_device_id);

            this.updateDeviceSnmpStatusByDeviceId(device_id);
        } catch (Exception e) {
            System.out.println("Error in updating device snmp status " + e);
        }
    }

    // update snmp status by device id
    public void updateDeviceSnmpStatusByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Boolean alert = snmpService.getSnmpDeviceAlertStatusByDeviceId(device_id);
                String snmp_status = "no-alert";
                if (alert) {
                    snmp_status = "alert";
                }
                deviceRepository.updateDeviceSnmpStatus(device_id, snmp_status);
            }
        } catch (Exception e) {
            System.out.println("Error in updating device snmp status by device id " + e);
        }
    }

    // update interface count
    public void updateDeviceInterfaceCount(String device_id) {
        try {
            Integer interface_count = interfaceService.getInterfaceCountByDevice(device_id);
            deviceRepository.updateDeviceInterfaceCount(device_id, interface_count);
        } catch (Exception e) {
            System.out.println("Error updating interface count " + e);
        }

    }

    // update notes count
    public void updateDeviceNotesCount(String device_id) {
        try {
            Integer notes_count = notesService.getNotesCountByDeviceId(device_id);
            deviceRepository.updateDeviceNotesCount(device_id, notes_count);
        } catch (Exception e) {
            System.out.println("Error in Notes Count Update " + e);
        }
    }

    // update ticket count
    public void updateDeviceTicketCount(String device_id) {
        try {
            Integer ticket_count = ticketService.getTicketCountByDeviceId(device_id);
            deviceRepository.updateDeviceTicketCount(device_id, ticket_count);
        } catch (Exception e) {
            System.out.println("Error in updating ticket count " + e);
        }

    }

    // update ticket status
    public void updateDeviceTicketStatus(String device_id) {
        try {
            Boolean status = ticketService.getOpenTicketStatus(device_id);
            String ticket_status = "closed";
            if (status) {
                ticket_status = "open";
            }
            deviceRepository.updateDeviceTicketStatus(device_id, ticket_status);
        } catch (Exception e) {
            System.out.println("Error in updating ticket status " + e);
        }
    }

    // update bacnet object count
    public void updateDeviceBacnetCount(String bacnet_device_id, String bacnet_object_id) {
        try {
            String device_id = bacnetService.getDeviceIdByBacnetObjectId(bacnet_device_id, bacnet_object_id);
            this.updateDeviceBacnetCountByDeviceId(device_id);
        } catch (Exception e) {
            System.out.println("Error in updating bacnet count " + e);
        }
    }

    // update bacnet object count by device id
    public void updateDeviceBacnetCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer bacnet_count = bacnetService.getBacnetObjectCountByDeviceId(device_id);
                deviceRepository.updateDeviceBacnetCount(device_id, bacnet_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating bacnet count by device id " + e);
        }
    }

    // update bacnet object status
    public void updateDeviceBacnetStatus(String bacnet_device_id, String bacnet_object_id) {
        try {
            String device_id = bacnetService.getDeviceIdByBacnetObjectId(bacnet_device_id, bacnet_object_id);

            this.updateDeviceBacnetStatusByDeviceId(device_id);

        } catch (Exception e) {
            System.out.println("Error in updating bacnet status " + e);
        }
    }

    // update bacnet object status by device id
    public void updateDeviceBacnetStatusByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Boolean alert = bacnetService.getBacnetObjectAlertStatusByDeviceId(device_id);
                String bacnet_status = "no-alert";
                if (alert) {
                    bacnet_status = "alert";
                }
                deviceRepository.updateDeviceBacnetStatus(device_id, bacnet_status);
            }
        } catch (Exception e) {
            System.out.println("Error in updating bacnet status by device id " + e);
        }
    }

    // update lorawan sensor count
    public void updateDeviceLorawanCount(String lorawan_sensor_id) {
        try {
            String device_id = lorawanService.getDeviceIdByLorawanSensorId(lorawan_sensor_id);
            this.updateDeviceLorawanCountByDeviceId(device_id);
        } catch (Exception e) {
            System.out.println("Error in updating lorawan count " + e);
        }
    }

    // update lorawan sensor count by device id
    public void updateDeviceLorawanCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer lorawan_count = lorawanService.getLorawanSensorCountByDeviceId(device_id);
                deviceRepository.updateDeviceLorawanCount(device_id, lorawan_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating lorawan count by device id " + e);
        }
    }

    // update lorawan sensor status
    public void updateDeviceLorawanStatus(String lorawan_sensor_id) {
        try {
            String device_id = lorawanService.getDeviceIdByLorawanSensorId(lorawan_sensor_id);

            this.updateDeviceLorawanStatusByDeviceId(device_id);

        } catch (Exception e) {
            System.out.println("Error in updating lorawan status " + e);
        }
    }

    // update lorawan sensor status by device id
    public void updateDeviceLorawanStatusByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Boolean alert = lorawanService.getLorawanSensorAlertStatusByDeviceId(device_id);
                String lorawan_status = "no-alert";
                if (alert) {
                    lorawan_status = "alert";
                }
                deviceRepository.updateDeviceLorawanStatus(device_id, lorawan_status);
            }
        } catch (Exception e) {
            System.out.println("Error in updating lorawan status by device id " + e);
        }
    }

    // update disruptive sensor count
    public void updateDeviceDisruptiveCount(String disruptive_sensor_id) {
        try {
            String device_id = disruptiveService.getDeviceIdByDisruptiveSensorId(disruptive_sensor_id);
            this.updateDeviceDisruptiveCountByDeviceId(device_id);
        } catch (Exception e) {
            System.out.println("Error in updating disruptive count " + e);
        }
    }

    // update disruptive sensor count by device id
    public void updateDeviceDisruptiveCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer disruptive_count = disruptiveService.getDisruptiveSensorCountByDeviceId(device_id);
                deviceRepository.updateDeviceDisruptiveCount(device_id, disruptive_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating disruptive count by device id " + e);
        }
    }

    // update disruptive sensor status
    public void updateDeviceDisruptiveStatus(String disruptive_sensor_id) {
        try {
            String device_id = disruptiveService.getDeviceIdByDisruptiveSensorId(disruptive_sensor_id);

            this.updateDeviceDisruptiveStatusByDeviceId(device_id);
        } catch (Exception e) {
            System.out.println("Error in updating disruptive status " + e);
        }
    }

    // update disruptive sensor status by device id
    public void updateDeviceDisruptiveStatusByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Boolean alert = disruptiveService.getDisruptiveSensorAlertStatusByDeviceId(device_id);
                String disruptive_status = "no-alert";
                if (alert) {
                    disruptive_status = "alert";
                }
                deviceRepository.updateDeviceDisruptiveStatus(device_id, disruptive_status);
            }
        } catch (Exception e) {
            System.out.println("Error in updating disruptive status by device id " + e);
        }
    }

    // update my devices sensor count
    public void updateDeviceMyDevicesCount(String my_devices_sensor_id) {
        try {
            String device_id = myDevicesService.getDeviceIdByMyDevicesSensorId(my_devices_sensor_id);
            this.updateDeviceMyDevicesCountByDeviceId(device_id);
        } catch (Exception e) {
            System.out.println("Error in updating my devices count " + e);
        }
    }

    // update my devices sensor count by device id
    public void updateDeviceMyDevicesCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer my_devices_count = myDevicesService.getMyDevicesSensorCountByDeviceId(device_id);
                deviceRepository.updateDeviceMyDevicesCount(device_id, my_devices_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating my devices count by device id " + e);
        }
    }

    // update my devices sensor status
    public void updateDeviceMyDevicesStatus(String my_devices_sensor_id) {
        try {
            String device_id = myDevicesService.getDeviceIdByMyDevicesSensorId(my_devices_sensor_id);

            this.updateDeviceMyDevicesStatusByDeviceId(device_id);

        } catch (Exception e) {
            System.out.println("Error in updating my devices status " + e);
        }
    }

    // update my devices sensor status by device id
    public void updateDeviceMyDevicesStatusByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Boolean alert = myDevicesService.getMyDevicesSensorAlertStatusByDeviceId(device_id);
                String my_devices_status = "no-alert";
                if (alert) {
                    my_devices_status = "alert";
                }
                deviceRepository.updateDeviceMyDevicesStatus(device_id, my_devices_status);
            }
        } catch (Exception e) {
            System.out.println("Error in updating my devices status by device id " + e);
        }
    }

    // update monnit sensor count
    public void updateDeviceMonnitCount(String monnit_sensor_id) {
        try {
            String device_id = monnitService.getDeviceIdByMonnitSensorId(monnit_sensor_id);
            this.updateDeviceDisruptiveCountByDeviceId(device_id);
        } catch (Exception e) {
            System.out.println("Error in updating monnit count " + e);
        }
    }

    // update monnit count by device id
    public void updateDeviceMonnitCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer monnit_count = monnitService.getMonnitCountByDeviceId(device_id);
                deviceRepository.updateDeviceMonnitCount(device_id, monnit_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating monnit count by device id " + e);
        }
    }

    // update monnit status
    public void updateDeviceMonnitStatus(String monnit_sensor_id) {
        try {
            String device_id = monnitService.getDeviceIdByMonnitSensorId(monnit_sensor_id);

            this.updateDeviceMonnitStatusByDeviceId(device_id);

        } catch (Exception e) {
            System.out.println("Error in updating monnit status " + e);
        }
    }

    // update monnit status by device id
    public void updateDeviceMonnitStatusByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Boolean alert = monnitService.getMonnitAlertStatusByDeviceId(device_id);
                String monnit_status = "no-alert";
                if (alert) {
                    monnit_status = "alert";
                }
                deviceRepository.updateDeviceMonnitStatus(device_id, monnit_status);
            }
        } catch (Exception e) {
            System.out.println("Error in updating monnit status by device id " + e);
        }
    }

    // update pelican sensor count
    public void updateDevicePelicanCount(String pelican_sensor_id) {
        try {
            String device_id = pelicanService.getDeviceIdByPelicanSensorId(pelican_sensor_id);
            this.updateDevicePelicanCountByDeviceId(device_id);
        } catch (Exception e) {
            System.out.println("Error in updating pelican count " + e);
        }
    }

    // update pelican sensor count by device id
    public void updateDevicePelicanCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer pelican_count = pelicanService.getPelicanSensorCountByDeviceId(device_id);
                deviceRepository.updateDevicePelicanCount(device_id, pelican_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating pelican count by device id " + e);
        }
    }

    // update pelican sensor status
    public void updateDevicePelicanStatus(String pelican_sensor_id) {
        try {
            String device_id = pelicanService.getDeviceIdByPelicanSensorId(pelican_sensor_id);
            this.updateDevicePelicanStatusByDeviceId(device_id);
        } catch (Exception e) {
            System.out.println("Error in updating pelican status " + e);
        }
    }

    // update pelican sensor status by device id
    public void updateDevicePelicanStatusByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Boolean alert = pelicanService.getPelicanSensorAlertStatusByDeviceId(device_id);
                String pelican_status = "no-alert";
                if (alert) {
                    pelican_status = "alert";
                }
                deviceRepository.updateDevicePelicanStatus(device_id, pelican_status);
            }
        } catch (Exception e) {
            System.out.println("Error in updating pelican status by device id " + e);
        }
    }

    // update knx group count
    public void updateDeviceKNXCount(String knx_device_address, String knx_group_address) {
        try {
            String device_id = knxService.getDeviceIdByKNXGroupAddress(knx_device_address, knx_group_address);
            this.updateDeviceKNXCountByDeviceId(device_id);
        } catch (Exception e) {
            System.out.println("Error in updating knx count " + e);
        }
    }

    // update knx group count by device id
    public void updateDeviceKNXCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer knx_count = knxService.getKNXGroupCountByDeviceId(device_id);
                deviceRepository.updateDeviceKNXCount(device_id, knx_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating knx count by device id " + e);
        }
    }

    // update knx group status
    public void updateDeviceKNXStatus(String knx_device_address, String knx_group_address) {
        try {
            String device_id = knxService.getDeviceIdByKNXGroupAddress(knx_device_address, knx_group_address);
            this.updateDeviceKNXStatusByDeviceId(device_id);

        } catch (Exception e) {
            System.out.println("Error in updating knx status " + e);
        }
    }

    // update knx group status by device id
    public void updateDeviceKNXStatusByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Boolean alert = knxService.getKNXGroupAlertStatusByDeviceId(device_id);
                String knx_status = "no-alert";
                if (alert) {
                    knx_status = "alert";
                }
                deviceRepository.updateDeviceKNXStatus(device_id, knx_status);
            }
        } catch (Exception e) {
            System.out.println("Error in updating knx status by device id " + e);
        }
    }

    // update measure count by device id
    public void updateDeviceMeasureCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer measure_count = measuringInstrumentService.getInstrumentCountByDeviceId(device_id);
                deviceRepository.updateDeviceMeasureCount(device_id, measure_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating measure count by device id " + e);
        }
    }

    // update documents count by device id
    public void updateDeviceDocumentsCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer documents_count = documentService.getDocumentsCountByDeviceId(device_id);
                deviceRepository.updateDeviceDocumentsCount(device_id, documents_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating documents count by device id " + e);
        }
    }

    // update media count by device id
    public void updateDeviceMediaCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer media_count = mediaService.getMediasCountByDeviceId(device_id);
                deviceRepository.updateDeviceMediaCount(device_id, media_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating media count by device id " + e);
        }
    }

    // update checklists count by device id
    public void updateDeviceCheckListsCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer checklists_count = checkListTemplateService.getCheckListTemplatesCountByDeviceId(device_id);
                deviceRepository.updateDeviceCheckListsCount(device_id, checklists_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating checklists count by device id " + e);
        }
    }

    //update snmp object count by device id
    public void updateDeviceSnmpObjectCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer snmp_object_count = snmpService.getSnmpObjectCountByDeviceId(device_id);
                deviceRepository.updateDeviceSnmpObjectCount(device_id, snmp_object_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating snmp object count by device id " + e);
        }
    }

    //update snmp object status by oid and configuration id
    public void updateDeviceSnmpObjectStatus(String snmp_device_configuration_id, String snmp_object_oid) {
        try {
            String device_id = snmpService.getDeviceIdBySnmpObjectId(snmp_device_configuration_id, snmp_object_oid);

            this.updateDeviceSnmpObjectStatusByDeviceId(device_id);

        } catch (Exception e) {
            System.out.println("Error in updating snmp object status " + e);
        }
    }

    //update snmp object status by device id
    public void updateDeviceSnmpObjectStatusByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Boolean alert = snmpService.getSnmpObjectAlertStatusByDeviceId(device_id);
                String snmp_object_status = "no-alert";
                if (alert) {
                    snmp_object_status = "alert";
                }
                deviceRepository.updateDeviceSnmpObjectStatus(device_id, snmp_object_status);
            }
        } catch (Exception e) {
            System.out.println("Error in updating device snmp object status by device id " + e);
        }
    }


    public AllSensorsDTO getDeviceSensors(String username, String vdmsid, String dockername, String device_id) {
        AllSensorsDTO sensors = new AllSensorsDTO();

        sensors.setLorawan_sensors(lorawanService.getDeviceLorawanSensors(username, vdmsid, dockername, device_id));
        sensors.setBacnet_objects(bacnetService.getDeviceBacnetObjects(username, vdmsid, dockername, device_id));
        sensors.setDisruptive_sensors(disruptiveService.getDeviceDisruptiveSensors(username, vdmsid, device_id));
        sensors.setData_hoist_devices(dataHoistService.getDataHoistDeviceById(username, vdmsid, device_id));
        sensors.setMy_devices_sensors(myDevicesService.getDeviceMyDevicesSensors(username, vdmsid, device_id));
        sensors.setMonnit_sensors(monnitService.getDeviceMonnitSensors(username, vdmsid, device_id));
        sensors.setPelican_sensors(pelicanService.getDevicePelicanSensors(username, vdmsid, device_id));
        sensors.setKnx_groups(knxService.getDeviceKNXGroups(username, vdmsid, dockername, device_id));
        sensors.setSnmp_objects(snmpService.getDeviceSnmpObjects(username, vdmsid, dockername, device_id));
        sensors.setDaintree_devices(daintreeService.getDaintreeDevicesByDeviceId(username, vdmsid, dockername, device_id));
        sensors.setEcobee_sensors(ecobeeService.getEcobeeDevicesByDeviceId(username, vdmsid, dockername, device_id));
        sensors.setModbus_registers(modbusService.getDeviceModbusRegisters(username, vdmsid, dockername, device_id));
        sensors.setPoly_lens_devices(polyLensService.getAllPolyLensDevices(username, vdmsid, dockername, device_id));
        sensors.setMqtt_devices(mqttService.getAllMqttDevices(username, vdmsid, dockername, device_id));
        return sensors;
    }

    // ********************************************************** Toushscreen
    // required Services
    // ****************************************************************************************************************************
    //
    //To be removed after new pagination api works
    public Set<DeviceListDTO> listDevicesTs(String networkname, String buildingid, String floorid, String
            locationid,
                                            Integer devicestatus) {
        return deviceRepository.listDevicesTs(networkname, buildingid, floorid, locationid, devicestatus);
    }

    // Added Pagination for listDevices
    public Set<DeviceListDTO> listDevicesByPaginationTs(String networkname, String buildingid, String floorid, String locationid, Integer status, Integer pagesize, Integer offset, Integer virtual_device_type) {
        return deviceRepository.listDevicesByPaginationTs(networkname, buildingid, floorid, locationid, status, pagesize, offset, virtual_device_type);
    }

    public DeviceDetailsDTO getDeviceInfoById(String deviceid) {

        return deviceRepository.getDeviceInfoById(deviceid);
    }

    public Map<String, Integer> onlineOfflineCount() {

        Map<String, Integer> deviceStatuscount = new HashMap<>();

        deviceStatuscount.put("online_device_count", deviceRepository.onlineOfflineCount(1));
        deviceStatuscount.put("offline_device_count", deviceRepository.onlineOfflineCount(0));
        deviceStatuscount.put("other_device_count", deviceRepository.otherDeviceCountByDocker("all"));
        deviceStatuscount.put("all_device_count", deviceRepository.getMonitoredDeviceCountByDocker("all"));

        return deviceStatuscount;
    }

    // *************************************** Monitor required Services

    // public void deviceUpsertbyId(String dockername, List<DeviceMonitorDTO>
    // deviceMonitors) {
    //
    // System.out.println("Device**UPSET " + deviceMonitors.toString());
    // for(DeviceMonitorDTO device : deviceMonitors)
    // {
    //
    // if(deviceRepository.checkDeviceByDeviceId(device.getMac_address(),
    // device.getVdms_id(), device.getDocker_name()) != 0)
    // {
    // if(device.getAlarm() != 7)
    // {
    // try {
    // this.updateOnlineStatus(device);
    // this.updateofflineStatus(device);
    // } catch (Exception e) {
    // System.out.println("error in online offline");
    // }
    // }
    //
    // try {
    // deviceRepository.updateDeviceStatus(device.getIp_address(),
    // device.getMac_address(), device.getStatus(),
    // device.getAlarm(), device.getLast_seen_on(), device.getVdms_id(),
    // device.getDocker_name(), device.getId());
    // } catch (Exception e) {
    // System.out.println("error in update device");
    // }
    //
    // }else
    // {
    // try {
    // deviceRepository.insertDeviceStatus(device.getIp_address(),
    // device.getMac_address(), device.getStatus(),
    // device.getAlarm(), device.getLast_seen_on(), device.getVdms_id(),
    // device.getDocker_name(), device.getId(), "");
    //
    // } catch (Exception e) {
    // System.out.println("error in Insert device");
    // }
    //
    // }
    //
    // if(device.getAlarm() != 3 && device.getAlarm() != 4 ) {
    //
    // try {
    //
    // historyService.insertDeviceStatusHistory(device.getAlarm(), device.getId());
    //
    // if(device.getAlarm() == 7 )
    // {
    // socketservice.socketOfflineOnlineDevice(device.getId());
    // }
    //
    // socketservice.socketDeviceStatus(device);
    // socketservice.socketDeviceCount();
    // socketservice.sockerDeviceCountByDocker(dockername);
    // } catch (Exception e) {
    // // TODO: handle exception
    // }
    // }
    // }
    // }

    // Device Upsert by monitor code newly updated
    public void deviceUpsertbyId(String dockername, List<DeviceMonitorDTO> deviceMonitors, String assignee) {

        System.out.println("Device**UPSET " + deviceMonitors.toString());
        for (DeviceMonitorDTO device : deviceMonitors) {
            String ipAddress = null;
            if (deviceRepository.checkDeviceByDeviceId(device.getMac_address(), device.getVdms_id(),
                    device.getDocker_name()) != 0) {

                if (device.getStatus() != null) {
                    try {
                        this.updateOnlineStatus(device);
                        this.updateofflineStatus(device);
                    } catch (Exception e) {
                        System.out.println("error in upsert device by monitor online/offline device " + e);
                        System.out.println(e);
                    }
                }

                try {
                    List<DeviceIPAddressDTO> deviceIPAddresses = device.getIp_addresses();
                    if (deviceIPAddresses != null && deviceIPAddresses.size() < 10) {
                        List<DeviceIPAddressDTO> oldDeviceIPAddresses = this
                                .getDeviceIPAddressByDeviceId(device.getId());
                        System.out.println("oldDeviceIPAddresses " + oldDeviceIPAddresses);
                        deviceIPAddressRepository.deleteIPAddressByDeviceId(device.getId());

                        ipAddress = deviceIPAddresses.get(0).getIp_address();

                        for (int i = 0; i < deviceIPAddresses.size(); i++) {
                            DeviceIPAddressDTO deviceIPAddress = deviceIPAddresses.get(i);
                            String id = Generators.timeBasedGenerator().generate().toString();

                            if (deviceIPAddress.getIp_conflict_status() != null) {
                                System.out.println("Inside 1stt" + deviceIPAddress.toString());
                                deviceIPAddressRepository.insertIPAddressByDeviceId(id, deviceIPAddress.getIp_address(),
                                        deviceIPAddress.getIp_conflict_status(), device.getId());
                            } else {
                                for (int j = 0; j < oldDeviceIPAddresses.size(); j++) {
                                    DeviceIPAddressDTO oldDeviceIPAddress = oldDeviceIPAddresses.get(j);
                                    System.out.println("oldDeviceIPAddress " + oldDeviceIPAddress.getIp_address()
                                            + "      device.getIp_address() " + device.getIp_address());
                                    if (oldDeviceIPAddress.getIp_address().equals(deviceIPAddress.getIp_address())) {
                                        System.out.println("Inside 2nd" + deviceIPAddress.toString());
                                        deviceIPAddress
                                                .setIp_conflict_status(oldDeviceIPAddress.getIp_conflict_status());
                                    }
                                }
                                System.out.println("Inside 3nd" + deviceIPAddress.toString());
                                deviceIPAddressRepository.insertIPAddressByDeviceId(id, deviceIPAddress.getIp_address(),
                                        deviceIPAddress.getIp_conflict_status(), device.getId());

                            }
                        }
                    }

                    deviceRepository.updateDeviceStatus(ipAddress, device.getMac_address(), device.getStatus(),
                            device.getLast_seen_on(), device.getVdms_id(), device.getDocker_name(), device.getId());
                } catch (Exception e) {
                    System.out.println("error in upsert device by monitor update device " + e);
                    System.out.println("***********************************************************************");
                    System.out.println(device.toString());
                    System.out.println("***********************************************************************");
                    System.out.println(e);
                }

            } else {
                try {
                    List<DeviceIPAddressDTO> deviceIPAddresses = device.getIp_addresses();

                    if (deviceIPAddresses != null) {
                        ipAddress = deviceIPAddresses.get(0).getIp_address();
                        BigInteger created_timestamp = BigInteger.valueOf(System.currentTimeMillis());
                        deviceRepository.insertDeviceStatus(ipAddress, device.getMac_address(), device.getStatus(),
                                device.getLast_seen_on(), device.getVdms_id(), device.getDocker_name(), device.getId(),
                                "", created_timestamp, "Auto Discovered");

                        for (int i = 0; i < deviceIPAddresses.size(); i++) {
                            DeviceIPAddressDTO deviceIPAddress = deviceIPAddresses.get(i);
                            String id = Generators.timeBasedGenerator().generate().toString();
                            deviceIPAddressRepository.insertIPAddressByDeviceId(id, deviceIPAddress.getIp_address(),
                                    deviceIPAddress.getIp_conflict_status(), device.getId());
                        }
                        // update vendor by mac address and hostname by ip address
                        this.updateVendorByMacAddress(device.getId(), device.getMac_address());
                        this.getDeviceHostNameByIP(dockername, device.getId(), ipAddress);

                        this.updateDeviceOnboardStatusByAutoDiscovered(device, "Auto Discovered");
                    }

                } catch (Exception e) {
                    System.out.println("error in upsert device by monitor insert device " + e);
                    System.out.println("***********************************************************************");
                    System.out.println(device.toString());
                    System.out.println("***********************************************************************");
                    System.out.println(e);
                }

            }

            if (device.getStatus() != null) {
                try {
                    socketservice.socketDeviceStatus(device);
                    socketservice.socketDeviceCount();
                    socketservice.sockerDeviceCountByDocker(dockername,assignee);
                } catch (Exception e) {
                    System.out.println("error in upsert device by monitor socket event " + e);
                }

            }

        }
    }

    public void updateVendorByMacAddress(String device_id, String mac_address) {
        try {
            asyncService.updateVendorByMacAddress(device_id, mac_address);
        } catch (Exception e) {
            System.out.println("error updating device vendor " + e);
            System.out.println(e);
        }
    }

    public void getDeviceHostNameByIP(String dockername, String device_id, String ip_address) {
        try {
            String internal_ip_address = dockerService.getDockerInternalIp(dockername);
            String display_name = apicallService.getDeviceHostNameByIP(internal_ip_address, ip_address);
            System.out.println("********************************************************");
            System.out.println("Display Name: " + display_name);
            System.out.println("********************************************************");
            this.updateDevicesDisplayNameById(device_id, display_name);
        } catch (Exception e) {
            System.out.println("error updating device vendor/hostname " + e);
            System.out.println(e);
        }

    }

    public List<DeviceIPAddressDTO> getDeviceIPAddressByDeviceId(String device_id) {
        return deviceIPAddressRepository.getIPAddressByDeviceId(device_id);
    }

    // Insert devices history, newly added api after monitor code changes
    public void insertDevicesHistory(String dockername, List<DeviceHistoryDTO> devicesHistory) {
        for (DeviceHistoryDTO deviceHistory : devicesHistory) {
            try {
                historyService.insertDeviceStatusHistory(deviceHistory.getAlarm(), deviceHistory.getIp_address(),
                        deviceHistory.getOld_ip_address(), deviceHistory.getTimestamp(), deviceHistory.getId());
                rabbitmqService.rabbitmqDeviceEvent(dockername, deviceHistory);
            } catch (Exception e) {
                System.out.println("************************History******************************************");
                System.out.println(deviceHistory);
                System.out.println("************************History******************************************");
                System.out.println(e);
            }
        }

    }

    public void updateOnlineStatus(DeviceMonitorDTO device) {
        if (device.getStatus().equals(1)) {
            Integer deviceStatus = this.getDeviceStatus(device.getId());
            if (deviceStatus != null && deviceStatus == 0) {
                if (device.getStatus() == 1) {
                    try {
                        socketservice.socketOnlineDevice(device.getId());
                        // new changes
                        this.getDeviceConditionStatus(device.getId(), device.getStatus());
                        String deviceConditionId=deviceConditionsService.deviceConditionsRepository.getDeviceConditionIdByDeviceId(device.getId());
                        System.out.println("Device Condition Id: " + deviceConditionId);
                        if(deviceConditionId!=null) {
                            aiCallService.updateDeviceOnlineStatus(device.getId(), device.getStatus(),deviceConditionId);
                        }
                        else{
                            aiCallService.updateDeviceOnlineStatus(device.getId(), device.getStatus(),null);
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }

    }

    public void updateofflineStatus(DeviceMonitorDTO device) {

        if (device.getStatus().equals(0)) {
            String parentDevice = deviceRepository.getDeviceparent(device.getId());
            System.out.println("Parent Device: " + parentDevice);

            if (parentDevice == null || parentDevice.equals("no_parent")) {
                String snmpParentDevice = deviceRepository.getDeviceSnmpparent(device.getId());
                if (snmpParentDevice != null) {
                    if (this.getDeviceStatus(snmpParentDevice) == 1) {
                        socketservice.socketOfflineDevice(device.getId());
                        if(device.getId() != null && device.getStatus() == 0) {
                            BigInteger dndTimestamp = BigInteger.valueOf(System.currentTimeMillis());
                            Boolean aiCallEnabled = deviceRepository.checkAiCallEnableStatus(device.getId());
                            System.out.println("aiCallEnabled: " + aiCallEnabled);
                            if (aiCallEnabled == null) {
                                aiCallEnabled = false;
                                log.info("aiCallEnabled was null, set to false for device: {} ", device.getId());
                            }

                            Boolean aiCallDndEnabled = deviceRepository.checkAiCallDndEnabled(device.getId());
                            if (aiCallDndEnabled == null) {
                                aiCallDndEnabled = false;
                               log.info("aiCallDndEnabled was null, set to false for device {} ", device.getId());
                            }

                            if(aiCallEnabled && !aiCallDndEnabled) {
                                String issueType = "Device Offline";
                                this.UpdateDeviceDndEnabledAndTimestamp(device.getId(),dndTimestamp);
                                aiCallService.createCallLog(device.getId(), issueType);
                            }
                            else {
                                log.info("aiCallEnabled or aiCallDndEnabled is false for device: {} ", device.getId());
                            }
                        }

//                        try {
//                            // Device Offline Alert
//                            alertService.sendDeviceAlert(device.getId());
//                        } catch (Exception e) {
//                            // TODO: handle exception
//                        }

                    }
                } else {
                    socketservice.socketOfflineDevice(device.getId());

//                    try {
//                        // Device Offline Alert
//                        alertService.sendDeviceAlert(device.getId());
//                    } catch (Exception e) {
//                        // TODO: handle exception
//                    }

                }

            } else {
                if (this.getDeviceStatus(parentDevice) == 1) {
                    socketservice.socketOfflineDevice(device.getId());
                    if(device.getId() != null && device.getStatus() == 0) {
                        BigInteger dndTimestamp = BigInteger.valueOf(System.currentTimeMillis());
                        Boolean aiCallEnabled = deviceRepository.checkAiCallEnableStatus(device.getId());
                        if (aiCallEnabled == null) {
                            aiCallEnabled = false;
                            log.info("aiCallEnabled was null, set to false for device: {} ", device.getId());
                        }

                        Boolean aiCallDndEnabled = deviceRepository.checkAiCallDndEnabled(device.getId());
                        if (aiCallDndEnabled == null) {
                            aiCallDndEnabled = false;
                            log.info("aiCallDndEnabled was null, set to false for device {} ", device.getId());
                        }

                        if(aiCallEnabled && !aiCallDndEnabled) {
                            String issueType = "Device Offline";
                            this.UpdateDeviceDndEnabledAndTimestamp(device.getId(),dndTimestamp);
                            aiCallService.createCallLog(device.getId(), issueType);
                        }
                    }

//                    try {
//                        // Device Offline Alert
//                        alertService.sendDeviceAlert(device.getId());
//                    } catch (Exception e) {
//                        // TODO: handle exception
//                    }
                }

            }

            Integer deviceStatus = this.getDeviceStatus(device.getId());
            if (deviceStatus != null && deviceStatus == 1) {
                if (device.getStatus() == 0) {
                    try {

                        this.getDeviceConditionStatus(device.getId(), device.getStatus());

                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }

        }
    }


    public Integer getDeviceStatus(String DeviceId) {

        return deviceRepository.getDeviceStatus(DeviceId);
    }

    public List<DeviceMonitorDTO> getDeviceListMonitor(String dockername) {

        return deviceRepository.getDeviceListMonitor(dockername);
    }

    public List<DeviceMonitorDTO> getDeviceListMonitorIp(String dockername) {

        return deviceRepository.getDeviceListMonitorIp(dockername);
    }

    public List<SnmpValuesDTO> getDeviceListSnmp(String dockername) {
        // TODO Auto-generated method stub
        return deviceRepository.getDeviceListSnmp(dockername);
    }

    // Get Single Device Info By Device Id for Snmp
    public SnmpValuesDTO getDeviceSnmpByDeviceId(String dockername, String device_id) {
        // TODO Auto-generated method stub
        return deviceRepository.getDeviceSnmpByDeviceId(dockername, device_id);
    }

    public void updateSnmpParent(String dockername, SnmpValuesDTO device) {
        try {
            deviceRepository.updateSnmpParent(dockername, device.getId(), device.getSnmp_parent(), device.getDevice_type());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void updateTopology(String username, String vdmsid, String
            dockername, List<DeviceTopologyDTO> devices, HttpServletRequest httpServletRequest) {
        log.info("updateTopology, Params: devices: {}, endpoint : {}", devices, httpServletRequest.getRequestURI());
        if (devices != null && devices.size() > 0) {
            for (DeviceTopologyDTO device : devices) {
                try {
                    deviceRepository.updateDeviceParent(device.getId(), device.getParent(), device.getUser_connection_type());
                    log.info("device parent : {}", device.getParent());
                    userActionLogService.addUserAction(username, "asset", "UPDATE", "Topology is updated  for Network  " + dockername, "success", "topology", null);
                } catch (Exception e) {
                    log.error("Exception. Params: devices: {}, endpoint : {}", devices, httpServletRequest, e);
                    userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to update Topology for Network  " + dockername, "failed", "topology", null);
                }
            }
        }
    }

    public String getGatewayId(String dockername) {
        String gatewayIp = dockerService.getGatewayIp(dockername);

        return deviceRepository.getGatewayIdFromGatewayIp(gatewayIp);
    }

    //To be removed after pagination api works
    public Set<DeviceListDTO> listofflinedeviceByParentTs() {
        // TODO Auto-generated method stub
        return deviceRepository.listofflinedeviceByParentTs();
    }

    //Added pagination for listofflinedeviceByParentTs
    public Set<DeviceListDTO> listofflinedeviceByParentByPaginationTs(Integer pagesize, Integer offset) {
        // TODO Auto-generated method stub
        return deviceRepository.listofflinedeviceByParentByPaginationTs(pagesize, offset);
    }

    public DeviceListDTO DeviceInfoById(String deviceId) {

        return deviceRepository.DeviceInfoById(deviceId);
    }

    public void unLinkVendorByVendorIdAndVdmsId(String phoneaccountid, String vdmsid) {
        deviceRepository.unLinkVendorByVendorIdAndVdmsId(phoneaccountid, vdmsid);
    }

    public List<DeviceDTO> listAlldevices() {
        return deviceRepository.listAlldevices();
    }

    public void updateDevicesDisplayNameById(String id, String display_name) {
        deviceRepository.updateDevicesDisplayNameById(id, display_name);
    }

    public void updateDeviceVendorById(String id, String vendor) {

        deviceRepository.updateDeviceVendorById(id, vendor);
    }

    // to get device info for device alert
    public AlertDTO getDeviceAlertInfoByDeviceId(String device_id) {
        return deviceRepository.getDeviceAlertInfoByDeviceId(device_id);
    }

    public Integer getDeviceCountForIOC(String username, String vdmsid, String dockername) {
        return deviceRepository.getAllDeviceCountByDocker(dockername);
    }

    public Map<String, Integer> getDeviceCount(String username, String vdmsid, String dockername,String assignee) {

        Map<String, Integer> deviceStatuscountAll = new HashMap<>();

        Integer onlineDeviceCount = deviceRepository.onlineOfflineCountByDocker(dockername, 1, assignee);
        Integer offlineDeviceCount = deviceRepository.onlineOfflineCountByDocker(dockername, 0, assignee);
        Integer unmonitoredDeviceCount = deviceRepository.unmonitorCountByDocker(dockername,assignee);
        Integer otherDeviceCount = deviceRepository.otherDeviceCountByDockerAssignee(dockername,assignee);
        Integer allDeviceCount = deviceRepository.getAllDeviceCountByDockerAssignee(dockername,assignee);

//        Integer matchedDeviceCount = deviceRepository.getMatchedUnmatchedDeviceCountByDocker(dockername, 1);
//        Integer unmatchedDeviceCount = deviceRepository.getMatchedUnmatchedDeviceCountByDocker(dockername, 0);
//        Integer verifiedDeviceCount = deviceRepository.getMatchedUnmatchedDeviceCountByDocker(dockername, 2);
        Integer archivedDeviceCount = deviceRepository.getMatchedUnmatchedDeviceCountByDocker(dockername, 3, assignee);

        Integer onboardedCount = deviceRepository.getOnboardedDeviceCountByDocker(dockername,assignee);
        Integer notOnboardedCount = deviceRepository.getNotOnboardedDeviceCountByDocker(dockername,assignee);

        Integer monitoredCount = deviceRepository.monitorCountByDocker(dockername,assignee);
        Integer assignedCount = deviceRepository.assignedCountByDocker(dockername,assignee);
        Integer unAssignedCount = deviceRepository.unAssignedCountByDocker(dockername,assignee);

        deviceStatuscountAll.put("online_device_count", onlineDeviceCount);
        deviceStatuscountAll.put("offline_device_count", offlineDeviceCount);
        deviceStatuscountAll.put("unmonitor_device_count", unmonitoredDeviceCount);
        deviceStatuscountAll.put("other_device_count", otherDeviceCount);
        deviceStatuscountAll.put("all_device_count", allDeviceCount);

//        deviceStatuscountAll.put("matched_device_count", matchedDeviceCount);
//        deviceStatuscountAll.put("unmatched_device_count", unmatchedDeviceCount);
//        deviceStatuscountAll.put("verified_device_count", verifiedDeviceCount);
        deviceStatuscountAll.put("archived_device_count", archivedDeviceCount);

        deviceStatuscountAll.put("onboarded_device_count", onboardedCount);
        deviceStatuscountAll.put("notonboarded_device_count", notOnboardedCount);
        deviceStatuscountAll.put("monitored_device_count", monitoredCount);
        deviceStatuscountAll.put("assigned_device_count", assignedCount);
        deviceStatuscountAll.put("unassigned_device_count", unAssignedCount);


        return deviceStatuscountAll;
    }

    public DeviceTopologyDTO getDeviceObjectbyId(List<DeviceTopologyDTO> devices, String id) {
        if (devices != null && devices.size() > 0) {
            for (DeviceTopologyDTO device : devices) {
                if (device.getId() != null && device.getId().equals(id)) {
                    return device;
                }
            }
        }
        return null;
    }

    public List<DeviceTopologyDTO> listFormatedDevicesForAllNetwork(List<DeviceTopologyDTO> devices) {
        Map<String, String> map_id_to_distinct_id = new HashMap<String, String>();

        List<DeviceTopologyDTO> formated_devices = new ArrayList<DeviceTopologyDTO>();

        Map<String, List<String>> mac_address_list = new HashMap<String, List<String>>();

        if (devices != null && devices.size() > 0) {
            for (DeviceTopologyDTO device : devices) {
                try {
                    if (device.getMac_address() != null) {

                        if (mac_address_list.containsKey(device.getMac_address())) {
                            List<String> ids = mac_address_list.get(device.getMac_address());
                            ids.add(device.getId());

                            map_id_to_distinct_id.put(device.getId(), ids.get(0));
                            DeviceTopologyDTO formated_device = getDeviceObjectbyId(formated_devices, ids.get(0));
                            if (formated_device != null) {
                                if (formated_device.getSnmp_parent_ids() != null && device.getSnmp_parent() != null && !formated_device.getSnmp_parent_ids().contains(device.getSnmp_parent())) {
                                    formated_device.getSnmp_parent_ids().add(device.getSnmp_parent());
                                }

                                if (formated_device.getParent_ids() != null && device.getParent() != null && !formated_device.getParent_ids().contains(device.getParent())) {
                                    formated_device.getParent_ids().add(device.getParent());
                                }
                            }

                        } else {
                            List<String> ids = new ArrayList<String>();
                            ids.add(device.getId());
                            mac_address_list.put(device.getMac_address(), ids);

                            List<String> parents = new ArrayList<String>();
                            if (device.getParent() != null) {
                                parents.add(device.getParent());
                            }
                            device.setParent_ids(parents);


                            List<String> snmp_parents = new ArrayList<String>();
                            if (device.getSnmp_parent() != null) {
                                snmp_parents.add(device.getSnmp_parent());
                            }
                            device.setSnmp_parent_ids(snmp_parents);

                            formated_devices.add(device);
                        }
                    } else {

                        if (device.getParent() != null) {
                            List<String> parents = new ArrayList<String>();
                            parents.add(device.getParent());
                            device.setParent_ids(parents);
                        }

                        formated_devices.add(device);
                    }
                } catch (Exception e) {
                    System.out.println("FAILED TO FETCH DEVICE IN FORMATING FOR ALL NETWORK TOPOLOGY WITH DEVICE " + device.getId());
                }
            }

            System.out.println("TOTAL COUNT : " + map_id_to_distinct_id.size());


            for (DeviceTopologyDTO device : formated_devices) {
                try {

                    List<String> filtered_parent_ids = new ArrayList<String>();
                    List<String> parent_ids = device.getParent_ids();
                    if (parent_ids != null) {
                        for (String id : parent_ids) {
                            if (map_id_to_distinct_id.containsKey(id)) {
                                if (!filtered_parent_ids.contains(map_id_to_distinct_id.get(id))) {
                                    filtered_parent_ids.add(map_id_to_distinct_id.get(id));
                                }
                            } else {
                                filtered_parent_ids.add(id);
                            }
                        }
                        device.setParent_ids(filtered_parent_ids);
                    }


                    List<String> filtered_snmp_parent_ids = new ArrayList<String>();
                    List<String> snmp_parent_ids = device.getSnmp_parent_ids();
                    if (snmp_parent_ids != null) {
                        for (String id : snmp_parent_ids) {
                            if (map_id_to_distinct_id.containsKey(id)) {
                                if (!filtered_snmp_parent_ids.contains(map_id_to_distinct_id.get(id))) {
                                    filtered_snmp_parent_ids.add(map_id_to_distinct_id.get(id));
                                }
                            } else {
                                filtered_snmp_parent_ids.add(id);
                            }
                        }

                        device.setSnmp_parent_ids(filtered_snmp_parent_ids);
                    }


                    if (device.getMac_address() != null && mac_address_list.containsKey(device.getMac_address())) {

                        device.setIds(mac_address_list.get(device.getMac_address()));
                    }

                } catch (Exception e) {
                    System.out.println(e);
                    System.out.println("FAILED TO FETCH DEVICE IN ALL NETWORK TOPOLOGY FOR DEVICE " + device.getId());
                }

            }
        }
        return formated_devices;
    }


    public List<DeviceTopologyDTO> listTopologyDevicesByDockerName(String username, String vdmsid, String
            dockername) {
        try {
            List<DeviceTopologyDTO> devices = deviceRepository.listTopologyDevices(dockername, null);
            if (dockername.equals("all")) {
                devices = listFormatedDevicesForAllNetwork(devices);
            } else {
                int gatewayindex = 0;
                for (int i = 0; i < devices.size(); i++) {
                    if (devices.get(i).getId().equals(devices.get(i).getSnmp_parent())) {
                        gatewayindex = i;
                        break;
                    }
                }

                for (int i = 0; i < devices.size(); i++) {
                    boolean flag = true;
                    for (int j = 0; j < devices.size(); j++) {
                        if (devices.get(i).getSnmp_parent() != null && devices.get(i).getSnmp_parent().equals(devices.get(j).getId())) {
                            flag = false;
                            break;
                        }
                    }

                    boolean parentFlag = true;
                    for (DeviceTopologyDTO device : devices) {
                        if (devices.get(i).getParent() != null && devices.get(i).getParent().equals(device.getId())) {
                            parentFlag = false;
                            break;
                        }
                    }

                    if (devices.get(i).getSnmp_parent() != null && flag == true) {
                        List<DeviceTopologyDTO> newParents = new ArrayList<DeviceTopologyDTO>();
                        boolean checkedallparents = false;
                        List<DeviceTopologyDTO> newDevices = deviceRepository.listTopologyDevices(null, devices.get(i).getSnmp_parent());
                        if (newDevices != null && newDevices.size() > 0) {
                            DeviceTopologyDTO firstParent = newDevices.get(0);
                            DeviceTopologyDTO newDevice = firstParent;
                            while (!checkedallparents) {
                                if (newDevice.getId().equals(newDevice.getSnmp_parent())) {
                                    if (newDevice.getMac_address().equals(devices.get(gatewayindex).getMac_address())) {
                                        if (firstParent.getMac_address().equals(devices.get(gatewayindex).getMac_address())) {
                                            devices.get(i).setSnmp_parent(devices.get(gatewayindex).getId());
                                        } else {
                                            devices.get(i).setSnmp_parent(firstParent.getId());
                                        }
                                        devices.addAll(newParents);
                                    } else if (!firstParent.getId().equals(firstParent.getSnmp_parent())) {
                                        devices.get(i).setSnmp_parent(firstParent.getId());
                                        firstParent.setSnmp_parent(devices.get(gatewayindex).getId());
                                        devices.add(firstParent);
                                    } else {
                                        devices.get(i).setSnmp_parent(devices.get(gatewayindex).getId());
                                    }
                                    checkedallparents = true;
                                } else {
                                    if (!isparentPresentInCurrentNetwork(newDevice.getId(), devices, newParents)) {
                                        newParents.add(newDevice);
                                        newDevice = getNextParentDevice(newDevice.getSnmp_parent(), devices, newParents);
                                    } else {
                                        devices.addAll(newParents);
                                        checkedallparents = true;
                                    }
                                    if (newDevice == null) {
                                        devices.addAll(newParents);
                                        checkedallparents = true;
                                    }
                                }

                            }
                        }
                    }

                    if (devices.get(i).getParent() != null && parentFlag && !devices.get(i).getParent().equals("no_parent") && !devices.get(i).getParent().isBlank()) {
                        boolean checkedAllParents = false;
                        List<DeviceTopologyDTO> newParents = new ArrayList<DeviceTopologyDTO>();
                        List<DeviceTopologyDTO> newDevices = deviceRepository.listTopologyDevices(null, devices.get(i).getParent());
                        if (newDevices != null && newDevices.size() > 0) {
                            DeviceTopologyDTO firstParent = newDevices.get(0);
                            DeviceTopologyDTO newDevice = firstParent;
                            while (!checkedAllParents) {
                                if (newDevice.getId().equals(newDevice.getSnmp_parent())) {
                                    if (newDevice.getMac_address().equals(devices.get(gatewayindex).getMac_address())) {
                                        devices.addAll(newParents);
                                    } else if (!firstParent.getId().equals(firstParent.getSnmp_parent())) {
                                        firstParent.setParent(devices.get(gatewayindex).getId());
                                        firstParent.setSnmp_parent(devices.get(gatewayindex).getId());
                                        devices.add(firstParent);
                                    } else {
                                        devices.get(i).setParent(devices.get(gatewayindex).getId());
                                        devices.get(i).setSnmp_parent(devices.get(gatewayindex).getId());

                                    }
                                    checkedAllParents = true;
                                } else {
                                    if (!isparentPresentInCurrentNetwork(newDevice.getId(), devices, newParents)) {
                                        newParents.add(newDevice);
                                        if (newDevice.getParent() != null) {
                                            newDevice = getNextParentDevice(newDevice.getParent(), devices, newParents);
                                            if (newDevice != null && newDevice.getParent() != null && newDevice.getParent().equals("no_parent") && devices.get(i).getParent().isBlank()) {
                                                newDevice = null;
                                            }
                                        } else if (newDevice.getSnmp_parent() != null) {
                                            newDevice = getNextParentDevice(newDevice.getSnmp_parent(), devices, newParents);
                                        }
                                        if (newDevice == null) {
                                            devices.addAll(newParents);
                                            checkedAllParents = true;
                                        }
                                    } else {
                                        devices.addAll(newParents);
                                        checkedAllParents = true;
                                    }
                                }
                            }
                        }
                    }
                }

                for (DeviceTopologyDTO device : devices) {

                    if (device.getUser_data_name() != null) {
                        device.setDisplay_name(device.getUser_data_name());
                    }

                    List<InterfaceDTO> unmanaged_interfaces = new ArrayList<InterfaceDTO>();
                    List<InterfaceDTO> interfaces = interfaceService.listDeviceSnmpInterfaceByDeviceId(username, vdmsid, dockername, device.getId());
                    if (interfaces != null && interfaces.size() > 0) {

                        for (InterfaceDTO snmp_interface : interfaces) {
                            int nonhostdevices = 0;
                            if (snmp_interface.getDevices() != null && snmp_interface.getDevices().size() > 1) {
                                for (DeviceDTO connected_device : snmp_interface.getDevices()) {
                                    if (!connected_device.getDocker_name().equals(dockername)) {
                                        nonhostdevices++;
                                    }
                                }
                                if (nonhostdevices != snmp_interface.getDevices().size()) {
                                    unmanaged_interfaces.add(snmp_interface);
                                }
                            }
                        }
                    }
                    if (unmanaged_interfaces.size() > 0) {
                        device.setPorts(unmanaged_interfaces);
                    }
                }
            }
            return devices;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;

    }


    private DeviceTopologyDTO getNextParentDevice(String
                                                          deviceParent, List<DeviceTopologyDTO> devices, List<DeviceTopologyDTO> new_devices) {
        List<DeviceTopologyDTO> newDevices = null;
        DeviceTopologyDTO newDevice = null;
        if (deviceParent != null) {
            if (!isparentPresentInCurrentNetwork(deviceParent, devices, new_devices)) {
                newDevices = deviceRepository.listTopologyDevices(null, deviceParent);
            }
        }
        if (newDevices != null && newDevices.size() > 0) {
            newDevice = newDevices.get(0);
        }
        return newDevice;
    }

    private boolean isparentPresentInCurrentNetwork(String deviceId, List<DeviceDTO> devices) {
        if (deviceId != null) {
            for (DeviceDTO device : devices) {
                if (deviceId.equals(device.getId())) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private boolean isparentPresentInCurrentNetwork(String
                                                            deviceId, List<DeviceTopologyDTO> devices, List<DeviceTopologyDTO> new_devices) {
        if (deviceId != null) {
            for (DeviceTopologyDTO device : devices) {
                if (deviceId.equals(device.getId())) {
                    return true;
                }
            }

            for (DeviceTopologyDTO device : new_devices) {
                if (deviceId.equals(device.getId())) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    //reset topology
    public void resetTopologyByDockername(String username, String vdmsid, String dockername, HttpServletRequest httpServletRequest) {
        log.info("resetTopologyByDockername, Params: dockername: {}, endpoint : {}", dockername, httpServletRequest.getRequestURI());
        List<DeviceDTO> devices = deviceRepository.listTopologyDevicesByDockerName(dockername, null);
        try {
            for (DeviceDTO device : devices) {
                try {
                    if (device.getParent() != null && !device.getParent().isBlank() && !device.getParent().equals("no_parent")) {
                        boolean foreignDevice = true;
                        for (DeviceDTO parent : devices) {
                            if (device.getParent().equals(parent.getId())) {
                                foreignDevice = false;
                                break;
                            }
                        }

                        if (foreignDevice) {
                            List<DeviceDTO> foreignNetworkDevices = deviceRepository.listTopologyDevicesByDockerName(null, device.getParent());
                            if (foreignNetworkDevices != null && foreignNetworkDevices.size() > 0 && !foreignNetworkDevices.get(0).getParent().equals("no_parent")
                                    && !foreignNetworkDevices.get(0).getParent().isBlank()) {
                                DeviceDTO foreignNetworkDevice = foreignNetworkDevices.get(0);
                                if (foreignNetworkDevice.getParent() != null) {
                                    for (DeviceDTO currentNetworkDevices : devices) {
                                        if (foreignNetworkDevice.getParent().equals(currentNetworkDevices.getId())) {
                                            foreignNetworkDevice.setParent(null);
                                            break;
                                        }
                                    }
                                    deviceRepository.updateDeviceParent(foreignNetworkDevice.getId(), foreignNetworkDevice.getParent(), null);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("RESET FAILED FOR DEVICE " + device.getIp_address());
                }
            }

            deviceRepository.resetDeviceParentByDockername(dockername, null);
            log.info("dockername : {}", dockername);
            userActionLogService.addUserAction(username, "asset", "UPDATE", "Topology is re-seted for Network " + dockername, "success", "topology", null);

        } catch (Exception e) {
            log.error("Exception. Params: dockername: {}, endpoint : {}", dockername, httpServletRequest.getRequestURI(), e);
            userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to reset topology for Network " + dockername, "failed", "topology", null);
        }
    }

    public Set<DevicedataDTO> getAllDevices() {
        return deviceRepository.getAllDevices();
    }

    //get device types
    public List<String> getUniqueDeviceTypes(String network_name, String floor_id) {
        return deviceRepository.getUniqueDeviceTypes(network_name, floor_id);
    }

    public List<String> getUniqueAssetGroups(String network_name) {
        return deviceRepository.getUniqueAssetGroups(network_name);
    }

    public List<String> getUniqueCategory(String network_name) {
        return deviceRepository.getUniqueCategory(network_name);
    }

    public List<String> getUniqueAssignedUserEmail(String vdms_id,String network_name) {
        return deviceRepository.getUniqueAssignedUserEmail(vdms_id,network_name);
    }

    public List<String> getUniqueSubCategory(String network_name, String category) {
        return deviceRepository.getUniqueSubCategory(network_name, category);
    }

    public String getDevicesByTypeCount(String network_name, String floor_id, Set<String> types) {
        String deviceSensors = deviceRepository.getDevicesByTypeCount(network_name, floor_id, types);
        return deviceSensors;
    }

    private List<SensorDTO> getAllDeviceSensorsTS(String device_id) {
        List<SensorDTO> sensors = new ArrayList<SensorDTO>();

        List<SensorDTO> bacnetSensors = bacnetService.getBacnetObjectsByDeviceId(device_id);
        List<SensorDTO> lorawanSensors = lorawanService.getLorawanSensorsByDeviceId(device_id);
        //List<SensorDTO> disruptiveSensors = disruptiveService.getDisruptiveSensorsByDeviceId(device_id);
        List<SensorDTO> myDevicesSensors = myDevicesService.getMydevicesSensorsByDeviceId(device_id);
        List<SensorDTO> monnitSensors = monnitService.getMonnitSensorsByDeviceId(device_id);
        List<SensorDTO> pelicanSensors = pelicanService.getPelicanSensorsByDeviceId(device_id);
        List<SensorDTO> knxSensors = knxService.getKNXGroupsByDeviceAddress(device_id);
        List<SensorDTO> snmpDevices = snmpService.getSnmpDevicesByDeviceId(device_id);
        List<SensorDTO> measuringInstrumentSensors = measuringInstrumentService.getMeasuringInstrumentSensorsByDeviceId(device_id);
        List<SensorDTO> modbusSensoors = modbusService.getModbusRegistersByDeviceId(device_id);

        sensors.addAll(bacnetSensors);
        sensors.addAll(lorawanSensors);
        //sensors.addAll(disruptiveSensors);
        sensors.addAll(myDevicesSensors);
        sensors.addAll(monnitSensors);
        sensors.addAll(pelicanSensors);
        sensors.addAll(knxSensors);
        sensors.addAll(snmpDevices);
        sensors.addAll(measuringInstrumentSensors);
        sensors.addAll(modbusSensoors);

        return sensors;
    }

//		public List<SensorDTO> getAllDeviceSensors(String device_id)
//		{
//			List<SensorDTO> sensors = new ArrayList<SensorDTO>();
//			List<SensorDTO> bacnetSensors = bacnetService.getBacnetObjectsByDeviceIdTS(device_id);
//			List<SensorDTO> lorawanSensors = lorawanService.getLorawanSensorsByDeviceIdTS(device_id);
//			List<SensorDTO> disruptiveSensors = disruptiveService.getDisruptiveSensorsByDeviceIdTS(device_id);
//			List<SensorDTO> myDevicesSensors = myDevicesService.getMyDevicesSensorsByDeviceIdTS(device_id);
//			List<SensorDTO> monnitSensors = monnitService.getMonnitSensorsByDeviceIdTS(device_id);
//			List<SensorDTO> pelicanSensors = pelicanService.getPelicanSensorsByDeviceIdTS(device_id);
//			List<SensorDTO> knxSensors = knxService.getKNXGroupsByDeviceAddressTS(device_id);
//			List<SensorDTO> snmpDevices = snmpService.getSnmpDevicesByDeviceIdTS(device_id);
//
//			sensors.addAll(bacnetSensors);
//			sensors.addAll(lorawanSensors);
//			sensors.addAll(disruptiveSensors);
//			sensors.addAll(myDevicesSensors);
//			sensors.addAll(monnitSensors);
//			sensors.addAll(pelicanSensors);
//			sensors.addAll(knxSensors);
//			sensors.addAll(snmpDevices);
//
//			return sensors;
//		}

    public void updateDeviceLocation(String location_id, String username) {
        Set<DeviceDTO> deviceDTOS = deviceRepository.getDevicesByLocationId(location_id);
        deviceRepository.updateDeviceLocation(location_id, null, null, null);
        if (deviceDTOS.size() > 0) {
            for (DeviceDTO deviceDTO : deviceDTOS) {
                deviceDTO.setOnboard_data(new DeviceOnboardStatusDTO(null, deviceDTO.getImage_status(), deviceDTO.getGeolocation_status(), deviceDTO.getTag_status(), deviceDTO.getField_status()));
                if (deviceDTO.getOnboard_data() != null && deviceDTO.getOnboard_data().getGeolocation_status() == 1) {
                    deviceDTO.getOnboard_data().setGeolocation_status(0);
                    this.updateAssetOnboardData(username, null, deviceDTO.getId(), deviceDTO.getOnboard_data(), deviceDTO.getOnboard_status());
                }
            }
        }
    }

    public void updateDevicePosition(List<DeviceDTO> devicePositions, String vdmsid, String username, HttpServletRequest httpServletRequest) {
        log.info("updateDevicePosition, Params: devicePositions: {}, endpoint : {}", devicePositions, httpServletRequest.getRequestURI());
        if (devicePositions.size() > 0) {

            for (DeviceDTO devicePosition : devicePositions) {
                DeviceDTO existingDevice = this.getDeviceByDeviceId("", "", "", devicePosition.getId());
                String device_name = existingDevice.getUser_data_name() == null || existingDevice.getUser_data_name().equals("") ? existingDevice.getDisplay_name() : existingDevice.getUser_data_name();
                LocationDTO locationDTO = null;
                if (devicePosition.getLocation_id() == null) {
                    if (existingDevice.getAsset_match_status() != null) {
                        if (existingDevice.getAsset_match_status() == 2) {
                            devicePosition.setAsset_match_status(1);
                        } else {
                            devicePosition.setAsset_match_status(existingDevice.getAsset_match_status());
                        }
                    }
                } else {
                    devicePosition.setAsset_match_status(2);
                }

                if (devicePosition.getLocation_id() != null) {
                    try {
                        deviceRepository.updateDevicePosition(devicePosition.getId(), devicePosition.getPosition(),
                                devicePosition.getLocation_id(), devicePosition.getLatitude(), devicePosition.getLongitude(),
                                devicePosition.getAsset_match_status());

                        if (existingDevice.getOnboard_data() != null && existingDevice.getOnboard_data().getGeolocation_status() != 1) {
                            existingDevice.getOnboard_data().setGeolocation_status(1);
                            this.updateAssetOnboardData(username, vdmsid, devicePosition.getId(), existingDevice.getOnboard_data(), existingDevice.getOnboard_status());
                        }
                        locationDTO = locationService.getLocationByLocationId(devicePosition.getLocation_id());
                        log.info("device position : {}", devicePosition.getPosition());
                        userActionLogService.addUserAction(username, "asset", "UPDATE", "Position details are updated for Device name: " + device_name + " and id: " + devicePosition.getId() + (devicePosition.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + devicePosition.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "geolocation", devicePosition.getId());

                    } catch (Exception e) {
                        log.error("Exception. Params: devicePositions: {}, endpoint : {}", devicePositions, httpServletRequest.getRequestURI(), e);
                        userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable Position details are updated for Device name: " + device_name + " and id: " + devicePosition.getId() + (devicePosition.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + devicePosition.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "geolocation", devicePosition.getId());
                    }
                }

//                if (locationService.checkLocationById(devicePosition.getLocation_id())) {
//                    deviceRepository.updateDevicePosition(devicePosition.getId(), devicePosition.getPosition(),
//                            devicePosition.getLocation_id(), devicePosition.getLatitude(), devicePosition.getLongitude(),
//                            devicePosition.getAsset_match_status());
//                } else {
//                    buildingService.synclocationbyId(devicePosition.getLocation_id(), vdmsid);
//                    deviceRepository.updateDevicePosition(devicePosition.getId(), devicePosition.getPosition(),
//                            devicePosition.getLocation_id(), devicePosition.getLatitude(), devicePosition.getLongitude(),
//                            devicePosition.getAsset_match_status());
//                }

            }
        }

    }


    // Device list for integration
    public List<DeviceDTO> listDevicebyDockerIntegration(String dockername) {
        return deviceRepository.listDevicebyDockerIntegration(dockername);

    }

    //Get Parent Device by Pagination
//		public Set<DeviceDTO> getParentDeviceByPagination(String username, String vdmsid, String searchKey,Integer pageno,
//				Integer pagesize, Set<String> dockernames) {
//
//			Integer offset = pagesize * (pageno - 1);
//
//			if(dockernames.size() == 1 && dockernames.iterator().next().equals("all"))
//			{
//				return deviceRepository.getAllParentDeviceByPagination(searchKey, pagesize, offset);
//			}
//			else
//			{
//				return deviceRepository.getNetworkParentDeviceByPagination(dockernames, searchKey, pagesize, offset);
//			}
//		}

    public Set<DeviceDTO> getParentDeviceByPagination(String username, String vdmsid, String searchKey, Integer
            pageno,
                                                      Integer pagesize, Set<String> dockernames, Set<String> types, Set<String> virtual_device_types) {

        Integer offset = pagesize * (pageno - 1);
        return deviceRepository.getNetworkParentDeviceByPagination(dockernames, types, searchKey, pagesize, offset, virtual_device_types);
    }

    //Get parent device by id
    public Set<DeviceDTO> getParentDeviceById(String username, String vdmsid, String dockername,
                                              Set<DeviceDTO> parent_devices, HttpServletRequest httpServletRequest) {
        log.info("getParentDeviceById, Params: parent devices: {},endpoint : {}", parent_devices, httpServletRequest.getRequestURI());
        for (DeviceDTO parent_device : parent_devices) {
            String parent_device_name = deviceRepository.getParentDeviceNameById(parent_device.getParent());
            parent_device.setParent_name(parent_device_name);

            String subsystem_parent_name = deviceRepository.getParentDeviceNameById(parent_device.getSubsystem_parent_id());
            parent_device.setSubsystem_parent_name(subsystem_parent_name);

            String snmp_parent_name = deviceRepository.getParentDeviceNameById(parent_device.getSnmp_parent());
            parent_device.setSnmp_parent_name(snmp_parent_name);
        }

        return parent_devices;
    }


    //new get method with subsystem parent device get initial
    public Set<DeviceDTO> getSubsystemParentDevicesByPagination(String username, String vdmsid, String dockername,
                                                                String condition, Integer pageno, Integer pagesize, String assignee) {
        return this.getAllSubsystemDevicesByPagination(username, vdmsid, dockername, null, condition, pageno, pagesize, assignee);
    }

    //new get method with subsystem devices get
    public Set<DeviceDTO> getSubsystemDevicesByPagination(String username, String vdmsid, String dockername, String
            device_id,
                                                          String condition, Integer pageno, Integer pagesize, String assignee) {
        return this.getAllSubsystemDevicesByPagination(username, vdmsid, dockername, device_id, condition, pageno, pagesize, assignee);
    }

    //new get method with subsystem devices get all based on device_id
    public Set<DeviceDTO> getAllSubsystemDevicesByPagination(String username, String vdmsid, String dockername,
                                                             String device_id, String condition, Integer pageno, Integer pagesize, String assignee) {
        Set<DeviceDTO> devices;
        Integer virtual_device_type = null;
        Integer status = null;
        Integer monitor = 123;
        Integer offset = pagesize * (pageno - 1);
        Integer asset_match_status = null;
        Integer onboard_status = 123;
        Integer assigned_status = null;

        System.out.println("outside all" + virtual_device_type + status + monitor + offset + pageno);

        if (condition.equals("all")) {
            System.out.println("inside all" + virtual_device_type + status + monitor + offset + pageno);
        } else if (condition.equals("unmonitored")) {
            System.out.println("outsidxse all" + virtual_device_type + status + monitor + offset + pageno);
            monitor = 0;
        } else if (condition.equals("online")) {

            monitor = 1;
            status = 1;
            System.out.println("Inside Online" + monitor + status);

        } else if (condition.equals("offline")) {
            monitor = 1;
            status = 0;
        } else if (condition.equals("other")) {
            virtual_device_type = 123;
        } else if (condition.equals("matched")) {
            asset_match_status = 1;
        } else if (condition.equals("unmatched")) {
            asset_match_status = 0;
        } else if (condition.equals("verified")) {
            asset_match_status = 2;
        } else if (condition.equals("archived")) {
            asset_match_status = 3;
        } else if (condition.equals("onboarded")) {
            onboard_status = 3;
        } else if (condition.equals("notonboarded")) {
            onboard_status = 210;
        } else if (condition.equals("assigned")) {
            assigned_status = 1;
        } else if (condition.equals("unassigned")) {
            assigned_status = 0;
        }


        if (device_id != null) {
            devices = deviceRepository.getSubsystemDevicesByPagination(device_id, pagesize, offset, assignee);
        } else {
            devices = deviceRepository.getSubsystemParentDevicesByPagination(vdmsid, dockername, virtual_device_type, status, monitor, asset_match_status, pagesize, offset, onboard_status, assigned_status, assignee);
        }

        for (DeviceDTO device : devices) {
            try {
                device.setIp_addresses(this.getDeviceIPAddressByDeviceId(device.getId()));
                device.setOnboard_data(new DeviceOnboardStatusDTO(device.getAssignee_email(), device.getImage_status(), device.getGeolocation_status(), device.getTag_status(), device.getField_status(),
                        deviceOnboardStatusAssigneeService.getDeviceOnboardStatusAssignees(device.getDevice_onboard_status_id())));
                device.setSubsystems(new HashSet<>());
                //ITAM Changes
                DeviceSpecification spec = deviceSpecificationRepository.findByDeviceId(device.getId());
                if (spec != null) {
                    device.setIs_agent_device(true);
                    JSONObject osInfo = parseObjectSafe(spec.getOsInfo());
                    JSONObject locationInfo = parseObjectSafe(spec.getLocationInfo());

                    if (osInfo != null && osInfo.containsKey("username")) {
                        device.setUsername(osInfo.getString("username"));
                    }

                    if (locationInfo != null) {
                        if (locationInfo.containsKey("ispLocation")) {
                            device.setIsp_location(locationInfo.getString("ispLocation"));
                        }
                        if (locationInfo.containsKey("pcLocation")) {
                            device.setPc_location(locationInfo.getString("pcLocation"));
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error getting device ip address " + e);
                System.out.println(e);
            }

        }

        Set<DeviceDTO> devicesWithQrCodeDetails = this.getDevicesWithQrCodeCount(vdmsid, devices);
        if (devicesWithQrCodeDetails != null) {
            return devicesWithQrCodeDetails;
        }
        return devices;
    }

    private JSONObject parseObjectSafe(String json) {
        try {
            return JSON.parseObject(json);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public void updateParentSubsystemCount(String device_id) {
        if (device_id != null) {
            Integer subsystem_count = deviceRepository.getSubsystemCount(device_id);
            deviceRepository.updateSubsystemCount(device_id, subsystem_count);
        }
    }


    public DeviceDTO getSubsystemParentDeviceInfo(String username, String vdmsid, String dockername, String
            device_id, String parent_id) {
        DeviceDTO device = this.getDeviceByDeviceId(username, vdmsid, dockername, parent_id);
        if (device != null) {
            device.setSubsystems(Collections.singleton(this.getDeviceByDeviceId(username, vdmsid, dockername, device_id)));
        }

        return device;
    }

    public void archiveDevices(String username, String vdmsid, String dockername, Integer archive, Set<String> deviceIds, HttpServletRequest httpServletRequest, String assignee) {
        log.info("archiveDevices, Params: archive: {}, deviceIds: {}, endpoint : {}", archive, deviceIds, httpServletRequest.getRequestURI());
        int assetMatchStatus = 3;
        String status = "archived";
        LocationDTO locationDTO = null;
        for (String deviceId : deviceIds) {
            DeviceDTO device = this.getDeviceByDeviceId(username, vdmsid, dockername, deviceId);
            if (archive == 0) {
                // Making device as un archive
                String location_id = device.getLocation_id();
                if (location_id != null) {
                    assetMatchStatus = 2;
                } else {
                    assetMatchStatus = 0;
                }
                status = "unarchived";
            }
            try {
                this.updateDeviceAssetStatus(assetMatchStatus, deviceId, username, BigInteger.valueOf(System.currentTimeMillis()));
                log.info("device name : {}", device.getDisplay_name());

                if (device.getLocation_id() != null) {
                    locationDTO = locationService.getLocationByLocationId(device.getLocation_id());
                }
                if (archive == 0) {
                    userActionLogService.addUserAction(username, "asset", "UPDATE", "Device name: " + device.getDisplay_name() + " and id: " + device.getId() + " is unarchived" + (device.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + device.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_info", device.getId());
                } else if (archive == 1) {
                    userActionLogService.addUserAction(username, "asset", "UPDATE", "Device name: " + device.getDisplay_name() + " and id: " + device.getId() + " is archived" + (device.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + device.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_info", device.getId());
                }
            } catch (Exception e) {
                log.error("Exception. Params: archive: {}, deviceIds: {}, endpoint : {}", archive, deviceIds, httpServletRequest.getRequestURI(), e);
                if (archive == 0) {
                    userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to unarchive Device name: " + device.getDisplay_name() + " and id: " + device.getId() + (device.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + device.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_info", device.getId());
                } else if (archive == 1) {
                    userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to archive Device name: " + device.getDisplay_name() + " and id: " + device.getId() + (device.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + device.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_info", device.getId());
                }
            }

            String AlertMessage = "This asset has been " + status + " by " + username;
            HistoryDTO history = new HistoryDTO();
            history.setStatus(status);
            history.setType(20);
            history.setAlert_message(AlertMessage);
            history.setDevice_id(deviceId);
            history.setCreated_email(username);
            historyService.addHistory(history);
        }

        try {
            socketservice.sockerDeviceCountByDocker(dockername,assignee);
        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            inspectionRecordService.updateInspectionStatusOnDeviceArchive(deviceIds);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void updateDeviceAssetStatus(int assetMatchStatus, String deviceId, String updated_email, BigInteger
            updated_timestamp) {
        deviceRepository.updateDeviceAssetStatus(assetMatchStatus, deviceId, updated_email, updated_timestamp);
    }

    /*****************************************Asset Mapper Methods*********************************************************/

    public void upsertVirtualDeviceByAssetMapper(DeviceDTO virtual_device, String username) {
        /*
         * virtual_device_type is always 2 for asset mapper device
         * monitor is always 1 for added virtual device by asset mapper
         */

        try {
            Integer virtual_device_type = 2;
            Integer monitor = 1;

            System.out.println("Upserting device " + virtual_device);
            virtual_device.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
            deviceRepository.upsertVirtualDeviceByAssetMapper(virtual_device.getId(), virtual_device.getDocker_name(), virtual_device.getVdms_id(),
                    virtual_device.getUser_data_name(), virtual_device.getUser_data_model(),
                    virtual_device.getUser_data_vendor(), virtual_device.getType(), virtual_device.getMac_address(), virtual_device.getIp_address(), virtual_device.getNetwork_layer(), virtual_device.getSerial_number(),
                    virtual_device.getWarranty(), virtual_device.getCustom_fields(), virtual_device.getSubsystem_parent_id(), virtual_device_type, monitor, virtual_device.getSubsystem_count(), virtual_device.getCreated_timestamp(), username);
            virtual_device = deviceRepository.getDeviceByDeviceId(virtual_device.getId());
            log.info("virtual device:{} ", virtual_device);
            this.updateVirtualDeviceOnboardStatusByAssetMapper(virtual_device, username);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void upsertProductDetailsForVirtualDevice(String username, String vdmsid, String dockername, DeviceDTO
            virtual_device) {
        try {
            String virtual_device_id = virtual_device.getId();

            DeviceDTO existingDevice = this.getDeviceByDeviceId(username, vdmsid, dockername, virtual_device_id);
            String previous_product_id = existingDevice.getProduct_id();

            this.getProductDetails("", virtual_device.getVdms_id(), virtual_device.getDocker_name(), virtual_device_id, virtual_device.getProduct_id(), previous_product_id);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public List<AssetDTO> getUntaggedProductDevicesByPagination(Integer pageNo, Integer pageSize) {
        Integer offset = pageSize * (pageNo - 1);
        return deviceRepository.getUntaggedProductDevicesByPagination(pageSize, offset);
    }

    public Integer getUntaggedProductDevicesCount() {
        return deviceRepository.getUntaggedProductDevicesCount();
    }

    public void updateDeviceMatchedProductIds(String device_id, String matched_product_ids) {
        deviceRepository.updateDeviceMatchedProductIds(device_id, matched_product_ids);
    }

    public List<AssetDTO> getAssetMapperDevicesByIdList(List<String> device_ids) {
        return deviceRepository.getAssetMapperDevicesByIdList(device_ids);
    }

    public AssetDTO getAssetMapperDevicesById(String device_id) {
        return deviceRepository.getAssetMapperDeviceById(device_id);
    }

    public List<AssetDTO> getAssetMapperSubSystemDevicesById(String device_id) {
        return deviceRepository.getAssetMapperSubSystemDevicesById(device_id);
    }

    public void updateMatchedDeviceProduct(String username, String vdmsid, String dockername, DeviceDTO device, HttpServletRequest httpServletRequest) {
        log.info("updateMatchedDeviceProduct, Params: device: {}, endpoint : {}", device, httpServletRequest.getRequestURI());
        String device_name = device.getUser_data_name() == null || device.getUser_data_name().equals("") ? device.getDisplay_name() : device.getUser_data_name();
        try {
            this.upsertProductDetailsForVirtualDevice("", "", "", device);
            this.updateDeviceProductDetails(device);
            this.updateDeviceMatchedProductIds(device.getId(), null);
            log.info("device name : {}", device_name);
            userActionLogService.addUserAction(username, "asset", "UPDATE", "Matched Product details are updated for device name: " + device_name + " and id: " + device.getId(), "success", "products", device.getId());
        } catch (Exception e) {
            log.error("Exception. Params: device: {}, endpoint : {}", device, httpServletRequest.getRequestURI(), e);
            userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to update Matched Product details for device name: " + device_name + " and id: " + device.getId(), "failed", "products", device.getId());
        }
    }

    public void updateDeviceProductDetails(DeviceDTO device) {
        deviceRepository.updateDeviceProductDetails(device.getId(), device.getProduct_id(), device.getUser_data_model(), device.getUser_data_name(),
                device.getUser_data_vendor(), device.getType(), device.getNetwork_layer());
    }

    /*****************************************Asset Mapper Methods*********************************************************/

    //list devices for snmp discovery
    public List<DeviceDTO> getAllDeviceByVdmsIdAndDockerName(String username, String vdmsid, String
            dockername, Integer pagesize, Integer offset) {

        List<DeviceDTO> devices = deviceRepository.getAllDeviceByVdmsIdAndDockerName(vdmsid, dockername, pagesize, offset);
        for (DeviceDTO device : devices) {
            try {
                device.setIp_addresses(this.getDeviceIPAddressByDeviceId(device.getId()));
            } catch (Exception e) {
                System.out.println("Error getting device ip address " + e);
                System.out.println(e);
            }
        }
        return devices;
    }

    //get device status count
    public Map<String, Integer> getDeviceStatusCountTS(String networkname, String buildingid, String
            floorid, String locationid) {

        Map<String, Integer> deviceStatuscount = new HashMap<>();

        deviceStatuscount.put("online_device_count", deviceRepository.getDeviceStatusCountTS(1, networkname, buildingid, floorid, locationid));
        deviceStatuscount.put("offline_device_count", deviceRepository.getDeviceStatusCountTS(0, networkname, buildingid, floorid, locationid));
        deviceStatuscount.put("other_device_count", deviceRepository.otherDevicesCountByDocker(networkname, buildingid, floorid, locationid));
        deviceStatuscount.put("all_device_count", deviceRepository.getMonitoredDevicesCountByDocker(networkname, buildingid, floorid, locationid));

        return deviceStatuscount;
    }

    //update device product port status in new thread
    public void updateDeviceProductPortStatus(String vdms_id, Set<String> device_ids) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {

//			Set<DeviceDTO> devices = this.getDevicesByIdList(device_ids);

            Set<DeviceDTO> devices = new HashSet<>(this.getDevicesByIdList(vdms_id, device_ids));
            this.updateMultipleDevicesProductPortStatus(vdms_id, devices);
        });
        try {
            executorService.shutdown();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void updateMultipleDevicesProductPortStatus(String vdms_id, Set<DeviceDTO> devices) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            for (DeviceDTO device : devices) {

                Set<Product_PortsDTO> ports = portService.getPortsByDeviceId(device.getId());

                for (Product_PortsDTO port : ports) {
                    Integer portStatus = portService.getDeviceportStatus(device.getVdms_id(), device.getDocker_name(), device.getIp_address(), port.getPort());

                    if (portStatus != null) {
                        BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
                        port.setStatus(portStatus);
                        port.setTimestamp(timestamp);
                        portService.updatePortStatusById(port);
                    }
                }
            }
        });
        try {
            executorService.shutdown();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public Optional<Device> findById(String id) {
        return deviceRepository.findById(id);
    }

    public List<String> getDevicesBySubSystemParentId(String device_id) {
        return deviceRepository.getDevicesBySubSystemParentId(device_id);
    }

    public void updateSubsystemParentDevice(String device_id, String subsystem_parent_id) {
        deviceRepository.updateSubsystemParentDevice(device_id, subsystem_parent_id);
    }

    // update measuring instrument status
    public void updateDeviceMeasuringInstrumentStatus(String measuring_instrument_id) {
        try {
            String device_id = measuringInstrumentService.getDeviceIdByMeasuringInstrumentSensorId(measuring_instrument_id);

            this.updateDeviceMeasuringInstrumentStatusByDeviceId(device_id);

        } catch (Exception e) {
            System.out.println("Error in updating measuring instrument status " + e);
        }
    }

    // update measuring instrument status by device id
    public void updateDeviceMeasuringInstrumentStatusByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Boolean alert = measuringInstrumentService.getMeasuringInstrumentAlertStatusByDeviceId(device_id);
                String measuring_instrument_status = "no-alert";
                if (alert) {
                    measuring_instrument_status = "alert";
                }
                deviceRepository.updateDeviceMeasuringInstrumentStatus(device_id, measuring_instrument_status);
            }
        } catch (Exception e) {
            System.out.println("Error in updating measuring_instrument status by device id " + e);
        }
    }

    //update device co-ordinates(latitude, longitude and position)
    private void updateDeviceCoordinates(String latitude, String longitude, String position, String device_id) {
        deviceRepository.updateDeviceCoordinates(device_id, latitude, longitude, position);
    }

    /***********************************Update Product Details By Model and Mac Vendor - Model Detection Script***********************************/
    public void updateDeviceProductModel(String device_id, String mac_by_vendor, String model) {
        ProductDTO db_product = apicallService.getProductDetailsByModelAndMBV(mac_by_vendor, model);

        if (db_product != null) {
            DeviceDTO device = new DeviceDTO();
            device.setId(device_id);
            device.setProduct_id(db_product.getId());
            device.setUser_data_model(db_product.getModel());
            device.setUser_data_name(db_product.getDevice_name());
            device.setUser_data_vendor(db_product.getManufacturer());
            device.setType(db_product.getDevice_type());
            device.setNetwork_layer(db_product.getNetwork_layer());


            this.upsertProductDetailsForVirtualDevice("", "", "", device);
            this.updateDeviceProductDetails(device);

            System.out.println(db_product.toString());
        }
    }
    /***********************************Update Product Details By Model and Mac Vendor - Model Detection Script************************************/


    /**********************************************  New methods for edit device optimisation *********************************************/

    public MultiDeviceDTO setProductVendorsForDevices(MultiDeviceDTO multidevicedto, String previous_product_id) {
        System.out.println("---------- Entered setVendorsForDevices -------------");

        if ((multidevicedto.getProduct_id() != null) && (!multidevicedto.getProduct_id().equals(previous_product_id))) {
            DeviceDTO device = deviceRepository.getDeviceByDeviceId(multidevicedto.getId());

            if (multidevicedto.getGlobal_vendor_id() == null) {
                multidevicedto.setGlobal_vendor_id(device.getGlobal_vendor_id());
            }

            if (multidevicedto.getLocal_vendor_id() == null) {
                multidevicedto.setLocal_vendor_id(device.getLocal_vendor_id());
            }

            if (multidevicedto.getOther_vendor_1_id() == null) {
                multidevicedto.setOther_vendor_1_id(device.getOther_vendor_1_id());
            }

            if (multidevicedto.getOther_vendor_2_id() == null) {
                multidevicedto.setOther_vendor_2_id(device.getOther_vendor_2_id());
            }

            if (multidevicedto.getOther_vendor_3_id() == null) {
                multidevicedto.setOther_vendor_3_id(device.getOther_vendor_3_id());
            }

        }

        return multidevicedto;
    }


    public void getProductDetails(String username, String vdmsid, String dockername, String device_id,
                                  String product_id, String previous_product_id) {
        System.out.println("~~~~~~~~~~~~~~ Entered getProductDetails ~~~~~~~~~~~~~~~~~~~~~~~~~");

        if (product_id == null) {
            this.deleteDeviceGlobalProductDetails(device_id);
        }

        if ((product_id != null) && (!product_id.equals(previous_product_id))) {
            if (previous_product_id != null) {
                this.deleteDeviceGlobalProductDetails(device_id);
            }

            if (device_id != null) {
                this.getProductDetailsByProductId(username, vdmsid, dockername, product_id, device_id);
            }
        }

    }

    public void deleteDeviceGlobalProductDetails(String device_id) {
        System.out.println("~~~~~~~~~~~~~~~~~ Entered deleteGlobalDetails ~~~~~~~~~~~~~~~~~~~~~~`");

        snmpService.deleteGlobalSnmpByDeviceId(device_id);
        portService.deleteGlobalPortByDeviceId(device_id);
        notesService.deleteGlobalNotesByDeviceId(device_id);
    }

    public void getProductDetailsByProductId(String username, String vdmsid, String dockername, String
            product_id, String device_id) {
        try {
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~ getProductDetailsFromCloud ~~~~~~~~~~~~~~~~~~~~~~~~~~");

            ProductDTO db_product = apicallService.getProductDetailsByProductId(product_id);

            if (db_product != null) {
                if (db_product.getProduct_snmps() != null && db_product.getProduct_snmps().size() > 0) {
                    snmpService.upsertGlobalSnmpByDeviceId(db_product.getProduct_snmps(), device_id);
                }

                if (db_product.getProduct_ports() != null && db_product.getProduct_ports().size() > 0) {
                    portService.upsertGlobalPortByDeviceId(db_product.getProduct_ports(), device_id);
                }

                if (db_product.getProduct_notes() != null && db_product.getProduct_notes().size() > 0) {
                    notesService.upsertGlobalNotesByDeviceId(db_product.getProduct_notes(), device_id);
                }


                if (db_product.getVendors() != null && db_product.getVendors().size() > 0) {
                    phonebookService.addPhoneBookByDeviceId(username, vdmsid, dockername, db_product.getVendors(),
                            device_id);
                    deviceRepository.updateDeviceVendorsByDeviceID(db_product.getGlobal_vendor_id(),
                            db_product.getLocal_vendor_id(), db_product.getOther_vendor_1_id(),
                            db_product.getOther_vendor_2_id(), db_product.getOther_vendor_3_id(), device_id);
                }

                tagProductImages(db_product);
                product_detailsService.upsertProductDetail(db_product);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }


    public MultiDeviceDTO updateVendorPhoneBookDetails(String username, String vdmsid, String
            dockername, MultiDeviceDTO multiDeviceDTO) {
        System.out.println(" ~~~~~~~~~~~~~~ Entered updateVendorPhoneBookDetails ~~~~~~~~~~~~~~~~~~~");

        if (multiDeviceDTO.getGlobal_vendor() != null) {
            multiDeviceDTO.setGlobal_vendor_id(multiDeviceDTO.getGlobal_vendor().getId());
            this.addPhoneBookAddressDetailsById(username, vdmsid, dockername, multiDeviceDTO.getGlobal_vendor());
        }
        if (multiDeviceDTO.getLocal_vendor() != null) {
            multiDeviceDTO.setLocal_vendor_id(multiDeviceDTO.getLocal_vendor().getId());
            this.addPhoneBookAddressDetailsById(username, vdmsid, dockername, multiDeviceDTO.getLocal_vendor());
        }
        if (multiDeviceDTO.getOther_vendor_1() != null) {
            multiDeviceDTO.setOther_vendor_1_id(multiDeviceDTO.getOther_vendor_1().getId());
            this.addPhoneBookAddressDetailsById(username, vdmsid, dockername, multiDeviceDTO.getOther_vendor_1());
        }
        if (multiDeviceDTO.getOther_vendor_2() != null) {
            multiDeviceDTO.setOther_vendor_2_id(multiDeviceDTO.getOther_vendor_2().getId());
            this.addPhoneBookAddressDetailsById(username, vdmsid, dockername, multiDeviceDTO.getOther_vendor_2());
        }
        if (multiDeviceDTO.getOther_vendor_3() != null) {
            multiDeviceDTO.setOther_vendor_3_id(multiDeviceDTO.getOther_vendor_3().getId());
            this.addPhoneBookAddressDetailsById(username, vdmsid, dockername, multiDeviceDTO.getOther_vendor_3());
        }
        return multiDeviceDTO;
    }

    public void addPhoneBookAddressDetailsById(String username, String vdmsid, String
            dockername, PhonebookAddressDto phonebook) {
        System.out.println("~~~~~~~~~~~~~~~~~~~ addPhoneBookAddressDetailsById ~~~~~~~~~~~~~~~~~~~~~");
        utilsService.upsertPhoneAddressById(username, vdmsid, dockername, phonebook);
    }

    public MultiDeviceDTO mapProductVendorsToMultideviceDTO(DevicesDTO devicesDTO) {
        System.out.println("~~~~~~~~~~~~~ mappingDTO ~~~~~~~~~~~~~~~");

        MultiDeviceDTO multiDeviceDTO = new MultiDeviceDTO();
        multiDeviceDTO.setGlobal_vendor(devicesDTO.getGlobal_vendor());
        multiDeviceDTO.setLocal_vendor(devicesDTO.getLocal_vendor());
        multiDeviceDTO.setOther_vendor_1(devicesDTO.getOther_vendor_1());
        multiDeviceDTO.setOther_vendor_2(devicesDTO.getOther_vendor_2());
        multiDeviceDTO.setOther_vendor_3(devicesDTO.getOther_vendor_3());

        return multiDeviceDTO;
    }

    /**********************************************  New methods for edit device optimisation *********************************************/


    public DeviceDTO getDeviceDetails(String checklist_id) {
        return deviceRepository.getDeviceDetails(checklist_id);
    }


    public void updateDeviceRecordChecklistStatusByDeviceId(String device_id, String record_type) {
        try {
            if (device_id != null) {
                String record_checklist_status = recordChecklistService.getRecordChecklistStatusByDeviceId(device_id, record_type);
                String checklist_status = "completed";
                if (record_checklist_status.equals("todo")) {
                    checklist_status = "todo";
                }
                deviceRepository.updateDeviceRecordChecklistStatus(device_id, checklist_status);
            }
        } catch (Exception e) {
            System.out.println("Error in updating record checklist status by device id " + e);
        }
    }

    public void updateDeviceRecordChecklistCount(String device_id, String record_type) {
        Integer record_checklist_count = recordChecklistService.getChecklistStatusCountDeviceId(device_id, "inspection", record_type);
        System.out.println("count--------------------" + record_checklist_count);
        deviceRepository.updateDeviceRecordChecklistCount(device_id, record_checklist_count);
    }


    public void updateDeviceRecordChecklistStatusById(String device_id, String record_type) {
        if (device_id != null) {
            this.updateDeviceRecordChecklistStatusByDeviceId(device_id, record_type);
            this.updateDeviceRecordChecklistCount(device_id, record_type);
        }
    }


    // update DainTree sensor count by device id
    public void updateDeviceDaintreeCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer daintree_count = daintreeService.getDeviceDaintreeDevicesCountByDeviceId(device_id);
                deviceRepository.updateDeviceDaintreeDevicesCount(daintree_count, device_id);
            }
        } catch (Exception e) {
            System.out.println("Error in updating daintree count by device id " + e);
        }
    }

    public void updateDeviceQrcodeCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer qrcode_count = globalQrcodeService.getDeviceQrcodeCountByDeviceId(device_id);
                deviceRepository.updateDeviceQrcodeCount(qrcode_count, device_id);
            }
        } catch (Exception e) {
            System.out.println("Error in updating qrcode count by device id " + e);
        }
    }


    //alert message filters
    public List<ConditionsDTO> getDeviceAlertMessages(String username, String vdmsid, String dockername) {
        List<String> ids = deviceRepository.listDevicesByAlertStatus(dockername);
        List<ConditionsDTO> conditions = new ArrayList<>();
        if (ids != null && ids.size() > 0) {

            conditions.addAll(bacnetService.listBacnetDevicesAlertMessagesByDeviceIds(ids));
            conditions.addAll(disruptiveService.listDisruptiveDevicesAlertMessagesByDeviceIds(ids));
            conditions.addAll(lorawanService.listLorawanDevicesAlertMessagesByDeviceIds(ids));
            conditions.addAll(myDevicesService.listmydevicesDeviceAlertMessagesByDeviceIds(ids));
            conditions.addAll(monnitService.listmonnitDevicesAlertMessagesByDeviceIds(ids));
            conditions.addAll(pelicanService.listpelicanDevicesAlertMessagesByDeviceIds(ids));
            conditions.addAll(knxService.listKNXDevicesAlertMessagesByDeviceIds(ids));
            conditions.addAll(measuringInstrumentService.listMeasuringIntrumentDevicesAlertMessagesByDeviceIds(ids));
            conditions.addAll(daintreeService.listDaintreeDevicesAlertMessagesByDeviceIds(ids));
            conditions.addAll(modbusService.listModbusDevicesAlertMessagesByDeviceIds(ids));

        }
        if (conditions.size() > 0) {
            return this.getDeviceAlertMessagesWithDevices(conditions);
        }
        return null;
    }

    private List<ConditionsDTO> getDeviceAlertMessagesWithDevices(List<ConditionsDTO> conditions) {
        if (conditions != null && conditions.size() > 0) {
            List<ConditionsDTO> alertMessagesWithDevices = new ArrayList<>();
            for (ConditionsDTO condition : conditions) {
                if (condition.getAlert_message() != null) {
                    if (alertMessagesWithDevices.stream().noneMatch(x -> x.getAlert_message().equals(condition.getAlert_message()))) {

                        ConditionsDTO alertCondition = new ConditionsDTO();
                        alertCondition.setAlert_message(condition.getAlert_message());
                        Set<String> devicesWithAlerts = conditions.stream()
                                .filter(x -> x.getAlert_message() != null)
                                .filter(x -> x.getAlert_message().equals(condition.getAlert_message()))
                                .map(ConditionsDTO::getDevice_id)
                                .collect(Collectors.toSet());
                        alertCondition.setDevice_ids(devicesWithAlerts);
                        alertMessagesWithDevices.add(alertCondition);
                    }
                }
            }
            return alertMessagesWithDevices;
        }
        return null;
    }

    public void updateDeviceDainTreeStatus(String daintree_device_id) {
        try {

            String device_id = daintreeService.getDeviceIdByDaintreeDeviceId(daintree_device_id);
            this.updateDeviceDaintreeStatusByDeviceId(device_id);

        } catch (Exception e) {
            System.out.println("Error in updating DainTree status " + e);
        }
    }

    public void updateDeviceDaintreeStatusByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Boolean alert = daintreeService.getDaintreeAlertStatusByDeviceId(device_id);
                String daintree_status = "no-alert";
                if (alert) {
                    daintree_status = "alert";
                }
                deviceRepository.updateDeviceDaintreeStatus(device_id, daintree_status);

            }
        } catch (Exception e) {
            System.out.println("Error in updating Daintree status by device id " + e);
        }
    }

    public void modelResetbyDockerName(String docker_name) {
        deviceRepository.modelResetbyDockerName(docker_name);
    }

    public DeviceAlertDTO getDeviceAlertInfoById(String device_id) {
        return deviceRepository.getDeviceAlertInfoById(device_id);
    }

    public RoomStatusDTO getRoomStatusByDeviceId(String deviceid) {
        return deviceRepository.getRoomStatusByDeviceId(deviceid);
    }

    public List<DeviceMonitorSpaceDTO> getRoomStatusByLocationId(String deviceid) {
        return deviceRepository.getRoomStatusByLocationId(deviceid);
    }

    public void upsertAssetImages(String username, String vdms_id, List<String> device_ids, List<MultipartFile> asset_images, HttpServletRequest httpServletRequest) {
        log.info("upsertAssetImages, Params: device ids: {}, asset images: {}, endpoint : {}", device_ids, asset_images, httpServletRequest.getRequestURI());
        for (String device_id : device_ids) {
            JSONArray array = new JSONArray();
            String action = "ADD";
            DeviceDTO deviceDTO = this.getDeviceAndOnboardStatusByDeviceId(device_id);
            String device_name = deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().equals("") ? deviceDTO.getDisplay_name() : deviceDTO.getUser_data_name();
            LocationDTO locationDTO = null;
            try {
                if (deviceDTO.getAsset_image_url() != null) {
                    array = JSON.parseArray(deviceDTO.getAsset_image_url());
                    action = "UPDATE";
                }
                if (device_ids.size() > 1) {
                    for (int i = 0; i < array.size(); i++) {
                        utils.removeFileFromServerByImageURL(array.getString(i), server_asset_images_absolute_path);
                        array.clear();
                    }
                }

                for (MultipartFile asset_image : asset_images) {
                    try {
                        if (asset_image.getSize() > 0) {
                            //get file path
                            String asset_image_url = utils.addFileToServer(asset_image.getBytes(), server_asset_images_absolute_path, device_id, utils.getFileExtensionByFileUrl(asset_image.getOriginalFilename()), server_asset_images_url);
                            if (asset_image_url != null) {
                                array.add(asset_image_url);
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (array.size() > 0) {
                    deviceRepository.updateAssetImage(device_id, array.toJSONString());
                    if (deviceDTO.getOnboard_data() != null) {
                        deviceDTO.getOnboard_data().setImage_status(1);
                        this.updateAssetOnboardData(username, vdms_id, device_id, deviceDTO.getOnboard_data(), deviceDTO.getOnboard_status());
                    }
                }
                if (deviceDTO.getLocation_id() != null) {
                    locationDTO = locationService.getLocationByLocationId(deviceDTO.getLocation_id());
                }
                if (action.equals("ADD")) {
                    log.info("added device id : {}", device_id);
                    userActionLogService.addUserAction(username, "asset", action, "Asset images are added for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_image", deviceDTO.getId());
                } else if (action.equals("UPDATE")) {
                    log.info("updated device id : {}", device_id);
                    userActionLogService.addUserAction(username, "asset", action, "Asset images are updated for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_image", deviceDTO.getId());
                }
            } catch (RuntimeException e) {
                if (action.equals("ADD")) {
                    log.error("Exception.  Params: device ids: {}, asset images: {}, endpoint : {}", device_ids, asset_images, httpServletRequest.getRequestURI(), e);
                    userActionLogService.addUserAction(username, "asset", action, "Unable to add Asset images for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_image", deviceDTO.getId());
                } else if (action.equals("UPDATE")) {
                    log.error("Exception.  Params: device ids: {}, asset images: {}, endpoint : {}", device_ids, asset_images, httpServletRequest.getRequestURI(), e);
                    userActionLogService.addUserAction(username, "asset", action, "Unable to update Asset images for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_image", deviceDTO.getId());
                }

            }
        }
    }

    public void deleteAssetImages(String username, String vdms_id, List<DeviceDTO> deviceDTOS, HttpServletRequest httpServletRequest) {
        log.info("deleteAssetImages, Params: deviceDTOs: {}, endpoint : {}", deviceDTOS, httpServletRequest.getRequestURI());
        for (DeviceDTO device : deviceDTOS) {
            DeviceDTO deviceDTO = this.getDeviceAndOnboardStatusByDeviceId(device.getId());
            String device_name = deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().equals("") ? deviceDTO.getDisplay_name() : deviceDTO.getUser_data_name();

            JSONArray image_array = JSON.parseArray(deviceDTO.getAsset_image_url());
            JSONArray asset_image_array = JSON.parseArray(device.getAsset_image_url());
            JSONArray update_array = new JSONArray();
            LocationDTO locationDTO = null;
            try {
                for (int i = 0; i < asset_image_array.size(); i++) {
                    utils.removeFileFromServerByImageURL(asset_image_array.getString(i), server_asset_images_absolute_path);
                    for (int j = 0; j < image_array.size(); j++) {
                        if (!image_array.getString(j).equals(asset_image_array.getString(i))) {
                            update_array.add(image_array.getString(j));
                        }
                    }
                }
                if (deviceDTO.getLocation_id() != null) {
                    locationDTO = locationService.getLocationByLocationId(deviceDTO.getLocation_id());
                }
                deviceRepository.updateAssetImage(device.getId(), update_array.toJSONString());
                log.info("device name : {}", device_name);
                userActionLogService.addUserAction(username, "asset", "DELETE", "Asset images are removed from Device name: " + device_name + " and id: " + device.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_image", device.getId());
            } catch (Exception e) {
                log.error("Exception. Params: deviceDTOs: {}, endpoint : {}", deviceDTOS, httpServletRequest.getRequestURI(), e);
                userActionLogService.addUserAction(username, "asset", "DELETE", "Unable to remove asset images from Device name: " + device_name + " and id: " + device.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_image", device.getId());

            }

            if (update_array.size() == 0) {
                if (deviceDTO.getOnboard_data() != null) {
                    if (deviceDTO.getImage_status() == 1) {
                        deviceDTO.getOnboard_data().setImage_status(0);
                        this.updateAssetOnboardData(username, vdms_id, device.getId(), deviceDTO.getOnboard_data(), deviceDTO.getOnboard_status());
                    }
                }
            }
        }
    }

    public void deleteDeviceImages(String username, String vdms_id, List<DeviceDTO> deviceDTOS, String category, HttpServletRequest httpServletRequest) {

        String endpoint = httpServletRequest != null ? httpServletRequest.getRequestURI() : "unknown";
        String adc_document_url = "https://qa-app.sclera.com/api/asset-data-collection/sclera-qa-resource/asset-data-collection/images/";
        log.info("deleteDeviceImages started. Category: {}, Devices count: {}, Endpoint: {}", category, deviceDTOS != null ? deviceDTOS.size() : 0, endpoint);

        if (deviceDTOS == null || deviceDTOS.isEmpty()) {
            log.warn("deleteDeviceImages called with empty device list. Category: {}", category);
            return;
        }

        for (DeviceDTO device : deviceDTOS) {

            if (device == null || device.getId() == null) {
                log.warn("Skipping device because device or device id is null.");
                continue;
            }

            try {

                DeviceDTO deviceDTO = deviceRepository.getDeviceByDeviceId(device.getId());

                if (deviceDTO == null) {
                    log.warn("Device not found in DB. DeviceId: {}", device.getId());
                    continue;
                }

                String device_name = (deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().isEmpty()) ? deviceDTO.getDisplay_name() : deviceDTO.getUser_data_name();

                if (device_name == null) {
                    device_name = "Unknown Device";
                }

                log.info("Processing deviceId: {}, deviceName: {}", device.getId(), device_name);

                JSONArray existingArray = new JSONArray();
                JSONArray deleteArray = new JSONArray();
                JSONArray updateArray = new JSONArray();

                String serverPath = null;
                String fieldName = null;

                // Determine category behaviour
                switch (category) {

                    case "asset":

                        if (deviceDTO.getAsset_image_url() != null)
                            existingArray = JSON.parseArray(deviceDTO.getAsset_image_url());

                        if (device.getAsset_image_url() != null)
                            deleteArray = JSON.parseArray(device.getAsset_image_url());

                        serverPath = server_asset_images_absolute_path;
                        fieldName = "asset_image";
                        break;

                    case "nameplate":

                        if (deviceDTO.getAsset_ocr_image_url() != null)
                            existingArray = JSON.parseArray(deviceDTO.getAsset_ocr_image_url());

                        if (device.getAsset_ocr_image_url() != null)
                            deleteArray = JSON.parseArray(device.getAsset_ocr_image_url());

                        serverPath = server_asset_ocr_images_absolute_path;
                        fieldName = "asset_ocr_image";
                        break;

                    case "labelId":

                        if (deviceDTO.getAsset_tag_images_url() != null)
                            existingArray = JSON.parseArray(deviceDTO.getAsset_tag_images_url());

                        if (device.getAsset_tag_images_url() != null)
                            deleteArray = JSON.parseArray(device.getAsset_tag_images_url());

                        serverPath = server_asset_images_absolute_path; // temporary
                        fieldName = "asset_tag_image";
                        break;

                    default:
                        log.warn("Unknown category received: {}", category);
                        return;
                }

                log.info("Existing image count: {}, Delete image count: {}", existingArray.size(), deleteArray.size());

                LocationDTO locationDTO = null;

                for (int i = 0; i < deleteArray.size(); i++) {

                    String deleteUrl = deleteArray.getString(i);

                    if (deleteUrl == null || deleteUrl.isEmpty()) {
                        log.warn("Skipping null or empty delete URL for deviceId {}", device.getId());
                        continue;
                    }

                    log.info("Attempting to delete image: {}", deleteUrl);

                    // Remove only if not ADC storage
                    if (!deleteUrl.startsWith(adc_document_url)) {

                        try {
                            utils.removeFileFromServerByImageURL(deleteUrl, serverPath);
                            log.info("File removed from server: {}", deleteUrl);

                        } catch (Exception ex) {
                            log.error("Failed to remove file from server: {}", deleteUrl, ex);
                        }
                    }

                    for (int j = 0; j < existingArray.size(); j++) {

                        String existingUrl = existingArray.getString(j);

                        if (existingUrl == null) continue;

                        if (!existingUrl.equals(deleteUrl)) {
                            updateArray.add(existingUrl);
                        }
                    }
                }

                if (deviceDTO.getLocation_id() != null) {

                    try {
                        locationDTO = locationService.getLocationByLocationId(deviceDTO.getLocation_id());
                    } catch (Exception e) {
                        log.error("Failed to fetch location for locationId: {}", deviceDTO.getLocation_id(), e);
                    }
                }

                log.info("Updating DB with remaining image count: {}", updateArray.size());

                // Update DB based on category
                switch (category) {

                    case "asset":
                        deviceRepository.updateAssetImage(device.getId(), updateArray.toJSONString());
                        break;

                    case "nameplate":
                        deviceRepository.updateAssetOcrImage(device.getId(), updateArray.toJSONString());
                        break;

                    case "labelId":
                        deviceRepository.updateAssetTagImages(device.getId(), updateArray.toJSONString());
                        break;
                }

                log.info("{} images removed successfully from Device: {}", category, device_name);

                userActionLogService.addUserAction(username, "asset", "DELETE", category + " images removed from Device name: " + device_name + " and id: " + device.getId() + (deviceDTO.getLocation_id() != null && locationDTO != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", fieldName, device.getId());

                // Maintain onboard logic only for asset images
                if ("asset".equals(category) && updateArray.size() == 0) {

                    if (deviceDTO.getOnboard_data() != null && deviceDTO.getImage_status() == 1) {

                        log.info("Resetting onboard image status for deviceId {}", device.getId());

                        deviceDTO.getOnboard_data().setImage_status(0);

                        this.updateAssetOnboardData(username, vdms_id, device.getId(), deviceDTO.getOnboard_data(), deviceDTO.getOnboard_status());
                    }
                }

            } catch (Exception e) {

                log.error("Exception while deleting images for deviceId: {}, category: {}, endpoint: {}", device.getId(), category, endpoint, e);

                userActionLogService.addUserAction(username, "asset", "DELETE", "Unable to remove " + category + " images from Device id: " + device.getId(), "failed", category, device.getId());
            }
        }

        log.info("deleteDeviceImages completed for category: {}", category);
    }


    public String getAssetImageUrls(String username, String vdms_id, String device_id) {
        return deviceRepository.getAssetImageUrls(device_id);
    }

    public String getAssetImageUrlsCategory(String username, String vdms_id, String device_id) {
        DeviceDTO deviceDTO = deviceRepository.getAllDeviceImages(device_id);

        JSONArray result = new JSONArray();

        addWithCategory(result, deviceDTO.getAsset_image_url(), "asset");
        addWithCategory(result, deviceDTO.getAsset_ocr_image_url(), "nameplate");
        addWithCategory(result, deviceDTO.getAsset_tag_images_url(), "labelId");

        return result.toJSONString();

    }


    private void addWithCategory(JSONArray result, String jsonArrayString, String category) {
        if (jsonArrayString == null || jsonArrayString.isBlank()) {
            return;
        }

        JSONArray urls = JSON.parseArray(jsonArrayString);

        for (Object obj : urls) {
            if (obj != null && !obj.toString().isBlank()) {
                JSONObject imageObject = new JSONObject();
                imageObject.put("url", obj.toString());
                imageObject.put("category", category);
                result.add(imageObject);
            }
        }
    }

    private void removeAssetImages(DeviceDTO deviceDTO) {
        if (deviceDTO.getAsset_image_url() != null) {
            JSONArray asset_image_array = JSON.parseArray(deviceDTO.getAsset_image_url());
            for (int i = 0; i < asset_image_array.size(); i++) {
                utils.removeFileFromServerByImageURL(asset_image_array.getString(i), server_asset_images_absolute_path);
            }
        }
    }

    public Set<DeviceDTO> getAllDevicesPagination(String username, String vdmsid, String group, String
            searchkey, Integer pageno, Integer pagesize, JSONObject filterObject) {

        JSONArray dockernames = filterObject.getJSONArray("docker_names");
        JSONArray types = filterObject.getJSONArray("types");
        JSONArray global_checklist_ids = filterObject.getJSONArray("global_checklist_ids");
        String global_inspection_record_id = filterObject.getString("global_inspection_record_id");

        JSONArray virtual_device_types = filterObject.getJSONArray("virtual_device_types");
        Boolean isTaggedToQrCode = filterObject.getBoolean("isTaggedToQrCode");
        Boolean isTaggedToNfc = filterObject.getBoolean("isTaggedToNfc");
        Boolean isTaggedToBarCode = filterObject.getBoolean("isTaggedToBarCode");
        String inspection_record_id = filterObject.getString("inspection_record_id");
        JSONArray deviceIdsTaggedToQrCode = new JSONArray();
        JSONArray deviceIdsTaggedToClientQrCode = new JSONArray();
        JSONArray deviceIdsTaggedToNfc = new JSONArray();
        JSONArray deviceIdsTaggedToClientNfc = new JSONArray();
        JSONArray deviceIdsTaggedToClientBarCode = new JSONArray();
        Set<DeviceDTO> devices;
        if (isTaggedToQrCode != null) {
            deviceIdsTaggedToQrCode = qrCodeService.getDeviceIdsTaggedToQrCode(vdmsid);
            deviceIdsTaggedToClientQrCode = clientQrCodeService.getDeviceIdsTaggedToClientQrCode(vdmsid);
            deviceIdsTaggedToQrCode.addAll(deviceIdsTaggedToClientQrCode);
            if (deviceIdsTaggedToQrCode.isEmpty()) {
                deviceIdsTaggedToQrCode.add("");
            }
        }
        if (isTaggedToNfc != null) {
            deviceIdsTaggedToNfc = nfcService.getDeviceIdsTaggedToNfc(vdmsid);
            deviceIdsTaggedToClientNfc = clientNfcService.getDeviceIdsTaggedToClientNfc(vdmsid);
            deviceIdsTaggedToNfc.addAll(deviceIdsTaggedToClientNfc);
            if (deviceIdsTaggedToNfc.isEmpty()) {
                deviceIdsTaggedToNfc.add("");
            }
        }

        if (isTaggedToBarCode != null) {
            deviceIdsTaggedToClientBarCode = clientBarCodeService.getDeviceIdsTaggedToClientBarCode(vdmsid);
            if (deviceIdsTaggedToClientBarCode.isEmpty()) {
                deviceIdsTaggedToClientBarCode.add("");
            }
        }
        Integer offset = pagesize * (pageno - 1);
        String santized_search_key = searchkey.replaceAll("[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]", "").toLowerCase();

        switch (group) {
            case "all": {
                devices = this.getAllParentDeviceByPagination(dockernames, types, santized_search_key, pagesize, offset, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc,isTaggedToBarCode,deviceIdsTaggedToClientBarCode);
                break;
            }
            case "tagged": {
                // Procedures and Reactive services
                devices = this.getAllChecklistDevicesPagination(santized_search_key, pagesize, offset, dockernames, types, global_checklist_ids, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc,isTaggedToBarCode,deviceIdsTaggedToClientBarCode);
                break;
            }
            case "inspection": {
                //Scheduled inspections and Services
                devices = this.getAllInspectionDevicesPagination(santized_search_key, pagesize, offset, dockernames, types, global_checklist_ids, global_inspection_record_id, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc,isTaggedToBarCode,deviceIdsTaggedToClientBarCode);
                break;

            }
            case "record_checklist": {
                //Scheduled inspections and Services
                devices = this.getAllRecordChecklistDevicesPagination(santized_search_key, pagesize, offset, dockernames, types, global_checklist_ids, inspection_record_id, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc,isTaggedToBarCode,deviceIdsTaggedToClientBarCode);
                break;

            }
            case "qrcode": {
                //Qrcode
                devices = this.getAllQrcodeDevicesPagination(santized_search_key, pagesize, offset, dockernames, types, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc,isTaggedToBarCode,deviceIdsTaggedToClientBarCode);
                break;
            }
            case "barcode": {
                //Barcode
                devices = this.getAllBarCodeDevicesPagination(santized_search_key, pagesize, offset, dockernames, types, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc,isTaggedToBarCode,deviceIdsTaggedToClientBarCode);
                break;
            }
            default:
                return Collections.emptySet();
        }
        return getDevicesWithQrCodeCount(vdmsid, devices);
    }

    private Set<DeviceDTO> getAllBarCodeDevicesPagination(String searchkey, Integer pagesize, Integer offset, JSONArray dockernames, JSONArray types, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode) {
        return deviceRepository.getAllBarCodeDevicesPagination(searchkey, pagesize, offset, dockernames, types, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc,isTaggedToBarCode,deviceIdsTaggedToClientBarCode);
    }


    private Set<DeviceDTO> getAllRecordChecklistDevicesPagination(String searchkey, Integer pagesize, Integer
            offset, JSONArray dockernames, JSONArray types, JSONArray global_checklist_ids, String
                                                                          inspection_record_id, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray
                                                                          deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode) {
        return deviceRepository.getAllRecordChecklistDevicesPagination(searchkey, pagesize, offset, dockernames, types, global_checklist_ids, inspection_record_id, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc,isTaggedToBarCode,deviceIdsTaggedToClientBarCode);
    }


    private Set<DeviceDTO> getAllChecklistDevicesPagination(String searchkey, Integer pagesize, Integer
            offset, JSONArray dockernames, JSONArray types, JSONArray global_checklist_ids, JSONArray
                                                                    virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean
                                                                    isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode) {
        return deviceRepository.getAllChecklistDevicesPagination(searchkey, pagesize, offset, dockernames, types, global_checklist_ids, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc,isTaggedToBarCode,deviceIdsTaggedToClientBarCode);
    }

    private Set<DeviceDTO> getAllInspectionDevicesPagination(String searchkey, Integer pagesize, Integer
            offset, JSONArray dockernames, JSONArray types, JSONArray global_checklist_ids, String
                                                                     global_inspection_record_id, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray
                                                                     deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode) {
        return deviceRepository.getAllInspectionDevicesPagination(searchkey, pagesize, offset, dockernames, types, global_checklist_ids, global_inspection_record_id, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc,isTaggedToBarCode,deviceIdsTaggedToClientBarCode);
    }

    private Set<DeviceDTO> getAllQrcodeDevicesPagination(String searchkey, Integer pagesize, Integer
            offset, JSONArray dockernames, JSONArray types, JSONArray virtual_device_types, Boolean
                                                                 isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode) {
        return deviceRepository.getAllQrcodeDevicesPagination(searchkey, pagesize, offset, dockernames, types, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc,isTaggedToBarCode,deviceIdsTaggedToClientBarCode);
    }

    private Set<DeviceDTO> getAllParentDeviceByPagination(JSONArray dockernames, JSONArray types, String
            searchkey, Integer pagesize, Integer offset, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray
                                                                  deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc, Boolean isTaggedToBarCode, JSONArray deviceIdsTaggedToClientBarCode) {
        return deviceRepository.getAllNetworkParentDeviceByPagination(dockernames, types, searchkey, pagesize, offset, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc,isTaggedToBarCode,deviceIdsTaggedToClientBarCode);
    }

    public void getDeviceConditionStatus(String deviceId, Integer status) {

        Set<DeviceConditionsDTO> deviceConditions = deviceConditionsService.getDeviceConditions(null, null, null, deviceId);
        for (DeviceConditionsDTO deviceCondition : deviceConditions) {
            AlertProfileDTO alertProfile = alertProfileService.getAlertProfileById(deviceCondition.getAlert_profile_id());

            DeviceAlertDTO deviceAlert = this.getDeviceConditionAlertInfoById(deviceCondition.getDevice_id());

            Boolean checkCondition = true;
            Boolean alerted = false;
            if (deviceCondition.getLast_alerted() != null) {

                alerted = deviceCondition.getLast_alerted();

            }

            BigInteger currentTimestamp = BigInteger.valueOf(System.currentTimeMillis());

            if (deviceCondition.getSchedule() != null && deviceCondition.getSchedule() == 1) {
                System.out.println("Start Time " + deviceCondition.getStart_time() + "    End Time " + deviceCondition.getEnd_time());
                checkCondition = conditionUtils.verifyCurrentSystemTimeWithinScheduledTime(deviceCondition.getStart_time(), deviceCondition.getEnd_time()); // reusing from conditions from sensor
                System.out.println("checkCondition inside " + checkCondition);
            }

            if (checkCondition) {


                if (deviceCondition.getAlert_condition().equals("device_online_alert") && (status != null && status == 1) && (!alerted)) {

                    deviceAlert.setTemplate_type("device_online");

                    if (deviceCondition.getTrigger_time() != null) {

                        deviceCondition.setLast_alerted(true);
                        ScheduledJobDTO scheduledJobDTO = jobSchedulerService.getScheduledJobByConditionId(deviceCondition.getId());

                        if (scheduledJobDTO == null) {

                            System.out.println("------------ Online job added ----------------");
                            this.scheduleDeviceAlertJob("add", deviceCondition);
                        } else {
                            this.replaceDeviceAlertJob("replace", deviceCondition, scheduledJobDTO.getId());
                        }
                    } else if (deviceCondition.getTrigger_time() == null && deviceCondition.getAlert_count_time() == null) {

                        if (deviceCondition.getAlert_count_enabled() == 1) {
                            deviceCondition.setAlert_count(deviceCondition.getAlert_count() + 1);
                        }
                        deviceCondition.setLast_alerted(true);
                    }

                    if (deviceCondition.getAlert_count_time() != null) {
                        if (deviceCondition.getLast_alerted_time() != null) {
                            Integer timeDifference = (currentTimestamp.subtract(deviceCondition.getLast_alerted_time())).intValue() / 1000;
                            if (timeDifference > deviceCondition.getAlert_count_time()) {
                                deviceCondition.setAlert_count(1);
                                deviceCondition.setLast_alerted_time(currentTimestamp);
                                deviceCondition.setLast_alerted(true);
                            } else {
                                deviceCondition.setAlert_count(deviceCondition.getAlert_count() + 1);
                                deviceCondition.setLast_alerted(true);
                            }

                        } else {
                            deviceCondition.setAlert_count(1);
                            deviceCondition.setLast_alerted_time(currentTimestamp);
                            deviceCondition.setLast_alerted(true);
                        }
                    }

                }

                if (deviceCondition.getAlert_condition().equals("device_offline_alert") && (status != null && status == 0) && (!alerted)) {
                    deviceAlert.setTemplate_type("device_offline");

                    if (deviceCondition.getTrigger_time() != null) {
                        ScheduledJobDTO scheduledJobDTO = jobSchedulerService.getScheduledJobByConditionId(deviceCondition.getId());
                        deviceCondition.setLast_alerted(true);
                        if (scheduledJobDTO == null) {
                            System.out.println("------------ Offline job added ----------------");
                            this.scheduleDeviceAlertJob("add", deviceCondition);
                        } else {
                            this.replaceDeviceAlertJob("replace", deviceCondition, scheduledJobDTO.getId());
                        }
                    } else if (deviceCondition.getTrigger_time() == null && deviceCondition.getAlert_count_time() == null) {

                        if (deviceCondition.getAlert_count_enabled() == 1) {
                            deviceCondition.setAlert_count(deviceCondition.getAlert_count() + 1);

                        }
                        deviceCondition.setLast_alerted(true);

                    }

                    if (deviceCondition.getAlert_count_time() != null) {
                        if (deviceCondition.getLast_alerted_time() != null) {
                            Integer timeDifference = (currentTimestamp.subtract(deviceCondition.getLast_alerted_time())).intValue() / 1000;
                            if (timeDifference > deviceCondition.getAlert_count_time()) {
                                deviceCondition.setAlert_count(1);
                                deviceCondition.setLast_alerted_time(currentTimestamp);
                                deviceCondition.setLast_alerted(true);
                            } else {
                                deviceCondition.setAlert_count(deviceCondition.getAlert_count() + 1);
                                deviceCondition.setLast_alerted(true);
                            }

                        } else {
                            deviceCondition.setAlert_count(1);
                            deviceCondition.setLast_alerted_time(currentTimestamp);
                            deviceCondition.setLast_alerted(true);

                        }
                    }

                }

                if ((deviceCondition.getAlert_condition().equals("device_offline_alert") && (status != null && status == 1)) || (deviceCondition.getAlert_condition().equals("device_online_alert") && (status != null && status == 0))) {

                    deviceCondition.setLast_alerted(false);
                    this.deleteDeviceAlertJob(deviceCondition.getId());
                }

                deviceConditionsService.updateLastAlertedDetails(deviceCondition.getId(), deviceCondition.getLast_alerted_time(), deviceCondition.getLast_alerted(), deviceCondition.getAlert_count());
                if (alertProfile != null) {
                    if ((!alerted) && deviceCondition.getLast_alerted()) {
                        if (deviceCondition.getAlert_count_enabled() != 1 && deviceCondition.getTrigger_time() == null) {
                            System.out.println("------------ sending normal email --------------");
                            this.sendDeviceEmailAlerts(deviceCondition, deviceAlert, alertProfile, currentTimestamp, status);
                        } else if (deviceCondition.getAlert_count_enabled() == 1 && (deviceCondition.getAlert_count() == deviceCondition.getMax_alert_count())) {
                            System.out.println("------------ sending count based email --------------");
                            this.sendDeviceEmailAlerts(deviceCondition, deviceAlert, alertProfile, currentTimestamp, status);
                        }
                    }
                }

            } else if (!checkCondition) {
                if ((deviceCondition.getAlert_condition().equals("device_offline_alert") && (status != null && status == 1)) || (deviceCondition.getAlert_condition().equals("device_online_alert") && (status != null && status == 0))) {
                    deviceCondition.setLast_alerted(false);
                    deviceConditionsService.updateLastAlertedDetails(deviceCondition.getId(), deviceCondition.getLast_alerted_time(), deviceCondition.getLast_alerted(), deviceCondition.getAlert_count());
                    this.deleteDeviceAlertJob(deviceCondition.getId());
                }
            }
        }

    }


    public void scheduleDeviceAlertJob(String job_type, DeviceConditionsDTO deviceCondition) {
        ScheduledJobDTO jobSchedulerDTO = new ScheduledJobDTO();
        jobSchedulerDTO.setJob_type(job_type);
        jobSchedulerDTO.setTime_in_seconds(Long.valueOf(deviceCondition.getTrigger_time()));
        String job_id = jobSchedulerService.createScheduledJob(jobSchedulerDTO);

        if (job_id != null) {
            ScheduledJobDTO scheduledJobDTO = new ScheduledJobDTO();
            scheduledJobDTO.setId(job_id);
            scheduledJobDTO.setCondition_id(deviceCondition.getId());
            scheduledJobDTO.setCondition_type("device");
            scheduledJobDTO.setCondition_group("device");

            jobSchedulerService.addScheduledJob(Set.of(scheduledJobDTO));
        }
    }

    public void replaceDeviceAlertJob(String job_type, DeviceConditionsDTO deviceCondition, String job_key) {
        ScheduledJobDTO jobSchedulerDTO = new ScheduledJobDTO();
        jobSchedulerDTO.setJob_type(job_type);
        jobSchedulerDTO.setTime_in_seconds(Long.valueOf(deviceCondition.getTrigger_time()));
        jobSchedulerDTO.setId(job_key);
        String job_id = jobSchedulerService.createScheduledJob(jobSchedulerDTO);

        if (job_id != null) {
            jobSchedulerService.deleteScheduledJob(Set.of(job_key));

            ScheduledJobDTO scheduledJobDTO = new ScheduledJobDTO();
            scheduledJobDTO.setId(job_id);
            scheduledJobDTO.setCondition_id(deviceCondition.getId());
            scheduledJobDTO.setCondition_type("device");
            scheduledJobDTO.setCondition_group("device");

            jobSchedulerService.addScheduledJob(Set.of(scheduledJobDTO));
        }
    }

    public void deleteDeviceAlertJob(String conditionId) {
        try {
            ScheduledJobDTO scheduledJobDTO = jobSchedulerService.getScheduledJobByConditionId(conditionId);
            if (scheduledJobDTO != null) {
                //api call to quartz to delete the job
                ScheduledJobDTO jobSchedulerDTO = new ScheduledJobDTO();
                jobSchedulerDTO.setJob_type("delete");
                jobSchedulerDTO.setId(scheduledJobDTO.getId());
                System.out.println("deleting from scheduler");
                String job_id = jobSchedulerService.createScheduledJob(jobSchedulerDTO);
                //delete the record
                if (job_id != null) {
                    scheduledJobRepository.deleteByConditionId(conditionId);
                }
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void sendDeviceEmailAlerts(DeviceConditionsDTO deviceConditionsDTO, DeviceAlertDTO
            deviceAlert, AlertProfileDTO alertProfile, BigInteger current_timestamp, Integer status) {
        log.info("Checking AlertDownTimeState for DeviceId: {} and AlertProfileId: {}",deviceConditionsDTO.getDevice_id(), alertProfile.getId());
        Boolean alertDowntimeState = alertDowntimeScheduleService.checkAlertDowntime(deviceConditionsDTO.getDevice_id(), alertProfile.getId());
        log.info("AlertDownTimeState {}",alertDowntimeState);
        if (!(alertDowntimeState)) {
            if (deviceAlert.getLocal_vendor_id() != null) {
                PhonebookAddressDto phonebook_details = phonebookService.getPhoneAddressById(deviceAlert.getLocal_vendor_id());
                deviceAlert.setLocal_vendor(phonebook_details);
            }
            deviceAlert.setAlert_time(current_timestamp);
            deviceAlert.setAlert_message(deviceConditionsDTO.getAlert_message());

            alertService.sendDeviceConditionsAlertInfo(deviceAlert, alertProfile, current_timestamp);

            if (alertProfile.getIoc() != null && alertProfile.getIoc() == 1 && status == 0) {
                iocService.sendDeviceAlertDataIOC(deviceConditionsDTO, deviceAlert, status, alertProfile, current_timestamp);
            }
        }
    }

    public DeviceAlertDTO getDeviceConditionAlertInfoById(String device_id) {
        return deviceRepository.getDeviceConditionAlertInfoById(device_id);
    }

    public Set<DeviceDTO> listAllDeviceByVdmsId(String vdms_id) {
        return deviceRepository.listAllDeviceByVdmsId(vdms_id);
    }


    // update ecobee sensor count by device id
    public void updateDeviceEcobeeCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer ecobee_count = ecobeeService.getEcobeeSensorCountByDeviceId(device_id);
                deviceRepository.updateDeviceEcobeeCount(device_id, ecobee_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating ecobee count by device id " + e);
        }
    }


    public void updateDeviceEcobeeStatusByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Boolean alert = ecobeeService.getEcobeeSensorAlertStatusByDeviceId(device_id);
                String ecobee_status = "no-alert";
                if (alert) {
                    ecobee_status = "alert";
                }
                deviceRepository.updateDeviceEcobeeStatus(device_id, ecobee_status);
            }
        } catch (Exception e) {
            System.out.println("Error in updating pelican status by device id " + e);
        }
    }

    // update ecobee sensor status
    public void updateDeviceEcobeeStatus(String ecobee_sensor_id) {
        try {
            String device_id = ecobeeService.getDeviceIdByEcobeeSensorId(ecobee_sensor_id);
            this.updateDeviceEcobeeStatusByDeviceId(device_id);
        } catch (Exception e) {
            System.out.println("Error in updating ecobee status " + e);
        }
    }

    public List<DeviceSensorsDTO> getDeviceSensorsWithQrCodeDetails(String vdms_id, List<DeviceSensorsDTO> deviceSensors) {

        try {
            Set<String> deviceIds = new HashSet<>();
            for (DeviceSensorsDTO deviceSensor : deviceSensors) {
                deviceIds.add(deviceSensor.getId());
            }
            Set<QrCodeDTO> deviceQrCodes = qrCodeService.getQrCodesByDeviceIds(deviceIds);
            for (DeviceSensorsDTO deviceSensor : deviceSensors) {
                for (QrCodeDTO deviceQrCode : deviceQrCodes) {
                    if (deviceQrCode.getDeviceId().equals(deviceSensor.getId())) {
                        deviceSensor.setGlobal_qrcode_id(deviceQrCode.getId());
                        break;
                    }
                }
            }

            Set<NfcDTO> deviceNfcs = nfcService.getNfcsByDeviceIds(deviceIds);
            for (DeviceSensorsDTO deviceSensor : deviceSensors) {
                for (NfcDTO nfcDTO : deviceNfcs) {
                    if (nfcDTO.getDeviceId().equals(deviceSensor.getId())) {
                        deviceSensor.setNfc_id(nfcDTO.getId());
                        break;
                    }
                }
            }
            Set<ClientBarCodeDTO> barCodesTaggedToLocations = clientBarCodeService.getBarCodesByDeviceIds(deviceIds);
            for (DeviceSensorsDTO deviceSensor : deviceSensors) {
                for (ClientBarCodeDTO clientBarCodeDTO : barCodesTaggedToLocations) {
                    if (clientBarCodeDTO.getDeviceId().equals(deviceSensor.getId())) {
                        deviceSensor.setBarcode_id(clientBarCodeDTO.getId());
                        break;
                    }
                }
            }
            return deviceSensors;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }


    //get devices by types
    public List<DeviceSensorsDTO> getDevicesByType(String vdms_id, String network_name, String
            floor_id, Set<String> types) {

        List<DeviceSensorsDTO> deviceSensors = deviceRepository.getDevicesByType(network_name, floor_id, types);
        for (DeviceSensorsDTO deviceSensor : deviceSensors) {
            deviceSensor.setSensor_alert(false);
            List<SensorDTO> allSensors = this.getAllDeviceSensorsTS(deviceSensor.getId());
            for (SensorDTO sensor : allSensors) {
                if (sensor.getAlert()) {
                    deviceSensor.setSensor_alert(true);
                    break;
                }
            }
            deviceSensor.setSensors(allSensors);
        }

        List<DeviceSensorsDTO> deviceSensorsWithQrCodeDetails = this.getDeviceSensorsWithQrCodeDetails(vdms_id, deviceSensors);
        if (deviceSensorsWithQrCodeDetails != null) {
            return deviceSensorsWithQrCodeDetails;
        }
        return deviceSensors;
    }

    public List<DeviceSensorsDTO> getDevicesByTypePagination(String vdms_id, String network_name, String
            floor_id, Set<String> types, Integer pagesize,
                                                             Integer pageno) {
        Integer offset = pagesize * (pageno - 1);
        List<DeviceSensorsDTO> deviceSensors = deviceRepository.getDevicesByTypePagination(network_name, floor_id, types, pagesize, offset);
        for (DeviceSensorsDTO deviceSensor : deviceSensors) {
            deviceSensor.setSensor_alert(false);
            List<SensorDTO> allSensors = this.getAllDeviceSensorsTS(deviceSensor.getId());
            for (SensorDTO sensor : allSensors) {
                if (sensor.getAlert()) {
                    deviceSensor.setSensor_alert(true);
                    break;
                }
            }

            deviceSensor.setSensors(allSensors);
        }

        List<DeviceSensorsDTO> deviceSensorsWithQrCodeDetails = this.getDeviceSensorsWithQrCodeDetails(vdms_id, deviceSensors);
        if (deviceSensorsWithQrCodeDetails != null) {
            return deviceSensorsWithQrCodeDetails;
        }
        return deviceSensors;
    }

    public Set<DeviceDTO> getDevicesWithQrCodeCount(String vdms_id, Set<DeviceDTO> devices) {
        try {
            for (DeviceDTO device : devices) {
                device.setQrcode_count(qrCodeService.getQrCodeCountByDeviceId(device.getId()) + clientQrCodeService.getClientQrCodeCountByDeviceId(device.getId()));
                device.setNfc_count(nfcService.getQrNfcCountByDeviceId(device.getId()) + clientNfcService.getClientNfcCountByDeviceId(device.getId()));
                device.setBarcode_count(clientBarCodeService.getClientBarCodeCountByDeviceId(device.getId()));
            }
            return devices;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }


    // Get Single device information
    public DeviceDTO getDeviceByDeviceId(String username, String vdmsid, String dockername, String device_id) {

        DeviceDTO device = deviceRepository.getDeviceByDeviceId(device_id);
        if (device != null) {
            device.setIp_addresses(this.getDeviceIPAddressByDeviceId(device_id));
            device.setSubsystems(new HashSet<>());
            device.setOnboard_data(new DeviceOnboardStatusDTO(device.getAssignee_email(), device.getImage_status(), device.getGeolocation_status(), device.getTag_status(), device.getField_status(), deviceOnboardStatusAssigneeService.getDeviceOnboardStatusAssignees(device.getDevice_onboard_status_id())));
            // ITAM Changes
            DeviceSpecification spec = deviceSpecificationRepository.findByDeviceId(device.getId());
            if (spec != null) {
                device.setIs_agent_device(true);
                JSONObject osInfo = parseObjectSafe(spec.getOsInfo());
                JSONObject locationInfo = parseObjectSafe(spec.getLocationInfo());

                if (osInfo != null && osInfo.containsKey("username")) {
                    device.setUsername(osInfo.getString("username"));
                }

                if (locationInfo != null) {
                    if (locationInfo.containsKey("ispLocation")) {
                        device.setIsp_location(locationInfo.getString("ispLocation"));
                    }
                    if (locationInfo.containsKey("pcLocation")) {
                        device.setPc_location(locationInfo.getString("pcLocation"));
                    }
                }
            }


            try {
                device.setQrcode_count(qrCodeService.getQrCodeCountByDeviceId(device.getId()) + clientQrCodeService.getClientQrCodeCountByDeviceId(device.getId()));
                device.setNfc_count(nfcService.getQrNfcCountByDeviceId(device.getId()) + clientNfcService.getClientNfcCountByDeviceId(device.getId()));
                device.setBarcode_count(clientBarCodeService.getClientBarCodeCountByDeviceId(device.getId()));
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return device;
    }

    // get devices by ids
    public Set<DeviceDTO> getDevicesByIdList(String vdms_id, Set<String> device_ids) {
        Set<DeviceDTO> devices = deviceRepository.getDevicesByIdList(device_ids);
        for (DeviceDTO device : devices) {
            device.setIp_addresses(this.getDeviceIPAddressByDeviceId(device.getId()));
            device.setSubsystems(new HashSet<>());
            device.setOnboard_data(new DeviceOnboardStatusDTO(device.getAssignee_email(), device.getImage_status(), device.getGeolocation_status(), device.getTag_status(), device.getField_status(), deviceOnboardStatusAssigneeService.getDeviceOnboardStatusAssignees(device.getDevice_onboard_status_id())));
            // ITAM Changes
            DeviceSpecification spec = deviceSpecificationRepository.findByDeviceId(device.getId());
            if (spec != null) {
                device.setIs_agent_device(true);
                JSONObject osInfo = parseObjectSafe(spec.getOsInfo());
                JSONObject locationInfo = parseObjectSafe(spec.getLocationInfo());

                if (osInfo != null && osInfo.containsKey("username")) {
                    device.setUsername(osInfo.getString("username"));
                }

                if (locationInfo != null) {
                    if (locationInfo.containsKey("ispLocation")) {
                        device.setIsp_location(locationInfo.getString("ispLocation"));
                    }
                    if (locationInfo.containsKey("pcLocation")) {
                        device.setPc_location(locationInfo.getString("pcLocation"));
                    }
                }
            }
        }

        Set<DeviceDTO> devicesWithQrCodeDetails = this.getDevicesWithQrCodeCount(vdms_id, devices);
        if (devicesWithQrCodeDetails != null) {
            return devicesWithQrCodeDetails;
        }

        return devices;
    }

    public Set<DeviceDTO> getFilterVirtualDevicesByPagination(String username, String vdmsid, String
            searchKey, Integer pageNo, Integer
                                                                      pageSize, Set<String> dockernames, Set<String> types, Set<String> virtual_device_types) {

        Integer offset = pageSize * (pageNo - 1);

        Set<DeviceDTO> devices = deviceRepository.getFilterVirtualDevicesByPagination(searchKey, pageSize, offset, dockernames, types, virtual_device_types);
        for (DeviceDTO device : devices) {

            List<SpecificationsDTO> allSpecifications = new ArrayList<>();

            // if required can be added here
            if (device.getVirtual_device_type() == null || device.getVirtual_device_type() != 4) {
                List<ConnectedDevicesDTO> allInputSpecifications = connectedDevicesService.getAllInputConnectedSpecifications(device.getId());
                List<ConnectedDevicesDTO> allOutputSpecifications = connectedDevicesService.getAllOutputConnectedSpecifications(device.getId());


                if (allInputSpecifications != null && allInputSpecifications.size() > 0) {
                    allSpecifications.addAll(connectedDevicesService.mappingConnectedDevicesToSpecifications(allInputSpecifications));
                }
                if (allOutputSpecifications != null && allOutputSpecifications.size() > 0) {
                    allSpecifications.addAll(connectedDevicesService.mappingConnectedDevicesToSpecifications(allOutputSpecifications));
                }

                if (allSpecifications != null && allSpecifications.size() > 0) {
                    device.setSpecifications(allSpecifications);
                }

            }
        }
        return devices;

    }


    public List<DeviceDTO> getDeviceDetailsByDeviceIdList(Set<String> device_ids) {
        return deviceRepository.getDeviceDetailsByDeviceIdList(device_ids);

    }

    public DeviceDTO updateCustomFields(String device_id, JSONArray newCustomFields) {
        if (newCustomFields != null) {
            DeviceDTO existingDeviceDetails = deviceRepository.getDeviceByDeviceId(device_id);
            if (existingDeviceDetails != null) {
                if (existingDeviceDetails.getCustom_fields() != null) {
                    JSONArray updatedCustomFields = new JSONArray();
                    // Get existing custom fields from db
                    JSONArray existingCustomFields = JSONArray.parseArray(existingDeviceDetails.getCustom_fields());
                    // Map db custom fields
                    Map<String, Object> dbCustomFields = new HashMap<>();
                    for (int j = 0; j < existingCustomFields.size(); j++) {
                        dbCustomFields.putAll(existingCustomFields.getJSONObject(j));
                    }
                    for (int i = 0; i < newCustomFields.size(); i++) {
                        //  Get custom fields  keys , keys are unknown
                        Set<String> newCustomFieldsKeys = newCustomFields.getJSONObject(i).keySet();
                        int flag = 0;
                        for (String newCustomFieldsKey : newCustomFieldsKeys) {
                            if (dbCustomFields.containsKey(newCustomFieldsKey)) {
                                //Update the value of existing key
                                dbCustomFields.put(newCustomFieldsKey, newCustomFields.getJSONObject(i).get(newCustomFieldsKey));
                                flag = 1;
                            }
                        }
                        if (flag == 0) {
                            // Add new custom field
                            updatedCustomFields.add(newCustomFields.get(i));
                        }
                    }
                    // Convert map to Json array
                    for (var entry : dbCustomFields.entrySet()) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(entry.getKey(), entry.getValue());
                        updatedCustomFields.add(jsonObject);
                    }
                    existingDeviceDetails.setCustom_fields(JSON.toJSONString(updatedCustomFields, SerializerFeature.WriteMapNullValue));
                } else {
                    existingDeviceDetails.setCustom_fields(JSON.toJSONString(newCustomFields, SerializerFeature.WriteMapNullValue));
                }
            }
            return existingDeviceDetails;
        }
        return null;
    }

    public Integer getPowerSourceTopologyConnectionsCount(String username, String vdmsid) {

        return connectedDevicesService.getPowerSourceTopologyConnectionsCount();
    }

    public PowerSourceTopologyDTO getPowerSourceTopologyByPagination(String username, String vdmsid, Integer
            pageno, Integer pagesize) {

        return connectedDevicesService.getPowerSourceTopologyByPagination(pageno, pagesize);
    }


    public Set<DeviceDTO> getDevicesByIds(Set<String> device_ids) {
        Set<DeviceDTO> devices = deviceRepository.getDevicesByIdList(device_ids);
        return devices.stream().filter(device -> (device.getAsset_match_status() != 3 && (device.getType() != null && device.getType() != ""))).collect(Collectors.toSet());
    }

    public JSONObject getDeviceQrNfcCountFromCloud(String vdms_id) {
        try {
            int qrDeviceCount;
            int nfcDeviceCount;
            int qrNfcDeviceCount;
            JSONObject deviceQrNfcCount = new JSONObject();
            JSONArray deviceIdsTaggedToNFC = apicallService.getNfcIdsByVdmsAndType(vdms_id, "device");
            JSONArray deviceIdsTaggedToQrCode = apicallService.getQrCodeIdsByVdmsIdAndType(vdms_id, "device");
            Set<String> qrCodeNfcSet = new HashSet<>();
            Set<String> qrCodeSet = new HashSet<>();
            Set<String> nfcSet = new HashSet<>();
            if (deviceIdsTaggedToNFC != null) {
                for (int i = 0; i < deviceIdsTaggedToQrCode.size(); i++) {
                    qrCodeNfcSet.add(deviceIdsTaggedToQrCode.getString(i));
                    qrCodeSet.add(deviceIdsTaggedToQrCode.getString(i));
                }
            }

            if (deviceIdsTaggedToNFC != null) {
                for (int i = 0; i < deviceIdsTaggedToNFC.size(); i++) {
                    qrCodeNfcSet.add(deviceIdsTaggedToNFC.getString(i));
                    nfcSet.add(deviceIdsTaggedToNFC.getString(i));
                }
            }


            Set<DeviceDTO> qrCodeTaggedDevices = this.getDevicesByIds(qrCodeSet);
            Set<DeviceDTO> nfcTaggedDevices = this.getDevicesByIds(nfcSet);
            Set<DeviceDTO> qrcodeNfcTaggedDevices = this.getDevicesByIds(qrCodeNfcSet);

            qrDeviceCount = qrCodeTaggedDevices.size();
            nfcDeviceCount = nfcTaggedDevices.size();
            qrNfcDeviceCount = qrcodeNfcTaggedDevices.size();
            deviceQrNfcCount.put("qr_device_count", qrDeviceCount);
            deviceQrNfcCount.put("nfc_device_count", nfcDeviceCount);
            deviceQrNfcCount.put("qr_nfc_device_count", qrNfcDeviceCount);
            return deviceQrNfcCount;
        } catch (Exception e) {
            System.out.println("error getting qr nfc count from cloud");
            System.out.println(e);

        }
        return null;
    }

    public void updateDeviceModbusStatus(String modbus_register_id) {
        try {
            String device_id = modbusService.getDeviceIdByModbusRegisterId(modbus_register_id);
            this.updateDeviceModbusStatusByDeviceId(device_id);
        } catch (Exception e) {
            System.out.println("Error in updating modbus status " + e);
        }
    }

    public void updateDeviceModbusStatusByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Boolean alert = modbusService.getModbusRegisterAlertStatusByDeviceId(device_id);
                String modbus_status = "no-alert";
                if (alert) {
                    modbus_status = "alert";
                }
                deviceRepository.updateDeviceModbusStatus(device_id, modbus_status);
            }
        } catch (Exception e) {
            System.out.println("Error in updating modbus status by device id " + e);
        }
    }

    // update modbus  count
    public void updateDeviceModbusCount(String modbus_register_id) {
        try {
            String device_id = modbusService.getDeviceIdByModbusRegisterId(modbus_register_id);
            this.updateDeviceModbusCountByDeviceId(device_id);
        } catch (Exception e) {
            System.out.println("Error in updating modbus count " + e);
        }
    }

    // update modbus register count by device id
    public void updateDeviceModbusCountByDeviceId(String device_id) {
        try {
            if (device_id != null) {
                Integer modbus_count = modbusService.getModbusRegistersCountByDeviceId(device_id);
                deviceRepository.updateDeviceModbusCount(device_id, modbus_count);
            }
        } catch (Exception e) {
            System.out.println("Error in updating modbus count by device id " + e);
        }
    }

    public DeviceDTO getDeviceDetailsByDeviceId(String device_id) {
        return deviceRepository.getDeviceByDeviceId(device_id);
    }

    public Set<DeviceDTO> getAssetsByLocationId(String username, String vdmsid, String location_id, Integer
            pageno, Integer pagesize) {
        Set<DeviceDTO> devices;
        Integer offset = pagesize * (pageno - 1);

        devices = deviceRepository.getAssetsByLocationId(location_id, pagesize, offset);

        for (DeviceDTO device : devices) {
            try {
                device.setIp_addresses(this.getDeviceIPAddressByDeviceId(device.getId()));
                device.setOnboard_data(new DeviceOnboardStatusDTO(device.getAssignee_email(), device.getImage_status(), device.getGeolocation_status(), device.getTag_status(), device.getField_status(), deviceOnboardStatusAssigneeService.getDeviceOnboardStatusAssignees(device.getDevice_onboard_status_id())));
                device.setSubsystems(new HashSet<>());
            } catch (Exception e) {
                System.out.println("Error getting device ip address " + e);
                System.out.println(e);
            }
        }
        Set<DeviceDTO> devicesWithQrCodeDetails = this.getDevicesWithQrCodeCount(vdmsid, devices);
        if (devicesWithQrCodeDetails != null) {
            return devicesWithQrCodeDetails;
        }
        return devices;
    }

    public String getDeviceRebootStatus(String username, String vdmsid, String deviceid) {
        String reboot_status = deviceRepository.getDeviceRebootStatus(deviceid);
        if (reboot_status != null && (!reboot_status.contains("in-progress"))) {
            deviceRepository.updateDeviceRebootStatus(deviceid, null);
        }
        return reboot_status;
    }

    public void updateDeviceRebootStatus(String device_id, String status) {
        deviceRepository.updateDeviceRebootStatus(device_id, status);
    }

    public Set<DeviceDTO> getAllDevicesByGroup(String username, String vdmsid, JSONObject filterObject, String
            global_checklist_id, String global_inspection_record_id, String group) {
        JSONArray dockernames = filterObject.getJSONArray("docker_names");
        JSONArray types = filterObject.getJSONArray("types");
        String searchkey = filterObject.getString("search_key");
        JSONArray virtual_device_types = filterObject.getJSONArray("virtual_device_types");
        Boolean isTaggedToQrCode = filterObject.getBoolean("is_tagged_to_qrcode");
        Boolean isTaggedToNfc = filterObject.getBoolean("is_tagged_to_nfc");
        Boolean isTaggedToBarCode = filterObject.getBoolean("is_tagged_to_barcode");

        JSONArray deviceIdsTaggedToQrCode = new JSONArray();
        JSONArray deviceIdsTaggedToClientQrCode = new JSONArray();
        JSONArray deviceIdsTaggedToNfc = new JSONArray();
        JSONArray deviceIdsTaggedToClientNfc = new JSONArray();
        JSONArray deviceIdsTaggedToClientBarCode = new JSONArray();
        Set<DeviceDTO> devices;
        if (isTaggedToQrCode != null) {
            deviceIdsTaggedToQrCode = qrCodeService.getDeviceIdsTaggedToQrCode(vdmsid);
            deviceIdsTaggedToClientQrCode = clientQrCodeService.getDeviceIdsTaggedToClientQrCode(vdmsid);
            deviceIdsTaggedToQrCode.addAll(deviceIdsTaggedToClientQrCode);
            if (deviceIdsTaggedToQrCode.isEmpty()) {
                deviceIdsTaggedToQrCode.add("");
            }
        }
        if (isTaggedToNfc != null) {
            deviceIdsTaggedToNfc = nfcService.getDeviceIdsTaggedToNfc(vdmsid);
            deviceIdsTaggedToClientNfc = clientNfcService.getDeviceIdsTaggedToClientNfc(vdmsid);
            deviceIdsTaggedToNfc.addAll(deviceIdsTaggedToClientNfc);
            if (deviceIdsTaggedToNfc.isEmpty()) {
                deviceIdsTaggedToNfc.add("");
            }
        }

        if (isTaggedToBarCode != null) {
            deviceIdsTaggedToClientBarCode = clientBarCodeService.getDeviceIdsTaggedToClientBarCode(vdmsid);
            if (deviceIdsTaggedToClientBarCode.isEmpty()) {
                deviceIdsTaggedToClientBarCode.add("");
            }
        }

        switch (group) {
            case "all": {
                devices = this.getAllParentDevices(dockernames, types, searchkey, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc);
                break;
            }
            case "tagged": {
                // Procedures and Reactive services
                devices = this.getAllChecklistDevices(searchkey, dockernames, types, global_checklist_id, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc);
                break;
            }
            case "inspection": {
                //Scheduled inspections and Services
                devices = this.getAllInspectionDevices(searchkey, dockernames, types, global_checklist_id, global_inspection_record_id, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc);
                break;

            }
            case "qrcode": {
                //Qrcode
                devices = this.getAllQrcodeDevices(searchkey, dockernames, types, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc);
                break;
            }
            default:
                return Collections.emptySet();
        }
        return devices;
    }


    private Set<DeviceDTO> getAllChecklistDevices(String searchkey, JSONArray dockernames, JSONArray types, String
            global_checklist_id, JSONArray virtual_device_types, Boolean isTaggedToQrCode, JSONArray
                                                          deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc) {
        return deviceRepository.getAllChecklistDevices(searchkey, dockernames, types, global_checklist_id, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc);
    }

    private Set<DeviceDTO> getAllInspectionDevices(String searchkey, JSONArray dockernames, JSONArray types, String
            global_checklist_ids, String global_inspection_record_id, JSONArray virtual_device_types, Boolean
                                                           isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean isTaggedToNfc, JSONArray deviceIdsTaggedToNfc) {
        return deviceRepository.getAllInspectionDevices(searchkey, dockernames, types, global_checklist_ids, global_inspection_record_id, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc);
    }

    private Set<DeviceDTO> getAllQrcodeDevices(String searchkey, JSONArray dockernames, JSONArray types, JSONArray
            virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean
                                                       isTaggedToNfc, JSONArray deviceIdsTaggedToNfc) {
        return deviceRepository.getAllQrcodeDevices(searchkey, dockernames, types, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc);
    }

    private Set<DeviceDTO> getAllParentDevices(JSONArray dockernames, JSONArray types, String searchkey, JSONArray
            virtual_device_types, Boolean isTaggedToQrCode, JSONArray deviceIdsTaggedToQrCode, Boolean
                                                       isTaggedToNfc, JSONArray deviceIdsTaggedToNfc) {
        return deviceRepository.getAllNetworkParentDevices(dockernames, types, searchkey, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode, isTaggedToNfc, deviceIdsTaggedToNfc);
    }

    public List<String> getDeviceIdsByFilter(List<String> dockerNames, List<String> types, String searchKey,
                                             List<String> virtual_device_types, Boolean isTaggedToQrCode,
                                             Boolean isTaggedToNfc, List<String> locationIds) {
        String vdmsid = authenticationUtils.getVdms_id();
        List<String> deviceIdsTaggedToQrCode = new ArrayList<>();
        List<String> deviceIdsTaggedToClientQrCode = new ArrayList<>();
        List<String> deviceIdsTaggedToNfc = new ArrayList<>();
        List<String> deviceIdsTaggedToClientToNfc = new ArrayList<>();
        if (isTaggedToQrCode != null) {
            deviceIdsTaggedToQrCode = (List) qrCodeService.getDeviceIdsTaggedToQrCode(vdmsid);
            deviceIdsTaggedToClientQrCode = (List) clientQrCodeService.getDeviceIdsTaggedToClientQrCode(vdmsid);
            deviceIdsTaggedToQrCode.addAll(deviceIdsTaggedToClientQrCode);
            if (deviceIdsTaggedToQrCode.isEmpty()) {
                deviceIdsTaggedToQrCode.add("");
            }
        }
        if (isTaggedToNfc != null) {
            deviceIdsTaggedToNfc = (List) nfcService.getDeviceIdsTaggedToNfc(vdmsid);
            deviceIdsTaggedToClientToNfc = (List) clientNfcService.getDeviceIdsTaggedToClientNfc(vdmsid);
            deviceIdsTaggedToNfc.addAll(deviceIdsTaggedToClientToNfc);
            if (deviceIdsTaggedToNfc.isEmpty()) {
                deviceIdsTaggedToNfc.add("");
            }
        }

        return deviceRepository.getDeviceIds(dockerNames, types, searchKey, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode,
                isTaggedToNfc, deviceIdsTaggedToNfc, locationIds);
    }

    public List<DeviceDTO> getDevicesByFilter(List<String> dockerNames, List<String> types, String
            searchKey, List<String> virtual_device_types,
                                              Boolean isTaggedToQrCode, Boolean isTaggedToNfc, List<String> locationIds, List<String> deviceIds) {
        String vdmsid = authenticationUtils.getVdms_id();
        List<String> deviceIdsTaggedToQrCode = new ArrayList<>();
        List<String> deviceIdsTaggedToNfc = new ArrayList<>();
        List<String> deviceIdsTaggedToClientQrCode = new ArrayList<>();
        List<String> deviceIdsTaggedToClientNfc = new ArrayList<>();
        if (isTaggedToQrCode != null) {
            deviceIdsTaggedToQrCode = (List) qrCodeService.getDeviceIdsTaggedToQrCode(vdmsid);
            deviceIdsTaggedToClientQrCode = (List) clientQrCodeService.getDeviceIdsTaggedToClientQrCode(vdmsid);
            deviceIdsTaggedToQrCode.addAll(deviceIdsTaggedToClientQrCode);
            if (deviceIdsTaggedToQrCode.isEmpty()) {
                deviceIdsTaggedToQrCode.add("");
            }
        }
        if (isTaggedToNfc != null) {
            deviceIdsTaggedToNfc = (List) nfcService.getDeviceIdsTaggedToNfc(vdmsid);
            deviceIdsTaggedToClientNfc = (List) clientNfcService.getDeviceIdsTaggedToClientNfc(vdmsid);
            deviceIdsTaggedToNfc.addAll(deviceIdsTaggedToClientNfc);
            if (deviceIdsTaggedToNfc.isEmpty()) {
                deviceIdsTaggedToNfc.add("");
            }

        }
        return deviceRepository.getDevicesByFilter(dockerNames, types, searchKey, virtual_device_types, isTaggedToQrCode, deviceIdsTaggedToQrCode,
                isTaggedToNfc, deviceIdsTaggedToNfc, locationIds, deviceIds);
    }

    public void addAssetOnboardedDevices(String username, String vdmsid, List<DeviceDTO> deviceDTOS) {
        for (DeviceDTO deviceDTO : deviceDTOS) {
            String device_id = vdmsid + "_" + deviceDTO.getDocker_name() + "_" + deviceDTO.getUser_data_name() + "_" + System.nanoTime();
            String final_device_id = utils.replaceSpecialCharactersWithUnderscore(device_id);
            LocationDTO locationDTO = null;
            try {
                if (deviceDTO.getLocation_id() != null) {
                    //if location is tagged then match status is 2 - verified
                    deviceDTO.setAsset_match_status(2);
                } else {
                    deviceDTO.setAsset_match_status(0);
                }

                deviceDTO.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
                deviceRepository.addAssetOnboardedDevices(final_device_id, deviceDTO.getLocation_id(), deviceDTO.getModel(), deviceDTO.getVendor(), deviceDTO.getDescription(),
                        deviceDTO.getDocker_name(), vdmsid, deviceDTO.getAsset_match_status(), deviceDTO.getCreated_timestamp(), username, deviceDTO.getAsset_group(),
                        deviceDTO.getVirtual_device_type(), deviceDTO.getMonitor(), deviceDTO.getUser_data_name());
                if (deviceDTO.getSpecifications() != null && deviceDTO.getSpecifications().size() > 0) {
                    for (SpecificationsDTO specificationsDTO : deviceDTO.getSpecifications()) {
                        int count = specificationsService.checkSpecificationByDeviceId(final_device_id, specificationsDTO.getKey_name());
                        if (count == 0) {
                            specificationsDTO.setDevice_id(final_device_id);
                            specificationsService.upsertDeviceSpecification(specificationsDTO);
                        }
                    }
                }
                if (deviceDTO.getAsset_image_url() != null) {
                    this.upsertOnboardedAssetImages(username, vdmsid, final_device_id, utils.getJSONArrayFromJSONString(deviceDTO.getAsset_image_url(), String.class));
                }
                if (deviceDTO.getLocation_id() != null) {
                    locationDTO = locationService.getLocationByLocationId(deviceDTO.getLocation_id());
                }
                userActionLogService.addUserAction(username, "asset", "ADD", "A Onboarded Device  Model: " + deviceDTO.getModel() + " and id: " + final_device_id + " is added for network " + deviceDTO.getDocker_name() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_info", final_device_id);

            } catch (Exception e) {
                userActionLogService.addUserAction(username, "asset", "ADD", "Unable to Add onboarded Device name: " + deviceDTO.getModel() + " and id: " + final_device_id + " for network " + deviceDTO.getDocker_name() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_info", final_device_id);

            }

            this.syncSingleDeviceOnboardStatus(vdmsid, final_device_id);
        }
    }

    public void upsertOnboardedAssetImages(String username, String vdms_id, String
            device_id, List<String> asset_image_urls) {
        JSONArray array = new JSONArray();
        String action = "ADD";
        DeviceDTO deviceDTO = deviceRepository.getDeviceByDeviceId(device_id);
        String device_name = deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().equals("") ? deviceDTO.getDisplay_name() : deviceDTO.getUser_data_name();
        LocationDTO locationDTO = null;
        try {
            if (deviceDTO.getAsset_image_url() != null) {
                array = JSON.parseArray(deviceDTO.getAsset_image_url());
                action = "UPDATE";
            }

            for (int i = 0; i < array.size(); i++) {
                utils.removeFileFromServerByImageURL(array.getString(i), server_asset_images_absolute_path);
                array.clear();
            }


            for (String asset_image_url : asset_image_urls) {
                try {
                    if (asset_image_url != null) {
                        //get file path
                        String final_asset_image_url = utils.addImageByUrl(asset_image_url, server_asset_images_absolute_path, device_id, server_asset_images_url);
                        if (final_asset_image_url != null) {
                            array.add(final_asset_image_url);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (array.size() > 0) {
                deviceRepository.updateAssetImage(device_id, array.toJSONString());
            }
            if (deviceDTO.getLocation_id() != null) {
                locationDTO = locationService.getLocationByLocationId(deviceDTO.getLocation_id());
            }
            if (action.equals("ADD")) {
                userActionLogService.addUserAction(username, "asset", action, "Asset images are added for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_image", deviceDTO.getId());
            } else if (action.equals("UPDATE")) {
                userActionLogService.addUserAction(username, "asset", action, "Asset images are updated for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_image", deviceDTO.getId());
            }
        } catch (RuntimeException e) {
            if (action.equals("ADD")) {
                userActionLogService.addUserAction(username, "asset", action, "Unable to add Asset images for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_image", deviceDTO.getId());
            } else if (action.equals("UPDATE")) {
                userActionLogService.addUserAction(username, "asset", action, "Unable to update Asset images for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_image", deviceDTO.getId());
            }

        }
    }

    public void upsertOnboardAssets(String username, String vdmsid, JSONObject onboard_asset_data) {
        try {
            Set<String> device_ids = utils.getJSONArrayFromJSONStringForSet(onboard_asset_data.getJSONArray("device_ids").toJSONString(), String.class);
            DeviceOnboardStatusDTO deviceOnboardStatusDTO = utils.getJSONObjectFromString(onboard_asset_data.getJSONObject("onboard_data").toJSONString(), DeviceOnboardStatusDTO.class);
            for (String device_id : device_ids) {
                this.upsertOnboardAsset(username, vdmsid, deviceOnboardStatusDTO, device_id, onboard_asset_data.getString("onboard_type"));
            }
        } catch (JsonProcessingException e) {
            System.out.println("Unable to upsert onboard assets");
        }

    }

    private void upsertOnboardAsset(String username, String vdms_id, DeviceOnboardStatusDTO
            deviceOnboardStatusDTO, String device_id, String onboard_type) {
        log.info("Upsert onboard asset for deviceOnboardStatusDTO:{} device id: {}  and onboard_type:{}", deviceOnboardStatusDTO, device_id, onboard_type);

        if (onboard_type != null && onboard_type.equals("automatic")) {
            log.info("Process is automatic..");
            DeviceDTO deviceDTO = this.getDeviceByDeviceId(null, vdms_id, null, device_id);
            log.info("Device dto:{}", deviceDTO);

            if (deviceDTO.getAsset_image_url() == null || deviceDTO.getAsset_image_url().equals("[]")) {
                deviceOnboardStatusDTO.setImage_status(0);
            } else {
                deviceOnboardStatusDTO.setImage_status(1);
            }
            if (deviceDTO.getPosition() == null || deviceDTO.getLatitude() == null || deviceDTO.getLongitude() == null) {
                deviceOnboardStatusDTO.setGeolocation_status(0);
            } else {
                deviceOnboardStatusDTO.setGeolocation_status(1);
            }
            if (deviceDTO.getQrcode_count() == 0) {
                deviceOnboardStatusDTO.setTag_status(0);
            } else {
                deviceOnboardStatusDTO.setTag_status(1);
            }
            if (((deviceDTO.getName() == null || deviceDTO.getName().isBlank()) &&
                    (deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().isBlank())) ||
                    ((deviceDTO.getVendor() == null || deviceDTO.getVendor().isBlank()) &&
                            (deviceDTO.getUser_data_vendor() == null || deviceDTO.getUser_data_vendor().isBlank())) ||
                    ((deviceDTO.getModel() == null || deviceDTO.getModel().isBlank()) &&
                            (deviceDTO.getUser_data_model() == null && deviceDTO.getUser_data_model().isBlank()))) {
                deviceOnboardStatusDTO.setField_status(0);
            } else {
                deviceOnboardStatusDTO.setField_status(1);
            }
        }

        deviceOnboardStatusDTO.setDevice_id(device_id);
        String onboardAssetId = this.getOnboardAssetIdByDeviceId(device_id);

        log.info("Got  onboard asset id:{} ", onboardAssetId);


        if (onboardAssetId != null) {
            deviceOnboardStatusDTO.setId(onboardAssetId);
            this.updateOnboardAsset(deviceOnboardStatusDTO, username);
        } else {
            this.addOnboardAsset(deviceOnboardStatusDTO, username);
        }


    }

    private void addOnboardAsset(DeviceOnboardStatusDTO deviceOnboardStatusDTO, String username) {
        this.updateOnboardAssetHistoryDetails(username, deviceOnboardStatusDTO.getDevice_id(), "asset_onboard", "todo", "");
        deviceOnboardStatusDTO.setId(Generators.timeBasedGenerator().generate().toString());

        log.info("Added history addOnboardAsset.. deviceOnboardStatusDTO:{}", deviceOnboardStatusDTO);

        deviceOnboardStatusRepository.addOnboardAsset(deviceOnboardStatusDTO.getId(), deviceOnboardStatusDTO.getDevice_id(), deviceOnboardStatusDTO.getAssignee_email(),
                deviceOnboardStatusDTO.getImage_status(), deviceOnboardStatusDTO.getGeolocation_status(), deviceOnboardStatusDTO.getTag_status(), deviceOnboardStatusDTO.getField_status());

        this.updateVirtualDeviceHistory(deviceOnboardStatusDTO, deviceOnboardStatusDTO.getDevice_id(), username);

        if ((deviceOnboardStatusDTO.getImage_status() == 1 || deviceOnboardStatusDTO.getImage_status() == 3) &&
                (deviceOnboardStatusDTO.getField_status() == 1 || deviceOnboardStatusDTO.getField_status() == 3) &&
                (deviceOnboardStatusDTO.getTag_status() == 1 || deviceOnboardStatusDTO.getTag_status() == 3) &&
                (deviceOnboardStatusDTO.getGeolocation_status() == 1 || deviceOnboardStatusDTO.getGeolocation_status() == 3)) {
            this.updateOnboardAssetStatus(Collections.singleton(deviceOnboardStatusDTO.getDevice_id()), 2);
            this.updateOnboardAssetHistoryDetails(username, deviceOnboardStatusDTO.getDevice_id(), "asset_onboard", "completed", "");
            log.info("Added history all true ");
            DeviceDTO deviceDTO = deviceRepository.getDeviceByDeviceId(deviceOnboardStatusDTO.getDevice_id());
            if (deviceDTO != null) {
                String device_name = deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().equals("") ? deviceDTO.getDisplay_name() : deviceDTO.getUser_data_name();
                userActionLogService.addUserAction(username, "asset", "ADD", "Asset onboard completed for Device name: " + device_name + " and id: " + deviceDTO.getId(), "success", "onboarded", deviceDTO.getId());
            }
        } else {
            this.updateOnboardAssetStatus(Collections.singleton(deviceOnboardStatusDTO.getDevice_id()), 1);
        }

        deviceOnboardStatusAssigneeService.updateDeviceOnboardStautsAssignee(deviceOnboardStatusDTO);
    }

    private void updateOnboardAsset(DeviceOnboardStatusDTO deviceOnboardStatusDTO, String username) {
        log.info("Updating onboard asset for deviceOnboardStatusDTO:{}", deviceOnboardStatusDTO);

        deviceOnboardStatusRepository.updateOnboardAsset(deviceOnboardStatusDTO.getId(), deviceOnboardStatusDTO.getDevice_id(), deviceOnboardStatusDTO.getAssignee_email(),
                deviceOnboardStatusDTO.getImage_status(), deviceOnboardStatusDTO.getGeolocation_status(), deviceOnboardStatusDTO.getTag_status(), deviceOnboardStatusDTO.getField_status());

        DeviceDTO deviceDTO = deviceRepository.getDeviceByDeviceId(deviceOnboardStatusDTO.getDevice_id());
        LocationDTO locationDTO = null;
        log.info("After Updating onboard asset for deviceDTO:{}", deviceDTO);

        if (deviceDTO.getOnboard_status() != 1 && deviceDTO.getOnboard_status() != 2) {

            this.updateOnboardAssetHistoryDetails(username, deviceOnboardStatusDTO.getDevice_id(), "asset_onboard", "todo", "");

            log.info("Added history...");

            this.updateVirtualDeviceHistory(deviceOnboardStatusDTO, deviceOnboardStatusDTO.getDevice_id(), username);

            DeviceOnboardStatusDTO deviceOnboardStatusDTO1 = deviceOnboardStatusRepository.getOnboardDataByDeviceId(deviceOnboardStatusDTO.getDevice_id());

            log.info("After adding onboard sttatus: {} ", deviceOnboardStatusDTO1);

            if ((deviceOnboardStatusDTO1.getImage_status() == 1 || deviceOnboardStatusDTO1.getImage_status() == 3) && (deviceOnboardStatusDTO1.getField_status() == 1 || deviceOnboardStatusDTO1.getField_status() == 3) && (deviceOnboardStatusDTO1.getTag_status() == 1 || deviceOnboardStatusDTO1.getTag_status() == 3) && (deviceOnboardStatusDTO1.getGeolocation_status() == 1 || deviceOnboardStatusDTO1.getGeolocation_status() == 3)) {
                this.updateOnboardAssetStatus(Collections.singleton(deviceOnboardStatusDTO.getDevice_id()), 2);
                this.updateOnboardAssetHistoryDetails(username, deviceOnboardStatusDTO.getDevice_id(), "asset_onboard", "completed", "");
                log.info("Added history details all true");

                if (deviceDTO != null) {
                    if (deviceDTO.getLocation_id() != null) {
                        locationDTO = locationService.getLocationByLocationId(deviceDTO.getLocation_id());
                    }
                    String device_name = deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().equals("") ? deviceDTO.getDisplay_name() : deviceDTO.getUser_data_name();
                    userActionLogService.addUserAction(username, "asset", "ADD", "Asset onboard completed for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "onboarded", deviceDTO.getId());
                }
            } else {
                log.info("All are not true..");
                this.updateOnboardAssetStatus(Collections.singleton(deviceOnboardStatusDTO.getDevice_id()), 1);
            }
        }

        deviceOnboardStatusAssigneeService.updateDeviceOnboardStautsAssignee(deviceOnboardStatusDTO);
    }


    private String getOnboardAssetIdByDeviceId(String device_id) {
        return deviceOnboardStatusRepository.getOnboardAssetIdByDeviceId(device_id);
    }

    private void updateOnboardAssetStatus(Set<String> device_ids, Integer onboard_status) {
        BigInteger updated_timestamp = BigInteger.valueOf(System.currentTimeMillis());
        deviceRepository.updateOnboardAssetStatus(device_ids, onboard_status, updated_timestamp);
        log.info("Adding onboard asset status: device ids:{} onboard status :{} and timestamp:{}", device_ids, onboard_status, updated_timestamp);
    }


    public void updateAssetOnboardStatus(String username, String vdmsid, JSONObject onboard_asset_data) {
        Set<String> device_ids = utils.getJSONArrayFromJSONStringForSet(onboard_asset_data.getJSONArray("device_ids").toJSONString(), String.class);
        this.updateOnboardAssetStatus(device_ids, onboard_asset_data.getInteger("onboard_status"));
        log.info("Updated onboard status...");
        for (String device_id : device_ids) {
            this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard", "verified", "");
            log.info("Successfully asset verified for device id :{} ", device_id);
        }

    }

    /**
     * @param deviceOnboardStatusDTO Image Status -> 0 - Not added
     *                               1 - Added
     *                               2 - Re-tag
     *                               3 - Not added & Exception
     *                               <p>
     *                               Field Status -> 0 - Not added
     *                               1 - Added
     *                               2 - Re-add
     *                               3 - Not added & Exception
     *                               <p>
     *                               Geolocation Status -> 0 - Not added
     *                               1 - Added
     *                               2 - Re-tag (deleted or none)
     *                               3 - Not added & Exception
     *                               <p>
     *                               Tag Status ->   0 - Not added
     *                               1 - Added
     *                               2 - Re-tag
     *                               3 - Not added & Exception
     * @param onboard_status
     */

    public void updateAssetOnboardData(String username, String vdmsid, String device_id, DeviceOnboardStatusDTO
            deviceOnboardStatusDTO, Integer onboard_status) {
        DeviceOnboardStatusDTO existingDeviceOnboardStatus = deviceOnboardStatusRepository.getOnboardDataByDeviceId(device_id);

        log.info("existingDeviceOnboardStatus : {}", existingDeviceOnboardStatus);

        if (onboard_status == null) {
            onboard_status = existingDeviceOnboardStatus.getOnboard_status();
            log.info("Onboard status :{}", onboard_status);
        }

        deviceOnboardStatusRepository.updateAssetOnboardData(device_id, deviceOnboardStatusDTO.getImage_status(), deviceOnboardStatusDTO.getGeolocation_status(),
                deviceOnboardStatusDTO.getTag_status(), deviceOnboardStatusDTO.getField_status());

        DeviceOnboardStatusDTO updatedDeviceOnboardStatus = deviceOnboardStatusRepository.getOnboardDataByDeviceId(device_id);

        log.info("updatedDeviceOnboardStatus : {}", updatedDeviceOnboardStatus);

        if (existingDeviceOnboardStatus != null && !Objects.equals(existingDeviceOnboardStatus.getImage_status(), updatedDeviceOnboardStatus.getImage_status())) {
            if (updatedDeviceOnboardStatus.getImage_status() == 0) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_image", "todo", "");
            } else if (updatedDeviceOnboardStatus.getImage_status() == 1) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_image", "completed", "");
            } else if (updatedDeviceOnboardStatus.getImage_status() == 2) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_image", "retag", deviceOnboardStatusDTO.getImage_comment());
            } else if (updatedDeviceOnboardStatus.getImage_status() == 3) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_image", "exception", deviceOnboardStatusDTO.getImage_comment());
            }
            log.info("Added history for image status:{}", updatedDeviceOnboardStatus.getImage_status());

        }

        if (existingDeviceOnboardStatus != null && !Objects.equals(existingDeviceOnboardStatus.getField_status(), updatedDeviceOnboardStatus.getField_status())) {
            if (updatedDeviceOnboardStatus.getField_status() == 0) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_field", "todo", "");
            } else if (updatedDeviceOnboardStatus.getField_status() == 1) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_field", "completed", "");
            } else if (updatedDeviceOnboardStatus.getField_status() == 2) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_field", "retag", deviceOnboardStatusDTO.getField_comment());
            } else if (updatedDeviceOnboardStatus.getField_status() == 3) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_field", "exception", deviceOnboardStatusDTO.getField_comment());
            }
            log.info("Added history for field status:{}", updatedDeviceOnboardStatus.getField_status());

        }

        if (existingDeviceOnboardStatus != null && !Objects.equals(existingDeviceOnboardStatus.getTag_status(), updatedDeviceOnboardStatus.getTag_status())) {
            if (updatedDeviceOnboardStatus.getTag_status() == 0) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_tag", "todo", "");
            } else if (updatedDeviceOnboardStatus.getTag_status() == 1) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_tag", "completed", "");
            } else if (updatedDeviceOnboardStatus.getTag_status() == 2) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_tag", "retag", deviceOnboardStatusDTO.getTag_comment());
            } else if (updatedDeviceOnboardStatus.getTag_status() == 3) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_tag", "exception", deviceOnboardStatusDTO.getTag_comment());
            }
            log.info("Added history for tag status:{}", updatedDeviceOnboardStatus.getTag_status());

        }

        if (existingDeviceOnboardStatus != null && !Objects.equals(existingDeviceOnboardStatus.getGeolocation_status(), updatedDeviceOnboardStatus.getGeolocation_status())) {
            if (updatedDeviceOnboardStatus.getGeolocation_status() == 0) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_geolocation", "todo", "");
            } else if (updatedDeviceOnboardStatus.getGeolocation_status() == 1) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_geolocation", "completed", "");
            } else if (updatedDeviceOnboardStatus.getGeolocation_status() == 2) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_geolocation", "retag", deviceOnboardStatusDTO.getGeolocation_comment());
            } else if (updatedDeviceOnboardStatus.getGeolocation_status() == 3) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_geolocation", "exception", deviceOnboardStatusDTO.getGeolocation_comment());
            }
            log.info("Added history for geolocation status:{}", updatedDeviceOnboardStatus.getGeolocation_status());

        }

        if (updatedDeviceOnboardStatus != null) {
            BigInteger updated_timestamp = BigInteger.valueOf(System.currentTimeMillis());
            if ((updatedDeviceOnboardStatus.getField_status() == 1 || updatedDeviceOnboardStatus.getField_status() == 3) && (updatedDeviceOnboardStatus.getTag_status() == 1 || updatedDeviceOnboardStatus.getTag_status() == 3) && (updatedDeviceOnboardStatus.getImage_status() == 1 || updatedDeviceOnboardStatus.getImage_status() == 3) && (updatedDeviceOnboardStatus.getGeolocation_status() == 1 || updatedDeviceOnboardStatus.getGeolocation_status() == 3)) {
                log.info("All status true... ");
                if (onboard_status == null || onboard_status == 1) {
                    LocationDTO locationDTO = null;
                    deviceRepository.updateOnboardAssetStatus(Collections.singleton(device_id), 2, updated_timestamp);
                    log.info("Moved from pending/not onboarded to completed.");
                    this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard", "completed", "");
                    DeviceDTO deviceDTO = deviceRepository.getDeviceByDeviceId(device_id);
                    System.out.println("device dto : " + deviceDTO);
                    if (deviceDTO.getLocation_id() != null) {
                        locationDTO = locationService.getLocationByLocationId(deviceDTO.getLocation_id());
                    }
                    if (deviceDTO != null) {
                        String device_name = deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().equals("") ? deviceDTO.getDisplay_name() : deviceDTO.getUser_data_name();
                        userActionLogService.addUserAction(username, "asset", "ADD", "Asset onboard completed for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "onboarded", deviceDTO.getId());
                    }
                } else if (onboard_status == 0) {
                    log.info("Moved from not onboarded to completed.");
                    deviceRepository.updateOnboardAssetStatus(Collections.singleton(device_id), 3, updated_timestamp);
                }
            } else {
                if ((onboard_status == null || onboard_status == 2)) {
                    log.info("Redo...Asset onboard");
                    deviceRepository.updateOnboardAssetStatus(Collections.singleton(device_id), 1, updated_timestamp);
                    this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard", "todo", "");
                } else if (onboard_status == 3) {
                    log.info("Update same status...Manage Assets:{}", onboard_status);
                    deviceRepository.updateOnboardAssetStatus(Collections.singleton(device_id), 0, updated_timestamp);
                }
            }
        }

    }

    private void updateOnboardAssetHistoryDetails(String username, String device_id, String sub_type, String
            status, String alert_message) {
        HistoryDTO history = new HistoryDTO();
        history.setCreated_email(username);
        history.setType(18);
        history.setStatus(status);
        history.setDevice_id(device_id);
        history.setSub_type(sub_type);
        history.setAlert_message(alert_message);
        historyService.addHistory(history);

        log.info("Added onboard history:{}", history);

    }

//    public Map<String, Integer> getAssetOnboardCount(String username, String vdmsid, String dockername) {
//        Map<String, Integer> deviceStatuscountAll = new HashMap<>();
//
//        Integer todoAssetOnboardCount = deviceRepository.getAssetOnboardCount(dockername, 1);
//        Integer completedAssetOnboardCount = deviceRepository.getAssetOnboardCount(dockername, 2);
//
//        deviceStatuscountAll.put("todo_asset_onboard_count", todoAssetOnboardCount);
//        deviceStatuscountAll.put("completed_asset_onboard_count", completedAssetOnboardCount);
//
//        return deviceStatuscountAll;
//    }

    public Map<String, Integer> getAssetOnboardCount(String username, String vdmsid, String dockername, JSONObject search_sort_filter_details) {
        Map<String, Integer> deviceStatuscountAll = new HashMap<>();

        Integer todoAssetOnboardCount = Integer.parseInt(deviceSearchService.multipleKeywordSearchSortFilterDevicesCount(username, vdmsid, dockername, "onboardpending", search_sort_filter_details, 1));
        Integer completedAssetOnboardCount = Integer.valueOf(deviceSearchService.multipleKeywordSearchSortFilterDevicesCount(username, vdmsid, dockername, "onboardcompleted", search_sort_filter_details, 2));

        deviceStatuscountAll.put("todo_asset_onboard_count", todoAssetOnboardCount);
        deviceStatuscountAll.put("completed_asset_onboard_count", completedAssetOnboardCount);

        return deviceStatuscountAll;
    }

    public void upsertAssetOcrImages(String username, String vdms_id, List<String> device_ids, List<MultipartFile> asset_ocr_images, HttpServletRequest httpServletRequest) {
        log.info("upsertAssetOcrImages, Params: device ids: {}, asset ocr images: {}, endpoint : {}", device_ids, asset_ocr_images, httpServletRequest.getRequestURI());
        for (String device_id : device_ids) {
            JSONArray array = new JSONArray();
            String action = "ADD";
            LocationDTO locationDTO = null;
            DeviceDTO deviceDTO = deviceRepository.getDeviceByDeviceId(device_id);
            String device_name = deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().equals("") ? deviceDTO.getDisplay_name() : deviceDTO.getUser_data_name();
            try {
                if (deviceDTO.getAsset_ocr_image_url() != null) {
                    array = JSON.parseArray(deviceDTO.getAsset_ocr_image_url());
                    action = "UPDATE";
                }
                if (device_ids.size() >= 1) {
                    for (int i = 0; i < array.size(); i++) {
                        utils.removeFileFromServerByImageURL(array.getString(i), server_asset_ocr_images_absolute_path);
                        array.clear();
                    }
                }

                for (MultipartFile asset_ocr_image : asset_ocr_images) {
                    try {
                        if (asset_ocr_image.getSize() > 0) {
                            //get file path
                            String asset_ocr_image_url = utils.addFileToServer(asset_ocr_image.getBytes(), server_asset_ocr_images_absolute_path, device_id, utils.getFileExtensionByFileUrl(asset_ocr_image.getOriginalFilename()), server_asset_ocr_images_url);
                            if (asset_ocr_image_url != null) {
                                array.add(asset_ocr_image_url);
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (array.size() > 0) {
                    deviceRepository.updateAssetOcrImage(device_id, array.toJSONString());
                }
                if (deviceDTO.getLocation_id() != null) {
                    locationDTO = locationService.getLocationByLocationId(deviceDTO.getLocation_id());
                }
                if (action.equals("ADD")) {
                    log.info("Asset OCR images are added for Device name: {}", device_name);
                    userActionLogService.addUserAction(username, "asset", action, "Asset OCR images are added for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_ocr_image", deviceDTO.getId());
                } else if (action.equals("UPDATE")) {
                    log.info("Asset OCR images are updated for Device name: {}", device_name);
                    userActionLogService.addUserAction(username, "asset", action, "Asset OCR images are updated for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_ocr_image", deviceDTO.getId());
                }
            } catch (RuntimeException e) {
                if (action.equals("ADD")) {
                    log.error("Exception.  Params: device ids: {}, asset ocr images: {}, endpoint : {}", device_ids, asset_ocr_images, httpServletRequest.getRequestURI(), e);
                    userActionLogService.addUserAction(username, "asset", action, "Unable to add Asset OCR images for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_ocr_image", deviceDTO.getId());
                } else if (action.equals("UPDATE")) {
                    log.error("Exception.  Params: device ids: {}, asset ocr images: {}, endpoint : {}", device_ids, asset_ocr_images, httpServletRequest.getRequestURI(), e);
                    userActionLogService.addUserAction(username, "asset", action, "Unable to update Asset OCR images for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_ocr_image", deviceDTO.getId());
                }

            }
        }
    }

    public void deleteAssetOcrImages(String username, String vdms_id, List<DeviceDTO> deviceDTOS, HttpServletRequest httpServletRequest) {
        log.info("deleteAssetOcrImages, Params: deviceDTOs: {}, endpoint : {}", deviceDTOS, httpServletRequest.getRequestURI());
        for (DeviceDTO device : deviceDTOS) {
            DeviceDTO deviceDTO = deviceRepository.getDeviceByDeviceId(device.getId());
            String device_name = deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().equals("") ? deviceDTO.getDisplay_name() : deviceDTO.getUser_data_name();

            JSONArray image_array = JSON.parseArray(deviceDTO.getAsset_ocr_image_url());
            JSONArray asset_ocr_image_array = JSON.parseArray(device.getAsset_ocr_image_url());
            JSONArray update_array = new JSONArray();
            LocationDTO locationDTO = null;
            try {
                for (int i = 0; i < asset_ocr_image_array.size(); i++) {
                    utils.removeFileFromServerByImageURL(asset_ocr_image_array.getString(i), server_asset_ocr_images_absolute_path);
                    for (int j = 0; j < image_array.size(); j++) {
                        if (!image_array.getString(j).equals(asset_ocr_image_array.getString(i))) {
                            update_array.add(image_array.getString(j));
                        }
                    }
                }
                if (deviceDTO.getLocation_id() != null) {
                    locationDTO = locationService.getLocationByLocationId(deviceDTO.getLocation_id());
                }
                deviceRepository.updateAssetOcrImage(device.getId(), update_array.toJSONString());
                log.info("Asset OCR images are removed from Device name: {}", device_name);
                userActionLogService.addUserAction(username, "asset", "DELETE", "Asset OCR images are removed from Device name: " + device_name + " and id: " + device.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_ocr_image", device.getId());
            } catch (Exception e) {
                log.error("Exception. Params: deviceDTOs: {}, endpoint : {}", deviceDTOS, httpServletRequest.getRequestURI(), e);
                userActionLogService.addUserAction(username, "asset", "DELETE", "Unable to remove asset OCR images from Device name: " + device_name + " and id: " + device.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_ocr_image", device.getId());

            }
        }
    }

    public String getAssetOcrImageUrls(String username, String vdms_id, String device_id) {
        return deviceRepository.getAssetOcrImageUrls(device_id);
    }

    private void removeAssetOcrImages(DeviceDTO deviceDTO) {
        if (deviceDTO.getAsset_ocr_image_url() != null) {
            JSONArray asset_ocr_image_array = JSON.parseArray(deviceDTO.getAsset_ocr_image_url());
            for (int i = 0; i < asset_ocr_image_array.size(); i++) {
                utils.removeFileFromServerByImageURL(asset_ocr_image_array.getString(i), server_asset_ocr_images_absolute_path);
            }
        }
    }

    public Set<String> getAssetOnboardAssignees(String username, String vdms_id) {
        Set<String> assignees = new HashSet<>();
        Set<String> primary_assignees = deviceOnboardStatusRepository.getAssetOnboardAssignees();
        Set<String> device_onboard_status_assignees = deviceOnboardStatusAssigneeService.getDeviceOnboardStatusAssigneesEmail();
        assignees.addAll(primary_assignees);
        assignees.addAll(device_onboard_status_assignees);
        List<String> sortedAssigneesList = new ArrayList<>(assignees);
        Collections.sort(sortedAssigneesList);
        return new LinkedHashSet<>(sortedAssigneesList);
    }

    public DeviceDTO addDevice(String username, String vdmsid, String dockername, DeviceDTO deviceDto, HttpServletRequest httpServletRequest) {
        log.info("addDevice, Params: deviceDto: {}, endpoint : {}", deviceDto, httpServletRequest.getRequestURI());
        if (deviceDto.getId() == null) {
            String device_id = vdmsid + "_" + dockername + "_" + deviceDto.getUser_data_name() + "_"
                    + System.nanoTime();
            deviceDto.setId(utils.replaceSpecialCharactersWithUnderscore(device_id));
            deviceDto.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
            deviceDto.setOnboard_status(1);
            deviceDto.setMonitor(1);
            deviceDto.setVirtual_device_type(2);
            try {
                deviceRepository.addDevice(deviceDto.getId(), vdmsid,
                        dockername, deviceDto.getIp_address(), deviceDto.getStatus(),
                        deviceDto.getMac_address(), deviceDto.getLast_seen_on(),
                        deviceDto.getDisplay_name(), deviceDto.getVendor(), deviceDto.getCreated_timestamp(),
                        deviceDto.getUser_data_name(), deviceDto.getType(), deviceDto.getDescription(),
                        deviceDto.getCustom_fields(), username, deviceDto.getAsset_group(), deviceDto.getOnboard_status(), deviceDto.getMonitor(), deviceDto.getVirtual_device_type());
                log.info("Added device id : {}, device name : {}", device_id, deviceDto.getUser_data_name());
                userActionLogService.addUserAction(username, "asset", "ADD", "A Device name: " + deviceDto.getUser_data_name() + " and id: " + deviceDto.getId() + " is added for network " + dockername, "success", "asset_info", deviceDto.getId());

                DeviceOnboardStatusDTO deviceOnboardStatusDTO = new DeviceOnboardStatusDTO(deviceDto.getId(), deviceDto.getOnboard_data().getAssignee_email(), 0, 0, 0, 0);
                deviceOnboardStatusDTO.setDevice_onboard_status_assignees(deviceDto.getOnboard_data().getDevice_onboard_status_assignees());
                this.addOnboardAsset(deviceOnboardStatusDTO, username);
            } catch (Exception e) {
                log.error("Exception.  Params: deviceDto: {}, endpoint : {}", deviceDto, httpServletRequest.getRequestURI(), e);
                userActionLogService.addUserAction(username, "asset", "ADD", "Unable to Add Device name: " + deviceDto.getUser_data_name() + " and id: " + deviceDto.getId() + " for network " + dockername, "failed", "asset_info", deviceDto.getId());
            }

        }
        return this.getDeviceByDeviceId(username, vdmsid, dockername, deviceDto.getId());
    }

//    public void exportFilteredDevices(HttpServletResponse response, String username, String vdmsid, String
//            dockername, String condition, JSONObject searchSortFilterDetails, Integer onboardStatus, String template_name) {
//        Set<DeviceDTO> devices = deviceSearchService.multipleKeywordSearchSortFilterDevicesForAssetExport(username, vdmsid, dockername, condition, searchSortFilterDetails, onboardStatus);
//        try {
//            if (template_name.equals("simple_report")) {
//                this.generateExcel(devices, response);
//            } else if (template_name.equals("measuring_instrument_report")) {
//                this.generateExcelForMeasuringInstruments(username, vdmsid, devices, response);
//            }
//        } catch (IOException e) {
//            System.out.println(e);
//        }
//
//    }

    public void exportFilteredDevices(HttpServletResponse response, String username, String vdmsid, String
                                              dockername, String condition, JSONObject searchSortFilterDetails, Integer onboardStatus, String template_name,
                                      String email, HttpServletRequest httpServletRequest, String file_type) {
        log.info("exportFilteredDevices, Params: condition: {}, searchSortFilterDetails: {}, onboardStatus: {}, template name: {}, email: {}, endpoint : {}", condition, searchSortFilterDetails, onboardStatus, template_name, email, httpServletRequest.getRequestURI());

        try {
            VdmsDTO vdmsDetails = vdmsService.getVDMSDetails();
            String timeZoneId = vdmsDetails.getTimezone();
            String propertyName = vdmsDetails.getProperty_name();
            String modified_filename = utils.replaceSlashCharactersWithHyphen(propertyName);
            String currentDateTime = utils.getCurrentDateByTimezone(BigInteger.valueOf(System.currentTimeMillis()), timeZoneId);
            Set<DeviceDTO> devices = deviceSearchService.multipleKeywordSearchSortFilterDevicesForAssetExport(username, vdmsid, dockername, condition, searchSortFilterDetails, onboardStatus);
            log.info("Devices Count: {}", devices.size());

            Boolean includeImages = false;

            if (searchSortFilterDetails != null && searchSortFilterDetails.containsKey("include_images")) {
                includeImages = searchSortFilterDetails.getBoolean("include_images");
                if (includeImages == null) {
                    includeImages = false;  // fallback if explicitly null
                }
            }

            if (template_name.equals("simple_report")) {
                this.simpleAssetExportReport(response, file_type, devices, includeImages, email, modified_filename, currentDateTime, vdmsid);
            } else if (template_name.equals("measuring_instrument_report")) {
                if (email.isEmpty()) {
                    this.generateExcelForMeasuringInstruments(username, vdmsid, devices, response);
                } else {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        try {

                            for (DeviceDTO device : devices) {
                                device.setMeasuringInstruments(measuringInstrumentService.getInstrumentsByDeviceId(username, vdmsid, device.getId()));
                            }
                            System.out.println("-----------------------------------------");
                            log.info("Get is complete");
                            byte[] bytes = this.generateExcelForMeasuringInstrumentsEmail(username, vdmsid, devices);
                            this.sendAssetExportEmail(email, "Advanced Asset Export", modified_filename + "_" + "AssetListAdvancedExport" + "_" + currentDateTime, bytes, vdmsid);
                            System.out.println("-------------------------------------------------------------------------");
                            log.info("Sent Advance Report to: {}" + email);
                            System.out.println("-------------------------------------------------------------------------");

                        } catch (Exception e) {
                            System.out.println(e);
                            log.error("Exception.  Params: condition: {}, searchSortFilterDetails: {}, onboardStatus: {}, template name: {}, email: {}, endpoint : {}", condition, searchSortFilterDetails, onboardStatus, template_name, email, httpServletRequest.getRequestURI(), e);

                        }
                    });
                    executorService.shutdown();
                }
            }
        } catch (IOException e) {
            System.out.println(e);
            log.error("Exception.  Params: condition: {}, searchSortFilterDetails: {}, onboardStatus: {}, template name: {}, email: {}, endpoint : {}", condition, searchSortFilterDetails, onboardStatus, template_name, email, httpServletRequest.getRequestURI(), e);

        }

    }

    public void sendAssetExportEmail(String email, String reportName, String filename, byte[] bytes, String vdmsid) {
        JSONObject body = new JSONObject();
        body.put("template_type", "download_email");
        body.put("email", email);
        body.put("subject", reportName + " Download link!!");
        body.put("report_name", reportName);
        body.put("file_name", filename);
        MultipartFile file = new ConvertByteArrayToMultipartFile(bytes, filename + ".xlsx", filename, null);
        alertService.sendDownloadEmail(body, file, "excel", vdmsid);
    }


    public byte[] generateSimpleAssetExportReportExcel(Set<DeviceDTO> devices, Boolean includeImages, HttpServletResponse response, String email, String modified_filename, String currentDateTime) throws IOException {
        VdmsDTO vdmsDetails = vdmsService.getVDMSDetails();
        String timeZoneId = vdmsDetails.getTimezone();
        String propertyName = vdmsDetails.getProperty_name();
        ObjectMapper objectMapper = new ObjectMapper();
        String object = objectMapper.writeValueAsString(devices);

        JSONArray jsonArray = JSON.parseArray(object);
        Map<String, Integer> headerMap = new HashMap<>();

        Map<String, String> modifiedHeaderNames = new HashMap<>();


//        String[] headers = {
//                "ID", "Name", "Model", "Vendor", "Network", "Serial Number",
//                "Created Date", "Created By", "Description", "Location", "Asset Type", "QR Code Status", "Geo Location Status", "VDMS ID", "Asset Image 1", "Asset Image 2"
//        };
        // Header initialization based on includeImages flag
        List<String> headers = new ArrayList<>(List.of(
                "ID", "Name", "Model", "Vendor", "Network", "Serial Number",
                "Created Date", "Created By", "Description", "Location", "Asset Type",
                "QR Code Status", "Geo Location Status", "VDMS ID"
        ));

        if (includeImages) {
            headers.add("Asset Image 1");
            headers.add("Asset Image 2");
        } else {
            headers.add("Image Status");
        }

        int col = 0;
        for (String header : headers) {
            headerMap.put(header, col++);
        }
        List<Map<String, String>> excelDataList = new ArrayList<>();

        for (Object obj : jsonArray) {

            Map<String, String> excelMap = new HashMap<>();
            if (obj instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) obj;

                excelMap.put("ID", Objects.requireNonNullElse(jsonObject.getString("id"), ""));
                String userDataName = Objects.requireNonNullElse(jsonObject.getString("user_data_name"), "");
                excelMap.put("Name", !userDataName.equals("") ? userDataName : Objects.requireNonNullElse(jsonObject.getString("display_name"), ""));

                String userDataModel = Objects.requireNonNullElse(jsonObject.getString("user_data_model"), "");
                excelMap.put("Model", !userDataModel.equals("") ? userDataModel : Objects.requireNonNullElse(jsonObject.getString("model"), ""));

                String userDataVendor = Objects.requireNonNullElse(jsonObject.getString("user_data_vendor"), "");
                excelMap.put("Vendor", !userDataVendor.equals("") ? userDataVendor : Objects.requireNonNullElse(jsonObject.getString("vendor"), ""));


                excelMap.put("Network", Objects.requireNonNullElse(jsonObject.getString("docker_name"), ""));
                excelMap.put("Serial Number", Objects.requireNonNullElse(jsonObject.getString("serial_number"), ""));

                if (jsonObject.getString("created_timestamp") != null) {

                    String date = utils.getCurrentDateByTimezone(jsonObject.getBigInteger("created_timestamp"), timeZoneId);
                    excelMap.put("Created Date", date);
                }


                if (jsonObject.getString("created_email") != null) {
                    excelMap.put("Created By", Objects.requireNonNullElse(jsonObject.getString("created_email"), ""));
                }

                excelMap.put("Description", Objects.requireNonNullElse(jsonObject.getString("description"), ""));


                String locationId = jsonObject.getString("location_id");
                if (locationId != null) {
                    String loc = Objects.requireNonNullElse(jsonObject.getString("location"), "") + ", " +
                            Objects.requireNonNullElse(jsonObject.getString("floor"), "") + ", " +
                            Objects.requireNonNullElse(jsonObject.getString("building"), "");
                    excelMap.put("Location", loc);
                }

                excelMap.put("Asset Type", Objects.requireNonNullElse(jsonObject.getString("type"), ""));

                JSONObject onboardData = jsonObject.getJSONObject("onboard_data");

                if (onboardData != null && onboardData.getInteger("tag_status") != null) {
                    int tag_status = onboardData.getInteger("tag_status");
                    if (tag_status == 0) {
                        excelMap.put("QR Code Status", "Not Completed");
                    } else if (tag_status == 1) {
                        excelMap.put("QR Code Status", "Completed");
                    } else if (tag_status == 2) {
                        excelMap.put("QR Code Status", "Re-tag");
                    } else if (tag_status == 3) {
                        excelMap.put("QR Code Status", "Exception");
                    }
                }

                if (onboardData != null && onboardData.getInteger("geolocation_status") != null) {
                    int geolocation_status = onboardData.getInteger("geolocation_status");
                    if (geolocation_status == 0) {
                        excelMap.put("Geo Location Status", "Not Completed");
                    } else if (geolocation_status == 1) {
                        excelMap.put("Geo Location Status", "Completed");
                    } else if (geolocation_status == 2) {
                        excelMap.put("Geo Location Status", "Re-tag");
                    } else if (geolocation_status == 3) {
                        excelMap.put("Geo Location Status", "Exception");
                    }
                }

                excelMap.put("VDMS ID", Objects.requireNonNullElse(jsonObject.getString("vdms_id"), ""));

                // Added logic for conditional image columns
                if (includeImages) {
                    if (jsonObject.containsKey("asset_image_url")) {
                        String assetImageUrl = jsonObject.getString("asset_image_url");
                        JSONArray imageUrls = JSONArray.parseArray(assetImageUrl);

                        if (imageUrls.size() > 0) {
                            for (int i = 0; i < Math.min(imageUrls.size(), 2); i++) {
                                if (i == 0) {
                                    excelMap.put("Asset Image 1", imageUrls.getString(i));
                                } else if (i == 1) {
                                    excelMap.put("Asset Image 2", imageUrls.getString(i));
                                }
                            }
                        }
                    }
                } else {
                    if (onboardData != null && onboardData.getInteger("image_status") != null) {
                        int image_status = onboardData.getInteger("image_status");
                        if (image_status == 0) {
                            excelMap.put("Image Status", "Not Completed");
                        } else if (image_status == 1) {
                            excelMap.put("Image Status", "Completed");
                        } else if (image_status == 2) {
                            excelMap.put("Image Status", "Re-tag");
                        } else if (image_status == 3) {
                            excelMap.put("Image Status", "Exception");
                        }
                    }
                }

                if (jsonObject.containsKey("custom_fields")) {
                    JSONArray customFieldsArray = JSON.parseArray(jsonObject.getString("custom_fields"));


                    for (Object customFieldObj : customFieldsArray) {
                        if (customFieldObj instanceof JSONObject) {
                            JSONObject customField = (JSONObject) customFieldObj;

                            for (String key : customField.keySet()) {
                                String lowercaseKey = key.toLowerCase();
                                //removing all spaces
                                String modifiedKey = this.removeUnderscores(key).toLowerCase();

                                if (!headerMap.containsKey(lowercaseKey) && !modifiedHeaderNames.containsKey(modifiedKey)) {
                                    try {

                                        int columnIndex = headerMap.size();
                                        headerMap.put(modifiedKey, columnIndex);
                                        modifiedHeaderNames.put(modifiedKey, lowercaseKey);

                                    } catch (Exception e) {
                                        System.out.println(e);
                                    }

                                }
                                excelMap.put(modifiedKey, customField.getString(key));
                            }
                        }
                    }
                }
            }
            excelDataList.add(excelMap);
        }

        return this.generateSimpleAssetExportReportExcelData(excelDataList, headerMap, timeZoneId, propertyName, modifiedHeaderNames, response, email, modified_filename, currentDateTime);

    }

    public byte[] generateSimpleAssetExportReportExcelData(List<Map<String, String>> excelData, Map<String, Integer> headerMap, String timeZoneId, String
            propertyName, Map<String, String> modifiedHeaderNames, HttpServletResponse response, String email, String modified_filename, String currentDateTime) throws IOException {
        // Create a new workbook and sheet

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] bytes = null;


        String filename = modified_filename + "_" + "AssetList" + "_";

        System.out.println("------- File name --------" + filename);


        SXSSFWorkbook workbook = new SXSSFWorkbook();

        // needs changes
        SXSSFSheet sheet = workbook.createSheet(modified_filename);

        //Setting Header Bold
        CellStyle headerCellStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerCellStyle.setFont(headerFont);

        // Create a header row
        SXSSFRow headerRow = sheet.createRow(0);

        //Populate header row with column names
        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
            String modifiedName = removeUnderscores(entry.getKey());
            String actualKey = "";
            if (modifiedHeaderNames.get(modifiedName) != null) {
                actualKey = modifiedHeaderNames.get(modifiedName);
            } else {
                actualKey = entry.getKey();
            }
            String titleCaseKey = utils.convertSnakeCaseToTitleCase(actualKey);
            Cell headerCell = headerRow.createCell(entry.getValue());
            headerCell.setCellValue(titleCaseKey);

            // Set the font style to bold for the header cell
            headerCell.setCellStyle(headerCellStyle);
        }
        //Populate data rows
        int rowIndex = 1;

        for (Map<String, String> excelMap : excelData) {
            System.out.println("EXCEL MAP: " + excelMap);
            SXSSFRow dataRow = sheet.createRow(rowIndex++);

            for (Map.Entry<String, String> entry : excelMap.entrySet()) {
                System.out.println("Entry: " + entry);
                Integer colIndex = headerMap.get(entry.getKey());

                if (colIndex != null) {
                    Cell dataCell = dataRow.createCell(colIndex);

                    if (entry.getKey().contains("Asset Image")) {
                        System.out.println("Image add came");
                        try {
                            URL url = new URL(entry.getValue());
                            byte[] imageBytes = IOUtils.toByteArray(url.openStream());

                            // Set column width (approx. 100 pixels)
                            sheet.setColumnWidth(colIndex, 100 * 37); // 1 character = 37 pixels

                            // Set row height (100 pixels)
                            dataRow.setHeightInPoints(100 * (72f / 96f)); // 1 pixel = 0.75 points

                            // Add the image to the workbook
                            int pictureIdx = workbook.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);
                            CreationHelper helper = workbook.getCreationHelper();
                            Drawing<?> drawing = sheet.createDrawingPatriarch();

                            // Define the anchor for the image
                            ClientAnchor anchor = helper.createClientAnchor();
                            anchor.setCol1(colIndex); // Column index
                            anchor.setRow1(rowIndex - 1); // Row index
                            anchor.setCol2(colIndex + 1); // Span 1 column (optional)
                            anchor.setRow2(rowIndex); // Span 1 row (optional)

                            // Create the picture
                            Picture picture = drawing.createPicture(anchor, pictureIdx);
                            picture.resize(1.0, 1.0);
                            System.out.println("Image Addded Successfully");
                        } catch (IOException e) {
                            System.err.println("Failed to load image from URL: " + entry.getValue());
                            e.printStackTrace();
                        }
                    } else {
                        dataCell.setCellValue(entry.getValue());
                    }
                }
            }
        }

        if (email.isEmpty()) {
            response.setContentType("application/octet-stream");
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=" + filename + currentDateTime + ".xlsx";
            System.out.println("--------- header value -------" + headerValue);
            response.setHeader(headerKey, headerValue);

            ServletOutputStream outputStream = response.getOutputStream();
            workbook.write(outputStream);
            outputStream.close();
        } else {
            workbook.write(stream);
            bytes = stream.toByteArray();
            stream.close();
        }
        workbook.close();

        return bytes;

    }

    public byte[] generateExcelForMeasuringInstrumentsEmail(String username, String vdmsid, Set<DeviceDTO> devices) {
        String deviceId = "";
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            VdmsDTO vdmsDetails = vdmsService.getVDMSDetails();
            String timeZoneId = vdmsDetails.getTimezone();
            String propertyName = vdmsDetails.getProperty_name();

            // Prepare file name with current timestamp
            String modified_filename = utils.replaceSlashCharactersWithHyphen(propertyName);
            String filename = modified_filename + "_AssetListAdvancedExport_";
            String currentDateTime = utils.getCurrentDateByTimezone(BigInteger.valueOf(System.currentTimeMillis()), timeZoneId);
            String headerValue = "attachment; filename=" + filename + currentDateTime + ".xlsx";
            System.out.println("------- File name --------" + headerValue);

            // Create workbook and sheet
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Asset Data Advanced Export");

            // Define styles
            CellStyle headingStyle = createHeadingStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle assetStyle = createDataStyle(workbook);

            // Create header rows
            createHeaderRows(sheet, headingStyle);

            // Auto-size columns 0-12 (asset columns)
            for (int j = 0; j <= 12; j++) {
                sheet.autoSizeColumn(j);
            }

            // Merge header cells for main sections
            mergeMainHeaders(sheet);

            // Current row index for data (starts after 2 header rows)
            int currentRowIndex = 2;

            // Process each device
            for (DeviceDTO device : devices) {
                    deviceId = device.getId();

                    // Fetch related data for this device
                    List<BacnetAdvanceExportExcelDTO> bacnetDataList = fetchBacnetData(username, vdmsid, deviceId);
                    List<SiemensAdvanceExportExcelDTO> siemensDataList = fetchSiemensData(username, vdmsid, deviceId);
                    List<ConditionsAdvanceExportExcelDto> conditions = fetchConditions(username, vdmsid, deviceId);
                    List<SiemensBmsExportDTO> bmsDataList = fetchBmsData(username, vdmsid, deviceId);

                    // Process device and get the new row index
                    currentRowIndex = processDevice(sheet, device, bacnetDataList, siemensDataList, conditions,bmsDataList,
                            currentRowIndex, dataStyle, assetStyle);

            }

            // Auto-size all columns
            for (int i = 0; i <= 57; i++) {
                sheet.autoSizeColumn(i);
            }

            // Set specific column widths for better readability
            sheet.setColumnWidth(0, 10000);
            sheet.setColumnWidth(1, 10000);
            sheet.setColumnWidth(2, 7000);
            sheet.setColumnWidth(3, 7000);
            sheet.setColumnWidth(4, 7000);
            sheet.setColumnWidth(5, 7000);
            sheet.setColumnWidth(6, 7000);
            sheet.setColumnWidth(7, 6000);
            sheet.setColumnWidth(8, 6000);
            sheet.setColumnWidth(9, 6000);
            sheet.setColumnWidth(10, 6000);
            sheet.setColumnWidth(11, 6000);
            sheet.setColumnWidth(12, 6000);

            // Write to stream and return bytes
            workbook.write(stream);
            byte[] bytes = stream.toByteArray();
            workbook.close();
            stream.close();

            System.out.println("Generated bytes for the exports");
            return bytes;

        } catch (Exception e) {
            System.out.println("Error generating Excel for Device Id: " + deviceId);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates the two header rows with proper column organization:
     * Row 0: Main section headers (Asset Info, Instruments, Attributes, Conditions,BMS)
     * Row 1: Sub-column headers for sections that need them
     */
    private void createHeaderRows(XSSFSheet sheet, CellStyle headingStyle) {
        // Row 0: Main section headers
        Row headerRow0 = sheet.createRow(0);
        headerRow0.setHeightInPoints(40);

        // Columns 0-12: Asset Information (no sub-headers needed)
        createCell(headerRow0, 0, "Asset Id", headingStyle);
        createCell(headerRow0, 1, "Asset Name", headingStyle);
        createCell(headerRow0, 2, "Network Name", headingStyle);
        createCell(headerRow0, 3, "Corrigo Id", headingStyle);
        createCell(headerRow0, 4, "Building Name", headingStyle);
        createCell(headerRow0, 5, "Floor Name", headingStyle);
        createCell(headerRow0, 6, "Location Name", headingStyle);
        createCell(headerRow0, 7, "Model", headingStyle);
        createCell(headerRow0, 8, "Vendor", headingStyle);
        createCell(headerRow0, 9, "Type", headingStyle);
        createCell(headerRow0, 10, "Asset Group", headingStyle);
        createCell(headerRow0, 11, "Category", headingStyle);
        createCell(headerRow0, 12, "Sub Category", headingStyle);

        // Columns 13-20: Instruments section header
        createCell(headerRow0, 13, "Instruments", headingStyle);

        // Columns 21-34: Attributes section header
        createCell(headerRow0, 21, "Attributes", headingStyle);

        // Columns 35-52: Conditions section header
        createCell(headerRow0, 35, "Conditions", headingStyle);

        // Columns: 53-56 : BMS
        createCell(headerRow0, 53, "BMS", headingStyle);

        // Row 1: Sub-headers for Instruments, Attributes, and Conditions
        Row headerRow1 = sheet.createRow(1);
        headerRow1.setHeightInPoints(40);

        for (int i = 0; i <= 12; i++) {
            createCell(headerRow1, i, "", headingStyle);
        }

        // Columns 13-20: Instrument sub-headers
        createCell(headerRow1, 13, "Name", headingStyle);
        createCell(headerRow1, 14, "Category", headingStyle);
        createCell(headerRow1, 15, "Sub-Category", headingStyle);
        createCell(headerRow1, 16, "Value", headingStyle);
        createCell(headerRow1, 17, "Unit", headingStyle);
        createCell(headerRow1, 18, "Entity", headingStyle);
        createCell(headerRow1, 19, "Location", headingStyle);
        createCell(headerRow1, 20, "Last Seen Status", headingStyle);

        // Columns 21-34: Attribute sub-headers
        createCell(headerRow1, 21, "Name", headingStyle);
        createCell(headerRow1, 22, "Type", headingStyle);
        createCell(headerRow1, 23, "Protocol", headingStyle);
        createCell(headerRow1, 24, "Value", headingStyle);
        createCell(headerRow1, 25, "Primary Id", headingStyle);
        createCell(headerRow1, 26, "Secondary Id", headingStyle);
        createCell(headerRow1, 27, "Tertiary Id", headingStyle);
        createCell(headerRow1, 28, "Unit", headingStyle);
        createCell(headerRow1, 29, "BACnet Object", headingStyle);
        createCell(headerRow1, 30, "BACnet Attribute", headingStyle);
        createCell(headerRow1, 31, "BACnet Device", headingStyle);
        createCell(headerRow1, 32, "Siemens Device", headingStyle);
        createCell(headerRow1, 33, "Siemens Attribute", headingStyle);
        createCell(headerRow1, 34, "Siemens Sensor Path", headingStyle);

        // Columns 35-49: Condition sub-headers
        createCell(headerRow1, 35, "Condition Id", headingStyle);
        createCell(headerRow1, 36, "Condition", headingStyle);
        createCell(headerRow1, 37, "Condition Value", headingStyle);
        createCell(headerRow1, 38, "Alert Message", headingStyle);
        createCell(headerRow1, 39, "Priority", headingStyle);
        createCell(headerRow1, 40, "Alert Id", headingStyle);
        createCell(headerRow1, 41, "Alert Profile Name", headingStyle);
        createCell(headerRow1, 42, "IOC", headingStyle);
        createCell(headerRow1, 43, "Show Alert", headingStyle);
        createCell(headerRow1, 44, "Consider the message as value on alert", headingStyle);
        createCell(headerRow1, 45, "Show threshold line on chart", headingStyle);
        createCell(headerRow1, 46, "Color of the threshold line", headingStyle);
        createCell(headerRow1, 47, "Alert After ", headingStyle);
        createCell(headerRow1, 48, "Schedule Alert", headingStyle);
        createCell(headerRow1, 49, "Schedule Alert From", headingStyle);
        createCell(headerRow1, 50, "Schedule Alert To", headingStyle);
        createCell(headerRow1, 51, "Days", headingStyle);
        createCell(headerRow1, 52, "Enable Alert Count", headingStyle);

        //Columns 53-56: BMS sub-headers
        createCell(headerRow1, 53, "Siemens Name", headingStyle);
        createCell(headerRow1, 54, "Conditions Siemens Name", headingStyle);
        createCell(headerRow1, 55, "Alert When", headingStyle);
        createCell(headerRow1, 56, "Priority", headingStyle);
        createCell(headerRow1, 57, "Alert Profile", headingStyle);
    }


    /**
     * Merges header cells to create main section headers
     */
    private void mergeMainHeaders(XSSFSheet sheet) {
        // Merge "Instruments" header across columns 13-20
        mergeCells(0, 0, 13, 20, sheet);

        // Merge "Attributes" header across columns 21-34
        mergeCells(0, 0, 21, 34, sheet);

        // Merge "Conditions" header across columns 35-52
        mergeCells(0, 0, 35, 52, sheet);

        //Merge "BMS" header across columns 53-56
        mergeCells(0, 0, 53, 57, sheet);

        // Merge asset column headers (columns 0-12) from row 0 to row 1
        for (int i = 0; i <= 12; i++) {
            mergeCells(0, 1, i, i, sheet);
        }
    }

    /**
     * Processes a single device and writes its data to the sheet
     * Returns the next available row index
     */
    private int processDevice(XSSFSheet sheet, DeviceDTO device,
                              List<BacnetAdvanceExportExcelDTO> bacnetDataList,
                              List<SiemensAdvanceExportExcelDTO> siemensDataList,
                              List<ConditionsAdvanceExportExcelDto> conditions,
                              List<SiemensBmsExportDTO> bmsDataList,
                              int startRowIndex, CellStyle dataStyle, CellStyle assetStyle) {


        int currentRowIndex = startRowIndex;
        int deviceStartRow = startRowIndex; // Track where this device starts for merging


        Set<MeasuringInstrumentDTO> instruments = device.getMeasuringInstruments();

        // NEW: Calculate BMS rows and group by asset then sensor
        int bmsRowsNeeded = calculateBmsTotalRows(bmsDataList);
        Map<String, Map<String, List<SiemensBmsExportDTO>>> bmsByAssetThenSensor =
                groupBmsByAssetThenSensor(bmsDataList);



        // ===== CASE 1: No instruments =====
        if (instruments == null || instruments.isEmpty()) {
            if (bmsRowsNeeded > 0) {
                // Device has BMS data but no instruments
                for ( Map.Entry<String, Map<String, List<SiemensBmsExportDTO>>> assetEntry : bmsByAssetThenSensor.entrySet()) {
                    Map<String, List<SiemensBmsExportDTO>> sensorsMap = assetEntry.getValue();
                    // Iterate Sensors within Asset
                    for (Map.Entry<String, List<SiemensBmsExportDTO>> sensorEntry : sensorsMap.entrySet()) {
                        List<SiemensBmsExportDTO> bmsListForSensor = sensorEntry.getValue();
                        int sensorStartRow = currentRowIndex; // Start merge tracker for THIS sensor

                        // Iterate Conditions
                        for (SiemensBmsExportDTO bms : bmsListForSensor) {
                            Row row = sheet.createRow(currentRowIndex);

                            // Write Asset info only on the very first row of the device
                            if (currentRowIndex == deviceStartRow) {
                                writeAssetData(row, device, assetStyle);
                            }

                            writeEmptyInstrumentData(row, dataStyle);
                            writeEmptyAttributeData(row, dataStyle);
                            writeEmptyConditionData(row, dataStyle);
                            writeBmsData(row, bms, dataStyle); // Write BMS Data

                            currentRowIndex++;
                        }

                        // Merge Sensor Name (Col 53) ONLY if valid and multiple rows
                        if (bmsListForSensor.size() > 1) {
                            mergeCells(sensorStartRow, currentRowIndex - 1, 53, 53, sheet);
                        }
                    }
                }
            } else {
                // No instruments AND no BMS - write one empty row
                Row row = sheet.createRow(currentRowIndex);
                writeAssetData(row, device, assetStyle);
                writeEmptyInstrumentData(row, dataStyle);
                writeEmptyAttributeData(row, dataStyle);
                writeEmptyConditionData(row, dataStyle);
                writeEmptyBmsData(row, dataStyle);
                currentRowIndex++;
            }

            // Merge asset cells (Col 0-12)
            int deviceRowCount = currentRowIndex - deviceStartRow;
            if (deviceRowCount > 1) {
                for (int col = 0; col <= 12; col++) {
                    mergeCells(deviceStartRow, currentRowIndex - 1, col, col, sheet);
                }
            }

            return currentRowIndex;
        }
        // Process each instrument
        // Process each instrument
        int bacnetIndex = 0;
        int siemensIndex = 0;
        int instrumentRowsUsed = 0;

        for (MeasuringInstrumentDTO instrument : instruments) {
            int instrumentStartRow = currentRowIndex;

            List<MeasuringInstrumentAttributesDTO> attributes = instrument.getMeasuring_instrument_attributes();
            List<LocationDTO> locations = instrument.getLocations() != null ?
                    new ArrayList<>(instrument.getLocations()) : new ArrayList<>();

            // Filter conditions for THIS specific instrument only
            List<ConditionsAdvanceExportExcelDto> instrumentConditions = filterConditionsForInstrument(conditions, instrument);

            // Determine how many rows this instrument needs
            int attributeCount = (attributes != null) ? attributes.size() : 0;
            int locationCount = locations.size();
            int conditionCount = instrumentConditions.size();
            int rowsNeeded = Math.max(1, Math.max(attributeCount, Math.max(locationCount, conditionCount)));

            // Write rows for this instrument
            for (int i = 0; i < rowsNeeded; i++) {
                Row row = sheet.createRow(currentRowIndex);

                // Write asset data (only on first row of device)
                if (currentRowIndex == deviceStartRow) {
                    writeAssetData(row, device, assetStyle);
                }

                // Write instrument data (only on first row of this instrument)
                if (currentRowIndex == instrumentStartRow) {
                    writeInstrumentData(row, instrument, dataStyle);
                }

                // Write location if applicable
                if (instrument.getMeasuring_entity().equals("location") && i < locationCount) {
                    createCell(row, 19, locations.get(i).getName(), dataStyle);
                }

                // Write attribute data if available
                if (attributes != null && i < attributes.size()) {
                    MeasuringInstrumentAttributesDTO attr = attributes.get(i);

                    // Use existing helper method to create attribute cells (columns 21-28)
                    createAttributeCells(attr, row, dataStyle);

                    // Add protocol-specific data using existing helper methods
                    if ("bacnet".equalsIgnoreCase(attr.getProtocol())) {
                        createBacnetCellsWithIndex(row, dataStyle, bacnetDataList, bacnetIndex);
                        bacnetIndex++;
                    } else if ("siemens".equalsIgnoreCase(attr.getProtocol())) {
                        createSiemensCellsWithIndex(row, dataStyle, siemensDataList, siemensIndex);
                        siemensIndex++;
                    } else {
                        // Clear protocol-specific columns if no protocol match
                        clearProtocolColumns(row, dataStyle);
                    }
                } else {
                    writeEmptyAttributeData(row, dataStyle);
                }

                // Write condition data ONLY if this instrument has conditions and condition has valid ID
                if (i < instrumentConditions.size()) {
                    ConditionsAdvanceExportExcelDto condition = instrumentConditions.get(i);
                    // Check if condition ID exists before writing
                    if (condition.getConditionId() != null && !condition.getConditionId().isEmpty()) {
                        createConditionCellsWithIndex(row, dataStyle, instrumentConditions, i);
                    } else {
                        writeEmptyConditionData(row, dataStyle);
                    }
                } else {
                    // No condition for this row - write empty
                    writeEmptyConditionData(row, dataStyle);
                }

                // NEW: Initialize BMS columns as empty
                writeEmptyBmsData(row, dataStyle);

                currentRowIndex++;
            }

            // Merge instrument cells if multiple rows were written
            if (rowsNeeded > 1) {
                for (int col = 13; col <= 18; col++) {
                    mergeCells(instrumentStartRow, currentRowIndex - 1, col, col, sheet);
                }
                mergeCells(instrumentStartRow, currentRowIndex - 1, 20, 20, sheet); // Last seen status
            }
            instrumentRowsUsed += rowsNeeded;

        }


        // ===== CASE 3: Write BMS Data (Overlaying existing rows or creating new ones) =====
        if (bmsRowsNeeded > 0) {
            // Create additional rows if BMS needs more space than instruments
            if (bmsRowsNeeded > instrumentRowsUsed) {
                int additionalRowsNeeded = bmsRowsNeeded - instrumentRowsUsed;
                for (int i = 0; i < additionalRowsNeeded; i++) {
                    Row row = sheet.createRow(currentRowIndex);
                    writeEmptyInstrumentData(row, dataStyle);
                    writeEmptyAttributeData(row, dataStyle);
                    writeEmptyConditionData(row, dataStyle);
                    // BMS data will be written in the loop below
                    currentRowIndex++;
                }
            }

            int bmsWriteRowIndex = deviceStartRow;

            // Iterate Assets
            for (Map.Entry<String, Map<String, List<SiemensBmsExportDTO>>> assetEntry : bmsByAssetThenSensor.entrySet()) {
                Map<String, List<SiemensBmsExportDTO>> sensorsMap = assetEntry.getValue();

                // Iterate Sensors
                for (Map.Entry<String, List<SiemensBmsExportDTO>> sensorEntry : sensorsMap.entrySet()) {
                    List<SiemensBmsExportDTO> bmsListForSensor = sensorEntry.getValue();
                    int sensorMergeStartRow = bmsWriteRowIndex;

                    // Iterate Conditions
                    for (SiemensBmsExportDTO bms : bmsListForSensor) {
                        Row row = sheet.getRow(bmsWriteRowIndex);

                        // Safety check: Create row if logic gap occurred (shouldn't happen with math above)
                        if (row == null) {
                            row = sheet.createRow(bmsWriteRowIndex);
                            writeEmptyInstrumentData(row, dataStyle);
                            writeEmptyAttributeData(row, dataStyle);
                            writeEmptyConditionData(row, dataStyle);
                        }

                        writeBmsData(row, bms, dataStyle);
                        bmsWriteRowIndex++;
                    }

                    // Merge BMS Sensor Name (Col 53)
                    if (bmsListForSensor.size() > 1) {
                        mergeCells(sensorMergeStartRow, bmsWriteRowIndex - 1, 53, 53, sheet);
                    }
                }
            }
        }


            // Merge asset cells across all rows of this device
        int deviceRowCount = currentRowIndex - deviceStartRow;
        if (deviceRowCount > 1) {
            for (int col = 0; col <= 12; col++) {
                mergeCells(deviceStartRow, currentRowIndex - 1, col, col, sheet);
            }
        }

        return currentRowIndex;


    }

    /**
     * Writes asset data to columns 0-12
     */
    private void writeAssetData(Row row, DeviceDTO device, CellStyle style) {
        createCell(row, 0, device.getId(), style);
        createCell(row, 1, device.getUser_data_name() != null ? device.getUser_data_name() : device.getDisplay_name(), style);
        createCell(row, 2, device.getDocker_name(), style);

        // Extract Corrigo ID from custom fields
        String corrigoId = "";
        if (device.getCustom_fields() != null) {
            JSONArray customFieldsArray = JSONArray.parseArray(device.getCustom_fields());
            for (int i = 0; i < customFieldsArray.size(); i++) {
                JSONObject customField = customFieldsArray.getJSONObject(i);
                if (customField.containsKey("Corrigo_ID")) {
                    corrigoId = customField.getString("Corrigo_ID");
                    break;
                }
            }
        }
        createCell(row, 3, corrigoId, style);

        createCell(row, 4, device.getBuilding(), style);
        createCell(row, 5, device.getFloor(), style);
        createCell(row, 6, device.getLocation(), style);
        createCell(row, 7, device.getUser_data_model(), style);
        createCell(row, 8, device.getUser_data_vendor(), style);
        createCell(row, 9, device.getType(), style);
        createCell(row, 10, device.getAsset_group(), style);
        createCell(row, 11, device.getCategory(), style);
        createCell(row, 12, device.getSub_category(), style);
    }

    /**
     * Writes instrument data to columns 13-20
     */
    private void writeInstrumentData(Row row, MeasuringInstrumentDTO instrument, CellStyle style) {
        createCell(row, 13, instrument.getName(), style);
        createCell(row, 14, instrument.getSensor_type(), style);
        createCell(row, 15, instrument.getSub_category(), style);
        createCell(row, 16, instrument.getValue() != null ? instrument.getValue() : "", style);
        createCell(row, 17, instrument.getUnit() != null ? instrument.getUnit() : "", style);
        createCell(row, 18, instrument.getMeasuring_entity(), style);
        // Column 19 is for location (written separately when applicable)
        createCell(row, 20, instrument.getTimestamp() == null ? "" : Instant.ofEpochMilli(instrument.getTimestamp().longValue()).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), style);


    }

    /**
     * Writes empty instrument data to columns 13-20
     */
    private void writeEmptyInstrumentData(Row row, CellStyle style) {
        for (int col = 13; col <= 20; col++) {
            createCell(row, col, "", style);
        }
    }

    /**
     * Writes empty attribute data (columns 21-34) when no attributes exist
     */
    private void writeEmptyAttributeData(Row row, CellStyle style) {
        for (int col = 21; col <= 34; col++) {
            createCell(row, col, "", style);
        }
    }

    /**
     * Clears protocol-specific columns (BACnet and Siemens) when protocol doesn't match
     */
    private void clearProtocolColumns(Row row, CellStyle style) {
        // Clear BACnet columns (29-31)
        createCell(row, 29, "", style);
        createCell(row, 30, "", style);
        createCell(row, 31, "", style);

        // Clear Siemens columns (32-34)
        createCell(row, 32, "", style);
        createCell(row, 33, "", style);
        createCell(row, 34, "", style);
    }

    /**
     * Writes empty condition data to columns 35-41
     */
    private void writeEmptyConditionData(Row row, CellStyle style) {
        for (int col = 35; col <= 52; col++) {
            createCell(row, col, "", style);
        }
    }

    /**
     * Fetches BACnet data with error handling
     */
    private List<BacnetAdvanceExportExcelDTO> fetchBacnetData(String username, String vdmsid, String deviceId) {
        try {
            List<BacnetAdvanceExportExcelDTO> data = bacnetService.getBacnetDeviceIdForAdvanceExcelExport(username, vdmsid, deviceId);
            return data;
        } catch (Exception e) {
            System.out.println("No BACnet data for device: " + deviceId);
            return new ArrayList<>();
        }
    }

    /**
     * Fetches Siemens data with error handling
     */
    private List<SiemensAdvanceExportExcelDTO> fetchSiemensData(String username, String vdmsid, String deviceId) {
        try {
            List<SiemensAdvanceExportExcelDTO> data = siemensService.getSiemensDeviceIdForAdvanceExcelExport(username, vdmsid, deviceId);
            return data;
        } catch (Exception e) {
            System.out.println("No Siemens data for device: " + deviceId);
            return new ArrayList<>();
        }
    }

    /**
     * Fetches conditions data with error handling
     */
    private List<ConditionsAdvanceExportExcelDto> fetchConditions(String username, String vdmsid, String deviceId) {
        try {
            List<ConditionsAdvanceExportExcelDto> data = conditionsService.getConditionsForAdvanceExcelExport(username, vdmsid, deviceId);
            return data;
        } catch (Exception e) {

            System.out.println("No Conditions data for device: " + deviceId);
            return new ArrayList<>();
        }
    }
    /**
     * Fetches BMS data for specific device
     */
    private List<SiemensBmsExportDTO> fetchBmsData(String username, String vdmsid, String deviceId) {
        try {
            List<SiemensBmsExportDTO> data = siemensService.getSiemensBmsData(username, vdmsid, deviceId);
            return data;
        } catch (Exception e) {
            System.out.println("No BMS data for device: " + deviceId);
            return new ArrayList<>();
        }
    }


    /**
     * Filters conditions that belong to a specific instrument
     */
    private List<ConditionsAdvanceExportExcelDto> filterConditionsForInstrument(
            List<ConditionsAdvanceExportExcelDto> allConditions,
            MeasuringInstrumentDTO instrument) {

        if (allConditions == null || allConditions.isEmpty()) {
            return new ArrayList<>();
        }

        String instrumentId = instrument.getId();

        return allConditions.stream()
                .filter(condition -> instrumentId.equals(condition.getMeasuringInstrumentId()))
                .collect(Collectors.toList());
    }
    /**
     * Groups BMS data: First by BMS Asset ID (to separate devices),
     * then by Sensor Name (to group conditions).
     */
    private Map<String, Map<String, List<SiemensBmsExportDTO>>> groupBmsByAssetThenSensor(List<SiemensBmsExportDTO> bmsList) {
        if (bmsList == null || bmsList.isEmpty()) {
            return new LinkedHashMap<>();
        }

        // 1. Group by BMS Asset ID (This ensures different assets don't merge)
        Map<String, List<SiemensBmsExportDTO>> byAssetId = bmsList.stream()
                .collect(Collectors.groupingBy(
                        SiemensBmsExportDTO::getAssetId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 2. Group inside each asset by Sensor Name
        Map<String, Map<String, List<SiemensBmsExportDTO>>> result = new LinkedHashMap<>();

        for (Map.Entry<String, List<SiemensBmsExportDTO>> entry : byAssetId.entrySet()) {
            String assetId = entry.getKey();
            List<SiemensBmsExportDTO> assetBmsList = entry.getValue();

            Map<String, List<SiemensBmsExportDTO>> bySensorName = assetBmsList.stream()
                    .collect(Collectors.groupingBy(
                            SiemensBmsExportDTO::getAssetName,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            result.put(assetId, bySensorName);
        }

        return result;
    }

    /**
     * Creates a heading cell style with bold font
     */
    private CellStyle createHeadingStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Calibri");
        style.setFont(font);

        return style;
    }

    /**
     * Creates a data cell style
     */
    private CellStyle createDataStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        font.setFontName("Calibri");
        style.setFont(font);

        return style;
    }

    /**
     * Helper method to create a cell with value and style
     */
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    /**
     * Helper method to merge cells
     */
    private void mergeCells(int firstRow, int lastRow, int firstCol, int lastCol, XSSFSheet sheet) {
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }

// ==================== CORRECTED EXISTING HELPER METHODS ====================

    /**
     * Creates attribute cells for columns 21-28
     * These are the basic attribute properties before protocol-specific data
     */
    public void createAttributeCells(MeasuringInstrumentAttributesDTO attribute, Row row, CellStyle style) {
        createCell(row, 21, attribute.getName() != null ? attribute.getName() : "", style);
        createCell(row, 22, attribute.getType() != null ? attribute.getType() : "", style);
        createCell(row, 23, attribute.getProtocol() != null ? attribute.getProtocol() : "", style);
        createCell(row, 24, attribute.getValue() != null ? attribute.getValue() : "", style);
        createCell(row, 25, attribute.getPrimary_id() != null ? attribute.getPrimary_id() : "", style);
        createCell(row, 26, attribute.getSecondary_id() != null ? attribute.getSecondary_id() : "", style);
        createCell(row, 27, attribute.getTertiary_id() != null ? attribute.getTertiary_id() : "", style);
        createCell(row, 28, attribute.getUnit() != null ? attribute.getUnit() : "", style);
    }

    /**
     * Creates BACnet-specific cells for columns 29-31
     * Writes BACnet data if available, otherwise writes empty cells
     */
    public void createBacnetCellsWithIndex(Row row, CellStyle style,
                                           List<BacnetAdvanceExportExcelDTO> bacnetDataList,
                                           int bacnetIndex) {
        if (bacnetDataList != null && bacnetIndex < bacnetDataList.size()) {
            BacnetAdvanceExportExcelDTO bacnetData = bacnetDataList.get(bacnetIndex);

            // Column 29: BACnet Object Name
            String bacnetObjectName = bacnetData.getBacnetObjectName() != null ? bacnetData.getBacnetObjectName() : "";
            createCell(row, 29, bacnetObjectName, style);

            // Column 30: BACnet Attribute Name
            String bacnetAttributeName = bacnetData.getBacnetAttributeName() != null ? bacnetData.getBacnetAttributeName() : "";
            createCell(row, 30, bacnetAttributeName, style);

            // Column 31: BACnet Device Name
            String bacnetDeviceName = bacnetData.getBacnetDeviceName() != null ? bacnetData.getBacnetDeviceName() : "";
            createCell(row, 31, bacnetDeviceName, style);

            // Clear Siemens columns since this is BACnet
            createCell(row, 32, "", style);
            createCell(row, 33, "", style);
            createCell(row, 34, "", style);
        } else {
            // No BACnet data - clear all protocol columns
            createCell(row, 29, "", style);
            createCell(row, 30, "", style);
            createCell(row, 31, "", style);
            createCell(row, 32, "", style);
            createCell(row, 33, "", style);
            createCell(row, 34, "", style);
        }
    }

    /**
     * Creates Siemens-specific cells for columns 32-34
     * Writes Siemens data if available, otherwise writes empty cells
     */
    public void createSiemensCellsWithIndex(Row row, CellStyle style,
                                            List<SiemensAdvanceExportExcelDTO> siemensDataList,
                                            int siemensIndex) {
        if (siemensDataList != null && siemensIndex < siemensDataList.size()) {
            SiemensAdvanceExportExcelDTO siemensData = siemensDataList.get(siemensIndex);

            // Clear BACnet columns since this is Siemens
            createCell(row, 29, "", style);
            createCell(row, 30, "", style);
            createCell(row, 31, "", style);

            // Column 32: Siemens Device Name
            createCell(row, 32, siemensData.getSiemensDeviceName() != null ? siemensData.getSiemensDeviceName() : "", style);

            // Column 33: Siemens Attribute Name
            createCell(row, 33, siemensData.getSiemensAttributeName() != null ? siemensData.getSiemensAttributeName() : "", style);

            // Column 34: Siemens Sensor Path
            createCell(row, 34, siemensData.getSiemensSensorPath() != null ? siemensData.getSiemensSensorPath() : "", style);
        } else {
            // No Siemens data - clear all protocol columns
            createCell(row, 29, "", style);
            createCell(row, 30, "", style);
            createCell(row, 31, "", style);
            createCell(row, 32, "", style);
            createCell(row, 33, "", style);
            createCell(row, 34, "", style);
        }
    }

    /**
     * Creates condition cells for columns 35-41
     * Writes condition data if available, otherwise writes empty cells
     */
    public void createConditionCellsWithIndex(Row row, CellStyle style,
                                              List<ConditionsAdvanceExportExcelDto> conditionsDataList,
                                              int conditionIndex) {
        if (conditionsDataList != null && conditionIndex < conditionsDataList.size()) {
            ConditionsAdvanceExportExcelDto conditionData = conditionsDataList.get(conditionIndex);

            // Column 35: Condition Id
            String conditionId = conditionData.getConditionId() != null ?
                    conditionData.getConditionId() : "";
            createCell(row, 35, conditionId, style);

            // Column 36: Condition (Condition info -> condition name + alert condition + condition value)
            boolean isBlank=false;
            if(conditionData.getConditionName()==null || conditionData.getAlertCondition()==null || conditionData.getValueName()==null ){
                isBlank=true;
            }
            String condition = Stream.of(

                            conditionData.getConditionName(),
                            conditionData.getAlertCondition(),
                            conditionData.getValueName()
                    )

                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" "));
            if(isBlank) {
                createCell(row, 36, " ", style);
            }
            else {
                createCell(row, 36, condition, style);

            }

            // Column 37: Condition Value
            String conditionValue = conditionData.getValueName() != null ? conditionData.getValueName() : "";
            createCell(row, 37, conditionValue, style);

            // Column 38: Alert Message
            String alertMessage = conditionData.getAlertMessage() != null ? conditionData.getAlertMessage() : "";
            createCell(row, 38, alertMessage, style);

            // Column 39: Priority
            String priority = conditionData.getPriority() != null ? conditionData.getPriority() : "";
            createCell(row, 39, priority, style);

            // Column 40: Alert Id
            String alertId = conditionData.getAlertProfileId() != null ? conditionData.getAlertProfileId() : "";
            createCell(row, 40, alertId, style);

            // Column 40: Alert Profile Name (Alert Name)
            String alertProfileName = conditionData.getAlertProfileName() != null ? conditionData.getAlertProfileName() : "";
            createCell(row, 41, alertProfileName, style);

            // Column 41: IOC (Alert IOC)
            Integer ioc = conditionData.getIoc() != null ? conditionData.getIoc() : null;
            createCell(row, 42, ioc, style);

            // Column 43: Show Alert
            Boolean showAlert = conditionData.getShowAlert() != null ? conditionData.getShowAlert() : null;
            createCell(row, 43, showAlert!=Boolean.FALSE ? 1:0, style);

            // Column 44: Show Alert Message As Value
            Boolean showAlertMessageAsValue = conditionData.getShowAlertMessageAsValue() != null ? conditionData.getShowAlertMessageAsValue() : null;
            createCell(row, 44, showAlertMessageAsValue!=Boolean.FALSE ? 1:0, style);

            // Column 45: Enable Threshold Line
           Integer enableThresholdLine = conditionData.getEnableThresholdLineOnChart() != null ? conditionData.getEnableThresholdLineOnChart() : null;
            createCell(row, 45, enableThresholdLine, style);

            // Column 46: Threshold Line Color
            String thresholdLineColor = conditionData.getColorOfThresholdLineOnChart() != null ? conditionData.getColorOfThresholdLineOnChart() : "";
            createCell(row, 46, thresholdLineColor, style);

            // Column 47: Alert After
            Long totalSeconds = (long) (conditionData.getAlertAfterTime() != null ? conditionData.getAlertAfterTime() : 0);
            long timeInDays = totalSeconds / (24 * 3600);
            totalSeconds %= (24 * 3600);

            long timeInHours = totalSeconds / 3600;
            totalSeconds %= 3600;

            long timeInMinutes = totalSeconds / 60;

            String alertAfter = timeInDays + " days " + timeInHours + " hours " +
                    timeInMinutes + " minutes ";
            createCell(row, 47, alertAfter, style);

            // Column 48: Schedule Alert
            Integer scheduleAlert = conditionData.getScheduleAlert() != null ? conditionData.getScheduleAlert() : null;
            createCell(row, 48, scheduleAlert, style);

            // Column 49: Schedule Alert From

            String scheduleAlertFrom = conditionData.getScheduleStartTime() != null ? conditionData.getScheduleStartTime() : "";
            createCell(row, 49, scheduleAlertFrom, style);

            // Column 50: Schedule Alert To
            String scheduleAlertTo = conditionData.getScheduleEndTime() != null ? conditionData.getScheduleEndTime() : "";
            createCell(row, 50, scheduleAlertTo, style);

            //  Column  51: Days
            String days = conditionData.getScheduleConditions() != null ? conditionData.getScheduleConditions() : "";
            String daysCleaned = daysCleaned(days);
            createCell(row, 51, daysCleaned, style);


            // Column 52: Alert Count Enable
            Integer alertCountEnable = conditionData.getAlertCountEnable() != null ? conditionData.getAlertCountEnable() : null;
            createCell(row, 52, alertCountEnable, style);




        } else {
            // No condition data - write empty cells
            createCell(row, 35, "", style);
            createCell(row, 36, "", style);
            createCell(row, 37, "", style);
            createCell(row, 38, "", style);
            createCell(row, 39, "", style);
            createCell(row, 40, "", style);
            createCell(row, 41, "", style);
            createCell(row, 42, "", style);
            createCell(row, 43, "", style);
            createCell(row, 44, "", style);
            createCell(row, 45, "", style);
            createCell(row, 46, "", style);
            createCell(row, 47, "", style);
            createCell(row, 48, "", style);
            createCell(row, 49, "", style);
            createCell(row, 50, "", style);
            createCell(row, 51, "", style);
            createCell(row, 52, "", style);
        }
    }
public String daysCleaned(String input){
    if (input == null || input.isEmpty()) {
        return "";
    }

    Pattern pattern = Pattern.compile("\"day\"\\s*:\\s*\"([^\"]+)\"");
    Matcher matcher = pattern.matcher(input);

    StringBuilder result = new StringBuilder();

    while (matcher.find()) {
        result.append(matcher.group(1)).append(",");
    }

    // remove last comma
    if (result.length() > 0) {
        result.setLength(result.length() - 1);
    }

    return result.toString();
}
    /**
     * Calculates total BMS rows needed for a device
     */
    private int calculateBmsTotalRows(List<SiemensBmsExportDTO> bmsList) {
        if (bmsList == null || bmsList.isEmpty()) {
            return 0;
        }
        return bmsList.size();
    }
    /**
     * Writes BMS data to columns 53-56
     */
    private void writeBmsData(Row row, SiemensBmsExportDTO bmsData, CellStyle style) {
        createCell(row, 53, bmsData.getAssetName() != null ? bmsData.getAssetName() : "", style);

       if(bmsData.getAssetConditionId()!=null ){
           createCell(row, 54, bmsData.getAssetName(), style);}
       else{
              createCell(row, 54, "", style);
       }
        createCell(row, 55, bmsData.getAlertCondition() != null ? bmsData.getAlertCondition() : "", style);
        createCell(row, 56, bmsData.getPriority() != null ? bmsData.getPriority() : "", style);
        createCell(row, 57, bmsData.getAlertProfileName() != null ? bmsData.getAlertProfileName() : "", style);
    }
    /**
     * Writes empty BMS data to columns 53-56
     */
    private void writeEmptyBmsData(Row row, CellStyle style) {
        for (int col = 53; col <= 57; col++) {
            createCell(row, col, "", style);
        }
    }

//    public void generateExcel(Set<DeviceDTO> devices, HttpServletResponse response) throws IOException {
//
//
//        VdmsDTO vdmsDetails = vdmsService.getVDMSDetails();
//        String timeZoneId = vdmsDetails.getTimezone();
//        String propertyName = vdmsDetails.getProperty_name();
//        ObjectMapper objectMapper = new ObjectMapper();
//        String object = objectMapper.writeValueAsString(devices);
//
//        JSONArray jsonArray = JSON.parseArray(object);
//        Map<String, Integer> headerMap = new HashMap<>();
//
//        Map<String, String> modifiedHeaderNames = new HashMap<>();
//
//
//        String[] headers = {
//                "ID", "Name", "Model", "Vendor", "Network", "Serial Number",
//                "Created Date", "Created By", "Description", "Location", "VDMS ID"
//        };
//
//        int col = 0;
//        for (String header : headers) {
//            headerMap.put(header, col++);
//        }
//        List<Map<String, String>> excelDataList = new ArrayList<>();
//
//        for (Object obj : jsonArray) {
//
//            Map<String, String> excelMap = new HashMap<>();
//            if (obj instanceof JSONObject) {
//                JSONObject jsonObject = (JSONObject) obj;
//
//                excelMap.put("ID", Objects.requireNonNullElse(jsonObject.getString("id"), ""));
//                String userDataName = Objects.requireNonNullElse(jsonObject.getString("user_data_name"), "");
//                excelMap.put("Name", !userDataName.equals("") ? userDataName : Objects.requireNonNullElse(jsonObject.getString("display_name"), ""));
//
//                String userDataModel = Objects.requireNonNullElse(jsonObject.getString("user_data_model"), "");
//                excelMap.put("Model", !userDataModel.equals("") ? userDataModel : Objects.requireNonNullElse(jsonObject.getString("model"), ""));
//
//                String userDataVendor = Objects.requireNonNullElse(jsonObject.getString("user_data_vendor"), "");
//                excelMap.put("Vendor", !userDataVendor.equals("") ? userDataVendor : Objects.requireNonNullElse(jsonObject.getString("vendor"), ""));
//
//
//                excelMap.put("Network", Objects.requireNonNullElse(jsonObject.getString("docker_name"), ""));
//                excelMap.put("Serial Number", Objects.requireNonNullElse(jsonObject.getString("serial_number"), ""));
//
//                if (jsonObject.getString("created_timestamp") != null) {
//
//                    String date = utils.getCurrentDateByTimezone(jsonObject.getBigInteger("created_timestamp"), timeZoneId);
//                    excelMap.put("Created Date", date);
//                }
//
//
//                if (jsonObject.getString("created_email") != null) {
//                    excelMap.put("Created By", Objects.requireNonNullElse(jsonObject.getString("created_email"), ""));
//                }
//
//                excelMap.put("Description", Objects.requireNonNullElse(jsonObject.getString("description"), ""));
//
//
//                String locationId = jsonObject.getString("location_id");
//                if (locationId != null) {
//                    String loc = Objects.requireNonNullElse(jsonObject.getString("location"), "") + ", " +
//                            Objects.requireNonNullElse(jsonObject.getString("floor"), "") + ", " +
//                            Objects.requireNonNullElse(jsonObject.getString("building"), "");
//                    excelMap.put("Location", loc);
//                }
//
//                excelMap.put("VDMS ID", Objects.requireNonNullElse(jsonObject.getString("vdms_id"), ""));
//
//                if (jsonObject.containsKey("custom_fields")) {
//                    JSONArray customFieldsArray = JSON.parseArray(jsonObject.getString("custom_fields"));
//
//
//                    for (Object customFieldObj : customFieldsArray) {
//                        if (customFieldObj instanceof JSONObject) {
//                            JSONObject customField = (JSONObject) customFieldObj;
//
//                            for (String key : customField.keySet()) {
//                                String lowercaseKey = key.toLowerCase();
//                                //removing all spaces
//                                String modifiedKey = this.removeUnderscores(key).toLowerCase();
//
//                                if (!headerMap.containsKey(lowercaseKey) && !modifiedHeaderNames.containsKey(modifiedKey)) {
//                                    try {
//
//                                        int columnIndex = headerMap.size();
//                                        headerMap.put(modifiedKey, columnIndex);
//                                        modifiedHeaderNames.put(modifiedKey, lowercaseKey);
//
//                                    } catch (Exception e) {
//                                        System.out.println(e);
//                                    }
//
//                                }
//                                excelMap.put(modifiedKey, customField.getString(key));
//                            }
//                        }
//                    }
//                }
//            }
//            excelDataList.add(excelMap);
//        }
//
//        this.generateExcelFile(response, excelDataList, headerMap, timeZoneId, propertyName, modifiedHeaderNames);
//
//    }

    public String removeUnderscores(String input) {
        // Use the replaceAll method to replace underscores with an empty string
        return input.replaceAll("_", "");
    }

//    public void generateExcelFile(HttpServletResponse
//                                          response, List<Map<String, String>> excelData, Map<String, Integer> headerMap, String timeZoneId, String
//                                          propertyName, Map<String, String> modifiedHeaderNames) throws IOException {
//        // Create a new workbook and sheet
//
//        response.setContentType("application/octet-stream");
//        String headerKey = "Content-Disposition";
//
//        String modified_filename = utils.replaceSpecialCasesInFilename(propertyName);
//
//        String filename = modified_filename + "_" + "AssetList" + "_";
//
//        System.out.println("------- File name --------" + filename);
//
//        String currentDateTime = utils.getCurrentDateByTimezone(BigInteger.valueOf(System.currentTimeMillis()), timeZoneId);
//
//        String headerValue = "attachment; filename=" + filename + currentDateTime + ".xlsx";
//
//        System.out.println("--------- header value -------" + headerValue);
//
//        response.setHeader(headerKey, headerValue);
//        SXSSFWorkbook workbook = new SXSSFWorkbook();
//
//        // needs changes
//        SXSSFSheet sheet = workbook.createSheet(modified_filename);
//
//        //Setting Header Bold
//        CellStyle headerCellStyle = workbook.createCellStyle();
//        Font headerFont = workbook.createFont();
//        headerFont.setBold(true);
//        headerCellStyle.setFont(headerFont);
//
//        // Create a header row
//        SXSSFRow headerRow = sheet.createRow(0);
//
//        //Populate header row with column names
//        for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
//            String modifiedName = removeUnderscores(entry.getKey());
//            String actualKey = "";
//            if (modifiedHeaderNames.get(modifiedName) != null) {
//                actualKey = modifiedHeaderNames.get(modifiedName);
//            } else {
//                actualKey = entry.getKey();
//            }
//            String titleCaseKey = utils.convertSnakeCaseToTitleCase(actualKey);
//            Cell headerCell = headerRow.createCell(entry.getValue());
//            headerCell.setCellValue(titleCaseKey);
//
//            // Set the font style to bold for the header cell
//            headerCell.setCellStyle(headerCellStyle);
//        }
//
//        //Populate data rows
//        int rowIndex = 1;
//        for (Map<String, String> excelMap : excelData) {
//            SXSSFRow dataRow = sheet.createRow(rowIndex++);
//            for (Map.Entry<String, String> entry : excelMap.entrySet()) {
//                if (headerMap.get(entry.getKey()) != null) {
//                    Cell dataCell = dataRow.createCell(headerMap.get(entry.getKey()));
//                    dataCell.setCellValue(entry.getValue());
//                }
//
//            }
//        }
//
//        ServletOutputStream outputStream = response.getOutputStream();
//        workbook.write(outputStream);
//        workbook.close();
//        outputStream.close();
//
//    }

    public DeviceDTO getDeviceAndOnboardStatusByDeviceId(String device_id) {
        DeviceDTO deviceDTO = deviceRepository.getDeviceByDeviceId(device_id);
        deviceDTO.setOnboard_data(new DeviceOnboardStatusDTO(deviceDTO.getAssignee_email(), deviceDTO.getImage_status(),
                deviceDTO.getGeolocation_status(), deviceDTO.getTag_status(), deviceDTO.getField_status(),
                deviceOnboardStatusAssigneeService.getDeviceOnboardStatusAssignees(deviceDTO.getDevice_onboard_status_id())));

        return deviceDTO;
    }


    public void syncDeviceOnboardStatus(String vdmsid) {
        Set<DeviceDTO> deviceDTOS = deviceRepository.listAllDeviceByVdmsId(vdmsid);
        System.out.println("Total device size: " + deviceDTOS.size());
        for (DeviceDTO device : deviceDTOS) {
            this.syncSingleDeviceOnboardStatus(vdmsid, device.getId());
        }
    }

    public void syncAllDeviceOnboardStatusForQrSync(String vdmsid) {
        Set<DeviceDTO> deviceDTOS = deviceRepository.listAllDeviceByVdmsId(vdmsid);
        log.info("Total device size: {}", deviceDTOS.size());
        for (DeviceDTO device : deviceDTOS) {
            this.syncSingleDeviceOnboardStatusForQrSync(vdmsid, device.getId());
        }
    }

    public void syncSingleDeviceOnboardStatus(String vdmsid, String device_id) {
        if (vdmsid != null && device_id != null) {
            DeviceDTO deviceDTO = this.getDeviceByDeviceIdNew(null, vdmsid, null, device_id);

            System.out.println("Device id : " + device_id);

            int field_status = 1;
            int image_status = 1;
            int tag_status = 1;
            int geolocation_status;

            if (deviceDTO.getAsset_image_url() == null || deviceDTO.getAsset_image_url().equals("[]")) {
                image_status = 0;
            }
            if (deviceDTO.getLocation_status() != null && deviceDTO.getLocation_status().equals("no_fixed_location")) {
                geolocation_status = 1;
            } else if (deviceDTO.getLocation_id() == null && deviceDTO.getLocation_status() == null) {
                geolocation_status = 0;
            } else if (deviceDTO.getLocation_id() != null && (deviceDTO.getPosition() == null || deviceDTO.getLatitude() == null || deviceDTO.getLongitude() == null)) {
                geolocation_status = 0;
            } else {
                geolocation_status = 1;
            }


            if (deviceDTO.getQrcode_count() == 0) {
                tag_status = 0;
            }

            if ((deviceDTO.getName() == null && deviceDTO.getUser_data_name() == null) || (deviceDTO.getVendor() == null && deviceDTO.getUser_data_vendor() == null) || (deviceDTO.getModel() == null && deviceDTO.getUser_data_model() == null)) {
                field_status = 0;
            }

            if (deviceDTO.getOnboard_data().getId() != null) {
                deviceOnboardStatusRepository.updateAssetOnboardData(deviceDTO.getId(), image_status, geolocation_status, tag_status, field_status);
            } else {
                String id = Generators.timeBasedGenerator().generate().toString();
                deviceOnboardStatusRepository.addOnboardAsset(id, deviceDTO.getId(), null, image_status, geolocation_status, tag_status, field_status);
            }

            System.out.println("Image : " + image_status);
            System.out.println("field_status : " + field_status);
            System.out.println("geolocation_status : " + geolocation_status);
            System.out.println("tag_status : " + tag_status);

            if (image_status == 0 || field_status == 0 || geolocation_status == 0 || tag_status == 0) {
                System.out.println("Updating status.......");
                if (deviceDTO.getOnboard_status() != null) {
                    if (deviceDTO.getOnboard_status() == 3) {
                        this.updateOnboardAssetStatus(Collections.singleton(deviceDTO.getId()), 0);
                    } else if (deviceDTO.getOnboard_status() == 2) {
                        this.updateOnboardAssetStatus(Collections.singleton(deviceDTO.getId()), 1);
                    }
                }
            }
        }
    }

    public void syncSingleDeviceOnboardStatusForQrSync(String vdmsid, String device_id) {
        if (vdmsid != null && device_id != null) {
            DeviceDTO deviceDTO = this.getDeviceByDeviceIdNew(null, vdmsid, null, device_id);

            log.info("Device id : {}", device_id);

            int field_status = deviceDTO.getField_status();
            int image_status = deviceDTO.getImage_status();
            int tag_status = deviceDTO.getTag_status();
            int geolocation_status = deviceDTO.getGeolocation_status();
            if (deviceDTO.getQrcode_count() > 0) {
                switch (deviceDTO.getTag_status()) {
                    case 0:
                    case 1:
                    case 3:
                        tag_status = 1;
                        break;
                    case 2:
                        BigInteger maxUpdatedQrCodeTimeStamp = qrCodeService.getMaxUpdatedQrCodeTimeStamp(deviceDTO.getId());
                        BigInteger maxUpdatedClientQrCodeTimeStamp = clientQrCodeService.maxUpdatedClientQrCodeTimeStamp(deviceDTO.getId());
                        BigInteger maxTimestamp = Utils.getMaxTimeStamp(maxUpdatedQrCodeTimeStamp, maxUpdatedClientQrCodeTimeStamp);
                        if (maxTimestamp != null && Utils.isLessThan10Minutes(maxTimestamp)) {
                            tag_status = 1;
                        } else {
                            tag_status = 2;
                        }
                        break;
                }

            } else if (deviceDTO.getQrcode_count() == 0) {
                switch (deviceDTO.getTag_status()) {
                    case 0:
                    case 1:
                        tag_status = 0;
                        break;
                    case 3:
                        tag_status = 3;
                        break;
                    case 2:
                        tag_status = 2;
                        break;
                }
            }

            if (deviceDTO.getOnboard_data().getId() != null) {
                deviceOnboardStatusRepository.updateAssetOnboardData(deviceDTO.getId(), image_status, geolocation_status, tag_status, field_status);
            } else {
                String id = Generators.timeBasedGenerator().generate().toString();
                deviceOnboardStatusRepository.addOnboardAsset(id, deviceDTO.getId(), null, image_status, geolocation_status, tag_status, field_status);
            }

            log.info("Image : {}", image_status);
            log.info("field_status : {}", field_status);
            log.info("geolocation_status : {}", geolocation_status);
            log.info("tag_status : {}", tag_status);

            if (image_status == 0 || field_status == 0 || geolocation_status == 0 || tag_status == 0) {
                System.out.println("Updating status.......");
                if (deviceDTO.getOnboard_status() != null) {
                    if (deviceDTO.getOnboard_status() == 3) {
                        log.info("Updating status.......from 3 to 0");
                        this.updateOnboardAssetStatus(Collections.singleton(deviceDTO.getId()), 0);
                    } else if (deviceDTO.getOnboard_status() == 2) {
                        log.info("Updating status.......from 2 to 1");
                        this.updateOnboardAssetStatus(Collections.singleton(deviceDTO.getId()), 1);
                    }
                }
            } else if ((image_status == 1 || image_status == 3) && (field_status == 1 || field_status == 3)
                    && (geolocation_status == 1 || geolocation_status == 3) && (tag_status == 1 || tag_status == 3)) {
                if (deviceDTO.getOnboard_status() != null) {
                    if (deviceDTO.getOnboard_status() == 0) {
                        log.info("Updating status.......from 0 to 3");
                        this.updateOnboardAssetStatus(Collections.singleton(deviceDTO.getId()), 3);
                    } else if (deviceDTO.getOnboard_status() == 1) {
                        log.info("Updating status.......from 1 to 2");
                        this.updateOnboardAssetStatus(Collections.singleton(deviceDTO.getId()), 2);
                    }
                }
            }
        }
    }

    public DeviceDTO getDeviceByDeviceIdNew(String username, String vdmsid, String dockername, String device_id) {
        System.out.println(device_id);

        DeviceDTO device = deviceRepository.getDeviceByDeviceIdNew(device_id);
        if (device != null) {
            device.setOnboard_data(new DeviceOnboardStatusDTO(device.getAssignee_email(), device.getImage_status(), device.getGeolocation_status(), device.getTag_status(), device.getField_status(), device.getDevice_onboard_status_id()));
            try {
                device.setQrcode_count(qrCodeService.getQrCodeCountByDeviceId(device.getId()) + clientQrCodeService.getClientQrCodeCountByDeviceId(device.getId()));
                device.setNfc_count(nfcService.getQrNfcCountByDeviceId(device.getId()) + clientNfcService.getClientNfcCountByDeviceId(device.getId()));
                device.setBarcode_count(clientBarCodeService.getClientBarCodeCountByDeviceId(device.getId()));
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return device;
    }

    public void updateVirtualDeviceOnboardStatus(Set<DeviceDTO> virtual_devices, String username) {
        List<DeviceOnboardStatusDTO> deviceOnboardStatusDTOList = new ArrayList<>();
        for (DeviceDTO deviceDTO : virtual_devices) {
            DeviceOnboardStatusDTO deviceOnboardStatusDTO = this.updateDeviceOnboardStatus(deviceDTO);

            deviceOnboardStatusDTO.setId(Generators.timeBasedGenerator().generate().toString());
            deviceOnboardStatusDTO.setDevice_id(deviceDTO.getId());
            deviceOnboardStatusRepository.addOnboardAsset(deviceOnboardStatusDTO.getId(), deviceOnboardStatusDTO.getDevice_id(), deviceOnboardStatusDTO.getAssignee_email(),
                    deviceOnboardStatusDTO.getImage_status(), deviceOnboardStatusDTO.getGeolocation_status(), deviceOnboardStatusDTO.getTag_status(), deviceOnboardStatusDTO.getField_status());

            int deviceOnboardStatus;

            if (deviceOnboardStatusDTO.getImage_status() == 1 && deviceOnboardStatusDTO.getGeolocation_status() == 1 && deviceOnboardStatusDTO.getTag_status() == 1 && deviceOnboardStatusDTO.getField_status() == 1) {
                deviceOnboardStatus = 3;
            } else {
                deviceOnboardStatus = 0;
            }

            updateOnboardAssetStatus(Collections.singleton(deviceDTO.getId()), deviceOnboardStatus);

            deviceOnboardStatusDTOList.add(deviceOnboardStatusDTO);

        }
        this.updateVirtualDeviceHistories(deviceOnboardStatusDTOList, username);
    }

    private void updateVirtualDeviceHistories(List<DeviceOnboardStatusDTO> deviceOnboardStatusDTOs, String username) {
        for (DeviceOnboardStatusDTO deviceOnboardStatusDTO : deviceOnboardStatusDTOs) {
            this.updateVirtualDeviceHistory(deviceOnboardStatusDTO, deviceOnboardStatusDTO.getDevice_id(), username);
        }
    }


    private void updateVirtualDeviceHistory(DeviceOnboardStatusDTO deviceOnboardStatusDTO, String device_id, String username) {
        log.info("Added updateVirtualDeviceHistory : {} ", deviceOnboardStatusDTO);

        if (deviceOnboardStatusDTO.getImage_status() != null) {
            if (deviceOnboardStatusDTO.getImage_status() == 0) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_image", "todo", "");
            } else if (deviceOnboardStatusDTO.getImage_status() == 1) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_image", "completed", "");
            } else if (deviceOnboardStatusDTO.getImage_status() == 2) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_image", "retag", deviceOnboardStatusDTO.getImage_comment());
            } else if (deviceOnboardStatusDTO.getImage_status() == 3) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_image", "exception", deviceOnboardStatusDTO.getImage_comment());
            }
            log.info("Added history for image status:{}", deviceOnboardStatusDTO.getImage_status());

        }

        if (deviceOnboardStatusDTO.getField_status() != null) {
            if (deviceOnboardStatusDTO.getField_status() == 0) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_field", "todo", "");
            } else if (deviceOnboardStatusDTO.getField_status() == 1) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_field", "completed", "");
            } else if (deviceOnboardStatusDTO.getField_status() == 2) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_field", "retag", deviceOnboardStatusDTO.getField_comment());
            } else if (deviceOnboardStatusDTO.getField_status() == 3) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_field", "exception", deviceOnboardStatusDTO.getField_comment());
            }
            log.info("Added history for field status:{}", deviceOnboardStatusDTO.getField_status());

        }

        if (deviceOnboardStatusDTO.getTag_status() != null) {
            if (deviceOnboardStatusDTO.getTag_status() == 0) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_tag", "todo", "");
            } else if (deviceOnboardStatusDTO.getTag_status() == 1) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_tag", "completed", "");
            } else if (deviceOnboardStatusDTO.getTag_status() == 2) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_tag", "retag", deviceOnboardStatusDTO.getTag_comment());
            } else if (deviceOnboardStatusDTO.getTag_status() == 3) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_tag", "exception", deviceOnboardStatusDTO.getTag_comment());
            }
            log.info("Added history for tag status:{}", deviceOnboardStatusDTO.getTag_status());

        }

        if (deviceOnboardStatusDTO.getGeolocation_status() != null) {
            if (deviceOnboardStatusDTO.getGeolocation_status() == 0) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_geolocation", "todo", "");
            } else if (deviceOnboardStatusDTO.getGeolocation_status() == 1) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_geolocation", "completed", "");
            } else if (deviceOnboardStatusDTO.getGeolocation_status() == 2) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_geolocation", "retag", deviceOnboardStatusDTO.getGeolocation_comment());
            } else if (deviceOnboardStatusDTO.getGeolocation_status() == 3) {
                this.updateOnboardAssetHistoryDetails(username, device_id, "asset_onboard_geolocation", "exception", deviceOnboardStatusDTO.getGeolocation_comment());
            }

            log.info("Added history for geolocation status:{}", deviceOnboardStatusDTO.getGeolocation_status());

        }
    }

    private DeviceOnboardStatusDTO updateDeviceOnboardStatus(DeviceDTO deviceDTO) {

        DeviceOnboardStatusDTO deviceOnboardStatusDTO = new DeviceOnboardStatusDTO();

        if (deviceDTO.getAsset_image_url() == null || deviceDTO.getAsset_image_url().equals("[]")) {
            deviceOnboardStatusDTO.setImage_status(0);
        } else {
            deviceOnboardStatusDTO.setImage_status(1);
        }
        if (deviceDTO.getPosition() == null || deviceDTO.getLatitude() == null || deviceDTO.getLongitude() == null) {
            deviceOnboardStatusDTO.setGeolocation_status(0);
        } else {
            deviceOnboardStatusDTO.setGeolocation_status(1);
        }
        if (deviceDTO.getQrcode_count() == null || deviceDTO.getQrcode_count() == 0) {
            deviceOnboardStatusDTO.setTag_status(0);
        } else {
            deviceOnboardStatusDTO.setTag_status(1);
        }

        if ((deviceDTO.getName() == null && deviceDTO.getUser_data_name() == null)
                || (deviceDTO.getVendor() == null && deviceDTO.getUser_data_vendor() == null)
                || (deviceDTO.getModel() == null && deviceDTO.getUser_data_model() == null)) {
            deviceOnboardStatusDTO.setField_status(0);
        } else {
            deviceOnboardStatusDTO.setField_status(1);
        }

        // Derive overall onboard_status — all sub-statuses must be 1 (completed) for full completion
        boolean allCompleted = deviceOnboardStatusDTO.getImage_status() == 1
                && deviceOnboardStatusDTO.getGeolocation_status() == 1
                && deviceOnboardStatusDTO.getTag_status() == 1
                && deviceOnboardStatusDTO.getField_status() == 1;

        deviceOnboardStatusDTO.setOnboard_status(allCompleted ? 3 : 0);
        log.info("status for device id {}: image_status {}, geolocation_status {}, tag_status {}, field_status {}, overall onboard_status {}",
                deviceDTO.getId(), deviceOnboardStatusDTO.getImage_status(), deviceOnboardStatusDTO.getGeolocation_status(),
                deviceOnboardStatusDTO.getTag_status(), deviceOnboardStatusDTO.getField_status(), deviceOnboardStatusDTO.getOnboard_status());

        return deviceOnboardStatusDTO;
    }

    public void updateVirtualDeviceOnboardStatusByAssetMapper(DeviceDTO deviceDTO, String username) {

        log.info("deviceDTO : {} ", deviceDTO);
        DeviceOnboardStatusDTO deviceOnboardStatusDTO;
        String onboardAssetId = this.getOnboardAssetIdByDeviceId(deviceDTO.getId());
        log.info("onboardAssetId : {} ", onboardAssetId);

        if (onboardAssetId != null) {
            log.info("Updating onboard status for device id {} since onboardAssetId is not null", deviceDTO.getId());
            deviceOnboardStatusDTO = this.updateDeviceOnboardStatusNotNull(deviceDTO);
            log.info("deviceOnboardStatusDTO after updateDeviceOnboardStatusNotNull: {} ", deviceOnboardStatusDTO);
            deviceOnboardStatusDTO.setDevice_id(deviceDTO.getId());
            deviceOnboardStatusDTO.setId(onboardAssetId);
            deviceOnboardStatusRepository.updateOnboardAsset(deviceOnboardStatusDTO.getId(), deviceOnboardStatusDTO.getDevice_id(), deviceOnboardStatusDTO.getAssignee_email(),
                    deviceOnboardStatusDTO.getImage_status(), deviceOnboardStatusDTO.getGeolocation_status(), deviceOnboardStatusDTO.getTag_status(), deviceOnboardStatusDTO.getField_status());
            log.info("Updated onboard status: updateVirtualDeviceOnboardStatusByAssetMapper..");
        } else {
            log.info("Adding onboard status for device id {} since onboardAssetId is null", deviceDTO.getId());
            deviceOnboardStatusDTO = this.updateDeviceOnboardStatus(deviceDTO);
            deviceOnboardStatusDTO.setDevice_id(deviceDTO.getId());
            deviceOnboardStatusDTO.setId(Generators.timeBasedGenerator().generate().toString());
            deviceOnboardStatusRepository.addOnboardAsset(deviceOnboardStatusDTO.getId(), deviceOnboardStatusDTO.getDevice_id(), deviceOnboardStatusDTO.getAssignee_email(),
                    deviceOnboardStatusDTO.getImage_status(), deviceOnboardStatusDTO.getGeolocation_status(), deviceOnboardStatusDTO.getTag_status(), deviceOnboardStatusDTO.getField_status());
        }

        int deviceOnboardStatus;
        log.info("onboard status check............");
        Integer imageStatus =
                deviceOnboardStatusDTO.getImage_status() != null
                        ? deviceOnboardStatusDTO.getImage_status()
                        : deviceDTO.getImage_status();

        Integer geoStatus =
                deviceOnboardStatusDTO.getGeolocation_status() != null
                        ? deviceOnboardStatusDTO.getGeolocation_status()
                        : deviceDTO.getGeolocation_status();

        Integer tagStatus =
                deviceOnboardStatusDTO.getTag_status() != null
                        ? deviceOnboardStatusDTO.getTag_status()
                        : deviceDTO.getTag_status();

        Integer fieldStatus =
                deviceOnboardStatusDTO.getField_status() != null
                        ? deviceOnboardStatusDTO.getField_status()
                        : deviceDTO.getField_status();
        log.info("Derived statuses for device id {}: imageStatus {}, geoStatus {}, tagStatus {}, fieldStatus {}",
                deviceDTO.getId(), imageStatus, geoStatus, tagStatus, fieldStatus);
        if (imageStatus == 1 && geoStatus == 1 && tagStatus == 1 && fieldStatus == 1) {
            deviceOnboardStatus = 3;
        } else {
            deviceOnboardStatus = 0;
        }
        log.info("Updating onboard asset status for device id {} to {}", deviceDTO.getId(), deviceOnboardStatus);
        updateOnboardAssetStatus(Collections.singleton(deviceDTO.getId()), deviceOnboardStatus);

        this.updateVirtualDeviceHistory(deviceOnboardStatusDTO, deviceDTO.getId(), username);

        System.out.println("Added onboard status: updateVirtualDeviceOnboardStatusByAssetMapper..");
    }

    private DeviceOnboardStatusDTO updateDeviceOnboardStatusNotNull(DeviceDTO deviceDTO){
        log.info("updateDeviceOnboardStatusNotNull for device id {} ", deviceDTO.getId());
        DeviceOnboardStatusDTO deviceOnboardStatusDTO = new DeviceOnboardStatusDTO();

        if (deviceDTO.getAsset_image_url() != null && !deviceDTO.getAsset_image_url().equalsIgnoreCase("[]")){
            deviceOnboardStatusDTO.setImage_status(1);
        }
        log.info("Image status set to {} for device id {} in updateDeviceOnboardStatusNotNull", deviceOnboardStatusDTO.getImage_status(), deviceDTO.getId());
        if (deviceDTO.getPosition() != null || deviceDTO.getLatitude() != null || deviceDTO.getLongitude() != null) {
            deviceOnboardStatusDTO.setGeolocation_status(1);
        }
        log.info("Geolocation status set to {} for device id {} in updateDeviceOnboardStatusNotNull", deviceOnboardStatusDTO.getGeolocation_status(), deviceDTO.getId());
        if (deviceDTO.getQrcode_count() != null && deviceDTO.getQrcode_count() != 0) {
            deviceOnboardStatusDTO.setTag_status(1);
        }
        log.info("Tag status set to {} for device id {} in updateDeviceOnboardStatusNotNull", deviceOnboardStatusDTO.getTag_status(), deviceDTO.getId());

        boolean hasName =
                (deviceDTO.getName() != null && !deviceDTO.getName().isBlank()) ||
                        (deviceDTO.getUser_data_name() != null && !deviceDTO.getUser_data_name().isBlank());

        boolean hasVendor =
                (deviceDTO.getVendor() != null && !deviceDTO.getVendor().isBlank()) ||
                        (deviceDTO.getUser_data_vendor() != null && !deviceDTO.getUser_data_vendor().isBlank());

        boolean hasModel =
                (deviceDTO.getModel() != null && !deviceDTO.getModel().isBlank()) ||
                        (deviceDTO.getUser_data_model() != null && !deviceDTO.getUser_data_model().isBlank());

        if (hasName && hasVendor && hasModel) {
            deviceOnboardStatusDTO.setField_status(1);
        }  else if (deviceDTO.getField_status() != null) {
            deviceOnboardStatusDTO.setField_status(deviceDTO.getField_status());
        }

        log.info("status for device id {}: image_status {}, geolocation_status {}, tag_status {}, field_status {}, overall onboard_status {}",
                deviceDTO.getId(), deviceOnboardStatusDTO.getImage_status(), deviceOnboardStatusDTO.getGeolocation_status(),
                deviceOnboardStatusDTO.getTag_status(), deviceOnboardStatusDTO.getField_status(), deviceOnboardStatusDTO.getOnboard_status());

        return deviceOnboardStatusDTO;
    }


    private void updateDeviceOnboardStatusByAutoDiscovered(DeviceMonitorDTO deviceMonitorDTO, String username) {

        System.out.println("heree id : " + deviceMonitorDTO.getId());
        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setId(deviceMonitorDTO.getId());

        DeviceOnboardStatusDTO deviceOnboardStatusDTO = this.updateDeviceOnboardStatus(deviceDTO);

        deviceOnboardStatusDTO.setId(Generators.timeBasedGenerator().generate().toString());
        deviceOnboardStatusRepository.addOnboardAsset(deviceOnboardStatusDTO.getId(), deviceDTO.getId(), deviceOnboardStatusDTO.getAssignee_email(),
                deviceOnboardStatusDTO.getImage_status(), deviceOnboardStatusDTO.getGeolocation_status(), deviceOnboardStatusDTO.getTag_status(), deviceOnboardStatusDTO.getField_status());


        int deviceOnboardStatus;

        if (deviceOnboardStatusDTO.getImage_status() == 1 && deviceOnboardStatusDTO.getGeolocation_status() == 1 && deviceOnboardStatusDTO.getTag_status() == 1 && deviceOnboardStatusDTO.getField_status() == 1) {
            deviceOnboardStatus = 3;
        } else {
            deviceOnboardStatus = 0;
        }
        updateOnboardAssetStatus(Collections.singleton(deviceDTO.getId()), deviceOnboardStatus);

        this.updateVirtualDeviceHistory(deviceOnboardStatusDTO, deviceDTO.getId(), username);

        System.out.println("Added onboard status: updateDeviceOnboardStatusByAutoDiscovered..");
    }


    public DeviceDTO updateAssetMatchDetails(String username, String vdmsid, String dockername, JSONObject deviceObject, HttpServletRequest httpServletRequest,String assignee) throws JsonProcessingException {
        log.info("updateAssetMatchDetails, Params: deviceObject: {}, endpoint : {}", deviceObject, httpServletRequest.getRequestURI());
        JSONArray featuresArray = deviceObject.getJSONArray("features");
        String primaryDeviceId = deviceObject.getString("primary_device_id");
        JSONObject deviceDetails = deviceObject.getJSONObject("device");
        ObjectMapper objectMapper = new ObjectMapper();
        DeviceDTO deviceDTO = objectMapper.readValue(deviceDetails.toJSONString(), DeviceDTO.class);
        Set<String> deleteDevices = utils.getJSONArrayFromJSONStringForSet(deviceObject.getJSONObject("merge_devices").getJSONArray("delete_devices").toJSONString(), String.class);
        Set<String> retainDevices = utils.getJSONArrayFromJSONStringForSet(deviceObject.getJSONObject("merge_devices").getJSONArray("retain_devices").toJSONString(), String.class);
        for (int i = 0; i < featuresArray.size(); i++) {

            JSONObject featuresFormat = featuresArray.getJSONObject(i);
            String existingDeviceId = featuresFormat.getString("device_id");

            if (existingDeviceId != null) {

                if (featuresFormat.getString("type").equals("procedures")) {
                    this.updateProcedureDeviceId(primaryDeviceId, existingDeviceId, retainDevices);

                } else if (featuresFormat.getString("type").equals("device_position")) {
                    DeviceDTO existingDeviceDTO = deviceRepository.getDeviceByDeviceId(existingDeviceId);
                    deviceDTO.setLocation_id(existingDeviceDTO.getLocation_id());
                    this.updateDeviceCoordinatesAndLocationId(existingDeviceDTO.getLatitude(), existingDeviceDTO.getLongitude(), existingDeviceDTO.getPosition(), primaryDeviceId, existingDeviceDTO.getLocation_id());

                } else if (featuresFormat.getString("type").equals("asset_images")) {
                    DeviceDTO existingDeviceDTO = deviceRepository.getDeviceByDeviceId(existingDeviceId);
                    this.updateAssetImagesDetails(username, vdmsid, existingDeviceDTO.getAsset_image_url(), primaryDeviceId);

                } else if (featuresFormat.getString("type").equals("sensors")) {
                    this.updateSensorsDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
                } else if (featuresFormat.getString("type").equals("integrations")) {
                    this.updateIntegrationsDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
                } else if (featuresFormat.getString("type").equals("media")) {
                    this.updateMediaDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
                } else if (featuresFormat.getString("type").equals("documents")) {
                    this.updateDocumentsDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
                } else if (featuresFormat.getString("type").equals("siemens")) {
                    this.updateSiemensDeviceId(primaryDeviceId, existingDeviceId);
                }


            } else if (featuresFormat.containsKey("device_ids")) {
                Set<String> deviceIds = utils.getJSONArrayFromJSONStringForSet(featuresFormat.getJSONArray("device_ids").toJSONString(), String.class);
                if (deviceIds != null) {
                    if (featuresFormat.getString("type").equals("history")) {
                        this.updateHistoryDeviceId(primaryDeviceId, deviceIds);
                    }
                }
            }
        }
        if (deviceDetails.containsKey("virtual_device_type")) {
            try {
                this.editVirtualDeviceByVirtualDeviceId(username, vdmsid, dockername, Collections.singleton(deviceDTO), httpServletRequest);
                log.info("this is virtual device updated: {}" + deviceDTO.getDisplay_name());

            } catch (IOException e) {
                System.out.println(e);
            }
        } else {
            try {
                this.editDeviceByDeviceID(username, vdmsid, dockername, deviceDTO.getId(), deviceDTO, httpServletRequest,assignee);
                log.info("this is device updated: {}" + deviceDTO.getDisplay_name());
            } catch (IOException e) {
                System.out.println(e);
            } catch (JSONException e) {
                System.out.println(e);
            }
        }
        try {

            this.deleteDevicesById(username, vdmsid, dockername, deleteDevices, httpServletRequest,assignee);
        } catch (Exception e) {
            log.error("Exception. Params: deviceObject: {}, endpoint : {}", deviceObject, httpServletRequest.getRequestURI(), e);
        }

        try {
            this.syncSingleDeviceOnboardStatus(vdmsid, primaryDeviceId);
        } catch (Exception e) {
            log.error("Exception. Params: deviceObject: {}, endpoint : {}", deviceObject, httpServletRequest.getRequestURI(), e);
        }

        return this.getDeviceByDeviceId(username, vdmsid, dockername, primaryDeviceId);

    }


    public void updateSiemensDeviceId(String primaryDeviceId, String existingDeviceId) {
        siemensService.updateSiemensDeviceId(primaryDeviceId, existingDeviceId);
    }

    private void updateDeviceCoordinatesAndLocationId(String latitude, String longitude, String position, String device_id, String location_id) {
        deviceRepository.updateDeviceCoordinatesAndLocationId(device_id, latitude, longitude, position, location_id);
    }

    public void updateProcedureDeviceId(String primaryDeviceId, String existingDeviceId, Set<String> retainDevices) {
        globalInspectionRecordService.updateGlobalInspectionByDeviceId(primaryDeviceId, existingDeviceId);
        globalChecklistService.updateDeviceGlobalChecklistDeviceId(primaryDeviceId, existingDeviceId);
        recordChecklistService.updateRecordChecklistByDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
    }

    public void updateIntegrationsDeviceId(String primaryDeviceId, String existingDeviceId, Set<String> retainDevices) {
        bacnetService.updateBacnetObjectDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
        daintreeService.updateDaintreeDeviceByDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
        disruptiveService.updateDisruptiveSensorDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
        ecobeeService.updateEcobeeSensorDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
        knxService.updateKnxGroupDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
        lorawanService.updateLorawanSensorDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
        monnitService.updateMonnitSensorDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
        myDevicesService.updateMyDevicesSensorDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
        pelicanService.updatePelicanSensorDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
//        siemensService.updateSiemensAssetDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
        snmpService.updateSnmpObjectDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
        polyLensService.updatePolyLensDeviceId(primaryDeviceId, existingDeviceId, retainDevices);

    }

    public void updateSensorsDeviceId(String primaryDeviceId, String existingDeviceId, Set<String> retainDevices) {
        measuringInstrumentService.updateMeasuringInstrumentDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
    }

    private void updateAssetImagesDetails(String username, String vdmsid, String asset_image_urls, String deviceId) {
        List<String> updated_asset_image_urls = utils.getJSONArrayFromJSONString(asset_image_urls, String.class);
        System.out.println("images url :" + updated_asset_image_urls);
        if (updated_asset_image_urls != null && updated_asset_image_urls.size() > 0) {
            this.upsertAssetImagesFromUrl(username, vdmsid, Collections.singletonList(deviceId), updated_asset_image_urls);
        }
    }

    public void upsertAssetImagesFromUrl(String username, String
            vdms_id, List<String> device_ids, List<String> asset_images) {

        for (String device_id : device_ids) {
            JSONArray array = new JSONArray();
            String action = "ADD";
            DeviceDTO deviceDTO = this.getDeviceAndOnboardStatusByDeviceId(device_id);
            String device_name = deviceDTO.getUser_data_name() == null || deviceDTO.getUser_data_name().equals("") ? deviceDTO.getDisplay_name() : deviceDTO.getUser_data_name();
            LocationDTO locationDTO = null;
            try {
                if (deviceDTO.getAsset_image_url() != null) {
                    array = JSON.parseArray(deviceDTO.getAsset_image_url());
                    action = "UPDATE";
                }
                if (device_ids.size() > 1) {
                    for (int i = 0; i < array.size(); i++) {
                        utils.removeFileFromServerByImageURL(array.getString(i), server_asset_images_absolute_path);
                        array.clear();
                    }
                }

                for (String asset_image : asset_images) {
                    try {
                        byte[] imageBytes = utils.getBytesArrayByImageUrl(asset_image);
                        if (imageBytes.length > 0) {
                            //get file path
                            String asset_image_url = utils.addFileToServer(imageBytes, server_asset_images_absolute_path, device_id, utils.getFileExtensionByFileUrl(asset_image), server_asset_images_url);
                            if (asset_image_url != null) {
                                array.add(asset_image_url);
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (array.size() > 0) {
                    deviceRepository.updateAssetImage(device_id, array.toJSONString());
                    if (deviceDTO.getOnboard_data() != null) {
                        deviceDTO.getOnboard_data().setImage_status(1);
                        this.updateAssetOnboardData(username, vdms_id, device_id, deviceDTO.getOnboard_data(), deviceDTO.getOnboard_status());
                    }
                }
                if (deviceDTO.getLocation_id() != null) {
                    locationDTO = locationService.getLocationByLocationId(deviceDTO.getLocation_id());
                }
                if (action.equals("ADD")) {
                    userActionLogService.addUserAction(username, "asset", action, "Asset images are added for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_image", deviceDTO.getId());
                } else if (action.equals("UPDATE")) {
                    userActionLogService.addUserAction(username, "asset", action, "Asset images are updated for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "success", "asset_image", deviceDTO.getId());
                }
            } catch (RuntimeException e) {
                if (action.equals("ADD")) {
                    userActionLogService.addUserAction(username, "asset", action, "Unable to add Asset images for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_image", deviceDTO.getId());
                } else if (action.equals("UPDATE")) {
                    userActionLogService.addUserAction(username, "asset", action, "Unable to update Asset images for Device name: " + device_name + " and id: " + deviceDTO.getId() + (deviceDTO.getLocation_id() != null && locationDTO.getName() != null ? ", Location id: " + deviceDTO.getLocation_id() + ", Location name: " + locationDTO.getName() : ""), "failed", "asset_image", deviceDTO.getId());
                }

            }
        }
    }

    public void updateMediaDeviceId(String primaryDeviceId, String existingDeviceId, Set<String> retainDevices) {
        mediaService.updateMediaDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
    }

    public void updateDocumentsDeviceId(String primaryDeviceId, String existingDeviceId, Set<String> retainDevices) {
        documentService.updateDocumentDeviceId(primaryDeviceId, existingDeviceId, retainDevices);
    }

    private void updateHistoryDeviceId(String primaryDeviceId, Set<String> existingDeviceIds) {
        for (String existingDeviceId : existingDeviceIds) {
            historyService.updateHistoryDeviceId(primaryDeviceId, existingDeviceId);
        }
    }

    public List<DeviceDTO> getAllDeviceByVdmsIdAndDockerNameWithoutPagination(String username, String vdmsid, String dockername) {
        System.out.println(vdmsid + " " + dockername);
        return deviceRepository.getAllDeviceByVdmsIdAndDockerNameWithoutPagination(vdmsid, dockername);
    }

    public void upsertDigitalTwin(String username, String vdmsid, TagDeviceOrLocationDTO filterObject, HttpServletRequest httpServletRequest) {
        log.info("upsertDigitalTwin, Params: filterObject: {}, endpoint : {}", filterObject, httpServletRequest.getRequestURI());

        Integer select_all_status = filterObject.getSelect_all_status();
        JSONObject general_object = filterObject.getGeneral_object();
        Set<MeasuringInstrumentDTO> measuringInstrumentDTOS = new HashSet<>();
        Set<DeviceDTO> devices = utils.getJSONArrayFromJSONStringForSet(general_object.getJSONArray("devices").toJSONString(), DeviceDTO.class);
        Set<MeasuringInstrumentDTO> deleted_measuring_instruments = utils.getJSONArrayFromJSONStringForSet(general_object.getJSONArray("deleted_measuring_instruments").toJSONString(), MeasuringInstrumentDTO.class);
        List<UserActionLogDTO> userActionLogDTOS = new ArrayList<>();
        Set<String> device_ids = new HashSet<>();

        String shareMethod = general_object.getString("share_method");

        if (general_object != null) {
            if (select_all_status == 0) {
                measuringInstrumentDTOS = utils.getJSONArrayFromJSONStringForSet(general_object.getJSONArray("measuring_instruments").toJSONString(), MeasuringInstrumentDTO.class);
            }

            device_ids = devices.stream()
                    .filter(dto -> dto.getId() != null)
                    .map(DeviceDTO::getId)
                    .collect(Collectors.toSet());


            if (measuringInstrumentDTOS.size() > 0) {

                if (shareMethod != null && shareMethod.equals("replace")) {
                    for (String device_id : device_ids) {
                        measuringInstrumentService.deleteInstrumentsByDeviceId(username, vdmsid, device_id, httpServletRequest);
                    }
                }

                if (shareMethod != null && shareMethod.equals("edit") && !deleted_measuring_instruments.isEmpty()) {

                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.execute(() -> {
                        try {
                            log.info("Deleting measuring instruments on edit:  {}", deleted_measuring_instruments.size());
                            measuringInstrumentService.deleteInstrumentById(username, vdmsid, deleted_measuring_instruments, httpServletRequest);
                        } catch (Exception ex) {
                            log.info("Error during deleting measuring instruments on edit:  {}", ex.getMessage(), ex);
                        }
                    });
                    executorService.shutdown();

                }


                if (shareMethod != null && (shareMethod.equals("add") || shareMethod.equals("replace") || shareMethod.equals("edit"))) {
                    for (MeasuringInstrumentDTO instrument : measuringInstrumentDTOS) {
                        for (String device_id : device_ids) {
                            UserActionLogDTO userActionLogDTO = new UserActionLogDTO();
                            try {
                                if (instrument.getMeasuring_entity() == null) {
                                    instrument.setMeasuring_entity("device");
                                }

                                instrument.setDevice_id(device_id);

                                userActionLogDTO.setEmail(username);
                                userActionLogDTO.setType("sensors");
                                userActionLogDTO.setAction("UPDATE");
                                userActionLogDTO.setSub_type("sensors_info");
                                userActionLogDTO.setPrimary_id(instrument.getId());
                                userActionLogDTO.setStatus("success");
                                userActionLogDTO.setMessage(" Update measuring instrument: " + instrument.getName() + " with  id: " + instrument.getId() + " for device with id: " + instrument.getDevice_id());
                                if (instrument.getId() == null) {
                                    instrument.setId(Generators.timeBasedGenerator().generate().toString());
                                    userActionLogDTO.setAction("ADD");
                                    userActionLogDTO.setMessage(" Added measuring instrument: " + instrument.getName() + " with  id: " + instrument.getId() + " for device with id: " + instrument.getDevice_id());
                                    log.info(" Added measuring instrument: {}" + instrument.getName());

                                }

                                measuringInstrumentService.upsertMeasuringInstrument(instrument);

                                measuringInstrumentService.updateMeasuringInstrumentValueByFormula(instrument.getId(), instrument.getValue_changed_status());

                            } catch (Exception e) {
                                userActionLogDTO.setStatus("failed");
                                userActionLogDTO.setMessage(" Unable to add measuring instrument: " + instrument.getName() + " with  id: " + instrument.getId() + " for device with id: " + instrument.getDevice_id());
                                log.error("Exception. Params: filterObject: {}, endpoint : {}", filterObject, httpServletRequest.getRequestURI(), e);

                            }

                            userActionLogDTOS.add(userActionLogDTO);
                        }
                    }
                }
            }
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Set<String> finalDevice_ids = device_ids;
        executorService.execute(() -> {
            System.out.println("Executor started now ");
            measuringInstrumentService.updateDeviceMeasureCountByDeviceIds(finalDevice_ids);
            this.updateDigitalTwinImageUrlByIds(devices);

            // new Digital Twin IOC Changes
            iocService.sendDigitalTwinData(finalDevice_ids);
            userActionLogService.batchUpdateUserActionLogs(userActionLogDTOS);
        });
        executorService.shutdown();

    }

    public void updateDigitalTwinImageUrlByIds(Set<DeviceDTO> deviceDTOS) {
        for (DeviceDTO device : deviceDTOS) {
            this.updateDigitalTwinImageUrlById(device.getDigital_twin_image_url(), device.getId());
        }
    }

    private void updateDigitalTwinImageUrlById(String digital_twin_image_url, String device_id) {
        deviceRepository.updateDigitalTwinImageUrlById(digital_twin_image_url, device_id);
    }

    public void deleteDigitalTwin(String username, String vdmsid, String device_id, HttpServletRequest httpServletRequest) {
        log.info("deleteDigitalTwin, Params: device id: {}, endpoint : {}", device_id, httpServletRequest.getRequestURI());
        String imageUrl = deviceRepository.getDigitalTwinImageUrl(device_id);
        measuringInstrumentService.deleteDigitalTwinPositions(device_id);
        deviceRepository.deleteDigitalTwinImageUrl(device_id);
        log.info("imageUrl : {}", imageUrl);
        apiCallService.deleteDigitalTwinImageUrl(Collections.singleton(imageUrl), username, vdmsid);

        // new Digital Twin Changes
        iocService.sendDigitalTwinData(new HashSet<>(Collections.singleton(device_id)));
    }

    public void multiEditDigitalTwin(String username, String vdmsid, String data, String image_url, MultipartFile multipartFile, HttpServletRequest httpServletRequest) throws JsonProcessingException {

        log.info("multiEditDigitalTwin, Params: data: {}, image url: {}, multipartFile: {}, endpoint : {}", data, image_url, multipartFile, httpServletRequest.getRequestURI());
        ObjectMapper objectMapper = new ObjectMapper();
        TagDeviceOrLocationDTO filterObject = objectMapper.readValue(data, TagDeviceOrLocationDTO.class);
        Integer select_all_status = filterObject.getSelect_all_status();
        JSONObject general_object = filterObject.getGeneral_object();

        Set<String> device_ids = utils.getJSONArrayFromJSONStringForSet(general_object.getJSONArray("device_ids").toJSONString(), String.class);

        JSONArray imageUrls = webClientService.multiEditDigitalTwin(vdmsid, multipartFile, image_url, device_ids);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            List<UserActionLogDTO> userActionLogDTOS = new ArrayList<>();
            Set<MeasuringInstrumentDTO> measuringInstrumentDTOS = new HashSet<>();
            String shareMethod = general_object.getString("share_method");

            if (imageUrls != null && imageUrls.size() > 0) {
                Set<DeviceDTO> devices = new HashSet<>();
                Set<String> final_ids = new HashSet<>();

                log.info("imageUrl : {}", imageUrls);
                for (int i = 0; i < imageUrls.size(); i++) {
                    DeviceDTO deviceDTO = new DeviceDTO();
                    JSONObject jsonObject = imageUrls.getJSONObject(i);

                    log.info("imageUrl jsonObject : {}", jsonObject);
                    String deviceId = jsonObject.getString("device_id").replaceAll("[\\[\\]]", "");
                    String digitalTwinImageUrl = jsonObject.getString("digital_twin_image_url");
                    log.info("Device ID : {}", deviceId);
                    log.info("Digital Twin Image URL: {}", digitalTwinImageUrl);
                    deviceDTO.setId(deviceId);
                    deviceDTO.setDigital_twin_image_url(digitalTwinImageUrl);
                    devices.add(deviceDTO);
                    final_ids.add(deviceId);
                }


                log.info("Final devices : {}", devices);
                log.info("Executor started now ");

                this.updateDigitalTwinImageUrlByIds(devices);

                // new Digital Twin Changes

                iocService.sendDigitalTwinData(final_ids);

                log.info("Digital twin url added.");

                log.info("final device ids : {}" + final_ids);
                if (general_object != null) {
                    if (select_all_status == 0) {
                        measuringInstrumentDTOS = utils.getJSONArrayFromJSONStringForSet(general_object.getJSONArray("measuring_instruments").toJSONString(), MeasuringInstrumentDTO.class);
                    }

                    if (!measuringInstrumentDTOS.isEmpty()) {

                        if (shareMethod != null && shareMethod.equals("replace")) {
                            for (String device_id : final_ids) {
                                measuringInstrumentService.deleteInstrumentsByDeviceId(username, vdmsid, device_id, httpServletRequest);
                            }
                        }

                        if (shareMethod != null && (shareMethod.equals("add") || shareMethod.equals("replace"))) {
                            for (MeasuringInstrumentDTO instrument : measuringInstrumentDTOS) {
                                for (String device_id : final_ids) {
                                    MeasuringInstrumentDTO measuringInstrumentDTO = new MeasuringInstrumentDTO();

                                    String measuring_instrument_id = instrument.getId();

//                                    measuringInstrumentDTO.setId(instrument.getId());
                                    measuringInstrumentDTO.setType(instrument.getType());
                                    measuringInstrumentDTO.setName(instrument.getName());
                                    measuringInstrumentDTO.setDescription(instrument.getDescription());
                                    measuringInstrumentDTO.setCalculation_type(instrument.getCalculation_type());
                                    measuringInstrumentDTO.setMeasuring_instrument_attributes(instrument.getMeasuring_instrument_attributes());
                                    measuringInstrumentDTO.setParameter(instrument.getParameter());
                                    measuringInstrumentDTO.setCategory(instrument.getCategory());
                                    measuringInstrumentDTO.setValue(instrument.getValue());
                                    measuringInstrumentDTO.setUnit(instrument.getUnit());
                                    measuringInstrumentDTO.setTags(instrument.getTags());
                                    measuringInstrumentDTO.setDevice_id(device_id);
                                    measuringInstrumentDTO.setSensor_type(instrument.getSensor_type());
                                    measuringInstrumentDTO.setShow_on_map(instrument.getShow_on_map());
                                    measuringInstrumentDTO.setShow_on_scan(instrument.getShow_on_scan());
                                    measuringInstrumentDTO.setMeasuring_entity(instrument.getMeasuring_entity());
                                    measuringInstrumentDTO.setSub_category(instrument.getSub_category());
                                    measuringInstrumentDTO.setDigital_twin_position(instrument.getDigital_twin_position());
                                    measuringInstrumentDTO.setScale_type(instrument.getScale_type());

                                    log.info("device id : {}", device_id);
                                    UserActionLogDTO userActionLogDTO = new UserActionLogDTO();
                                    try {
                                        if (measuringInstrumentDTO.getMeasuring_entity() == null) {
                                            measuringInstrumentDTO.setMeasuring_entity("device");
                                        }


                                        userActionLogDTO.setEmail(username);
                                        userActionLogDTO.setType("sensors");
                                        userActionLogDTO.setAction("UPDATE");
                                        userActionLogDTO.setSub_type("sensors_info");
                                        userActionLogDTO.setPrimary_id(instrument.getId());
                                        userActionLogDTO.setStatus("success");
                                        log.info("Update measuring instrument: {} with  id: {} for device with id: {}", measuringInstrumentDTO.getName(), measuring_instrument_id, measuringInstrumentDTO.getDevice_id());
                                        userActionLogDTO.setMessage(" Update measuring instrument: " + measuringInstrumentDTO.getName() + " with  id: " + measuring_instrument_id + " for device with id: " + measuringInstrumentDTO.getDevice_id());
                                        if (measuringInstrumentDTO.getId() == null) {
                                            measuring_instrument_id = Generators.timeBasedGenerator().generate().toString();
                                            userActionLogDTO.setAction("ADD");
                                            log.info(" Added measuring instrument: {}  with  id: {} for device with id: {} ", measuringInstrumentDTO.getName(), measuring_instrument_id, measuringInstrumentDTO.getDevice_id());
                                            userActionLogDTO.setMessage(" Added measuring instrument: " + measuringInstrumentDTO.getName() + " with  id: " + measuring_instrument_id + " for device with id: " + measuringInstrumentDTO.getDevice_id());
                                        }

                                        measuringInstrumentService.upsertMeasuringInstrumentForMultiDigitaltwin(measuringInstrumentDTO, measuring_instrument_id);

                                        measuringInstrumentService.updateMeasuringInstrumentValueByFormula(measuring_instrument_id, measuringInstrumentDTO.getValue_changed_status());

                                    } catch (Exception e) {
                                        userActionLogDTO.setStatus("failed");
                                        log.error("Exception. Params: data: {}, image url: {}, multipartFile: {}, endpoint : {}", data, image_url, multipartFile, httpServletRequest.getRequestURI(), e);
                                        userActionLogDTO.setMessage(" Unable to add measuring instrument: " + measuringInstrumentDTO.getName() + " with  id: " + instrument.getId() + " for device with id: " + measuringInstrumentDTO.getDevice_id());
                                    }

                                    userActionLogDTOS.add(userActionLogDTO);
                                }
                            }
                        }
                    }
                }

                measuringInstrumentService.updateDeviceMeasureCountByDeviceIds(final_ids);
                log.info("Instruments count updated");
                userActionLogService.batchUpdateUserActionLogs(userActionLogDTOS);

                log.info("Logs added");
            }

        });
        executorService.shutdown();
    }


    public void exportFilteredMeasuringInstrument(HttpServletResponse response, String username, String vdmsid, String dockername, String condition,
                                                  Integer pageno, Integer pagesize,
                                                  JSONObject search_sort_filter_details, Integer onboard_status) {
        Set<DeviceDTO> devices = deviceSearchService.multipleKeywordSearchSortFilterDevices(username, vdmsid, dockername, condition, pageno, pagesize, search_sort_filter_details, onboard_status);
        try {
            this.generateExcelForMeasuringInstruments(username, vdmsid, devices, response);
        } catch (IOException e) {
            System.out.println(e);
        }

    }

    public void generateExcelForMeasuringInstruments(String username, String vdmsid, Set<DeviceDTO> devices, HttpServletResponse response) throws IOException {

        VdmsDTO vdmsDetails = vdmsService.getVDMSDetails();
        String timeZoneId = vdmsDetails.getTimezone();
        String propertyName = vdmsDetails.getProperty_name();

        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";


        String modified_filename = utils.replaceSlashCharactersWithHyphen(propertyName);

        String filename = modified_filename + "_" + "AssetListAdvancedExport" + "_";

        System.out.println("------- File name --------" + filename);

        String currentDateTime = utils.getCurrentDateByTimezone(BigInteger.valueOf(System.currentTimeMillis()), timeZoneId);

        String headerValue = "attachment; filename=" + filename + currentDateTime + ".xlsx";

        response.setHeader(headerKey, headerValue);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Asset Data Advanced Export");
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);

        CellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);

        int rowNum = 0;
        int rowNum1 = 1;
        int rowNum3 = 2;
        int instrumentColumnCount = 0;
        boolean flag = false;

        // bold font for headers
        XSSFCellStyle headingStyle = workbook.createCellStyle();
        headingStyle.setAlignment(HorizontalAlignment.CENTER);
        headingStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont font = workbook.createFont();
        font.setBold(true);
        headingStyle.setFont(font);

        //headers
        Row headerRow = sheet.createRow(rowNum);
        headerRow.setRowStyle(boldStyle);
        createCell(headerRow, 0, "Asset Id", headingStyle);
        createCell(headerRow, 1, "Asset Name", headingStyle);
        createCell(headerRow, 2, "Network Name", headingStyle);
        createCell(headerRow, 3, "Corrigo Id", headingStyle);
        createCell(headerRow, 4, "Building Name", headingStyle);
        createCell(headerRow, 5, "Floor Name", headingStyle);
        createCell(headerRow, 6, "Location Name", headingStyle);
        createCell(headerRow, 7, "Model", headingStyle);
        createCell(headerRow, 8, "Vendor", headingStyle);
        createCell(headerRow, 9, "Type", headingStyle);
        createCell(headerRow, 10, "Asset Group", headingStyle);
        createCell(headerRow, 11, "Category", headingStyle);
        createCell(headerRow, 12, "Sub Category", headingStyle);
        createCell(headerRow, 13, "Instruments", headingStyle);
        createCell(headerRow, 20, "Attributes", headingStyle);

        Row headerRow1 = sheet.createRow(rowNum1);
        createCell(headerRow1, 13, "Name", headingStyle);
        createCell(headerRow1, 14, "Category", headingStyle);
        createCell(headerRow1, 15, "Sub-Category", headingStyle);
        createCell(headerRow1, 16, "Value", headingStyle);
        createCell(headerRow1, 17, "Unit", headingStyle);
        createCell(headerRow1, 18, "Entity", headingStyle);
        createCell(headerRow1, 19, "Location", headingStyle);
        createCell(headerRow1, 20, "Name", headingStyle);
        createCell(headerRow1, 21, "Type", headingStyle);
        createCell(headerRow1, 22, "Protocol", headingStyle);
        createCell(headerRow1, 23, "Value", headingStyle);
        createCell(headerRow1, 24, "Primary Id", headingStyle);
        createCell(headerRow1, 25, "Secondary Id", headingStyle);
        createCell(headerRow1, 26, "Tertiary Id", headingStyle);



        //Auto-size columns 0-12 (heading)
        for (int j = 0; j < 13; j++) {
            sheet.autoSizeColumn(j);
        }

        // Merging header cells
        mergeCells(0, 0, 13, 19, sheet);
        mergeCells(0, 0, 20, 26, sheet);
        for (int i = 0; i < 13; i++) {
            mergeCells(0, 1, i, i, sheet);
        }

        // adding of values
        for (DeviceDTO device : devices){

            Row row = sheet.createRow(++rowNum1);
            createCell(row, 0, device.getId(), style);
            createCell(row, 1, device.getUser_data_name() != null ? device.getUser_data_name() : device.getDisplay_name(), style);
            createCell(row, 2, device.getDocker_name(), style);

            String corrigoId = "";
            if (!(device.getCustom_fields() == null)) {
                JSONArray customFieldsArray = JSONArray.parseArray(device.getCustom_fields());
                for (int i = 0; i < customFieldsArray.size(); i++) {
                    JSONObject customField = customFieldsArray.getJSONObject(i);
                    if (customField.containsKey("Corrigo_ID")) {
                        corrigoId = customField.getString("Corrigo_ID");
                        break;
                    }
                }
            }
            createCell(row, 3, corrigoId, style);
            Set<MeasuringInstrumentDTO> instruments = measuringInstrumentService.getInstrumentsByDeviceId(username, vdmsid, device.getId());
            createCell(row, 4, device.getBuilding(), style);
            createCell(row, 5, device.getFloor(), style);
            createCell(row, 6, device.getLocation(), style);
            createCell(row, 7, device.getUser_data_model(), style);
            createCell(row, 8, device.getUser_data_vendor(), style);
            createCell(row, 9, device.getType(), style);
            createCell(row, 10, device.getAsset_group(), style);
            createCell(row, 11, device.getCategory(), style);
            createCell(row, 12, device.getSub_category(), style);

            //Auto-size columns 0-12 (data)
            for (int j = 0; j < 13; j++) {
                sheet.autoSizeColumn(j);
            }

            if (instruments != null && !instruments.isEmpty()) {
                for (MeasuringInstrumentDTO instrument : instruments) {
                    createCell(row, 13, instrument.getName(), style);
                    createCell(row, 14, instrument.getCategory(), style);
                    createCell(row, 15, instrument.getSub_category(), style);
                    createCell(row, 16, instrument.getValue() != null ? instrument.getValue() : "", style);
                    createCell(row, 17, instrument.getUnit() != null ? instrument.getUnit() : "", style);
                    createCell(row, 18, instrument.getMeasuring_entity(), style);
                    createCell(row, 19, instrument.getLocation(), style);
                    //AUto-size columns 13-19
//                    for (int i = 13; i < 20; i++) {
//                        sheet.autoSizeColumn(i);
//                    }
                    Integer ArraySize = instrument.getMeasuring_instrument_attributes().size();
                    instrumentColumnCount = rowNum1;
                    for (MeasuringInstrumentAttributesDTO attribute : instrument.getMeasuring_instrument_attributes()) {
                        createCell(row, 20, attribute.getName() != null ? attribute.getName() : "", style);
                        createCell(row, 21, attribute.getType() != null ? attribute.getType() : "", style);
                        createCell(row, 22, attribute.getProtocol() != null ? attribute.getProtocol() : "", style);
                        createCell(row, 23, attribute.getValue() != null ? attribute.getValue() : "", style);
                        createCell(row, 24, attribute.getPrimary_id() != null ? attribute.getPrimary_id() : "", style);
                        createCell(row, 25, attribute.getSecondary_id() != null ? attribute.getSecondary_id() : "", style);
                        createCell(row, 26, attribute.getTertiary_id() != null ? attribute.getTertiary_id() : "", style);

                        // Auto-size columns 20-26
//                        for (int j = 20; j < 27; j++) {
//                            sheet.autoSizeColumn(j);
//                        }
                        row = sheet.createRow(++rowNum1);
                    }


                    // Merge cells of instruments w.r.t attributes
                    for (int i = 13; i < 20 && ArraySize > 1; i++) {
                        flag = true;
                        mergeCells(instrumentColumnCount, instrumentColumnCount + ArraySize - 1, i, i, sheet);
                    }
                }
                rowNum1--;
                // Merge cells of assets w.r.t instruments
                for (int i = 0; i < 13 && (flag || instruments.size() > 1); i++) {
                    mergeCells(rowNum3, rowNum1, i, i, sheet);
                }
                rowNum3 = rowNum1 + 1;
            } else {
                rowNum3 = rowNum1 + 1;
            }
            flag = false;
        }

        for (int i = 0; i < 27; i++) {
            sheet.autoSizeColumn(i);
        }

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    public void updateDevicePolyLensCountByDeviceId(String deviceId) {
        try {
            if (deviceId != null) {
                Integer polyLensDeviceCount = polyLensService.getPolyLensDeviceCountByDeviceId(deviceId);
                deviceRepository.updatePolyLensDeviceCount(deviceId, polyLensDeviceCount);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {

        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    public void updateDeviceMqttDeviceCountByDeviceId(String deviceId) {
        try {
            if (deviceId != null) {
                Integer mqttDeviceCount = mqttService.getMqttDeviceCountByDeviceId(deviceId);
                deviceRepository.updateMqttDeviceDeviceCount(deviceId, mqttDeviceCount);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void simpleAssetExportReport(HttpServletResponse response, String file_type, Set<DeviceDTO> devices, Boolean includeImages, String email, String modified_filename, String currentDateTime, String vdmsid) throws IOException {
        if (email.isEmpty()) {

            if (file_type.equals("excel")) {
                this.generateSimpleAssetExportReportExcel(devices, includeImages, response, email, modified_filename, currentDateTime);
            } else if (file_type.equals("pdf")) {
                this.generateSimpleAssetExportReportPDF(devices, includeImages, response, email, modified_filename, currentDateTime);
            }
        } else {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                try {
                    if (file_type.equals("excel")) {
                        byte[] bytes = this.generateSimpleAssetExportReportExcel(devices, includeImages, response, email, modified_filename, currentDateTime);
                        this.sendAssetExportEmail(email, "Basic Asset Export", modified_filename + "_" + "AssetList" + "_" + currentDateTime, bytes, vdmsid);
                    } else if (file_type.equals("pdf")) {
                        byte[] bytes = this.generateSimpleAssetExportReportPDF(devices, includeImages, response, email, modified_filename, currentDateTime);
                        this.sendAssetPDFExportEmail(email, "Basic Asset Export", modified_filename + "_" + "AssetList" + "_" + currentDateTime, bytes, vdmsid);
                    }
                    System.out.println("-------------------------------------------------------------------------");
                    log.info("Sent Simple Report to: {} " + email);
                    System.out.println("-------------------------------------------------------------------------");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            executorService.shutdown();
        }

    }


    public byte[] generateSimpleAssetExportReportPDF(Set<DeviceDTO> devices, Boolean includeImages, HttpServletResponse response, String email, String modified_filename, String currentDateTime) throws IOException {

        VdmsDTO vdmsDetails = vdmsService.getVDMSDetails();
        String vdmsId = vdmsDetails.getId();
        String timeZoneId = vdmsDetails.getTimezone();
        String propertyName = vdmsDetails.getProperty_name();
        ObjectMapper objectMapper = new ObjectMapper();
        String object = objectMapper.writeValueAsString(devices);

        JSONArray jsonArray = JSON.parseArray(object);

        List<List<HashMap<String, String>>> mainList = new ArrayList<>();
        for (Object obj : jsonArray) {

            if (obj instanceof JSONObject) {
                List<HashMap<String, String>> combinedList = new ArrayList<>();
                HashMap<String, String> staticMap = new HashMap<>();
                JSONObject jsonObject = (JSONObject) obj;
                log.info("Devices Object {}", jsonObject);
                staticMap.put("ID", Objects.requireNonNullElse(jsonObject.getString("id"), ""));
                String userDataName = Objects.requireNonNullElse(jsonObject.getString("user_data_name"), "");
                staticMap.put("Asset Name", !userDataName.equals("") ? userDataName : Objects.requireNonNullElse(jsonObject.getString("display_name"), ""));

                String userDataModel = Objects.requireNonNullElse(jsonObject.getString("user_data_model"), "");
                staticMap.put("Model", !userDataModel.equals("") ? userDataModel : Objects.requireNonNullElse(jsonObject.getString("model"), ""));

                String userDataVendor = Objects.requireNonNullElse(jsonObject.getString("user_data_vendor"), "");
                staticMap.put("Vendor", !userDataVendor.equals("") ? userDataVendor : Objects.requireNonNullElse(jsonObject.getString("vendor"), ""));


                staticMap.put("Network", Objects.requireNonNullElse(jsonObject.getString("docker_name"), ""));
                staticMap.put("Serial Number", Objects.requireNonNullElse(jsonObject.getString("serial_number"), ""));

                if (jsonObject.getString("created_timestamp") != null) {

                    String date = utils.getCurrentDateByTimezone(jsonObject.getBigInteger("created_timestamp"), timeZoneId);
                    staticMap.put("Created Date", date);
                } else {
                    staticMap.put("Created Date", "");
                }


                if (jsonObject.getString("created_email") != null) {
                    staticMap.put("Created By", Objects.requireNonNullElse(jsonObject.getString("created_email"), ""));
                }

                staticMap.put("Description", Objects.requireNonNullElse(jsonObject.getString("description"), ""));

                staticMap.put("Asset Type", Objects.requireNonNullElse(jsonObject.getString("type"), ""));

                JSONObject onboardData = jsonObject.getJSONObject("onboard_data");

                if (onboardData != null && onboardData.getInteger("tag_status") != null) {
                    int tag_status = onboardData.getInteger("tag_status");
                    if (tag_status == 0) {
                        staticMap.put("QR Code Status", "Not Completed");
                    } else if (tag_status == 1) {
                        staticMap.put("QR Code Status", "Completed");
                    } else if (tag_status == 2) {
                        staticMap.put("QR Code Status", "Re-tag");
                    } else if (tag_status == 3) {
                        staticMap.put("QR Code Status", "Exception");
                    } else {
                        staticMap.put("QR Code Status", "");
                    }
                }

                if (onboardData != null && onboardData.getInteger("geolocation_status") != null) {
                    int geolocation_status = onboardData.getInteger("geolocation_status");
                    if (geolocation_status == 0) {
                        staticMap.put("Geo Location Status", "Not Completed");
                    } else if (geolocation_status == 1) {
                        staticMap.put("Geo Location Status", "Completed");
                    } else if (geolocation_status == 2) {
                        staticMap.put("Geo Location Status", "Re-tag");
                    } else if (geolocation_status == 3) {
                        staticMap.put("Geo Location Status", "Exception");
                    } else {
                        staticMap.put("Geo Location Status", "");
                    }
                }

                String locationId = jsonObject.getString("location_id");
                if (locationId != null) {
                    String loc = Objects.requireNonNullElse(jsonObject.getString("location"), "") + ", " +
                            Objects.requireNonNullElse(jsonObject.getString("floor"), "") + ", " +
                            Objects.requireNonNullElse(jsonObject.getString("building"), "");
                    staticMap.put("Location", loc);
                } else {
                    staticMap.put("Location", "");
                }

                staticMap.put("VDMS ID", Objects.requireNonNullElse(jsonObject.getString("vdms_id"), ""));

//                if (jsonObject.containsKey("asset_image_url")) {
//                    staticMap.put("Asset Images", Objects.requireNonNullElse(jsonObject.getString("asset_image_url"), ""));
//                }

                // Modification to have image optionally
                if (includeImages) {
                    if (jsonObject.containsKey("asset_image_url")) {
                        staticMap.put("Asset Images", Objects.requireNonNullElse(jsonObject.getString("asset_image_url"), ""));
                    }
                } else {
                    if (onboardData != null && onboardData.getInteger("image_status") != null) {
                        int image_status = onboardData.getInteger("image_status");
                        if (image_status == 0) {
                            staticMap.put("Image Status", "Not Completed");
                        } else if (image_status == 1) {
                            staticMap.put("Image Status", "Completed");
                        } else if (image_status == 2) {
                            staticMap.put("Image Status", "Re-tag");
                        } else if (image_status == 3) {
                            staticMap.put("Image Status", "Exception");
                        } else {
                            staticMap.put("Image Status", "");
                        }
                    }
                }


                if (jsonObject.containsKey("custom_fields")) {
                    HashMap<String, String> customMap = new HashMap<>();
                    JSONArray customFieldsArray = JSON.parseArray(jsonObject.getString("custom_fields"));

                    for (Object customFieldObj : customFieldsArray) {
                        if (customFieldObj instanceof JSONObject) {
                            JSONObject customField = (JSONObject) customFieldObj;
                            for (String key : customField.keySet()) {
                                String modifiedKey = this.convertUnderscoresToSpaces(key);
                                customMap.put(modifiedKey, customField.getString(key));
                            }
                        }
                    }
                    combinedList.add(staticMap);
                    combinedList.add(customMap);
                } else {
                    combinedList.add(staticMap); // Add staticMap even if customMap is empty
                }
                mainList.add(combinedList);
            }
        }
        return this.generateSimpleAssetExportReportPDFData(mainList, propertyName, includeImages, vdmsId, response, email, modified_filename, currentDateTime);
    }

    public byte[] generateSimpleAssetExportReportPDFData(List<List<HashMap<String, String>>> mainList, String propertyName, Boolean includeImages, String vdmsId, HttpServletResponse response, String email, String modified_filename, String currentDateTime) throws IOException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] bytes = null;
        try {

            Document document = new Document(PageSize.A4);
            PdfWriter writer;

            if (email.isEmpty()) {
                writer = PdfWriter.getInstance(document, response.getOutputStream());
            } else {
                writer = PdfWriter.getInstance(document, stream);
            }

            document.open();
            document.setMargins(0, 0, 40, 20);

            com.itextpdf.text.Font headingFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 12, BaseColor.BLACK);
            com.itextpdf.text.Font sideHeadingFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 10, new BaseColor(28, 32, 135));
            com.itextpdf.text.Font contentFont = FontFactory.getFont(FontFactory.COURIER, 10, BaseColor.BLACK);


            PdfPTable outerTable1 = new PdfPTable(1);
            outerTable1.getDefaultCell().setBorder(0);
            outerTable1.setSplitLate(false);
            outerTable1.addCell(new Phrase(propertyName + "(" + vdmsId + ")", headingFont));

            PdfPCell dummyCell = new PdfPCell();
            dummyCell.setBorder(Rectangle.NO_BORDER);
            dummyCell.setFixedHeight(5);

            outerTable1.addCell(dummyCell);
            outerTable1.addCell(dummyCell);
            document.add(outerTable1);

            int assetNumber = 1;
            for (List<HashMap<String, String>> sublist : mainList) {
                System.out.println("SUBLIST" + sublist);
                HashMap<String, String> staticMap = sublist.get(0);

                PdfPTable outerTable = new PdfPTable(1);
                outerTable.getDefaultCell().setBorder(0);
                outerTable.setSpacingAfter(100);
                outerTable.setHorizontalAlignment(Element.ALIGN_CENTER);
                outerTable.setSplitLate(false); // table split issue

                // Add Asset Name as a side heading
                outerTable.addCell(new Phrase(assetNumber++ + "." + "Asset Name: " + staticMap.get("Asset Name"), sideHeadingFont));

                outerTable.addCell(dummyCell);
                outerTable.addCell(dummyCell);

                PdfPTable table1 = new PdfPTable(4);
                table1.setHorizontalAlignment(Element.ALIGN_CENTER);
                // Adding header cells with centered content
                table1.addCell(createCenteredCell("Model", sideHeadingFont));
                table1.addCell(createCenteredCell("Vendor", sideHeadingFont));
                table1.addCell(createCenteredCell("Network", sideHeadingFont));
                table1.addCell(createCenteredCell("Serial Number", sideHeadingFont));

                table1.addCell(createCenteredCell(getValueOrDefault(staticMap.get("Model")), contentFont));
                table1.addCell(createCenteredCell(getValueOrDefault(staticMap.get("Vendor")), contentFont));
                table1.addCell(createCenteredCell(getValueOrDefault(staticMap.get("Network")), contentFont));
                table1.addCell(createCenteredCell(getValueOrDefault(staticMap.get("Serial Number")), contentFont));

                outerTable.addCell(table1);
                outerTable.addCell(dummyCell);

                PdfPTable table2 = new PdfPTable(5);
                table2.setHorizontalAlignment(Element.ALIGN_CENTER);

                table2.addCell(createCenteredCell("Location", sideHeadingFont));
                table2.addCell(createCenteredCell("Asset Type", sideHeadingFont));
                table2.addCell(createCenteredCell("QR Code Status", sideHeadingFont));
                table2.addCell(createCenteredCell("Geo Location Status", sideHeadingFont));
                table2.addCell(createCenteredCell("Image Status", sideHeadingFont));

                table2.addCell(createCenteredCell(getValueOrDefault(staticMap.get("Location")), contentFont));
                table2.addCell(createCenteredCell(getValueOrDefault(staticMap.get("Asset Type")), contentFont));
                table2.addCell(createCenteredCell(getValueOrDefault(staticMap.get("QR Code Status")), contentFont));
                table2.addCell(createCenteredCell(getValueOrDefault(staticMap.get("Geo Location Status")), contentFont));
                table2.addCell(createCenteredCell(getValueOrDefault(staticMap.get("Image Status")), contentFont));

                outerTable.addCell(table2);
                outerTable.addCell(dummyCell);
                PdfPTable table3 = new PdfPTable(4);
                table2.setHorizontalAlignment(Element.ALIGN_CENTER);

                table3.addCell(createCenteredCell("Created Date", sideHeadingFont));
                table3.addCell(createCenteredCell(getValueOrDefault(staticMap.get("Created Date")), contentFont));
                table3.addCell(createCenteredCell("Created By", sideHeadingFont));
                table3.addCell(createCenteredCell(getValueOrDefault(staticMap.get("Created By")), contentFont));

                outerTable.addCell(table3);
                outerTable.addCell(dummyCell);

                PdfPTable descriptionTable = new PdfPTable(2);
                descriptionTable.setWidths(new float[]{1, 3});

                PdfPCell textCell = new PdfPCell(new Phrase("Description", sideHeadingFont));
                textCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                descriptionTable.addCell(textCell);

                descriptionTable.addCell(new Phrase(getValueOrDefault(staticMap.get("Description")), contentFont));

                outerTable.addCell(descriptionTable);
                outerTable.addCell(dummyCell);

                if (sublist.size() >= 2) {
                    HashMap<String, String> customMap = sublist.get(1);


                    if (!customMap.isEmpty()) {
                        outerTable.addCell(new Phrase("Custom Fields : ", sideHeadingFont));
                        PdfPTable customTable = new PdfPTable(2);
                        customTable.addCell(new Phrase("Custom Key", sideHeadingFont));
                        customTable.addCell(new Phrase("Custom Value", sideHeadingFont));
                        for (String key : customMap.keySet()) {
                            customTable.addCell(new Phrase(key, contentFont));
                            customTable.addCell(new Phrase(getValueOrDefault(customMap.get(key)), contentFont));
                        }
                        outerTable.addCell(customTable);
                    }
                }
                outerTable.addCell(dummyCell);

                //Image toggle optional
                if (includeImages) {
                    if (staticMap.containsKey("Asset Images") && staticMap.get("Asset Images") != null) {
                        outerTable.addCell(new Phrase("Asset Images : ", sideHeadingFont));
                        System.out.println("Asset image present ");
                        String assetImageUrl = staticMap.get("Asset Images");
                        System.out.println("Asset Image Url: " + assetImageUrl);
                        JSONArray imageUrls = JSONArray.parseArray(assetImageUrl);
                        PdfPTable imageTable = new PdfPTable(2);
                        imageTable.setSplitLate(false);

                        if (imageUrls.size() > 0) {
                            for (int i = 0; i < Math.min(imageUrls.size(), 2); i++) {
                                try {
                                    System.out.println(imageUrls.getString(i));
                                    log.info("Asset image ", imageUrls.getString(i));
                                    Image image = Image.getInstance(new URL(imageUrls.getString(i)));
                                    float originalWidth = image.getScaledWidth() * 0.3f;
                                    float originalHeight = image.getScaledHeight() * 0.3f;

                                    image.scaleToFit(originalWidth, originalHeight);

                                    PdfPCell imageCell = new PdfPCell(image);
                                    imageCell.setBorder(Rectangle.BOX);
                                    imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    imageCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                                    imageCell.setPadding(10f);
                                    imageTable.addCell(imageCell);

                                    if (imageUrls.size() == 1) {
                                        PdfPCell noImgCell = new PdfPCell(new Phrase("No Image Available", contentFont));
                                        noImgCell.setBorder(Rectangle.BOX);
                                        noImgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                        noImgCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                                        noImgCell.setPadding(10f);
                                        imageTable.addCell(noImgCell);
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error loading image: " + e.getMessage());
                                }
                                outerTable.addCell(imageTable);
                            }
                        }
                    }
                }
                document.add(outerTable);
                document.newPage();

            }

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }

        if (email.isEmpty()) {
            // Set the response headers
            String filename = modified_filename + "_" + "AssetList" + "_";
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=" + filename + currentDateTime + ".pdf";
            System.out.println("--------- header value -------" + headerValue);
            response.setContentType("application/pdf");
            response.setHeader(headerKey, headerValue);

            // Write the PDF bytes to the response output stream
            response.getOutputStream().flush();
        } else {
            bytes = stream.toByteArray();
        }

        return bytes;
    }

    public String getValueOrDefault(String value) {
        return (value == null || value.isEmpty()) ? " - " : value;
    }

    public PdfPCell createCenteredCell(String text, com.itextpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER); // Center horizontally
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE); // Center vertically (optional)
        return cell;
    }

    public String convertUnderscoresToSpaces(String input) {
        if (input == null) {
            return null; // Handle null input
        }
        return input.replace("_", " "); // Replace underscores with spaces
    }

    public void sendAssetPDFExportEmail(String email, String reportName, String filename, byte[] bytes, String vdmsid) {
        JSONObject body = new JSONObject();
        body.put("template_type", "download_email");
        body.put("email", email);
        body.put("subject", reportName + " Download link!!");
        body.put("report_name", reportName);
        body.put("file_name", filename);
        MultipartFile file = new ConvertByteArrayToMultipartFile(bytes, filename + ".pdf", filename, null);
        alertService.sendDownloadEmail(body, file, "pdf", vdmsid);
    }

    public void updateAllRecordChecklistStatusInBatchForDevice(List<DeviceDTO> updatedDeviceStatus) {
        log.info("updateAllRecordChecklistStatusInBatchForDevice");
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatementUpdate = connection.prepareStatement(deviceQueryRepository.getQueryForUpdateDeviceRecordChecklistStatus());
            int batchCounter = 0;
            int maxBatchLimit = 200;
            for (DeviceDTO deviceDTO : updatedDeviceStatus) {
                log.info("Device Id : {}", deviceDTO.getDevice_id());
                log.info("RecordChecklist count : {}", deviceDTO.getRecord_checklist_count());
                log.info("RecordChecklist status : {}", deviceDTO.getRecord_checklist_status());
                try {
                    preparedStatementUpdate.setString(1, deviceDTO.getRecord_checklist_status());
                    preparedStatementUpdate.setInt(2, deviceDTO.getRecord_checklist_count());
                    preparedStatementUpdate.setString(3, deviceDTO.getDevice_id());
                    preparedStatementUpdate.addBatch();
                    batchCounter++;
                    if (batchCounter == maxBatchLimit) {
                        preparedStatementUpdate.executeBatch();
                        log.info("added 100 devices in a batch");
                        preparedStatementUpdate.clearBatch();
                        batchCounter = 0;
                    }

                } catch (Exception e) {
                    log.error("Exception in batch update of device record checklist status", e);
                }
            }
            if (batchCounter > 0) {
                preparedStatementUpdate.executeBatch();
                log.info("Excecuted batch update of: {} device record checklist status ", batchCounter);
            }
            preparedStatementUpdate.close();
        } catch (Exception e) {
            log.error("Exception in batch update of device record checklist status", e);

        }

    }

    public Set<DeviceDTO> getDeviceDetailsByIdList(Set<String> device_ids) {
        return deviceRepository.getDeviceDetailsByIdList(device_ids);
    }

    public void syncDeviceOnboardStatusForQrSync(String vdmsId, Set<QrCodeDTO> existingQrCodes, Set<QrCodeDTO> updatedQrCodes,
                                                 Set<QrCodeDTO> existingClientQrCodes, Set<QrCodeDTO> updatedClientQrCodes) {

        Set<QrCodeDTO> existingAllQrCodes = new HashSet<>();
        existingAllQrCodes.addAll(existingQrCodes);
        existingAllQrCodes.addAll(existingClientQrCodes);

        Set<QrCodeDTO> updatedAllQrCodes = new HashSet<>();
        updatedAllQrCodes.addAll(updatedQrCodes);
        updatedAllQrCodes.addAll(updatedClientQrCodes);


        for (QrCodeDTO qrCodeDTO : updatedAllQrCodes) {
            Set<DeviceDTO> deviceDTOS = new HashSet<>();
            log.info("New Details For qrcode : {} and device id :{} location id :{}", qrCodeDTO.getId(), qrCodeDTO.getDeviceId(), qrCodeDTO.getLocationId());
            for (QrCodeDTO existingQrcodeDTO : existingAllQrCodes) {
                if (existingQrcodeDTO.getId().equals(qrCodeDTO.getId())) {
                    log.info("Existing Details for qrcode : {} and device id :{} location id :{}", existingQrcodeDTO.getId(), existingQrcodeDTO.getDeviceId(), existingQrcodeDTO.getLocationId());
                    if (!existingQrcodeDTO.getDeviceId().equals(qrCodeDTO.getDeviceId())) {
                        log.info("Retag from old device.. and update status");
                        DeviceDTO deviceDTO = this.getDeviceDetailsForOnboard(existingQrcodeDTO.getDeviceId());

                        int field_status = deviceDTO.getField_status();
                        int image_status = deviceDTO.getImage_status();
                        int tag_status = deviceDTO.getTag_status();
                        int geolocation_status = deviceDTO.getGeolocation_status();

                        log.info("Before retag onboard details..");
                        log.info("Image : {}", image_status);
                        log.info("Field_status : {}", field_status);
                        log.info("Geolocation_status : {}", geolocation_status);
                        log.info("Tag_status : {}", tag_status);

                        switch (deviceDTO.getTag_status()) {
                            case 1:
                                tag_status = 0;
                                break;
                            case 3:
                                tag_status = 3;
                                break;
                            case 2:
                                tag_status = 2;
                                break;
                        }

                        log.info("After retag onboard details..");
                        log.info("Image : {}", image_status);
                        log.info("Field_status : {}", field_status);
                        log.info("Geolocation_status : {}", geolocation_status);
                        log.info("Tag_status : {}", tag_status);

                        log.info("all existing qrcode...." + existingAllQrCodes);
                        log.info("exixting size" + qrCodeRepository.countByDeviceId(existingQrcodeDTO.getDeviceId()));

                        log.info("all existing qrcode...." + existingAllQrCodes);
                        log.info("exixting size" + clientQrCodeRepository.countByDeviceId(existingQrcodeDTO.getDeviceId()));

                        long addallqrcodecount = (qrCodeRepository.countByDeviceId(existingQrcodeDTO.getDeviceId()) + clientQrCodeRepository.countByDeviceId(existingQrcodeDTO.getDeviceId()));

//If Qrcode is already there for same device then dont change the tagstatus
                        if (addallqrcodecount == 0) {
                            if (deviceDTO.getOnboard_data() != null) {
                                deviceOnboardStatusRepository.updateAssetOnboardData(deviceDTO.getId(), image_status, geolocation_status, tag_status, field_status);
                                log.info("Updated existing onboard asset data for device ID: {}", deviceDTO.getId());
                            } else {
                                String id = Generators.timeBasedGenerator().generate().toString();
                                deviceOnboardStatusRepository.addOnboardAsset(id, deviceDTO.getId(), null, image_status, geolocation_status, tag_status, field_status);
                                log.info("Added new onboard asset entry with ID: {} for device ID: {}", id, deviceDTO.getId());
                            }


                            deviceDTO.getOnboard_data().setTag_status(tag_status);
                            if (image_status == 0 || field_status == 0 || geolocation_status == 0 || tag_status == 0) {
                                System.out.println("Updating status.......");
                                if (deviceDTO.getOnboard_status() != null) {

                                    if (deviceDTO.getOnboard_status() == 3) {
                                        log.info("Updating status.......from 3 to 0");
                                        this.updateOnboardAssetStatus(Collections.singleton(deviceDTO.getId()), 0);
                                        deviceDTO.setOnboard_status(0);
                                    } else if (deviceDTO.getOnboard_status() == 2) {
                                        log.info("Updating status.......from 2 to 1");
                                        this.updateOnboardAssetStatus(Collections.singleton(deviceDTO.getId()), 1);
                                        deviceDTO.setOnboard_status(1);
                                    }
                                }
                            }
                        } else {
                            log.warn("Skipping tag_status update. Device ID {} bzc it has other qr code tagged", existingQrcodeDTO.getDeviceId());
                            deviceDTOS.add(deviceDTO);
                        }

//                        //If clientQrcode is already there for same device then dont change the tagstatus
//                        if (clientQrCodeRepository.countByDeviceId(qrCodeDTO.getDeviceId())==0) {
//                            if (deviceDTO.getOnboard_data() != null) {
//                                deviceOnboardStatusRepository.updateAssetOnboardData(deviceDTO.getId(), image_status, geolocation_status, tag_status, field_status);
//                                log.info("Updated existing onboard asset data for device ID: {}", deviceDTO.getId());
//                            } else {
//                                String id = Generators.timeBasedGenerator().generate().toString();
//                                deviceOnboardStatusRepository.addOnboardAsset(id, deviceDTO.getId(), null, image_status, geolocation_status, tag_status, field_status);
//                                log.info("Added new onboard asset entry with ID: {} for device ID: {}", id, deviceDTO.getId());
//                            }
//
//
//                            deviceDTO.getOnboard_data().setTag_status(tag_status);
//                            if (image_status == 0 || field_status == 0 || geolocation_status == 0 || tag_status == 0) {
//                                System.out.println("Updating status.......");
//                                if (deviceDTO.getOnboard_status() != null) {
//
//                                    if (deviceDTO.getOnboard_status() == 3) {
//                                        log.info("Updating status.......from 3 to 0");
//                                        this.updateOnboardAssetStatus(Collections.singleton(deviceDTO.getId()), 0);
//                                        deviceDTO.setOnboard_status(0);
//                                    } else if (deviceDTO.getOnboard_status() == 2) {
//                                        log.info("Updating status.......from 2 to 1");
//                                        this.updateOnboardAssetStatus(Collections.singleton(deviceDTO.getId()), 1);
//                                        deviceDTO.setOnboard_status(1);
//                                    }
//                                }
//                            }
//                        }else {
//                            log.warn("Skipping tag_status update. Device ID {} bzc it has other client qr code tagged", existingQrcodeDTO.getDeviceId());
//                            deviceDTOS.add(deviceDTO);
//                        }


                        this.updateOnboardAssetHistoryDetailsWithTimestamp(qrCodeDTO.getUpdatedBy(), existingQrcodeDTO.getDeviceId(),
                                "asset_onboard_tag", "untag", "QrCode is untagged and tagged to other device", BigInteger.valueOf(new BigDecimal(qrCodeDTO.getUpdatedTime()).longValue()));

                        log.info("Logged 'untag' action in onboard history for QR code ID: {} from device ID: {} by user: {} at time: {}", qrCodeDTO.getId(), existingQrcodeDTO.getDeviceId(), qrCodeDTO.getUpdatedBy(), qrCodeDTO.getUpdatedTime());

                        deviceDTOS.add(deviceDTO);
                    }

                }
            }


            log.info("Tag qrcode  device.. and update status");
            DeviceDTO deviceDTO1 = this.getDeviceDetailsForOnboard(qrCodeDTO.getDeviceId());

            int field_status = deviceDTO1.getField_status();
            int image_status = deviceDTO1.getImage_status();
            int tag_status = deviceDTO1.getTag_status();
            int geolocation_status = deviceDTO1.getGeolocation_status();

            log.info("Before tag onboard details..");
            log.info("Image : {}", image_status);
            log.info("Field_status : {}", field_status);
            log.info("Geolocation_status : {}", geolocation_status);
            log.info("Tag_status : {}", tag_status);

            switch (deviceDTO1.getTag_status()) {
                case 0:
                    tag_status = 1;
                    break;
                case 1:
                case 3:
                    tag_status = 1;
                    break;
                case 2:
                    BigInteger maxUpdatedQrCodeTimeStamp = qrCodeService.getMaxUpdatedQrCodeTimeStamp(deviceDTO1.getId());
                    BigInteger maxUpdatedClientQrCodeTimeStamp = clientQrCodeService.maxUpdatedClientQrCodeTimeStamp(deviceDTO1.getId());
                    BigInteger maxTimestamp = Utils.getMaxTimeStamp(maxUpdatedQrCodeTimeStamp, maxUpdatedClientQrCodeTimeStamp);
                    if (maxTimestamp != null && Utils.isLessThan10Minutes(maxTimestamp)) {
                        tag_status = 1;
                    } else {
                        tag_status = 2;
                    }
                    break;
            }


            log.info("After tag onboard details..");
            log.info("Image : {}", image_status);
            log.info("Field_status : {}", field_status);
            log.info("Geolocation_status : {}", geolocation_status);
            log.info("Tag_status : {}", tag_status);

            if (deviceDTO1.getOnboard_data() != null) {
                deviceOnboardStatusRepository.updateAssetOnboardData(deviceDTO1.getId(), image_status, geolocation_status, tag_status, field_status);
                log.info("Updated onboard asset data for device ID: {} during QR code tagging.", deviceDTO1.getId());
            } else {
                String id = Generators.timeBasedGenerator().generate().toString();
                deviceOnboardStatusRepository.addOnboardAsset(id, deviceDTO1.getId(), null, image_status, geolocation_status, tag_status, field_status);
                log.info("Added new onboard asset record with ID: {} for device ID: {} during QR code tagging.", id, deviceDTO1.getId());
            }

            this.updateOnboardAssetHistoryDetailsWithTimestamp(qrCodeDTO.getUpdatedBy(), qrCodeDTO.getDeviceId(),
                    "asset_onboard_tag", "tag", "", BigInteger.valueOf(new BigDecimal(qrCodeDTO.getUpdatedTime()).longValue()));
            log.info("Logged 'tag' action in onboard history for QR code ID: {} to device ID: {} by user: {} at time: {}", qrCodeDTO.getId(), qrCodeDTO.getDeviceId(), qrCodeDTO.getUpdatedBy(), qrCodeDTO.getUpdatedTime());

            deviceDTO1.getOnboard_data().setTag_status(tag_status);

            if ((image_status == 1 || image_status == 3) && (field_status == 1 || field_status == 3)
                    && (geolocation_status == 1 || geolocation_status == 3) && (tag_status == 1 || tag_status == 3)) {
                if (deviceDTO1.getOnboard_status() != null) {
                    if (deviceDTO1.getOnboard_status() == 0) {
                        log.info("Updating status.......from 0 to 3");
                        this.updateOnboardAssetStatus(Collections.singleton(deviceDTO1.getId()), 3);
                        deviceDTO1.setOnboard_status(3);
                    } else if (deviceDTO1.getOnboard_status() == 1) {
                        log.info("Updating status.......from 1 to 2");
                        this.updateOnboardAssetStatus(Collections.singleton(deviceDTO1.getId()), 2);
                        deviceDTO1.setOnboard_status(2);
                    }
                }
            }

            deviceDTOS.add(deviceDTO1);
            //send topic
            System.out.println("--------- Sending device tag status update via WebSocket ---------");
            socketservice.socketDeviceUpdate(deviceDTOS);
        }
    }

    public DeviceDTO getDeviceDetailsForOnboard(String deviceId) {
        DeviceDTO device = deviceRepository.getDeviceByDeviceIdNew(deviceId);
        if (device != null) {
            device.setOnboard_data(new DeviceOnboardStatusDTO(device.getAssignee_email(), device.getImage_status(), device.getGeolocation_status(), device.getTag_status(), device.getField_status(), deviceOnboardStatusAssigneeService.getDeviceOnboardStatusAssignees(device.getDevice_onboard_status_id())));
        }
        return device;
    }

    private void updateOnboardAssetHistoryDetailsWithTimestamp(String username, String device_id, String sub_type, String
            status, String alert_message, BigInteger timestamp) {
        HistoryDTO history = new HistoryDTO();
        history.setCreated_email(username);
        history.setType(18);
        history.setStatus(status);
        history.setDevice_id(device_id);
        history.setSub_type(sub_type);
        history.setAlert_message(alert_message);
        history.setTimestamp(timestamp);
        historyService.addHistoryWithTimestamp(history);

        log.info("Method: updateOnboardAssetHistoryDetailsWithTimestamp. Added onboard history:{}", history);

    }

    public Integer getAssetCount() {
        Integer assetCount = deviceRepository.getAllDeviceCount();
        if (assetCount == null) {
            log.info("Asset count is null, returning default value 0");
            return 0;
        }
        return assetCount;
    }


    public ResponseDTO updateDeviceTypeForAll(String vdmsId) {

        log.info("updateDeviceTypeForAll started for vdmsId: {}", vdmsId);
        try {
            deviceTypeService.syncAndUpsertDeviceTypes(vdmsId);
            List<DeviceTypesDTO> deviceTypesDTOS = deviceTypesRepository.getAllDeviceTypes();
            List<String> deviceTypes = deviceRepository.getAllUniqueDeviceTypes();
            log.info("Device Types: " + deviceTypes);
            Pattern pattern = Pattern.compile("^(\\d{4})\\d*[-. ]?(.+)$");
            for (String type : deviceTypes) {
                if (type == null || type.isBlank()) {
                    deviceRepository.setTypeGeneric();
                    continue;
                }
                Matcher matcher = pattern.matcher(type);
                if (matcher.find()) {
                    String code = matcher.group(1);
                    String label = JacksCodeMapping.getLabelByCode(code);
                    if (label != null) {
                        log.info("Updating device type: " + label + " for code: " + code + " with type: " + type);
                        deviceRepository.updateTypeByType(label, code);
                    }
                }


                String normalizedAssetType = type.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

                String matched = deviceTypesDTOS.stream()
                        .map(DeviceTypesDTO::getName)
                        .filter(Objects::nonNull)
                        .filter(name -> name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase().equals(normalizedAssetType))
                        .findFirst()
                        .orElse(null);


                if (matched == null || matched.isEmpty()) {
                    deviceRepository.updateDeviceType(type, "generic");
                } else {
                    deviceRepository.updateDeviceType(type, matched);
                }
            }
            assetMapperService.updateDeviceTypeForAllAsset(deviceTypesDTOS);
            log.info("updateDeviceTypeForAll completed for vdmsId: {}", vdmsId);
            return new ResponseDTO("Success Device types updated successfully", 200, null, true, new BigInteger(String.valueOf(System.currentTimeMillis())));
        } catch (Exception ex) {
            log.info("Error during async sync :  {}", ex.getMessage(), ex);
            return new ResponseDTO("Failed to update device types", 500, null, false, new BigInteger(String.valueOf(System.currentTimeMillis())));
        }

    }

    private String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    public void updateDeviceType(String type, DeviceDTO deviceDTO) {
        Pattern pattern = Pattern.compile("^(\\d{4})\\d*[-. ]?(.+)$");
        Matcher matcher = pattern.matcher(type);
        if (matcher.find()) {
            String code = matcher.group(1);
            String label = JacksCodeMapping.getLabelByCode(code);

            if (label != null) {
                deviceDTO.setType(label);
            }
        }
    }

    public DeviceDTO getDeviceByMeasuringInstrumentId(String measuringInstrumentId) {
        return deviceRepository.getDeviceByMeasuringInstrumentId(measuringInstrumentId);
    }

    public List<DeviceDTO> browseAiCallFlowDevicesWithSearch(String username, String vdmsid, String dockername, Integer pageno, Integer pagesize, String searchkey) {
        Integer offset = pagesize * (pageno - 1);
        String sanitizedSearchKey = searchkey.replaceAll("[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]", "").toLowerCase();
        return deviceRepository.browseAiCallFlowDevicesWithSearch(vdmsid, dockername, offset, pagesize, sanitizedSearchKey);
    }


    public void toggleDndStatus(String device_id, Boolean is_dnd_enabled, HttpServletRequest httpServletRequest) {
        deviceRepository.toggleDndStatus(device_id, is_dnd_enabled);
    }

    public String getDeviceNameById(String device_id) {
        return deviceRepository.getDeviceNameById(device_id);
    }

    public DeviceDTO getDeviceById(String deviceId) {
        return deviceRepository.getDeviceById(deviceId);
    }


    public DeviceDTO getDeviceInfoFromDb(String deviceId) {
        return deviceRepository.getDeviceInfoFromDb(deviceId);
    }

    public Set<DeviceDTO> upsertInventoryDevices(Set<DeviceDTO> deviceDTOS, String vdmsId, String email) {

        Set<DeviceDTO> inventoryDeviceDTOS = new HashSet<>();
        Set<String> device_ids = new HashSet<>();
        Set<DeviceDTO> virtual_devices;

        for (DeviceDTO virtualDevice : deviceDTOS) {

            String final_device_id = virtualDevice.getDevice_id();
            DeviceDTO existingDeviceDTO = null;

            try {

                if (virtualDevice.getDevice_id() == null) {

                    String device_id = vdmsId + "_" + virtualDevice.getDocker_name() + "_" + virtualDevice.getUser_data_name() + "_"
                            + System.currentTimeMillis();
                    final_device_id = utils.replaceSpecialCharactersWithUnderscore(device_id);

                    deviceRepository.addVirtualDeviceFromInventory(final_device_id,
                            virtualDevice.getUser_data_name(), virtualDevice.getMonitor(), virtualDevice.getDocker_name(), vdmsId,
                            virtualDevice.getWarranty(), virtualDevice.getVirtual_device_type(),
                            virtualDevice.getSerial_number(),
                            virtualDevice.getAsset_match_status(), virtualDevice.getCreated_timestamp(),
                            email, virtualDevice.getAsset_group(), virtualDevice.getAssigned_user_email(), virtualDevice.getMac_address(),
                            virtualDevice.getIp_address(), virtualDevice.getStatus(), virtualDevice.getLast_seen_on(),
                            virtualDevice.getUser_data_model(), virtualDevice.getSubsystem_parent_id());

                    if(virtualDevice.getSerial_number() != null){
                        log.info("serial number is not null and update device specification..");
                        deviceSpecificationService.updateDeviceIdBySerialNumber(virtualDevice.getSerial_number(), final_device_id);
                        deviceInstalledAppsService.updateDeviceIdBySerialNumber(virtualDevice.getSerial_number(), final_device_id);
                        deviceSpecificationService.updateChildDevices(final_device_id,vdmsId,email);
                    }
                    device_ids.add(final_device_id);

                    log.info("device id : {}", final_device_id);
                    userActionLogService.addUserAction(email, "asset", "ADD", "A Virtual Device  name: " + virtualDevice.getUser_data_name() + " and id: " + final_device_id + " is added for network " + virtualDevice.getDocker_name(), "success", "asset_info", final_device_id);
                } else {
                    existingDeviceDTO = getDeviceById(final_device_id);
                    deviceRepository.updateAssignedUserEmail(final_device_id, virtualDevice.getAssigned_user_email());
                    archiveDevicesForInventoryDevice(email, vdmsId, 0, Collections.singleton(final_device_id));
                    userActionLogService.addUserAction(email, "asset", "UPDATE", "A Virtual Device  name: " + virtualDevice.getUser_data_name() + " and id: " + final_device_id + " is added for network " + virtualDevice.getDocker_name(), "success", "asset_info", final_device_id);
                }

                if (virtualDevice.getAsset_group() != null) {
                    DeviceLifecycleHistoryDTO dto = new DeviceLifecycleHistoryDTO();
                    dto.setDevice_id(final_device_id);
                    if(existingDeviceDTO != null){
                        dto.setOperational_status(existingDeviceDTO.getOperational_status());
                    }else {
                        dto.setOperational_status("Working");
                    }

                    dto.setAssigned_user_id(virtualDevice.getAssigned_user_email());
                    dto.setAssigned_by_user_id(email);
                    deviceLifecycleHistoryService.addDeviceHistory(email, vdmsId, dto, null);

                }

                DeviceDTO deviceDTO1 = new DeviceDTO();
                deviceDTO1.setDevice_id(final_device_id);
                deviceDTO1.setInventory_tracking_id(virtualDevice.getInventory_tracking_id());
                inventoryDeviceDTOS.add(deviceDTO1);

            } catch (Exception e) {
                log.error("Exception:upsertInventoryDevices Params: virtualDeviceDto: {}}", deviceDTOS, e);
                userActionLogService.addUserAction(email, "asset", "ADD", "Unable to Add Virtual Device name: " + virtualDevice.getUser_data_name() + " and id: " + final_device_id + " for network " + virtualDevice.getDocker_name(), "failed", "asset_info", final_device_id);

            }

        }

        this.tagInventoryDevices(inventoryDeviceDTOS);

        virtual_devices = this.getDevicesByIdList(vdmsId, device_ids);
        this.updateVirtualDeviceOnboardStatus(virtual_devices, email);

        return inventoryDeviceDTOS;
    }

    public void archiveDevicesForInventoryDevice(String username, String vdmsid, Integer archive, Set<String> deviceIds) {
        log.info("archiveDevicesForInventoryDevice, Params: archive: {}, deviceIds: {}, ", archive, deviceIds);
        int assetMatchStatus = 3;
        String status = "archived";
        for (String deviceId : deviceIds) {
            DeviceDTO device = this.getDeviceByDeviceId(username, vdmsid, null, deviceId);
            if (archive == 0) {
                // Making device as un archive
                String location_id = device.getLocation_id();
                if (location_id != null) {
                    assetMatchStatus = 2;
                } else {
                    assetMatchStatus = 0;
                }
                status = "unarchived";
            }
            try {
                this.updateDeviceAssetStatus(assetMatchStatus, deviceId, username, BigInteger.valueOf(System.currentTimeMillis()));
                log.info("device name : {}", device.getDisplay_name());
                if (archive == 0) {
                    userActionLogService.addUserAction(username, "asset", "UPDATE", "Device name: " + device.getDisplay_name() + " and id: " + device.getId() + " is unarchived", "success", "asset_info", device.getId());
                } else if (archive == 1) {
                    userActionLogService.addUserAction(username, "asset", "UPDATE", "Device name: " + device.getDisplay_name() + " and id: " + device.getId() + " is archived", "success", "asset_info", device.getId());
                }
            } catch (Exception e) {
                log.error("Exception. Params: archive: {}, deviceIds: {}", archive, deviceIds, e);
                if (archive == 0) {
                    userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to unarchive Device name: " + device.getDisplay_name() + " and id: " + device.getId(), "failed", "asset_info", device.getId());
                } else if (archive == 1) {
                    userActionLogService.addUserAction(username, "asset", "UPDATE", "Unable to archive Device name: " + device.getDisplay_name() + " and id: " + device.getId(), "failed", "asset_info", device.getId());
                }
            }

        }
    }

    private void tagInventoryDevices(Set<DeviceDTO> inventoryDeviceDTOS) {
        log.info("tagInventoryDevices........... ");
        for (DeviceDTO inventoryDeviceDTO : inventoryDeviceDTOS) {
            try{
                deviceRepository.tagInventoryDevices(inventoryDeviceDTO.getDevice_id(), inventoryDeviceDTO.getInventory_tracking_id());
            } catch (Exception e) {
                log.error("tagInventoryDevices error..", e);
            }
        }
    }

    public Set<String> listAiEnabledDockers(String  email, String vdmsid, String searchkey) {
        String sanitizedSearchKey = searchkey.replaceAll("[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]", "").toLowerCase();
        return deviceRepository.listAiEnabledDockers(vdmsid, sanitizedSearchKey);
    }

    public void UpdateDeviceDndEnabledAndTimestamp(String id,BigInteger dndTimestamp) {
        deviceRepository.updateDeviceDndEnabledStatus(id);
        deviceRepository.updateDeviceDndTimestamp(id, dndTimestamp);
        deviceRepository.updateSystemDndEnabled(id);
    }

    public void getAiCallDeviceOfflineConditionStatus(String deviceId, Integer status) {
        System.out.println("Came inside getDeviceOfflineConditionStatus" + deviceId + " status: " + status);
        Set<DeviceConditionsDTO> deviceConditions = deviceConditionsService.getDeviceConditionsForAiCall(null, null, null, deviceId);
        for (DeviceConditionsDTO deviceCondition : deviceConditions) {
            System.out.println("Device condition: " + deviceCondition.getAlert_condition());
            if(deviceCondition.getAlert_condition().equals("device_offline_ai_call_alert") && (status != null && status == 0)) {
                System.out.println("Device is offline and condition is device_offline_ai_call_alert");
                if (deviceCondition.getTrigger_time() != null) {
                    ScheduledJobDTO scheduledJobDTO = jobSchedulerService.getScheduledJobByConditionId(deviceCondition.getId());
                    if (scheduledJobDTO == null) {
                        System.out.println("------------ Offline job added ----------------");
                        String jobId=this.scheduleAiCallDeviceAlertJob("add", deviceCondition);
                        if(jobId != null) {

                        } else {
                            System.out.println("Failed to schedule job for device condition: " + deviceCondition.getId());
                        }
                    }
                }
            }

        }
    }

    private String scheduleAiCallDeviceAlertJob(String job_type, DeviceConditionsDTO deviceCondition) {
        System.out.println("Came inside scheduleAiCallDeviceAlertJob : " + deviceCondition.getId() + " job_type: " + job_type);
            ScheduledJobDTO jobSchedulerDTO = new ScheduledJobDTO();
            jobSchedulerDTO.setJob_type(job_type);
            jobSchedulerDTO.setTime_in_seconds(Long.valueOf(deviceCondition.getTrigger_time()));
            String job_id = jobSchedulerService.createScheduledJob(jobSchedulerDTO);
            System.out.println("JOb id: " + job_id);

            if (job_id != null) {
                ScheduledJobDTO scheduledJobDTO = new ScheduledJobDTO();
                scheduledJobDTO.setId(job_id);
                scheduledJobDTO.setCondition_id(deviceCondition.getId());
                scheduledJobDTO.setCondition_type("ai_call_device");
                scheduledJobDTO.setCondition_group("device");
                jobSchedulerService.addScheduledJob(Set.of(scheduledJobDTO));
            }
            return job_id;


    }

    public List<DeviceDTO> getAiCallAndDeviceStatus(String deviceId)
    {
        return deviceRepository.getAiCallAndDeviceStatus(deviceId);
    }

    public void dndCheckAndUpdate() {
        BigInteger currentTimestamp = BigInteger.valueOf(System.currentTimeMillis());
        BigInteger twentyFourHoursInMillis = BigInteger.valueOf(24L * 60 * 60 * 1000);
        //BigInteger threeMinutesInMillis = BigInteger.valueOf(3L * 60 * 1000); // 3 minutes = 180000 ms


        List<DeviceDTO> dndEnabledDevices = deviceRepository.getDndDevices(true);

        for (DeviceDTO deviceDTO : dndEnabledDevices) {
            BigInteger dndEnabledAt = deviceDTO.getDnd_timestamp();

            if (dndEnabledAt != null) {
                BigInteger timeSinceEnabled = currentTimestamp.subtract(dndEnabledAt);

                if (timeSinceEnabled.compareTo(twentyFourHoursInMillis) > 0) {
                    this.updateDeviceDndAndSystemDndStatus(deviceDTO.getId(), false);
                    log.info("Disabled dnd_enabled AND system_dnd_enabled  for device {}", deviceDTO.getId());
                }
            } else {
                log.warn("DND timestamp missing for device {}", deviceDTO.getId());
            }
        }
    }

    public void updateDeviceDndAndSystemDndStatus(String deviceId, Boolean isDndEnabled) {
        deviceRepository.updateDndStatus(deviceId, isDndEnabled);
        deviceRepository.updateSystemDndDisabled(deviceId, isDndEnabled);
        log.info("Updated DND and system DND status for device {}", deviceId);
    }

    public void getDeviceDndAndSystemDndStatus(String deviceId, Boolean isDndEnabled) {
        System.out.println("*****************getDeviceDndAndSystemDndStatus*****"+ deviceId + " isDndEnabled: " + isDndEnabled);
        DeviceDTO deviceDTO=deviceRepository.getDeviceDndAndSystemDndStatus(deviceId, isDndEnabled);
        System.out.println("Device DND status: " + deviceDTO.getIs_dnd_enabled() + ", System DND status: " + deviceDTO.getSystem_dnd_enabled());
        if (deviceDTO.getIs_dnd_enabled() && deviceDTO.getSystem_dnd_enabled()) {
            this.updateDeviceDndAndSystemDndStatus(deviceId,false);
            System.out.println("DND and system DND is enabled for device " + deviceId);
        } else {
            log.info("DND and system DND is disabled for device {}", deviceId);
        }
    }


    public void getAllDeviceDetails(HttpServletResponse response, String username, String vdmsid, Integer pageno, Integer pagesize, String searchKey, JSONObject filterObject) {




//        @NamedNativeQuery(
//                name = "Device.getAllDevices",
//                query = "SELECT  d.id AS device_id ,d.display_name ,d.last_seen_on ,d.model ,d.vendor ,d.user_data_name ,"
//                        + "d.user_data_model ,d.user_data_vendor ,d.docker_name AS network_name ,d.docker_vdms_id AS vdms ,"
//                        + "d.virtual_device_type ,d.warranty ,d.type,d.asset_group FROM device d ",
//                resultSetMapping = "devicedatamapping"
//        )
    }

    public List<DeviceDTO> getAllDeviceCustomDetails(String username, String vdmsid, String docker_name, Integer page_no, Integer page_size, String search_key, String profile_type) {

        log.info("getAllDeviceCustomDetails() started for username: {}, vdmsid: {}, docker_name: {}", username, vdmsid, docker_name);

        List<DeviceDTO> deviceDTOS = new ArrayList<>();

        try {
            JSONObject jsonObject = syslogService.getSyslogExcludeDeviceIds(username, vdmsid, docker_name, profile_type);
            log.info("Syslog response received: {}", jsonObject);

            JSONArray jsonArray = jsonObject.getJSONArray("device_ids");
            log.info("Fetched device_ids array: {}", jsonArray);

            List<String> excludeDeviceIds = new ArrayList<>();

            if (jsonArray == null || jsonArray.isEmpty()) {
                log.info("No device_ids found. Defaulting excludeDeviceIds to ['all'].");
                excludeDeviceIds.add("all");
            } else {
                for (int i = 0; i < jsonArray.size(); i++) {
                    excludeDeviceIds.add(jsonArray.getString(i));
                }
                log.info("ExcludeDeviceIds extracted: {}", excludeDeviceIds);
            }

            log.info("getDevices() called with parameters:");
            log.info("username: {}", username);
            log.info("vdmsid: {}", vdmsid);
            log.info("page_no: {}", page_no);
            log.info("page_size: {}", page_size);
            log.info("search_key: {}", search_key);
            log.info("excludeDeviceIds: {}", excludeDeviceIds);

            if (page_no == 0 || page_size == 0) {
                log.info("Pagination not applied. Fetching all device details.");
                deviceDTOS = deviceRepository.getAllDeviceCustomDetails(search_key, excludeDeviceIds, docker_name);
            } else {
                Integer offset = page_size * (page_no - 1);
                log.info("Pagination applied. page_no: {}, page_size: {}, offset: {}", page_no, page_size, offset);
                deviceDTOS = deviceRepository.getAllDeviceCustomDetailsPaginated(search_key, page_size, offset, excludeDeviceIds, docker_name);
            }

            log.info("getAllDeviceCustomDetails() completed successfully. Total devices fetched: {}",
                    (deviceDTOS != null ? deviceDTOS.size() : 0));

        } catch (Exception e) {
            log.error("Error occurred in getAllDeviceCustomDetails() for username: {}, vdmsid: {}, docker_name: {}", username, vdmsid, docker_name, e);
        }

        return deviceDTOS;
    }


    public List<DeviceDTO> getDeviceCustomDetails(String docker_name, Integer page_no, Integer page_size, String search_key, List<String> device_ids) {

        List<DeviceDTO> deviceDTOS = new ArrayList<>();
        if( page_no == 0 || page_size == 0){
            deviceDTOS = deviceRepository.getDeviceCustomDetails(search_key, device_ids);
        }else{
            Integer offset = page_size * (page_no - 1);
            deviceDTOS = deviceRepository.getDeviceCustomDetailsPaginated(search_key, page_size, offset, device_ids);
        }
        return deviceDTOS;
    }

    public List<DeviceDTO> getDeviceCustomDetailsByIds(
            String username,
            String vdms_id,
            String docker_name,
            Integer has_pagination,
            Integer page_no,
            Integer page_size,
            String search_key,
            JSONObject requestBody) {

        // Extract lists safely
        List<String> deviceIds = getList(requestBody, "device_ids");
        log.info("Received deviceIds: {}", deviceIds);

        List<String> models = getList(requestBody, "models");
        log.info("Received models: {}", models);

        List<String> assetCategory = getList(requestBody, "asset_category");
        log.info("Received assetCategory: {}", assetCategory);

        // Now pass them to repository queries
        List<DeviceDTO> deviceDTOS;

        if (has_pagination == 0 ) {
            log.info(" has_pagination == 0 ");
            deviceDTOS = deviceRepository.getDeviceCustomDetailsByIds(
                    search_key, deviceIds, models, assetCategory
            );
        } else {
            log.info(" has_pagination == 1 ");
            Integer offset = page_size * (page_no - 1);
            deviceDTOS = deviceRepository.getDeviceCustomDetailsByIdsPaginated(
                    search_key, page_size, offset, deviceIds, models, assetCategory
            );

        }
        log.info("deviceDTOS: {}", deviceDTOS);

        return deviceDTOS;
    }

    public List<String> getList(JSONObject obj, String key) {
        if (obj == null || !obj.containsKey(key) || obj.get(key) == null) {
            log.info("Key '{}' missing or null in requestBody. Returning empty list.", key);
            return new ArrayList<>(Arrays.asList("null"));
        }

        JSONArray arr = obj.getJSONArray(key);
        if (arr == null || arr.isEmpty()) {
            log.info("JSONArray for key '{}' is null. Returning empty list.", key);
            return new ArrayList<>(Arrays.asList("null"));
        }

        List<String> list = arr.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        log.info("Extracted list for key '{}': {}", key, list);

        return list;
    }


    public List<String> getAllDeviceIds(String docker_name, Integer page_no, Integer page_size, String search_key, String is_select_all) {
        Integer offset = page_size * (page_no - 1);
        List<String> deviceIds;
        log.info("getAllDeviceIds");

        if (is_select_all != null && is_select_all.equals("true")) {
            if (search_key != null && !search_key.isBlank() && !search_key.equals("null")) {
                // search_key exists + select all = return all matching IDs
                deviceIds = deviceRepository.getAllDeviceIdsSearchKeySelectAll(search_key, docker_name);
            } else {
                // select all, no search key = return all device IDs
                log.info("select all, no search key = return all device IDs");
                deviceIds = deviceRepository.getAllDeviceIdsSelectAll(docker_name);
            }
        } else {
            if (search_key != null && !search_key.isBlank() && !search_key.equals("null")) {
                // search_key exists + pagination
                deviceIds = deviceRepository.getAllDeviceIdsSearchKeyPaginated(search_key, page_size, offset, docker_name);
            } else {
                // no search key + pagination
                deviceIds = deviceRepository.getAllDeviceIdsPaginated(page_size, offset, docker_name);
            }
        }

        return deviceIds;
    }


}


