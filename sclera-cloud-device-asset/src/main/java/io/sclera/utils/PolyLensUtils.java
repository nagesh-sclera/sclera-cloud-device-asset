package io.sclera.utils;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.sclera.dto.PolyLensDeviceAttributesDTO;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Component
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolyLensUtils {

    private String access_token;

    private List<JSONObject> attributes = new ArrayList<>();

    public List<PolyLensDeviceAttributesDTO> generatePolyLensAttributes(String networkStatusValue, String firmwareValue, String audioValue, String ipNetworkValue, String cameraValue, String microphoneValue, String wifiValue, String callStatusValue, String activeApplication, String softwareVersion, String polyLensDeviceId) {
        List<PolyLensDeviceAttributesDTO> attributes = new ArrayList<>();
        attributes.add(createPolyLensAttribute("connected", "Network Status", networkStatusValue, "", "", polyLensDeviceId));
        attributes.add(createPolyLensAttribute("isFirmwareUptoDate", "Update Available", firmwareValue, "", "", polyLensDeviceId));
        attributes.add(createPolyLensAttribute("audio", "Audio", audioValue, "", "", polyLensDeviceId));
        attributes.add(createPolyLensAttribute("ipnetwork", "IP Network", ipNetworkValue, "", "", polyLensDeviceId));
        attributes.add(createPolyLensAttribute("camera", "Cameras", cameraValue, "", "", polyLensDeviceId));
        attributes.add(createPolyLensAttribute("microphones", "Microphones", microphoneValue, "", "", polyLensDeviceId));
        attributes.add(createPolyLensAttribute("wifi", "WiFi", wifiValue, "", "", polyLensDeviceId));
        attributes.add(createPolyLensAttribute("callStatus", "Call Status", callStatusValue, "", "", polyLensDeviceId));
        attributes.add(createPolyLensAttribute("activeApplicationName", "Provider", activeApplication, "", "", polyLensDeviceId));
        attributes.add(createPolyLensAttribute("softwareVersion", "Device Software", softwareVersion, "", "", polyLensDeviceId));
        return attributes;
    }


    public PolyLensDeviceAttributesDTO createPolyLensAttribute(String name, String displayName, String value, String unit, String userDataValue, String polyLensDeviceId) {
        return new PolyLensDeviceAttributesDTO(name, displayName, value, unit, userDataValue, BigInteger.valueOf(System.currentTimeMillis()), polyLensDeviceId);
    }


    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }


    public String generateDeviceQuery(String nextToken) {
        if (nextToken == null) {
            return "{\n" +
                    "    \"query\": \"query DeviceSearch($params: DeviceFindArgs) {\\n  deviceSearch(params: $params) {\\n        pageInfo {\\n      nextToken\\n      hasNextPage\\n      totalCount\\n    }\\n    edges {\\n      node {\\n        activeApplicationName\\n        callStatus\\n        connected\\n        connections {\\n          displayName\\n          id\\n          serialNumber\\n          softwareVersion\\n          macAddress\\n        }\\n        id\\n        externalIp\\n        internalIp\\n    macAddress\\n        location {\\n          coordinate {\\n            latitude\\n            longitude\\n          }\\n        }\\n        product {\\n          availableProductSoftware {\\n            id\\n            name\\n            version\\n            updatedAt\\n          }\\n          id\\n        }\\n        room {\\n          id\\n          name\\n        }\\n        serialNumber\\n     model {name}   site {\\n          address {\\n            address1\\n          }\\n          id\\n          name\\n        }\\n        softwareVersion\\n        systemStatus {\\n          data {\\n            com {\\n              poly {\\n                device {\\n                  status {\\n                    audio {\\n                      state\\n                    }\\n                    camera {\\n                      state\\n                    }\\n                    inacall {\\n                      state\\n                    }\\n                    microphones {\\n                      state\\n                    }\\n                    wifi {\\n                      state\\n                    }\\n                  }\\n                }\\n              }\\n            }\\n          }\\n        }\\n        displayName\\n      }\\n    }\\n  }\\n}\",\n" +
                    "    \"variables\": {\n" +
                    "        \"params\": {\n" +
                    "            \"pageSize\": null,\n" +
                    "            \"nextToken\": " + nextToken + "\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";
        } else {
            return "{\n" +
                    "    \"query\": \"query DeviceSearch($params: DeviceFindArgs) {\\n  deviceSearch(params: $params) {\\n        pageInfo {\\n      nextToken\\n      hasNextPage\\n      totalCount\\n    }\\n    edges {\\n      node {\\n        activeApplicationName\\n        callStatus\\n        connected\\n        connections {\\n          displayName\\n          id\\n          serialNumber\\n          softwareVersion\\n          macAddress\\n        }\\n        id\\n        externalIp\\n        internalIp\\n    macAddress\\n        location {\\n          coordinate {\\n            latitude\\n            longitude\\n          }\\n        }\\n        product {\\n          availableProductSoftware {\\n            id\\n            name\\n            version\\n            updatedAt\\n          }\\n          id\\n        }\\n        room {\\n          id\\n          name\\n        }\\n        serialNumber\\n       model {name} site {\\n          address {\\n            address1\\n          }\\n          id\\n          name\\n        }\\n        softwareVersion\\n        systemStatus {\\n          data {\\n            com {\\n              poly {\\n                device {\\n                  status {\\n                    audio {\\n                      state\\n                    }\\n                    camera {\\n                      state\\n                    }\\n                    inacall {\\n                      state\\n                    }\\n                    microphones {\\n                      state\\n                    }\\n                    wifi {\\n                      state\\n                    }\\n                  }\\n                }\\n              }\\n            }\\n          }\\n        }\\n        displayName\\n      }\\n    }\\n  }\\n}\",\n" +
                    "    \"variables\": {\n" +
                    "        \"params\": {\n" +
                    "            \"pageSize\": null,\n" +
                    "            \"nextToken\": \"" + nextToken + "\"\n" +
                    "        }\n" +
                    "    }\n" +
                    "}";
        }
    }

    public Boolean checkForValue(String value, String updatedValue) {
        if (value == null && updatedValue == null) {
            return false;
        } else if (value != null && updatedValue != null) {
            if (value.equals(updatedValue)) {
                return false;
            }
        }
        return true;
    }
}
