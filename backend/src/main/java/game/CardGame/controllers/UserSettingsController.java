package game.CardGame.controllers;

import game.CardGame.services.JwtService;
import game.CardGame.services.UserSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/settings")
public class UserSettingsController {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserSettingsService userSettingsService;

    @PostMapping("/changeUsername")
    public ResponseEntity<String> changeUsername(@RequestParam("newUsername") String newUsername, HttpServletRequest request) {
        String jwt = jwtService.extractJwtFromRequest(request);
        Integer userId = jwtService.extractClaim(jwt, claims -> claims.get("userId", Integer.class));
        userSettingsService.changeUsername(userId, newUsername);
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/changeEmail")
    public ResponseEntity<String> changeEmail(@RequestParam("newEmail") String newEmail, HttpServletRequest request) {
        String jwt = jwtService.extractJwtFromRequest(request);
        Integer userId = jwtService.extractClaim(jwt, claims -> claims.get("userId", Integer.class));
        userSettingsService.changeEmail(userId, newEmail);
        return ResponseEntity.ok("Success");
    }
}
