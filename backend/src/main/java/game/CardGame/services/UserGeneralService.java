package game.CardGame.services;

import game.CardGame.models.UserModel;
import game.CardGame.repositories.UserRepository;
import game.CardGame.responseDtos.UserStatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserGeneralService {
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

    public UserStatsDto getUserStats(Integer userId) {
        UserModel user = userRepository.findById(userId).get();
        UserStatsDto userStatsDto = new UserStatsDto();
        userStatsDto.setGamesLost(user.getStatGamesLost());
        userStatsDto.setGamesWon(user.getStatGamesWon());
        return userStatsDto;
    }
}
