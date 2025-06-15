package game.CardGame.dtos;

import game.CardGame.enums.ResponseType;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WebSocketResponseDto {
    private ResponseType responseType;
    private String id;
    private String sender;
    private Object value;

    public WebSocketResponseDto(ResponseType responseType, String message) {
        this.responseType = responseType;
        this.id = message;
    }
}
