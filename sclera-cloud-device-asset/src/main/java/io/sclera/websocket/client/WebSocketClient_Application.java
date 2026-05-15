package io.sclera.websocket.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** STUB: edge-only websocket client */
@Component
public class WebSocketClient_Application {
    private static final Logger log = LoggerFactory.getLogger(WebSocketClient_Application.class);

    public void connectP2PSocket() {
        log.warn("STUB: connectP2PSocket called");
    }

    public void connectIntegrationSocket() {
        log.warn("STUB: connectIntegrationSocket called");
    }
}
