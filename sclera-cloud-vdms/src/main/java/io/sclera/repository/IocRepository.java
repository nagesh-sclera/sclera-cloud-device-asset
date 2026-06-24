package io.sclera.repository;

import io.sclera.dto.IocDto;
import io.sclera.model.Ioc;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IocRepository extends JpaRepository<Ioc, String> {

    @Query(nativeQuery = true)
    List<IocDto> getAllIocDetails();

    @Query(nativeQuery = true)
    IocDto getIocDetailsByIocId(String iocId);

    @Query(nativeQuery = true)
    List<IocDto> getAllIocDetailsByOrgId(String orgId);

    @Query(nativeQuery = true)
    IocDto getIocDetailsByIocIdAndOrgId(String iocId,String orgId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO `ioc` (`id`, `name`, `server_url`, `web_url`, `customer_org_id`)  VALUES (?1,?2,?3,?4,?5) ", nativeQuery = true)
    void addIocData(String id, String name, String serverUrl, String web_url, String orgId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE ioc SET name = ?1 , server_url = ?2, web_url = ?3 WHERE id = ?4 AND customer_org_id = ?5", nativeQuery = true)
    void editIocData(String name, String serverUrl, String web_url, String id, String orgId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ioc WHERE id = ?1 AND customer_org_id = ?2 ", nativeQuery = true)
    void deleteIocData(String iocId, String orgId);


    @Modifying
    @Transactional
    @Query(value = "DELETE FROM ioc WHERE  customer_org_id = ?1 ", nativeQuery = true)
    void deleteIocDataByOrgId(String orgId);
}


