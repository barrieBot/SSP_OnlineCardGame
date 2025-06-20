package game.CardGame.repositories;

import game.CardGame.models.GameModel;
import game.CardGame.models.PlayerModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PlayerRepository extends CrudRepository<PlayerModel, String> {
    Optional<PlayerModel> findById(String id);

    Optional<Set<PlayerModel>> findByUserId_Username(String username);

    Optional<PlayerModel> findByGameIdAndTurnIndicator(GameModel gameModel, Integer turnIndicator);

    Optional<List<PlayerModel>> findByGameIdOrderByTurnIndicatorDesc(GameModel gameId);
}
