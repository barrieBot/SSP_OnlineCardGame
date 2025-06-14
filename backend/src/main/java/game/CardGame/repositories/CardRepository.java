package game.CardGame.repositories;

import game.CardGame.models.CardModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends CrudRepository<CardModel, Integer> {
    Optional<CardModel> findById(Integer id);
}
