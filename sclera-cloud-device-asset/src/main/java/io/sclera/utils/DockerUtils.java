package io.sclera.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sclera.Repository.DockerRepository;
import io.sclera.Repository.VdmsconfigurationRepository;
import io.sclera.dto.touchscreen.settings.DockerDTO;
import io.sclera.dto.touchscreen.settings.NetworkConditionsResponseDTO;
import io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO;
import io.sclera.dto.touchscreen.settings.dockercli.ConnectorDTO;
import io.sclera.dto.touchscreen.settings.dockercli.ContainerDTO;
import io.sclera.dto.touchscreen.settings.dockercli.NetworkDTO;
import io.sclera.service.MasterSlaveAPICallService;
import io.sclera.service.DockerService;
import io.sclera.service.touchscreen.VdmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class DockerUtils {
    private static final Logger log = LoggerFactory.getLogger(DockerUtils.class);

    @Autowired
    private DockerService dockerService;

    @Autowired
    private NetworkUtils networkUtils;

    @Autowired
    private DockerRepository dockerRepository;

    @Autowired
    private VdmsconfigurationRepository vdmsconfigurationRepository;

    @Autowired
    private APIRequest apiRequest;

    @Autowired
    private VdmsService vdmsService;

    @Autowired
    private MasterSlaveAPICallService masterSlaveAPICallService;

    private final String dockerDaemonURL = "http://127.0.0.1:2375";

    private Boolean validateIP(String IP) {
        String zeroTo255
                = "(\\d{1,2}|(0|1)\\"
                + "d{2}|2[0-4]\\d|25[0-5])";

        String regex
                = zeroTo255 + "\\."
                + zeroTo255 + "\\."
                + zeroTo255 + "\\."
                + zeroTo255;

        Pattern p = Pattern.compile(regex);

        if (IP == null) {
            return false;
        }

        Matcher m = p.matcher(IP);

        return m.matches();
    }

    private Map<String, Object> validateIps(String externalIP, String gateway, String primaryDNS, String secondaryDNS) {
        Map<String, Object> validationResult = new HashMap<>();

        validationResult.put("isValid", true);
        validationResult.put("message", "Validation successful.");

        if (externalIP != null && !validateIP(externalIP)) {
            validationResult.put("isValid", false);
            validationResult.put("message", "Not a valid External IP address.");
        } else if (gateway != null && !validateIP(gateway)) {
            validationResult.put("isValid", false);
            validationResult.put("message", "Not a valid gateway.");
        } else if (primaryDNS != null && !validateIP(primaryDNS)) {
            validationResult.put("isValid", false);
            validationResult.put("message", "Not a valid primary DNS.");
        } else if (secondaryDNS != null && !validateIP(secondaryDNS)) {
            validationResult.put("isValid", false);
            validationResult.put("message", "Not a valid secondary DNS.");
        }

        return validationResult;
    }

    private Boolean isValidCidr(Integer cidr){
        if (cidr > 0 && cidr < 32) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean isValidVlanId(Integer vlanId){
        if (vlanId > 0 && vlanId < 4096) {
            return true;
        } else {
            return false;
        }
    }

//    public Boolean checkIfNetworkNameExist(List<DockerDTO> dockers, String name, Integer network_origin) {
//        log.info("Network id {}", name);
//        log.info("NETWORKS: {}", dockers);
//        return dockers.stream().anyMatch(d -> d.getName() != null && d.getName().equals(name) && d.getNetwork_origin() != null && d.getNetwork_origin().equals(network_origin));
//    }

    public Boolean checkIfNetworkNameExist(List<DockerDTO> dockers, String name) {
        log.info("Network id {}", name);
        log.info("NETWORKS: {}", dockers);
        return dockers.stream().anyMatch(d -> d.getName() != null && d.getName().equals(name));
    }

    private Boolean isSubnetPresent(String subnet, List<DockerDTO> dockers, String name, Integer network_origin) {
        log.info("Dockers is {} ", dockers);
        return dockers.stream()
                .anyMatch(d -> {
                    if (d.getName() != null && d.getNetwork_origin() != null && d.getName().equals(name) && d.getNetwork_origin().equals(network_origin)) {
                        return false;
                    }
                    if (d.getExternal_ip_address() != null && d.getCidr() != null && d.getNetwork_origin() != null) {
                        return networkUtils.generateSubnetFromIpAndCidr(d.getExternal_ip_address(), d.getCidr()).equals(subnet) && d.getNetwork_origin().equals(network_origin);
                    } else {
                        return false;
                    }
                });
    }

    private Boolean isVlanIdAlreadyTaggedToInterface(Integer vlanId, String interfaceId, List<DockerDTO> dockers, String name, Integer network_origin) {
        return dockers.stream().anyMatch(d -> {
            if (d.getName() != null && d.getNetwork_origin() != null && d.getName().equals(name) && d.getNetwork_origin().equals(network_origin)) {
                return false;
            }
            if (d.getInterface_out() != null && d.getVlan_id() != null && d.getNetwork_origin() != null && vlanId != null && network_origin != null) {
                return ((d.getInterface_out().equals(interfaceId) && d.getVlan_id().equals(vlanId) && d.getNetwork_origin().equals(network_origin)));
            } else {
                return false;
            }
        });
    }

    private Boolean checkIfMacVlanExist(String macVlan, List<DockerDTO> dockers, String name, Integer network_origin) {
        return dockers.stream().anyMatch(d -> {
            if (!d.getName().equals(name) && d.getNetwork_origin() != null && d.getNetwork_origin().equals(network_origin)) {
                return d.getMacvlan_name() != null && d.getMacvlan_name().equals(macVlan) && d.getNetwork_origin().equals(network_origin);
            }
            return false;
        });
    }

//  private boolean alreadyOneUntagged(String interfaceOut, List<DockerDTO> dockers, String name, Integer network_origin) {
//    return dockers.stream().anyMatch(d -> {
//      if (!d.getName().equals(name) && interfaceOut.equals(d.getInterface_out()) && d.getNetwork_origin().equals(network_origin)) {
//        return !d.getIs_tagged();
//      }
//      return false;
//    });
//  }

    private Boolean alreadyOneUntagged(String interfaceOut, List<DockerDTO> dockers, String name, Integer network_origin) {
        return dockers.stream().anyMatch(d -> {
            if (!d.getName().equals(name) && interfaceOut.equals(d.getInterface_out()) && d.getNetwork_origin().equals(network_origin)) {
                return !d.getIs_tagged() && d.getNetwork_origin().equals(network_origin);
            }
            return false;
        });
    }

    public String generateMacvlanName(String interface_name, Integer vlan_id) {
        if (interface_name != null && vlan_id != null) {
            return interface_name + "_" + vlan_id;
        } else {
            return interface_name;
        }
    }

    private NetworkConditionsResponseDTO validationHelper(String name, String externalIpAddress, String gateway, String primaryDNS,
                                                          String secondaryDNS, Boolean internetRequired, Integer cidr, Boolean isStatic, Boolean isTagged,
                                                          String interfaceOut, Boolean isHost, Integer vlanId, List<DockerDTO> networks, Integer networkOrigin) {

        Map<String, Object> validationRes = validateIps(externalIpAddress, gateway, primaryDNS, secondaryDNS);

        log.info("NETWORKS INSIDE VALIDATION HELPER {}", networks);

        // Checks if addresses are valid or not
        if (!Boolean.parseBoolean(validationRes.get("isValid").toString())) {
            return new NetworkConditionsResponseDTO(validationRes.get("message").toString(), HttpStatus.INTERNAL_SERVER_ERROR, false);
        }

        // Check if the user entered valid cidr
        if (cidr != null && !isValidCidr(cidr)) {
            return new NetworkConditionsResponseDTO("Not a valid CIDR", HttpStatus.INTERNAL_SERVER_ERROR, false);
        }

        // Check if the user entered valid vlan id
        if (isTagged != null && isTagged && vlanId != null && !isValidVlanId(vlanId)) {
            return new NetworkConditionsResponseDTO("Not a valid vlan ID ", HttpStatus.INTERNAL_SERVER_ERROR, false);
        }

        // Checks if network is tagged in case of DHCP
        if (isStatic != null && isTagged != null && !isStatic && isTagged) {
            log.error("DHCP cannot be tagged");
            return new NetworkConditionsResponseDTO("If DHCP is selected, network must be untagged", HttpStatus.INTERNAL_SERVER_ERROR, false);
        }

        // Checks if untagged network is already present in same interface
        if (isTagged != null && interfaceOut != null && !isTagged && alreadyOneUntagged(interfaceOut, networks, name, networkOrigin)) {
            log.error("Interface out {}", interfaceOut);
            log.error("Only one network can be untagged 1");
            return new NetworkConditionsResponseDTO("Only one network can be untagged for an interface", HttpStatus.INTERNAL_SERVER_ERROR, false);
        }

        // Checks if dhcp is enabled in guest mode (will be removed in future)
        if (isHost != null && isStatic != null && !isHost && !isStatic) {
            log.error("DHCP macvlan support coming soon");
            return new NetworkConditionsResponseDTO("DHCP support for Guest network coming soon", HttpStatus.INTERNAL_SERVER_ERROR, false);
        }

        // Checks if gateway and external ip is same
        if (gateway != null && externalIpAddress != null && !gateway.isBlank() && !externalIpAddress.isBlank() && externalIpAddress.equals(gateway)) {
            log.error("Gateway and External IP should not be the same");
            return new NetworkConditionsResponseDTO("Gateway and External IP should not be the same", HttpStatus.INTERNAL_SERVER_ERROR, false);
        }

        // Checks if gateway is provided in case of internet is enabled(only for guest)
        if (!isHost && (internetRequired != null) && (internetRequired && gateway == null)) {
            return new NetworkConditionsResponseDTO("Gateway must be provided if internet is enabled", HttpStatus.INTERNAL_SERVER_ERROR, false);
        }

        // Checks if gateway and ip are under same subnet
        if (externalIpAddress != null && gateway != null && cidr != null && !networkUtils.generateSubnetFromIpAndCidr(externalIpAddress, cidr)
                .equals(networkUtils.generateSubnetFromIpAndCidr(gateway, cidr))) {
            return new NetworkConditionsResponseDTO("IP Address and Gateway must be in the same subnet", HttpStatus.INTERNAL_SERVER_ERROR, false);
        }

        // Checks if macvlan name(Network name) already exist
        if (isTagged != null && isHost != null && isTagged && vlanId != null && !isHost) {
            if (checkIfMacVlanExist(generateMacvlanName(interfaceOut, vlanId), networks, name, networkOrigin)) {
                log.error("Provided mac vlan name exists");
                return new NetworkConditionsResponseDTO("Entered VLAN ID is already taken by the selected interface", HttpStatus.INTERNAL_SERVER_ERROR, false);
            }
        }

        // Checks if given subnet is already present
        if (externalIpAddress != null && cidr != null && isSubnetPresent(networkUtils.generateSubnetFromIpAndCidr(externalIpAddress, cidr), networks, name, networkOrigin)) {
            log.error("Provided subnet already exist");
            return new NetworkConditionsResponseDTO("Subnet of entered IP Address already exists", HttpStatus.INTERNAL_SERVER_ERROR, false);
        }

        // Checks if vlan id is already present under given interface
        if (vlanId != null && interfaceOut != null && isVlanIdAlreadyTaggedToInterface(vlanId, interfaceOut, networks, name, networkOrigin)) {
            log.error("Same vlan id cannot be present under same interface 1");
            return new NetworkConditionsResponseDTO("Entered VLAN ID is already taken by the selected interface", HttpStatus.INTERNAL_SERVER_ERROR, false);
        }

        // Checks if atleast one dns is provided
        if (isHost != null && !isHost && internetRequired != null && internetRequired && (primaryDNS == null && secondaryDNS == null)) {
            log.error("There should be atleast one dns when internet is enabled");
            return new NetworkConditionsResponseDTO("There should be atleast one dns when internet is enabled", HttpStatus.INTERNAL_SERVER_ERROR, false);
        }

        return new NetworkConditionsResponseDTO("success", HttpStatus.OK, true);
    }


    /**
     * Function to validate network boundary conditions while configuring sclera and creating containers
     *
     * @param externalIpAddress External ip address of the container/ip address of sclera device
     * @param gateway           Gateway of container/sclera device
     * @param primaryDNS        Primary dns of container/sclera device
     * @param secondaryDNS      Secondary dns of container/sclera device
     * @param internetRequired  Internet required field of container / true by default in sclera
     * @param cidr              Cidr field of container/sclera
     * @param isStatic          isStatic field of container/sclera
     * @param isTagged          isTagged field of container/sclera
     * @param interfaceOut      interfaceOut field of container/interface_id field of sclera
     * @param isHost            isHost field of container/true by default for sclera
     * @param vlanId            vlanId field of container/sclera
     * @param name              name field of container/ vdms-configuration-network by default for sclera
     * @return NetworkConditionsResponse object
     */
    public NetworkConditionsResponseDTO validateNetworkBoundaries(String name, String externalIpAddress, String gateway, String primaryDNS,
                                                                  String secondaryDNS, Boolean internetRequired, Integer cidr, Boolean isStatic, Boolean isTagged,
                                                                  String interfaceOut, Boolean isHost, Integer vlanId, Integer networkOrigin) {

        Integer isMaster = vdmsService.getIsMaster();

        List<DockerDTO> networks = new ArrayList<>();
        if (isMaster != null && isMaster == 0) {
            try {
                String response = masterSlaveAPICallService.accessMasterFromSlave("http://10.255.255.126:8888/network_origin/" + networkOrigin + "/getAllNetworksByNetworkOrigin", "GET", null, null);

                if (response != null) {
                    log.info("Response received from Master to Slave for validateNetworkBoundaries: /getAllNetworksByNetworkOrigin....: {}", response);
                    ObjectMapper objectMapper = new ObjectMapper();
                    networks = objectMapper.readValue(response, new TypeReference<>() {
                    });
                } else {
                    log.error("Null response received from Master to Slave while fetching networks.");
                }
            } catch (Exception e) {
                log.error("Exception while accessing Master from Slave during validateNetworkBoundaries for /getAllNetworksByNetworkOrigin: {}", e.getMessage(), e);
            }
        } else {
            networks = dockerRepository.getAllNetworksByNetworkOrigin(networkOrigin);
        }

        log.info("NETWORK'S INSIDE VALIDATE NETWORK BOUNDARIES FN {}", networks);

        //VDMS configuration does not check for host docker condition.
//    if (name != null && !name.equals("vdms-configuration-network")) {
//      DockerDTO dockerDTO = dockerService.checkIfHostNetworkPresent(networkOrigin);
//      System.out.println("Printing docker : " + dockerDTO.getName() + " " + dockerDTO.getHost() + " " + isHost + " " + networkOrigin);
//      if (dockerDTO.getName() != null && !dockerDTO.getName().equals(name) && dockerDTO.getHost() && isHost) {
//        if (dockerDTO.getHost()) {
//          logger.error("Only one Host network is possible.");
//          return new NetworkConditionsResponseDTO("Only one Host network is possible", HttpStatus.INTERNAL_SERVER_ERROR, false);
//        }
//      }
//    }

        // VDMS configuration does not check for host docker condition.
        // this condition will only be used during docker network creation and thus can only take place in Master
        if (name != null && !name.equals("vdms-configuration-network")) {
            DockerDTO existingHostNetwork = dockerService.checkIfHostNetworkPresentByNetworkOrigin(networkOrigin);
            log.info("Printing existingHostNetwork : {} \n Network Origin: {} ", existingHostNetwork, networkOrigin);

            if (existingHostNetwork != null && existingHostNetwork.getName() != null &&
                    !existingHostNetwork.getName().equals(name) && existingHostNetwork.getHost() && isHost) {
                log.error("Only one Host network is possible.");
                return new NetworkConditionsResponseDTO("Only one Host network is possible", HttpStatus.INTERNAL_SERVER_ERROR, false);
            }
        }

        // If host network, then skip checking with vdms configuration and with itself.
        if (isHost) {
            networks = networks.stream()
                    .filter(dockerDTO -> {
                        if (dockerDTO.getHost() != null) {
                            return !dockerDTO.getHost();
                        } else {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
        }

        // If container is not in host network check if current config details are clashing with vdms config details
        if (!isHost) {
            VdmsConfigurationDTO vdmsConfigurationDTO = null;
            NetworkConditionsResponseDTO validationResponse = null;

            if (networkOrigin == 1) {
                vdmsConfigurationDTO = vdmsconfigurationRepository.getConfiguration();
            } else {
                try {
                    String response = masterSlaveAPICallService.accessSlaveFromMaster("http://10.255.255.254:8888/getConfiguration", "GET", null, null);

                    if (response != null) {
                        log.info("Response received from Slave to Master for validateNetworkBoundaries: /getConfiguration....: {}", response);
                        ObjectMapper objectMapper = new ObjectMapper();
                        vdmsConfigurationDTO = objectMapper.readValue(response, new TypeReference<>() {
                        });
                    } else {
                        log.error("Null response received from Slave to Master while getting vdms Configuration details.");
                    }
                } catch (Exception e) {
                    log.error("Exception while accessing Slave from Master during validateNetworkBoundaries for /getConfiguration: {}", e.getMessage(), e);
                }
            }

//      if (vdmsConfigurationDTO.getTagged() != null && vdmsConfigurationDTO.getInterface_id() != null &&
//              interfaceOut.equals(vdmsConfigurationDTO.getInterface_id()) && !vdmsConfigurationDTO.getTagged() && !isTagged && !networkOrigin.equals(0)) {
//        logger.error("Only one network can be untagged 2.");
//        validationResponse = new NetworkConditionsResponseDTO("Only one network can be untagged for an interface", HttpStatus.INTERNAL_SERVER_ERROR, false);
//      }

            if (vdmsConfigurationDTO != null && vdmsConfigurationDTO.getInterface_id() != null && vdmsConfigurationDTO.getTagged() != null && vdmsConfigurationDTO.getVlan_id() != null &&
                    vdmsConfigurationDTO.getInterface_id().equals(interfaceOut) && (vdmsConfigurationDTO.getTagged() && isTagged && vdmsConfigurationDTO.getVlan_id().equals(vlanId))) {
                log.error("Same vlan id cannot be present under same interface 2.");
                validationResponse = new NetworkConditionsResponseDTO("Entered VLAN ID is already taken by the selected interface", HttpStatus.INTERNAL_SERVER_ERROR,
                        false);
            }

//            if (vdmsConfigurationDTO != null && vdmsConfigurationDTO.getPrivate_ip() != null && vdmsConfigurationDTO.getCidr() != null &&
//                    isStatic && networkUtils.generateSubnetFromIpAndCidr(vdmsConfigurationDTO.getPrivate_ip(), vdmsConfigurationDTO.getCidr()).equals(
//                    networkUtils.generateSubnetFromIpAndCidr(externalIpAddress, cidr)) && !networkOrigin.equals(0)) {
//                log.error("Provided subnet already exist.");
//                validationResponse = new NetworkConditionsResponseDTO("Subnet of entered IP Address already exists", HttpStatus.INTERNAL_SERVER_ERROR, false);
//            }

            if (vdmsConfigurationDTO != null && vdmsConfigurationDTO.getPrivate_ip() != null && vdmsConfigurationDTO.getCidr() != null &&
                    isStatic && networkUtils.generateSubnetFromIpAndCidr(vdmsConfigurationDTO.getPrivate_ip(), vdmsConfigurationDTO.getCidr()).equals(
                    networkUtils.generateSubnetFromIpAndCidr(externalIpAddress, cidr))) {
                log.error("Provided subnet already exist.");
                validationResponse = new NetworkConditionsResponseDTO("Subnet of entered IP Address already exists", HttpStatus.INTERNAL_SERVER_ERROR, false);
            }

            if (validationResponse != null && !validationResponse.getSuccess()) {
                return validationResponse;
            }
        }

        return validationHelper(name, externalIpAddress, gateway, primaryDNS,
                secondaryDNS, internetRequired, cidr, isStatic, isTagged,
                interfaceOut, isHost, vlanId, networks, networkOrigin);
    }


    // Docker API calls
    public NetworkConditionsResponseDTO createNetwork(ContainerDTO container, Integer network_origin) {
        log.info("Container DTO is : {}", container);
        log.info("Network origin is : {}", network_origin);

        Map<String, String> param = new HashMap<>();
        param.put("name", container.getContainerName());

        String response = null;

        if (network_origin == 0) {
            log.info("Inside Network Origin 0");
            response = apiRequest.dockerHttpRequest("http://10.255.255.254:2375" + "/containers/create", container.toString(), null, param, "POST");
            log.info("Container DTO : {}", container);
        } else {
            response = apiRequest.dockerHttpRequest(dockerDaemonURL + "/containers/create", container.toString(), null, param, "POST");
        }
        return buildResponse(response);
    }

    public NetworkConditionsResponseDTO start(String id, Integer network_origin) {
        String response = null;
        if (network_origin == 0) {
            response = apiRequest.dockerHttpRequest("http://10.255.255.254:2375" + "/containers/" + id + "/start", null, null, null, "POST");
        } else {
            response = apiRequest.dockerHttpRequest(dockerDaemonURL + "/containers/" + id + "/start", null, null, null, "POST");
        }
        return buildResponse(response);
    }

    public NetworkConditionsResponseDTO connect(ConnectorDTO connector, String networkName, Integer network_origin) {
        String response = null;
        if (network_origin == 0) {
            response = apiRequest.dockerHttpRequest("http://10.255.255.254:2375" + "/networks/" + networkName + "/connect", connector.toString(), null, null, "POST");
        } else {
            response = apiRequest.dockerHttpRequest(dockerDaemonURL + "/networks/" + networkName + "/connect", connector.toString(), null, null, "POST");
        }
        return buildResponse(response);
    }

    public NetworkConditionsResponseDTO createMacVlan(NetworkDTO network, Integer network_origin) {
        String response = null;
        if (network_origin == 0) {
            response = apiRequest.dockerHttpRequest("http://10.255.255.254:2375" + "/networks/create", network.toString(), null, null, "POST");
        } else {
            response = apiRequest.dockerHttpRequest(dockerDaemonURL + "/networks/create", network.toString(), null, null, "POST");
        }
        return buildResponse(response);
    }

    public NetworkConditionsResponseDTO deleteMacVlan(String id, Integer network_origin) {
        String response = null;
        if (network_origin == 0) {
            response = apiRequest.dockerHttpRequest("http://10.255.255.254:2375" + "/networks/" + id, null, null, null, "DELETE");
        } else {
            response = apiRequest.dockerHttpRequest(dockerDaemonURL + "/networks/" + id, null, null, null, "DELETE");
        }
        if (response.isEmpty()) {
            return new NetworkConditionsResponseDTO("MacVlan deleted successfully.", HttpStatus.NO_CONTENT, true);
        } else {
            return buildResponse(response);
        }
    }

    public NetworkConditionsResponseDTO deleteNetwork(String name, Integer network_origin) {
        String response = null;
        if (network_origin == 0) {
            response = apiRequest.dockerHttpRequest("http://10.255.255.254:2375" + "/containers/" + name + "?force=true", null, null, null, "DELETE");
        } else {
            response = apiRequest.dockerHttpRequest(dockerDaemonURL + "/containers/" + name + "?force=true", null, null, null, "DELETE");
            log.info("Response after deleting container : {}", response);
        }
        if (response.isEmpty()) {
            return new NetworkConditionsResponseDTO("Network deleted successfully.", HttpStatus.NO_CONTENT, true);
        } else {
            return buildResponse(response);
        }
    }

    public NetworkConditionsResponseDTO dockerKillNetwork(String name, Integer network_origin) {
        Map<String, String> body = new HashMap<>();
        body.put("signal", "KILL");

        String response = null;
        if (network_origin == 0) {
            response = apiRequest.dockerHttpRequest("http://10.255.255.254:2375" + "/containers/" + name + "/kill", body.toString(), null, null, "POST");
        } else {
            response = apiRequest.dockerHttpRequest(dockerDaemonURL + "/containers/" + name + "/kill", body.toString(), null, null, "POST");
        }
        if (response.isEmpty()) {
            log.info("Network killed successfully.");
            return new NetworkConditionsResponseDTO("Network killed successfully", HttpStatus.NO_CONTENT, true);
        } else {
            return buildResponse(response);
        }
    }

    public NetworkConditionsResponseDTO deleteNetworkAfterKilling(String name, Integer network_origin) {
        String response = null;
        if (network_origin == 0) {
            response = apiRequest.dockerHttpRequest("http://10.255.255.254:2375" + "/containers/" + name, null, null, null, "DELETE");
        } else {
            response = apiRequest.dockerHttpRequest(dockerDaemonURL + "/containers/" + name, null, null, null, "DELETE");
        }
        if (response.isEmpty()) {
            log.info("Network deleted successfully");
            return new NetworkConditionsResponseDTO("Network deleted successfully", HttpStatus.NO_CONTENT, true);
        } else {
            return buildResponse(response);
        }
    }

    public NetworkConditionsResponseDTO buildResponse(String result) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
            };
            HashMap<String, Object> response = mapper.readValue(result, typeRef);

            if (response == null) {
                return new NetworkConditionsResponseDTO("Successfull", HttpStatus.OK, true);
            }

            if (response.isEmpty()) {
                return new NetworkConditionsResponseDTO("Successfull", HttpStatus.OK, true);
            }

            if (response.get("Id") != null) {
                return new NetworkConditionsResponseDTO("Created Successfully", HttpStatus.OK, true);
            }

            if (response.get("message") != null) {
                log.error("error message : {}", response.get("message").toString());
                if (response.get("message").toString().contains("Conflict. The container name") && response.get("message").toString().contains("is already in use by container") && response.get("message")
                        .toString().contains("You have to remove (or rename) that container to be able to reuse that name.")) {
                    return new NetworkConditionsResponseDTO("Container with same name already exist", HttpStatus.INTERNAL_SERVER_ERROR, false);
                } else if (response.get("message").toString().contains("failed to allocate gateway") && response.get("message").toString().contains("Requested address is out of range")) {
                    return new NetworkConditionsResponseDTO("Subnet and Gateway cannot be different", HttpStatus.INTERNAL_SERVER_ERROR, false);
                } else if (response.get("message").toString().contains("Pool overlaps with other one on this address space")) {
                    return new NetworkConditionsResponseDTO("Subnet already exist", HttpStatus.INTERNAL_SERVER_ERROR, false);
                } else if (response.get("message").toString().contains("Address already in use")) {
                    return new NetworkConditionsResponseDTO("Tagged with same vlan ID", HttpStatus.INTERNAL_SERVER_ERROR, false);
                } else {
                    return new NetworkConditionsResponseDTO(response.get("message").toString(), HttpStatus.INTERNAL_SERVER_ERROR, false);
                }
            }

            if (response.get("Warnings") != null) {
                log.warn("Warnings : {}", response.get("Warnings").toString());
            }
        } catch (JsonProcessingException e) {
            log.error("Exception in parsing docker response : {}", e.getMessage());
        }

        return null;
    }
}