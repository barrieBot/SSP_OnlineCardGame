package game.CardGame.webSocketServices;

import game.CardGame.dtos.CardDto;
import game.CardGame.dtos.WebSocketResponseDto;
import game.CardGame.models.CardModel;
import game.CardGame.models.DeckModel;
import game.CardGame.models.GameModel;
import game.CardGame.models.PlayerModel;
import game.CardGame.repositories.CardRepository;
import game.CardGame.repositories.GameRepository;
import game.CardGame.repositories.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class WebSocketUtilService {
    private final SimpMessagingTemplate template;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private GameRepository gameRepository;

    public CardModel viewTopCard(DeckModel deck) {
        Optional<CardModel> cardOptional = cardRepository.findTopDeckPositionByDeckIdOrderByDeckPositionDesc(deck);
        if(cardOptional.isEmpty()) {
            return null;
        }
        else {
            return cardOptional.get();
        }
    }

    public PlayerModel findPlayer(String username, String gameCode) throws IllegalArgumentException {
        Optional<Set<PlayerModel>> playerSetOptional = playerRepository.findByUserId_Username(username);
        if(playerSetOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Username");
        }
        PlayerModel[] playersOfUser = playerSetOptional.get().toArray(new PlayerModel[0]);
        PlayerModel player = null;
        for(PlayerModel p : playersOfUser) {
            if(p.getGameId().getGameCode().equals(gameCode)) {
                player = p;
            }
        }
        return player;
    }


    public void broadcastWithPlayerHandCards(String game_code, WebSocketResponseDto action, MessageHeaders headers) throws IllegalArgumentException {
        Optional<GameModel> gameOptional = gameRepository.findByGameCode(game_code);
        if(gameOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        GameModel game = gameOptional.get();
        List<Object> values = new ArrayList<>();
        for (PlayerModel player : playerRepository.findByGameIdOrderByTurnIndicatorDesc(game).get()) {
            ArrayList<CardDto> cardDtos = new ArrayList<>();
            Set<CardModel> cards =  cardRepository.findByDeckId(player.getHandCards()).get();
            for(CardModel card : cards) {
                CardDto cardDto = new CardDto();
                cardDto.setCardName(card.getCardType().getCardName());
                cardDto.setCardValue(card.getCardType().getCardValue());
                cardDto.setCardEvent(card.getCardType().getCardEvent());
                cardDtos.add(cardDto);
            }
            values.add(cardDtos);
        }
        broadcast(game_code, action, headers, values);
    }


    public void broadcast(String game_code, WebSocketResponseDto action, MessageHeaders headers) throws IllegalArgumentException {
        broadcast(game_code, action, headers, null);
    }


    public void broadcast(String game_code, WebSocketResponseDto action, MessageHeaders headers, List<Object> values) throws IllegalArgumentException {
        Optional<GameModel> gameOptional = gameRepository.findByGameCode(game_code);
        if(gameOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        GameModel game = gameOptional.get();
        Map<String, Object> map = new HashMap<>();
        map.put("simpMessageType",headers.get("simpMessageType"));
        map.put("stompCommand",headers.get("stompCommand"));
        Map<String, Object> unmodifiableNativeHeaders = (Map<String, Object>) headers.get("nativeHeaders");
        Map<String, Object> nativeHeaders = new HashMap<>(unmodifiableNativeHeaders);
        nativeHeaders.remove("Authorization");
        map.put("nativeHeaders",nativeHeaders);
        map.put("simpSessionAttributes",headers.get("simpSessionAttributes"));
        map.put("simpHeartbeat",headers.get("simpHeartbeat"));
        map.put("lookupDestination",headers.get("lookupDestination"));
        map.put("contentType",headers.get("contentType"));
        int i = 0;
        for (PlayerModel player : playerRepository.findByGameIdOrderByTurnIndicatorDesc(game).get()) {
            map.put("simpSessionId",player.getWebSocketId().toString());
            map.put("simpDestination", "/user/" + player.getWebSocketId().toString() + "/queue/private");
            MessageHeaders newHeaders = new MessageHeaders(map);
            if(values != null) {
                action.setValue(values.get(i));
            }
            template.convertAndSendToUser(player.getWebSocketId(), "/queue/private", action, newHeaders);
            i++;
        }
    }
}
