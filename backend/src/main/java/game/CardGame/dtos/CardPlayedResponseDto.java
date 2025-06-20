package game.CardGame.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardPlayedResponseDto {
    String newCurrentPlayer;
    String cardName;
    Integer cardValue;
    String cardEvent;
}
