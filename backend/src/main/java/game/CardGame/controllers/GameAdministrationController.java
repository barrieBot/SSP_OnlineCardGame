package game.CardGame.controllers;

import game.CardGame.dtos.CreateMatchDto;
import game.CardGame.dtos.JoinGameDto;
import game.CardGame.dtos.PlayerIdDto;
import game.CardGame.models.GameModel;
import game.CardGame.services.GameAdministrationService;
import game.CardGame.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gameAdmin")
public class GameAdministrationController {

    @Autowired
    private GameAdministrationService gameAdministrationService;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/createMatch")
    public ResponseEntity<CreateMatchDto> createMatch(){
        CreateMatchDto newMatch = new CreateMatchDto();
        return ResponseEntity.ok(newMatch);
    }

    @GetMapping("/idFromToken")
    public ResponseEntity<Integer> getIdFromToken(HttpServletRequest request) {
        String jwt = jwtService.extractJwtFromRequest(request);

        Integer userId = jwtService.extractClaim(jwt, claims -> claims.get("userId", Integer.class));
        return ResponseEntity.ok(userId);
    }

    @PostMapping("/joinmatch/{gameCode}")
    public ResponseEntity<JoinGameDto> joinMatch(@PathVariable String gameCode, @RequestBody PlayerIdDto playerid){
        GameModel updatedGame = gameAdministrationService.joinMatch(gameCode, Integer.toString(playerid.getPlayerId()));
        JoinGameDto joinMatchDto = new JoinGameDto();
        joinMatchDto.setGameCode(updatedGame.getGameCode());
        return ResponseEntity.ok(joinMatchDto);
    }
}
