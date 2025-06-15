package game.CardGame.configs;

import game.CardGame.dtos.WebSocketResponseDto;
import game.CardGame.enums.ResponseType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations gameStateSendTemplate;
    private final Map<String, Set<String>> game_sessions;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username").toString();
        String game_Code = headerAccessor.getSessionAttributes().get("game_code").toString();

        if(username != null && game_Code != null) {
            log.info("Disconnected from {}", username);

            var game_state = WebSocketResponseDto.builder()
                    .responseType(ResponseType.LEAVE_GAME)
                    .sender(username)
                    .id(game_Code)
                    .build();

            gameStateSendTemplate.convertAndSend("/topic/public",  game_state);
        }
    }
}
