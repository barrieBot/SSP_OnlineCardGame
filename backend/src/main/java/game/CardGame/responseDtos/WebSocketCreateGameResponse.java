package game.CardGame.responseDtos;

import game.CardGame.enums.ResponseType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WebSocketCreateGameResponse extends WebSocketResponseDto {
    private String gameCode;
    private String sender;
    private ResponseType responseType;
}
