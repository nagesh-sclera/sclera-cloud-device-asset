package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.UserDTO;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

/** STUB Repository: real impl deferred to Phase 2 */
@Repository
public interface UserRepository {

    /**
     * Returns the organisation identifier of the user with the given email.
     *
     * @param email the user email
     * @return the organisation identifier
     */
    String getOrganisationIdByUserEmail(String email);

    /**
     * Returns whether a user exists for the given email and organisation.
     *
     * @param email the user email
     * @param organisation_id the organisation identifier
     * @return a non-zero value if the user exists, otherwise zero
     */
    int checkUser(String email, String organisation_id);

    /**
     * Inserts a new user with the given details.
     *
     * @param email the user email
     * @param company_name the company name
     * @param created_by the identifier of the creating user
     * @param creation_timestamp the creation timestamp
     * @param name the user name
     * @param phone the phone number
     * @param phone_type the phone type
     * @param value the contact value
     * @param website the website
     * @param organisation_id the organisation identifier
     * @param image_url the profile image URL
     * @param language the preferred language
     * @param role the user role
     */
    void insertUser(String email, String company_name, String created_by, BigInteger creation_timestamp,
                    String name, String phone, String phone_type, String value, String website,
                    String organisation_id, String image_url, String language, String role);

    /**
     * Updates the core profile fields of the user with the given email.
     *
     * @param company_name the company name
     * @param created_by the identifier of the creating user
     * @param name the user name
     * @param phone the phone number
     * @param phone_type the phone type
     * @param value the contact value
     * @param website the website
     * @param organisation_id the organisation identifier
     * @param email the email identifying the user
     */
    void updateUser(String company_name, String created_by, String name, String phone,
                    String phone_type, String value, String website, String organisation_id, String email);

    /**
     * Deletes all users belonging to the given organisation.
     *
     * @param customer_org_id the customer organisation identifier
     */
    void deleteUsersByOrganisationId(String customer_org_id);

    /**
     * Returns a paginated, filtered set of all users.
     *
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @param searchkey the search key to match
     * @return the matching users for the page
     */
    Set<UserDTO> getAllUsers(Integer pagesize, Integer offset, String searchkey);

    /**
     * Deletes the user with the given email.
     *
     * @param email the user email
     */
    void deleteById(String email);

    /**
     * Updates the editable profile fields of the user with the given email.
     *
     * @param company_name the company name
     * @param name the user name
     * @param phone the phone number
     * @param phone_type the phone type
     * @param value the contact value
     * @param website the website
     * @param email the email identifying the user
     * @param language the preferred language
     */
    void editUsers(String company_name, String name, String phone, String phone_type,
                   String value, String website, String email, String language);

    /**
     * Returns a paginated, filtered set of users in the given organisation.
     *
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @param searchkey the search key to match
     * @param customer_org_id the customer organisation identifier
     * @return the matching users for the page
     */
    Set<UserDTO> getAllOrganisationUsersByPagination(Integer pagesize, Integer offset, String searchkey, String customer_org_id);

    /**
     * Returns a paginated, filtered set of users outside the current organisation.
     *
     * @param pagesize the maximum number of rows to return
     * @param offset the starting row offset
     * @param searchkey the search key to match
     * @return the matching users for the page
     */
    Set<UserDTO> getAllOtherUsersByPagination(Integer pagesize, Integer offset, String searchkey);

    /**
     * Returns the user with the given email.
     *
     * @param email the user email
     * @return the matching user
     */
    UserDTO getUserByEmail(String email);

    /**
     * Returns the email addresses of all users.
     *
     * @return the set of user emails
     */
    Set<String> getAllUsersEmail();

    /**
     * Inserts or updates the full set of fields of the user with the given email.
     *
     * @param company_name the company name
     * @param created_by the identifier of the creating user
     * @param name the user name
     * @param phone the phone number
     * @param phone_type the phone type
     * @param value the contact value
     * @param website the website
     * @param organisation_id the organisation identifier
     * @param email the email identifying the user
     * @param image_url the profile image URL
     * @param language the preferred language
     * @param role the user role
     * @param creation_timestamp the creation timestamp
     */
    void updateAllUser(String company_name, String created_by, String name, String phone,
                       String phone_type, String value, String website, String organisation_id,
                       String email, String image_url, String language, String role, BigInteger creation_timestamp);

    /**
     * Returns all users belonging to the given organisation.
     *
     * @param customer_org_id the customer organisation identifier
     * @return the matching users
     */
    List<UserDTO> getAllUsersByOrganisationId(String customer_org_id);

    /**
     * Reassigns users from one customer organisation to another.
     *
     * @param existing_customer_org_id the current customer organisation identifier
     * @param new_customer_org_id the new customer organisation identifier
     */
    void updateCustomerOrgIdForUsers(String existing_customer_org_id, String new_customer_org_id);

    /**
     * Returns all users.
     *
     * @return all users
     */
    Set<UserDTO> getUsers();

    /**
     * Returns the name of the user with the given email.
     *
     * @param email the user email
     * @return the user name
     */
    String getUserNameByEmail(String email);

    /**
     * Returns the roles of the user with the given email.
     *
     * @param email the user email
     * @return the user roles
     */
    String getAllUserRoles(String email);

    /**
     * Returns whether a user exists for the given email.
     *
     * @param email the user email
     * @return a non-zero value if the user exists, otherwise zero
     */
    int checkUserByEmail(String email);

    /**
     * Returns the email address of the master user.
     *
     * @return the master user email
     */
    String getMasterUserEmail();
}