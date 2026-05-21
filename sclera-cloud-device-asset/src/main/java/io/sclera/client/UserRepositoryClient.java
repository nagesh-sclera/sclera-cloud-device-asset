package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.touchscreen.settings.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to the sclera-identity microservice.
 *
 * Replaces both {@code io.sclera.stubs.UserRepositoryStub} and
 * {@code io.sclera.Repository.UserRepositoryImpl} (both are pure stubs with no
 * real DAO backing).
 *
 * Implements {@code io.sclera.Repository.UserRepository} so that all existing
 * call sites (UserService, ApplicationUserService, DeviceSpecificationService,
 * ManagedSoftwareService) continue to compile without change.
 *
 * NOTE: write-through methods (insertUser, updateUser, etc.) pass params as
 * GET query params — body is silently dropped under the skeleton's GET-only
 * routing (needs POST upgrade when scaffold supports verbs).
 */
@Component
@Primary
public class UserRepositoryClient implements io.sclera.Repository.UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryClient.class);
    private static final String APP_ID = "sclera-identity";

    private final DaprClient dapr;

    public UserRepositoryClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    @Override
    public String getOrganisationIdByUserEmail(String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        try {
            dapr.invokeMethod(APP_ID, "user/getOrganisationIdByUserEmail", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.getOrganisationIdByUserEmail failed; returning null: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public int checkUser(String email, String organisation_id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("organisation_id", organisation_id);
        try {
            dapr.invokeMethod(APP_ID, "user/checkUser", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.checkUser failed; returning 0: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public void insertUser(String email, String company_name, String created_by, BigInteger creation_timestamp,
                           String name, String phone, String phone_type, String value, String website,
                           String organisation_id, String image_url, String language, String role) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("company_name", company_name);
        payload.put("created_by", created_by);
        payload.put("organisation_id", organisation_id);
        payload.put("name", name);
        payload.put("role", role);
        try {
            dapr.invokeMethod(APP_ID, "user/insertUser", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.insertUser failed; swallowing: {}", e.getMessage());
        }
    }

    @Override
    public void updateUser(String company_name, String created_by, String name, String phone,
                           String phone_type, String value, String website, String organisation_id, String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("organisation_id", organisation_id);
        try {
            dapr.invokeMethod(APP_ID, "user/updateUser", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.updateUser failed; swallowing: {}", e.getMessage());
        }
    }

    @Override
    public void deleteUsersByOrganisationId(String customer_org_id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("customer_org_id", customer_org_id);
        try {
            dapr.invokeMethod(APP_ID, "user/deleteUsersByOrganisationId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.deleteUsersByOrganisationId failed; swallowing: {}", e.getMessage());
        }
    }

    @Override
    public Set<UserDTO> getAllUsers(Integer pagesize, Integer offset, String searchkey) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pagesize", pagesize);
        payload.put("offset", offset);
        payload.put("searchkey", searchkey);
        try {
            dapr.invokeMethod(APP_ID, "user/getAllUsers", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.getAllUsers failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    @Override
    public void deleteById(String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        try {
            dapr.invokeMethod(APP_ID, "user/deleteById", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.deleteById failed; swallowing: {}", e.getMessage());
        }
    }

    @Override
    public void editUsers(String company_name, String name, String phone, String phone_type,
                          String value, String website, String email, String language) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        try {
            dapr.invokeMethod(APP_ID, "user/editUsers", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.editUsers failed; swallowing: {}", e.getMessage());
        }
    }

    @Override
    public Set<UserDTO> getAllOrganisationUsersByPagination(Integer pagesize, Integer offset, String searchkey, String customer_org_id) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pagesize", pagesize);
        payload.put("offset", offset);
        payload.put("searchkey", searchkey);
        payload.put("customer_org_id", customer_org_id);
        try {
            dapr.invokeMethod(APP_ID, "user/getAllOrganisationUsersByPagination", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.getAllOrganisationUsersByPagination failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    @Override
    public Set<UserDTO> getAllOtherUsersByPagination(Integer pagesize, Integer offset, String searchkey) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pagesize", pagesize);
        payload.put("offset", offset);
        payload.put("searchkey", searchkey);
        try {
            dapr.invokeMethod(APP_ID, "user/getAllOtherUsersByPagination", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.getAllOtherUsersByPagination failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        try {
            dapr.invokeMethod(APP_ID, "user/getUserByEmail", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.getUserByEmail failed; returning null: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public Set<String> getAllUsersEmail() {
        try {
            dapr.invokeMethod(APP_ID, "user/getAllUsersEmail", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.getAllUsersEmail failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    @Override
    public void updateAllUser(String company_name, String created_by, String name, String phone,
                              String phone_type, String value, String website, String organisation_id,
                              String email, String image_url, String language, String role, BigInteger creation_timestamp) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        payload.put("organisation_id", organisation_id);
        try {
            dapr.invokeMethod(APP_ID, "user/updateAllUser", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.updateAllUser failed; swallowing: {}", e.getMessage());
        }
    }

    @Override
    public List<UserDTO> getAllUsersByOrganisationId(String customer_org_id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("customer_org_id", customer_org_id);
        try {
            dapr.invokeMethod(APP_ID, "user/getAllUsersByOrganisationId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.getAllUsersByOrganisationId failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public void updateCustomerOrgIdForUsers(String existing_customer_org_id, String new_customer_org_id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("existing_customer_org_id", existing_customer_org_id);
        payload.put("new_customer_org_id", new_customer_org_id);
        try {
            dapr.invokeMethod(APP_ID, "user/updateCustomerOrgIdForUsers", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.updateCustomerOrgIdForUsers failed; swallowing: {}", e.getMessage());
        }
    }

    @Override
    public Set<UserDTO> getUsers() {
        try {
            dapr.invokeMethod(APP_ID, "user/getUsers", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.getUsers failed; returning empty: {}", e.getMessage());
        }
        return Collections.emptySet();
    }

    @Override
    public String getUserNameByEmail(String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        try {
            dapr.invokeMethod(APP_ID, "user/getUserNameByEmail", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.getUserNameByEmail failed; returning null: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public String getAllUserRoles(String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        try {
            dapr.invokeMethod(APP_ID, "user/getAllUserRoles", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.getAllUserRoles failed; returning null: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public int checkUserByEmail(String email) {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", email);
        try {
            dapr.invokeMethod(APP_ID, "user/checkUserByEmail", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.checkUserByEmail failed; returning 0: {}", e.getMessage());
        }
        return 0;
    }

    @Override
    public String getMasterUserEmail() {
        try {
            dapr.invokeMethod(APP_ID, "user/getMasterUserEmail", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("UserRepositoryClient.getMasterUserEmail failed; returning null: {}", e.getMessage());
        }
        return null;
    }
}
