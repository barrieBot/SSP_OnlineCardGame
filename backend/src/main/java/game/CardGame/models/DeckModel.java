package game.CardGame.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Table(name = "deck")
@Entity
@Getter
@Setter
public class DeckModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @OneToMany(mappedBy = "deckId")
    private Set<CardModel> cardId;

}
