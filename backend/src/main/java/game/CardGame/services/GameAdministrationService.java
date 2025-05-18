package game.CardGame.services;

import game.CardGame.models.GameModel;
import game.CardGame.models.PlayerModel;
import game.CardGame.repositories.GameRepository;
import game.CardGame.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.Set;

@Service
public class GameAdministrationService {

    private final int MIN = 100000;
    private final int MAX = 999999;

    private GameRepository gameRepository;
    private PlayerRepository playerRepository;

    @Autowired
    public GameAdministrationService(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    public String createMatch() {
        GameModel game = new GameModel();
        Random random = new Random();
        game.setMatchToken(random.nextInt(MAX + 1 - MIN) + MIN);
        //TODO: fehlt noch Prüfung ob der Token schon in der Datenbank existiert
        gameRepository.save(game);
        return game.getMatchToken();
    }

    public GameModel joinMatch(Integer matchToken, String playerToAdd) { //TODO: ich habe hier auch noch die Spielerid verlangt weil ich sonst nicht weiß welchen spieler ich zum Spiel hinzufügen soll, Äanderung ist noch nicht mit dem Swagger Editor synchronisiert
        GameModel game = gameRepository.findById(matchToken)
                .orElseThrow(() -> new RuntimeException("Invalid match token"));
        PlayerModel playerThatJoins = playerRepository.findById(playerToAdd)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        Set<PlayerModel> players = game.getPlayers();
        players.add(playerThatJoins);
        game.setPlayers(players);
        gameRepository.save(game);
        return game;
    }
}
