package com.custify.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private Long conversationId;
    private Long expediteurId;
    private String nomExpediteur;
    private String contenu;
    private LocalDateTime luLe;
    private LocalDateTime createdAt;
}
