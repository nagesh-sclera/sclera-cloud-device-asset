package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.TechnicianCertificateRepository;
import io.sclera.dto.TechnicianAvailabilityDTO;
import io.sclera.dto.TechnicianCertificateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages technician certificate records, providing create, update, upsert,
 * lookup, and delete operations.
 *
 * <p>Delegates all persistence to {@link TechnicianCertificateRepository} and
 * exchanges data using {@link TechnicianCertificateDTO}.
 */
@Service
public class TechnicianCertificateService {
    private static final Logger log = LoggerFactory.getLogger(TechnicianCertificateService.class);

    private final TechnicianCertificateRepository technicianCertificateRepository;

    @Autowired
    public TechnicianCertificateService(TechnicianCertificateRepository technicianCertificateRepository) {
        this.technicianCertificateRepository = technicianCertificateRepository;
    }

    /**
     * Inserts or updates each supplied technician certificate, skipping any that fail.
     *
     * @param technicianCertificateDTOS the certificates to upsert; may be null or empty
     * @return the IDs of the certificates successfully inserted or updated
     */
    public Set<String> upsertTechnicianCertificate(List<TechnicianCertificateDTO> technicianCertificateDTOS) {
        Set<String> insertedTechnicianCertificateIds = new HashSet<>();
        if(technicianCertificateDTOS != null && !technicianCertificateDTOS.isEmpty()) {
            for (TechnicianCertificateDTO technicianCertificateDTO : technicianCertificateDTOS) {
                try {
                    Integer rowsAffected = technicianCertificateRepository.upsertTechnicianCertificate(
                            technicianCertificateDTO.getId(),
                            technicianCertificateDTO.getName(),
                            technicianCertificateDTO.getType(),
                            technicianCertificateDTO.getUrl(),
                            technicianCertificateDTO.getTechnicianId()
                    );
                    if (rowsAffected != null && rowsAffected > 0) {
                        System.out.println("Technician certificate upserted successfully.");
                        insertedTechnicianCertificateIds.add(technicianCertificateDTO.getId());
                    }
                } catch (Exception e) {
                    log.error("Error upserting TechnicianCertificate with ID {}: {}", technicianCertificateDTO.getId(), e.getMessage(), e);
                }
            }
        }
        log.info("Inserted or updated {} Technician Certificate records.", insertedTechnicianCertificateIds.size());
        return insertedTechnicianCertificateIds;
    }

    /**
     * Deletes the technician certificates that exist among the supplied DTOs, matched by ID.
     *
     * @param technicianCertificateDTOS the certificates whose IDs identify records to delete; may be null or empty
     * @return the IDs of the certificates that existed and were deleted
     */
    public Set<String> deleteTechnicianCertificatesById(List<TechnicianCertificateDTO> technicianCertificateDTOS){
        Set<String> existingIds = Set.of();
        if (technicianCertificateDTOS != null && !technicianCertificateDTOS.isEmpty()) {
            List<String> allIds = technicianCertificateDTOS.stream()
                    .map(TechnicianCertificateDTO::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!allIds.isEmpty()) {
                try {
                    existingIds = this.findExistingTechnicianCertificatesByIds(allIds);
                    if (existingIds != null && !existingIds.isEmpty()) {
                        this.deleteTechnicianCertificatesByIds(existingIds);
                        return existingIds;
                    }
                } catch (Exception e) {
                    log.error("Error deleting TechnicianCertificates: {}", e.getMessage(), e);
                }
            }
        }
        return existingIds;
    }
    /**
     * Creates a new technician certificate, assigning it a freshly generated time-based UUID.
     *
     * @param technicianCertificateDto the certificate to create; its ID is overwritten
     */
    public void createTechnicianCertificate(TechnicianCertificateDTO technicianCertificateDto) {
        technicianCertificateDto.setId(Generators.timeBasedGenerator().generate().toString());
        technicianCertificateRepository.createTechnicianCertificate(
                technicianCertificateDto.getId(),
                technicianCertificateDto.getName(),
                technicianCertificateDto.getType(),
                technicianCertificateDto.getUrl(),
                technicianCertificateDto.getTechnicianId()
        );

    }

    /**
     * Updates an existing technician certificate; no-op when the DTO has no ID.
     *
     * @param technicianCertificateDto the certificate to update; must carry an ID
     */
    public void updateTechnicianCertificate(TechnicianCertificateDTO technicianCertificateDto) {
        if(technicianCertificateDto.getId()!=null){
            technicianCertificateRepository.updateTechnicianCertificate(
                    technicianCertificateDto.getId(),
                    technicianCertificateDto.getName(),
                    technicianCertificateDto.getType(),
                    technicianCertificateDto.getUrl(),
                    technicianCertificateDto.getTechnicianId()
            );
        }
        else {
            System.out.println("Technician certificate ID is missing.");
        }
    }

    /**
     * Returns all technician certificates.
     *
     * @return every persisted technician certificate
     */
    public List<TechnicianCertificateDTO> getAllTechnicianCertificates() {
        return  technicianCertificateRepository.getAllTechnicianCertificates();
    }

    /**
     * Returns the technician certificate with the given ID.
     *
     * @param id the certificate ID to look up
     * @return the matching certificate, or null if none exists
     */
    public TechnicianCertificateDTO getTechnicianCertificateById(String id) {
        return technicianCertificateRepository.getTechnicianCertificateById(id);
    }

    /**
     * Returns which of the supplied IDs correspond to existing technician certificates.
     *
     * @param ids the candidate certificate IDs; may be null or empty
     * @return the subset of IDs that exist
     */
    public Set<String> findExistingTechnicianCertificatesByIds(List<String> ids) {
        Set<String> existingIds = new HashSet<>();
        if (ids != null && !ids.isEmpty()) {
            existingIds = technicianCertificateRepository.findExistingTechnicianCertificatesByIds(ids);
        }
        return existingIds;
    }

    /**
     * Deletes the technician certificates with the given IDs.
     *
     * @param ids the certificate IDs to delete
     */
    public void deleteTechnicianCertificatesByIds(Set<String> ids) {
        int noOfRecordsDeleted = technicianCertificateRepository.deleteTechnicianCertificatesByIds(ids);
        log.info("No of ids got to delete : {}, Number of TechnicianCertificatesByIds records got deleted : {} ", ids.size(), noOfRecordsDeleted);
    }

}