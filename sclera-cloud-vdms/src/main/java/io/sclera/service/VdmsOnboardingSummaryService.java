package io.sclera.service;

import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsOnboardingSummaryDTO;
import io.sclera.repository.VdmsOnboardingSummaryRepository;
import io.sclera.util.ScleraUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VdmsOnboardingSummaryService {

    @Autowired
    private VdmsOnboardingSummaryRepository vdmsOnboardingSummaryRepository;


    public ResponseEntity<?> getVdmsOnboardingSummaryByVdmsId(String vdmsId) {
        VdmsOnboardingSummaryDTO vdmsOnboardingSummaryDTO = vdmsOnboardingSummaryRepository.getVdmsOnboardingSummaryByVdmsId(vdmsId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsOnboardingSummaryDTO ,200 ,true);
        return new ResponseEntity<>(responseDTO , HttpStatus.OK);
    }

    public List<VdmsOnboardingSummaryDTO> getVdmsOnboardingSummaryByOrganisationId(String organisationId) {
        return vdmsOnboardingSummaryRepository.getVdmsOnboardingSummaryByOrganisationId(organisationId);
    }

    public void deleteVdmsOnboardingSummaryByVdmsId(String vdmsId) {
        vdmsOnboardingSummaryRepository.deleteVdmsOnboardingSummaryByVdmsId(vdmsId);
    }
}
