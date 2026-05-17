package com.custify.service;

import com.custify.dto.MessageDto;
import com.custify.model.Conversation;
import com.custify.model.MessageOpportunite;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.repository.ConversationRepository;
import com.custify.repository.MessageOpportuniteRepository;
import com.custify.repository.OpportuniteRepository;
import com.custify.repository.UtilisateurRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageOpportuniteRepository messageRepository;
    private final OpportuniteRepository opportuniteRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Conversation getOrCreateConversation(Long opportuniteId, Long userId) {
        Opportunite opportunite = opportuniteRepository.findById(opportuniteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunité introuvable"));

        // TODO: En environnement réel, récupérer l'affectation du commercial via AffectationService.
        // Ici on suppose que userId doit être soit clientVendeur soit le commercial affecté.
        // Pour l'instant on vérifie juste que l'utilisateur existe.
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        return conversationRepository.findByOpportuniteId(opportuniteId)
                .orElseGet(() -> {
                    Conversation newConv = new Conversation();
                    newConv.setOpportunite(opportunite);
                    return conversationRepository.save(newConv);
                });
    }

    @Transactional(readOnly = true)
    public Page<MessageDto> getMessages(Long conversationId, int page, int size, Long userId) {
        // Vérifier l'accès
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable"));

        Pageable pageable = PageRequest.of(page, size);
        Page<MessageOpportunite> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);

        return messages.map(msg -> MessageDto.builder()
                .id(msg.getId())
                .conversationId(msg.getConversation().getId())
                .expediteurId(msg.getExpediteur().getId())
                .nomExpediteur(msg.getExpediteur().getNom())
                .contenu(msg.getContenu())
                .luLe(msg.getLuLe())
                .createdAt(msg.getCreatedAt())
                .build());
    }

    @Transactional
    public MessageDto sendMessage(Long conversationId, String contenu, Long expediteurId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable"));

        Utilisateur expediteur = utilisateurRepository.findById(expediteurId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        MessageOpportunite message = new MessageOpportunite();
        message.setConversation(conversation);
        message.setExpediteur(expediteur);
        message.setContenu(contenu);

        MessageOpportunite savedMessage = messageRepository.save(message);

        MessageDto dto = MessageDto.builder()
                .id(savedMessage.getId())
                .conversationId(savedMessage.getConversation().getId())
                .expediteurId(savedMessage.getExpediteur().getId())
                .nomExpediteur(savedMessage.getExpediteur().getNom())
                .contenu(savedMessage.getContenu())
                .luLe(savedMessage.getLuLe())
                .createdAt(savedMessage.getCreatedAt())
                .build();

        // Envoyer le message via WebSocket
        messagingTemplate.convertAndSend("/topic/conversation." + conversationId, dto);

        return dto;
    }
}
