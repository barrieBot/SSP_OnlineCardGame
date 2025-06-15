package game.CardGame.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinGameDto {
    private String gameCode;
    private String displayName;
}
