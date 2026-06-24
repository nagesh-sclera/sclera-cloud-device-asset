package io.sclera.service;

import io.sclera.repository.VdmsCoordinatesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VdmsCoordinatesService {

    @Autowired
    private VdmsCoordinatesRepository vdmsCoordinatesRepository;


    public void addVdmsCoordinates(String coordinatesId, String coordinates)
    {
        vdmsCoordinatesRepository.addVdmsCoordinates(coordinatesId,coordinates);

    }

    public String getCoordinatesById(String coordinatesId)
    {
        return vdmsCoordinatesRepository.getCoordinatesById(coordinatesId);
    }

    public Long checkIsPresent(float lat, float lng, String coordinates)
    {
        return vdmsCoordinatesRepository.checkIsPresent(lat,lng,coordinates);
    }

    public void updateVdmsCoordinates(String coordinateId, String coordinates)
    {
        vdmsCoordinatesRepository.updateVdmsCoordinates(coordinateId,coordinates);

    }

    public String  getVdmsCoordinatesByVdmsId(String vdmsId)
    {
        return vdmsCoordinatesRepository.getVdmsCoordinatesByVdmsId(vdmsId);
    }

    public void   deleteVdmsCoordinatesById(String id)
    {
        vdmsCoordinatesRepository.deleteVdmsCoordinatesById(id);
    }
}
