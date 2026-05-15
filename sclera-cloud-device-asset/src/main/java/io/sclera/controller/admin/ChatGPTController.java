package io.sclera.controller.admin;

import io.sclera.dto.ChatGPTDTO;
import org.springframework.web.bind.annotation.RestController;
import io.sclera.service.ChatGPTService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChatGPTController {

    @Autowired
    ChatGPTService chatGPTService;

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/troubleshoot-asset")
    public ResponseEntity<ResponseBodyEmitter> generateMessage(@RequestBody ChatGPTDTO chatGPTDTO) throws JSONException {
        return chatGPTService.generateMessage(chatGPTDTO);
    }
}

