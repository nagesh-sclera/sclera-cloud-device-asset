package io.sclera.repository;

import io.sclera.dto.DigitalTwinMeasuringInstrumentDTO;
import io.sclera.model.DigitalTwinMeasuringInstrument;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DigitalTwinMeasuringInstrumentRepository extends JpaRepository<DigitalTwinMeasuringInstrument, String> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO digital_twin_measuring_instrument(id, name, digital_twin_position, digital_twin_template_id, measuring_instruments_type) " +
            "VALUES(?1,?2,?3,?4,?5)", nativeQuery = true)
    void addDigitalTwinMeasuringInstrumentByDigitalTwinTemplateId(String digitalTwinMeasuringInstrumentsId, String name, String digitalTwinPosition,
                                                                  String digitalTwinTemplateId, String measuringInstrumentsType);

    @Query(nativeQuery = true)
    List<DigitalTwinMeasuringInstrumentDTO> getDigitalTwinMeasuringInstrumentsByDigitalTwinTemplateId(String digitalTwinTemplateId);

    @Query(value = "SELECT DISTINCT measuring_instruments_type FROM digital_twin_measuring_instrument WHERE digital_twin_template_id = ?1 AND grouped_measuring_instrument_id IS NULL", nativeQuery = true)
    List<String> getDigitalTwinMeasuringInstrumentTypesByDigitalTwinTemplateId(String digitalTwinTemplateId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM digital_twin_measuring_instrument WHERE digital_twin_template_id IN ?1", nativeQuery = true)
    void deleteDigitalTwinMeasuringInstrumentsDigitalTwinTemplateIds(List<String> digitalTwinTemplateIds);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM digital_twin_measuring_instrument WHERE digital_twin_template_id = ?1", nativeQuery = true)
    void deleteDigitalTwinMeasuringInstrumentsDigitalTwinTemplateId(String digitalTwinTemplateId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO digital_twin_measuring_instrument(id, name, digital_twin_position, digital_twin_template_id, measuring_instruments_type, grouped_measuring_instrument_id) " +
            "VALUES(?1,?2,?3,?4,?5,?6)", nativeQuery = true)
    void addDigitalTwinGroupedMeasuringInstrumentByDigitalTwinTemplateId(String digitalTwinMeasuringInstrumentsId, String name,
                                                                         String digitalTwinPosition, String digitalTwinTemplateId,
                                                                         String type, String groupedMeasuringInstrumentId);

    @Query(value = "SELECT grouped_measuring_instrument_id FROM digital_twin_measuring_instrument WHERE digital_twin_template_id = ?1 AND grouped_measuring_instrument_id IS NOT NULL", nativeQuery = true)
    List<String> getDigitalTwinGroupedMeasuringInstrumentIdsByDigitalTwinTemplateId(String digitalTwinTemplateId);
}
