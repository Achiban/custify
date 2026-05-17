package com.custify.controller;

import com.custify.service.ChatService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatService chatService;

    @MessageMapping("/chat/{conversationId}/send")
    public void handleMessageViaWebSocket(
            @DestinationVariable Long conversationId,
            Map<String, Object> payload) {
        
        String contenu = (String) payload.get("contenu");
        // On devrait récupérer l'ID de l'utilisateur depuis l'authentification WebSocket (Principal).
        // En attendant, on suppose que le client l'envoie dans le payload.
        Long userId = ((Number) payload.get("userId")).longValue();

        if (contenu != null && !contenu.trim().isEmpty()) {
            // Le service va s'occuper de sauvegarder et de diffuser sur /topic/conversation.{id}
            chatService.sendMessage(conversationId, contenu, userId);
        }
    }
}
