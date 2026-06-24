package io.sclera.service;

import io.sclera.dto.OrganisationOnboardingSummaryDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsOnboardingSummaryDTO;
import io.sclera.repository.OrganisationOnboardingSummaryRepository;
import io.sclera.util.ScleraUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganisationOnboardingSummaryService {


    @Autowired
    private OrganisationOnboardingSummaryRepository organisationOnboardingSummaryRepository;


    @Autowired
    private VdmsOnboardingSummaryService vdmsOnboardingSummaryService;

    public ResponseEntity<?> getOrganisationOnboardingSummaryByOrganisationId(String organisationId) {
        OrganisationOnboardingSummaryDTO organisationOnboardingSummaryDTO = organisationOnboardingSummaryRepository.getOrganisationOnboardingSummaryByOrganisationId(organisationId);
        List<VdmsOnboardingSummaryDTO> vdmsOnboardingSummaryDTOS = vdmsOnboardingSummaryService.getVdmsOnboardingSummaryByOrganisationId(organisationId);

        if (organisationOnboardingSummaryDTO != null) {
            organisationOnboardingSummaryDTO.setStatus("active");
            organisationOnboardingSummaryDTO.setVdmsOnboardingSummaryDTOS(vdmsOnboardingSummaryDTOS);
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload(organisationOnboardingSummaryDTO, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
