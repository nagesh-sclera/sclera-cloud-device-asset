package io.sclera.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class MonnitUtils {
    HashMap<String, String> monnitCategories = new HashMap<>();
    HashMap<String, String> monnitUnits = new HashMap<>();
    
    public void updateMonnitCategoriesMap()
    {
    	monnitCategories.put("1", "analog");
        monnitCategories.put("2", "temperature");
        monnitCategories.put("3", "conductivity");
        monnitCategories.put("4", "water");
        monnitCategories.put("5", "motion");
        monnitCategories.put("6", "occupancy");
        monnitCategories.put("9", "digital");
        monnitCategories.put("11", "button");
        monnitCategories.put("12", "generic");
        monnitCategories.put("13", "alarm");
        monnitCategories.put("15", "motion");
        monnitCategories.put("16", "motion");
        monnitCategories.put("19", "generic");
        monnitCategories.put("20", "motion");
        monnitCategories.put("21", "humidity");
        monnitCategories.put("22", "current");
        monnitCategories.put("23", "motion");
        monnitCategories.put("24", "generic");
        monnitCategories.put("26", "height");
        monnitCategories.put("27", "light");
        monnitCategories.put("28", "location");
        monnitCategories.put("30", "humidity");
        monnitCategories.put("32", "voltage");
        monnitCategories.put("33", "occupancy");
        monnitCategories.put("34", "gas");
        monnitCategories.put("35", "temperature");
        monnitCategories.put("36", "height");
        monnitCategories.put("39", "occupancy");
        monnitCategories.put("40", "speed");
        monnitCategories.put("42", "generic");
        monnitCategories.put("43", "humidity");
        monnitCategories.put("44", "generic");
        monnitCategories.put("45", "generic");
        monnitCategories.put("46", "temperature");
        monnitCategories.put("47", "count");
        monnitCategories.put("48", "count");
        monnitCategories.put("51", "occupancy");
        monnitCategories.put("52 ","flow");
        monnitCategories.put("55", "power");
        monnitCategories.put("59", "voltage");
        monnitCategories.put("64", "voltage");
        monnitCategories.put("65", "temperature");
        monnitCategories.put("66", "generic");
        monnitCategories.put("67", "generic");
        monnitCategories.put("69", "count");
        monnitCategories.put("70", "conductivity");
        monnitCategories.put("71", "voltage");
        monnitCategories.put("72", "voltage");
        monnitCategories.put("73", "count");
        monnitCategories.put("74", "voltage");
        monnitCategories.put("75", "tilt");
        monnitCategories.put("76", "generic");
        monnitCategories.put("77", "occupancy");
        monnitCategories.put("78", "water");
        monnitCategories.put("79", "pressure");
        monnitCategories.put("82", "pressure");
        monnitCategories.put("83", "pressure");
        monnitCategories.put("84", "temperature");
        monnitCategories.put("85", "generic");
        monnitCategories.put("86", "temperature");
        monnitCategories.put("89", "current");
        monnitCategories.put("90", "count");
        monnitCategories.put("91", "temperature");
        monnitCategories.put("92", "temperature");
        monnitCategories.put("93", "current");
        monnitCategories.put("94", "current");
        monnitCategories.put("95", "vibration");
        monnitCategories.put("97", "temperature");
        monnitCategories.put("99", "conductivity");
        monnitCategories.put("100", "temperature");
        monnitCategories.put("101", "motion");
        monnitCategories.put("102", "gas");
        monnitCategories.put("103", "pressure");
        monnitCategories.put("104", "vibration");
        monnitCategories.put("105", "generic");
        monnitCategories.put("106", "co2");
        monnitCategories.put("107", "light");
        monnitCategories.put("108", "power");
        monnitCategories.put("109", "current");
        monnitCategories.put("110", "generic");
        monnitCategories.put("111", "vibration");
        monnitCategories.put("113", "voltage");
        monnitCategories.put("114", "speed");
        monnitCategories.put("116", "gas");
        monnitCategories.put("117", "location");
        monnitCategories.put("118", "location");
        monnitCategories.put("119", "occupancy");
        monnitCategories.put("120", "current");
        monnitCategories.put("121", "pressure");
        monnitCategories.put("122", "voltage");
        monnitCategories.put("123", "voltage");
        monnitCategories.put("124", "generic");
        monnitCategories.put("126", "force");
        monnitCategories.put("127", "generic");
        monnitCategories.put("128", "generic");
        monnitCategories.put("129", "current");
        monnitCategories.put("130", "tilt");
    }
    
    /*
     * Unicode character of degree is \u00B0
     * Unicode character of micro is \u00B5
     * 
     * */
    public void updateMonnitUnitsMap()
    {
        monnitUnits.put("1", "V");
        monnitUnits.put("2", "\u00B0C");
        monnitUnits.put("3", null);
        monnitUnits.put("4", null);
        monnitUnits.put("5", null);
        monnitUnits.put("6", null);
        monnitUnits.put("9", null);
        monnitUnits.put("11", null);
        monnitUnits.put("12", null);
        monnitUnits.put("13", null);
        monnitUnits.put("15", null);
        monnitUnits.put("16", null);
        monnitUnits.put("19", null);
        monnitUnits.put("20", null);
        monnitUnits.put("21", "%");
        monnitUnits.put("22", "mA");
        monnitUnits.put("23", null);
        monnitUnits.put("24", null);
        monnitUnits.put("26", null);
        monnitUnits.put("27", null);
        monnitUnits.put("28", null);
        monnitUnits.put("30", "%");
        monnitUnits.put("32", null);
        monnitUnits.put("33", null);
        monnitUnits.put("34", "\u00B5g/m3");
        monnitUnits.put("35", "\u00B0C");
        monnitUnits.put("36", null);
        monnitUnits.put("39", null);
        monnitUnits.put("40", null);
        monnitUnits.put("42", null);
        monnitUnits.put("43", "%");
        monnitUnits.put("44", null);
        monnitUnits.put("45", null);
        monnitUnits.put("46", "\u00B0C");
        monnitUnits.put("47", null);
        monnitUnits.put("48", null);
        monnitUnits.put("51", null);
        monnitUnits.put("52 ", null);
        monnitUnits.put("55", null);
        monnitUnits.put("59", "V");
        monnitUnits.put("64", "V");
        monnitUnits.put("65", "\u00B0C");
        monnitUnits.put("66", null);
        monnitUnits.put("67", null);
        monnitUnits.put("69", null);
        monnitUnits.put("70", null);
        monnitUnits.put("71", "V");
        monnitUnits.put("72", "V");
        monnitUnits.put("73", null);
        monnitUnits.put("74", "V");
        monnitUnits.put("75", null);
        monnitUnits.put("76", null);
        monnitUnits.put("77", null);
        monnitUnits.put("78", null);
        monnitUnits.put("79", "PSI");
        monnitUnits.put("82", "PSI");
        monnitUnits.put("83", "PSI");
        monnitUnits.put("84", "\u00B0C");
        monnitUnits.put("85", null);
        monnitUnits.put("86", "\u00B0C");
        monnitUnits.put("89", null);
        monnitUnits.put("90", null);
        monnitUnits.put("91", "\u00B0C");
        monnitUnits.put("92", "\u00B0C");
        monnitUnits.put("93", null);
        monnitUnits.put("94", null);
        monnitUnits.put("95", null);
        monnitUnits.put("97", "\u00B0C");
        monnitUnits.put("99", null);
        monnitUnits.put("100", "\u00B0C");
        monnitUnits.put("101", null);
        monnitUnits.put("102", "\u00B5g/m3");
        monnitUnits.put("103", "PSI");
        monnitUnits.put("104", null);
        monnitUnits.put("105", null);
        monnitUnits.put("106", null);
        monnitUnits.put("107", null);
        monnitUnits.put("108", null);
        monnitUnits.put("109", null);
        monnitUnits.put("110", null);
        monnitUnits.put("111", null);
        monnitUnits.put("113", "V");
        monnitUnits.put("114", null);
        monnitUnits.put("116", "\u00B5g/m3");
        monnitUnits.put("117", null);
        monnitUnits.put("118", null);
        monnitUnits.put("119", null);
        monnitUnits.put("120", null);
        monnitUnits.put("121", "PSI");
        monnitUnits.put("122", null);
        monnitUnits.put("123", null);
        monnitUnits.put("124", null);
        monnitUnits.put("126", null);
        monnitUnits.put("127", null);
        monnitUnits.put("128", null);
        monnitUnits.put("129", null);
        monnitUnits.put("130", null);
    }
    
    public String getMonnitCategory(String monnit_application_id){
        return monnitCategories.get(monnit_application_id);
    }
    
    public String getMonnitUnit(String monnit_application_id){
        return monnitUnits.get(monnit_application_id);
    }
}
