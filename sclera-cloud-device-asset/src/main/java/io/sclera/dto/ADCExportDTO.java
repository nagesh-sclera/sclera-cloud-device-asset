package io.sclera.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregates a property's buildings, floors, and locations for export to the
 * Alarm.com (ADC) integration, keyed by property, VDMS, and Sclera organization identifiers.
 */
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

