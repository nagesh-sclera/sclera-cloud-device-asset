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

@Service
public class TechnicianCertificateService {
    private static final Logger log = LoggerFactory.getLogger(TechnicianCertificateService.class);

    private final TechnicianCertificateRepository technicianCertificateRepository;

    @Autowired
    public TechnicianCertificateService(TechnicianCertificateRepository technicianCertificateRepository) {
        this.technicianCertificateRepository = technicianCertificateRepository;
    }

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

    public List<TechnicianCertificateDTO> getAllTechnicianCertificates() {
        return  technicianCertificateRepository.getAllTechnicianCertificates();
    }

    public TechnicianCertificateDTO getTechnicianCertificateById(String id) {
        return technicianCertificateRepository.getTechnicianCertificateById(id);
    }

    public Set<String> findExistingTechnicianCertificatesByIds(List<String> ids) {
        Set<String> existingIds = new HashSet<>();
        if (ids != null && !ids.isEmpty()) {
            existingIds = technicianCertificateRepository.findExistingTechnicianCertificatesByIds(ids);
        }
        return existingIds;
    }

    public void deleteTechnicianCertificatesByIds(Set<String> ids) {
        int noOfRecordsDeleted = technicianCertificateRepository.deleteTechnicianCertificatesByIds(ids);
        log.info("No of ids got to delete : {}, Number of TechnicianCertificatesByIds records got deleted : {} ", ids.size(), noOfRecordsDeleted);
    }

}