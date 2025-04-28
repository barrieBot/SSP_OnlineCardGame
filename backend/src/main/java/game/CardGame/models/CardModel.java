package game.CardGame.models;

import jakarta.persistence.*;

@Table(name = "card")
@Entity
public class CardModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private CardTypeModel type;


    public CardTypeModel getType() {
        return type;
    }

    public void setType(CardTypeModel type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
