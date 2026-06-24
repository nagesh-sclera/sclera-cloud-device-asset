package io.sclera.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.amazonaws.HttpMethod;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.ResponseDTO;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.util.List;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Service
public class FloorService {

    @Autowired
    private WebClientService webClientService;

    @Autowired
    private ResourceUrlConfig resourceUrlConfig;

    @Autowired
    private AwsService awsService;

    @Value("${run-pod.api-key}")
    private String runPodAPIKey;


    public ResponseEntity<ResponseDTO> getRegionCoordinates(String orgId, String email, String floorId, String floorMapUrl,
                                                            HttpServletRequest httpServletRequest) throws MalformedURLException, InterruptedException {

        log.info("ORG-ID : {} , Email : {}  , Floor-id : {} , FloorMapURL : {}", orgId, email, floorId, floorMapUrl);

        String authorizationHeader = httpServletRequest.getHeader(AUTHORIZATION);
        String accessToken = authorizationHeader.substring("Bearer ".length());

        String imagePath = awsService.getFilePathByImageUrl(floorMapUrl, httpServletRequest);
        String preSignedUrl = awsService.getPreSignedUrlForFileUpload(imagePath, 30, HttpMethod.GET);
        String extension = awsService.getFileExtensionByImageUrl(floorMapUrl, httpServletRequest);

        JSONObject runPodPayload = new JSONObject();

        JSONObject input = new JSONObject();
        input.put("floorId", floorId);
        input.put("accessToken", accessToken);
        input.put("image_link", preSignedUrl);
        input.put("image_type", extension);

        runPodPayload.put("input", input);

        log.info("RUN-POD PAYLOAD : {}", runPodPayload);

        ResponseEntity<JSONObject> runPodResponse = webClientService.getRegionCoordinatesByFloorMap(runPodPayload, runPodAPIKey, httpServletRequest);

        JSONObject runPodResponseObject = runPodResponse.getBody();

        log.info("RUN-POD Response Code : {}", runPodResponse.getStatusCode().value());

        if (runPodResponseObject != null && runPodResponseObject.containsKey("output")) {
            List<JSONObject> output = JSONArray.parseArray(runPodResponseObject.getString("output"), JSONObject.class);
            log.info("RUN-POD Response Object Count : {}", output.size());
            ResponseDTO responseDTO = ScleraUtils.generatePayload(output, 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else if (runPodResponseObject != null && runPodResponseObject.containsKey("status")) {
            log.info("RUN-POD Response : {}", runPodResponse);
            String requestId = runPodResponseObject.getString("id");
            List<JSONObject> output = this.checkRunPodRequestStatus(0, runPodAPIKey, requestId, httpServletRequest);
            if (output != null) {
                log.info("RUN-POD Response Object Count : {}", output.size());
                ResponseDTO responseDTO = ScleraUtils.generatePayload(output, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("RUN-POD Process Still in Progress...");
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        }
    }


    private List<JSONObject> checkRunPodRequestStatus(int count, String runPodAPIKey, String requestId, HttpServletRequest httpServletRequest) throws InterruptedException {
        if (count < 5) {
            ResponseEntity<JSONObject> runPodResponse = webClientService.checkRunPodRequestStatus(runPodAPIKey, requestId, httpServletRequest);
            count++;
            JSONObject runPodResponseObject = runPodResponse.getBody();

            if (runPodResponseObject != null && runPodResponseObject.containsKey("output")) {
                List<JSONObject> output = JSONArray.parseArray(runPodResponseObject.getString("output"), JSONObject.class);
                log.info("RUN-POD Response Object Count : {}", output.size());
                return output;
            } else if (runPodResponseObject != null && runPodResponseObject.containsKey("status")) {
                log.info("RUN-POD Object Response : {}", runPodResponseObject);
                String status = runPodResponseObject.getString("status");
                if (status.equalsIgnoreCase("IN_QUEUE") ||
                        status.equalsIgnoreCase("IN_PROGRESS")) {
                    Thread.sleep(10000);
                    log.info("RUN-POD API call re-initiated ...");
                    log.info("API Call count {}", count);
                    return checkRunPodRequestStatus(count, runPodAPIKey, requestId, httpServletRequest);
                } else {
                    log.error("Error Occurred!");
                    log.error("RunPod Response {}", runPodResponse);
                    return null;
                }
            } else {
                log.error("Error Occurred!");
                log.error("RunPod Response {}", runPodResponse);
                return null;
            }
        } else {
            log.error("MAX API call reached!");
            return null;
        }
    }

}

