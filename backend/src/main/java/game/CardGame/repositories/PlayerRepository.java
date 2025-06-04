package game.CardGame.repositories;

import game.CardGame.models.PlayerModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends CrudRepository<PlayerModel, String> {
    Optional<PlayerModel> findById(String id);

    Optional<PlayerModel> findByUserId_Username(String username);
}
