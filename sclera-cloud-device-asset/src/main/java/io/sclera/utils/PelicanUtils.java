package io.sclera.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class PelicanUtils {

    HashMap<String, String> pelicanSensorAttributeNames = new HashMap<>();
    HashMap<String, String> pelicanSensorAttributeUnits = new HashMap<>();
    HashMap<String, String> pelicanSensorCategories = new HashMap<>();

    public void updatePelicanSensorAttributeNames()
    {
    	pelicanSensorAttributeNames.put("heatSetting", "Heat Setting");
    	pelicanSensorAttributeNames.put("coolSetting", "Cool Setting");
    	pelicanSensorAttributeNames.put("fan", "Fan");
    	pelicanSensorAttributeNames.put("humidity", "Humidity");
    	pelicanSensorAttributeNames.put("system", "System");
    	pelicanSensorAttributeNames.put("temperature", "Temperature");
    }
    
    public void updatePelicanSensorAttributeUnits()
    {
    	pelicanSensorAttributeUnits.put("temperatureFormatF", "\u00B0F");
    	pelicanSensorAttributeUnits.put("temperatureFormatC", "\u00B0C");
    	pelicanSensorAttributeUnits.put("humidityPercentage", "%");
    }
    
    public void updatePelicanSensorCategories()
    {
    	pelicanSensorCategories.put("heatSetting", "heat");
    	pelicanSensorCategories.put("coolSetting", "cool");
    	pelicanSensorCategories.put("fan", "fan");
    	pelicanSensorCategories.put("humidity", "humidity");
    	pelicanSensorCategories.put("system", "system");
    	pelicanSensorCategories.put("temperature", "temperature");
    }
    
    public String getPelicanSensorAttributeName(String pelican_sensor_attribute){
        return pelicanSensorAttributeNames.get(pelican_sensor_attribute);
    }

    public String getPelicanSensorAttributeUnit(String pelican_sensor_attribute_unit_name){
        return pelicanSensorAttributeUnits.get(pelican_sensor_attribute_unit_name);
    }
    
    public String getPelicanSensorAttributeCategory(String pelican_sensor_attribute){
        return pelicanSensorCategories.get(pelican_sensor_attribute);
    }
}
