package game.CardGame.webSocketServices;

import game.CardGame.dtos.*;
import game.CardGame.enums.ResponseType;
import game.CardGame.models.*;
import game.CardGame.repositories.*;
import game.CardGame.responseDtos.*;
import game.CardGame.services.AuthenticationService;
import game.CardGame.services.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
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
    @Autowired
    private WebSocketUtilService webSocketUtilService;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    public WebSocketCreateGameResponse createGame(String username, String displayName, String sessionId) throws IllegalStateException {
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

        // Create Player
        PlayerModel player = new PlayerModel();
        player.setUserId(user);
        player.setDisplayName(displayName);
        player.setWebSocketId(sessionId);
        DeckModel handCards = new DeckModel();
        deckRepository.save(handCards);
        player.setHandCards(handCards);
        player.setTurnIndicator(1);
        playerRepository.save(player);

        // Create Game
        GameModel new_Game = new GameModel();
        String game_code = generateUniqueGameCode();
        new_Game.setGameCode(game_code);
        new_Game.setHostId(player);
        new_Game.setDrawCount(0);
        DeckModel centerDeck = new DeckModel();
        deckRepository.save(centerDeck);
        new_Game.setCenterDeck(centerDeck);
        DeckModel discardPile = new DeckModel();
        deckRepository.save(discardPile);
        new_Game.setDiscardPile(discardPile);
        new_Game.setGameStatus("InLobby");
        gameRepository.save(new_Game);

        // Add player to game
        player.setGameId(new_Game);
        playerRepository.save(player);

        return WebSocketCreateGameResponse.builder()
                .gameCode(game_code)
                .sender(displayName)
                .responseType(ResponseType.NEW_GAME)
                .build();
    }

    public WebSocketJoinGameResponse joinGame(JoinGameDto joinGameDto, String username, String sessionId) throws IllegalArgumentException, IllegalStateException{
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

        PlayerModel player = webSocketUtilService.findPlayer(username, joinGameDto.getGameCode());
        if(player != null) {
            throw new IllegalStateException("User already joined the game");
        }

        // Create Player
        player = new PlayerModel();
        player.setUserId(user);
        player.setDisplayName(joinGameDto.getDisplayName());
        player.setWebSocketId(sessionId);
        DeckModel handCards = new DeckModel();
        deckRepository.save(handCards);
        player.setHandCards(handCards);
        // Find highest turnIndicator
        int hightestIndicator = 0;
        for(PlayerModel otherPlayer : game.getPlayers()) {
            if(otherPlayer.getTurnIndicator() > hightestIndicator) {
                hightestIndicator = otherPlayer.getTurnIndicator();
            }
        }
        player.setTurnIndicator(hightestIndicator + 1);
        playerRepository.save(player);

        player.setGameId(game);
        playerRepository.save(player);

        List<String> playersOfGame = new ArrayList<>();
        for(PlayerModel otherPlayer : game.getPlayers()) {
            playersOfGame.add(otherPlayer.getDisplayName());
        }

        return WebSocketJoinGameResponse.builder()
                .sender(joinGameDto.getDisplayName())
                .responseType(ResponseType.JOIN_GAME)
                .otherPlayers(playersOfGame)
                .host(game.getHostId().getDisplayName())
                .build();
    }


    public WebSocketJoinGameAnonymousResponse joinGameAnonymous(JoinGameDto joinGameDto, String sessionId) throws IllegalArgumentException, IllegalStateException {
        Optional<GameModel> gameOptional = gameRepository.findByGameCode(joinGameDto.getGameCode());
        if(gameOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        GameModel game = gameOptional.get();
        Set<PlayerModel> players = game.getPlayers();
        if(players.size() == 4) {
            throw new IllegalStateException("Game already has 4 players");
        }

        UserModel user = new UserModel();
        String username = generateRandomString(10);
        user.setUsername(username);
        user.setEmail(generateRandomString(10));
        String password = generateRandomString(10);
        user.setUserPassword(passwordEncoder.encode(password));
        user.setIsAnonymous(true);
        user.setStatGamesWon(0);
        user.setStatGamesLost(0);
        userRepository.save(user);

        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setPassword(password);
        loginUserDto.setUsername(username);
        UserModel authenticatedUser = authenticationService.authenticate(loginUserDto);
        String jwtToken = jwtService.generateToken(authenticatedUser);

        // Create Player
        PlayerModel player = new PlayerModel();
        player.setUserId(user);
        player.setDisplayName(joinGameDto.getDisplayName());
        player.setWebSocketId(sessionId);
        DeckModel handCards = new DeckModel();
        deckRepository.save(handCards);
        player.setHandCards(handCards);
        // Generate unique turn indicator
        int randomTurnIndicator = (int)(Math.random() * 5);
        boolean uniqueTurnIndicator = false;
        while (!uniqueTurnIndicator) {
            uniqueTurnIndicator = true;
            randomTurnIndicator = (int)(Math.random() * 5);
            for(PlayerModel otherPlayer : game.getPlayers()) {
                if(otherPlayer.getTurnIndicator() == randomTurnIndicator) {
                    uniqueTurnIndicator = false;
                }
            }
        }
        player.setTurnIndicator(randomTurnIndicator);
        playerRepository.save(player);

        player.setGameId(game);
        playerRepository.save(player);

        List<String> playersOfGame = new ArrayList<>();
        for(PlayerModel otherPlayer : game.getPlayers()) {
            playersOfGame.add(otherPlayer.getDisplayName());
        }

        return WebSocketJoinGameAnonymousResponse.builder()
                .sender(joinGameDto.getDisplayName())
                .responseType(ResponseType.JOIN_GAME)
                .otherPlayers(playersOfGame)
                .host(game.getHostId().getDisplayName())
                .jwt(jwtToken)
                .build();
    }


    public WebSocketStartGameResponse startGame(GameCodeDto gameCodeDto, String username) throws IllegalArgumentException, IllegalStateException {
        Optional<GameModel> gameOptional = gameRepository.findByGameCode(gameCodeDto.getGameCode());
        if(gameOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        GameModel game = gameOptional.get();

        if(!game.getGameStatus().equals("InLobby")) {
            throw new IllegalStateException("Game has already started or finished");
        }

        PlayerModel callingPlayer = webSocketUtilService.findPlayer(username, gameCodeDto.getGameCode());
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

        game.setGameStatus("Running");
        setupCardOfGame(game);

        CardDto cardDto = new CardDto();
        CardModel topCard = webSocketUtilService.viewTopCard(game.getDiscardPile());
        cardDto.setCardName(topCard.getCardType().getCardName());
        cardDto.setCardValue(topCard.getCardType().getCardValue());
        cardDto.setCardEvent(topCard.getCardType().getCardEvent());

        randomizeTurnOrder(game);
        PlayerModel startingPlayer = playerRepository.findByGameIdAndTurnIndicator(game, 1).get();
        game.setCurrentPlayerId(startingPlayer);
        gameRepository.save(game);
        TurnOrderDto turnOrderDto = webSocketUtilService.createTurnOrderDto(game);

        return WebSocketStartGameResponse.builder()
                .sender(callingPlayer.getDisplayName())
                .responseType(ResponseType.START_GAME)
                .centerCard(cardDto)
                .turnOrder(turnOrderDto)
                .build();
    }


    public WebSocketCloseGameResponse closeGame(GameCodeDto gameCodeDto, String username) throws IllegalArgumentException, IllegalStateException {
        Optional<GameModel> gameOptional = gameRepository.findByGameCode(gameCodeDto.getGameCode());
        if(gameOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        GameModel game = gameOptional.get();

        if(!game.getGameStatus().equals("Finished")) {
            throw new IllegalStateException("Game has already started or finished");
        }

        PlayerModel callingPlayer = webSocketUtilService.findPlayer(username, gameCodeDto.getGameCode());
        if(callingPlayer == null) {
            throw new IllegalStateException("User is not part of the game");
        }
        if(!game.getHostId().getId().equals(callingPlayer.getId())) {
            throw new IllegalStateException("User is not the host");
        }

        return WebSocketCloseGameResponse.builder()
                .sender(callingPlayer.getDisplayName())
                .responseType(ResponseType.HOST_CLOSE)
                .build();
    }


    @Transactional
    public void deleteGame(String gameCode) {
        GameModel game = gameRepository.findByGameCode(gameCode).get();
        for(PlayerModel player : playerRepository.findByGameIdOrderByTurnIndicatorDesc(game).get()) {
            for(CardModel card : cardRepository.findByDeckId(player.getHandCards()).get()) {
                cardRepository.delete(card);
            }
            playerRepository.delete(player);
            deckRepository.delete(player.getHandCards());
            UserModel user = player.getUserId();
            if(user.getIsAnonymous()) {
                userRepository.delete(user);
            }
        }
        gameRepository.delete(game);
        for(CardModel card : cardRepository.findByDeckId(game.getCenterDeck()).get()) {
            cardRepository.delete(card);
        }
        deckRepository.delete(game.getCenterDeck());
        for(CardModel card : cardRepository.findByDeckId(game.getDiscardPile()).get()) {
            cardRepository.delete(card);
        }
        deckRepository.delete(game.getDiscardPile());
    }


    public WebSocketStartGameResponse restartGame(GameCodeDto gameCodeDto, String username) throws IllegalArgumentException, IllegalStateException {
        Optional<GameModel> gameOptional = gameRepository.findByGameCode(gameCodeDto.getGameCode());
        if(gameOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        GameModel game = gameOptional.get();

        if(!game.getGameStatus().equals("Finished")) {
            throw new IllegalStateException("Game has already started or finished");
        }

        PlayerModel callingPlayer = webSocketUtilService.findPlayer(username, gameCodeDto.getGameCode());
        if(callingPlayer == null) {
            throw new IllegalStateException("User is not part of the game");
        }
        if(!game.getHostId().getId().equals(callingPlayer.getId())) {
            throw new IllegalStateException("User is not the host");
        }

        deleteCardsOfGame(gameCodeDto.getGameCode());

        setupCardOfGame(game);

        game.setGameStatus("Running");
        gameRepository.save(game);

        CardDto cardDto = new CardDto();
        CardModel topCard = webSocketUtilService.viewTopCard(game.getDiscardPile());
        cardDto.setCardName(topCard.getCardType().getCardName());
        cardDto.setCardValue(topCard.getCardType().getCardValue());
        cardDto.setCardEvent(topCard.getCardType().getCardEvent());

        randomizeTurnOrder(game);
        game.setCurrentPlayerId(playerRepository.findByGameIdAndTurnIndicator(game, 1).get());
        gameRepository.save(game);
        TurnOrderDto turnOrderDto = webSocketUtilService.createTurnOrderDto(game);

        return WebSocketStartGameResponse.builder()
                .sender(callingPlayer.getDisplayName())
                .responseType(ResponseType.HOST_RESTART)
                .centerCard(cardDto)
                .turnOrder(turnOrderDto)
                .build();
    }


    @Transactional
    private void deleteCardsOfGame(String gameCode) {
        GameModel game = gameRepository.findByGameCode(gameCode).get();
        for(PlayerModel player : playerRepository.findByGameIdOrderByTurnIndicatorDesc(game).get()) {
            for(CardModel card : cardRepository.findByDeckId(player.getHandCards()).get()) {
                cardRepository.delete(card);
            }
        }
        for(CardModel card : cardRepository.findByDeckId(game.getCenterDeck()).get()) {
            cardRepository.delete(card);
        }
        for(CardModel card : cardRepository.findByDeckId(game.getDiscardPile()).get()) {
            cardRepository.delete(card);
        }
    }


    private void randomizeTurnOrder(GameModel game) {
        int randomTurnIndicator = (int)(Math.random() * 5);
        boolean uniqueTurnIndicator;
        for(PlayerModel player : game.getPlayers()) {
            player.setTurnIndicator(0);
            playerRepository.save(player);
        }
        for(PlayerModel player : game.getPlayers()) {
            uniqueTurnIndicator = false;
            while (!uniqueTurnIndicator) {
                uniqueTurnIndicator = true;
                randomTurnIndicator = (int)(Math.random() * 5);
                for(PlayerModel otherPlayer : game.getPlayers()) {
                    if(otherPlayer.getTurnIndicator() == randomTurnIndicator) {
                        uniqueTurnIndicator = false;
                    }
                }
            }
            player.setTurnIndicator(randomTurnIndicator);
            playerRepository.save(player);
        }
    }


    private void setupCardOfGame(GameModel game) {
        setupStartingDeck(game.getCenterDeck());
        for(PlayerModel player : game.getPlayers()) {
            DeckModel handCards = player.getHandCards();
            for(int i = 0; i < 5; i++) {
                CardModel card = webSocketUtilService.viewTopCard(game.getCenterDeck());
                card.setDeckId(handCards);
                card.setDeckPosition(i);
                cardRepository.save(card);
            }
        }

        CardModel card = webSocketUtilService.viewTopCard(game.getCenterDeck());
        card.setDeckId(game.getDiscardPile());
        card.setDeckPosition(1);
        cardRepository.save(card);

        if(card.getCardType().getCardEvent().equals("DRAW")) {
            game.setDrawCount(card.getCardType().getCardValue());
            gameRepository.save(game);
        }
        else {
            game.setDrawCount(0);
            gameRepository.save(game);
        }
    }


    private void setupStartingDeck(DeckModel deck) {
        String[] types = {"Rock", "Paper", "Scissors"};
        int maxNumber = 9;
        int plus2Count = 4;
        int plus4Count = 2;
        int numberOfSets = 2;
        List<Integer> range = new ArrayList<>(IntStream.range(1, maxNumber * types.length * numberOfSets + 1 + plus2Count * types.length + plus4Count * types.length).boxed().toList());
        Collections.shuffle(range);
        Deque<Integer> stack = new ArrayDeque<>(range);
        for(int i = 0; i < numberOfSets; i++) {
            for (int j = 0; j < types.length; j++) {
                String type = types[j];
                for (int k = 1; k <= maxNumber; k++) {
                    CardModel card = new CardModel();
                    CardTypeModel cardType = cardTypeRepository.findByCardNameAndCardValueAndCardEvent(type, k, "NONE").get();
                    card.setCardType(cardType);
                    card.setDeckId(deck);
                    card.setDeckPosition(stack.pop());
                    cardRepository.save(card);
                }
            }
        }
        for(int i = 0; i < plus2Count; i++) {
            for (int j = 1; j <= types.length; j++) {
                String type = types[j - 1];
                CardModel card = new CardModel();
                CardTypeModel cardType = cardTypeRepository.findByCardNameAndCardValueAndCardEvent(type, 2, "DRAW").get();
                card.setCardType(cardType);
                card.setDeckId(deck);
                card.setDeckPosition(stack.pop());
                cardRepository.save(card);
            }
        }
        for(int i = 0; i < plus4Count; i++) {
            for (int j = 1; j <= types.length; j++) {
                String type = types[j - 1];
                CardModel card = new CardModel();
                CardTypeModel cardType = cardTypeRepository.findByCardNameAndCardValueAndCardEvent(type, 4, "DRAW").get();
                card.setCardType(cardType);
                card.setDeckId(deck);
                card.setDeckPosition(stack.pop());
                cardRepository.save(card);
            }
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
        return generateRandomString(6);
    }


    private String generateRandomString(int numberOfCharacters) {
        String allowed_characters = "ABCDEFGHIJKLMNOPRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(6);
        for(int i = 0; i < numberOfCharacters; i++){
            sb.append(allowed_characters.charAt((int)(Math.random()*allowed_characters.length())));
        }
        return sb.toString();
    }
}
