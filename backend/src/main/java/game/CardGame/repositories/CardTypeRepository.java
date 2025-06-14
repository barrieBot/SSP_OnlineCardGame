package game.CardGame.repositories;

import game.CardGame.models.CardTypeModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardTypeRepository extends CrudRepository<CardTypeModel, Integer> {
}
