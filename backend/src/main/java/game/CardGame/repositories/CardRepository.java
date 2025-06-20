package game.CardGame.repositories;

import game.CardGame.models.CardModel;
import game.CardGame.models.DeckModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CardRepository extends CrudRepository<CardModel, Integer> {
    Optional<CardModel> findById(Integer id);

    Optional<CardModel> findTopDeckPositionByDeckIdOrderByDeckPositionDesc(DeckModel deckId);

    Optional<Set<CardModel>> findByDeckId(DeckModel deckId);
}
