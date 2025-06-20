package game.CardGame.controllers;

import game.CardGame.services.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gameAdmin")
public class GameAdministrationController {
    @Autowired
    private JwtService jwtService;

    @GetMapping("/idFromToken")
    public ResponseEntity<Integer> getIdFromToken(HttpServletRequest request) {
        String jwt = jwtService.extractJwtFromRequest(request);

        Integer userId = jwtService.extractClaim(jwt, claims -> claims.get("userId", Integer.class));
        return ResponseEntity.ok(userId);
    }
}
