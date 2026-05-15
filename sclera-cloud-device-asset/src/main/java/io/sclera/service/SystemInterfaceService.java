package io.sclera.service;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//import io.sclera.service.touchscreen.VdmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.sclera.Repository.SystemInterfaceRepository;
import io.sclera.dto.DockerInfoDto;
import io.sclera.dto.VlanDTO;
import io.sclera.sockets.SocketService;

@Service
public class SystemInterfaceService {
    private static final Logger log = LoggerFactory.getLogger(SystemInterfaceService.class);

	@Autowired
	SystemInterfaceRepository systemInterfaceRepository;

	@Autowired
	SocketService socketService;

	@Autowired
	DockerService dockerService;

//	@Autowired
//	VdmsService vdmsService;

	@Autowired
	MasterSlaveAPICallService masterSlaveAPICallService;


//	public void interfaceStatus(String interface_name, String interface_status) throws JsonProcessingException {
//		Integer isMaster = vdmsService.getIsMaster();
//		Integer network_origin = null;
//
//		if (isMaster == null || isMaster == 1) {
//			network_origin = 1; // Non Master-Slave or Master network
//		} else {
//			network_origin = 0; // Slave network
//		}
//		log.info("[Interface Status] isMaster: {}, network_origin: {}", isMaster, network_origin);
//
//		String oldInterfaceStatus = systemInterfaceRepository.getInterfaceStatus(interface_name);
//		log.info("Updating interface status for {}: {} -> {}", interface_name, oldInterfaceStatus, interface_status);
//		systemInterfaceRepository.upsertInterfaceStatus(interface_name, interface_status);
//
//		List<DockerInfoDto> interfaces = new ArrayList<>();
//		if (network_origin == 1) {
//			// MASTER
//			log.info("Fetching Docker interface status list for Master device...");
//			interfaces.addAll(dockerService.getDockerInterfaceList(network_origin));
//			log.info("Fetching VDMS Config interface status list for Master device...");
//			interfaces.addAll(dockerService.getVdmsConfigInterfaceList());
//		} else {
//			// SLAVE
//			log.info("Fetching Docker interface status list from Master for Slave device...");
//			String response = masterSlaveAPICallService.accessMasterFromSlave("http://10.255.255.126:8888/network_origin/" + network_origin + "/getDockerInterfaceList", "GET", null, null);
//			log.info("Response from Master: {}", response);
//
//			if (response != null) {
//				ObjectMapper objectMapper = new ObjectMapper();
//				List<DockerInfoDto> responseDTO = objectMapper.readValue(response, new TypeReference<>() {
//                });
//				log.info("Fetched Docker interface status list from Master: {}", responseDTO);
//				interfaces.addAll(responseDTO);
//			} else {
//				log.warn("No response received from Master device for Docker interface status list...");
//			}
//
//			log.info("Fetching VDMS Config interface status list for Slave device...");
//			interfaces.addAll(dockerService.getVdmsConfigInterfaceList());
//		}
//
//		// Remove duplicates based on interface_out and then convert back to a list
//		List<DockerInfoDto> usedInterfaces = new ArrayList<>(interfaces.stream()
//				.collect(Collectors.toMap(
//						DockerInfoDto::getInterface_out,
//						dto -> dto,
//						(existing, replacement) -> existing
//				))
//				.values());
//
//		log.info("usedInterfaces: {}", usedInterfaces);
//
//		boolean isInterfaceUsed = false;
//		for (DockerInfoDto usedInterface : usedInterfaces) {
//			if (usedInterface.getInterface_out() != null && usedInterface.getInterface_out().equals(interface_name)) {
//				log.info("Interface {} is used by a Docker container whose interface_out is {}.", interface_name, usedInterface.getInterface_out());
//				isInterfaceUsed = true;
//				break;
//			}
//		}
//
//		if (isInterfaceUsed) {
//			if (!Objects.equals(oldInterfaceStatus, interface_status)) {
//				if (network_origin == 1) {
//					log.info("Interface status changed for {}: {} -> {}. Notifying touchscreen with interface_origin: {}...", interface_name, oldInterfaceStatus, interface_status, network_origin);
//					socketService.socketDockerInterfaceStatus(interface_name, interface_status, network_origin);
//				} else {
//					Map<String, Object> body = new HashMap<>();
//					body.put("interface_out", interface_name);
//					body.put("interface_status", interface_status);
//					body.put("interface_origin", network_origin);
//					String dataInJSON = new ObjectMapper().writeValueAsString(body);
//
//					log.info("Interface status changed for {}: {} -> {}. Notifying Master's touchscreen from Slave with interface_origin: {}...", interface_name, oldInterfaceStatus, interface_status, network_origin);
//					String response = masterSlaveAPICallService.accessMasterFromSlave("http://10.255.255.126:8888/socketDockerInterfaceStatus", "POST", dataInJSON, null);
//					log.info("Response from Master after notifying touchscreen: {}", response);
//				}
//			}
//		} else {
//			log.info("Interface {} is not used by any Docker container. No notification sent to touchscreen.", interface_name);
//		}
//	}

	public void deleteAllInterface() {
		systemInterfaceRepository.deleteAllInterface();
	}

//	public List<DockerInfoDto> getInterfaceStatusList() {
//		Integer isMaster = vdmsService.getIsMaster();
//		Integer hasSecondaryDevice = vdmsService.getHasSecondaryDevice();
//		String secondaryDeviceId = vdmsService.getSecondaryDeviceId();
//		Integer interfaceOrigin;
//
//		if (isMaster == null || isMaster == 1) {
//			interfaceOrigin = 1; // Non Master-Slave or Master interface
//		} else {
//			interfaceOrigin = 0; // Slave interface
//		}
//
//		List<DockerInfoDto> interfaceStatusList = systemInterfaceRepository.getInterfaceStatusList();
//		List<DockerInfoDto> interface_status_list = new ArrayList<>();
//
//		if (hasSecondaryDevice != null && secondaryDeviceId != null && hasSecondaryDevice == 0) {
//			// this is mainly needed when the sclera host configuration is updated on master
//			// during which any api to the slave will fail until the slave gateway is reachable again from master
//			log.info("Checking if Slave device is reachable before fetching interface status list...");
//			boolean isSlaveReachable = waitForSlaveGatewayReachable();
//			if (!isSlaveReachable) {
//				log.error("Slave device is not reachable after network changes!");
//				return Collections.emptyList();
//			}
//
//			log.info("Fetching active interface status list from Slave to Master...");
//			String response = masterSlaveAPICallService.accessSlaveFromMaster("http://10.255.255.254:8888/getactiveinterfacelist", "GET", null, null);
//
//			if (response != null) {
//				List<DockerInfoDto> interfaceStatusListFromSlave = JSON.parseArray(response, DockerInfoDto.class);
//				log.info("Fetched interface status list from Slave: {}", interfaceStatusListFromSlave);
//				interface_status_list.addAll(interfaceStatusListFromSlave);
//			} else {
//				log.warn("No response received from Slave device for interface status list...");
//			}
//		}
//
//		for (DockerInfoDto interfaceStatus : interfaceStatusList) {
//			interfaceStatus.setInterface_origin(interfaceOrigin);
//			interface_status_list.add(interfaceStatus);
//		}
//
//		return interface_status_list;
//	}

	public VlanDTO getVlanDiscoverPidByInterfaceName(String interface_name) {
		return systemInterfaceRepository.getVlanDiscoverPidByInterfaceName(interface_name);
	}

	public void updateVlanDiscoverPidByInterfaceName(String pid, BigInteger timestamp, String interface_name) {
		systemInterfaceRepository.updateVlanDiscoverPidByInterfaceName(pid, timestamp, interface_name);
	}

	private boolean waitForSlaveGatewayReachable() {
		long start = System.currentTimeMillis();

		// Try to ping the slave gateway every 2 seconds to check if it's reachable for up to 2 minutes
		while ((System.currentTimeMillis() - start) < 120 * 1000) {
			try {
				Process process = new ProcessBuilder("ping", "-c", "1", "10.255.255.254").start();
				int exitCode = process.waitFor();
				log.info("Slave gateway ping exit code: {}", exitCode);
				if (exitCode == 0) {
					log.info("****** PING SUCCESSFUL ******");
					return true;
				}
				log.info("****** PING FAILED ******");
			} catch (Exception ignored) {}

			try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
		}
		return false;
	}

}
