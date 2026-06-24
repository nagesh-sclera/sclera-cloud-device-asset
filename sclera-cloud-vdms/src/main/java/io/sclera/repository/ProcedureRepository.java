package io.sclera.repository;

import io.sclera.dto.CategoryDTO;
import io.sclera.model.Procedures;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface ProcedureRepository extends JpaRepository<Procedures,String> {

    @Query(nativeQuery = true)
    List<CategoryDTO> getAllProcedure(String searchKey, String sort, int pageSize, int offset);

    @Query(value = "SELECT COUNT(*) FROM procedures WHERE name = ?1", nativeQuery = true)
    Integer checkProcedureByName(String name);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO procedures (id, name, icon_url,display_name, creation_timestamp) VALUES (?1, ?2, ?3, ?4, ?5)", nativeQuery = true)
    void addProcedure(String id, String name, String iconUrl, String displayName, BigInteger creationTimestamp);

    @Query(value = "SELECT COUNT(*) FROM procedures WHERE id != ?1 AND name = ?2", nativeQuery = true)
    Integer checkProcedureByIdAndName(String ProcedureId, String name);

    @Query(value = "SELECT name FROM procedures WHERE id = ?1", nativeQuery = true)
    String getNameById(String ProcedureId);

    @Query(value = "SELECT icon_url FROM procedures WHERE id = ?1", nativeQuery = true)
    String getImageUrlById(String ProcedureId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE procedures SET name = ?1, icon_url = ?2, display_name = ?3 WHERE id = ?4", nativeQuery = true)
    void updateProcedureById(String name, String iconUrl, String displayName, String ProcedureId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE procedures SET icon_url = ?1 WHERE id = ?2", nativeQuery = true)
    void updateProcedureIconById(String url, String ProcedureId);

    @Query(value = "SELECT icon_url FROM procedures WHERE icon_url != NULL AND id IN ?1", nativeQuery = true)
    List<String> getImageUrlsByIds(List<String> ProcedureIds);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM procedures WHERE id IN ?1", nativeQuery = true)
    void deleteProcedureByIds(List<String> ProcedureIds);
}
