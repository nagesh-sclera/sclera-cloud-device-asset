package io.sclera.utils;

import io.sclera.dto.MeasuringInstrumentAttributesDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Drives {@link InstrumentFormula#getValuebyMeasuringParameter} across every supported
 * measuring-instrument type. This exercises both the type-dispatch switch and the
 * per-type formula methods: the simple pass-through types, the numeric/geometry
 * calculations, and the coded-status mappings.
 */
class InstrumentFormulaDispatchTest {

    private final InstrumentFormula formula = new InstrumentFormula();

    /** Builds an attribute list from alternating name/value pairs. */
    private static List<MeasuringInstrumentAttributesDTO> attrs(String... nameValuePairs) {
        List<MeasuringInstrumentAttributesDTO> list = new ArrayList<>();
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            MeasuringInstrumentAttributesDTO dto = new MeasuringInstrumentAttributesDTO();
            dto.setName(nameValuePairs[i]);
            dto.setValue(nameValuePairs[i + 1]);
            list.add(dto);
        }
        return list;
    }

    private String dispatch(String type, List<MeasuringInstrumentAttributesDTO> a) {
        return formula.getValuebyMeasuringParameter(null, type, null, null, a);
    }

    private static double asNumber(String s) {
        return Double.parseDouble(s.replace(",", "."));
    }

    /**
     * Every type whose formula simply returns the first attribute's value (or formats a
     * single value, which for "55" round-trips to "55"). Also covers the default branch.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "Generic", "Temperature", "Humidity", "Pressure", "Length", "Diameter",
            "Breadth", "Width", "Flow", "Voltage", "Current", "CO2", "Light Intensity",
            "Noise Level", "VOC", "Air Quality", "Electromagnetic Field", "Resistance",
            "Capacitance", "Speed", "Acceleration", "Level", "Alcohol Content",
            "Dissolved Oxygen", "Thickness", "Electrostatic Field", "Water Salinity",
            "Ferrite Content", "Density", "PH", "Inductance", "Water Hardness", "Battery",
            "Energy Consumption", "PM2.5 Level", "Belt Tension", "Gap Measurement",
            "Oil Level", "POE", "Surface Roughness", "TDS", "Thermal Conductivity",
            "Tint Measurement", "Torque", "Water Consumption", "Weight",
            "Digital Tachometer", "Analog Tachometer", "Chloride Level", "Sulfate Level",
            "UV Index", "Electromagnetic Interference", "Wood Moisture Content",
            "Belt Frequency", "Power Factor Analog", "Cable Length", "Viscosity",
            "Power Factor Digital", "Differential Pressure", "Radiation Dose",
            "Insulation Resistance", "Temperature 3", "Solar Irradiance", "ORP",
            "Impedance", "Notepad", "SF6 Purity", "Force", "COD",
            "Magnetic Permeability", "Chlorine Dioxide Level", "Surface Tension",
            "Vacuum Pressure", "Temperature 6", "Schedule", "Unrecognised-Type-Default"
    })
    void passThroughTypes_returnFirstValue(String type) {
        assertEquals("55", dispatch(type, attrs("parameter_1", "55")));
    }

    // ---- numeric / geometry calculations (integer-valued to avoid locale/rounding noise) ----

    @Test
    void power_isVoltageTimesCurrent() {
        assertEquals("20", dispatch("Power", attrs("Voltage", "10", "Current", "2")));
    }

    @Test
    void rectangularVolume_isLengthTimesBreadthTimesWidth() {
        assertEquals("24", dispatch("Rectangular Volume",
                attrs("Length", "2", "Breadth", "3", "Width", "4")));
    }

    @Test
    void rectangularArea_isLengthTimesBreadth() {
        assertEquals("20", dispatch("Rectangular Area", attrs("Length", "4", "Breadth", "5")));
    }

    @Test
    void printerInk_isCurrentOverMaxPercent() {
        assertEquals("25", dispatch("Printer Ink Level",
                attrs("Maximum Capacity", "200", "Current Capacity", "50")));
    }

    @Test
    void flow1_isKFactorTimesPulsePlusOffset() {
        assertEquals("7", dispatch("Flow 1",
                attrs("K-Factor", "2", "Pulse Count", "3", "Offset", "1")));
    }

    @Test
    void occupancyCounter_isInMinusOut() {
        assertEquals("7", dispatch("Occupancy Counter",
                attrs("Counter In", "10", "Counter Out", "3")));
    }

    @Test
    void occupancyCounter1_sumsBothInAndOutPairs() {
        assertEquals("5", dispatch("Occupancy Counter 1",
                attrs("Counter In-1", "5", "Counter Out-1", "2",
                        "Counter In-2", "3", "Counter Out-2", "1")));
    }

    @Test
    void multipleOccupancyCounter_sumsIndexedPairs() {
        assertEquals("3", dispatch("Multiple Occupancy Counter",
                attrs("Counter In-1", "5", "Counter Out-1", "2")));
    }

    @Test
    void multipleLightIntensity_averagesNonZeroValues() {
        assertEquals("15", dispatch("Multiple Light Intensity",
                attrs("p1", "10", "p2", "20")));
    }

    @Test
    void temperature4_convertsCelsiusToFahrenheit() {
        assertEquals("212", dispatch("Temperature 4", attrs("Temperature", "100")));
    }

    @Test
    void temperature5_convertsFahrenheitToCelsius() {
        assertEquals("100", dispatch("Temperature 5", attrs("Temperature", "212")));
    }

    @Test
    void circularArea_fromDiameter() {
        // 3.1415 * d^2 / 4 with d=2 -> 3.1415
        assertEquals(3.1415, asNumber(dispatch("Circular Area", attrs("Diameter", "2"))), 0.01);
    }

    @Test
    void ladder_lengthOverSqrt17() {
        // 17 / sqrt(17) = sqrt(17) ~ 4.123
        assertEquals(4.123, asNumber(dispatch("Ladder", attrs("Length", "17"))), 0.01);
    }

    // ---- coded status mappings ----

    @Test
    void interfaceStatus_mapsCodes() {
        assertEquals("Up", dispatch("Interface Status", attrs("p", "1")));
        assertEquals("Not Available", dispatch("Interface Status", attrs("p", "8")));
    }

    @Test
    void energyMonitor_wastedWhenInactiveButEquipmentActive() {
        assertEquals("Wasted", dispatch("Energy monitor",
                attrs("Occupancy Status", "inactive", "Equipment Status", "active")));
        assertEquals("Normal", dispatch("Energy monitor",
                attrs("Occupancy Status", "active", "Equipment Status", "active")));
    }

    @Test
    void lightStatus_onWhenIntensityPositive() {
        assertEquals("ON", dispatch("Light Status", attrs("Light Intensity", "5")));
        assertEquals("OFF", dispatch("Light Status", attrs("Light Intensity", "0")));
    }

    @Test
    void communicationStatus_faultyWhenFailureSincePositive() {
        assertEquals("Faulty", dispatch("Communication Status", attrs("Failure Since", "5")));
        assertEquals("Normal", dispatch("Communication Status", attrs("Failure Since", "0")));
    }

    @Test
    void configurationStatus_faultyWhenFailureSincePositive() {
        assertEquals("Faulty", dispatch("Configuration Status", attrs("Failure Since", "5")));
    }

    @Test
    void occupancyVariants_mapToOccupied() {
        assertEquals("Occupied", dispatch("Occupancy", attrs("p", "1")));
        assertEquals("Unoccupied", dispatch("Occupancy", attrs("p", "0")));
        assertEquals("Occupied", dispatch("Multiple Occupancy Status", attrs("p", "Occupied")));
    }

    @Test
    void deviceAndFeatureStatuses_map() {
        assertEquals("Online", dispatch("Device Status", attrs("p", "true")));
        assertEquals("Update Available", dispatch("Firmware Update Status", attrs("p", "true")));
        assertEquals("Connected", dispatch("Camera Status", attrs("p", "all_up")));
        assertEquals("Connected", dispatch("Microphone Status", attrs("p", "all_up")));
        assertEquals("Connected", dispatch("Audio Status", attrs("p", "all_up")));
        assertEquals("Connected", dispatch("Wifi Status", attrs("p", "all_up")));
        assertEquals("In Call", dispatch("Call Status", attrs("p", "in_call")));
        assertEquals("Normal", dispatch("Battery Status", attrs("p", "0")));
        assertEquals("Open", dispatch("Door", attrs("p", "1")));
    }
}
