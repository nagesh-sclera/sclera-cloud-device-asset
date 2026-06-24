package io.sclera.repository;

import io.sclera.dto.UserDTO;
import io.sclera.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;


@Repository
public interface UserRepository extends JpaRepository<User, String> {

    @Query(value = "SELECT COUNT(email) FROM user WHERE email = ?1", nativeQuery = true)
    int checkUser(String email);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user(email ,company_name ,creation_timestamp ,name ,phone ,phone_type ,value ,website ,"
            + "address_id ,role ,image_url ,customer_org_id,time_zone,language) VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,?14)", nativeQuery = true)
    void createUserByEmail(String email, String company_name, BigInteger creation_timestamp, String name, String phone,
                           String phone_type, String value, String website, String address_id, String role, String image_url, String customer_org_id, String timeZone, String language);


    @Query(value = "SELECT email FROM user WHERE customer_org_id = ?1 AND email != ?2 ", nativeQuery = true)
    Set<String> getUserEmailsByOrganisationId(String organisation_id, String user_email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET phone_type = ?1 , value = ?2 ,name = ?3 ,phone = ?4 ,company_name = ?5 ,website = ?6 ," +
            " role = ?7 ,image_url = ?8 , time_zone = ?9,language = ?10 WHERE email = ?11", nativeQuery = true)
    void updateUserByEmail(String phone_type, String value, String name, String phone, String company_name, String website,
                           String role, String image_url, String timeZone, String language, String email);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user WHERE email = ?1", nativeQuery = true)
    void deleteUserByEmail(String username);

    @Query(value = "SELECT role FROM user WHERE email = ?1", nativeQuery = true)
    String getRoleNameByUserEmail(String username);

    @Query(nativeQuery = true)
    List<UserDTO> getAllUserInfoByOrganisationId(String customer_org_id, String key, int pagesize, int offset);

    @Query(nativeQuery = true)
    UserDTO getMasterUserInfoByVdmsId(String vdmsid);

    @Query(value = "SELECT u.email " +
            " FROM user u " +
            " LEFT JOIN address a ON a.id = u.address_id " +
            " LEFT JOIN customer_organisation co ON u.customer_org_id = co.id " +
            " WHERE u.customer_org_id = ?1 AND u.role = 'master-user'  ",nativeQuery = true)
    String getMasterUserEmailByOrganisationId(String orgId);


    @Query(value = "SELECT customer_org_id FROM user WHERE email = ?1", nativeQuery = true)
    String getCustomerOrganisationIdByUserEmail(String created_by);

    @Query(value = "SELECT u.email FROM user u LEFT JOIN customer_organisation co ON u.customer_org_id = co.id WHERE u.email = ?1 AND co.id IN (SELECT customer_org_id FROM vdms WHERE id = ?2) ", nativeQuery = true)
    String getCustomerEmailByVdmsId(String email, String vdms_id);

    @Query(value = "SELECT COUNT(email) FROM user WHERE role = 'super-admin'", nativeQuery = true)
    Integer checkSuperAdmin();



    @Query(nativeQuery = true)
    UserDTO getUserDetailsByOrganisationIdAndUserEmailAndVdmsId(String response, String email, String vdms_id);

    @Query(nativeQuery = true)
    UserDTO getMasterUserDetailsByVdmsId(String vdms_id);

    @Query(value = "SELECT image_url FROM user WHERE email = ?1", nativeQuery = true)
    String getImageUrlByEmail(String email);


    @Query(value = "SELECT u.email " +
            " FROM user u " +
            " LEFT JOIN customer_organisation co ON co.id = u.customer_org_id " +
            " LEFT JOIN address a ON a.id = u.address_id " +
            " LEFT JOIN vdms_visibility vv ON u.email = vv.email AND vv.vdms_id= ?2 Where u.customer_org_id = ?1 AND (u.role = 'master-user' OR u.role = 'org-admin' OR vv.email IS NOT NULL) ORDER BY u.email DESC ", nativeQuery = true)
    List<String> getAllUserEmailByOrgIdAndVdmsId(String orgId, String vdmsId);

    @Query(value = "SELECT u.email FROM user u LEFT JOIN vdms v ON v.customer_org_id  = u.customer_org_id where v.id = ?1 AND u.customer_org_id =?2 AND u.email = ?3", nativeQuery = true)
    String checkVdmsVisbilityByEmail(String vdmsId, String orgId, String email);

    @Query(value = "SELECT language from user WHERE email= ?1", nativeQuery = true)
    String getLanguageByEmail(String email);



    @Query(value = "SELECT u.email FROM user u " +
            " LEFT JOIN customer_organisation co ON co.id = u.customer_org_id " +
            " LEFT JOIN vdms_visibility vv ON u.email = vv.email AND vv.vdms_id= ?1 " +
            " Where u.customer_org_id = ?2 AND (u.role = 'master-user' OR u.role = 'org-admin' OR vv.email IS NOT NULL)", nativeQuery = true)
    List<String> getUserList(String vdmsId, String orgId);

    @Query(value = "SELECT u.email " +
            "FROM user u " +
            "LEFT JOIN customer_organisation co ON co.id = u.customer_org_id " +
            "LEFT JOIN vdms_visibility vv ON u.email = vv.email AND vv.vdms_id= ?2 " +
            "Where u.customer_org_id = ?1 AND (u.role = 'master-user' OR u.role = 'org-admin' OR vv.email IS NOT NULL) ORDER BY u.email DESC", nativeQuery = true)
    List<String> getAllemailIdsByVdmsVisiblity(String orgId, String vdmsId);



    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user (id,email,role,customer_org_id,creation_timestamp) VALUE (?1,?2,?3,?4,?5)", nativeQuery = true)
    void createNewUserByEmail(String id, String email, String role, String customerOrgId, BigInteger creation_timestamp);

    @Query(value = "SELECT email FROM user u" +
            " LEFT JOIN vdms v ON u.customer_org_id = v.customer_org_id " +
            " LEFT JOIN customer_organisation co ON u.customer_org_id = co.id " +
            " WHERE u.role = 'master-user' AND v.id = ?1 ", nativeQuery = true)
    String getMasterUserEmailByVdmsId(String vdmsId);

    @Query(nativeQuery = true)
    List<UserDTO> getAllUserInfoByOrgIds(List<String> orgIds);

    @Query(nativeQuery = true)
    List<UserDTO> getAllUserInfoWithoutOrg();

    @Query(nativeQuery = true)
    List<UserDTO> getAllUserListByOrganisationId(String customer_org_id, String key, int pageSize, int offset);

    @Query(nativeQuery = true)
    List<UserDTO> getAllUserListBySuperAdminEmail(String key, int pageSize, int offset);

    @Query(nativeQuery = true)
    List<UserDTO> getAllUserListByAdminEmail(String key, int pageSize, int offset);

    @Query(nativeQuery = true)
    List<UserDTO> getAllUserInfoByOrgId(String customer_org_id);
    @Query(nativeQuery = true)
    List<UserDTO> getAllUserInfoByOrganisationIdAndVdmsId(String customer_org_id, String vdmsId);

    @Query(nativeQuery = true)
    List<UserDTO> getAllUserListByOrgAdmin(String customer_org_id, String key, int pageSize, int offset);

    @Query(nativeQuery = true)
    UserDTO getMasterUserInfoByOrganisationId(String customer_org_id);

    @Query(nativeQuery = true)
    UserDTO getUserDetailsByUserEmail(String user_email);

    @Query(nativeQuery = true)
    List<UserDTO> getUrlByEmails();

    @Query(value = "SELECT COUNT(*) FROM user u WHERE (?1 = 'all' OR CONCAT_WS('',u.name,u.email,u.role) LIKE CONCAT('%',?1,'%'))", nativeQuery = true)
    int getUserCount(String key);

    @Query(value = "SELECT time_zone FROM user WHERE email = ?1 ", nativeQuery = true)
    String getUserTimeZoneByEmail(String email);

    @Query(value = "SELECT email FROM user WHERE customer_org_id = ?1 AND role = 'master-user'", nativeQuery = true)
    String getMasterUserEmailsByUserOrganisationId(String customerOrgId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET role = ?1 WHERE email = ?2",nativeQuery = true)
    void updateRoleByEmail(String roleName, String email);

    @Query(nativeQuery = true)
    List<UserDTO> getCreationTimeStamps();
}
