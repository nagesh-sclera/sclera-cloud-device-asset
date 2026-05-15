package io.sclera.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
@Component
public class EcobeeUtils {

    private String oauth_token;

    public String getOauth_token() {
        return oauth_token;
    }

    public void setOauth_token(String oauth_token) {
        this.oauth_token = oauth_token;
    }
    HashMap<String, String> ecobeeSensorAttributeNames = new HashMap<>();
    HashMap<String, String> ecobeeSensorAttributeUnits = new HashMap<>();
    HashMap<String, String> ecobeeSensorCategories = new HashMap<>();

    public void updateEcobeeSensorAttributeNames()
    {
        ecobeeSensorAttributeNames.put("heatSetting", "Heat Setting");
        ecobeeSensorAttributeNames.put("coolSetting", "Cool Setting");
        ecobeeSensorAttributeNames.put("fan", "Fan");
        ecobeeSensorAttributeNames.put("humidity", "Humidity");
        ecobeeSensorAttributeNames.put("system", "System");
        ecobeeSensorAttributeNames.put("temperature", "Temperature");
    }

    public void updateEcobeeSensorAttributeUnits()
    {
        ecobeeSensorAttributeUnits.put("temperatureFormatF", "\u00B0F");
        ecobeeSensorAttributeUnits.put("temperatureFormatC", "\u00B0C");
        ecobeeSensorAttributeUnits.put("humidityPercentage", "%");
    }

    public void updateEcobeeSensorCategories()
    {
        ecobeeSensorCategories.put("heatSetting", "heat");
        ecobeeSensorCategories.put("coolSetting", "cool");
        ecobeeSensorCategories.put("fan", "fan");
        ecobeeSensorCategories.put("humidity", "humidity");
        ecobeeSensorCategories.put("system", "system");
        ecobeeSensorCategories.put("temperature", "temperature");
    }

    public String getEcobeeSensorAttributeName(String pelican_sensor_attribute){
        return ecobeeSensorAttributeNames.get(pelican_sensor_attribute);
    }

    public String getEcobeeSensorAttributeUnit(String pelican_sensor_attribute_unit_name){
        return ecobeeSensorAttributeUnits.get(pelican_sensor_attribute_unit_name);
    }

    public String getEcobeeSensorAttributeCategory(String pelican_sensor_attribute){
        return ecobeeSensorCategories.get(pelican_sensor_attribute);
    }
}
