package io.sclera.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.sclera.Repository.ApplicationUserRepository;
import io.sclera.Repository.DeviceInstalledAppsRepository;
import io.sclera.Repository.DeviceSpecificationRepository;
import io.sclera.Repository.ManagedSoftwareRepository;
import io.sclera.dto.InventoryApplicationDTO;
import io.sclera.dto.InventoryApplicationUserDTO;
import io.sclera.dto.ManagedSoftwareDTO;
import io.sclera.dto.ManagedSoftwareUsersDTO;
import io.sclera.models.ApplicationUser;
import io.sclera.models.DeviceSpecification;
import io.sclera.models.ManagedSoftware;
import org.json.JSONArray;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ManagedSoftwareService {
    private static final Logger log = LoggerFactory.getLogger(ManagedSoftwareService.class);

    ManagedSoftwareRepository managedSoftwareRepository;
    DeviceInstalledAppsService deviceInstalledAppsService;
    DeviceInstalledAppsRepository deviceInstalledAppsRepository;
    DeviceSpecificationRepository deviceSpecificationRepository;
    ApplicationUserRepository applicationUserRepository;
    APICallService apiCallService;
    JdbcTemplate jdbcTemplate;

    public ManagedSoftwareService(ManagedSoftwareRepository managedSoftwareRepository,
                                  DeviceInstalledAppsService deviceInstalledAppsService,
                                  DeviceInstalledAppsRepository deviceInstalledAppsRepository,
                                  DeviceSpecificationRepository deviceSpecificationRepository,
                                  ApplicationUserRepository applicationUserRepository,
                                  APICallService apiCallService,
                                  JdbcTemplate jdbcTemplate) {
        this.managedSoftwareRepository = managedSoftwareRepository;
        this.deviceInstalledAppsService = deviceInstalledAppsService;
        this.deviceInstalledAppsRepository = deviceInstalledAppsRepository;
        this.deviceSpecificationRepository = deviceSpecificationRepository;
        this.applicationUserRepository = applicationUserRepository;
        this.apiCallService = apiCallService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public List<ManagedSoftwareDTO> getAllManagedSoftwares(String username, String vdmsid, String dockername, String condition, String searchKey, Integer pageNo, Integer pageSize) {
        // Calculate offset
        Integer offset = pageSize * (pageNo - 1);
        log.info("Condition : {} ", condition);
        List<ManagedSoftwareDTO> managedSoftwareDTOS = managedSoftwareRepository.getAllManagedSoftwares(condition, searchKey, offset, pageSize);

        // Update status based on subscription dates
        for (ManagedSoftwareDTO managedSoftwareDTO : managedSoftwareDTOS) {
            if (managedSoftwareDTO.getApplicationId() != null) {
                String status;
                BigInteger currentTime = BigInteger.valueOf(System.currentTimeMillis());

                if (currentTime.compareTo(managedSoftwareDTO.getSubscriptionStartDate()) >= 0 && currentTime.compareTo(managedSoftwareDTO.getSubscriptionEndDate()) <= 0) {
                    status = "active";
                } else {
                    status = "expired";
                }

                if (!managedSoftwareDTO.getStatus().equals(status)) {
                    managedSoftwareRepository.updateStatusById(managedSoftwareDTO.getId(), status);
                    managedSoftwareDTO.setStatus(status);
                }
            }
        }

        return managedSoftwareDTOS;
    }

    public ManagedSoftwareDTO getManagedSoftwareById(String id){
        return managedSoftwareRepository.getManagedSoftwareById(id);
    }

//    public void upsertManagedSoftware(){
//        List<DeviceInstalledAppsDTO> installedApps = deviceInstalledAppsService.getAllUniqueInstalledAppDTOs();
//
//        List<String> existingNames = managedSoftwareRepository.findExistingNames(
//                installedApps.stream().map(DeviceInstalledAppsDTO::getName).collect(Collectors.toList())
//        );
//
//        List<ManagedSoftwareDTO> newRecords = installedApps.stream()
//                .filter(app -> !existingNames.contains(app.getName()))
//                .map(app -> ManagedSoftwareDTO.builder()
//                        .id(UUID.randomUUID().toString())
//                        .name(app.getName())
//                        .vendor(app.getVendor())
//                        .status("on-going")
//                        .build())
//                .collect(Collectors.toList());
//
//        // 3. Save all new records in batch
//        log.info("managedSoftwareRepository {}", newRecords);
//        if (!newRecords.isEmpty()) {
//            for (ManagedSoftwareDTO managedSoftwareDTO : newRecords) {
//                managedSoftwareRepository.upsertManagedSoftware(
//                        managedSoftwareDTO.getId(),
//                        managedSoftwareDTO.getName(),
//                        managedSoftwareDTO.getApplicationName(),
//                        managedSoftwareDTO.getUrl(),
//                        managedSoftwareDTO.getVendor(),
//                        managedSoftwareDTO.getSubscriptionType(),
//                        managedSoftwareDTO.getUnitPrice(),
//                        managedSoftwareDTO.getCurrency(),
//                        managedSoftwareDTO.getSubscriptionStartDate(),
//                        managedSoftwareDTO.getSubscriptionEndDate(),
//                        managedSoftwareDTO.getStatus(),
//                        managedSoftwareDTO.getApplicationId()
//                );
//            }
//        }
//
//
//    }

    public String insertManagedSoftware(String name, String vendor) {

        Optional<String> id = managedSoftwareRepository.getManagedSoftwareIdByName(name);
        if (id.isPresent()) {
            log.info("name exist and id is {}", id.get());
            return id.get();
        } else {
            String generatedId = UUID.randomUUID().toString();
            managedSoftwareRepository.upsertManagedSoftware(
                    generatedId,
                    name,
                    "",
                    "",
                    "",
                    vendor,
                    "",
                    "",
                    0.0,
                    "",
                    BigInteger.valueOf(0),
                    BigInteger.valueOf(0),
                    "",
                    null
            );
            log.info("name doesn't exist creating a new managedSoftwareDTO with id {}", generatedId);

            return generatedId;
        }
    }

    public ManagedSoftwareDTO updateManagedSoftware(String username, String vdmsid, ManagedSoftwareDTO managedSoftwareDTO) {
                managedSoftwareRepository.upsertManagedSoftware(
                managedSoftwareDTO.getId(),
                managedSoftwareDTO.getName(),
                managedSoftwareDTO.getApplicationName(),
                managedSoftwareDTO.getApplicationType(),
                managedSoftwareDTO.getUrl(),
                managedSoftwareDTO.getVendor(),
                managedSoftwareDTO.getSubscriptionId(),
                managedSoftwareDTO.getSubscriptionType(),
                managedSoftwareDTO.getUnitPrice(),
                managedSoftwareDTO.getCurrency(),
                managedSoftwareDTO.getSubscriptionStartDate(),
                managedSoftwareDTO.getSubscriptionEndDate(),
                managedSoftwareDTO.getStatus(),
                managedSoftwareDTO.getApplicationId()
        );
                return this.getManagedSoftwareById(managedSoftwareDTO.getId());
    }

    // Tag application details from inventory
    @Transactional
    public ManagedSoftwareDTO tagInventoryDetails(String username, String vdmsid, ManagedSoftwareDTO managedSoftwareDTO) {
        String managedSoftwareId = managedSoftwareDTO.getId();

        try {
            String applicationId = managedSoftwareDTO.getApplicationId();

            // 1. Call the inventory API to get the user data
            com.alibaba.fastjson.JSONArray applicationUsersJson = apiCallService.getApplicationUsersFromInventory(vdmsid, applicationId);

            if (applicationUsersJson == null) {
                log.error("No application user data returned during tag from inventory API for applicationId: {}", applicationId);
                throw new RuntimeException("Failed to fetch application users from inventory");
            }

            List<InventoryApplicationUserDTO> applicationUserDTOS = JSON.parseArray(
                    applicationUsersJson.toJSONString(),
                    InventoryApplicationUserDTO.class
            );

            log.info("applicationUserDTOs.. {}", applicationUserDTOS);

            // 2. Save all users into Application User table
            List<ApplicationUser> applicationUsersList = new ArrayList<>();

            try {
                if (!applicationUserDTOS.isEmpty()) {
                    for (InventoryApplicationUserDTO applicationUserDTO : applicationUserDTOS) {
                        ApplicationUser applicationUser = new ApplicationUser();
                        applicationUser.setId(applicationUserDTO.getId());
                        applicationUser.setType(applicationUserDTO.getUserType());
                        applicationUser.setEmail(applicationUserDTO.getEmail());
                        applicationUser.setTechnicianId(applicationUserDTO.getTechnicianId());
                        applicationUser.setManagedSoftwareId(managedSoftwareId); // Assign the managedSoftwareId

                        applicationUsersList.add(applicationUser);
                    }
                }

                applicationUserRepository.saveAll(applicationUsersList);
                log.info("Successfully saved {} application users to database", applicationUsersList.size());
            } catch (Exception e) {
                log.error("Error saving application users to database: {}", e.getMessage());
                throw new RuntimeException("Failed to save application users", e);
            }

            // 3. Get all emails from these application users
            List<String> applicationUserEmails = applicationUsersList.stream()
                    .map(ApplicationUser::getEmail)
                    .filter(Objects::nonNull)
                    .filter(email -> !email.trim().isEmpty())
                    .collect(Collectors.toList());

            // Note: Handling records with technician_id instead of email (if email is not available) is pending for now
            // These records will be skipped in the current implementation

            // 4. Get device specs linked to this managed software
            Set<String> deviceSpecificationIds = deviceInstalledAppsRepository.getDeviceSpecIdsByManagedSoftwareId(managedSoftwareId);
            List<DeviceSpecification> deviceSpecs = deviceSpecificationRepository.findByIdIn(deviceSpecificationIds);

            // Split into compliant and risky
            Set<String> compliantDeviceSpecIds = new HashSet<>();
            Set<String> riskyDeviceSpecIds = new HashSet<>();

            // 5. Find matching emails in application_user table and group device specs into Compliant/Risky
            for (DeviceSpecification spec : deviceSpecs) {
                String deviceUserEmail = spec.getEmail();

                if (deviceUserEmail != null && !deviceUserEmail.trim().isEmpty()) {
                    if (applicationUserEmails.contains(deviceUserEmail)) {
                        // Compliant: email exists in updated application_users
                        compliantDeviceSpecIds.add(spec.getId());
                    } else {
                        // Risky: email does not exist in updated application_users
                        riskyDeviceSpecIds.add(spec.getId());
                    }
                } else {
                    // Risky: no email in device_specification
                    log.info("DeviceSpecification ID: {} has no email, marking as Risky...", spec.getId());
                    riskyDeviceSpecIds.add(spec.getId());
                }
            }

            // 6. Batch update risk status
            if (!compliantDeviceSpecIds.isEmpty()) {
                deviceInstalledAppsRepository.updateRiskStatusForDevices(compliantDeviceSpecIds, managedSoftwareId, 0);
                log.info("Updated {} devices as compliant", compliantDeviceSpecIds.size());
            }
            if (!riskyDeviceSpecIds.isEmpty()) {
                deviceInstalledAppsRepository.updateRiskStatusForDevices(riskyDeviceSpecIds, managedSoftwareId, 1);
                log.info("Updated {} devices as risky", riskyDeviceSpecIds.size());
            }

            // Update managed software details (Tagging)
            ManagedSoftwareDTO managedSoftware = updateManagedSoftware(username, vdmsid, managedSoftwareDTO);

            if (managedSoftware != null) {
                log.info("Successfully tagged inventory details for managedSoftwareId: {}", managedSoftwareId);
                return managedSoftware;
            } else {
                throw new RuntimeException("Tagging failed: Error updating ManagedSoftware with ID: " + managedSoftwareId);
            }

        } catch (Exception e) {
            log.error("Failed to tag inventory details for managedSoftwareId={}", managedSoftwareId);
            throw new RuntimeException("Error tagging inventory details: ", e);
        }
    }

    // Un-tag inventory application details from managed software
    @Transactional
    public ManagedSoftwareDTO unTagInventoryDetails(String username, String vdmsid, ManagedSoftwareDTO managedSoftwareDTO) {
        String managedSoftwareId = managedSoftwareDTO.getId();

        try {
            Set<String> deviceSpecificationIds = deviceInstalledAppsRepository.getDeviceSpecIdsByManagedSoftwareId(managedSoftwareId);

            // 1. Update risk status to null for these devices
            deviceInstalledAppsRepository.updateRiskStatusForDevices(deviceSpecificationIds, managedSoftwareId, null);
            log.info("Updated risk_status=null for {} devices on managedSoftwareId: {}", deviceSpecificationIds.size(), managedSoftwareId);

            // 2. Clear managed software IDs from application user table
            String applicationId = managedSoftwareDTO.getApplicationId();

            if (applicationId == null || applicationId.isEmpty()) {
                log.info("No applicationId found! Skipping clearing managed software from application users table...");
            } else {
                // 3. Call the inventory API to get the user data
                com.alibaba.fastjson.JSONArray applicationUsersJson = apiCallService.getApplicationUsersFromInventory(vdmsid, applicationId);

                if (applicationUsersJson == null) {
                    log.error("No application user data returned during untag from inventory API for applicationId: {}", applicationId);
                    throw new RuntimeException("Failed to fetch application users from inventory");
                }

                // 4. Extract all the IDs
                Set<String> applicationUserIds = new HashSet<>();
                for (int i = 0; i < applicationUsersJson.size(); i++) {
                    JSONObject obj = applicationUsersJson.getJSONObject(i);
                    applicationUserIds.add(obj.getString("id"));
                }

                // 5. Delete all these users from application user table
                int deletedRecords = applicationUserRepository.deleteApplicationUsersByIds(applicationUserIds);
                log.info("Deleted {} records from application_user table where managedSoftwareId= {}", deletedRecords, managedSoftwareId);
            }

            // Reset managed software details (Un-Tagging)
            managedSoftwareRepository.upsertManagedSoftware(
                    managedSoftwareId,
                    managedSoftwareDTO.getName(),
                    "",
                    "",
                    "",
                    managedSoftwareDTO.getVendor(),
                    "",
                    "",
                    0.0,
                    "",
                    BigInteger.valueOf(0),
                    BigInteger.valueOf(0),
                    "",
                    null
            );

            ManagedSoftwareDTO updatedManagedSoftware = this.getManagedSoftwareById(managedSoftwareId);

            if (updatedManagedSoftware != null) {
                log.info("Successfully un-tagged inventory details from managedSoftware with ID: {}", managedSoftwareId);
                return updatedManagedSoftware;
            } else {
                throw new RuntimeException("Un-tagging failed: ManagedSoftware not found for ID: " + managedSoftwareId);
            }

        } catch (Exception e) {
            log.error("Failed to un-tag inventory details from managedSoftware with ID: {}", managedSoftwareId);
            throw new RuntimeException("Error un-tagging inventory details: ", e);
        }
    }

    public List<ManagedSoftwareUsersDTO> getManagedSoftwareUsers(String username, String vdmsid, String dockername, String managedsoftwareId) {
        return managedSoftwareRepository.getManagedSoftwareUsers(managedsoftwareId);
    }

    public Map<String, Integer> getManagedSoftwareLicense(String username, String vdmsid, String dockername, String managedsoftwareId, String applicationId) {
        JSONObject licenseJson = apiCallService.getLicenseDetailsFromInventory(vdmsid, applicationId);

        if (licenseJson == null) {
            log.error("No license data returned from inventory API for applicationId: {}", applicationId);
            throw new RuntimeException("Failed to fetch license details from inventory");
        }

        Map<String, Integer> licenseDetails = new HashMap<>();
        licenseDetails.put("licensePurchased", licenseJson.getIntValue("licensePurchased"));
        licenseDetails.put("licenseUsed", licenseJson.getIntValue("licenseUsed"));

        Integer compliantLicenseCount = deviceInstalledAppsRepository.getCompliantRiskStatusCount(managedsoftwareId); // risk_status = 0 (compliant) and 2 (risk overridden)
        licenseDetails.put("compliantLicense", compliantLicenseCount);

        log.info("licenseDetails: {}", licenseDetails);
        return licenseDetails;
    }

    public Map<String, Integer> getManagedSoftwareCount(String username, String vdmsid, String dockername) {
        Map<String, Integer> managedSoftwareCounts = new HashMap<>();

        Integer allCount = managedSoftwareRepository.getAllStatusCounts();
        Integer activeCount = managedSoftwareRepository.getActiveStatusCounts();
        Integer expiredCount = managedSoftwareRepository.getExpiredStatusCounts();
        Integer othersCount = managedSoftwareRepository.getOthersStatusCounts();
        Integer monthlySubscribedCount = managedSoftwareRepository.getMonthlySubscribedCount();
        Integer yearlySubscribedCount = managedSoftwareRepository.getYearlySubscribedCount();

        managedSoftwareCounts.put("all", allCount);
        managedSoftwareCounts.put("active", activeCount);
        managedSoftwareCounts.put("expired", expiredCount);
        managedSoftwareCounts.put("others", othersCount);
        managedSoftwareCounts.put("monthlySubscribed", monthlySubscribedCount);
        managedSoftwareCounts.put("yearlySubscribed", yearlySubscribedCount);

        log.info("managedSoftwareCounts: {}", managedSoftwareCounts);
        return managedSoftwareCounts;
    }

    public List<Map<String, String>> getAllRiskAndCompliances(String username, String vdmsId, String dockerName, String managedSoftwareId) {
        Set<String> riskyDeviceSpecIds = deviceInstalledAppsRepository.getRiskyDeviceSpecIdsByManagedSoftwareId(managedSoftwareId); // risk_status = 1 (risky)

        if (riskyDeviceSpecIds.isEmpty()) {
            log.info("No risky devices found for managedSoftwareId: {}", managedSoftwareId);
            return Collections.emptyList();
        }

        // Batch fetch all device specifications at once
        List<DeviceSpecification> deviceSpecs = deviceSpecificationRepository.findByIdIn(riskyDeviceSpecIds);

        List<Map<String, String>> riskAndComplianceList = new ArrayList<>();

        for (DeviceSpecification spec : deviceSpecs) {
            Map<String, String> map = new HashMap<>();

            map.put("deviceSpecificationId", spec.getId());
            map.put("user", spec.getUsername());
            map.put("device", spec.getDeviceName());
            map.put("severityLevel", "High");
            map.put("comment", "User not found in inventory");

            riskAndComplianceList.add(map);
        }

        log.info("Successfully fetched {} risk and compliance records for managedSoftwareId: {}", riskAndComplianceList.size(), managedSoftwareId);
        return riskAndComplianceList;
    }

    public void riskAndComplianceAction(String username, String vdmsId, String dockerName, String managedSoftwareId, JSONObject data) {
        Set<String> deviceSpecIds = new HashSet<>();
        deviceSpecIds.add(data.getString("deviceSpecificationId"));

        Integer rowsAffected = deviceInstalledAppsRepository.updateRiskStatusForDevices(deviceSpecIds, managedSoftwareId, 2); // risk_status = 2 (risk overridden)

        if (rowsAffected != null && rowsAffected > 0) {
            log.info("Updated risk_status=2 for managedSoftwareId: {} on deviceSpecificationId: {}", managedSoftwareId, data.getString("deviceSpecificationId"));
        } else {
            log.error("No records updated for managedSoftwareId: {} on deviceSpecificationId: {}", managedSoftwareId, data.getString("deviceSpecificationId"));
            throw new RuntimeException("Error updating risk and compliance action");
        }
    }

    public String getManagedSoftwareFieldsList(String username, String vdmsId) {
        try {
            // Read from file
            StringBuilder stringBuilder = new StringBuilder();
            InputStream inputStreamReader = getClass().getResourceAsStream("/managed_software_fields.json");

            assert inputStreamReader != null;
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStreamReader))) {
                stringBuilder.append(bufferedReader.lines().collect(Collectors.joining("\n")));
            } catch (Exception e) {
                log.error("Error reading managed_software_fields.json: {}", e.getMessage(), e);
            }

            // Parse managed_software_fields.json and collect columns
            JSONArray result = new JSONArray(stringBuilder.toString());
            return result.toString();

        } catch (Exception e) {
            log.error("Error parsing managed_software_fields.json: {}", e.getMessage(), e);
            return null;
        }
    }

    public List<String> getManagedSoftwareUsersList(String username, String vdmsId) {
        return deviceSpecificationRepository.findDistinctEmail();
    }

    public List<String> getManagedSoftwareOSTypesList(String username, String vdmsId) {
        return deviceSpecificationRepository.findDistinctOsType();
    }

    @Transactional
    public void deleteManagedSoftware(String username, String vdmsId, String dockerName, String managedSoftwareId) {
        try {
            // 1. Clear managed software IDs and their risk status from device installed apps table
            Integer rowsAffected = deviceInstalledAppsRepository.clearManagedSoftwareIdAndRiskStatus(managedSoftwareId);
            log.info("Cleared {} device installed apps associated with managedSoftwareId: {}", rowsAffected, managedSoftwareId);

            // 2. Clear managed software IDs from application users
            String applicationId = managedSoftwareRepository.getApplicationIdByManagedSoftwareId(managedSoftwareId);

            if (applicationId == null || applicationId.isEmpty()) {
                log.info("No applicationId found! Skipping clearing managed software from application users...");
            } else {
                // 3. Call the inventory API to get all users of the application
                com.alibaba.fastjson.JSONArray applicationUsersJson = apiCallService.getApplicationUsersFromInventory(vdmsId, applicationId);

                if (applicationUsersJson == null) {
                    log.error("No application user data returned during delete from inventory API for applicationId: {}", applicationId);
                    throw new RuntimeException("Failed to fetch application users from inventory");
                }

                // 4. Extract all the user IDs
                Set<String> applicationUserIds = new HashSet<>();
                for (int i = 0; i < applicationUsersJson.size(); i++) {
                    JSONObject obj = applicationUsersJson.getJSONObject(i);
                    applicationUserIds.add(obj.getString("id"));
                }

                // 5. Delete all these users from application user table
                int deletedRecords = applicationUserRepository.deleteApplicationUsersByIds(applicationUserIds);
                log.info("Deleted {} records from Application User table where managedSoftwareId= {}", deletedRecords, managedSoftwareId);
            }

            managedSoftwareRepository.deleteById(managedSoftwareId);
            log.info("Successfully deleted managedSoftware with ID: {}", managedSoftwareId);

        } catch (Exception e) {
            log.error("Failed to delete managedSoftware with ID: {}. Error: {}", managedSoftwareId, e.getMessage(), e);
            throw new RuntimeException("Error deleting managed software: ", e);
        }
    }

    // Filter inventory applications that do not exist in managed software and which has transaction
    public List<InventoryApplicationDTO> getInventoryApplications(String username, String vdmsId, String dockerName) {
        try {
            com.alibaba.fastjson.JSONArray applicationsJson = apiCallService.getAllInventoryApplications(vdmsId);

            if (applicationsJson == null || applicationsJson.isEmpty()) {
                log.warn("No application data returned from the inventory API for vdmsId: {}", vdmsId);
                return Collections.emptyList();
            }

            // Fetch existing application IDs from managed software table
            Set<String> existingApplicationIds = managedSoftwareRepository.findDistinctApplicationIds();
            log.info("Existing application id: {}", existingApplicationIds);

            // Convert JSONArray to List<InventoryApplicationDTO> using JSON.parseArray
            List<InventoryApplicationDTO> allApplications = JSON.parseArray(
                    applicationsJson.toJSONString(),
                    InventoryApplicationDTO.class
            );

            log.info("All applications: {}", allApplications.toString());

            // Filter out existing applications
            List<InventoryApplicationDTO> filteredApplications = allApplications.stream()
                    .filter(app -> app.getId() != null && !existingApplicationIds.contains(app.getId()))
                    .filter(app -> app.getTotal_license() != null && app.getTotal_license() > 0.0)
                    .collect(Collectors.toList());

            log.info("Filtered {} new applications out of {} total applications", filteredApplications.size(), allApplications.size());
            return filteredApplications;
        } catch (Exception e) {
            log.error("Error filtering inventory applications for vdmsId: {}", vdmsId, e);
            return Collections.emptyList();
        }
    }

    /*********************************************************** SYNC METHODS FOR INVENTORY INTEGRATION ***********************************************************/

    // Update managed software details for inventory application upsert sync call
    public Set<String> updateManagedSoftwareDetailsSync(List<InventoryApplicationDTO> applicationDetails) {
        if (applicationDetails == null || applicationDetails.isEmpty()) {
            log.info("No application details provided for update.");
            return Set.of();
        }

        Set<String> failedUpdates = new HashSet<>();
        Set<String> successfulUpdates = new HashSet<>();

        for (InventoryApplicationDTO applicationDTO : applicationDetails) {
            try {
                boolean updated = this.updateSingleManagedSoftwareTransaction(applicationDTO);

                if (updated) {
                    successfulUpdates.add(applicationDTO.getId());
                } else {
                    failedUpdates.add(applicationDTO.getId());
                    log.warn("No matching managed software record found for applicationId: {}", applicationDTO.getId());
                }

            } catch (Exception e) {
                failedUpdates.add(applicationDTO.getId());
                log.error("Error updating managed_software record for applicationId {}: {}", applicationDTO.getId(), e.getMessage(), e);
            }
        }

        log.info("Managed software update completed. Successful: {}, Failed: {}", successfulUpdates.size(), failedUpdates.size());

        if (!failedUpdates.isEmpty()) {
            log.info("Failed to UPSERT Application IDs: {}", failedUpdates);
        }

        return failedUpdates;
    }

    // Clear managed software details for inventory application delete sync call
    public Set<String> clearManagedSoftwareDetailsSync(List<InventoryApplicationDTO> applicationDetails) {
        if (applicationDetails == null || applicationDetails.isEmpty()) {
            log.info("No application details provided for delete...");
            return Set.of();
        }

        Set<String> successfulClears = new HashSet<>();
        Set<String> failedClears = new HashSet<>();

        for (InventoryApplicationDTO applicationDTO : applicationDetails) {
            try {
                boolean cleared = this.clearSingleManagedSoftwareTransaction(applicationDTO);

                if (cleared) {
                    successfulClears.add(applicationDTO.getId());
                } else {
                    failedClears.add(applicationDTO.getId());
                    log.warn("No matching managed_software record found for applicationId: {}", applicationDTO.getId());
                }

            } catch (Exception e) {
                failedClears.add(applicationDTO.getId());
                log.error("Error clearing managed_software record for applicationId {}: {}", applicationDTO.getId(), e.getMessage(), e);
            }
        }

        log.info("Successfully cleared {} managed_software record(s).", successfulClears.size());

        if (!failedClears.isEmpty()) {
            log.info("Failed to CLEAR Application IDs: {}", failedClears);
        }

        return failedClears;
    }

    // Helper methods for Applications SYNC

    // Starts a brand-new, independent transaction for each application
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean updateSingleManagedSoftwareTransaction(InventoryApplicationDTO applicationDTO) {
        // Fetch existing managed_software records by applicationId
        ManagedSoftware managedSoftware = managedSoftwareRepository.findByApplicationId(applicationDTO.getId());

        if (managedSoftware == null) {
            log.warn("No managed software record found for applicationId: {}", applicationDTO.getId());
            return false;
        }

        try {
            managedSoftware.setApplicationName(applicationDTO.getApplicationName());
            managedSoftware.setApplicationType(applicationDTO.getApplicationType());
//                managedSoftware.setApplicationNumber(applicationDTO.getNumber());
            managedSoftware.setUrl(applicationDTO.getUrl());
            managedSoftware.setVendor(applicationDTO.getVendor());
            managedSoftware.setSubscriptionId(applicationDTO.getSubscriptionId());
            managedSoftware.setSubscriptionType(applicationDTO.getSubscriptionType());
            managedSoftware.setUnitPrice(applicationDTO.getUnitPrice());
            managedSoftware.setCurrency(applicationDTO.getCurrency());
            managedSoftware.setSubscriptionStartDate(BigInteger.valueOf(applicationDTO.getSubscriptionStartDate()));
            managedSoftware.setSubscriptionEndDate(BigInteger.valueOf(applicationDTO.getSubscriptionEndDate()));
            managedSoftware.setStatus(applicationDTO.getStatus());

            // Save the update
            managedSoftwareRepository.save(managedSoftware);

            log.info("Successfully updated managed_software for applicationId: {}", applicationDTO.getId());
            return true;
        } catch (Exception e) {
            log.error("Failed to update managed_software for applicationId: {} {}", applicationDTO.getId(), e.getMessage(), e);
            throw e;
        }
    }

    // Starts a brand-new, independent transaction for each application
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean clearSingleManagedSoftwareTransaction(InventoryApplicationDTO applicationDTO) {
        // Fetch existing managed_software records by applicationId
        ManagedSoftware managedSoftware = managedSoftwareRepository.findByApplicationId(applicationDTO.getId());

        if (managedSoftware == null) {
            log.warn("No managed_software record found for applicationId: {}", applicationDTO.getId());
            return false;
        }

        // Step 1: Clear managed_software data
        try {
            managedSoftware.setApplicationName("");
            managedSoftware.setApplicationType("");
            managedSoftware.setUrl("");
            managedSoftware.setSubscriptionId("");
            managedSoftware.setSubscriptionType("");
            managedSoftware.setUnitPrice(0.0);
            managedSoftware.setCurrency("");
            managedSoftware.setSubscriptionStartDate(BigInteger.ZERO);
            managedSoftware.setSubscriptionEndDate(BigInteger.ZERO);
            managedSoftware.setStatus("");
            managedSoftware.setApplicationId(null);

            managedSoftwareRepository.save(managedSoftware);
            log.info("Cleared managed_software record for applicationId: {}", applicationDTO.getId());
        } catch (Exception e) {
            log.error("Failed to clear managed_software for applicationId: {} {}", applicationDTO.getId(), e.getMessage(), e);
            throw e;
        }

        // Step 2: Clear risk_status in device_installed_apps
        String managedSoftwareId = managedSoftware.getId();
        try {
            Integer riskCleared = deviceInstalledAppsRepository.clearRiskStatusByManagedSoftwareId(managedSoftwareId);
            log.info("Cleared risk_status for {} device_installed_apps record(s) (managedSoftwareId={}).", riskCleared, managedSoftwareId);
        } catch (Exception e) {
            log.error("Failed to clear risk_status for managedSoftwareId={}: {}", managedSoftwareId, e.getMessage(), e);
            throw e;
        }

        // Step 3: Delete application_user records
        try {
            Integer deleted = applicationUserRepository.deleteByManagedSoftwareId(managedSoftwareId);
            log.info("Deleted {} application_user record(s) for managedSoftwareId={}.", deleted, managedSoftwareId);
        } catch (Exception e) {
            log.error("Failed to delete application_user for managedSoftwareId={}: {}", managedSoftwareId, e.getMessage(), e);
            throw e; // rollback this transaction only
        }

        return true;
    }
}
