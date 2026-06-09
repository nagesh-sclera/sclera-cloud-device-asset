package io.sclera.controller.admin;

import io.sclera.dto.ChatGPTDTO;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.service.ChatGPTService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * REST endpoint for AI-assisted asset troubleshooting.
 * Delegates to {@link ChatGPTService}, which streams the generated response back
 * to the caller via a {@link ResponseBodyEmitter}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class ChatGPTController {

    @Autowired
    ChatGPTService chatGPTService;

    /**
     * Generates a streamed troubleshooting message for the given asset/prompt.
     *
     * @param chatGPTDTO prompt and asset context for the AI request
     * @return streaming emitter that delivers the generated response
     * @throws JSONException if the request payload cannot be parsed
     */
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/troubleshoot-asset")
    public ResponseEntity<ResponseBodyEmitter> generateMessage(@RequestBody ChatGPTDTO chatGPTDTO) throws JSONException {
        return chatGPTService.generateMessage(chatGPTDTO);
    }
}

