package com.custify.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class ParticipantCanalId implements Serializable {

    @Column(name = "id_canal")
    private Long canalId;

    @Column(name = "id_user")
    private Long utilisateurId;
}
