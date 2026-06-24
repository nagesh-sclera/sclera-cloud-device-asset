package io.sclera.repository;

import io.sclera.dto.P2PRemoteSessionDTO;
import io.sclera.model.AdminP2PRemoteSession;
import io.sclera.model.compositeclass.AdminP2PRemoteSessionIds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.List;

@Repository
public interface AdminP2PRemoteSessionRepository extends JpaRepository<AdminP2PRemoteSession , AdminP2PRemoteSessionIds> {

    @Query(nativeQuery = true)
    P2PRemoteSessionDTO getPortAndSessionIdByAdminEmailAndDevUID(String admin_email, String devuid , String access_port);

    @Query(value = "SELECT MAX(port) FROM adminp2premote_session " ,nativeQuery = true)
    Integer getMaxRemotePort();

    @Query(value = "SELECT MAX(session_id) FROM adminp2premote_session WHERE admin_email = ?1 AND devuid = ?2" ,nativeQuery = true)
    Integer getAdminP2PRemoteSessionIdByAdminEmailAndDevUID(String admin_email, String devuid);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO adminp2premote_session(admin_email,devuid,session_id,port) VALUE(?1,?2,?3,?4)" ,nativeQuery = true)
    void addAdminP2PRemoteSessionByAdminEmailAndDevUID(String admin_email, String devuid, Integer randomSessionId, int i);

    @Modifying
    @Transactional
    @Query(value = "UPDATE adminp2premote_session SET port = ?1 WHERE admin_email = ?2 AND devuid = ?3" ,nativeQuery = true)
    void updatePortByAdminEmailAndDevUID(int i, String admin_email, String devuid);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM adminp2premote_session WHERE admin_email = ?1" ,nativeQuery = true)
    void clearSessionsByAdminEmail(String admin_email);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM adminp2premote_session " ,nativeQuery = true)
    void clearAllSessions();

}
