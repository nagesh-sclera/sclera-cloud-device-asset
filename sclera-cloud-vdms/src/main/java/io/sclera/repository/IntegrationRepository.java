package io.sclera.repository;

import io.sclera.dto.IntegrationDTO;
import io.sclera.model.Integration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import java.util.Set;

@Repository
public interface IntegrationRepository extends JpaRepository<Integration ,String> {

    @Query(nativeQuery = true)
    Set<IntegrationDTO> getDistinctIntegrations();

    @Query(nativeQuery = true)
    Set<IntegrationDTO> getIntegrationNames();

    @Query(nativeQuery = true)
    IntegrationDTO getIntegrationDataByIntegrationId(String id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO integration(id,name,integration_name,subscriptions,protocols,tag_list," +
            "authentications,template,image_url,description,category) VALUE(?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11) ON DUPLICATE KEY" +
            " UPDATE name = IFNULL(?2 ,name) ,integration_name = IFNULL(?3 ,integration_name) ," +
            "subscriptions = IFNULL(?4 ,subscriptions) ,protocols = IFNULL(?5 ,protocols) ," +
            "tag_list = IFNULL(?6 ,tag_list) ,authentications = IFNULL(?7 ,authentications) ," +
            "template = IFNULL(?8 ,template) ,image_url = IFNULL(?9 ,image_url) ," +
            "description = IFNULL(?10 ,description) ,category = IFNULL(?11 ,category)" ,nativeQuery = true)
    void upsertIntegration(String integration_id, String name, String integration_name, String subscriptions,
                           String protocols, String tag_list, String authentications, String template,
                           String image_url, String description ,String category);


    @Query(nativeQuery = true)
    Set<IntegrationDTO> getIntegrationsByCategory(Set<String> categories);


}
