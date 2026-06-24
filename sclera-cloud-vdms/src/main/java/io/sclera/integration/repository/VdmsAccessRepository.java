package io.sclera.integration.repository;

import io.sclera.integration.dto.VdmsAccessDTO;
import io.sclera.integration.model.VdmsAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface VdmsAccessRepository extends JpaRepository<VdmsAccess,String> {
    @Query(nativeQuery = true)
    Set<VdmsAccessDTO> getVdmsAccessByUsername(String email, String orgId ,String key, int pageSize, int offset);

}
