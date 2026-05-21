package io.sclera.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ADCExportDTO {
    private String id;
    private String propertyId;
    private String vdmsId;
    private String scleraOrgId;

    private List<BuildingDTO> buildings;
    private List<FloorDTO> floors;
    private List<LocationDTO> locations;
}

