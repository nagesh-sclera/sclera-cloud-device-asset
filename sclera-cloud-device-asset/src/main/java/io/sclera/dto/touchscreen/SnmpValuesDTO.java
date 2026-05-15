package io.sclera.dto.touchscreen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SnmpValuesDTO {

	private String id;
	private String oid;
	private String value;
	private String community_string;
	private String version;
	private String ip_address;

	private String snmp_parent;
	private String mac_address;
	private Integer port;
	private Integer security_level;
	private String username;
	private String auth_passphrase;
	private String priv_passphrase;
	private String snmp_parent_index;
	private Map<String,String> snmp_child;
	private Map<String,String> connected_mac_address;

	private String device_id;

	private Map<String,String> basePortIfIndex;

	private String auth_type;
	private String priv_type;

	List<io.sclera.dto.InterfaceDTO> interfaces;
	private Map<String,String> basePortIfStatus;
	private String checked;
	private String device_type;
	private String snmp_parent_port;
	private String oid_key;
	private String oid_value;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getCommunity_string() {
		return community_string;
	}
	public void setCommunity_string(String community_string) {
		this.community_string = community_string;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getIp_address() {
		return ip_address;
	}
	public void setIp_address(String ip_address) {
		this.ip_address = ip_address;
	}
	public String getSnmp_parent() {
		return snmp_parent;
	}
	public void setSnmp_parent(String snmp_parent) {
		this.snmp_parent = snmp_parent;
	}
	public String getMac_address() {
		return mac_address;
	}
	public void setMac_address(String mac_address) {
		this.mac_address = mac_address;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public Integer getSecurity_level() {
		return security_level;
	}
	public void setSecurity_level(Integer security_level) {
		this.security_level = security_level;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getAuth_passphrase() {
		return auth_passphrase;
	}
	public void setAuth_passphrase(String auth_passphrase) {
		this.auth_passphrase = auth_passphrase;
	}
	public String getPriv_passphrase() {
		return priv_passphrase;
	}
	public void setPriv_passphrase(String priv_passphrase) {
		this.priv_passphrase = priv_passphrase;
	}
	public String getSnmp_parent_index() {
		return snmp_parent_index;
	}
	public void setSnmp_parent_index(String snmp_parent_index) {
		this.snmp_parent_index = snmp_parent_index;
	}
	public Map<String, String> getSnmp_child() {
		return snmp_child;
	}
	public void setSnmp_child(Map<String, String> snmp_child) {
		this.snmp_child = snmp_child;
	}
	public Map<String, String> getConnected_mac_address() {
		return connected_mac_address;
	}
	public void setConnected_mac_address(Map<String, String> connected_mac_address) {
		this.connected_mac_address = connected_mac_address;
	}
	public String getDevice_id() {
		return device_id;
	}
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}

	public Map<String, String> getBasePortIfIndex() {
		return basePortIfIndex;
	}
	public void setBasePortIfIndex(Map<String, String> basePortIfIndex) {
		this.basePortIfIndex = basePortIfIndex;
	}
	public String getAuth_type() {
		return auth_type;
	}
	public void setAuth_type(String auth_type) {
		this.auth_type = auth_type;
	}
	public String getPriv_type() {
		return priv_type;
	}
	public void setPriv_type(String priv_type) {
		this.priv_type = priv_type;
	}
	public List<io.sclera.dto.InterfaceDTO> getInterfaces() {
		return interfaces;
	}
	public void setInterfaces(List<io.sclera.dto.InterfaceDTO> interfaces) {
		this.interfaces = interfaces;
	}
	public Map<String, String> getBasePortIfStatus() {
		return basePortIfStatus;
	}
	public void setBasePortIfStatus(Map<String, String> basePortIfStatus) {
		this.basePortIfStatus = basePortIfStatus;
	}
	public String getChecked() {
		return checked;
	}
	public void setChecked(String checked) {
		this.checked = checked;
	}
	public String getDevice_type() {
		return device_type;
	}
	public void setDevice_type(String device_type) {
		this.device_type = device_type;
	}
	public String getSnmp_parent_port() {
		return snmp_parent_port;
	}
	public void setSnmp_parent_port(String snmp_parent_port) {
		this.snmp_parent_port = snmp_parent_port;
	}

	public String getOid_key() {
		return oid_key;
	}
	public void setOid_key(String oid_key) {
		this.oid_key = oid_key;
	}
	public String getOid_value() {
		return oid_value;
	}
	public void setOid_value(String oid_value) {
		this.oid_value = oid_value;
	}
	public SnmpValuesDTO() {
		super();
	}


	//getDeviceListSnmp
//	public SnmpValuesDTO(String id, String community_string, String version, String ip_address, String snmp_parent,
//			String mac_address, Integer port, Integer security_level, String username, String auth_passphrase,
//			String priv_passphrase) {
//		super();
//		this.id = id;
//		this.community_string = community_string;
//		this.version = version;
//		this.ip_address = ip_address;
//		this.snmp_parent = snmp_parent;
//		this.mac_address = mac_address;
//		this.port = port;
//		this.security_level = security_level;
//		this.username = username;
//		this.auth_passphrase = auth_passphrase;
//		this.priv_passphrase = priv_passphrase;
//	}
	
	public SnmpValuesDTO(String id, String community_string, String version, String ip_address, String snmp_parent,
			String mac_address, Integer port, Integer security_level, String username, String auth_passphrase,
			String priv_passphrase, String auth_type, String priv_type) {
		super();
		this.id = id;
		this.community_string = community_string;
		this.version = version;
		this.ip_address = ip_address;
		this.snmp_parent = snmp_parent;
		this.mac_address = mac_address;
		this.port = port;
		this.security_level = security_level;
		this.username = username;
		this.auth_passphrase = auth_passphrase;
		this.priv_passphrase = priv_passphrase;
		this.auth_type = auth_type;
		this.priv_type = priv_type;
	}


	public SnmpValuesDTO(String id,String version, String ip_address, String snmp_parent, String mac_address) {
		this.id = id;
		this.version=version;
		this.ip_address = ip_address;
		this.snmp_parent = snmp_parent;
		this.mac_address = mac_address;
	}

//	public SnmpValuesDTO(String id, String oid, String community_string, String version, String ip_address,
//			Integer port, Integer security_level, String username, String auth_passphrase, String priv_passphrase,
//			String device_id) {
//		super();
//		this.id = id;
//		this.oid = oid;
//		this.community_string = community_string;
//		this.version = version;
//		this.ip_address = ip_address;
//		this.port = port;
//		this.security_level = security_level;
//		this.username = username;
//		this.auth_passphrase = auth_passphrase;
//		this.priv_passphrase = priv_passphrase;
//		this.device_id = device_id;
//	}
	
	public SnmpValuesDTO(String id, String oid, String community_string, String version, String ip_address,
			Integer port, Integer security_level, String username, String auth_passphrase, String priv_passphrase,
			String device_id, String auth_type, String priv_type) {
		super();
		this.id = id;
		this.oid = oid;
		this.community_string = community_string;
		this.version = version;
		this.ip_address = ip_address;
		this.port = port;
		this.security_level = security_level;
		this.username = username;
		this.auth_passphrase = auth_passphrase;
		this.priv_passphrase = priv_passphrase;
		this.device_id = device_id;
		this.auth_type = auth_type;
		this.priv_type = priv_type;
	}

	//used for snmpvaluesconfigurationmapping
	public SnmpValuesDTO(String id, String community_string, String version, String ip_address, Integer port,
			Integer security_level, String username, String auth_passphrase, String priv_passphrase,
			String auth_type, String priv_type) {
		super();
		this.id = id;
		this.community_string = community_string;
		this.version = version;
		this.ip_address = ip_address;
		this.port = port;
		this.security_level = security_level;
		this.username = username;
		this.auth_passphrase = auth_passphrase;
		this.priv_passphrase = priv_passphrase;
		this.auth_type = auth_type;
		this.priv_type = priv_type;
	}

	public SnmpValuesDTO( String oid_key, String oid_value, String device_id, String mac_address) {
		super();
		this.mac_address = mac_address;
		this.device_id = device_id;
		this.oid_key = oid_key;
		this.oid_value = oid_value;
	}
	@Override
	public String toString() {
		return "SnmpValuesDTO [id=" + id + ", oid=" + oid + ", value=" + value + ", community_string="
				+ community_string + ", version=" + version + ", ip_address=" + ip_address + ", snmp_parent="
				+ snmp_parent + ", mac_address=" + mac_address + ", port=" + port + ", security_level=" + security_level
				+ ", username=" + username + ", auth_passphrase=" + auth_passphrase + ", priv_passphrase="
				+ priv_passphrase + ", snmp_parent_index=" + snmp_parent_index + ", snmp_child=" + snmp_child
				+ ", connected_mac_address=" + connected_mac_address + ", device_id=" + device_id + ", basePortIfIndex="
				+ basePortIfIndex + ", auth_type=" + auth_type + ", priv_type=" + priv_type + ", interfaces="
				+ interfaces + ", basePortIfStatus=" + basePortIfStatus + ", checked=" + checked + ", device_type="
				+ device_type + ", snmp_parent_port=" + snmp_parent_port + ", oid_key=" + oid_key + ", oid_value="
				+ oid_value + "]";
	}
	






}

