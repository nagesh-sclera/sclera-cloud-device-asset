package io.sclera.edge.controller;

import io.sclera.edge.defaults.Defaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/measuringinstrumentattributes")
public class MeasuringInstrumentAttributesController {

    @GetMapping("/upsertMeasuringInstrumentAttribute")
    public void upsertMeasuringInstrumentAttribute(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) String value,
            @RequestParam(required = false) String protocol,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String primaryId,
            @RequestParam(required = false) String secondaryId,
            @RequestParam(required = false) String tertiaryId,
            @RequestParam(required = false) String measuringInstrumentId,
            @RequestParam(required = false) String attributeIndex) {
        // no-op
    }

    @GetMapping("/getMeasuringInstrumentAttributeById")
    public String getMeasuringInstrumentAttributeById(@RequestParam(required = false) String id) {
        return Defaults.NULL_STRING;
    }

    @GetMapping("/getAllMeasuringInstrumentAttributes")
    public List<String> getAllMeasuringInstrumentAttributes() {
        return Defaults.emptyList();
    }

    @GetMapping("/getMeasuringInstrumentAttributesByMeasuringInstrumentId")
    public List<String> getMeasuringInstrumentAttributesByMeasuringInstrumentId(
            @RequestParam(required = false) String measuringInstrumentId) {
        return Defaults.emptyList();
    }
}
