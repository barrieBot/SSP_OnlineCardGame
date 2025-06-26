package game.CardGame.services;

import game.CardGame.models.UserModel;
import game.CardGame.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserSettingsService {
    @Autowired
    private UserRepository userRepository;

    public void changeUsername(Integer userId, String newUsername) {
        UserModel user = userRepository.findById(userId).get();
        user.setUsername(newUsername);
        userRepository.save(user);
    }


    public void changeEmail(Integer userId, String newEmail) {
        UserModel user = userRepository.findById(userId).get();
        user.setEmail(newEmail);
        userRepository.save(user);
    }
}
