package io.sclera.stubs;

import io.sclera.Repository.UserRepository;
import io.sclera.dto.touchscreen.settings.UserDTO;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class UserRepositoryStub implements UserRepository {

    @Override
    public String getOrganisationIdByUserEmail(String email) { return null; }

    @Override
    public int checkUser(String email, String organisation_id) { return 0; }

    @Override
    public void insertUser(String email, String company_name, String created_by, BigInteger creation_timestamp,
                           String name, String phone, String phone_type, String value, String website,
                           String organisation_id, String image_url, String language, String role) {}

    @Override
    public void updateUser(String company_name, String created_by, String name, String phone,
                           String phone_type, String value, String website, String organisation_id, String email) {}

    @Override
    public void deleteUsersByOrganisationId(String customer_org_id) {}

    @Override
    public Set<UserDTO> getAllUsers(Integer pagesize, Integer offset, String searchkey) {
        return Collections.emptySet();
    }

    @Override
    public void deleteById(String email) {}

    @Override
    public void editUsers(String company_name, String name, String phone, String phone_type,
                          String value, String website, String email, String language) {}

    @Override
    public Set<UserDTO> getAllOrganisationUsersByPagination(Integer pagesize, Integer offset, String searchkey, String customer_org_id) {
        return Collections.emptySet();
    }

    @Override
    public Set<UserDTO> getAllOtherUsersByPagination(Integer pagesize, Integer offset, String searchkey) {
        return Collections.emptySet();
    }

    @Override
    public UserDTO getUserByEmail(String email) { return null; }

    @Override
    public Set<String> getAllUsersEmail() { return Collections.emptySet(); }

    @Override
    public void updateAllUser(String company_name, String created_by, String name, String phone,
                              String phone_type, String value, String website, String organisation_id,
                              String email, String image_url, String language, String role, BigInteger creation_timestamp) {}

    @Override
    public List<UserDTO> getAllUsersByOrganisationId(String customer_org_id) {
        return Collections.emptyList();
    }

    @Override
    public void updateCustomerOrgIdForUsers(String existing_customer_org_id, String new_customer_org_id) {}

    @Override
    public Set<UserDTO> getUsers() { return Collections.emptySet(); }

    @Override
    public String getUserNameByEmail(String email) { return null; }

    @Override
    public String getAllUserRoles(String email) { return null; }

    @Override
    public int checkUserByEmail(String email) { return 0; }

    @Override
    public String getMasterUserEmail() { return null; }
}
