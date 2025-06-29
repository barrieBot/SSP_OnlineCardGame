package game.CardGame.responseDtos;

import game.CardGame.dtos.CardDto;
import game.CardGame.enums.ResponseType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class WebSocketReconnectGameResponse extends WebSocketReconnectResult {
    ResponseType responseType;
    String sender;
    TurnOrderDto turnOrder;
    CardDto centerCard;
    List<CardDto> handCards;
    Integer drawCount;
    String currentPlayer;
    String host;
    CardAmountsDto cardAmounts;
}
