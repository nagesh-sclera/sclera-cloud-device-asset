package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import jakarta.transaction.Transactional;

import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
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
	// PG-port: ON DUPLICATE KEY -> ON CONFLICT (id) DO UPDATE SET (VALUES->EXCLUDED)
	@Query(value = "INSERT INTO document (id , name, category , description, link, created_email, created_timestamp, encrypted_type) VALUES (?1,?2,?3,?4,?5,?6,?7,?8) "
			+ "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, category = EXCLUDED.category, description = EXCLUDED.description, link = EXCLUDED.link, encrypted_type = EXCLUDED.encrypted_type", nativeQuery = true)
	void upsertDocument(String id, String name, String category, String description, String link, String username,
						BigInteger createdTimestamp,Integer encryptedType);


	/**
	 * Deletes the document identified by the given id.
	 *
	 * @param documentid the document identifier to delete
	 */
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM document WHERE id = ?1", nativeQuery = true)
	void deleteDocumentById(String documentid);


	/**
	 * Retrieves a paginated, optionally filtered set of document projections.
	 *
	 * @param pagesize the maximum number of results
	 * @param offset the result offset
	 * @param searchkey the search filter key
	 * @return the matching document projections
	 */
	@Query(nativeQuery = true)
	Set<DocumentMediaDTO> getDocuments(Integer pagesize, Integer offset, String searchkey);

	/**
	 * Retrieves a paginated set of document projections tagged to the given device.
	 *
	 * @param deviceid the device identifier
	 * @param pagesize the maximum number of results
	 * @param offset the result offset
	 * @return the matching document projections
	 */
	@Query(nativeQuery = true)
	Set<DocumentMediaDTO> getDocumentsByDeviceIdByPagination(String deviceid, Integer pagesize, Integer offset);

	/**
	 * Retrieves all document projections tagged to the given device.
	 *
	 * @param deviceid the device identifier
	 * @return the matching document projections
	 */
	@Query(nativeQuery = true)
	Set<DocumentMediaDTO> getDocumentsByDeviceId(String deviceid);


	/**
	 * Tags a document to a device.
	 *
	 * @param document_id the document identifier
	 * @param device_id the device identifier
	 */
	@Modifying
	@Transactional
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
	@Query(value = "DELETE FROM device_document WHERE document_id = ?1 AND device_id = ?2", nativeQuery = true)
	void untagDocumentToDevice(String document_id, String device_id);


	/**
	 * Removes all device tag associations for the given document.
	 *
	 * @param document_id the document identifier
	 */
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM device_document WHERE document_id = ?1", nativeQuery = true)
	void deleteTagRecordByDocumentId(String document_id);


	/**
	 * Returns the number of documents tagged to the given device.
	 *
	 * @param device_id the device identifier
	 * @return the count of tagged documents
	 */
	@Query(value = "SELECT COUNT(*) FROM device_document WHERE device_id = ?1", nativeQuery = true)
	Integer getDocumentsCountByDeviceId(String device_id);

	/**
	 * Returns the device ids tagged to the given document.
	 *
	 * @param document_id the document identifier
	 * @return the list of associated device ids
	 */
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
	@Query(value = "UPDATE device_document SET device_id = ?1 WHERE (device_id IS NOT NULL) AND device_id =?2 ", nativeQuery = true)
	void updateDocumentDeviceId(String device_id, String existing_device_id);

	/**
	 * Returns the link for the given document.
	 *
	 * @param id the document identifier
	 * @return the document link
	 */
	@Query(value = "SELECT link FROM document WHERE id = ?1", nativeQuery = true)
	String getDocumentLinkByDocumentId(String id);

	/**
	 * Retrieves a single document projection by its identifier.
	 *
	 * @param documentIid the document identifier
	 * @return the matching document projection
	 */
	@Query(nativeQuery = true)
	DocumentMediaDTO getDocumentById(String documentIid);

	/**
	 * Updates the encryption type of the given document.
	 *
	 * @param id the document identifier
	 * @param encryptedType the new encryption type flag
	 */
	@Modifying
	@Transactional
	@Query(value = "UPDATE document SET encrypted_type = ?2 WHERE id = ?1", nativeQuery = true)
	void updateEncryption(String id, Integer encryptedType);
}
