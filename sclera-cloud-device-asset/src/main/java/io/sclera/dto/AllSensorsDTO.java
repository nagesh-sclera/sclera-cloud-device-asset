package io.sclera.dto;

import java.util.Set;

public class AllSensorsDTO {
	private Set<LorawanSensorDTO> lorawan_sensors;
	private Set<BacnetObjectDTO> bacnet_objects;
	private Set<DisruptiveSensorDTO> disruptive_sensors;
	private Set<DataHoistDTO> data_hoist_devices;
	private Set<MyDevicesSensorDTO> my_devices_sensors;
	private Set<MonnitSensorDTO> monnit_sensors;
	private Set<PelicanSensorDTO> pelican_sensors;
	private Set<KNXGroupDTO> knx_groups;
	private Set<SnmpObjectDTO> snmp_objects;
	private Set<DaintreeDeviceDTO> daintree_devices;
	private Set<EcobeeSensorDTO> ecobee_sensors;
	private Set<ModbusRegisterDTO> modbus_registers;


	private Set<PolyLensDeviceDTO> poly_lens_devices;


	private Set<MqttDeviceDTO> mqtt_devices;


	public Set<LorawanSensorDTO> getLorawan_sensors() {
		return lorawan_sensors;
	}


	public void setLorawan_sensors(Set<LorawanSensorDTO> lorawan_sensors) {
		this.lorawan_sensors = lorawan_sensors;
	}

	public Set<BacnetObjectDTO> getBacnet_objects() {
		return bacnet_objects;
	}

	public void setBacnet_objects(Set<BacnetObjectDTO> bacnet_objects) {
		this.bacnet_objects = bacnet_objects;
	}

	public Set<DisruptiveSensorDTO> getDisruptive_sensors() {
		return disruptive_sensors;
	}

	public void setDisruptive_sensors(Set<DisruptiveSensorDTO> disruptive_sensors) {
		this.disruptive_sensors = disruptive_sensors;
	}

	public Set<DataHoistDTO> getData_hoist_devices() {
		return data_hoist_devices;
	}

	public void setData_hoist_devices(Set<DataHoistDTO> data_hoist_devices) {
		this.data_hoist_devices = data_hoist_devices;
	}

	public Set<MyDevicesSensorDTO> getMy_devices_sensors() {
		return my_devices_sensors;
	}

	public void setMy_devices_sensors(Set<MyDevicesSensorDTO> my_devices_sensors) {
		this.my_devices_sensors = my_devices_sensors;
	}

	public Set<MonnitSensorDTO> getMonnit_sensors() {
		return monnit_sensors;
	}

	public void setMonnit_sensors(Set<MonnitSensorDTO> monnit_sensors) {
		this.monnit_sensors = monnit_sensors;
	}

	public Set<PelicanSensorDTO> getPelican_sensors() {
		return pelican_sensors;
	}

	public void setPelican_sensors(Set<PelicanSensorDTO> pelican_sensors) {
		this.pelican_sensors = pelican_sensors;
	}

	public Set<KNXGroupDTO> getKnx_groups() {
		return knx_groups;
	}

	public void setKnx_groups(Set<KNXGroupDTO> knx_groups) {
		this.knx_groups = knx_groups;
	}

	public Set<SnmpObjectDTO> getSnmp_objects() {
		return snmp_objects;
	}

	public void setSnmp_objects(Set<SnmpObjectDTO> snmp_objects) {
		this.snmp_objects = snmp_objects;
	}

	public Set<DaintreeDeviceDTO> getDaintree_devices() {
		return daintree_devices;
	}

	public void setDaintree_devices(Set<DaintreeDeviceDTO> daintree_devices) {
		this.daintree_devices = daintree_devices;
	}

	public Set<EcobeeSensorDTO> getEcobee_sensors() {
		return ecobee_sensors;
	}

	public void setEcobee_sensors(Set<EcobeeSensorDTO> ecobee_sensors) {
		this.ecobee_sensors = ecobee_sensors;
	}


	public Set<ModbusRegisterDTO> getModbus_registers() {
		return modbus_registers;
	}

	public void setModbus_registers(Set<ModbusRegisterDTO> modbus_registers) {
		this.modbus_registers = modbus_registers;
	}


	public Set<PolyLensDeviceDTO> getPoly_lens_devices() {
		return poly_lens_devices;
	}

	public void setPoly_lens_devices(Set<PolyLensDeviceDTO> poly_lens_devices) {
		this.poly_lens_devices = poly_lens_devices;
	}

	public Set<MqttDeviceDTO> getMqtt_devices() {
		return mqtt_devices;
	}

	public void setMqtt_devices(Set<MqttDeviceDTO> mqtt_devices) {
		this.mqtt_devices = mqtt_devices;
	}


	public AllSensorsDTO() {
	}

	@Override
	public String toString() {
		return "AllSensorsDTO{" +
				"lorawan_sensors=" + lorawan_sensors +
				", bacnet_objects=" + bacnet_objects +
				", disruptive_sensors=" + disruptive_sensors +
				", data_hoist_devices=" + data_hoist_devices +
				", my_devices_sensors=" + my_devices_sensors +
				", monnit_sensors=" + monnit_sensors +
				", pelican_sensors=" + pelican_sensors +
				", knx_groups=" + knx_groups +
				", snmp_objects=" + snmp_objects +
				", daintree_devices=" + daintree_devices +
				", ecobee_sensors=" + ecobee_sensors +
				'}';
	}
}
