package io.sclera.service.impl;
import io.sclera.service.*;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.*;
import io.sclera.dto.TechnicianAvailabilityDTO;
import io.sclera.dto.TechnicianCertificateDTO;
import io.sclera.dto.TechnicianDTO;
import io.sclera.dto.TechnicianSkillDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import io.sclera.service.TechnicianService;

/**
 * Service that manages technicians and their associated skill profiles,
 * certificates, availability, and device tagging.
 *
 * <p>Provides CRUD and upsert operations over technicians, assembles enriched
 * skill profiles for AI suggestions, and tags/un-tags technicians to devices.
 * Cascading deletes also clear related skill, availability, certificate, and AI
 * call-log records.
 *
 * <p>Key collaborators: {@link TechnicianRepository},
 * {@link TechnicianAvailabilityService}, {@link TechnicianCertificateService},
 * {@link TechnicianSkillService}, {@link TechnicianSkillRepository},
 * {@link TechnicianCertificateRepository},
 * {@link TechnicianAvailabilityRepository}, {@link AiCallLogRepository}, and
 * {@link AiCallLogHistoryRepository}.
 */
@Service
public class TechnicianServiceImpl implements TechnicianService {
    private static final Logger log = LoggerFactory.getLogger(TechnicianServiceImpl.class);

    private final TechnicianRepository technicianRepository;
    private final TechnicianAvailabilityService technicianAvailabilityService;
    private final TechnicianCertificateService technicianCertificateService;
    private final TechnicianSkillService technicianSkillService;
    private final TechnicianSkillRepository technicianSkillRepository;
    private final TechnicianCertificateRepository technicianCertificateRepository;
    private final TechnicianAvailabilityRepository technicianAvailabilityRepository;
    private final AiCallLogRepository aiCallLogRepository;
    private final AiCallLogHistoryRepository aiCallLogHistoryRepository;


    @Autowired
    public TechnicianServiceImpl(TechnicianRepository technicianRepository, TechnicianAvailabilityService technicianAvailabilityService, TechnicianCertificateService technicianCertificateService,
                             TechnicianSkillService technicianSkillService, TechnicianSkillRepository technicianSkillRepository, TechnicianCertificateRepository technicianCertificateRepository,
                             TechnicianAvailabilityRepository technicianAvailabilityRepository, AiCallLogRepository aiCallLogRepository, AiCallLogHistoryRepository aiCallLogHistoryRepository) {
        this.technicianRepository = technicianRepository;
        this.technicianAvailabilityService = technicianAvailabilityService;
        this.technicianCertificateService = technicianCertificateService;
        this.technicianSkillService = technicianSkillService;
        this.technicianSkillRepository = technicianSkillRepository;
        this.technicianCertificateRepository = technicianCertificateRepository;
        this.technicianAvailabilityRepository = technicianAvailabilityRepository;
        this.aiCallLogRepository = aiCallLogRepository;
        this.aiCallLogHistoryRepository = aiCallLogHistoryRepository;
    }

    /**
     * Returns the technician with the given identifier.
     *
     * @param id the technician identifier
     * @param httpServletRequest the incoming HTTP request
     * @return the matching {@link TechnicianDTO}, or {@code null} if none exists
     */
    public TechnicianDTO getTechnicianById(String id, HttpServletRequest httpServletRequest) {
        return technicianRepository.getTechnicianById(id);
    }

    /**
     * Returns all technicians.
     *
     * @return the list of all {@link TechnicianDTO} records
     */
    public List<TechnicianDTO> getAllTechnician() {
        return  technicianRepository.getAllTechnician();
    }

    /**
     * Returns the email addresses of all technicians.
     *
     * @return a list of sets containing technician email addresses
     */
    public List<Set> getAllTechniciansEmail(){
        return technicianRepository.getAllTechniciansEmail();
    }

    /**
     * Inserts or updates the supplied technicians, logging and skipping any that fail.
     *
     * @param technicianDtos the technicians to upsert
     * @return the set of identifiers that were successfully inserted or updated
     */
    public Set<String> upsertTechnician(List<TechnicianDTO> technicianDtos) {
        Set<String> insertedTechnicianIds = new HashSet<>();
        if (technicianDtos != null && !technicianDtos.isEmpty()) {
            for (TechnicianDTO technicianDto : technicianDtos) {
                try {
                    Integer rowsAffected = technicianRepository.upsertTechnician(
                            technicianDto.getId(),
                            technicianDto.getEmail(),
                            technicianDto.getPhone(),
                            technicianDto.getCountryCode(),
                            technicianDto.getName(),
                            technicianDto.getDepartment(),
                            technicianDto.getDesignation(),
                            technicianDto.getTimeZone(),
                            technicianDto.getCreatedBy(),
                            technicianDto.getCreatedAt(),
                            technicianDto.getCost(),
                            technicianDto.getUnit(),
                            technicianDto.getType(),
//                    technicianDto.getIsDisabled(),
                            technicianDto.getVdmsId()
                    );
                    if (rowsAffected != null && rowsAffected > 0) {
                        insertedTechnicianIds.add(technicianDto.getId());
                    }
                } catch (Exception e) {
                    log.error("Error upserting Technician with ID {}: {}", technicianDto.getId(), e.getMessage(), e);
                }
            }
        }
        log.info("Inserted or updated {} technicians records.", insertedTechnicianIds.size());
        return insertedTechnicianIds;
    }

    /**
     * Deletes the supplied technicians together with their related skill,
     * availability, certificate, device-tagging, and AI call-log records.
     *
     * @param technicianDtos the technicians to delete
     * @return the set of technician identifiers that existed and were deleted
     */
    public Set<String> deleteTechniciansById(List<TechnicianDTO> technicianDtos) {
        Set<String> existingIds = Set.of();
        if (technicianDtos != null && !technicianDtos.isEmpty()) {
            List<String> allIds = technicianDtos.stream()
                    .map(TechnicianDTO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!allIds.isEmpty()) {
                existingIds = this.findExistingTechniciansByIds(allIds);
                if (existingIds != null && !existingIds.isEmpty()) {
                    try {
                        technicianSkillRepository.deleteTechnicianSkillsByTechnicianIds(existingIds);
                        technicianAvailabilityRepository.deleteTechnicianAvailabilityByTechnicianIds(existingIds);
                        technicianCertificateRepository.deleteTechnicianCertificatesByTechnicianIds(existingIds);

                        int noOfDeviceTechnicianRecordsDeleted = technicianRepository.deleteDeviceTechniciansByTechnicianIds(existingIds);
                        log.info("No of ids got to delete : {}, Number of DeviceTechnician records got deleted : {} ", existingIds.size(), noOfDeviceTechnicianRecordsDeleted);

                        int noOfAICallLogHistoryRecordsDeleted = aiCallLogHistoryRepository.deleteAICallLogsHistoryByTechnicianIds(existingIds);
                        log.info("No of ids got to delete : {}, Number of AICallLogHistory records got deleted : {} ", existingIds.size(), noOfAICallLogHistoryRecordsDeleted);

                        int noOfAICallLogRecordsDeleted = aiCallLogRepository.deleteAICallLogsByTechnicianIds(existingIds);
                        log.info("No of ids got to delete : {}, Number of AICallLog records got deleted : {} ", existingIds.size(), noOfAICallLogRecordsDeleted);

                        int noOfTechnicianRecordsDeleted = technicianRepository.deleteTechniciansByIds(existingIds);
                        log.info("No of ids got to delete : {}, Number of Technician records got deleted : {} ", existingIds.size(), noOfTechnicianRecordsDeleted);
                        return existingIds;
                    } catch (Exception e) {
                        log.error("Error deleting technicians and related records: {}", e.getMessage());
                        throw new RuntimeException("Error deleting technicians and related records", e);
                    }
                }
            }
        }
        return existingIds;
    }

    /**
     * Returns the subset of the given identifiers that correspond to existing technicians.
     *
     * @param ids the technician identifiers to check
     * @return the set of identifiers that exist
     */
    public Set<String> findExistingTechniciansByIds(List<String> ids) {
        Set<String> existingIds = new HashSet<>();
        if (ids != null && !ids.isEmpty()) {
            existingIds = technicianRepository.findExistingTechniciansByIds(ids);
        }
        return existingIds;
    }

    /**
     * Updates technicians matched by email and phone, and creates or updates
     * their associated availability, certificate, and skill records.
     *
     * @param technicianDtos the technicians to update
     * @param httpServletRequest the incoming HTTP request
     */
    public void updateTechnician(List<TechnicianDTO> technicianDtos, HttpServletRequest httpServletRequest) {
        for (TechnicianDTO technicianDto : technicianDtos) {
            if (technicianDto.getEmail() != null && technicianDto.getPhone() != null) {
                Integer updatedRows = technicianRepository.updateTechnicianByEmailAndPhone(
                        technicianDto.getCountryCode(),
                        technicianDto.getName(),
                        technicianDto.getDepartment(),
                        technicianDto.getDesignation(),
                        technicianDto.getTimeZone(),
                        technicianDto.getCreatedBy(),
                        technicianDto.getCreatedAt(),
                        technicianDto.getVdmsId(),
                        technicianDto.getEmail(),
                        technicianDto.getPhone()
                );

                if (updatedRows == 0) {
                    log.warn("No technician found with email: {} and phone: {}", technicianDto.getEmail(), technicianDto.getPhone());
                }

                // Update Availability
                if (technicianDto.getTechnicianAvailabilityDto() != null && !technicianDto.getTechnicianAvailabilityDto().isEmpty()) {
                    for (TechnicianAvailabilityDTO availabilityDto : technicianDto.getTechnicianAvailabilityDto()) {
                        availabilityDto.setTechnicianId(technicianDto.getId());
                        if (availabilityDto.getId() != null && !availabilityDto.getId().isEmpty()) {
                            technicianAvailabilityService.updateTechnicianAvailability(availabilityDto);
                        } else {
                            technicianAvailabilityService.createTechnicianAvailability(availabilityDto);
                        }
                    }
                }

                // Update or Create Certificates
                if (technicianDto.getTechnicianCertificateDtos() != null && !technicianDto.getTechnicianCertificateDtos().isEmpty()) {
                    for (TechnicianCertificateDTO technicianCertificateDto : technicianDto.getTechnicianCertificateDtos()) {
                        technicianCertificateDto.setTechnicianId(technicianDto.getId());
                        if (technicianCertificateDto.getId() != null && !technicianCertificateDto.getId().isEmpty()) {
                            technicianCertificateService.updateTechnicianCertificate(technicianCertificateDto);
                        } else {
                            technicianCertificateService.createTechnicianCertificate(technicianCertificateDto);
                        }
                    }
                }

                // Update or Create Skills
                if (technicianDto.getTechnicianSkillDto() != null) {
                    for (TechnicianSkillDTO skill : technicianDto.getTechnicianSkillDto()) {
                        skill.setTechnicianId(technicianDto.getId());
                        if (skill.getId() != null) {
                            technicianSkillService.updateTechnicianSkill(skill);
                        } else {
                            technicianSkillService.createTechnicianSkill(skill);
                        }
                    }
                }

            } else {
                log.warn("Email and phone are required for update.");
            }
        }
    }



    /**
     * Creates technicians with newly generated identifiers along with their
     * associated availability, certificate, and skill records.
     *
     * @param technicianDtos the technicians to create
     * @param httpServletRequest the incoming HTTP request
     */
    public void createTechnician(List<TechnicianDTO> technicianDtos,HttpServletRequest httpServletRequest) {
        for (TechnicianDTO technicianDto : technicianDtos) {
            technicianDto.setId((Generators.timeBasedGenerator().generate().toString()));
            Integer rowsAffected = technicianRepository.createTechnician(
                    technicianDto.getId(),
                    technicianDto.getEmail(),
                    technicianDto.getPhone(),
                    technicianDto.getCountryCode(),
                    technicianDto.getName(),
                    technicianDto.getDepartment(),
                    technicianDto.getDesignation(),
                    technicianDto.getTimeZone(),
                    technicianDto.getCreatedBy(),
                    technicianDto.getCreatedAt(),
                    technicianDto.getVdmsId()
            );
            // Create technician availability (list version)
            if (technicianDto.getTechnicianAvailabilityDto() != null && !technicianDto.getTechnicianAvailabilityDto().isEmpty()) {
                for (TechnicianAvailabilityDTO availabilityDto : technicianDto.getTechnicianAvailabilityDto()) {
                    availabilityDto.setTechnicianId(technicianDto.getId());
                    technicianAvailabilityService.createTechnicianAvailability(availabilityDto);
                }
            }
            if (technicianDto.getTechnicianCertificateDtos() != null && !technicianDto.getTechnicianCertificateDtos().isEmpty()) {
                for (TechnicianCertificateDTO certDto : technicianDto.getTechnicianCertificateDtos()) {
                    certDto.setTechnicianId(technicianDto.getId());
                    technicianCertificateService.createTechnicianCertificate(certDto);
                }
            }
            if (technicianDto.getTechnicianSkillDto() != null) {
                for (TechnicianSkillDTO skill : technicianDto.getTechnicianSkillDto()) {
                    skill.setTechnicianId(technicianDto.getId());
                    technicianSkillService.createTechnicianSkill(skill);
                }
            }

        }
    }


    /**
     * Returns the technician with the given identifier, enriched with its skills and certificates.
     *
     * @param id the technician identifier
     * @return the enriched {@link TechnicianDTO}
     */
    public TechnicianDTO getTechnicianDetailsById(String id) {
        TechnicianDTO technician = technicianRepository.getTechnicianById(id);
        if (technician == null) {
            throw new RuntimeException("Technician not found with ID: " + id);
        }

        List<TechnicianSkillDTO> skills = technicianSkillRepository.getSkillsByTechnicianId(id);
        List<TechnicianCertificateDTO> certificates = technicianCertificateRepository.getCertificatesByTechnicianId(id);

        technician.setTechnicianSkillDto(skills);
        technician.setTechnicianCertificateDtos(certificates);

        return technician;
    }


    // For AI Suggestions skill profiles and list all Tagged Technicians
    /**
     * Returns the technician's skill profile including primary skill and current
     * availability, resolved against the current UTC time.
     *
     * @param id the technician identifier
     * @return the {@link TechnicianDTO} skill profile
     */
    public TechnicianDTO getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById(String id) {
        try {
            Instant nowUtc = Instant.now();
            ZonedDateTime utcDateTime = nowUtc.atZone(ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");
            String formattedUtc = utcDateTime.format(formatter);
            String formattedDateTime = String.valueOf(nowUtc.toEpochMilli());

            log.info("Formatted UTC: {}", formattedUtc);
            log.info("DateTime In epoch: {}", formattedDateTime);

            return technicianRepository.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById(id, formattedDateTime);

        } catch (Exception e) {
            log.error("Error fetching technician skill profile details for ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error fetching technician skill profile details for ID: " + id, e);
        }
    }

    // List all skill profiles
    /**
     * Returns a page of technician skill profiles including primary skill and
     * current availability, resolved against the current UTC time.
     *
     * @param size the page size
     * @param page the one-based page number
     * @return the list of {@link TechnicianDTO} skill profiles for the page
     */
    public List<TechnicianDTO> getAllTechnicianSkillProfilesWithPrimarySkillAndAvailability(int size, int page) {
        try {
            Instant nowUtc = Instant.now();
            ZonedDateTime utcDateTime = nowUtc.atZone(ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");
            String formattedUtc = utcDateTime.format(formatter);
            String formattedDateTime = String.valueOf(nowUtc.toEpochMilli());

            log.info("Formatted UTC: {}", formattedUtc);
            log.info("DateTime In epoch: {}", formattedDateTime);

            int offset = (page - 1) * size;

            return technicianRepository.getAllTechnicianSkillProfilesWithPrimarySkillAndAvailability(formattedDateTime, size, offset);
        } catch (Exception e) {
            log.error("Error fetching all technician skill profiles: {}", e.getMessage());
            throw new RuntimeException("Error fetching all technician skill profiles", e);
        }
    }

    // Tag technicians to a device
    /**
     * Tags the given technicians to the specified device.
     *
     * @param deviceId the device identifier
     * @param technicianIds the technician identifiers to tag
     */
    @Transactional
    public void tagTechniciansToDevice(String deviceId, List<String> technicianIds) {
        if (technicianIds != null && !technicianIds.isEmpty() && deviceId != null && !deviceId.isEmpty()) {
            for (String technicianId : technicianIds) {
                try {
                    technicianRepository.tagTechniciansToDevice(technicianId, deviceId);
                    log.info("Technician {} tagged to device successfully.", technicianId);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to tag Technician: " + technicianId + " to device: " + deviceId + "\n" + e.getMessage(), e);
                }
            }
        } else {
            log.warn("No technician id's provided to tag to device.");
        }
    }

    // Un-Tag technicians from a device
    /**
     * Removes the tagging between the given technicians and the specified device.
     *
     * @param deviceId the device identifier
     * @param technicianIds the technician identifiers to un-tag
     */
    @Transactional
    public void unTagTechniciansFromDevice(String deviceId, List<String> technicianIds) {
        if (technicianIds != null && !technicianIds.isEmpty() && deviceId != null && !deviceId.isEmpty()) {
            for (String technicianId : technicianIds) {
                try {
                    technicianRepository.unTagTechniciansFromDevice(technicianId, deviceId);
                    log.info("Technician {} un-tagged from device successfully.", technicianId);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to un-tag Technician: " + technicianId + " from device: " + deviceId + "\n" + e.getMessage(), e);
                }
            }
        } else {
            log.warn("No technician id's provided to un-tag from device.");
        }
    }

    // Get all technicians tagged to a specific device
    /**
     * Returns the skill profiles of all technicians tagged to the specified device.
     *
     * @param deviceId the device identifier
     * @return the list of tagged {@link TechnicianDTO} skill profiles, or an empty list if none
     */
    public List<TechnicianDTO> getAllTechniciansByDeviceId(String deviceId) {
        try {
            if (deviceId != null && !deviceId.isEmpty()) {
                List<String> technicianIds = technicianRepository.getAllTaggedTechnicianIds(deviceId);

                if (technicianIds != null && !technicianIds.isEmpty()) {
                    List<TechnicianDTO> taggedTechnicianSkillProfiles = new ArrayList<>();

                    for (String technicianId : technicianIds) {
                        TechnicianDTO technician = this.getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById(technicianId);
                        if (technician != null) {
                            taggedTechnicianSkillProfiles.add(technician);
                        }
                    }

                    return taggedTechnicianSkillProfiles;
                } else {
                    log.info("No technicians found tagged to device with ID: {}", deviceId);
                    return Collections.emptyList();
                }
            }
        } catch (Exception e) {
            log.error("Error fetching tagged technicians for device ID {}: {}", deviceId, e.getMessage());
            throw new RuntimeException("Error fetching tagged technicians for device ID: " + deviceId, e);
        }
        return Collections.emptyList();
    }

    // Get all Available technicians tagged to a specific device
    /**
     * Returns the skill profiles of technicians tagged to the specified device
     * that are currently available.
     *
     * @param deviceId the device identifier
     * @return the list of available tagged {@link TechnicianDTO} skill profiles, or an empty list if none
     */
    public List<TechnicianDTO> getAllAvailableTechnicianByDeviceId(String deviceId) {
        try {
            List<TechnicianDTO> taggedTechnicianSkillProfiles = this.getAllTechniciansByDeviceId(deviceId);

            if (taggedTechnicianSkillProfiles != null && !taggedTechnicianSkillProfiles.isEmpty()) {
                // Filter technicians based on availability
                return taggedTechnicianSkillProfiles.stream()
                        .filter(technician -> technician.getAvailability() != null && technician.getAvailability().equalsIgnoreCase("Available"))
                        .collect(Collectors.toList());
            } else {
                log.info("No tagged technician skill profiles for device ID: {}", deviceId);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("Error fetching available tagged technicians for device ID {}: {}", deviceId, e.getMessage());
            throw new RuntimeException("Error fetching available tagged technicians for device ID: " + deviceId, e);
        }
    }

    // Get all Available technicians tagged to a specific device with country code and phone number
    /**
     * Returns currently available technicians tagged to the specified device,
     * each including country code and phone number.
     *
     * @param deviceId the device identifier
     * @return the list of available tagged {@link TechnicianDTO} records, or an empty list if none
     */
    public List<TechnicianDTO> getAvailableTechnicianCountryCodePhoneByDeviceId(String deviceId) {
        log.debug("{}", "Fetching all available technicians with country code and phone for device ID: " + deviceId);
        try {

            List<String> technicianIds = technicianRepository.getAllTaggedTechnicianIds(deviceId);

            if (technicianIds != null && !technicianIds.isEmpty()) {
                List<TechnicianDTO> taggedTechnicianSkillProfiles = new ArrayList<>();

                for (String technicianId : technicianIds) {
                    try {
                        Instant nowUtc = Instant.now();
                        ZonedDateTime utcDateTime = nowUtc.atZone(ZoneOffset.UTC);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");
                        String formattedUtc = utcDateTime.format(formatter);
                        String formattedDateTime = String.valueOf(nowUtc.toEpochMilli());

                        log.info("Formatted UTC: {}", formattedUtc);
                        log.info("DateTime In epoch: {}", formattedDateTime);

                        TechnicianDTO technicianDTO = technicianRepository.getTechnicianWithCountryCodePhoneAndAvailabilityById(technicianId, formattedDateTime);

                        if (technicianDTO != null)
                            taggedTechnicianSkillProfiles.add(technicianDTO);

                    } catch (Exception e) {
                        log.error("Error fetching technician country code and phone details for ID {}", technicianId);
                        throw new RuntimeException("Error fetching technician country code and phone details ", e);
                    }
                }

                // Filter technicians based on availability
                return taggedTechnicianSkillProfiles.stream()
                        .filter(technician -> technician.getAvailability() != null && technician.getAvailability().equalsIgnoreCase("Available"))
                        .collect(Collectors.toList());

            } else {
                log.info("No technicians tagged to device with ID: {}", deviceId);
                return Collections.emptyList();
            }

        } catch (Exception e) {
            log.error("Error fetching available tagged technicians with country code and phone for device ID {}: {}", deviceId, e.getMessage());
            throw new RuntimeException("Error fetching available tagged technicians with country code and phone for device ID: " + deviceId, e);
        }
    }

    /**
     * Returns the name of the technician with the given identifier.
     *
     * @param technicianId the technician identifier
     * @return the technician name
     */
    public String getTechnicianNameById(String technicianId) {
        return technicianRepository.getTechnicianNameById(technicianId);
    }

    // List all skill profiles by filter
    /**
     * Returns a page of technician skill profiles filtered by technician id,
     * department, and availability, resolved against the current UTC time.
     *
     * @param size the page size
     * @param page the one-based page number
     * @param technicianIdFilter the technician id filter
     * @param departmentFilter the department filter
     * @param availabilityFilter the availability filter
     * @return the list of matching {@link TechnicianDTO} skill profiles for the page
     */
    public List<TechnicianDTO> getAllTechniciansByFilterByPagination(int size, int page, String technicianIdFilter, String departmentFilter, String availabilityFilter) {
        try {
            Instant nowUtc = Instant.now();
            ZonedDateTime utcDateTime = nowUtc.atZone(ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");
            String formattedUtc = utcDateTime.format(formatter);
            String formattedDateTime = String.valueOf(nowUtc.toEpochMilli());

            log.info("Formatted UTC: {}", formattedUtc);
            log.info("DateTime In epoch: {}", formattedDateTime);

            int offset = (page - 1) * size;

            return technicianRepository.getAllTechniciansByFilterByPagination(formattedDateTime, size, offset, technicianIdFilter, departmentFilter, availabilityFilter);
        } catch (Exception e) {
            log.error("Error fetching all technician skill profiles by filter: {}", e.getMessage());
            throw new RuntimeException("Error fetching all technician skill profiles by filter", e);
        }
    }

    // Get all technician names and IDs for filtering
    /**
     * Returns a page of technician names and identifiers matching the search key.
     *
     * @param page the one-based page number
     * @param size the page size
     * @param searchKey the search term to match against technicians
     * @return the list of matching {@link TechnicianDTO} records carrying name and id
     */
    public List<TechnicianDTO> getAllTechnicianNamesAndIds(int page, int size, String searchKey) {
        int offset = (page - 1) * size;
        return technicianRepository.getAllTechnicianNamesAndIds(size, offset, searchKey);
    }

    // Get all technician departments for filtering
    /**
     * Returns the distinct set of technician departments.
     *
     * @return the list of unique department names
     */
    public List<String> getUniqueTechnicianDepartments() {
        return technicianRepository.getUniqueTechnicianDepartments();
    }

}
