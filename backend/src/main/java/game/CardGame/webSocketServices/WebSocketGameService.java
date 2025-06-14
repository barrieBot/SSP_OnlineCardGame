package game.CardGame.webSocketServices;

import game.CardGame.dtos.WebSocketResponseDto;
import game.CardGame.enums.GameAction;
import game.CardGame.exceptions.UnknownUsernameException;
import game.CardGame.models.*;
import game.CardGame.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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

    public WebSocketResponseDto createGame(String username, String displayName, String sessionId) throws UnknownUsernameException {
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
            throw new UnknownUsernameException();
        }
        UserModel user = userOptional.get();
        user.setWebSocketId(sessionId);

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

    public WebSocketResponseDto joinGame(String game_code, String playerName, SimpMessageHeaderAccessor headerAccessor) {
        Optional<GameModel> gameOptional = gameRepository.findByGameCode(game_code);
        /*if(gameOptional.isEmpty()){
            return GameStateDto.builder()
                    .id(game_code)
                    .action(GameAction.Invalid_action)
                    .value("Game-Key invalid")
                    .build();
        }*/
        if(gameOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        GameModel game = gameOptional.get();
        Optional<Set<PlayerModel>> playerSetOptional = playerRepository.findByUserId_Username(playerName);
        if(playerSetOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Username");
        }
        PlayerModel[] playersOfUser = playerSetOptional.get().toArray(new PlayerModel[0]);
        PlayerModel player = playersOfUser[0];
        //player.setWebSocketId(headerAccessor.getSessionId()); // TODO: Change to user
        player.setGameId(game);
        playerRepository.save(player);

        return WebSocketResponseDto.builder()
                .id(game_code)
                .sender(playerName)
                .action(GameAction.JOIN_GAME)
                .build();
    }


    //Noch mehr Funktionen wie
    //StartGame
    //rematch
    //etc.


    public void broadcast(String game_code, WebSocketResponseDto action){
        GameModel game = gameRepository.findByGameCode(game_code).get();
        for (PlayerModel player : game.getPlayers()) {
            //template.convertAndSendToUser(player.getWebSocketId(), "/queue/private", action); // TODO: Change to user
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
