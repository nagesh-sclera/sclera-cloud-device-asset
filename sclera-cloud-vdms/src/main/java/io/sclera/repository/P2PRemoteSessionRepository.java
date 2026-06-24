package io.sclera.repository;

import io.sclera.dto.P2PRemoteSessionDTO;
import io.sclera.model.P2PRemoteSession;
import io.sclera.model.compositeclass.P2PRemoteSessionIds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;


@Repository
public interface P2PRemoteSessionRepository extends JpaRepository<P2PRemoteSession, P2PRemoteSessionIds> {
	
	@Query(nativeQuery = true)
	P2PRemoteSessionDTO getP2PRemoteSessionByVdmsIdAndVendorEmail(String vdms_id, String vendor_email);

	@Query(value = "SELECT MAX(port) FROM p2premote_session " ,nativeQuery = true)
	Integer getMaxRemotePort();
	
	@Modifying
	@Transactional
	@Query(value = "INSERT INTO p2premote_session(vdms_id,vendor_email,session_id,port) VALUE(?1,?2,?3,?4)" ,nativeQuery = true)
	void addP2PRemoteSessionByVdmsIdAndVendorEmail(String vdms_id, String vendor_email, Integer session_id , Integer port);
	
	@Query(value = "SELECT MAX(session_id) FROM p2premote_session WHERE vdms_id = ?1 AND vendor_email = ?2" ,nativeQuery = true)
	Integer getP2PRemoteSessionIdByVdmsIdAndVendorEmail(String vdms_id, String vendor_email);
	
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM p2premote_session WHERE vdms_id = ?1" ,nativeQuery = true)
	void deleteP2PRemoteSessionByVdmsId(String vdms_id);

}
