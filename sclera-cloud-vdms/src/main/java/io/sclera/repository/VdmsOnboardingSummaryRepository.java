package io.sclera.repository;

import io.sclera.dto.VdmsOnboardingSummaryDTO;
import io.sclera.model.VdmsOnboardingSummary;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VdmsOnboardingSummaryRepository extends JpaRepository<VdmsOnboardingSummary, String> {


    @Query(nativeQuery = true)
    VdmsOnboardingSummaryDTO getVdmsOnboardingSummaryByVdmsId(String vdmsId);

    @Query(nativeQuery = true)
    List<VdmsOnboardingSummaryDTO> getVdmsOnboardingSummaryByOrganisationId(String organisationId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM vdms_onboarding_summary WHERE vdms_id = ?1",nativeQuery = true)
    void deleteVdmsOnboardingSummaryByVdmsId(String vdmsId);
}
