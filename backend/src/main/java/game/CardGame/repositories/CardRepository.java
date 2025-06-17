package game.CardGame.repositories;

import game.CardGame.models.CardModel;
import game.CardGame.models.DeckModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends CrudRepository<CardModel, Integer> {
    Optional<CardModel> findById(Integer id);

    Optional<CardModel> findTopDeckPositionByDeckIdOrderByDeckPosition(DeckModel deckId);
}
