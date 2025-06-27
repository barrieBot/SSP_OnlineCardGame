package game.CardGame.responseDtos;

import game.CardGame.enums.ResponseType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class WebSocketReconnectLobbyResponse extends WebSocketReconnectResult {
    ResponseType responseType;
    String sender;
    List<String> players;
}
