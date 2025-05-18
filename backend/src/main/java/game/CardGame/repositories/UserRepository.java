package game.CardGame.repositories;

import game.CardGame.models.UserModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserModel, Integer> {
    Optional<UserModel> findByEmail(String email);

    Optional<UserModel> findByUsername(String username);
}
