package io.sclera.service;

import io.sclera.dto.TechnicianCertificateDTO;
import java.util.List;
import java.util.Set;

/** Service contract for the matching service class. */
public interface TechnicianCertificateService {
    Set<String> upsertTechnicianCertificate(List<TechnicianCertificateDTO> technicianCertificateDTOS);
    Set<String> deleteTechnicianCertificatesById(List<TechnicianCertificateDTO> technicianCertificateDTOS);
    void createTechnicianCertificate(TechnicianCertificateDTO technicianCertificateDto);
    void updateTechnicianCertificate(TechnicianCertificateDTO technicianCertificateDto);
    List<TechnicianCertificateDTO> getAllTechnicianCertificates();
    TechnicianCertificateDTO getTechnicianCertificateById(String id);
    Set<String> findExistingTechnicianCertificatesByIds(List<String> ids);
    void deleteTechnicianCertificatesByIds(Set<String> ids);
}
