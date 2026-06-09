package io.sclera.interfaces;

import io.sclera.dto.TechnicianDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/** Service contract for {@link io.sclera.service.TechnicianService}. */
public interface TechnicianServiceInterface {

    TechnicianDTO getTechnicianById(String id, HttpServletRequest httpServletRequest);

    List<TechnicianDTO> getAllTechnician();

    List<Set> getAllTechniciansEmail();

    Set<String> upsertTechnician(List<TechnicianDTO> technicianDtos);

    Set<String> deleteTechniciansById(List<TechnicianDTO> technicianDtos);

    Set<String> findExistingTechniciansByIds(List<String> ids);

    void updateTechnician(List<TechnicianDTO> technicianDtos, HttpServletRequest httpServletRequest);

    void createTechnician(List<TechnicianDTO> technicianDtos, HttpServletRequest httpServletRequest);

    TechnicianDTO getTechnicianDetailsById(String id);

    TechnicianDTO getTechnicianSkillProfileWithPrimarySkillAndAvailabilityById(String id);

    List<TechnicianDTO> getAllTechnicianSkillProfilesWithPrimarySkillAndAvailability(int size, int page);

    void tagTechniciansToDevice(String deviceId, List<String> technicianIds);

    void unTagTechniciansFromDevice(String deviceId, List<String> technicianIds);

    List<TechnicianDTO> getAllTechniciansByDeviceId(String deviceId);

    List<TechnicianDTO> getAllAvailableTechnicianByDeviceId(String deviceId);

    List<TechnicianDTO> getAvailableTechnicianCountryCodePhoneByDeviceId(String deviceId);

    String getTechnicianNameById(String technicianId);

    List<TechnicianDTO> getAllTechniciansByFilterByPagination(int size, int page, String technicianIdFilter, String departmentFilter, String availabilityFilter);

    List<TechnicianDTO> getAllTechnicianNamesAndIds(int page, int size, String searchKey);

    List<String> getUniqueTechnicianDepartments();
}
