package game.CardGame.repositories;

import game.CardGame.models.DeckModel;
import game.CardGame.models.GameModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeckRepository extends CrudRepository<DeckModel, Integer> {
}
