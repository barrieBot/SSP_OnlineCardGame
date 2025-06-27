package game.CardGame.controllers;

import game.CardGame.responseDtos.UserStatsDto;
import game.CardGame.services.JwtService;
import game.CardGame.services.UserGeneralService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/general")
public class UserGeneralController {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserGeneralService userGeneralService;

    @PostMapping("/changeUsername")
    public ResponseEntity<String> changeUsername(@RequestParam("newUsername") String newUsername, HttpServletRequest request) {
        String jwt = jwtService.extractJwtFromRequest(request);
        Integer userId = jwtService.extractClaim(jwt, claims -> claims.get("userId", Integer.class));
        userGeneralService.changeUsername(userId, newUsername);
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/changeEmail")
    public ResponseEntity<String> changeEmail(@RequestParam("newEmail") String newEmail, HttpServletRequest request) {
        String jwt = jwtService.extractJwtFromRequest(request);
        Integer userId = jwtService.extractClaim(jwt, claims -> claims.get("userId", Integer.class));
        userGeneralService.changeEmail(userId, newEmail);
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/getUserStats")
    public ResponseEntity<UserStatsDto> getUserStats(HttpServletRequest request) {
        String jwt = jwtService.extractJwtFromRequest(request);
        Integer userId = jwtService.extractClaim(jwt, claims -> claims.get("userId", Integer.class));
        UserStatsDto playerStats = userGeneralService.getUserStats(userId);
        return ResponseEntity.ok(playerStats);
    }
}
