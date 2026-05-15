package io.sclera.dto.touchscreen;

public class RemoteAccessSessionDTO {

    private String id;
    private String device_id;
    private String network_name;
    private Integer public_port;
    private Integer private_port;
    private String ip_address;
    private String mac_address;
    private String timestamp;
    private Boolean is_host_network;
    private String display_name;
    private String user_data_name;
    private String image_url_1;
    private Boolean isAlive = false;
    
    private String email;
    private String type;

    public RemoteAccessSessionDTO(){};

    public RemoteAccessSessionDTO(String id, String device_id, String network_name, Integer public_port, Integer private_port, String ip_address,
                                  String mac_address, String timestamp, Boolean is_host_network, String type) {
        this.id = id;
        this.device_id = device_id;
        this.network_name = network_name;
        this.public_port = public_port;
        this.private_port = private_port;
        this.ip_address = ip_address;
        this.mac_address = mac_address;
        this.timestamp = timestamp;
        this.is_host_network = is_host_network;
        this.type = type;
    }
    
    public RemoteAccessSessionDTO(String id, String device_id, String network_name, Integer public_port,
			Integer private_port, String ip_address, String mac_address, String timestamp, Boolean is_host_network,
			String display_name, String user_data_name, String image_url_1, String type) {
		super();
		this.id = id;
		this.device_id = device_id;
		this.network_name = network_name;
		this.public_port = public_port;
		this.private_port = private_port;
		this.ip_address = ip_address;
		this.mac_address = mac_address;
		this.timestamp = timestamp;
		this.is_host_network = is_host_network;
		this.display_name = display_name;
		this.user_data_name = user_data_name;
		this.image_url_1 = image_url_1;
        this.type = type;

    }

    public RemoteAccessSessionDTO(String id, String device_id, String network_name, Integer public_port, Integer private_port,
                                  String ip_address, String mac_address, String timestamp, Boolean is_host_network , String email, String type) {
        this.id = id;
        this.device_id = device_id;
        this.network_name = network_name;
        this.public_port = public_port;
        this.private_port = private_port;
        this.ip_address = ip_address;
        this.mac_address = mac_address;
        this.timestamp = timestamp;
        this.is_host_network = is_host_network;
        this.email = email;
        this.type = type;
    }
    
	public Boolean getIs_host_network() {
        return is_host_network;
    }

    public void setIs_host_network(Boolean is_host_network) {
        this.is_host_network = is_host_network;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getNetwork_name() {
        return network_name;
    }

    public void setNetwork_name(String network_name) {
        this.network_name = network_name;
    }

    public Integer getPublic_port() {
        return public_port;
    }

    public void setPublic_port(Integer public_port) {
        this.public_port = public_port;
    }

    public Integer getPrivate_port() {
        return private_port;
    }

    public void setPrivate_port(Integer private_port) {
        this.private_port = private_port;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getMac_address() {
        return mac_address;
    }

    public void setMac_address(String mac_address) {
        this.mac_address = mac_address;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getDisplay_name() {
		return display_name;
	}

	public void setDisplay_name(String display_name) {
		this.display_name = display_name;
	}

	public String getUser_data_name() {
		return user_data_name;
	}

	public void setUser_data_name(String user_data_name) {
		this.user_data_name = user_data_name;
	}
	
	public String getImage_url_1() {
		return image_url_1;
	}

	public void setImage_url_1(String image_url_1) {
		this.image_url_1 = image_url_1;
	}
	

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	public Boolean getIsAlive() {
		return isAlive;
	}

	public void setIsAlive(Boolean isAlive) {
		this.isAlive = isAlive;
	}
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
	@Override
	public String toString() {
		return "RemoteAccessSessionDTO [id=" + id + ", device_id=" + device_id + ", network_name=" + network_name
				+ ", public_port=" + public_port + ", private_port=" + private_port + ", ip_address=" + ip_address
				+ ", mac_address=" + mac_address + ", timestamp=" + timestamp + ", is_host_network=" + is_host_network
				+ ", display_name=" + display_name + ", user_data_name=" + user_data_name + ", image_url_1="
				+ image_url_1 + ", isAlive=" + isAlive + ", email=" + email + "]";
	}

	

	

	
}
