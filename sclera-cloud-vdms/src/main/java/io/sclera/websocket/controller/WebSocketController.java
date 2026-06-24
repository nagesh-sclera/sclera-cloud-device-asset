package io.sclera.websocket.controller;

import io.sclera.model.P2PRemoteSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;

@Controller
@Slf4j
public class WebSocketController {

    @MessageMapping("/test")
    @SendToUser("/queue/greeting")
    public String greeting(Principal principal,String message){
        log.info(principal.getName());
        return "Hello "+ HtmlUtils.htmlEscape(message);
    }

    @MessageMapping("/start-session")
    @SendToUser("/queue/session")
    public P2PRemoteSession start(Principal principal, P2PRemoteSession p2premotesession){
        return p2premotesession;
    }

    @MessageMapping("/end-session")
    @SendToUser("/queue/session")
    public P2PRemoteSession end(Principal principal,P2PRemoteSession p2premotesession){
        return p2premotesession;
    }

}
