package game.CardGame.responseDtos;

import game.CardGame.enums.ResponseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class WebSocketErrorResponse extends WebSocketResponseDto {
    private ResponseType errorType;
    private String message;
}
