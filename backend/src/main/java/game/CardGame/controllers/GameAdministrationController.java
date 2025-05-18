package game.CardGame.controllers;

import game.CardGame.dtos.CreateMatchDto;
import game.CardGame.dtos.JoinMatchDto;
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
        String jwt = extractJwtFromRequest(request);

        Integer userId = jwtService.extractClaim(jwt, claims -> claims.get("userId", Integer.class));
        return ResponseEntity.ok(userId);
    }

    @PostMapping("/joinmatch/{matchtoken}")
    public ResponseEntity<JoinMatchDto> joinMatch(@PathVariable Integer matchtoken, @RequestBody PlayerIdDto playerid){
        GameModel updatedGame = gameAdministrationService.joinMatch(matchtoken, Integer.toString(playerid.getPlayerId()));
        JoinMatchDto joinMatchDto = new JoinMatchDto();
        joinMatchDto.setMatchToken(updatedGame.getMatchToken());
        return ResponseEntity.ok(joinMatchDto);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Entfernt "Bearer "
        }
        return null;
    }
}
