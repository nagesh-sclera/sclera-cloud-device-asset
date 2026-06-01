package io.sclera.integrations.repository;

import io.sclera.integrations.model.Bacnet_Object;
import io.sclera.integrations.model.compositeclass.Bacnet_ObjectIds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Bacnet_ObjectRepository extends JpaRepository<Bacnet_Object, Bacnet_ObjectIds> {

    @Query("SELECT COUNT(bo) FROM Bacnet_Object bo WHERE bo.device_id = :deviceId")
    long countByDeviceId(String deviceId);

    @Query("SELECT bo.id FROM Bacnet_Object bo WHERE bo.device_id = :deviceId")
    List<String> findIdsByDeviceId(String deviceId);
}