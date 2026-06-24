package io.sclera.repository;

import io.sclera.dto.OrganisationOnboardingSummaryDTO;
import io.sclera.model.OrganisationOnboardingSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganisationOnboardingSummaryRepository extends JpaRepository<OrganisationOnboardingSummary ,String> {


    @Query(nativeQuery = true)
    OrganisationOnboardingSummaryDTO getOrganisationOnboardingSummaryByOrganisationId(String organisationId);
}
