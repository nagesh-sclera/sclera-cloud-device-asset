package io.sclera.service;
import io.sclera.client.APICallClient;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.sclera.Repository.UserRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.dto.touchscreen.settings.UserDTO;

/**
 * Manages user records and their organisation associations.
 *
 * <p>Persists and queries users via {@link UserRepository}, resolves customer
 * organisation identifiers from VDMS identifiers via {@link VdmsRepository},
 * and fetches remote user data through {@link APICallClient}.
 */
@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    APICallClient apicallService;

    @Autowired
    VdmsRepository vdmsRepository;

    /**
     * Returns the organisation identifier associated with the given user email.
     *
     * @param email the user's email address
     * @return the matching organisation identifier, or {@code null} if none
     */
    public String getOrganisationIdByUserEmail(String email) {
        return userRepository.getOrganisationIdByUserEmail(email);
    }


    /**
     * Synchronises users for an organisation by fetching them remotely and
     * upserting each into the local store (updating existing users, inserting new ones).
     *
     * @param organisation_id the organisation identifier whose users are fetched
     * @param vdms_id the VDMS identifier used for the remote lookup
     */
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
    /**
     * Returns the customer organisation identifier mapped to the given VDMS identifier.
     *
     * @param vdms_id the VDMS identifier
     * @return the matching customer organisation identifier, or {@code null} if none
     */
    public String getCustomerOrgIdByVdmsId(String vdms_id) {
        return vdmsRepository.getCustomerOrgIdByVdmsId(vdms_id);
    }


    /**
     * Deletes all users belonging to the given customer organisation.
     *
     * @param customer_org_id the customer organisation identifier
     */
    public void deleteUsersByOrganisationId(String customer_org_id) {
        userRepository.deleteUsersByOrganisationId(customer_org_id);
    }


    /**
     * Inserts the given user, logging any failure rather than propagating it.
     *
     * @param user the user to insert
     */
    public void insertUsers(UserDTO user) {
        try {
            userRepository.insertUser(user.getEmail(), user.getCompany_name(), user.getCreated_by(), user.getCreation_timestamp(), user.getName(), user.getPhone(),
                    user.getPhone_type(),
                    user.getValue(), user.getWebsite(), user.getOrganisation_id(), user.getImage_url(), user.getLanguage(), user.getRole());
        } catch (Exception e) {
            System.out.println("Error while inserting user: " + e.getMessage());
        }
    }


    /**
     * Returns a paginated, optionally filtered set of all users.
     *
     * @param username the requesting user's name
     * @param vdmsid the VDMS identifier of the request context
     * @param pageno the one-based page number
     * @param pagesize the number of users per page
     * @param searchkey the optional search filter
     * @return the matching page of users
     */
    public Set<UserDTO> getAllUsers(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey) {
        Integer offset = pagesize * (pageno - 1);
        return userRepository.getAllUsers(pagesize, offset, searchkey);
    }

    /**
     * Stamps a creation timestamp and default language on the user, then persists it.
     *
     * @param username the requesting user's name
     * @param vdmsid the VDMS identifier of the request context
     * @param user the user to add
     * @return the added user, with creation timestamp and default language applied
     */
    public UserDTO addUsers(String username, String vdmsid, UserDTO user) {
        user.setCreation_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
        if (user.getLanguage() == null) {
            user.setLanguage("EN");
        }
        this.insertUsers(user);
        return user;

    }

    /**
     * Deletes the users identified by the given set of email addresses.
     *
     * @param username the requesting user's name
     * @param vdmsid the VDMS identifier of the request context
     * @param email_ids the email addresses of the users to delete
     */
    public void deleteUsers(String username, String vdmsid, Set<String> email_ids) {
        for (String email : email_ids) {
            userRepository.deleteById(email);
        }
    }

    /**
     * Applies edits to each user in the given set.
     *
     * @param username the requesting user's name
     * @param vdmsid the VDMS identifier of the request context
     * @param users the users carrying the updated values to persist
     */
    public void editUsers(String username, String vdmsid, Set<UserDTO> users) {
        for (UserDTO user : users) {
            userRepository.editUsers(user.getCompany_name(), user.getName(), user.getPhone(), user.getPhone_type(), user.getValue(), user.getWebsite(), user.getEmail(), user.getLanguage());
        }
    }

    /**
     * Returns a paginated, optionally filtered set of users for the customer
     * organisation resolved from the given VDMS identifier.
     *
     * @param username the requesting user's name
     * @param vdmsid the VDMS identifier used to resolve the customer organisation
     * @param pageno the one-based page number
     * @param pagesize the number of users per page
     * @param searchkey the optional search filter
     * @return the matching page of organisation users
     */
    public Set<UserDTO> getAllOrganisationUsersByPagination(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey) {
        String customer_org_id = this.getCustomerOrgIdByVdmsId(vdmsid);
        Integer offset = pagesize * (pageno - 1);
        return userRepository.getAllOrganisationUsersByPagination(pagesize, offset, searchkey, customer_org_id);
    }

    /**
     * Returns a paginated, optionally filtered set of users outside the current organisation.
     *
     * @param username the requesting user's name
     * @param vdmsid the VDMS identifier of the request context
     * @param pageno the one-based page number
     * @param pagesize the number of users per page
     * @param searchkey the optional search filter
     * @return the matching page of other users
     */
    public Set<UserDTO> getAllOtherUsersByPagination(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey) {
        Integer offset = pagesize * (pageno - 1);
        return userRepository.getAllOtherUsersByPagination(pagesize, offset, searchkey);
    }

    /**
     * Returns the user with the given email address.
     *
     * @param user_email the user's email address
     * @return the matching user, or {@code null} if none
     */
    public UserDTO getUserByEmail(String user_email) {
        return userRepository.getUserByEmail(user_email);
    }

    /**
     * Returns the email addresses of all users.
     *
     * @return the set of all user email addresses
     */
    public Set<String> getAllUsersEmail() {
        return userRepository.getAllUsersEmail();
    }


    /**
     * Updates the full set of fields for the given user, logging any failure
     * rather than propagating it.
     *
     * @param user the user carrying the values to persist
     */
    public void updateAllUser(UserDTO user) {

        try {
            userRepository.updateAllUser(user.getCompany_name(), user.getCreated_by(), user.getName(), user.getPhone(),
                    user.getPhone_type(), user.getValue(), user.getWebsite(), user.getOrganisation_id(), user.getEmail(),
                    user.getImage_url(), user.getLanguage(), user.getRole(), user.getCreation_timestamp());
        } catch (Exception e) {
            System.out.println("Error while updating user: " + e.getMessage());
        }
    }


    /**
     * Returns all users belonging to the given customer organisation.
     *
     * @param customer_org_id the customer organisation identifier
     * @return the users for that organisation
     */
    public List<UserDTO> getAllUsersByOrganisationId(String customer_org_id) {
        return userRepository.getAllUsersByOrganisationId(customer_org_id);
    }

    /**
     * Deletes the user identified by the given email address.
     *
     * @param email_id the email address of the user to delete
     */
    public void deleteUserByEmailId(String email_id) {
        userRepository.deleteById(email_id);
    }

    /**
     * Reassigns users from one customer organisation identifier to another.
     *
     * @param existing_customer_org_id the current customer organisation identifier
     * @param new_customer_org_id the replacement customer organisation identifier
     */
    public void updateCustomerOrgIdForUsers(String existing_customer_org_id, String new_customer_org_id) {
        userRepository.updateCustomerOrgIdForUsers(existing_customer_org_id, new_customer_org_id);
    }

    /**
     * Returns all users.
     *
     * @return the set of all users
     */
    public Set<UserDTO> getUsers() {
        return userRepository.getUsers();
    }

    /**
     * Finds the user matching the given email within the supplied set.
     *
     * @param assignee_email the email address to match
     * @param users the set of users to search
     * @return the matching user, or {@code null} if none
     */
    public UserDTO getUserDetailsByEmail(String assignee_email, Set<UserDTO> users) {
        for (UserDTO user : users) {
            if (user.getEmail().equals(assignee_email)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Returns the name of the user with the given email address.
     *
     * @param email the user's email address
     * @return the user's name, or {@code null} if none
     */
    public String getUserNameByEmail(String email) {
        return userRepository.getUserNameByEmail(email);
    }

    /**
     * Fetches full user information for an organisation from the remote API.
     *
     * @param org_id the organisation identifier
     * @param vdms_id the VDMS identifier used for the remote lookup
     * @return the users returned by the remote API
     */
    public List<UserDTO> getAllUserInfoByOrganisationIdAndVdmsId(String org_id, String vdms_id) {
        return apicallService.getAllUserInfoByOrganisationIdAndVdmsId(org_id, vdms_id);
    }

    /**
     * Returns the roles assigned to the user with the given email address.
     *
     * @param email the user's email address
     * @return the user's roles
     */
    public String getAllUserRoles(String email) {
        return userRepository.getAllUserRoles(email);
    }

    /**
     * Checks whether a user exists with the given email address.
     *
     * @param email the user's email address
     * @return a non-zero count if a matching user exists, otherwise zero
     */
    public int checkUserByEmail(String email){
        return userRepository.checkUserByEmail(email);
    }
}
