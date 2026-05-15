package io.sclera.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Map;

@Component
@JsonInclude(JsonInclude.Include.NON_NULL)
@ConfigurationProperties(prefix = "sclera.chirpstackutils")
public class ChirpStackUtils {

	private String email;
	private String password;

	private String network_server_id;
	private String organization_id;
	private String application_id;
	private Boolean gateway_discovery_enabled;
	private Boolean sensor_is_disabled;
	private Integer network_server_limit;
	private Integer organization_limit;
	private Integer application_limit;
	private Integer gateway_profile_limit;
	private Integer service_profile_limit;
	private Integer device_profile_limit;
	private Integer device_count_limit;
	
	private String access_token;
	private String gateway_profile_id;
	private String service_profile_id;

	private String configuration_id;
	private String tenant_id;
	private String api_key;
	private Integer tenants_limit;
	private String server_url;
	private Boolean skipFcntCheck;

	private Integer stats_interval_time;
	private Map<String, String> sensor_device_profiles;

	
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getNetwork_server_id() {
		return network_server_id;
	}
	public void setNetwork_server_id(String network_server_id) {
		this.network_server_id = network_server_id;
	}
	public String getOrganization_id() {
		return organization_id;
	}
	public void setOrganization_id(String organization_id) {
		this.organization_id = organization_id;
	}
	public String getApplication_id() {
		return application_id;
	}
	public void setApplication_id(String application_id) {
		this.application_id = application_id;
	}
	public Boolean getGateway_discovery_enabled() {
		return gateway_discovery_enabled;
	}
	public void setGateway_discovery_enabled(Boolean gateway_discovery_enabled) {
		this.gateway_discovery_enabled = gateway_discovery_enabled;
	}
	public Boolean getSensor_is_disabled() {
		return sensor_is_disabled;
	}
	public void setSensor_is_disabled(Boolean sensor_is_disabled) {
		this.sensor_is_disabled = sensor_is_disabled;
	}
	public Integer getNetwork_server_limit() {
		return network_server_limit;
	}
	public void setNetwork_server_limit(Integer network_server_limit) {
		this.network_server_limit = network_server_limit;
	}
	public Integer getOrganization_limit() {
		return organization_limit;
	}
	public void setOrganization_limit(Integer organization_limit) {
		this.organization_limit = organization_limit;
	}
	public Integer getApplication_limit() {
		return application_limit;
	}
	public void setApplication_limit(Integer application_limit) {
		this.application_limit = application_limit;
	}
	public Integer getGateway_profile_limit() {
		return gateway_profile_limit;
	}
	public void setGateway_profile_limit(Integer gateway_profile_limit) {
		this.gateway_profile_limit = gateway_profile_limit;
	}
	public Integer getService_profile_limit() {
		return service_profile_limit;
	}
	public void setService_profile_limit(Integer service_profile_limit) {
		this.service_profile_limit = service_profile_limit;
	}
	public Integer getDevice_profile_limit() {
		return device_profile_limit;
	}
	public void setDevice_profile_limit(Integer device_profile_limit) {
		this.device_profile_limit = device_profile_limit;
	}
	public Integer getDevice_count_limit() {
		return device_count_limit;
	}
	public void setDevice_count_limit(Integer device_count_limit) {
		this.device_count_limit = device_count_limit;
	}
	public String getAccess_token() {
		return access_token;
	}
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	public String getGateway_profile_id() {
		return gateway_profile_id;
	}
	public void setGateway_profile_id(String gateway_profile_id) {
		this.gateway_profile_id = gateway_profile_id;
	}
	public String getService_profile_id() {
		return service_profile_id;
	}
	public void setService_profile_id(String service_profile_id) {
		this.service_profile_id = service_profile_id;
	}
	public Map<String, String> getSensor_device_profiles() {
		return sensor_device_profiles;
	}
	public void setSensor_device_profiles(Map<String, String> sensor_device_profiles) {
		this.sensor_device_profiles = sensor_device_profiles;
	}
	public String getTenant_id() {
		return tenant_id;
	}

	public void setTenant_id(String tenant_id) {
		this.tenant_id = tenant_id;
	}
	public Integer getStats_interval_time() {
		return stats_interval_time;
	}
	public void setStats_interval_time(Integer stats_interval_time) {
		this.stats_interval_time = stats_interval_time;
	}

	public String getApi_key() {
		return api_key;
	}
	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}

	public Integer getTenants_limit() {
		return tenants_limit;
	}

	public void setTenants_limit(Integer tenants_limit) {
		this.tenants_limit = tenants_limit;
	}

	public String getServer_url() {
		return server_url;
	}

	public void setServer_url(String server_url) {
		this.server_url = server_url;
	}

	public Boolean getSkipFcntCheck() {
		return skipFcntCheck;
	}

	public void setSkipFcntCheck(Boolean skipFcntCheck) {
		this.skipFcntCheck = skipFcntCheck;
	}

	public String getConfiguration_id() {
		return configuration_id;
	}

	public void setConfiguration_id(String configuration_id) {
		this.configuration_id = configuration_id;
	}

	//Convert Base 64 String to Bytes
	public byte[] base64StringToByteArray(String str){    
		   byte[] bt = null;    
		   try {    
		       Decoder decoder = Base64.getDecoder(); 
		       bt = decoder.decode(str);    
		   } catch (Exception e) {    
		       System.out.println(e);
		   }    
		   return bt;    
		   }   
	
	//Convert Bytes to Hexa string
	public String byteArrayToHexString(byte[] arr){
		StringBuilder sbd = new StringBuilder();
		for (byte b : arr) {
			String tmp = Integer.toHexString(0xFF & b);
			if (tmp.length() < 2)
				tmp = "0" + tmp;
			sbd.append(tmp);
		}
		return sbd.toString();
	}
	
	public String base64StringToHexString(String base64String)
	{
		byte[] byteArray = this.base64StringToByteArray(base64String);
		String hexString = this.byteArrayToHexString(byteArray);
		return hexString;
	}
	
	public BigInteger stringDateTimeToUTCTimestamp(String stringDateTime)
	{
        try{
            if(stringDateTime != null)
            {
                Instant instant = Instant.parse(stringDateTime);
                return BigInteger.valueOf(instant.toEpochMilli());
            }
            return null;
        }
        catch(Exception e){
            System.out.println("Error converting date time to timestamp " + e);
            return null;
        }
	}
}
