package game.CardGame.responseDtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WebSocketMessageResponse extends WebSocketResponseDto {
    String message;
}
