package com.custify.dto;

import com.custify.model.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModifierRoleRequest {

    @NotNull(message = "Le role est obligatoire")
    private Role role;
}
