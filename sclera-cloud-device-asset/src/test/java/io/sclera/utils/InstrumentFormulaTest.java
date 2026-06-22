package io.sclera.utils;

import io.sclera.dto.MeasuringInstrumentAttributesDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit coverage for InstrumentFormula: parameter counting, the type-dispatch entry point,
 * simple value pass-through formulas, and the geometric (area/volume) calculations.
 */
class InstrumentFormulaTest {

    private final InstrumentFormula formula = new InstrumentFormula();

    private static MeasuringInstrumentAttributesDTO attr(String name, String value) {
        MeasuringInstrumentAttributesDTO dto = new MeasuringInstrumentAttributesDTO();
        dto.setName(name);
        dto.setValue(value);
        return dto;
    }

    /** Parses the value out of a possibly-locale-formatted ("," or ".") decimal string. */
    private static double asNumber(String s) {
        return Double.parseDouble(s.replace(",", "."));
    }

    @Test
    void getNoOfParametersFromString_returnsHighestIndex() {
        assertEquals(3, formula.getNoOfParametersFromString("parameter_1 + parameter_3 - parameter_2"));
    }

    @Test
    void getNoOfParametersFromString_returnsZeroWhenNoParameters() {
        assertEquals(0, formula.getNoOfParametersFromString("no tokens here"));
    }

    @Test
    void getValuebyMeasuringParameter_genericReturnsFirstValue() {
        String result = formula.getValuebyMeasuringParameter(
                null, "Generic", null, null, List.of(attr("p1", "42")));
        assertEquals("42", result);
    }

    @Test
    void getValuebyMeasuringParameter_dispatchesToCylindricalVolume() {
        List<MeasuringInstrumentAttributesDTO> attrs = List.of(
                attr("Diameter", "2"), attr("Length", "10"));
        String result = formula.getValuebyMeasuringParameter(
                null, "Cylindrical Volume", null, null, attrs);
        // 3.1415 * length * diameter^2 / 4 = 3.1415 * 10 * 4 / 4 = 31.415
        assertEquals(31.415, asNumber(result), 0.01);
    }

    @Test
    void getValuebyMeasuringParameter_nullAttributesReturnsNull() {
        assertNull(formula.getValuebyMeasuringParameter(null, "Generic", null, null, null));
    }

    @Test
    void getValuebyMeasuringParameter_emptyAttributesReturnsNull() {
        assertNull(formula.getValuebyMeasuringParameter(null, "Generic", null, null, List.of()));
    }

    @Test
    void simplePassThroughFormulas_returnFirstAttributeValue() {
        List<MeasuringInstrumentAttributesDTO> attrs = List.of(attr("x", "7.5"));
        assertEquals("7.5", formula.getGenericValueByParameter(attrs));
        assertEquals("7.5", formula.getTemperatureByParameter(attrs));
        assertEquals("7.5", formula.getHumidityByParameter(attrs));
        assertEquals("7.5", formula.getPressureByParameter(attrs));
        assertEquals("7.5", formula.getLenghtValueByParameter(attrs));
        assertEquals("7.5", formula.getDiameterValueByParameter(attrs));
        assertEquals("7.5", formula.getBreadthValueByParameter(attrs));
        assertEquals("7.5", formula.getWidthValueByParameter(attrs));
        assertEquals("7.5", formula.getDefaultByParameter(attrs));
    }

    @Test
    void getCylindricalVolumeByParameter_computesVolume() {
        List<MeasuringInstrumentAttributesDTO> attrs = List.of(
                attr("Diameter", "2"), attr("Length", "10"));
        assertEquals(31.415, asNumber(formula.getCylindricalVolumeByParameter(attrs)), 0.01);
    }

    @Test
    void getRectangularAreaByParameter_computesLengthTimesBreadth() {
        List<MeasuringInstrumentAttributesDTO> attrs = List.of(
                attr("Length", "4"), attr("Breadth", "5"));
        assertEquals(20.0, asNumber(formula.getRectangularAreaByParameter(attrs)), 0.001);
    }

    @Test
    void getRectangularAreaByParameter_skipsBlankValues() {
        List<MeasuringInstrumentAttributesDTO> attrs = List.of(
                attr("Length", "4"), attr("Breadth", ""));
        // Breadth stays 0.0 -> area = 0
        assertEquals(0.0, asNumber(formula.getRectangularAreaByParameter(attrs)), 0.001);
    }

    @Test
    void getNoOfParametersFromString_isNeverNegative() {
        assertTrue(formula.getNoOfParametersFromString("parameter_0") >= 0);
    }
}
