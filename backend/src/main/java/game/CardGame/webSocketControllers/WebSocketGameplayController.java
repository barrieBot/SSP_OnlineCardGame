package game.CardGame.webSocketControllers;

import game.CardGame.dtos.GameCodeDto;
import game.CardGame.dtos.PlayCardDto;
import game.CardGame.responseDtos.*;
import game.CardGame.enums.ResponseType;
import game.CardGame.services.JwtService;
import game.CardGame.webSocketServices.WebSocketGameplayService;
import game.CardGame.webSocketServices.WebSocketUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketGameplayController {
    @Autowired
    private final SimpMessagingTemplate template;
    @Autowired
    private final WebSocketGameplayService webSocketGameplayService;
    @Autowired
    private final WebSocketUtilService webSocketUtilService;
    @Autowired
    private final JwtService jwtService;


    @MessageMapping("/game.card.play")
    public void playCard(@Payload PlayCardDto playCardDto, SimpMessageHeaderAccessor headerAccessor) {
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
        String gameToken = playCardDto.getGameCode();
        WebSocketPlayCardResult webSocketResponse;
        try {
            webSocketResponse = webSocketGameplayService.playCard(playCardDto, username, gameToken);
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

        webSocketUtilService.broadcast(gameToken, webSocketResponse, headerAccessor.getMessageHeaders());
    }


    @MessageMapping("/game.card.draw")
    public void drawCard(@Payload GameCodeDto gameCodeDto, SimpMessageHeaderAccessor headerAccessor) {
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
        WebSocketDrawCardIndividualResponse webSocketResponse;
        try {
            webSocketResponse = webSocketGameplayService.drawCard(gameCode, username);
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

        template.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/private", webSocketResponse, headerAccessor.getMessageHeaders());
        WebSocketDrawCardResponse webSocketDrawCardResponse = WebSocketDrawCardResponse.builder()
                .sender(webSocketResponse.getSender())
                .responseType(webSocketResponse.getResponseType())
                .drawCount(webSocketResponse.getDrawCount())
                .build();
        webSocketUtilService.broadcastToOthers(gameCode, webSocketDrawCardResponse, headerAccessor.getMessageHeaders());
    }
}
