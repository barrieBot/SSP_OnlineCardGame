package game.CardGame.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "card_type")
@Entity
@Getter
@Setter
public class CardTypeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false, length = 50, name = "card_name")
    private String cardName;

    @Column(nullable = false, name = "card_value")
    private Integer cardValue;

    @Column(nullable = false, length = 50, name = "card_event")
    private String cardEvent;
}
