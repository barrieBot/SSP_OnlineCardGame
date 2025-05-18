package game.CardGame.controllers;

import game.CardGame.dtos.CreateMatchDto;
import game.CardGame.dtos.JoinMatchDto;
import game.CardGame.dtos.PlayerIdDto;
import game.CardGame.models.GameModel;
import game.CardGame.services.GameAdministrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gameAdmin")
public class GameAdministrationController {

    @Autowired
    private GameAdministrationService gameAdministrationService;

    @PostMapping("/createMatch")
    public ResponseEntity<CreateMatchDto> createMatch(){
        CreateMatchDto newMatch = new CreateMatchDto();
        return ResponseEntity.ok(newMatch);
    }

    @PostMapping("/joinmatch/{matchtoken}")
    public ResponseEntity<JoinMatchDto> joinMatch(@PathVariable Integer matchtoken, @RequestBody PlayerIdDto playerid){
        GameModel updatedGame = gameAdministrationService.joinMatch(matchtoken, Integer.toString(playerid.getPlayerId()));
        JoinMatchDto joinMatchDto = new JoinMatchDto();
        joinMatchDto.setMatchToken(updatedGame.getMatchToken());
        return ResponseEntity.ok(joinMatchDto);
    }
}
