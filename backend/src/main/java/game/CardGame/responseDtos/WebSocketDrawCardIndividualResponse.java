package game.CardGame.responseDtos;

import game.CardGame.dtos.CardDto;
import game.CardGame.enums.ResponseType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class WebSocketDrawCardIndividualResponse extends WebSocketResponseDto {
    String sender;
    ResponseType responseType;
    List<CardDto> drawnCards;
    Integer drawCount;
}
