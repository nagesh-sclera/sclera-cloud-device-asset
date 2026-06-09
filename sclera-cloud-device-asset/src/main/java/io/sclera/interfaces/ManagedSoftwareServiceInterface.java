package io.sclera.interfaces;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.InventoryApplicationDTO;
import io.sclera.dto.ManagedSoftwareDTO;
import io.sclera.dto.ManagedSoftwareUsersDTO;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Service contract for {@link io.sclera.service.ManagedSoftwareService}. */
public interface ManagedSoftwareServiceInterface {

    List<ManagedSoftwareDTO> getAllManagedSoftwares(String username, String vdmsid, String dockername, String condition, String searchKey, Integer pageNo, Integer pageSize);

    ManagedSoftwareDTO getManagedSoftwareById(String id);

    String insertManagedSoftware(String name, String vendor);

    ManagedSoftwareDTO updateManagedSoftware(String username, String vdmsid, ManagedSoftwareDTO managedSoftwareDTO);

    ManagedSoftwareDTO tagInventoryDetails(String username, String vdmsid, ManagedSoftwareDTO managedSoftwareDTO);

    ManagedSoftwareDTO unTagInventoryDetails(String username, String vdmsid, ManagedSoftwareDTO managedSoftwareDTO);

    List<ManagedSoftwareUsersDTO> getManagedSoftwareUsers(String username, String vdmsid, String dockername, String managedsoftwareId);

    Map<String, Integer> getManagedSoftwareLicense(String username, String vdmsid, String dockername, String managedsoftwareId, String applicationId);

    Map<String, Integer> getManagedSoftwareCount(String username, String vdmsid, String dockername);

    List<Map<String, String>> getAllRiskAndCompliances(String username, String vdmsId, String dockerName, String managedSoftwareId);

    void riskAndComplianceAction(String username, String vdmsId, String dockerName, String managedSoftwareId, JSONObject data);

    String getManagedSoftwareFieldsList(String username, String vdmsId);

    List<String> getManagedSoftwareUsersList(String username, String vdmsId);

    List<String> getManagedSoftwareOSTypesList(String username, String vdmsId);

    void deleteManagedSoftware(String username, String vdmsId, String dockerName, String managedSoftwareId);

    List<InventoryApplicationDTO> getInventoryApplications(String username, String vdmsId, String dockerName);

    Set<String> updateManagedSoftwareDetailsSync(List<InventoryApplicationDTO> applicationDetails);

    Set<String> clearManagedSoftwareDetailsSync(List<InventoryApplicationDTO> applicationDetails);

    boolean updateSingleManagedSoftwareTransaction(InventoryApplicationDTO applicationDTO);

    boolean clearSingleManagedSoftwareTransaction(InventoryApplicationDTO applicationDTO);
}
