package game.CardGame.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name = "player")
@Entity
@Getter
@Setter
public class PlayerModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "game_id")
    private GameModel gameId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel userId;

    @OneToOne
    @JoinColumn(name = "hand_cards")
    private DeckModel handCards;

    @Column(name = "display_name", length = 50)
    private String displayName;

    @Column(name = "turn_indicator")
    private Integer turnIndicator;

    @Column(name = "web_socket_id")
    private String webSocketId;
}
