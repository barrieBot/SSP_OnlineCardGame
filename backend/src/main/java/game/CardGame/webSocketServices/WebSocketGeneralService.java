package game.CardGame.webSocketServices;

import game.CardGame.dtos.CardDto;
import game.CardGame.enums.ResponseType;
import game.CardGame.models.CardModel;
import game.CardGame.models.GameModel;
import game.CardGame.models.PlayerModel;
import game.CardGame.repositories.CardRepository;
import game.CardGame.repositories.GameRepository;
import game.CardGame.repositories.PlayerRepository;
import game.CardGame.responseDtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class WebSocketGeneralService {
    @Autowired
    private WebSocketUtilService webSocketUtilService;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private CardRepository cardRepository;

    public WebSocketReconnectResult reconnectWebSocket(String username, String gameCode, String newWebSocketId) throws IllegalArgumentException, IllegalStateException {
        PlayerModel callingPlayer = webSocketUtilService.findPlayer(username, gameCode);
        if(callingPlayer == null) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        callingPlayer.setWebSocketId(newWebSocketId);
        playerRepository.save(callingPlayer);

        GameModel game = gameRepository.findByGameCode(gameCode).get();
        switch (game.getGameStatus()) {
            case "InLobby":
                List<String> playersOfGame = new ArrayList<>();
                for(PlayerModel p : game.getPlayers()) {
                    playersOfGame.add(p.getDisplayName());
                }
                return WebSocketReconnectLobbyResponse.builder()
                        .sender(callingPlayer.getDisplayName())
                        .responseType(ResponseType.RECONNECT_LOBBY)
                        .players(playersOfGame)
                        .build();
            case "Running":
                CardDto centerCardDto = new CardDto();
                CardModel topCard = webSocketUtilService.viewTopCard(game.getDiscardPile());
                centerCardDto.setCardName(topCard.getCardType().getCardName());
                centerCardDto.setCardValue(topCard.getCardType().getCardValue());
                centerCardDto.setCardEvent(topCard.getCardType().getCardEvent());

                TurnOrderDto turnOrderDto = webSocketUtilService.createTurnOrderDto(game);

                ArrayList<CardDto> handCards = new ArrayList<>();
                Set<CardModel> cards =  cardRepository.findByDeckId(callingPlayer.getHandCards()).get();
                for(CardModel card : cards) {
                    CardDto cardDto = new CardDto();
                    cardDto.setCardName(card.getCardType().getCardName());
                    cardDto.setCardValue(card.getCardType().getCardValue());
                    cardDto.setCardEvent(card.getCardType().getCardEvent());
                    handCards.add(cardDto);
                }

                CardAmountsDto cardAmountsDto = webSocketUtilService.createCardAmountsDto(game);
                return WebSocketReconnectGameResponse.builder()
                        .sender(callingPlayer.getDisplayName())
                        .responseType(ResponseType.RECONNECT_GAME)
                        .turnOrder(turnOrderDto)
                        .centerCard(centerCardDto)
                        .handCards(handCards)
                        .drawCount(game.getDrawCount())
                        .currentPlayer(game.getCurrentPlayerId().getDisplayName())
                        .cardAmounts(cardAmountsDto)
                        .build();
            default:
                throw new IllegalStateException("Game is not running");
        }
    }
}
