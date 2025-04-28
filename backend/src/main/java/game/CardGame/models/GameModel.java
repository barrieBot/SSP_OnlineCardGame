package game.CardGame.models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.Set;

@Table(name = "game")
@Entity
public class GameModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(nullable = false, length = 20, name = "game_status")
    private String gameStatus;

    @OneToOne
    @JoinColumn(nullable = false)
    private PlayerModel currentPlayer;

    @CreationTimestamp
    @Column(nullable = false, name = "created_at")
    private Date createdAt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "center_deck", nullable = false)
    private DeckModel centerDeck;

    @OneToOne
    @JoinColumn(name = "host", nullable = false)
    private PlayerModel host;

    @OneToMany
    @JoinColumn(name = "players", nullable = false)
    private Set<PlayerModel> players;







}
