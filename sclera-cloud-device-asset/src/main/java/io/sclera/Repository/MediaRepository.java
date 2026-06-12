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
import io.sclera.models.Media;


/**
 * Manages persistence and querying of {@link Media} entities and their device tag associations.
 */
@Repository
public interface MediaRepository extends JpaRepository<Media, String> {

    /**
     * Inserts a media record, or updates its name, category, description and link if the id already exists.
     *
     * @param id media identifier
     * @param name media name
     * @param category media category
     * @param description media description
     * @param link media link
     * @param username email of the creating user
     * @param createdTimestamp creation timestamp
     * @param extension media file extension
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native: plain INSERT ON CONFLICT already valid PostgreSQL
    @Query(value = "INSERT INTO media (id , name, category , description, link, created_email, created_timestamp, extension) VALUES (?1,?2,?3,?4,?5,?6,?7,?8) "
            + "ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, category = EXCLUDED.category, description = EXCLUDED.description, link = EXCLUDED.link", nativeQuery = true)
    void upsertMedia(String id, String name, String category, String description, String link, String username,
                     BigInteger createdTimestamp, String extension);


    /**
     * Returns the file extension of the media with the given id.
     *
     * @param mediaid media identifier
     * @return the media file extension
     */
    @Query("SELECT m.extension FROM Media m WHERE m.id = ?1")
    String getExtensionById(String mediaid);

    /**
     * Deletes the media record with the given id.
     *
     * @param mediaid media identifier
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM Media m WHERE m.id = ?1")
    void deleteMediaById(String mediaid);


    /**
     * Returns a paginated, optionally filtered set of media documents.
     *
     * @param searchkey search term used to filter media; pass the literal string {@code 'null'} to skip filtering
     * @param pageable pagination descriptor (replaces former pagesize/offset params)
     * @return matching media documents
     */
    @Query("SELECT new io.sclera.dto.DocumentMediaDTO(m.id, m.name, m.category, m.link, m.description, m.created_email, m.created_timestamp) "
            + "FROM Media m "
            + "WHERE (?1 = 'null' OR CONCAT(COALESCE(m.name,''), COALESCE(m.category,''), COALESCE(m.description,'')) LIKE CONCAT('%', ?1, '%'))")
    List<DocumentMediaDTO> getMedias(String searchkey, Pageable pageable);

    /**
     * Returns a paginated set of media documents tagged to the given device.
     *
     * @param deviceid device identifier
     * @param pageable pagination descriptor (replaces former pagesize/offset params)
     * @return media documents tagged to the device
     */
    @Query("SELECT new io.sclera.dto.DocumentMediaDTO(m.id, m.name, m.category, m.link, m.description, m.created_email, m.created_timestamp, d.id) "
            + "FROM Media m JOIN m.device d "
            + "WHERE d.id = ?1")
    List<DocumentMediaDTO> getMediasByDeviceIdByPagination(String deviceid, Pageable pageable);

    /**
     * Returns all media documents tagged to the given device.
     *
     * @param deviceid device identifier
     * @return media documents tagged to the device
     */
    @Query("SELECT new io.sclera.dto.DocumentMediaDTO(m.id, m.name, m.category, m.link, m.description, m.created_email, m.created_timestamp, d.id) "
            + "FROM Media m JOIN m.device d "
            + "WHERE d.id = ?1")
    Set<DocumentMediaDTO> getMediasByDeviceId(String deviceid);

    /**
     * Tags a media item to a device.
     *
     * @param id media identifier
     * @param device_id device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native: plain INSERT into device_media join table (no entity)
    @Query(value = "INSERT INTO device_media (media_id , device_id) VALUES (?1,?2)", nativeQuery = true)
    void tagMediaToDevice(String id, String device_id);

    /**
     * Removes the tag association between a media item and a device.
     *
     * @param id media identifier
     * @param device_id device identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native: DELETE on device_media join table (no entity)
    @Query(value = "DELETE FROM device_media WHERE media_id = ?1 AND device_id = ?2", nativeQuery = true)
    void untagMediaToDevice(String id, String device_id);

    /**
     * Deletes all device tag associations for the given media item.
     *
     * @param mediaid media identifier
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native: DELETE on device_media join table (no entity)
    @Query(value = "DELETE FROM device_media WHERE media_id = ?1", nativeQuery = true)
    void deleteTagRecordByMediaId(String mediaid);

    /**
     * Returns the number of media items tagged to the given device.
     *
     * @param device_id device identifier
     * @return count of tagged media items
     */
    // NOT CONVERTED — stays native: COUNT on device_media join table (no entity)
    @Query(value = "SELECT COUNT(*) FROM device_media WHERE device_id = ?1", nativeQuery = true)
    Integer getMediasCountByDeviceId(String device_id);

    /**
     * Returns the device identifiers a media item is tagged to.
     *
     * @param mediaid media identifier
     * @return device identifiers tagged to the media item
     */
    // NOT CONVERTED — stays native: SELECT from device_media join table (no entity)
    @Query(value = "SELECT device_id FROM device_media WHERE media_id = ?1", nativeQuery = true)
    List<String> getMediaByDeviceId(String mediaid);

    /**
     * Reassigns media tag associations from an existing device to a new device.
     *
     * @param device_id new device identifier
     * @param existing_device_id current device identifier whose associations are reassigned
     */
    @Modifying
    @Transactional
    // NOT CONVERTED — stays native: UPDATE sets a relation FK column on device_media join table (no entity)
    @Query(value = "UPDATE device_media SET device_id = ?1 WHERE (device_id IS NOT NULL) AND device_id =?2 ", nativeQuery = true)
    void updateMediaDeviceId(String device_id, String existing_device_id);
}
