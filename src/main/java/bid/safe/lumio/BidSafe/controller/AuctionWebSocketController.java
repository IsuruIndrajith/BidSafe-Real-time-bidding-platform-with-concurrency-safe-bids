package bid.safe.lumio.BidSafe.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class AuctionWebSocketController {

    @MessageMapping("/test")
    @SendTo("/topic/test")
    public String test(String message) {
        return message;
    }
}