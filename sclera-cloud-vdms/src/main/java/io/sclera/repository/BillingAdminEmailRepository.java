package io.sclera.repository;

import io.sclera.model.BillingAdminEmail;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BillingAdminEmailRepository extends JpaRepository<BillingAdminEmail,String> {

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO billing_admin_email(email) VALUES (?1) ON DUPLICATE KEY UPDATE email = VALUES(email)",nativeQuery = true)
    void addEmail(String billingAdminEmail);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM billing_admin_email WHERE email IN (:billingAdminEmails)",nativeQuery = true)
    void deleteBillingAdminEmail(@Param("billingAdminEmails")List<String> billingEmail);

    @Query(value = "SELECT * FROM billing_admin_email",nativeQuery = true)
    List<String> findAllBillingEmails();
}
