package game.CardGame.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "card")
@Entity
@Getter
@Setter
public class CardModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "card_type", nullable = false)
    private CardTypeModel cardType;

    @ManyToOne
    @JoinColumn(name = "deck_id")
    private DeckModel deckId;

    @Column(name = "deck_position")
    private Integer deckPosition;
}
