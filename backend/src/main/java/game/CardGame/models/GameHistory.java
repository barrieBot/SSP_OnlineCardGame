package game.CardGame.models;


import jakarta.persistence.*;

@Table(name = "gameHistory")
@Entity
public class GameHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false, name = "user_id")
    private UserModel userId;
}
