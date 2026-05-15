package io.sclera.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sclera.dto.MeasuringInstrumentAttributesDTO;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InstrumentFormula {


    public Integer getNoOfParametersFromString(String input) {
        Integer maxNumber = 0;
        Pattern pattern = Pattern.compile("parameter_\\d+");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String match = matcher.group();
            int lastIndex = match.lastIndexOf('_');
            int number = Integer.parseInt(match.substring(lastIndex + 1));
            maxNumber = Math.max(maxNumber, number);
        }
        return maxNumber;
    }


    public String getValuebyMeasuringParameter(String calculation_type, String type, String value, String unit, List<MeasuringInstrumentAttributesDTO> attributes) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (attributes != null && attributes.size() > 0) {
                System.out.println(attributes);
                switch (type) {

                    case "Generic":
                        return getGenericValueByParameter(attributes);

                    case "Temperature":
                        return getTemperatureByParameter(attributes);

                    case "Humidity":
                        return getHumidityByParameter(attributes);

                    case "Pressure":
                        return getPressureByParameter(attributes);

                    case "Cylindrical Volume":
                        return getCylindricalVolumeByParameter(attributes);

                    case "Length":
                        return getLenghtValueByParameter(attributes);

                    case "Diameter":
                        return getDiameterValueByParameter(attributes);

                    case "Breadth":
                        return getBreadthValueByParameter(attributes);

                    case "Width":
                        return getWidthValueByParameter(attributes);

                    case "Rectangular Volume":
                        return getRectangularVolumeByParameter(attributes);

                    case "Rectangular Area":
                        return getRectangularAreaByParameter(attributes);

                    case "Flow":
                        return getFlowMeterValueByParameter(attributes);

                    case "Voltage":
                        return getVoltmeterValueByParameter(attributes);

                    case "Current":
                        return getCurrentValueByParameter(attributes);

                    case "CO2":
                        return getCO2AnalyserValueByParameter(attributes);

                    case "Light Intensity":
                        return getLightIntensityValueByParameter(attributes);

                    case "Power":
                        return getPowerByParameter(attributes);

                    case "Noise Level":
                        return getNoiseLevelByParameter(attributes);

                    case "VOC":
                        return getVOCAnalyserValueByParameter(attributes);

                    case "Air Quality":
                        return getAirQualityValueByParameter(attributes);

                    case "Electromagnetic Field":
                        return getEMFMeterValueByParameter(attributes);

                    case "Resistance":
                        return getOhmmeterValueByParameter(attributes);

                    case "Capacitance":
                        return getCapacitanceValueByParameter(attributes);

                    case "Speed":
                        return getSpeedByParameter(attributes);

                    case "Acceleration":
                        return getAccelerationByParameter(attributes);

                    case "Level":
                        return getLevelByParameter(attributes);

                    case "Alcohol Content":
                        return getAlcoholContentByParameter(attributes);

                    case "Dissolved Oxygen":
                        return getDissolvedOxygenByParameter(attributes);

                    case "Thickness":
                        return getThicknessByParameter(attributes);

                    case "Electrostatic Field":
                        return getElectrostaticFieldByParameter(attributes);

                    case "Water Salinity":
                        return getWaterSalinityByParameter(attributes);

                    case "Ferrite Content":
                        return getFerriteContentByParameter(attributes);

                    case "Density":
                        return getDensityValueByParameter(attributes);

                    case "Circular Area":
                        return getCircularAreaByParameter(attributes);

                    case "PH":
                        return getPHByParameter(attributes);

                    case "Inductance":
                        return getInductanceByParameter(attributes);

                    case "Water Hardness":
                        return getWaterHardnessByParameter(attributes);

                    case "Ladder":
                        return getLadderLengthbyParameter(attributes);

                    case "Battery":
                        return getBatteryLevelByParameter(attributes);

                    case "Energy Consumption":
                        return getEnergyConsumptionValueByParameter(attributes);

                    case "PM2.5 Level":
                        return getPMLevelValueByParameter(attributes);

                    case "Belt Tension":
                        return getBeltTensionlValueByParameter(attributes);

                    case "Gap Measurement":
                        return getGapMeasurementValueByParameter(attributes);

                    case "Oil Level":
                        return getOilLevelValueByParameter(attributes);

                    case "POE":
                        return getPOEValueByParameter(attributes);

                    case "Surface Roughness":
                        return getSurfaceRoughnessValueByParameter(attributes);

                    case "TDS":
                        return getTDSValueByParameter(attributes);

                    case "Thermal Conductivity":
                        return getThermalConductivityValueByParameter(attributes);

                    case "Tint Measurement":
                        return getTintMeasurementValueByParameter(attributes);

                    case "Torque":
                        return getTorqueValueByParameter(attributes);

                    case "Water Consumption":
                        return getWaterConsumptionValueByParameter(attributes);

                    case "Weight":
                        return getWeightValueByParameter(attributes);

                    case "Digital Tachometer":
                        return getDigitalTachometerValueByParameter(attributes);

                    case "Analog Tachometer":
                        return getAnalogTachometerValueByParameter(attributes);

                    case "Chloride Level":
                        return getChlorideLevelValueByParameter(attributes);

                    case "Sulfate Level":
                        return getSulfateLevelValueByParameter(attributes);

                    case "UV Index":
                        return getUVIndexValueByParameter(attributes);

                    case "Electromagnetic Interference":
                        return getElectromagneticInterferenceValueByParameter(attributes);

                    case "Wood Moisture Content":
                        return getWoodMoistureContentValueByParameter(attributes);

                    case "Belt Frequency":
                        return getBeltFrequencyValueByParameter(attributes);

                    case "Power Factor Analog":
                        return getPowerFactorAnalogValueByParameter(attributes);

                    case "Cable Length":
                        return getCableLengthValueByParameter(attributes);

                    case "Viscosity":
                        return getViscosityValueByParameter(attributes);

                    case "Power Factor Digital":
                        return getPowerFactorDigitalValueByParameter(attributes);

                    case "Differential Pressure":
                        return getDifferentialPressureValueByParameter(attributes);

                    case "Radiation Dose":
                        return getRadiationDoseValueByParameter(attributes);

                    case "Insulation Resistance":
                        return getInsulationResistanceValueByParameter(attributes);

                    case "Temperature 3":
                        return getTemperature3ValueByParameter(attributes);

                    case "Solar Irradiance":
                        return getSolarIrradianceValueByParameter(attributes);

                    case "ORP":
                        return getORPValueByParameter(attributes);

                    case "Impedance":
                        return getImpedanceValueByParameter(attributes);

                    case "Notepad":
                        return getNotepadValueByParameter(attributes);

                    case "SF6 Purity":
                        return getSF6PurityValueByParameter(attributes);

                    case "Force":
                        return getForceValueByParameter(attributes);

                    case "COD":
                        return getCODValueByParameter(attributes);

                    case "Magnetic Permeability":
                        return getMagneticPermeabilityValueByParameter(attributes);

                    case "Chlorine Dioxide Level":
                        return getChlorineDioxideLevelValueByParameter(attributes);

                    case "Surface Tension":
                        return getSurfaceTensionValueByParameter(attributes);

                    case "Vacuum Pressure":
                        return getVacuumPressureValueByParameter(attributes);

                    case "Printer Ink Level":
                        return getPrinterInkPercentageValueByParameter(attributes);

                    case "Interface Status":
                        return getInterfaceStatusByParameter(attributes);

                    case "Flow 1":
                        return getFlowValueByParameter(attributes);

                    case "Energy monitor":
                        return getEnergyConsumptionStatusByOccupancyAndEquipmentParameter(attributes);

                    case "Occupancy Counter":
                        return getCounterValueBySingleCounterInAndOut(attributes);

                    case "Occupancy Counter 1":
                        return getCounterValueByMultipleCounterInAndOut(attributes);

                    case "Light Status":
                        return getLightStatusByParameter(attributes);

                    case "Temperature 4":
                        return getFahrenheitByCelsiusParameter(attributes);

                    case "Communication Status":
                        return getCommunicationStatusByParameter(attributes);

                    case "Configuration Status":
                        return getConfigurationStatusByParameter(attributes);

                    case "Multiple Light Intensity":
                        return getMultipleLightIntensityByParameter(attributes);

                    case "Multiple Occupancy Status":
                        return getMultipleOccupancyStatusByParameter(attributes);

                    case "Occupancy":
                        return getOccupancyStatusByParameter(attributes);

                    case "Multiple Occupancy Counter":
                        return getCounterValueByMultipleAttributeCounterInAndOut(attributes);

                    case "Device Status":
                        return getDeviceStatus(attributes);

                    case "Firmware Update Status":
                        return getFirmwareUpdateStatus(attributes);

                    case "Camera Status":
                        return getCameraStatus(attributes);

                    case "Microphone Status":
                        return getMicrophoneStatus(attributes);

                    case "Audio Status":
                        return getAudioStatus(attributes);

                    case "Wifi Status":
                        return getWifiStatus(attributes);

                    case "Call Status":
                        return getCallStatus(attributes);

                    case "Temperature 5":
                        return getCelsiusByFahrenheitParameter(attributes);

                    case "Battery Status":
                        return getBatteryStatus(attributes);

                    case "Door":
                        return getDoorStatus(attributes);

                    case "Temperature 6":
                        return getTemperatureByParameter(attributes);

                    case "Schedule":
                        return getScheduleValueByParameter(attributes);

                    default:
                        return getDefaultByParameter(attributes);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in getValuebyMeasuringParameter for type : " + type);
        }
        return null;
    }


    private String getDoorStatus(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("0")) {
            return "Closed";
        } else if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("1")) {
            return "Open";
        }
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    private String getBatteryStatus(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("0")) {
            return "Normal";
        } else if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("1")) {
            return "Low";
        }
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getDefaultByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getGenericValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getTemperatureByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getHumidityByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getPressureByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getCylindricalVolumeByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOS) {
        try {
            Double diameter = 0.0;
            Double length = 0.0;
            for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOS) {
                if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getName().equals("Diameter")) {
                    diameter = Double.parseDouble(measuringInstrumentAttributesDTO.getValue().toString());
                }
                if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getName().equals("Length")) {
                    length = Double.parseDouble(measuringInstrumentAttributesDTO.getValue().toString());
                }
            }
            if (length != null && diameter != null) {
                DecimalFormat decimal_format = new DecimalFormat("#.##");
                return String.valueOf(decimal_format.format(3.1415 * length * diameter * diameter / 4));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String getLenghtValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getDiameterValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getBreadthValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getWidthValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getRectangularAreaByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        Double breadth = 0.0;
        Double length = 0.0;
        try {
            for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOList) {
                if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getValue() != null && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {
                    if (measuringInstrumentAttributesDTO.getName().equals("Length")) {
                        length = Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                    } else if (measuringInstrumentAttributesDTO.getName().equals("Breadth")) {
                        breadth = Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                    }
                }
            }
            if (length != null && breadth != null) {
                DecimalFormat decimal_format = new DecimalFormat("#.##");
                return String.valueOf(decimal_format.format(length * breadth));
            }
        } catch (Exception e) {
            System.out.println("FAILED TO CALCULATE RECTANGULAR VOLUME BY PARAMETER");
        }
        return null;
    }


    public String getRectangularVolumeByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        Double breadth = 0.0;
        Double length = 0.0;
        Double width = 0.0;
        try {
            for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOList) {
                if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getValue() != null && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {
                    if (measuringInstrumentAttributesDTO.getName().equals("Length")) {
                        length = Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                    } else if (measuringInstrumentAttributesDTO.getName().equals("Breadth")) {
                        breadth = Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                    } else if (measuringInstrumentAttributesDTO.getName().equals("Width")) {
                        width = Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                    }
                }
            }
            if (length != null && breadth != null && width != null) {
                DecimalFormat decimal_format = new DecimalFormat("#.##");
                return String.valueOf(decimal_format.format(length * breadth * width));
            }
        } catch (Exception e) {
            System.out.println("FAILED TO CALCULATE RECTANGULAR VOLUME BY PARAMETER");
        }
        return null;
    }


    public String getFlowMeterValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getVoltmeterValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getCurrentValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getCO2AnalyserValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getLightIntensityValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        String value = measuringInstrumentAttributesDTOList.get(0).getValue();
        if (value != null && (!value.isBlank())) {
            DecimalFormat decimal_format = new DecimalFormat("#.##");
            return String.valueOf(decimal_format.format(Double.parseDouble(value)));
        }
        return null;
    }

    public String getPowerByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        try {
            Double voltage = null;
            Double current = null;
            for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOList) {
                if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getValue() != null && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {
                    if (measuringInstrumentAttributesDTO.getName().equals("Voltage")) {
                        voltage = Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                    } else if (measuringInstrumentAttributesDTO.getName().equals("Current")) {
                        current = Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                    }
                }
            }
            if (voltage != null && current != null) {
                DecimalFormat decimal_format = new DecimalFormat("#.##");
                return String.valueOf(decimal_format.format(voltage * current));
            }

        } catch (Exception e) {
            System.out.println("FAILED TO CALCULATE CYLINDRICAL VOLUME BY PARAMETER");
        }
        return null;
    }

    public String getNoiseLevelByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getVOCAnalyserValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getAirQualityValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getEMFMeterValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getOhmmeterValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getCapacitanceValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getSpeedByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getAccelerationByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getLevelByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getAlcoholContentByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getDissolvedOxygenByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getThicknessByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getElectrostaticFieldByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getWaterSalinityByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getFerriteContentByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getDensityValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getCircularAreaByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOS) {

        try {
            Double diameter = null;
            for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOS) {
                if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getName().equals("Diameter") && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {
                    diameter = Double.parseDouble(measuringInstrumentAttributesDTO.getValue().toString());
                }
            }
            if (diameter != null) {
                DecimalFormat decimal_format = new DecimalFormat("#.##");
                return String.valueOf(decimal_format.format(3.1415 * diameter * diameter / 4));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String getPHByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getInductanceByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getWaterHardnessByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getLadderLengthbyParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOS) {
        try {
            Double base_length = null;
            for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOS) {
                if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getName().equals("Length") && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {
                    base_length = Double.parseDouble(measuringInstrumentAttributesDTO.getValue().toString()) / Math.sqrt(Double.parseDouble("17"));
                    if (base_length != null) {
                        DecimalFormat decimal_format = new DecimalFormat("#.##");
                        return String.valueOf(decimal_format.format(base_length));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED TO CALCULATE LADDER LENGTH BY PARAMETER");
        }
        return null;
    }

    public String getBatteryLevelByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getEnergyConsumptionValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getPMLevelValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getBeltTensionlValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getGapMeasurementValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getOilLevelValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getPOEValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getSurfaceRoughnessValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getTDSValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getThermalConductivityValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getTintMeasurementValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getTorqueValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getWaterConsumptionValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getWeightValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getDigitalTachometerValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getAnalogTachometerValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getChlorideLevelValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getSulfateLevelValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getUVIndexValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getElectromagneticInterferenceValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getWoodMoistureContentValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getBeltFrequencyValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getPowerFactorAnalogValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getCableLengthValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getViscosityValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getPowerFactorDigitalValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getDifferentialPressureValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getRadiationDoseValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getInsulationResistanceValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getTemperature3ValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getSolarIrradianceValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getORPValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getImpedanceValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getNotepadValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getSF6PurityValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getForceValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getCODValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getMagneticPermeabilityValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getChlorineDioxideLevelValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getSurfaceTensionValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }


    public String getVacuumPressureValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getScheduleValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        return measuringInstrumentAttributesDTOList.get(0).getValue();
    }

    public String getPrinterInkPercentageValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        try {
            Double maximumcapacity = 0.0;
            Double currentCapacity = 0.0;
            for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOList) {
                if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getValue() != null && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {
                    if (measuringInstrumentAttributesDTO.getName().equals("Maximum Capacity")) {
                        maximumcapacity = Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                    } else if (measuringInstrumentAttributesDTO.getName().equals("Current Capacity")) {
                        currentCapacity = Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                    }
                }
            }
            Double percentage_value = (currentCapacity / maximumcapacity) * 100;
            if (percentage_value != null) {
                DecimalFormat decimal_format = new DecimalFormat("#.##");
                return String.valueOf(decimal_format.format(percentage_value));
            }
        } catch (Exception e) {
            System.out.println("FAILED TO CALCULATE PERCENTAGE BY PARAMETER");
        }
        return null;
    }

    public String getInterfaceStatusByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        if (!measuringInstrumentAttributesDTOList.isEmpty()) {
            MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO = measuringInstrumentAttributesDTOList.get(0);
            if (measuringInstrumentAttributesDTO.getValue() != null) {
                switch (measuringInstrumentAttributesDTO.getValue()) {
                    case "1":
                        return "Up";
                    case "2":
                        return "Down";
                    case "3":
                        return "Testing";
                    case "4":
                        return "Unknown";
                    case "5":
                        return "Dormant";
                    case "6":
                        return "Not Present";
                    case "7":
                        return "Lower Layer Down";
                    default:
                        return "Not Available";
                }
            }
        }
        return null;
    }


    public String getFlowValueByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        Double pulseCountValue = 0.0;
        Double kFactorValue = 0.0;
        Double offsetValue = 0.0;
        for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOList) {
            String valueOfAttribute = measuringInstrumentAttributesDTO.getValue();
            if (measuringInstrumentAttributesDTO.getName() != null && valueOfAttribute != null && !valueOfAttribute.isEmpty()) {
                if (measuringInstrumentAttributesDTO.getName().equals("Pulse Count")) {
                    pulseCountValue = Double.parseDouble(valueOfAttribute);
                } else if (measuringInstrumentAttributesDTO.getName().equals("K-Factor")) {
                    kFactorValue = Double.parseDouble(valueOfAttribute);
                } else if (measuringInstrumentAttributesDTO.getName().equals("Offset")) {
                    offsetValue = Double.parseDouble(valueOfAttribute);
                }
            }
        }
        Double value = kFactorValue * pulseCountValue;
        if (value != null && value > 0) {
            value = value + offsetValue;
        }
        if (value != null) {
            DecimalFormat decimal_format = new DecimalFormat("#.##");
            return String.valueOf(decimal_format.format(value));
        }
        return null;
    }

    public String getEnergyConsumptionStatusByOccupancyAndEquipmentParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        String occupancyStatus = null;
        String equipmentStatus = null;
        for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOList) {
            if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getValue() != null && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {
                if (measuringInstrumentAttributesDTO.getName().equals("Occupancy Status")) {
                    occupancyStatus = measuringInstrumentAttributesDTO.getValue();
                } else if (measuringInstrumentAttributesDTO.getName().equals("Equipment Status")) {
                    equipmentStatus = measuringInstrumentAttributesDTO.getValue();
                }
            }
        }

        if (occupancyStatus != null && equipmentStatus != null && occupancyStatus.equalsIgnoreCase("inactive") && equipmentStatus.equalsIgnoreCase("active")) {
            return "Wasted";
        } else {
            return "Normal";
        }
    }

    public String getCounterValueBySingleCounterInAndOut(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        Double count_in_value = 0.0;
        Double count_out_value = 0.0;
        for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOList) {
            if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getValue() != null && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {
                if (measuringInstrumentAttributesDTO.getName().equalsIgnoreCase("Counter In")) {
                    count_in_value = Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                } else if (measuringInstrumentAttributesDTO.getName().equalsIgnoreCase("Counter Out")) {
                    count_out_value = Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                }
            }
        }
        Double value = count_in_value - count_out_value;
        if (value != null && value >= 0) {
            return String.valueOf(value.intValue());
        }
        return null;
    }


    public String getCounterValueByMultipleAttributeCounterInAndOut(List<MeasuringInstrumentAttributesDTO> attributes) {
        Double value = 0.0;
        int count = 1;
        for (int i = 0; i < attributes.size() - 1; i += 2) {
            MeasuringInstrumentAttributesDTO current = attributes.get(i);
            MeasuringInstrumentAttributesDTO next = attributes.get(i + 1);
            if (current != null && next != null && current.getName() != null && current.getValue() != null && !current.getValue().isEmpty() && next.getValue() != null && !next.getValue().isEmpty()) {
                if (current.getName().trim().equals("Counter In-" + count) && next.getName().trim().equals("Counter Out-" + count)) {
                    value = value + (Double.parseDouble(current.getValue()) - Double.parseDouble(next.getValue()));
                }
                count++;
            }
        }
        if (value != null && value >= 0) {
            return String.valueOf(value.intValue());
        } else {
            return null;
        }
    }

    public String getCounterValueByMultipleCounterInAndOut(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        Double counterInvalue = 0.0;
        Double counterOutvalue = 0.0;

        for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOList) {
            if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getValue() != null && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {
                if (measuringInstrumentAttributesDTO.getName().equalsIgnoreCase("Counter In-1")) {
                    counterInvalue = counterInvalue + Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                } else if (measuringInstrumentAttributesDTO.getName().equalsIgnoreCase("Counter Out-1")) {
                    counterOutvalue = counterOutvalue + Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                } else if (measuringInstrumentAttributesDTO.getName().equalsIgnoreCase("Counter In-2")) {
                    counterInvalue = counterInvalue + Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                } else if (measuringInstrumentAttributesDTO.getName().equalsIgnoreCase("Counter Out-2")) {
                    counterOutvalue = counterOutvalue + Double.parseDouble(measuringInstrumentAttributesDTO.getValue());
                }
            }
        }
        Double value = counterInvalue - counterOutvalue;
        if (value != null && value >= 0) {
            return String.valueOf(value.intValue());
        }
        return null;
    }


    public String getLightStatusByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOList) {
            if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getValue() != null) {
                if (measuringInstrumentAttributesDTO.getName().equals("Light Intensity") && !measuringInstrumentAttributesDTO.getValue().isEmpty() && Double.parseDouble(measuringInstrumentAttributesDTO.getValue()) > 0.0) {
                    return "ON";
                }
                return "OFF";
            }
        }
        return null;
    }


    public String getFahrenheitByCelsiusParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        Double value = 0.0;
        for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOList) {
            if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getName().equalsIgnoreCase("Temperature") && measuringInstrumentAttributesDTO.getValue() != null && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {
                value = (Double.parseDouble(measuringInstrumentAttributesDTO.getValue()) * 9 / 5) + 32;
                if (value != null) {
                    DecimalFormat decimal_format = new DecimalFormat("#.##");
                    return String.valueOf(decimal_format.format(value));
                }
            }
        }
        return null;
    }


    public String getConfigurationStatusByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOS) {
        for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOS) {
            if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getName().equalsIgnoreCase("Failure Since") && measuringInstrumentAttributesDTO.getValue() != null && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {
                if (Double.parseDouble(measuringInstrumentAttributesDTO.getValue()) > 0) {
                    return "Faulty";
                }
                return "Normal";
            }
        }
        return null;
    }


    public String getCommunicationStatusByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOS) {
        for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOS) {
            if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getName().equalsIgnoreCase("Failure Since") && measuringInstrumentAttributesDTO.getValue() != null && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {
                if (Double.parseDouble(measuringInstrumentAttributesDTO.getValue()) > 0) {
                    return "Faulty";
                }
                return "Normal";
            }
        }
        return null;
    }


    public String getMultipleLightIntensityByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOS) {
        try {
            double totalIntensity = 0.0;
            int intensityCount = 0;
            boolean checkForIntensity = false;

            for (MeasuringInstrumentAttributesDTO attribute : measuringInstrumentAttributesDTOS) {
                String parameter = this.getParameter(attribute.getValue());
                if (parameter != null && !parameter.isBlank() && !parameter.isEmpty()) {
                    try {
                        double parameterValue = Double.parseDouble(parameter);
                        if (parameterValue != 0.0) {
                            totalIntensity += parameterValue;
                            intensityCount++;
                        } else {
                            checkForIntensity = true;
                        }
                    } catch (Exception e) {
                        System.out.println("Exception in getMultipleLightIntensityByParameter : " + e.getMessage());
                    }
                }
            }

            if (intensityCount > 0) {
                double averageIntensity = totalIntensity / intensityCount;
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                return decimalFormat.format(averageIntensity);
            }

            if (checkForIntensity) {
                return "0";
            }

        } catch (Exception e) {
            System.out.println("Exception in getMultipleLightIntensityByParameter : " + e.getMessage());
        }

        return null;
    }


    private String getParameter(String parameter) {
        if (parameter != null) {
            return parameter;
        }
        return null;
    }

    public String getMultipleOccupancyStatusByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOS) {
        try {
            boolean isUnOccupied = false;
            for (MeasuringInstrumentAttributesDTO dto : measuringInstrumentAttributesDTOS) {
                String parameter = this.getParameter(dto.getValue());
                if (parameter != null && !parameter.isEmpty()) {
                    if (parameter.equalsIgnoreCase("Unoccupied") || parameter.equalsIgnoreCase("0") ||
                            parameter.equalsIgnoreCase("0.0") || parameter.equalsIgnoreCase("Inactive") ||
                            parameter.equalsIgnoreCase("UNOCC")) {
                        isUnOccupied = true;
                    } else if (parameter.equalsIgnoreCase("Occupied") || parameter.equalsIgnoreCase("1") ||
                            parameter.equalsIgnoreCase("1.0") || parameter.equalsIgnoreCase("Active") ||
                            parameter.equalsIgnoreCase("OCC")) {
                        return "Occupied";
                    }
                }
            }
            if (isUnOccupied) {
                return "Unoccupied";
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public String getOccupancyStatusByParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOList) {
            if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getValue() != null && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {
                if (measuringInstrumentAttributesDTO.getValue().equalsIgnoreCase("Unoccupied") || measuringInstrumentAttributesDTO.getValue().equalsIgnoreCase("0") || measuringInstrumentAttributesDTO.getValue().equalsIgnoreCase("0.0") || measuringInstrumentAttributesDTO.getValue().equalsIgnoreCase("Inactive") || measuringInstrumentAttributesDTO.getValue().equalsIgnoreCase("UNOCC")) {
                    return "Unoccupied";
                } else if (measuringInstrumentAttributesDTO.getValue().equalsIgnoreCase("Occupied") || measuringInstrumentAttributesDTO.getValue().equalsIgnoreCase("1") || measuringInstrumentAttributesDTO.getValue().equalsIgnoreCase("1.0") || measuringInstrumentAttributesDTO.getValue().equalsIgnoreCase("Active") || measuringInstrumentAttributesDTO.getValue().equalsIgnoreCase("OCC")) {
                    return "Occupied";
                }
            }
        }
        return null;
    }


    private String getDeviceStatus(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("")) {
            return "";
        } else if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("true")) {
            return "Online";
        } else {
            return "Offline";
        }
    }


    private String getFirmwareUpdateStatus(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("")) {
            return "";
        } else if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("true")) {
            return "Update Available";
        } else {
            return "Up to date";
        }

    }


    private String getCameraStatus(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {

        if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("")) {
            return "";
        } else if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("all_up")) {
            return "Connected";
        } else {
            return "Disconnected";
        }
    }

    private String getMicrophoneStatus(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("")) {
            return "";
        } else if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("all_up")) {
            return "Connected";
        } else {
            return "Disconnected";
        }
    }

    private String getAudioStatus(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("")) {
            return "";
        } else if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("all_up")) {
            return "Connected";
        } else {
            return "Disconnected";
        }
    }


    private String getWifiStatus(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("")) {
            return "";
        } else if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("all_up")) {
            return "Connected";
        } else {
            return "Disconnected";
        }
    }

    private String getCallStatus(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        if (measuringInstrumentAttributesDTOList.get(0).getValue().equals("")) {
            return "";
        } else if (measuringInstrumentAttributesDTOList.get(0).getValue().equalsIgnoreCase("in_call")) {
            return "In Call";
        } else if (measuringInstrumentAttributesDTOList.get(0).getValue().equalsIgnoreCase("not_in_call")) {
            return "Not In Call";
        } else {
            return "Unsupported";
        }
    }


    public String getCelsiusByFahrenheitParameter(List<MeasuringInstrumentAttributesDTO> measuringInstrumentAttributesDTOList) {
        Double value = 0.0;
        for (MeasuringInstrumentAttributesDTO measuringInstrumentAttributesDTO : measuringInstrumentAttributesDTOList) {
            if (measuringInstrumentAttributesDTO.getName() != null && measuringInstrumentAttributesDTO.getName().equalsIgnoreCase("Temperature") && measuringInstrumentAttributesDTO.getValue() != null && !measuringInstrumentAttributesDTO.getValue().isEmpty()) {


                value = (Double.parseDouble(measuringInstrumentAttributesDTO.getValue()) - 32) * (5.0 / 9.0);

                System.out.println("------ Value getCelsiusByFahrenheitParameter -----" + value);

                if (value != null) {
                    DecimalFormat decimal_format = new DecimalFormat("#.##");
                    return String.valueOf(decimal_format.format(value));
                }
            }
        }
        return null;
    }

}

