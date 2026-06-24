package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.dto.DigitalTwinMeasuringInstrumentDTO;
import io.sclera.dto.DigitalTwinTemplateDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.DigitalTwinMeasuringInstrumentRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DigitalTwinMeasuringInstrumentService {

    @Autowired
    private DigitalTwinMeasuringInstrumentRepository digitalTwinMeasuringInstrumentRepository;

    public void addDigitalTwinMeasuringInstrumentByDigitalTwinTemplateId(String digitalTwinTemplateId, List<DigitalTwinMeasuringInstrumentDTO> digitalTwinMeasuringInstrumentDTOS) {
        log.info("Payload: DigitalTwinTemplateId: {}, Size of DigitalTwinMeasuringInstrument: {}, DigitalTwinMeasuringInstrument:{}", digitalTwinTemplateId,
                digitalTwinMeasuringInstrumentDTOS.size(), digitalTwinMeasuringInstrumentDTOS);
        if (!digitalTwinMeasuringInstrumentDTOS.isEmpty()) {
            for (DigitalTwinMeasuringInstrumentDTO digitalTwinMeasuringInstrumentDTO : digitalTwinMeasuringInstrumentDTOS) {
                if (digitalTwinMeasuringInstrumentDTO.getGroupedMeasuringInstrumentId() != null) {
                    String digitalTwinMeasuringInstrumentsId = Generators.timeBasedGenerator().generate().toString();
                    digitalTwinMeasuringInstrumentRepository.addDigitalTwinGroupedMeasuringInstrumentByDigitalTwinTemplateId(digitalTwinMeasuringInstrumentsId, digitalTwinMeasuringInstrumentDTO.getName(),
                            digitalTwinMeasuringInstrumentDTO.getDigital_twin_position(), digitalTwinTemplateId, digitalTwinMeasuringInstrumentDTO.getType(), digitalTwinMeasuringInstrumentDTO.getGroupedMeasuringInstrumentId());
                }

                if (digitalTwinMeasuringInstrumentDTO.getGroupedMeasuringInstrumentId() == null && digitalTwinMeasuringInstrumentDTO.getType() != null) {
                    String digitalTwinMeasuringInstrumentsId = Generators.timeBasedGenerator().generate().toString();
                    digitalTwinMeasuringInstrumentRepository.addDigitalTwinMeasuringInstrumentByDigitalTwinTemplateId(digitalTwinMeasuringInstrumentsId, digitalTwinMeasuringInstrumentDTO.getName(),
                            digitalTwinMeasuringInstrumentDTO.getDigital_twin_position(), digitalTwinTemplateId, digitalTwinMeasuringInstrumentDTO.getType());
                }
            }
            log.info("Digital Twin Measuring Instruments Added Successfully For DigitalTwinTemplateId: {}", digitalTwinTemplateId);
        }
    }

    public List<DigitalTwinMeasuringInstrumentDTO> getDigitalTwinMeasuringInstrumentsByDigitalTwinTemplateId(String digitalTwinTemplateId) {
        log.info("Payload: DigitalTwinTemplateId: {}", digitalTwinTemplateId);
        log.info("Fetching Digital Twin Measuring Instruments By DigitalTwinTemplateId: {}", digitalTwinTemplateId);
        return digitalTwinMeasuringInstrumentRepository.getDigitalTwinMeasuringInstrumentsByDigitalTwinTemplateId(digitalTwinTemplateId);
    }

    public List<String> getDigitalTwinMeasuringInstrumentTypesByDigitalTwinTemplateId(String digitalTwinTemplateId) {
        log.info("Payload: DigitalTwinTemplateId: {}", digitalTwinTemplateId);
        log.info("Fetching All the Digital Twin Measuring Instruments Types By Digital Twin Template Id: {}", digitalTwinTemplateId);
        return digitalTwinMeasuringInstrumentRepository.getDigitalTwinMeasuringInstrumentTypesByDigitalTwinTemplateId(digitalTwinTemplateId);
    }

    public void deleteDigitalTwinMeasuringInstrumentsDigitalTwinTemplateIds(List<String> digitalTwinTemplateIds) {
        log.info("Payload: Size of DigitalTwinTemplateIds: {}, DigitalTwinTemplateIds: {}", digitalTwinTemplateIds.size(), digitalTwinTemplateIds);
        digitalTwinMeasuringInstrumentRepository.deleteDigitalTwinMeasuringInstrumentsDigitalTwinTemplateIds(digitalTwinTemplateIds);
        log.info("Digital Twin Measuring Instruments By DigitalTwinTemplateIds Deleted Successfully");
    }

    public void updateDigitalTwinMeasuringInstrumentByDigitalTwinTemplateId(List<DigitalTwinMeasuringInstrumentDTO> digitalTwinMeasuringInstrumentDTOS, String digitalTwinTemplateId) {
        log.info("Payload: DigitalTwinTemplateId: {}, DigitalTwinMeasuringInstrument:{}", digitalTwinTemplateId,
                digitalTwinMeasuringInstrumentDTOS);
        digitalTwinMeasuringInstrumentRepository.deleteDigitalTwinMeasuringInstrumentsDigitalTwinTemplateId(digitalTwinTemplateId);
        if (digitalTwinMeasuringInstrumentDTOS != null && !digitalTwinMeasuringInstrumentDTOS.isEmpty()) {
            for (DigitalTwinMeasuringInstrumentDTO digitalTwinMeasuringInstrumentDTO : digitalTwinMeasuringInstrumentDTOS) {
                if (digitalTwinMeasuringInstrumentDTO.getGroupedMeasuringInstrumentId() != null) {
                    String digitalTwinMeasuringInstrumentsId = Generators.timeBasedGenerator().generate().toString();
                    digitalTwinMeasuringInstrumentRepository.addDigitalTwinGroupedMeasuringInstrumentByDigitalTwinTemplateId(digitalTwinMeasuringInstrumentsId, digitalTwinMeasuringInstrumentDTO.getName(),
                            digitalTwinMeasuringInstrumentDTO.getDigital_twin_position(), digitalTwinTemplateId, digitalTwinMeasuringInstrumentDTO.getType(), digitalTwinMeasuringInstrumentDTO.getGroupedMeasuringInstrumentId());
                }

                if (digitalTwinMeasuringInstrumentDTO.getGroupedMeasuringInstrumentId() == null && digitalTwinMeasuringInstrumentDTO.getType() != null) {
                    String digitalTwinMeasuringInstrumentsId = Generators.timeBasedGenerator().generate().toString();
                    digitalTwinMeasuringInstrumentRepository.addDigitalTwinMeasuringInstrumentByDigitalTwinTemplateId(digitalTwinMeasuringInstrumentsId, digitalTwinMeasuringInstrumentDTO.getName(),
                            digitalTwinMeasuringInstrumentDTO.getDigital_twin_position(), digitalTwinTemplateId, digitalTwinMeasuringInstrumentDTO.getType());
                }
            }
            log.info("Digital Twin Measuring Instruments Updated Successfully For DigitalTwinTemplateId: {}", digitalTwinTemplateId);
        }
    }

    public List<String> getDigitalTwinGroupedMeasuringInstrumentIdsByDigitalTwinTemplateId(String digitalTwinTemplateId) {
        log.info("Payload: DigitalTwinTemplateId: {}", digitalTwinTemplateId);
        log.info("Fetching All the Digital Twin Grouped Measuring Instrument Ids By Digital Twin Template Id: {}", digitalTwinTemplateId);
        return digitalTwinMeasuringInstrumentRepository.getDigitalTwinGroupedMeasuringInstrumentIdsByDigitalTwinTemplateId(digitalTwinTemplateId);
    }
}
