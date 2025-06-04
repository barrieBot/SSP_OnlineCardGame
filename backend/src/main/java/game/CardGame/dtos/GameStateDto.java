package game.CardGame.dtos;

import game.CardGame.enums.GameAction;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameStateDto {
    private GameAction action;
    private String id;
    private String sender;
    private Object value;
}
