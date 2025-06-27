package game.CardGame.webSocketServices;

import game.CardGame.dtos.CardDto;
import game.CardGame.enums.ResponseType;
import game.CardGame.models.CardModel;
import game.CardGame.models.GameModel;
import game.CardGame.models.PlayerModel;
import game.CardGame.repositories.CardRepository;
import game.CardGame.repositories.GameRepository;
import game.CardGame.repositories.PlayerRepository;
import game.CardGame.responseDtos.TurnOrderDto;
import game.CardGame.responseDtos.WebSocketReconnectGameResponse;
import game.CardGame.responseDtos.WebSocketReconnectLobbyResponse;
import game.CardGame.responseDtos.WebSocketReconnectResult;
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
        PlayerModel player = webSocketUtilService.findPlayer(username, gameCode);
        if(player == null) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        player.setWebSocketId(newWebSocketId);
        playerRepository.save(player);

        GameModel game = gameRepository.findByGameCode(gameCode).get();
        switch (game.getGameStatus()) {
            case "InLobby":
                List<String> playersOfGame = new ArrayList<>();
                for(PlayerModel p : game.getPlayers()) {
                    playersOfGame.add(p.getDisplayName());
                }
                return WebSocketReconnectLobbyResponse.builder()
                        .sender(player.getDisplayName())
                        .responseType(ResponseType.RECONNECT)
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
                Set<CardModel> cards =  cardRepository.findByDeckId(player.getHandCards()).get();
                for(CardModel card : cards) {
                    CardDto cardDto = new CardDto();
                    cardDto.setCardName(card.getCardType().getCardName());
                    cardDto.setCardValue(card.getCardType().getCardValue());
                    cardDto.setCardEvent(card.getCardType().getCardEvent());
                    handCards.add(cardDto);
                }
                return WebSocketReconnectGameResponse.builder()
                        .sender(player.getDisplayName())
                        .responseType(ResponseType.RECONNECT)
                        .turnOrderDto(turnOrderDto)
                        .centerCard(centerCardDto)
                        .handCards(handCards)
                        .drawCount(game.getDrawCount())
                        .build();
            default:
                throw new IllegalStateException("Game is not running");
        }
    }
}
