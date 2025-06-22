package game.CardGame.webSocketServices;

import game.CardGame.dtos.*;
import game.CardGame.enums.ResponseType;
import game.CardGame.models.CardModel;
import game.CardGame.models.GameModel;
import game.CardGame.models.PlayerModel;
import game.CardGame.models.UserModel;
import game.CardGame.repositories.CardRepository;
import game.CardGame.repositories.GameRepository;
import game.CardGame.repositories.PlayerRepository;
import game.CardGame.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WebSocketGameplayService {
    @Autowired
    public WebSocketUtilService webSocketUtilService;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private UserRepository userRepository;

    public WebSocketResponseDto playCard(PlayCardDto playCardDto, String username, String gameCode) throws IllegalArgumentException, IllegalStateException {
        Optional<GameModel> gameOptional = gameRepository.findByGameCode(gameCode);
        if(gameOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        GameModel game = gameOptional.get();

        if(!game.getGameStatus().equals("Running")) {
            throw new IllegalStateException("Game is not running");
        }

        PlayerModel callingPlayer = webSocketUtilService.findPlayer(username, gameCode);
        if(callingPlayer == null) {
            throw new IllegalStateException("User is not part of the game");
        }
        if(!game.getCurrentPlayerId().getId().equals(callingPlayer.getId())) {
            throw new IllegalStateException("Not Users turn");
        }

        // Check if player has card
        Set<CardModel> handCards = cardRepository.findByDeckId(callingPlayer.getHandCards()).get();
        boolean playerHasCard = false;
        CardModel cardToPlay = null;
        for(CardModel card : handCards) {
            if(card.getCardType().getCardName().equals(playCardDto.getCard().getCardName())
                    && card.getCardType().getCardValue().equals(playCardDto.getCard().getCardValue())
                    && card.getCardType().getCardEvent().equals(playCardDto.getCard().getCardEvent())) {
                playerHasCard = true;
                cardToPlay = card;
            }
        }
        if(!playerHasCard) {
            throw new IllegalArgumentException("Invalid Card");
        }

        // Check if card is playable
        CardModel topCard = webSocketUtilService.viewTopCard(game.getDiscardPile());
        boolean validCardPlay = false;
        switch (topCard.getCardType().getCardName()) {
            case "Rock" -> {
                if (playCardDto.getCard().getCardName().equals("Paper")) {
                    validCardPlay = true;
                } else if (playCardDto.getCard().getCardName().equals("Rock")
                        && playCardDto.getCard().getCardValue() > topCard.getCardType().getCardValue()) {
                    validCardPlay = true;
                }
            }
            case "Paper" -> {
                if (playCardDto.getCard().getCardName().equals("Scissors")) {
                    validCardPlay = true;
                } else if (playCardDto.getCard().getCardName().equals("Paper")
                        && playCardDto.getCard().getCardValue() > topCard.getCardType().getCardValue()) {
                    validCardPlay = true;
                }
            }
            case "Scissors" -> {
                if (playCardDto.getCard().getCardName().equals("Rock")) {
                    validCardPlay = true;
                } else if (playCardDto.getCard().getCardName().equals("Scissors")
                        && playCardDto.getCard().getCardValue() > topCard.getCardType().getCardValue()) {
                    validCardPlay = true;
                }
            }
        }
        if(!validCardPlay) {
            throw new IllegalArgumentException("Card cannot be played");
        }

        // Play card
        cardToPlay.setDeckId(game.getDiscardPile());
        cardToPlay.setDeckPosition(webSocketUtilService.viewTopCard(game.getDiscardPile()).getDeckPosition() + 1);
        cardRepository.save(cardToPlay);

        PlayerModel newCurrentPlayer = setNextPlayer(game, callingPlayer);


        CardDto cardDto = new CardDto();
        cardDto.setCardEvent(playCardDto.getCard().getCardEvent());
        cardDto.setCardValue(playCardDto.getCard().getCardValue());
        cardDto.setCardName(playCardDto.getCard().getCardName());

        if(webSocketUtilService.viewTopCard(callingPlayer.getHandCards()) != null) {
            CardPlayedResponseDto cardPlayedResponseDto = new CardPlayedResponseDto();
            cardPlayedResponseDto.setCard(cardDto);
            cardPlayedResponseDto.setNewCurrentPlayer(newCurrentPlayer.getDisplayName());

            return WebSocketResponseDto.builder()
                    .sender(callingPlayer.getDisplayName())
                    .responseType(ResponseType.CARD_PLACED)
                    .value(cardPlayedResponseDto)
                    .build();
        }
        else {
            PlayerWonResponseDto playerWonResponseDto = new PlayerWonResponseDto();
            playerWonResponseDto.setPlayerName(callingPlayer.getDisplayName());
            playerWonResponseDto.setCard(cardDto);
            game.setGameStatus("Finished");
            game.setWinningPlayerId(callingPlayer);
            gameRepository.save(game);

            for(PlayerModel player : playerRepository.findByGameIdOrderByTurnIndicatorDesc(game).get()) {
                UserModel user = player.getUserId();
                if (player.getId().equals(game.getWinningPlayerId().getId())) {
                    user.setStatGamesWon(user.getStatGamesWon() + 1);
                    userRepository.save(user);
                } else {
                    user.setStatGamesLost(user.getStatGamesLost() + 1);
                    userRepository.save(user);
                }
            }

            return WebSocketResponseDto.builder()
                    .sender(callingPlayer.getDisplayName())
                    .responseType(ResponseType.GAME_FINISHED)
                    .value(playerWonResponseDto)
                    .build();
        }
    }


    public WebSocketResponseDto drawCard(String gameCode, String username) throws IllegalArgumentException, IllegalStateException {
        Optional<GameModel> gameOptional = gameRepository.findByGameCode(gameCode);
        if(gameOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        GameModel game = gameOptional.get();

        if(!game.getGameStatus().equals("Running")) {
            throw new IllegalStateException("Game is not running");
        }

        PlayerModel callingPlayer = webSocketUtilService.findPlayer(username, gameCode);
        if(callingPlayer == null) {
            throw new IllegalStateException("User is not part of the game");
        }
        if(!game.getCurrentPlayerId().getId().equals(callingPlayer.getId())) {
            throw new IllegalStateException("Not Users turn");
        }

        PlayerModel newCurrentPlayer = setNextPlayer(game, callingPlayer);

        CardModel drawnCard = webSocketUtilService.viewTopCard(game.getCenterDeck());
        if(drawnCard == null) {
            shuffleDiscardPile(game);
            drawnCard = webSocketUtilService.viewTopCard(game.getCenterDeck());
        }
        drawnCard.setDeckId(callingPlayer.getHandCards());
        drawnCard.setDeckPosition(webSocketUtilService.viewTopCard(callingPlayer.getHandCards()).getDeckPosition() + 1);
        cardRepository.save(drawnCard);

        CardDto cardDto = new CardDto();
        cardDto.setCardEvent(drawnCard.getCardType().getCardEvent());
        cardDto.setCardValue(drawnCard.getCardType().getCardValue());
        cardDto.setCardName(drawnCard.getCardType().getCardName());

        return WebSocketResponseDto.builder()
                .sender(callingPlayer.getDisplayName())
                .responseType(ResponseType.CARD_DRAWN)
                .value(cardDto)
                .build();
    }


    private void shuffleDiscardPile(GameModel game) {
        Set<CardModel> discardedCards = cardRepository.findByDeckId(game.getDiscardPile()).get();
        CardModel topCard = webSocketUtilService.viewTopCard(game.getDiscardPile());
        List<CardModel> cardsToShuffle = new ArrayList<>();
        for(CardModel card : discardedCards) {
            if(!card.getId().equals(topCard.getId())) {
                cardsToShuffle.add(card);
            }
        }
        Collections.shuffle(cardsToShuffle);
        for(int i = 1; i <= cardsToShuffle.size(); i++) {
            CardModel card = cardsToShuffle.get(i - 1);
            card.setDeckPosition(i);
            card.setDeckId(game.getCenterDeck());
            cardRepository.save(card);
        }
        topCard.setDeckPosition(1);
        cardRepository.save(topCard);
    }


    private PlayerModel setNextPlayer(GameModel game, PlayerModel callingPlayer) {
        int newPlayerTurnIndicator = callingPlayer.getTurnIndicator() + 1;
        if(newPlayerTurnIndicator == 5) {
            newPlayerTurnIndicator = 1;
        }
        PlayerModel newCurrentPlayer = playerRepository.findByGameIdAndTurnIndicator(game, newPlayerTurnIndicator).get();
        game.setCurrentPlayerId(newCurrentPlayer);
        gameRepository.save(game);
        return newCurrentPlayer;
    }
}
