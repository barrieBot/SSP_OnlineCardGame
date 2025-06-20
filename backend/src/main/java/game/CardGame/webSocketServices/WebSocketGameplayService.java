package game.CardGame.webSocketServices;

import game.CardGame.dtos.CardDto;
import game.CardGame.dtos.CardPlayedResponseDto;
import game.CardGame.dtos.PlayCardDto;
import game.CardGame.dtos.WebSocketResponseDto;
import game.CardGame.enums.ResponseType;
import game.CardGame.models.CardModel;
import game.CardGame.models.GameModel;
import game.CardGame.models.PlayerModel;
import game.CardGame.repositories.CardRepository;
import game.CardGame.repositories.GameRepository;
import game.CardGame.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

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

        // Change Current Player
        int newPlayerTurnIndicator = callingPlayer.getTurnIndicator() + 1;
        if(newPlayerTurnIndicator == 5) {
            newPlayerTurnIndicator = 1;
        }
        PlayerModel newCurrentPlayer = playerRepository.findByGameIdAndTurnIndicator(game, newPlayerTurnIndicator).get();
        game.setCurrentPlayerId(newCurrentPlayer);
        gameRepository.save(game);

        CardPlayedResponseDto cardPlayedResponseDto = new CardPlayedResponseDto();
        CardDto cardDto = new CardDto();
        cardDto.setCardEvent(playCardDto.getCard().getCardEvent());
        cardDto.setCardValue(playCardDto.getCard().getCardValue());
        cardDto.setCardName(playCardDto.getCard().getCardName());
        cardPlayedResponseDto.setCard(cardDto);
        cardPlayedResponseDto.setNewCurrentPlayer(newCurrentPlayer.getDisplayName());

        return WebSocketResponseDto.builder()
                .sender(callingPlayer.getDisplayName())
                .responseType(ResponseType.CARD_PLACED)
                .value(cardPlayedResponseDto)
                .build();
    }
}
