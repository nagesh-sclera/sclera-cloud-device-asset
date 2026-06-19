package io.sclera.controller.admin;

import io.sclera.dto.ChatGPTDTO;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.service.ChatGPTService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "ChatGPT", description = "AI-assisted asset troubleshooting; streams the generated response to the caller.")
public class ChatGPTController {

    private static final Logger log = LoggerFactory.getLogger(ChatGPTController.class);

    @Autowired
    ChatGPTService chatGPTService;

    /**
     * Generates a streamed troubleshooting message for the given asset/prompt.
     *
     * @param chatGPTDTO prompt and asset context for the AI request
     * @return streaming emitter that delivers the generated response
     * @throws JSONException if the request payload cannot be parsed
     */
    @Operation(summary = "Generate a troubleshooting message",
            description = "Generates a streamed AI troubleshooting message for the given asset and prompt. The response is delivered incrementally via a streaming emitter.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Streaming response started"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/troubleshoot-asset")
    public ResponseEntity<ResponseBodyEmitter> generateMessage(@RequestBody ChatGPTDTO chatGPTDTO) throws JSONException {
        log.info("generateMessage called");
        return chatGPTService.generateMessage(chatGPTDTO);
    }
}
