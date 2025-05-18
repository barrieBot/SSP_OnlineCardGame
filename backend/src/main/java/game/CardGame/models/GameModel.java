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

    @Column(unique = true, name = "game_token")
    private String matchToken;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<PlayerModel> getPlayers() {
        return players;
    }

    public void setPlayers(Set<PlayerModel> players) {
        this.players = players;
    }

    public PlayerModel getHost() {
        return host;
    }

    public void setHost(PlayerModel host) {
        this.host = host;
    }

    public DeckModel getCenterDeck() {
        return centerDeck;
    }

    public void setCenterDeck(DeckModel centerDeck) {
        this.centerDeck = centerDeck;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public PlayerModel getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(PlayerModel currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public String getGameStatus() {
        return gameStatus;
    }

    public void setGameStatus(String gameStatus) {
        this.gameStatus = gameStatus;
    }

    public String getMatchToken() {
        return matchToken;
    }

    public void setMatchToken(Integer gameToken) {
        this.matchToken = Integer.toString(gameToken);
    }
}
