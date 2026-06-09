package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.LocationHistoryRepository;
import io.sclera.dto.LocationHistoryDTO;
import io.sclera.interfaces.LocationHistoryServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Set;

/**
 * Records and retrieves location tagging history for locations identified by QR code or NFC.
 *
 * <p>Delegates persistence and lookups to {@link LocationHistoryRepository}.
 */
@Service
public class LocationHistoryService implements LocationHistoryServiceInterface {

    @Autowired
    LocationHistoryRepository locationHistoryRepository;


    /**
     * Persists a location history entry, stamping it with the updating user and current timestamp,
     * assigning a time-based id when absent, and deriving a description from its type and status.
     *
     * @param username the email of the user performing the update, recorded on the entry
     * @param vdmsid the VDMS identifier of the requesting context
     * @param locationHistory the location history entry to enrich and persist
     */
    public void addLocationHistory(String username, String vdmsid, LocationHistoryDTO locationHistory) {
        locationHistory.setUpdated_email(username);
        locationHistory.setUpdated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));

        if (locationHistory.getId() == null) {
            locationHistory.setId(Generators.timeBasedGenerator().generate().toString());
        }

        if (locationHistory.getType().equals("qr_code")) {
            if (locationHistory.getStatus().equals("tag")) {
                locationHistory.setDescription("Location successfully tagged to QR code.");
            } else if (locationHistory.getStatus().equals("retag")) {
                locationHistory.setDescription("Location successfully retagged to QR code.");
            }
        } else if (locationHistory.getType().equals("nfc")) {
            if (locationHistory.getStatus().equals("tag")) {
                locationHistory.setDescription("Location successfully tagged to NFC.");
            } else if (locationHistory.getStatus().equals("retag")) {
                locationHistory.setDescription("Location successfully retagged to NFC.");
            }
        }
        locationHistoryRepository.addLocationHistory(locationHistory.getId(), locationHistory.getStatus(), locationHistory.getType(), locationHistory.getDescription(), locationHistory.getUpdated_timestamp(), locationHistory.getUpdated_email(), locationHistory.getLocation_id());
    }

    /**
     * Returns all location history entries recorded for the given location.
     *
     * @param username the email of the requesting user
     * @param vdmsid the VDMS identifier of the requesting context
     * @param location_id the identifier of the location whose history is retrieved
     * @return the set of location history entries for the location
     */
    public Set<LocationHistoryDTO> getLocationHistory(String username, String vdmsid, String location_id) {
        return locationHistoryRepository.getLocationHistory(location_id);
    }
}
