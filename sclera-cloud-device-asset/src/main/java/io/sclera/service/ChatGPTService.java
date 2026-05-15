package io.sclera.service;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.MeasuringInstrumentDTO;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import io.sclera.dto.ChatGPTDTO;
import org.json.JSONObject;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.*;

@Service
public class ChatGPTService {

    @Autowired
    DeviceService deviceService;

    @Autowired
    MeasuringInstrumentService measuringInstrumentService;

    @Autowired
    APICallService apiCallService;

    @Autowired
    CorrigoService corrigoService;

    private static final Logger logger = LoggerFactory.getLogger(ChatGPTService.class);

    public ResponseEntity<ResponseBodyEmitter> generateMessage(ChatGPTDTO chatGPTDTO) throws JSONException {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(300000L);

        new Thread(() -> {
            try {
                if (chatGPTDTO.getUserName() == null || chatGPTDTO.getDeviceId() == null || chatGPTDTO.getOrganisation() == null ||
                        chatGPTDTO.getQueryType() == null) {
                    emitter.send("Request body invalid\n");
                    emitter.complete();
                    return;
                }

                JSONObject query = new JSONObject();
                StringBuilder finalQuestion = new StringBuilder();

                String descriptionQuestion = "Give me a detailed description of this asset.";
                String troubleshootQuestion = "Give me a troubleshooting guide for this asset.";
                String installationQuestion = "Give me a step by step guide to install this asset.";

                DeviceDTO device = null;
                device = deviceService.getDeviceByDeviceId(chatGPTDTO.getUserName(), chatGPTDTO.getVdmsId(), chatGPTDTO.getDockerId(), chatGPTDTO.getDeviceId());
                if (device == null) {
                    emitter.send("Asset not found\n");
                    emitter.complete();
                    return;
                }

                if (device.getUser_data_vendor() != null) {
                    query.put("manufacturer", device.getUser_data_vendor());
                    finalQuestion.append("Manufacturer: ").append(device.getUser_data_vendor()).append(",");
                } else if (device.getVendor() != null) {
                    query.put("manufacturer", device.getVendor());
                    finalQuestion.append("Manufacturer: ").append(device.getVendor()).append(",");
                }

                if (device.getUser_data_model() != null) {
                    query.put("model", device.getUser_data_model());
                    finalQuestion.append("Model: ").append(device.getUser_data_model()).append(",");
                } else if (device.getModel() != null) {
                    query.put("model", device.getModel());
                    finalQuestion.append("Model: ").append(device.getModel()).append(",");
                }

                // Check if asset has telemetry data
                Set<MeasuringInstrumentDTO> telemetryDataList = new HashSet<>();
                try {
                    telemetryDataList.addAll(measuringInstrumentService.getInstrumentByDeviceId(chatGPTDTO.getUserName(), chatGPTDTO.getVdmsId(), chatGPTDTO.getDeviceId()));
                } catch (Exception ignored) {
                }

                if (!telemetryDataList.isEmpty()) {
                    finalQuestion.append("This asset has the following attributes: ");
                    int count = 0;
                    for (MeasuringInstrumentDTO measuringInstrumentDTO : telemetryDataList) {

                        String name = (measuringInstrumentDTO.getName() != null && !measuringInstrumentDTO.getName().trim().isEmpty()) ? measuringInstrumentDTO.getName() : " ";
                        String value = (measuringInstrumentDTO.getValue() != null && !measuringInstrumentDTO.getValue().trim().isEmpty()) ? measuringInstrumentDTO.getValue() : " ";
                        String unit = (measuringInstrumentDTO.getUnit() != null && !measuringInstrumentDTO.getUnit().trim().isEmpty()) ? measuringInstrumentDTO.getUnit() : " ";

                        if (count > 0) {
                            finalQuestion.append(", ");
                        }

                        finalQuestion.append("Attribute Name: ").append(name).append(", ")
                                .append("Attribute value: ").append(value)
                                .append(unit);

                        count++;
                    }
                    finalQuestion.append(". ");
                }

                JSONArray jsonArray = corrigoService.getWorkordersByAssetIdForBot(device);
                if (jsonArray != null && jsonArray.length() != 0) {
                    finalQuestion.append("The following work orders are assigned to fix/inspect the asset: ");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Iterator keys = jsonObject.keys();

                        while (keys.hasNext()) {
                            String key = (String) keys.next();
                            String value = jsonObject.get(key).toString();
                            finalQuestion.append(key).append(": ").append(value).append("\n");
                        }
                    }
                }

                switch (chatGPTDTO.getQueryType()) {
                    // Description
                    case 1:
                        query.put("query", descriptionQuestion);
                        break;
                    // Troubleshooting
                    case 2:
                        query.put("query", troubleshootQuestion);
                        break;
                    // Custom query
                    case 3:
                        query.put("query", chatGPTDTO.getQuery());
                        break;
                    // Installation
                    case 4:
                        query.put("query", installationQuestion);
                        break;
                }

                query.put("context", finalQuestion.toString());

                query.put("chatHistory", new JSONArray(chatGPTDTO.getChatHistory()));
                logger.info("Request object for asset qa {}", query);

                apiCallService.generateChatbotMessage(query, emitter);
            } catch (Exception e) {
                try {
                    emitter.send("Error occurred: " + e.getMessage() + "\n");
                } catch (Exception ignored) {}
                emitter.complete();
            }
        }).start();

        return ResponseEntity.ok(emitter);
    }
}