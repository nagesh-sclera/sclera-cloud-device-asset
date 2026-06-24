package io.sclera.service;

import io.sclera.repository.BillingSelectedVdmsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingSelectedVdmsService {

    private final BillingSelectedVdmsRepository billingSelectedVdmsRepository;

    public void addBillingSelectedVdms(String billingSelectedVdmsId, String billingId, String vdmsId)
    {
        billingSelectedVdmsRepository.addBillingSelectedVdms(billingSelectedVdmsId,billingId,vdmsId);
    }

    public String getSelectedVdms(String billingId)
    {
        List<String> vdmsIds = billingSelectedVdmsRepository.findVdmsIdsByBillingId(billingId);
        return String.join(",", vdmsIds);
    }

    public void updatedSelectedVdms(String billingId, String selectedVdms)
    {
        // Split the comma-separated string of selected VDMS IDs from the DTO
        Set<String> newSelectedVdms = new HashSet<>(Arrays.asList(selectedVdms.split(",")));

        // Get the current VDMS IDs from the table (in a Set for easy comparison)
        Set<String> currentSelectedVdms = new HashSet<>(billingSelectedVdmsRepository.findVdmsIdsByBillingId(billingId));

        // Identify which VDMS IDs need to be removed (those in current but not in new list)
        Set<String> vdmsToRemove = new HashSet<>(currentSelectedVdms);
        vdmsToRemove.removeAll(newSelectedVdms);

        // Identify which VDMS IDs need to be added (those in new list but not in current)
        Set<String> vdmsToAdd = new HashSet<>(newSelectedVdms);
        vdmsToAdd.removeAll(currentSelectedVdms);

        // Remove the unselected VDMS IDs from the table
        if (!vdmsToRemove.isEmpty())
        {
            billingSelectedVdmsRepository.deleteByBillingIdAndVdmsIds(billingId, new ArrayList<>(vdmsToRemove));
        }

        // Add the newly selected VDMS IDs to the table
        if (!vdmsToAdd.isEmpty())
        {
            for (String vdmsId : vdmsToAdd) {
                String billingSelectedVdmsId = UUID.randomUUID().toString();
                billingSelectedVdmsRepository.addBillingSelectedVdms(billingSelectedVdmsId,billingId,vdmsId);
            }
        }
    }

    public void deleteSelectedVdms(List<String> billingIds)
    {
        billingSelectedVdmsRepository.deleteSelectedVdms(billingIds);
    }
}
