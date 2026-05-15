package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.hibernate.metamodel.model.convert.spi.JpaAttributeConverter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.dto.DocumentMediaDTO;
import io.sclera.models.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

	@Modifying
	@Transactional
	@Query(value = "INSERT INTO document (id , name, category , description, link, created_email, created_timestamp, encrypted_type) VALUES (?1,?2,?3,?4,?5,?6,?7,?8) "
			+ "ON DUPLICATE KEY UPDATE name = ?2 , category = ?3, description = ?4, link = ?5, encrypted_type = ?8 ", nativeQuery = true)
	void upsertDocument(String id, String name, String category, String description, String link, String username,
						BigInteger createdTimestamp,Integer encryptedType);


	@Modifying
	@Transactional
	@Query(value = "DELETE FROM document WHERE id = ?1", nativeQuery = true)
	void deleteDocumentById(String documentid);


	@Query(nativeQuery = true)
	Set<DocumentMediaDTO> getDocuments(Integer pagesize, Integer offset, String searchkey);

	@Query(nativeQuery = true)
	Set<DocumentMediaDTO> getDocumentsByDeviceIdByPagination(String deviceid, Integer pagesize, Integer offset);

	@Query(nativeQuery = true)
	Set<DocumentMediaDTO> getDocumentsByDeviceId(String deviceid);


	@Modifying
	@Transactional
	@Query(value = "INSERT INTO device_document (document_id , device_id) VALUE (?1,?2)", nativeQuery = true)
	void tagDocumentToDevice(String document_id, String device_id);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM device_document WHERE document_id = ?1 AND device_id = ?2", nativeQuery = true)
	void untagDocumentToDevice(String document_id, String device_id);


	@Modifying
	@Transactional
	@Query(value = "DELETE FROM device_document WHERE document_id = ?1", nativeQuery = true)
	void deleteTagRecordByDocumentId(String document_id);


	@Query(value = "SELECT COUNT(*) FROM device_document WHERE device_id = ?1", nativeQuery = true)
	Integer getDocumentsCountByDeviceId(String device_id);

	@Query(value = "SELECT device_id FROM device_document WHERE document_id = ?1", nativeQuery = true)
	List<String> getDocumentByDeviceId(String document_id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE device_document SET device_id = ?1 WHERE (device_id IS NOT NULL) AND device_id =?2 ", nativeQuery = true)
	void updateDocumentDeviceId(String device_id, String existing_device_id);

	@Query(value = "SELECT link FROM document WHERE id = ?1", nativeQuery = true)
	String getDocumentLinkByDocumentId(String id);

	@Query(nativeQuery = true)
	DocumentMediaDTO getDocumentById(String documentIid);

	@Modifying
	@Transactional
	@Query(value = "UPDATE document SET encrypted_type = ?2 WHERE id = ?1", nativeQuery = true)
	void updateEncryption(String id, Integer encryptedType);
}
