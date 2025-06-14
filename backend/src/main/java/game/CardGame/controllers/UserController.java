package game.CardGame.controllers;

import game.CardGame.models.UserModel;
import game.CardGame.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class UserController {
    private UserRepository userRepository;

    @Autowired
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @GetMapping("/user")
    public String getEmailByName(@RequestParam String email) {
        UserModel user = userRepository.findByEmail(email).get();
        return user.getUsername();
    }
}
