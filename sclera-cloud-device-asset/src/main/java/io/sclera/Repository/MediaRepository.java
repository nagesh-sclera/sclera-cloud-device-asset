package io.sclera.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.sclera.dto.DocumentMediaDTO;
import io.sclera.models.Media;


@Repository
public interface MediaRepository extends JpaRepository<Media, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO media (id , name, category , description, link, created_email, created_timestamp, extension) VALUES (?1,?2,?3,?4,?5,?6,?7,?8) "
            + "ON DUPLICATE KEY UPDATE name = ?2 , category = ?3, description = ?4, link = ?5 ", nativeQuery = true)
    void upsertMedia(String id, String name, String category, String description, String link, String username,
                     BigInteger createdTimestamp, String extension);


    @Query(value = "SELECT extension from media WHERE id = ?1", nativeQuery = true)
    String getExtensionById(String mediaid);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM media WHERE id = ?1", nativeQuery = true)
    void deleteMediaById(String mediaid);


    @Query(nativeQuery = true)
    Set<DocumentMediaDTO> getMedias(Integer pagesize, Integer offset, String searchkey);

    @Query(nativeQuery = true)
    Set<DocumentMediaDTO> getMediasByDeviceIdByPagination(String deviceid, Integer pagesize, Integer offset);

    @Query(nativeQuery = true)
    Set<DocumentMediaDTO> getMediasByDeviceId(String deviceid);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO device_media (media_id , device_id) VALUE (?1,?2)", nativeQuery = true)
    void tagMediaToDevice(String id, String device_id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM device_media WHERE media_id = ?1 AND device_id = ?2", nativeQuery = true)
    void untagMediaToDevice(String id, String device_id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM device_media WHERE media_id = ?1", nativeQuery = true)
    void deleteTagRecordByMediaId(String mediaid);

    @Query(value = "SELECT COUNT(*) FROM device_media WHERE device_id = ?1", nativeQuery = true)
    Integer getMediasCountByDeviceId(String device_id);

    @Query(value = "SELECT device_id FROM device_media WHERE media_id = ?1", nativeQuery = true)
    List<String> getMediaByDeviceId(String mediaid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE device_media SET device_id = ?1 WHERE (device_id IS NOT NULL) AND device_id =?2 ", nativeQuery = true)
    void updateMediaDeviceId(String device_id, String existing_device_id);
}
