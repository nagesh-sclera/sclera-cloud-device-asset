package io.sclera.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class MyDevicesUtils {
	
	HashMap<String, String> myDevicesUnitsMapping = new HashMap<>();
	HashMap<String, String> myDevicesCategoriesMapping = new HashMap<>();
	
	public Integer getPercentage(Integer current_value, Integer min_value, Integer max_value){
		Integer percentage = ((current_value - min_value) * 100) / (max_value - min_value);
		if(percentage > 100)
			percentage = 100;
		if(percentage < 0)
			percentage = 0;
		return percentage;
	}
	
	/*
     * Unicode character of degree(°) is \u00B0 
     * Unicode character of micro(µ) is \u00B5
     * Unicode character of ohm(Ω) is \u2126
     * 
     * */
	public void updateMyDevicesUnitsMap()
    {

        myDevicesUnitsMapping.put("kwh","kWh");
        myDevicesUnitsMapping.put("calorie","cal");
        myDevicesUnitsMapping.put("kvah","kVAh");
        myDevicesUnitsMapping.put("wh","Wh");
        myDevicesUnitsMapping.put("ma","mA");
        myDevicesUnitsMapping.put("a","A");
        myDevicesUnitsMapping.put("kw","kW");
        myDevicesUnitsMapping.put("w","W");
        myDevicesUnitsMapping.put("kva","kVA");
        myDevicesUnitsMapping.put("cm","cm");
        myDevicesUnitsMapping.put("μm","\u00B5m");
        myDevicesUnitsMapping.put("c","\u00B0C");
        myDevicesUnitsMapping.put("f","\u00B0F");
        myDevicesUnitsMapping.put("k","\u00B0K");
        myDevicesUnitsMapping.put("kohm","k\u2126");
        myDevicesUnitsMapping.put("ohm","\u2126");
        myDevicesUnitsMapping.put("mgpcm","\u00B5g/m3");
        myDevicesUnitsMapping.put("in","in");
        myDevicesUnitsMapping.put("ft","ft");
        myDevicesUnitsMapping.put("m","m");
        myDevicesUnitsMapping.put("yd","yd");
        myDevicesUnitsMapping.put("dbm","dBm");
        myDevicesUnitsMapping.put("ph","PH");
        myDevicesUnitsMapping.put("pa","Pa");
        myDevicesUnitsMapping.put("hpa","hPa");
        myDevicesUnitsMapping.put("kpa","kPa");
        myDevicesUnitsMapping.put("bar","bar");
        myDevicesUnitsMapping.put("mbar","mbar");
        myDevicesUnitsMapping.put("psi","psi");
        myDevicesUnitsMapping.put("p","%");
        myDevicesUnitsMapping.put("r","r");
        myDevicesUnitsMapping.put("v","V");
        myDevicesUnitsMapping.put("dps","d/s");
        myDevicesUnitsMapping.put("rpm","rpm");
        myDevicesUnitsMapping.put("mv","mV");
        myDevicesUnitsMapping.put("lux","lux");
        myDevicesUnitsMapping.put("mm","mm");
        myDevicesUnitsMapping.put("hz","Hz");
        myDevicesUnitsMapping.put("khz","kHz");
        myDevicesUnitsMapping.put("mph","m/h");
        myDevicesUnitsMapping.put("knot","knot");
        myDevicesUnitsMapping.put("kmph","km/h");
        myDevicesUnitsMapping.put("mmph","mm/h");
        myDevicesUnitsMapping.put("gpkg","g/kg");
        myDevicesUnitsMapping.put("fau","FAU");
        myDevicesUnitsMapping.put("ntu","NTU");
        myDevicesUnitsMapping.put("w_per_m2","w/m2");
        myDevicesUnitsMapping.put("uwpcm2","\u00B5W/cm2");
        myDevicesUnitsMapping.put("db","dB");
        myDevicesUnitsMapping.put("g","g");
        myDevicesUnitsMapping.put("mps2","m/s2");
        myDevicesUnitsMapping.put("mps","m/s");
        myDevicesUnitsMapping.put("kmh","km/h");
        myDevicesUnitsMapping.put("l","L");
        myDevicesUnitsMapping.put("gal","gal");
        myDevicesUnitsMapping.put("m3","m3");
        myDevicesUnitsMapping.put("af","ac ft");
        myDevicesUnitsMapping.put("ml","mL");
        myDevicesUnitsMapping.put("pl","% LEL");
        myDevicesUnitsMapping.put("dba","dBA");
        myDevicesUnitsMapping.put("uuid","Beacon");
        myDevicesUnitsMapping.put("degree","\u00B0");
        myDevicesUnitsMapping.put("mm/s","mm/s");
        myDevicesUnitsMapping.put("mgl","mg/L");
        myDevicesUnitsMapping.put("ppm","ppm");
        myDevicesUnitsMapping.put("mins","min(s)");
        myDevicesUnitsMapping.put("counter","People");
        myDevicesUnitsMapping.put("s","s");
        myDevicesUnitsMapping.put("hours","Hours(s)");
        myDevicesUnitsMapping.put("weeks","Week(s)");
        myDevicesUnitsMapping.put("months","Month(s)");
        myDevicesUnitsMapping.put("ms","Milisec(s)");
        myDevicesUnitsMapping.put("days","day(s)");
        myDevicesUnitsMapping.put("analog_sensor","PF");
        myDevicesUnitsMapping.put("m1","/m");
        myDevicesUnitsMapping.put("ppb","ppb");
        myDevicesUnitsMapping.put("mgpl","mg/L");
        myDevicesUnitsMapping.put("mg/m3","mg/m3");
        myDevicesUnitsMapping.put("μg/m3","\u00B5g/m3");
        myDevicesUnitsMapping.put("mgpkg","mg/kg");
        myDevicesUnitsMapping.put("m-1","/m");
        myDevicesUnitsMapping.put("uspcm","\u00B5S/cm");
    }

    public String getMyDevicesUnit(String key)
    {
    	if(key != null)
    	{
    		return  myDevicesUnitsMapping.get(key);
    	}
        return null;
    }
    
    public void updateMyDevicesCategoriesMap()
    {

        myDevicesCategoriesMapping.put("energy","energy");
        myDevicesCategoriesMapping.put("counter","count");
        myDevicesCategoriesMapping.put("current","current");
        myDevicesCategoriesMapping.put("pow","power");
        myDevicesCategoriesMapping.put("res","power");
        myDevicesCategoriesMapping.put("particulate_matter","chemical");
        myDevicesCategoriesMapping.put("prox","occupancy");
        myDevicesCategoriesMapping.put("rssi","generic");
        myDevicesCategoriesMapping.put("soil_ph","ph");
        myDevicesCategoriesMapping.put("bp","pressure");
        myDevicesCategoriesMapping.put("batt","battery");
        myDevicesCategoriesMapping.put("gyro","tilt");
        myDevicesCategoriesMapping.put("rel_hum","humidity");
        myDevicesCategoriesMapping.put("soil_w_ten","pressure");
        myDevicesCategoriesMapping.put("voltage","voltage");
        myDevicesCategoriesMapping.put("co","gas");
        myDevicesCategoriesMapping.put("lum","light");
        myDevicesCategoriesMapping.put("ext_wleak","leak");
        myDevicesCategoriesMapping.put("motion","motion");
        myDevicesCategoriesMapping.put("rain_level","height");
        myDevicesCategoriesMapping.put("low_battery","battery");
        myDevicesCategoriesMapping.put("co2","co2");
        myDevicesCategoriesMapping.put("soil_moist","humdity");
        myDevicesCategoriesMapping.put("trap","trap");
        myDevicesCategoriesMapping.put("analog_actuator","analog");
        myDevicesCategoriesMapping.put("analog_sensor","analog");
        myDevicesCategoriesMapping.put("temp","temperature");
        myDevicesCategoriesMapping.put("freq","frequency");
        myDevicesCategoriesMapping.put("gps","location");
        myDevicesCategoriesMapping.put("tl","height");
        myDevicesCategoriesMapping.put("snr","generic");
        myDevicesCategoriesMapping.put("accel","motion");
        myDevicesCategoriesMapping.put("digital_actuator","digital");
        myDevicesCategoriesMapping.put("LT100GPS","location");
        myDevicesCategoriesMapping.put("wind_speed","speed");
        myDevicesCategoriesMapping.put("parking","occupancy");
        myDevicesCategoriesMapping.put("digital_sensor","digital");
        myDevicesCategoriesMapping.put("waterleak","leak");
        myDevicesCategoriesMapping.put("intrusion","generic");
        myDevicesCategoriesMapping.put("vol","generic");
        myDevicesCategoriesMapping.put("percentage","generic");
        myDevicesCategoriesMapping.put("noise","sound");
        myDevicesCategoriesMapping.put("uuid","beacon");
        myDevicesCategoriesMapping.put("orient_roll","tilt");
        myDevicesCategoriesMapping.put("orient_pitch","tilt");
        myDevicesCategoriesMapping.put("waste_level","height");
        myDevicesCategoriesMapping.put("velocity","motion");
        myDevicesCategoriesMapping.put("open/closed","digital");
        myDevicesCategoriesMapping.put("wifistatus","digital");
        myDevicesCategoriesMapping.put("system","generic");
        myDevicesCategoriesMapping.put("acquisitionperiod","time");
        myDevicesCategoriesMapping.put("memory","generic");
        myDevicesCategoriesMapping.put("oxygen","o2");
        myDevicesCategoriesMapping.put("flow","flow");
        myDevicesCategoriesMapping.put("tamper","generic");
        myDevicesCategoriesMapping.put("pipe condition","generic");
        myDevicesCategoriesMapping.put("duration","time");
        myDevicesCategoriesMapping.put("flow condition","flow");
        myDevicesCategoriesMapping.put("signal condition","generic");
        myDevicesCategoriesMapping.put("leak","leak");
        myDevicesCategoriesMapping.put("in trip","motion");
        myDevicesCategoriesMapping.put("last fix","location");
        myDevicesCategoriesMapping.put("operating_mode","generic");
        myDevicesCategoriesMapping.put("time","time");
        myDevicesCategoriesMapping.put("cook_mode","generic");
        myDevicesCategoriesMapping.put("h2s","chemicals");
        myDevicesCategoriesMapping.put("probe_status","digital");
        myDevicesCategoriesMapping.put("nh3","chemicals");
        myDevicesCategoriesMapping.put("no2","gas");
        myDevicesCategoriesMapping.put("emergency","button");
        myDevicesCategoriesMapping.put("digital_input","digital");
        myDevicesCategoriesMapping.put("sos_alarm","alarm");
        myDevicesCategoriesMapping.put("dismiss_alarm","alarm");
        myDevicesCategoriesMapping.put("valve","generic");
        myDevicesCategoriesMapping.put("call","digital");
        myDevicesCategoriesMapping.put("kwh","energy");
        myDevicesCategoriesMapping.put("water_quality","generic");
        myDevicesCategoriesMapping.put("conc","gas");
        myDevicesCategoriesMapping.put("absorb","generic");
        myDevicesCategoriesMapping.put("wetness","humidity");
        myDevicesCategoriesMapping.put("airflow","flow");
        myDevicesCategoriesMapping.put("speed","speed");
        myDevicesCategoriesMapping.put("cond","conductivity");
        myDevicesCategoriesMapping.put("salinity","generic");
        myDevicesCategoriesMapping.put("turbidity","generic");
        myDevicesCategoriesMapping.put("acidity","ph");
        myDevicesCategoriesMapping.put("freezing flag","generic");
        myDevicesCategoriesMapping.put("water depth","height");
        myDevicesCategoriesMapping.put("dielectric permittivity","conductivity");
        myDevicesCategoriesMapping.put("volumetric water content","generic");
        myDevicesCategoriesMapping.put("radiation","light");
        myDevicesCategoriesMapping.put("precipitation","height");

    }

    public String getMyDevicesCategory(String key)
    {
        if(key != null)
        {
            return  myDevicesCategoriesMapping.get(key);
        }
        return null;
    }
    
    
}
