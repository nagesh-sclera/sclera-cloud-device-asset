package io.sclera.service;

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

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TechnicianService {
    private static final Logger log = LoggerFactory.getLogger(TechnicianService.class);

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
    public TechnicianService(TechnicianRepository technicianRepository, TechnicianAvailabilityService technicianAvailabilityService, TechnicianCertificateService technicianCertificateService,
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

    public TechnicianDTO getTechnicianById(String id, HttpServletRequest httpServletRequest) {
        return technicianRepository.getTechnicianById(id);
    }

    public List<TechnicianDTO> getAllTechnician() {
        return  technicianRepository.getAllTechnician();
    }

    public List<Set> getAllTechniciansEmail(){
        return technicianRepository.getAllTechniciansEmail();
    }

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

    public Set<String> findExistingTechniciansByIds(List<String> ids) {
        Set<String> existingIds = new HashSet<>();
        if (ids != null && !ids.isEmpty()) {
            existingIds = technicianRepository.findExistingTechniciansByIds(ids);
        }
        return existingIds;
    }

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
    public List<TechnicianDTO> getAvailableTechnicianCountryCodePhoneByDeviceId(String deviceId) {
        System.out.println("Fetching all available technicians with country code and phone for device ID: " + deviceId);
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

    public String getTechnicianNameById(String technicianId) {
        return technicianRepository.getTechnicianNameById(technicianId);
    }

    // List all skill profiles by filter
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
    public List<TechnicianDTO> getAllTechnicianNamesAndIds(int page, int size, String searchKey) {
        int offset = (page - 1) * size;
        return technicianRepository.getAllTechnicianNamesAndIds(size, offset, searchKey);
    }

    // Get all technician departments for filtering
    public List<String> getUniqueTechnicianDepartments() {
        return technicianRepository.getUniqueTechnicianDepartments();
    }

}
