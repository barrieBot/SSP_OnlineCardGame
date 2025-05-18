package game.CardGame.repositories;

import game.CardGame.models.GameModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends CrudRepository<GameModel, Integer> {
    Optional<GameModel> findById(Integer id);
}
