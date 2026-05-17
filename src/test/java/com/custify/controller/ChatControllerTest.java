package com.custify.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.custify.dto.MessageDto;
import com.custify.model.Conversation;
import com.custify.model.Opportunite;
import com.custify.model.enums.StatutOpportunite;
import com.custify.service.ChatService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    private ChatController controller;

    @BeforeEach
    void setUp() {
        controller = new ChatController(chatService);
    }

    @Test
    void getConversationShouldReturnOkWithConversation() {
        Opportunite opp = opportunite(1L);
        Conversation conv = conversation(10L, opp);
        when(chatService.getOrCreateConversation(1L, 2L)).thenReturn(conv);

        ResponseEntity<Conversation> response = controller.getConversation(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().getId());
    }

    @Test
    void getMessagesShouldReturnOkWithPagedMessages() {
        MessageDto msg = MessageDto.builder()
                .id(1L)
                .conversationId(10L)
                .contenu("Hello")
                .build();
        Page<MessageDto> page = new PageImpl<>(List.of(msg));
        when(chatService.getMessages(10L, 0, 20, 2L)).thenReturn(page);

        ResponseEntity<Page<MessageDto>> response = controller.getMessages(10L, 0, 20, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("Hello", response.getBody().getContent().get(0).getContenu());
    }

    @Test
    void sendMessageShouldReturnCreatedWithDto() {
        MessageDto dto = MessageDto.builder()
                .id(5L)
                .conversationId(10L)
                .contenu("Bonjour")
                .build();
        when(chatService.sendMessage(10L, "Bonjour", 2L)).thenReturn(dto);

        ResponseEntity<MessageDto> response = controller.sendMessage(10L, Map.of("contenu", "Bonjour"), 2L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Bonjour", response.getBody().getContenu());
    }

    @Test
    void sendMessageShouldReturnBadRequestWhenContentIsNull() {
        ResponseEntity<MessageDto> response = controller.sendMessage(10L, Map.of(), 2L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void sendMessageShouldReturnBadRequestWhenContentIsBlank() {
        ResponseEntity<MessageDto> response = controller.sendMessage(10L, Map.of("contenu", "  "), 2L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

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
