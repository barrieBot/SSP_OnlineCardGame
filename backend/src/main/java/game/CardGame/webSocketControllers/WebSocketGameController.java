package game.CardGame.webSocketControllers;


import game.CardGame.dtos.CreateGameDto;
import game.CardGame.dtos.WebSocketResponseDto;
import game.CardGame.dtos.JoinGameDto;
import game.CardGame.enums.ResponseType;
import game.CardGame.services.JwtService;
import game.CardGame.webSocketServices.WebSocketGameService;
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
    private final JwtService jwtService;


    @MessageMapping("/game.new")
    public void newGame(@Payload CreateGameDto createGameDto, SimpMessageHeaderAccessor headerAccessor) {
        String jwtToken;
        try {
            jwtToken = jwtService.verifyJwtForWebSocket(headerAccessor);
        }
        catch (Exception e) {
            WebSocketResponseDto errorResponse = new WebSocketResponseDto(ResponseType.ERROR_INVALID_JWT, "Authentication failed");
            template.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/private", errorResponse, headerAccessor.getMessageHeaders());
            return;
        }
        String username = jwtService.extractUsername(jwtToken);
        WebSocketResponseDto webSocketResponse;
        try {
            webSocketResponse = webSocketGameService.createGame(username, createGameDto.getDisplayName(), headerAccessor.getSessionId());
        } catch (IllegalStateException e) {
            WebSocketResponseDto errorResponse = new WebSocketResponseDto(ResponseType.ERROR_INVALID_ARGUMENT, e.getMessage());
            template.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/private", errorResponse, headerAccessor.getMessageHeaders());
            return;
        }

        headerAccessor.getSessionAttributes().put("gameCode", webSocketResponse.getId());
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
            WebSocketResponseDto errorResponse = new WebSocketResponseDto(ResponseType.ERROR_INVALID_JWT, "Authentication failed");
            template.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/private", errorResponse, headerAccessor.getMessageHeaders());
            return;
        }
        String username = jwtService.extractUsername(jwtToken);
        WebSocketResponseDto webSocketResponse;
        try {
            webSocketResponse = webSocketGameService.joinGame(joinGameDto, username, headerAccessor.getSessionId());
        }
        catch (IllegalArgumentException e) {
            WebSocketResponseDto errorResponse = new WebSocketResponseDto(ResponseType.ERROR_INVALID_ARGUMENT, e.getMessage());
            template.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/private", errorResponse, headerAccessor.getMessageHeaders());
            return;
        }
        catch (IllegalStateException e) {
            WebSocketResponseDto errorResponse = new WebSocketResponseDto(ResponseType.ERROR_INVALID_STATE, e.getMessage());
            template.convertAndSendToUser(headerAccessor.getSessionId(), "/queue/private", errorResponse, headerAccessor.getMessageHeaders());
            return;
        }

        template.convertAndSendToUser(Objects.requireNonNull(headerAccessor.getSessionId()), "/queue/private", webSocketResponse, headerAccessor.getMessageHeaders());
        webSocketGameService.broadcast(joinGameDto.getGameCode(), webSocketResponse, headerAccessor.getMessageHeaders());
    }


    /*@MessageMapping("/game.config")
    public void gameConfig(@Payload WebSocketResponseDto game_request, SimpMessageHeaderAccessor headerAccessor) {

        //Jede Änderung am Spiel_Conf müssen an alle Spielerinnen weitergegeben werden
        //Pause, Start, Join, End, Next, etc.... hier
        // Disconnected? Weiterleitung an das vielleicht?
        //ID hat andere Bedeutung je nach Action

        String requestSN = game_request.getSender();
        String requestId =  game_request.getId();

        //Kann das überhaupt null sein?
        String sessionId = headerAccessor.getSessionId();
        if(sessionId == null) { return; }


        if(game_request.getId() == null || game_request.getId().length() != 6){
            throw new IllegalArgumentException("Game-Code invalid");
        }

        switch (game_request.getAction()){
            case NEW_GAME -> {

                //Erzeugt neues Spiel in in Game-Session mit Game-Code
                //Gibt Code an den Erzeuger zurück -> da er der einzige ist -> an alle

                //resp = gameManager.rematch(requestId, requestSN, sessionId)

            }

            case START_GAME -> {
                //Start Game event an alle
                //Start_turn event an einen der Spieler ->
                //Start Game Fehlgeschlagen an den Besitzer
            }

            case JOIN_GAME -> {
                WebSocketResponseDto join_response = webSocketGameService.joinGame(requestId, requestSN, headerAccessor);
                if(join_response.getAction() == GameAction.JOIN_GAME){
                    headerAccessor.getSessionAttributes().put("game_code", requestId);
                    headerAccessor.getSessionAttributes().put("username", requestSN);

                    //Send response oder so....
                    //broadcast an alle Spieler
                } else {
                    template.convertAndSendToUser(sessionId, "/queue/private", join_response);
                }
                //Neuer Spieler beigetreten an alle
                //Token für den Spieler an den Spieler ....
            }


            case LEAVE_GAME -> {


                //Spieler verlässt das Spiel
                //Event an alle Mitspieler
            }

        }

    }*/



    /*@MessageMapping("/game.playerAction")
    @SendTo("/topic/public")
    public WebSocketResponseDto placeCard(@Payload WebSocketResponseDto placed_card, SimpMessageHeaderAccessor headerAccessor) {

        String game_code = (String) headerAccessor.getSessionAttributes().get("game_code");

        if(game_code == null) {
            throw new IllegalArgumentException("Game-Code invalid");
        }

        switch (placed_card.getAction()) {
            case PLACE_CARD -> {
                //Place Card - event an alle
            }
            case DRAW_CARD -> {
                //Response and Player - welche Karte gezogen wurde
            }
            case CONCEDE_CARD -> {
                //Nächster ist dran
                //meistens zusammen mit Place Card ? -> Spezial-Karten
            }

        }

        WebSocketResponseDto placedCard = placed_card;
        return placedCard;

    }*/

}
