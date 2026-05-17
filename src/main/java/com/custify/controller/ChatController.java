package com.custify.controller;

import com.custify.dto.MessageDto;
import com.custify.model.Conversation;
import com.custify.service.ChatService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // TODO: Utiliser l'ID de l'utilisateur connecté via SecurityContextHolder. 
    // Pour l'instant, on passe l'ID via un paramètre de requête pour simuler.

    @GetMapping("/opportunites/{id}/conversation")
    public ResponseEntity<Conversation> getConversation(
            @PathVariable Long id,
            @RequestParam Long userId) {
        Conversation conversation = chatService.getOrCreateConversation(id, userId);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<Page<MessageDto>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam Long userId) {
        Page<MessageDto> messages = chatService.getMessages(id, page, size, userId);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            @RequestParam Long userId) {
        String contenu = payload.get("contenu");
        if (contenu == null || contenu.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        MessageDto sentMessage = chatService.sendMessage(id, contenu, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(sentMessage);
    }
}
