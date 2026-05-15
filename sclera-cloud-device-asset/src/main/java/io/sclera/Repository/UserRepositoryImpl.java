package io.sclera.Repository;
import io.sclera.dto.touchscreen.settings.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;
@Repository("userRepositoryImpl")
@Primary
public class UserRepositoryImpl implements UserRepository {
    private static final Logger log = LoggerFactory.getLogger(UserRepositoryImpl.class);
    @Override public String getOrganisationIdByUserEmail(String email) { log.warn("[STUB] UserRepository.getOrganisationIdByUserEmail"); return null; }
    @Override public int checkUser(String email, String organisation_id) { log.warn("[STUB] UserRepository.checkUser"); return 0; }
    @Override public void insertUser(String email, String company_name, String created_by, BigInteger creation_timestamp, String name, String phone, String phone_type, String value, String website, String organisation_id, String image_url, String language, String role) { log.warn("[STUB] UserRepository.insertUser"); }
    @Override public void updateUser(String company_name, String created_by, String name, String phone, String phone_type, String value, String website, String organisation_id, String email) { log.warn("[STUB] UserRepository.updateUser"); }
    @Override public void deleteUsersByOrganisationId(String customer_org_id) { log.warn("[STUB] UserRepository.deleteUsersByOrganisationId"); }
    @Override public Set<UserDTO> getAllUsers(Integer pagesize, Integer offset, String searchkey) { log.warn("[STUB] UserRepository.getAllUsers"); return Collections.emptySet(); }
    @Override public void deleteById(String email) { log.warn("[STUB] UserRepository.deleteById"); }
    @Override public void editUsers(String company_name, String name, String phone, String phone_type, String value, String website, String email, String language) { log.warn("[STUB] UserRepository.editUsers"); }
    @Override public Set<UserDTO> getAllOrganisationUsersByPagination(Integer pagesize, Integer offset, String searchkey, String customer_org_id) { log.warn("[STUB] UserRepository.getAllOrganisationUsersByPagination"); return Collections.emptySet(); }
    @Override public Set<UserDTO> getAllOtherUsersByPagination(Integer pagesize, Integer offset, String searchkey) { log.warn("[STUB] UserRepository.getAllOtherUsersByPagination"); return Collections.emptySet(); }
    @Override public UserDTO getUserByEmail(String email) { log.warn("[STUB] UserRepository.getUserByEmail"); return null; }
    @Override public Set<String> getAllUsersEmail() { log.warn("[STUB] UserRepository.getAllUsersEmail"); return Collections.emptySet(); }
    @Override public void updateAllUser(String company_name, String created_by, String name, String phone, String phone_type, String value, String website, String organisation_id, String email, String image_url, String language, String role, BigInteger creation_timestamp) { log.warn("[STUB] UserRepository.updateAllUser"); }
    @Override public List<UserDTO> getAllUsersByOrganisationId(String customer_org_id) { log.warn("[STUB] UserRepository.getAllUsersByOrganisationId"); return Collections.emptyList(); }
    @Override public void updateCustomerOrgIdForUsers(String existing_customer_org_id, String new_customer_org_id) { log.warn("[STUB] UserRepository.updateCustomerOrgIdForUsers"); }
    @Override public Set<UserDTO> getUsers() { log.warn("[STUB] UserRepository.getUsers"); return Collections.emptySet(); }
    @Override public String getUserNameByEmail(String email) { log.warn("[STUB] UserRepository.getUserNameByEmail"); return null; }
    @Override public String getAllUserRoles(String email) { log.warn("[STUB] UserRepository.getAllUserRoles"); return null; }
    @Override public int checkUserByEmail(String email) { log.warn("[STUB] UserRepository.checkUserByEmail"); return 0; }
    @Override public String getMasterUserEmail() { log.warn("[STUB] UserRepository.getMasterUserEmail"); return null; }
}
