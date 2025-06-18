package game.CardGame.webSocketServices;

import game.CardGame.dtos.JoinGameDto;
import game.CardGame.dtos.StartGameDto;
import game.CardGame.dtos.WebSocketResponseDto;
import game.CardGame.enums.ResponseType;
import game.CardGame.models.*;
import game.CardGame.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public WebSocketResponseDto createGame(String username, String displayName, String sessionId) throws IllegalStateException {
        if(displayName.equals("")) {
            displayName = username;
        }

        //Generiere neue Game-Session
        DeckModel deck = new DeckModel();
        deckRepository.save(deck);

        // Set WebSocketId of User
        Optional<UserModel> userOptional = userRepository.findByUsername(username);
        if(userOptional.isEmpty()) {
            throw new IllegalStateException("Invalid Username");
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
        new_Game.setGameStatus("InLobby");
        gameRepository.save(new_Game);

        // Add player to game
        player.setGameId(new_Game);
        playerRepository.save(player);

        return WebSocketResponseDto.builder()
                .id(game_code)
                .sender(displayName)
                .responseType(ResponseType.NEW_GAME)
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
        Set<PlayerModel> players = game.getPlayers();
        if(players.size() == 4) {
            throw new IllegalStateException("Game already has 4 players");
        }

        // Set WebSocketId of User
        Optional<UserModel> userOptional = userRepository.findByUsername(username);
        if(userOptional.isEmpty()) {
            throw new IllegalStateException("Invalid Username");
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

        List<String> playersOfGame = new ArrayList<>();
        for(PlayerModel otherPlayer : game.getPlayers()) {
            playersOfGame.add(otherPlayer.getDisplayName());
        }

        return WebSocketResponseDto.builder()
                .sender(joinGameDto.getDisplayName())
                .responseType(ResponseType.JOIN_GAME)
                .value(playersOfGame)
                .build();
    }


    public WebSocketResponseDto startGame(StartGameDto startGameDto, String username) throws IllegalArgumentException, IllegalStateException {
        Optional<GameModel> gameOptional = gameRepository.findByGameCode(startGameDto.getGameCode());
        if(gameOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        GameModel game = gameOptional.get();

        if(!game.getGameStatus().equals("InLobby")) {
            throw new IllegalStateException("Game has already started or finished");
        }

        PlayerModel callingPlayer = findPlayer(username, startGameDto.getGameCode());
        if(callingPlayer == null) {
            throw new IllegalStateException("User is not part of the game");
        }
        if(!game.getHostId().getId().equals(callingPlayer.getId())) {
            throw new IllegalStateException("User is not the host");
        }

        Set<PlayerModel> players = game.getPlayers();
        if(players.size() < 4) {
            throw new IllegalStateException("Game does not have 4 players");
        }

        game.setGameStatus("Started");
        gameRepository.save(game);
        setupStartingDeck(game.getCenterDeck());
        for(PlayerModel player : game.getPlayers()) {
            DeckModel handCards = player.getHandCards();
            for(int i = 0; i < 5; i++) {
                CardModel card = drawTopCard(game.getCenterDeck());
                card.setDeckId(handCards);
                card.setDeckPosition(i);
                cardRepository.save(card);
            }
        }

        return WebSocketResponseDto.builder()
                .sender(callingPlayer.getDisplayName())
                .responseType(ResponseType.START_GAME)
                .build();
    }


    private CardModel drawTopCard(DeckModel deck) {
        Optional<CardModel> cardOptional = cardRepository.findTopDeckPositionByDeckIdOrderByDeckPosition(deck);
        if(cardOptional.isEmpty()) {
            // TODO: Shuffle
            return null;
        }
        else {
            return cardOptional.get();
        }
    }


    private void setupStartingDeck(DeckModel deck) {
        String[] types = {"Rock", "Paper", "Scissors"};
        int maxNumber = 9;
        int numberOfSets = 2;
        List<Integer> range = new ArrayList<>(IntStream.range(1, maxNumber * types.length * numberOfSets + 1).boxed().toList());
        Collections.shuffle(range);
        for(int i = 0; i < numberOfSets; i++) {
            for (int j = 0; j < types.length; j++) {
                String type = types[j];
                for (int k = 1; k <= maxNumber; k++) {
                    CardModel card = new CardModel();
                    CardTypeModel cardType = cardTypeRepository.findByCardNameAndCardValueAndCardEvent(type, k, "NONE").get();
                    card.setCardType(cardType);
                    card.setDeckId(deck);
                    card.setDeckPosition(range.get(maxNumber * j + k - 1));
                    cardRepository.save(card);
                }
            }
        }
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


    public void broadcastWithPlayerHandCards(String game_code, WebSocketResponseDto action, MessageHeaders headers) throws IllegalArgumentException {
        Optional<GameModel> gameOptional = gameRepository.findByGameCode(game_code);
        if(gameOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        GameModel game = gameOptional.get();
        List<Object> values = new ArrayList<>();
        for (PlayerModel player : game.getPlayers()) {
            ArrayList<String> cardNames = new ArrayList<>();
            Set<CardModel> cards = player.getHandCards().getCardId();
            for(CardModel card : cards) {
                cardNames.add(card.getCardType().getCardName() + ";" + card.getCardType().getCardValue() + ";" + card.getCardType().getCardEvent());
            }
            values.add(cardNames);
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
        for (PlayerModel player : game.getPlayers()) {
            map.put("simpSessionId",player.getUserId().getWebSocketId().toString());
            map.put("simpDestination", "/user/" + player.getUserId().getWebSocketId().toString() + "/queue/private");
            MessageHeaders newHeaders = new MessageHeaders(map);
            if(values != null) {
                action.setValue(values.get(i));
            }
            template.convertAndSendToUser(player.getUserId().getWebSocketId(), "/queue/private", action, newHeaders);
            i++;
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
