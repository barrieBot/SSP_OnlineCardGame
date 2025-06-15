package game.CardGame.webSocketServices;

import game.CardGame.dtos.JoinGameDto;
import game.CardGame.dtos.WebSocketResponseDto;
import game.CardGame.enums.GameAction;
import game.CardGame.models.*;
import game.CardGame.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WebSocketGameService {
    private final SimpMessagingTemplate template;

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeckRepository deckRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardTypeRepository cardTypeRepository;

    public WebSocketResponseDto createGame(String username, String displayName, String sessionId) throws IllegalArgumentException {
        if(displayName.equals("")) {
            displayName = username;
        }

        //Generiere neue Game-Session
        DeckModel deck = new DeckModel();
        deckRepository.save(deck);
        // TODO: Bei Spielstart Karten erstellen
        /*CardModel card = new CardModel();
        CardTypeModel cardType = cardTypeRepository.findByCardNameAndCardValueAndCardEvent("Rock", 1, "NONE").get();
        card.setCardType(cardType);
        card.setDeckId(deck);
        cardRepository.save(card);*/

        // Set WebSocketId of User
        Optional<UserModel> userOptional = userRepository.findByUsername(username);
        if(userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Username");
        }
        UserModel user = userOptional.get();
        user.setWebSocketId(sessionId);
        userRepository.save(user);

        // Create Player
        PlayerModel player = new PlayerModel();
        player.setUserId(user);
        player.setDisplayName(displayName);
        DeckModel handCards = new DeckModel();
        deckRepository.save(handCards);
        player.setHandCards(handCards);
        playerRepository.save(player);

        // Create Game
        GameModel new_Game = new GameModel();
        String game_code = generateUniqueGameCode();
        new_Game.setGameCode(game_code);
        new_Game.setHostId(player);
        new_Game.setCurrentPlayerId(player);
        DeckModel centerDeck = new DeckModel();
        deckRepository.save(centerDeck);
        new_Game.setCenterDeck(centerDeck);
        new_Game.setGameStatus("TestStatus");
        gameRepository.save(new_Game);

        // Add player to game
        player.setGameId(new_Game);
        playerRepository.save(player);

        return WebSocketResponseDto.builder()
                .id(game_code)
                .sender(displayName)
                .action(GameAction.NEW_GAME)
                .build();
    }

    public WebSocketResponseDto joinGame(JoinGameDto joinGameDto, String username, String sessionId) throws IllegalArgumentException, IllegalStateException{
        if(joinGameDto.getDisplayName().equals("")) {
            joinGameDto.setDisplayName(username);
        }

        Optional<GameModel> gameOptional = gameRepository.findByGameCode(joinGameDto.getGameCode());
        if(gameOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        GameModel game = gameOptional.get();

        // Set WebSocketId of User
        Optional<UserModel> userOptional = userRepository.findByUsername(username);
        if(userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Username");
        }
        UserModel user = userOptional.get();
        user.setWebSocketId(sessionId);
        userRepository.save(user);

        PlayerModel player = findPlayer(username, joinGameDto.getGameCode());
        if(player != null) {
            throw new IllegalStateException("User already joined the game");
        }

        // Create Player
        player = new PlayerModel();
        player.setUserId(user);
        player.setDisplayName(joinGameDto.getDisplayName());
        DeckModel handCards = new DeckModel();
        deckRepository.save(handCards);
        player.setHandCards(handCards);
        playerRepository.save(player);

        player.setGameId(game);
        playerRepository.save(player);

        return WebSocketResponseDto.builder()
                .sender(joinGameDto.getDisplayName())
                .action(GameAction.JOIN_GAME)
                .build();
    }


    private PlayerModel findPlayer(String username, String gameCode) throws IllegalArgumentException {
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


    public void broadcast(String game_code, WebSocketResponseDto action, MessageHeaders headers) throws IllegalArgumentException {
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
        for (PlayerModel player : game.getPlayers()) {
            map.put("simpSessionId",player.getUserId().getWebSocketId().toString());
            map.put("simpDestination", "/user/" + player.getUserId().getWebSocketId().toString() + "/queue/private");
            MessageHeaders newHeaders = new MessageHeaders(map);
            template.convertAndSendToUser(player.getUserId().getWebSocketId(), "/queue/private", action, newHeaders);
        }
    }


    private String generateUniqueGameCode() {
        //Generate unique Game-Key - 6 zeichen A-Z 0-9 Großbuchstaben
        String game_code = generateGameCode();
        while(gameRepository.findByGameCode(game_code).isPresent()) {
            game_code = generateGameCode();
        }
        return game_code;
    }


    private String generateGameCode(){
        String allowed_characters = "ABCDEFGHIJKLMNOPRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(6);
        for(int i = 0; i < 6; i++){
            sb.append(allowed_characters.charAt((int)(Math.random()*allowed_characters.length())));
        }
        return sb.toString();
    }
}
