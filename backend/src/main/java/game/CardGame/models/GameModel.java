package game.CardGame.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.Set;

@Table(name = "game")
@Entity
@Getter
@Setter
public class GameModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(unique = true, name = "game_code")
    private String gameCode;

    @Column(nullable = false, length = 20, name = "game_status")
    private String gameStatus;

    @OneToOne
    @JoinColumn(name = "current_player_id", nullable = false)
    private PlayerModel currentPlayerId;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at")
    private Date createdAt;

    @OneToOne
    @JoinColumn(name = "center_deck", nullable = false)
    private DeckModel centerDeck;

    @OneToOne
    @JoinColumn(name = "host_id", nullable = false)
    private PlayerModel hostId;

    @OneToMany(mappedBy = "gameId")
    private Set<PlayerModel> players;
}
