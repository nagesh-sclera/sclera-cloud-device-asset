package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.dto.DocumentMediaDTO;
import io.sclera.models.Document;

/**
 * Manages persistence and querying of {@link Document} entities and their device tag associations.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, String> {

	/**
	 * Inserts a document, updating its mutable fields on id conflict.
	 *
	 * @param id the document identifier
	 * @param name the document name
	 * @param category the document category
	 * @param description the document description
	 * @param link the document link
	 * @param username the creating user's email
	 * @param createdTimestamp the creation timestamp
	 * @param encryptedType the encryption type flag
	 */
	@Modifying
	@Transactional
	// NOT CONVERTED — stays native: plain INSERT ON CONFLICT already valid PostgreSQL
	@Query(value = "INSERT INTO document (id , name, category , description, link, created_email, created_timestamp, encrypted_type) VALUES (?1,?2,?3,?4,?5,?6,?7,?8) "
			+ "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, category = EXCLUDED.category, description = EXCLUDED.description, link = EXCLUDED.link, encrypted_type = EXCLUDED.encrypted_type", nativeQuery = true)
	void upsertDocument(String id, String name, String category, String description, String link, String username,
						BigInteger createdTimestamp, Integer encryptedType);


	/**
	 * Deletes the document identified by the given id.
	 *
	 * @param documentid the document identifier to delete
	 */
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("DELETE FROM Document d WHERE d.id = ?1")
	void deleteDocumentById(String documentid);


	/**
	 * Retrieves a paginated, optionally filtered set of document projections.
	 *
	 * @param searchkey the search filter key; pass the literal string {@code 'null'} to skip filtering
	 * @param pageable pagination descriptor (replaces former pagesize/offset params)
	 * @return the matching document projections
	 */
	@Query("SELECT new io.sclera.dto.DocumentMediaDTO(d.id, d.name, d.category, d.link, d.description, d.created_email, d.created_timestamp, d.encrypted_type) "
			+ "FROM Document d "
			+ "WHERE (?1 = 'null' OR CONCAT(COALESCE(d.name,''), COALESCE(d.category,''), COALESCE(d.description,'')) LIKE CONCAT('%', ?1, '%'))")
	List<DocumentMediaDTO> getDocuments(String searchkey, Pageable pageable);

	/**
	 * Retrieves a paginated set of document projections tagged to the given device.
	 *
	 * @param deviceid the device identifier
	 * @param pageable pagination descriptor (replaces former pagesize/offset params)
	 * @return the matching document projections
	 */
	@Query("SELECT new io.sclera.dto.DocumentMediaDTO(d.id, d.name, d.category, d.link, d.description, d.created_email, d.created_timestamp, dev.id, d.encrypted_type) "
			+ "FROM Document d JOIN d.device dev "
			+ "WHERE dev.id = ?1")
	List<DocumentMediaDTO> getDocumentsByDeviceIdByPagination(String deviceid, Pageable pageable);

	/**
	 * Retrieves all document projections tagged to the given device.
	 *
	 * @param deviceid the device identifier
	 * @return the matching document projections
	 */
	@Query("SELECT new io.sclera.dto.DocumentMediaDTO(d.id, d.name, d.category, d.link, d.description, d.created_email, d.created_timestamp, dev.id, d.encrypted_type) "
			+ "FROM Document d JOIN d.device dev "
			+ "WHERE dev.id = ?1")
	Set<DocumentMediaDTO> getDocumentsByDeviceId(String deviceid);


	/**
	 * Tags a document to a device.
	 *
	 * @param document_id the document identifier
	 * @param device_id the device identifier
	 */
	@Modifying
	@Transactional
	// NOT CONVERTED — stays native: plain INSERT into device_document join table (no entity)
	@Query(value = "INSERT INTO device_document (document_id , device_id) VALUES (?1,?2)", nativeQuery = true)
	void tagDocumentToDevice(String document_id, String device_id);

	/**
	 * Removes the tag association between a document and a device.
	 *
	 * @param document_id the document identifier
	 * @param device_id the device identifier
	 */
	@Modifying
	@Transactional
	// NOT CONVERTED — stays native: DELETE on device_document join table (no entity)
	@Query(value = "DELETE FROM device_document WHERE document_id = ?1 AND device_id = ?2", nativeQuery = true)
	void untagDocumentToDevice(String document_id, String device_id);


	/**
	 * Removes all device tag associations for the given document.
	 *
	 * @param document_id the document identifier
	 */
	@Modifying
	@Transactional
	// NOT CONVERTED — stays native: DELETE on device_document join table (no entity)
	@Query(value = "DELETE FROM device_document WHERE document_id = ?1", nativeQuery = true)
	void deleteTagRecordByDocumentId(String document_id);


	/**
	 * Returns the number of documents tagged to the given device.
	 *
	 * @param device_id the device identifier
	 * @return the count of tagged documents
	 */
	// NOT CONVERTED — stays native: COUNT on device_document join table (no entity)
	@Query(value = "SELECT COUNT(*) FROM device_document WHERE device_id = ?1", nativeQuery = true)
	Integer getDocumentsCountByDeviceId(String device_id);

	/**
	 * Returns the device ids tagged to the given document.
	 *
	 * @param document_id the document identifier
	 * @return the list of associated device ids
	 */
	// NOT CONVERTED — stays native: SELECT from device_document join table (no entity)
	@Query(value = "SELECT device_id FROM device_document WHERE document_id = ?1", nativeQuery = true)
	List<String> getDocumentByDeviceId(String document_id);

	/**
	 * Reassigns document tag associations from one device to another.
	 *
	 * @param device_id the new device identifier to assign
	 * @param existing_device_id the existing device identifier to replace
	 */
	@Modifying
	@Transactional
	// NOT CONVERTED — stays native: UPDATE sets a relation FK column on device_document join table (no entity)
	@Query(value = "UPDATE device_document SET device_id = ?1 WHERE (device_id IS NOT NULL) AND device_id =?2 ", nativeQuery = true)
	void updateDocumentDeviceId(String device_id, String existing_device_id);

	/**
	 * Returns the link for the given document.
	 *
	 * @param id the document identifier
	 * @return the document link
	 */
	@Query("SELECT d.link FROM Document d WHERE d.id = ?1")
	String getDocumentLinkByDocumentId(String id);

	/**
	 * Retrieves a single document projection by its identifier.
	 *
	 * @param documentIid the document identifier
	 * @return the matching document projection
	 */
	@Query("SELECT new io.sclera.dto.DocumentMediaDTO(d.id, d.link, d.encrypted_type) "
			+ "FROM Document d WHERE d.id = ?1")
	DocumentMediaDTO getDocumentById(String documentIid);

	/**
	 * Updates the encryption type of the given document.
	 *
	 * @param id the document identifier
	 * @param encryptedType the new encryption type flag
	 */
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query("UPDATE Document d SET d.encrypted_type = ?2 WHERE d.id = ?1")
	void updateEncryption(String id, Integer encryptedType);
}
