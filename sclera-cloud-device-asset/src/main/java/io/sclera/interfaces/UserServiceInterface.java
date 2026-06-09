package io.sclera.interfaces;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import io.sclera.dto.touchscreen.settings.UserDTO;

/** Service contract for {@link io.sclera.service.UserService}. */
public interface UserServiceInterface {

    String getOrganisationIdByUserEmail(String email);

    void getUserbyOrgId(String organisation_id, String vdms_id);

    String getCustomerOrgIdByVdmsId(String vdms_id);

    void deleteUsersByOrganisationId(String customer_org_id);

    void insertUsers(UserDTO user);

    Set<UserDTO> getAllUsers(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey);

    UserDTO addUsers(String username, String vdmsid, UserDTO user);

    void deleteUsers(String username, String vdmsid, Set<String> email_ids);

    void editUsers(String username, String vdmsid, Set<UserDTO> users);

    Set<UserDTO> getAllOrganisationUsersByPagination(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey);

    Set<UserDTO> getAllOtherUsersByPagination(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey);

    UserDTO getUserByEmail(String user_email);

    Set<String> getAllUsersEmail();

    void updateAllUser(UserDTO user);

    List<UserDTO> getAllUsersByOrganisationId(String customer_org_id);

    void deleteUserByEmailId(String email_id);

    void updateCustomerOrgIdForUsers(String existing_customer_org_id, String new_customer_org_id);

    Set<UserDTO> getUsers();

    UserDTO getUserDetailsByEmail(String assignee_email, Set<UserDTO> users);

    String getUserNameByEmail(String email);

    List<UserDTO> getAllUserInfoByOrganisationIdAndVdmsId(String org_id, String vdms_id);

    String getAllUserRoles(String email);

    int checkUserByEmail(String email);
}
