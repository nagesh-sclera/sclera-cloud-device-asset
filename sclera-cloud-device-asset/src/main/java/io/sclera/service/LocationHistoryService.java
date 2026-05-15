package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.LocationHistoryRepository;
import io.sclera.dto.LocationHistoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Set;

@Service
public class LocationHistoryService {

    @Autowired
    LocationHistoryRepository locationHistoryRepository;


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

    public Set<LocationHistoryDTO> getLocationHistory(String username, String vdmsid, String location_id) {
        return locationHistoryRepository.getLocationHistory(location_id);
    }
}
