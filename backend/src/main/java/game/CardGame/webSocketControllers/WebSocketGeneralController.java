package game.CardGame.webSocketControllers;

import game.CardGame.dtos.GameCodeDto;
import game.CardGame.enums.ResponseType;
import game.CardGame.responseDtos.WebSocketErrorResponse;
import game.CardGame.responseDtos.WebSocketReconnectLobbyResponse;
import game.CardGame.responseDtos.WebSocketReconnectResult;
import game.CardGame.services.JwtService;
import game.CardGame.webSocketServices.WebSocketGeneralService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@AllArgsConstructor
public class WebSocketGeneralController {
    @Autowired
    private final SimpMessagingTemplate template;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private WebSocketGeneralService webSocketGeneralService;

    @MessageMapping("/reconnect")
    public void reconnectWebSocket(@Payload GameCodeDto gameCodeDto, SimpMessageHeaderAccessor headerAccessor) {
        String jwtToken;
        try {
            jwtToken = jwtService.verifyJwtForWebSocket(headerAccessor);
        }
        catch (Exception e) {
            WebSocketErrorResponse errorResponse = new WebSocketErrorResponse(ResponseType.ERROR_INVALID_JWT, "Authentication failed");
            template.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/private", errorResponse, headerAccessor.getMessageHeaders());
            return;
        }
        String username = jwtService.extractUsername(jwtToken);
        String gameCode = gameCodeDto.getGameCode();
        WebSocketReconnectResult response;
        try {
            response = webSocketGeneralService.reconnectWebSocket(username, gameCode, headerAccessor.getSessionId());
        }
        catch (IllegalArgumentException e) {
            WebSocketErrorResponse errorResponse = new WebSocketErrorResponse(ResponseType.ERROR_INVALID_ARGUMENT, e.getMessage());
            template.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/private", errorResponse, headerAccessor.getMessageHeaders());
            return;
        }
        catch (IllegalStateException e) {
            WebSocketErrorResponse errorResponse = new WebSocketErrorResponse(ResponseType.ERROR_INVALID_STATE, e.getMessage());
            template.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/private", errorResponse, headerAccessor.getMessageHeaders());
            return;
        }
        template.convertAndSendToUser(Objects.requireNonNull(headerAccessor.getSessionId()), "/queue/private", response, headerAccessor.getMessageHeaders());
    }
}
