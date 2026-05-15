package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.UserDTO;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface UserRepository {

    String getOrganisationIdByUserEmail(String email);

    int checkUser(String email, String organisation_id);

    void insertUser(String email, String company_name, String created_by, BigInteger creation_timestamp,
                    String name, String phone, String phone_type, String value, String website,
                    String organisation_id, String image_url, String language, String role);

    void updateUser(String company_name, String created_by, String name, String phone,
                    String phone_type, String value, String website, String organisation_id, String email);

    void deleteUsersByOrganisationId(String customer_org_id);

    Set<UserDTO> getAllUsers(Integer pagesize, Integer offset, String searchkey);

    void deleteById(String email);

    void editUsers(String company_name, String name, String phone, String phone_type,
                   String value, String website, String email, String language);

    Set<UserDTO> getAllOrganisationUsersByPagination(Integer pagesize, Integer offset, String searchkey, String customer_org_id);

    Set<UserDTO> getAllOtherUsersByPagination(Integer pagesize, Integer offset, String searchkey);

    UserDTO getUserByEmail(String email);

    Set<String> getAllUsersEmail();

    void updateAllUser(String company_name, String created_by, String name, String phone,
                       String phone_type, String value, String website, String organisation_id,
                       String email, String image_url, String language, String role, BigInteger creation_timestamp);

    List<UserDTO> getAllUsersByOrganisationId(String customer_org_id);

    void updateCustomerOrgIdForUsers(String existing_customer_org_id, String new_customer_org_id);

    Set<UserDTO> getUsers();

    String getUserNameByEmail(String email);

    String getAllUserRoles(String email);

    int checkUserByEmail(String email);

    String getMasterUserEmail();
}