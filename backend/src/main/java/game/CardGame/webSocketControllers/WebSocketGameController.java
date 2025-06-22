package game.CardGame.webSocketControllers;


import game.CardGame.dtos.*;
import game.CardGame.enums.ResponseType;
import game.CardGame.responseDtos.*;
import game.CardGame.services.JwtService;
import game.CardGame.webSocketServices.WebSocketGameService;
import game.CardGame.webSocketServices.WebSocketUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class WebSocketGameController {
    @Autowired
    private final SimpMessagingTemplate template;
    @Autowired
    private final WebSocketGameService webSocketGameService;
    @Autowired
    private final WebSocketUtilService webSocketUtilService;
    @Autowired
    private final JwtService jwtService;


    @MessageMapping("/game.new")
    public void newGame(@Payload CreateGameDto createGameDto, SimpMessageHeaderAccessor headerAccessor) {
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
        WebSocketCreateGameResponse webSocketResponse;
        try {
            webSocketResponse = webSocketGameService.createGame(username, createGameDto.getDisplayName(), headerAccessor.getSessionId());
        } catch (IllegalStateException e) {
            WebSocketErrorResponse errorResponse = new WebSocketErrorResponse(ResponseType.ERROR_INVALID_ARGUMENT, e.getMessage());
            template.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/private", errorResponse, headerAccessor.getMessageHeaders());
            return;
        }

        headerAccessor.getSessionAttributes().put("gameCode", webSocketResponse.getGameCode());
        headerAccessor.getSessionAttributes().put("userName", username);
        template.convertAndSendToUser(Objects.requireNonNull(headerAccessor.getSessionId()), "/queue/private", webSocketResponse, headerAccessor.getMessageHeaders());
    }


    @MessageMapping("/game.join")
    public void joinGame(@Payload JoinGameDto joinGameDto, SimpMessageHeaderAccessor headerAccessor) {
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
        WebSocketJoinGameResponse webSocketResponse;
        try {
            webSocketResponse = webSocketGameService.joinGame(joinGameDto, username, headerAccessor.getSessionId());
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

        webSocketUtilService.broadcast(joinGameDto.getGameCode(), webSocketResponse, headerAccessor.getMessageHeaders());
    }

    @MessageMapping("/game.join.anonymous")
    public void joinGameAnonymous(@Payload JoinGameDto joinGameDto, SimpMessageHeaderAccessor headerAccessor) {
        WebSocketJoinGameAnonymousResponse webSocketResponse;
        try {
            webSocketResponse = webSocketGameService.joinGameAnonymous(joinGameDto, headerAccessor.getSessionId());
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

        template.convertAndSendToUser(Objects.requireNonNull(headerAccessor.getSessionId()), "/queue/private", webSocketResponse, headerAccessor.getMessageHeaders());
        WebSocketJoinGameResponse webSocketJoinGameResponse = WebSocketJoinGameResponse.builder()
                .sender(webSocketResponse.getSender())
                .host(webSocketResponse.getHost())
                .responseType(webSocketResponse.getResponseType())
                .otherPlayers(webSocketResponse.getOtherPlayers())
                .build();
        webSocketUtilService.broadcastToOthers(joinGameDto.getGameCode(), webSocketJoinGameResponse, headerAccessor.getMessageHeaders());
    }


    @MessageMapping("/game.start")
    public void startGame(@Payload GameCodeDto gameCodeDto, SimpMessageHeaderAccessor headerAccessor) {
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
        WebSocketStartGameResponse webSocketResponse;
        try {
            webSocketResponse = webSocketGameService.startGame(gameCodeDto, username);
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

        webSocketUtilService.broadcastWithPlayerHandCards(gameCodeDto.getGameCode(), webSocketResponse, headerAccessor.getMessageHeaders());
    }


    @MessageMapping("/game.close")
    public void closeGame(@Payload GameCodeDto gameCodeDto, SimpMessageHeaderAccessor headerAccessor) {
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
        WebSocketCloseGameResponse webSocketResponse;
        try {
            webSocketResponse = webSocketGameService.closeGame(gameCodeDto, username);
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
        webSocketUtilService.broadcast(gameCodeDto.getGameCode(), webSocketResponse, headerAccessor.getMessageHeaders());
        try {
            webSocketGameService.deleteGame(gameCodeDto.getGameCode());
        }
        catch (Exception e) {
            e.printStackTrace();
            template.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/private", "Error deleting game: " + e.getMessage(), headerAccessor.getMessageHeaders());
        }
    }


    @MessageMapping("/game.restart")
    public void restartGame(@Payload GameCodeDto gameCodeDto, SimpMessageHeaderAccessor headerAccessor) {
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
        WebSocketStartGameResponse webSocketResponse;
        try {
            webSocketResponse = webSocketGameService.restartGame(gameCodeDto, username);
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
        webSocketUtilService.broadcastWithPlayerHandCards(gameCodeDto.getGameCode(), webSocketResponse, headerAccessor.getMessageHeaders());
    }
}
