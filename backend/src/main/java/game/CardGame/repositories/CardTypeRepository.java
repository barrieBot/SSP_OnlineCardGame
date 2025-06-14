package game.CardGame.repositories;

import game.CardGame.models.CardTypeModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardTypeRepository extends CrudRepository<CardTypeModel, Integer> {
    Optional<CardTypeModel> findByCardNameAndCardValueAndCardEvent(String cardName, Integer cardValue, String cardEvent);
}
