package game.CardGame.responseDtos;

import game.CardGame.enums.ResponseType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WebSocketCloseGameResponse extends WebSocketResponseDto {
    String sender;
    ResponseType responseType;
}
