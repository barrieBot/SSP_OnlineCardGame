package game.CardGame.responseDtos;

import game.CardGame.dtos.CardDto;
import game.CardGame.enums.ResponseType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WebSocketStartGameResponse extends WebSocketResponseDto {
    String sender;
    ResponseType responseType;
    CardDto centerCard;
    TurnOrderDto turnOrder;
    Object handCards;
}
