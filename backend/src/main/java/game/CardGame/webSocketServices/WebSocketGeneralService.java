package game.CardGame.webSocketServices;

import game.CardGame.models.PlayerModel;
import game.CardGame.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebSocketGeneralService {
    @Autowired
    private WebSocketUtilService webSocketUtilService;
    @Autowired
    private PlayerRepository playerRepository;

    public void reconnectWebSocket(String username, String gameCode, String newWebSocketId) throws IllegalArgumentException {
        PlayerModel player = webSocketUtilService.findPlayer(username, gameCode);
        if(player == null) {
            throw new IllegalArgumentException("Invalid Game Code");
        }
        player.setWebSocketId(newWebSocketId);
        playerRepository.save(player);
    }
}
