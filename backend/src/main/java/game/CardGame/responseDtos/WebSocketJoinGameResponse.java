package game.CardGame.responseDtos;

import game.CardGame.enums.ResponseType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class WebSocketJoinGameResponse extends WebSocketResponseDto {
    String sender;
    ResponseType responseType;
    String host;
    List<String> otherPlayers;
}
