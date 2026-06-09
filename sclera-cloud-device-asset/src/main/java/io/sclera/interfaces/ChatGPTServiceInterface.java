package io.sclera.interfaces;

import org.json.JSONException;
import org.springframework.http.ResponseEntity;
import io.sclera.dto.ChatGPTDTO;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/** Service contract for {@link io.sclera.service.ChatGPTService}. */
public interface ChatGPTServiceInterface {
    ResponseEntity<ResponseBodyEmitter> generateMessage(ChatGPTDTO chatGPTDTO) throws JSONException;
}
