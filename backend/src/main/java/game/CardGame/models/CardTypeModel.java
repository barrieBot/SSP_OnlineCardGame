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

    @Column(nullable = false, length = 50)
    private String cardName;
}
