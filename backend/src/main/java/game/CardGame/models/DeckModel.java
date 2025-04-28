package game.CardGame.models;

import jakarta.persistence.*;

import java.util.Set;

@Table(name = "deck")
@Entity
public class DeckModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @OneToMany
    @JoinColumn
    private Set<CardModel> cardId;

}
