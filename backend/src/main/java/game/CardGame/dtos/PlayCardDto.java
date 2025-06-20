package game.CardGame.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayCardDto {
    String gameCode;
    CardDto card;
}
