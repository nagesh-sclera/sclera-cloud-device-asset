package io.sclera.Repository;

import io.sclera.dto.TechnicianDTO;

import java.util.List;

/** Custom (DAO) read methods for {@link TechnicianRepository} served by {@link TechnicianRepositoryImpl}. */
public interface TechnicianRepositoryCustom {

    /**
     * Returns all technician projections. Off branch runs the verbatim {@code Technician.getAllTechnician}
     * native query; on branch maps {@code Technician} entities via {@code TechnicianDtoMapper}. Selected
     * by the {@code mapstruct.read.technician.get-all} flag (default false).
     */
    List<TechnicianDTO> getAllTechnician();
}
