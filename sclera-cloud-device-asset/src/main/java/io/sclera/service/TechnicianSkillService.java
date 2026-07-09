package io.sclera.service;

import io.sclera.dto.TechnicianSkillDTO;
import java.util.List;
import java.util.Set;

/** Service contract for the matching service class. */
public interface TechnicianSkillService {
    Set<String> upsertTechnicianSkill(List<TechnicianSkillDTO> technicianSkillDTOs);
    void createTechnicianSkill(TechnicianSkillDTO technicianSkillDto);
    void updateTechnicianSkill(TechnicianSkillDTO technicianSkillDto);
    TechnicianSkillDTO getTechnicianSkillById(String id);
    List<TechnicianSkillDTO> getAllTechnicianSkill();
    Set<String> deleteTechnicianSkillsById(List<TechnicianSkillDTO> technicianSkillDTOS);
}
