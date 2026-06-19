package io.sclera.Repository;

import io.sclera.dto.TechnicianDTO;
import io.sclera.mapper.TechnicianDtoMapper;
import io.sclera.models.Technician;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class TechnicianRepositoryImpl implements TechnicianRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    private final TechnicianDtoMapper technicianDtoMapper;

    @Value("${mapstruct.read.technician.get-all:false}")
    private boolean useMapStruct;

    public TechnicianRepositoryImpl(TechnicianDtoMapper technicianDtoMapper) {
        this.technicianDtoMapper = technicianDtoMapper;
    }

    @Override
    public List<TechnicianDTO> getAllTechnician() {
        if (useMapStruct) {
            // On path: load entities, map via the existing MapStruct mapper (same 11 fields as technicianMapping).
            List<Technician> technicians =
                    em.createQuery("SELECT t FROM Technician t", Technician.class).getResultList();
            return technicians.stream().map(technicianDtoMapper::toDto).toList();
        }
        // Off path: verbatim existing named native query + technicianMapping — byte-identical to before.
        // The named query carries its own @SqlResultSetMapping, so getResultList() is raw — cast is unavoidable here.
        @SuppressWarnings("unchecked")
        List<TechnicianDTO> offPath = em.createNamedQuery("Technician.getAllTechnician").getResultList();
        return offPath;
    }
}
