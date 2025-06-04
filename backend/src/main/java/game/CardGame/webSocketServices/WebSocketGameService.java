package game.CardGame.webSocketServices;

import game.CardGame.dtos.GameStateDto;
import game.CardGame.enums.GameAction;
import game.CardGame.exceptions.UnknownUsernameException;
import game.CardGame.models.*;
import game.CardGame.repositories.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.mapping.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WebSocketGameService {
    //private final Map<String, GameSession> activeGames = new ConcurrentHashMap<>();
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

    public GameStateDto createGame(String playerName, String sessionId) throws UnknownUsernameException {
        //Generate Game-Key - 6 zeichen A-Z 0-9 Großbuchstaben
        String game_code = generateGameCode();

        //Generiere neue Game-Session
        CardTypeModel cardType = new CardTypeModel();
        cardType.setCardName("Test");
        cardTypeRepository.save(cardType);
        DeckModel deck = new DeckModel();
        deckRepository.save(deck);
        CardModel card = new CardModel();
        card.setCardType(cardType);
        card.setDeckId(deck);
        cardRepository.save(card);

        PlayerModel player = new PlayerModel();
        player.setWebSocketId(sessionId);
        Optional<UserModel> user = userRepository.findByUsername(playerName);
        if(user.isEmpty()) {
            throw new UnknownUsernameException();
        }
        player.setUserId(user.get());
        player.setDisplayName(playerName);      // TODO: Custom Display name
        player.setHandCards(deck);
        playerRepository.save(player);

        GameModel new_Game = new GameModel();
        new_Game.setGameCode(game_code);
        new_Game.setHostId(player);
        new_Game.setCurrentPlayerId(player);
        DeckModel centerDeck = new DeckModel();
        deckRepository.save(centerDeck);
        new_Game.setCenterDeck(centerDeck);
        new_Game.setGameStatus("TestStatus");
        gameRepository.save(new_Game);

        player.setGameId(new_Game);
        playerRepository.save(player);

        //Response für "Neues Spiel erstellt"
        return GameStateDto.builder()
                .id(game_code)
                .sender(playerName)
                .action(GameAction.NEW_GAME)
                .build();
    }

    public GameStateDto joinGame(String game_code, String playerName, SimpMessageHeaderAccessor headerAccessor) {
        Optional<GameModel> gameOptional = gameRepository.findByGameCode(game_code);
        if(gameOptional.isEmpty()){
            return GameStateDto.builder()
                    .id(game_code)
                    .action(GameAction.Invalid_action)
                    .value("Game-Key invalid")
                    .build();
        }
        GameModel game = gameOptional.get();
        PlayerModel player = playerRepository.findByUserId_Username(playerName).get();
        player.setWebSocketId(headerAccessor.getSessionId());
        playerRepository.save(player);
        game.getPlayers().add(player);
        gameRepository.save(game);

        return GameStateDto.builder()
                .id(game_code)
                .sender(playerName)
                .action(GameAction.JOIN_GAME)
                .build();
    }


    //Noch mehr Funktionen wie
    //StartGame
    //rematch
    //etc.


    public void broadcast(String game_code, GameStateDto action){
        GameModel game = gameRepository.findByGameCode(game_code).get();
        for (PlayerModel player : game.getPlayers()) {
            template.convertAndSendToUser(player.getWebSocketId(), "/queue/private", action);
        }
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
