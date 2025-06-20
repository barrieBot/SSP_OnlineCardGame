package game.CardGame.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerWonResponseDto {
    String playerName;
    CardDto card;
}
