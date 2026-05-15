package io.sclera.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.sclera.Repository.UserRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.dto.touchscreen.settings.UserDTO;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    APICallService apicallService;

    @Autowired
    VdmsRepository vdmsRepository;

    public String getOrganisationIdByUserEmail(String email) {
        return userRepository.getOrganisationIdByUserEmail(email);
    }


    public void getUserbyOrgId(String organisation_id, String vdms_id) {

        List<UserDTO> users = apicallService.getUsersByOrgId(organisation_id, vdms_id);

        for (UserDTO user : users) {
            if (userRepository.checkUser(user.getEmail(), user.getOrganisation_id()) != 0) {
                userRepository.updateUser(user.getCompany_name(), user.getCreated_by(), user.getName(), user.getPhone(),
                        user.getPhone_type(), user.getValue(), user.getWebsite(), user.getOrganisation_id(), user.getEmail());
            } else {
                userRepository.insertUser(user.getEmail(), user.getCompany_name(), user.getCreated_by(), user.getCreation_timestamp(), user.getName(), user.getPhone(),
                        user.getPhone_type(), user.getValue(), user.getWebsite(), user.getOrganisation_id(), user.getImage_url(), user.getLanguage(), user.getRole());
            }
        }

    }


    //Get Customer Organisation Id from VDMS Id
    //Need to moved to vdms Service in Futute, direct vdms repository accessed in user service
    public String getCustomerOrgIdByVdmsId(String vdms_id) {
        return vdmsRepository.getCustomerOrgIdByVdmsId(vdms_id);
    }


    public void deleteUsersByOrganisationId(String customer_org_id) {
        userRepository.deleteUsersByOrganisationId(customer_org_id);
    }


    public void insertUsers(UserDTO user) {
        try {
            userRepository.insertUser(user.getEmail(), user.getCompany_name(), user.getCreated_by(), user.getCreation_timestamp(), user.getName(), user.getPhone(),
                    user.getPhone_type(),
                    user.getValue(), user.getWebsite(), user.getOrganisation_id(), user.getImage_url(), user.getLanguage(), user.getRole());
        } catch (Exception e) {
            System.out.println("Error while inserting user: " + e.getMessage());
        }
    }


    public Set<UserDTO> getAllUsers(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey) {
        Integer offset = pagesize * (pageno - 1);
        return userRepository.getAllUsers(pagesize, offset, searchkey);
    }

    public UserDTO addUsers(String username, String vdmsid, UserDTO user) {
        user.setCreation_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
        if (user.getLanguage() == null) {
            user.setLanguage("EN");
        }
        this.insertUsers(user);
        return user;

    }

    public void deleteUsers(String username, String vdmsid, Set<String> email_ids) {
        for (String email : email_ids) {
            userRepository.deleteById(email);
        }
    }

    public void editUsers(String username, String vdmsid, Set<UserDTO> users) {
        for (UserDTO user : users) {
            userRepository.editUsers(user.getCompany_name(), user.getName(), user.getPhone(), user.getPhone_type(), user.getValue(), user.getWebsite(), user.getEmail(), user.getLanguage());
        }
    }

    public Set<UserDTO> getAllOrganisationUsersByPagination(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey) {
        String customer_org_id = this.getCustomerOrgIdByVdmsId(vdmsid);
        Integer offset = pagesize * (pageno - 1);
        return userRepository.getAllOrganisationUsersByPagination(pagesize, offset, searchkey, customer_org_id);
    }

    public Set<UserDTO> getAllOtherUsersByPagination(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey) {
        Integer offset = pagesize * (pageno - 1);
        return userRepository.getAllOtherUsersByPagination(pagesize, offset, searchkey);
    }

    public UserDTO getUserByEmail(String user_email) {
        return userRepository.getUserByEmail(user_email);
    }

    public Set<String> getAllUsersEmail() {
        return userRepository.getAllUsersEmail();
    }


    public void updateAllUser(UserDTO user) {

        try {
            userRepository.updateAllUser(user.getCompany_name(), user.getCreated_by(), user.getName(), user.getPhone(),
                    user.getPhone_type(), user.getValue(), user.getWebsite(), user.getOrganisation_id(), user.getEmail(),
                    user.getImage_url(), user.getLanguage(), user.getRole(), user.getCreation_timestamp());
        } catch (Exception e) {
            System.out.println("Error while updating user: " + e.getMessage());
        }
    }


    public List<UserDTO> getAllUsersByOrganisationId(String customer_org_id) {
        return userRepository.getAllUsersByOrganisationId(customer_org_id);
    }

    public void deleteUserByEmailId(String email_id) {
        userRepository.deleteById(email_id);
    }

    public void updateCustomerOrgIdForUsers(String existing_customer_org_id, String new_customer_org_id) {
        userRepository.updateCustomerOrgIdForUsers(existing_customer_org_id, new_customer_org_id);
    }

    public Set<UserDTO> getUsers() {
        return userRepository.getUsers();
    }

    public UserDTO getUserDetailsByEmail(String assignee_email, Set<UserDTO> users) {
        for (UserDTO user : users) {
            if (user.getEmail().equals(assignee_email)) {
                return user;
            }
        }
        return null;
    }

    public String getUserNameByEmail(String email) {
        return userRepository.getUserNameByEmail(email);
    }

    public List<UserDTO> getAllUserInfoByOrganisationIdAndVdmsId(String org_id, String vdms_id) {
        return apicallService.getAllUserInfoByOrganisationIdAndVdmsId(org_id, vdms_id);
    }

    public String getAllUserRoles(String email) {
        return userRepository.getAllUserRoles(email);
    }

    public int checkUserByEmail(String email){
        return userRepository.checkUserByEmail(email);
    }
}
