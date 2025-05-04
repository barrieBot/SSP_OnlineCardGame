package game.CardGame.models;

import jakarta.persistence.*;

@Table(name = "player")
@Entity
public class PlayerModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(unique = true, name = "player_token")
    private Integer playerToken;

    @OneToOne
    @JoinColumn(nullable = false)
    private GameModel gameId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel userId;

    @OneToOne
    @JoinColumn(name = "hand_cards", nullable = false)
    private DeckModel handCards;

    @Column(name = "displayName", length = 50)
    private String displayName;

}
