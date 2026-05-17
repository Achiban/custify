package com.custify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.custify.dto.MessageDto;
import com.custify.model.Conversation;
import com.custify.model.MessageOpportunite;
import com.custify.model.Opportunite;
import com.custify.model.Utilisateur;
import com.custify.model.enums.StatutOpportunite;
import com.custify.repository.ConversationRepository;
import com.custify.repository.MessageOpportuniteRepository;
import com.custify.repository.OpportuniteRepository;
import com.custify.repository.UtilisateurRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageOpportuniteRepository messageRepository;
    @Mock private OpportuniteRepository opportuniteRepository;
    @Mock private UtilisateurRepository utilisateurRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    private ChatService service;

    @BeforeEach
    void setUp() {
        service = new ChatService(conversationRepository, messageRepository,
                opportuniteRepository, utilisateurRepository, messagingTemplate);
    }

    @Test
    void getOrCreateConversationShouldReturnExistingConversation() {
        Opportunite opp = opportunite(1L);
        Utilisateur user = utilisateur(2L);
        Conversation existing = conversation(10L, opp);

        when(opportuniteRepository.findById(1L)).thenReturn(Optional.of(opp));
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(user));
        when(conversationRepository.findByOpportuniteId(1L)).thenReturn(Optional.of(existing));

        Conversation result = service.getOrCreateConversation(1L, 2L);

        assertEquals(10L, result.getId());
    }

    @Test
    void getOrCreateConversationShouldCreateNewConversationWhenNoneExists() {
        Opportunite opp = opportunite(1L);
        Utilisateur user = utilisateur(2L);

        when(opportuniteRepository.findById(1L)).thenReturn(Optional.of(opp));
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(user));
        when(conversationRepository.findByOpportuniteId(1L)).thenReturn(Optional.empty());
        when(conversationRepository.save(any())).thenAnswer(inv -> {
            Conversation c = inv.getArgument(0);
            c.setId(20L);
            return c;
        });

        Conversation result = service.getOrCreateConversation(1L, 2L);

        assertEquals(opp, result.getOpportunite());
        verify(conversationRepository).save(any(Conversation.class));
    }

    @Test
    void getOrCreateConversationShouldThrowWhenOpportuniteNotFound() {
        when(opportuniteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.getOrCreateConversation(99L, 1L));
    }

    @Test
    void getOrCreateConversationShouldThrowWhenUserNotFound() {
        Opportunite opp = opportunite(1L);
        when(opportuniteRepository.findById(1L)).thenReturn(Optional.of(opp));
        when(utilisateurRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.getOrCreateConversation(1L, 99L));
    }

    @Test
    void sendMessageShouldSaveMessageAndBroadcastViaWebSocket() {
        Opportunite opp = opportunite(1L);
        Conversation conv = conversation(10L, opp);
        Utilisateur expediteur = utilisateur(2L);
        expediteur.setNom("Alice");

        MessageOpportunite savedMsg = new MessageOpportunite();
        savedMsg.setId(55L);
        savedMsg.setConversation(conv);
        savedMsg.setExpediteur(expediteur);
        savedMsg.setContenu("Bonjour");

        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conv));
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(expediteur));
        when(messageRepository.save(any())).thenReturn(savedMsg);

        MessageDto result = service.sendMessage(10L, "Bonjour", 2L);

        assertEquals(55L, result.getId());
        assertEquals("Bonjour", result.getContenu());
        assertEquals("Alice", result.getNomExpediteur());
        verify(messagingTemplate).convertAndSend(eq("/topic/conversation.10"), any(MessageDto.class));
    }

    @Test
    void sendMessageShouldThrowWhenConversationNotFound() {
        when(conversationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.sendMessage(99L, "msg", 1L));
    }

    @Test
    void getMessagesShouldReturnPagedMessages() {
        Opportunite opp = opportunite(1L);
        Conversation conv = conversation(10L, opp);
        Utilisateur expediteur = utilisateur(2L);
        expediteur.setNom("Bob");

        MessageOpportunite msg = new MessageOpportunite();
        msg.setId(1L);
        msg.setConversation(conv);
        msg.setExpediteur(expediteur);
        msg.setContenu("Hello");

        Page<MessageOpportunite> page = new PageImpl<>(List.of(msg));

        when(conversationRepository.findById(10L)).thenReturn(Optional.of(conv));
        when(messageRepository.findByConversationIdOrderByCreatedAtDesc(eq(10L), any(Pageable.class)))
                .thenReturn(page);

        Page<MessageDto> result = service.getMessages(10L, 0, 20, 2L);

        assertEquals(1, result.getTotalElements());
        assertEquals("Hello", result.getContent().get(0).getContenu());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Utilisateur utilisateur(Long id) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        return u;
    }

    private Opportunite opportunite(Long id) {
        Opportunite o = new Opportunite();
        o.setId(id);
        o.setStatut(StatutOpportunite.DISPONIBLE);
        o.setTitre("T");
        o.setDescriptionComplete("D");
        o.setMontant(BigDecimal.TEN);
        return o;
    }

    private Conversation conversation(Long id, Opportunite opp) {
        Conversation c = new Conversation();
        c.setId(id);
        c.setOpportunite(opp);
        return c;
    }
}
