package io.sclera.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SocketUtils {

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;


    public <T> void invokeWebSocketEndpoint(String endpoint, T payload) {
        this.simpMessagingTemplate.convertAndSend(endpoint, payload);
        log.info("Triggered logs for , Endpoint {} , payload {}" ,endpoint ,payload);
    }


}
