package com.custify.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.custify.model.Utilisateur;
import com.custify.model.enums.Role;
import com.custify.repository.UtilisateurRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustifyUserDetailsServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    private CustifyUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustifyUserDetailsService(utilisateurRepository);
    }

    @Test
    void loadUserByUsernameShouldBuildSpringSecurityUser() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("admin@mail.com");
        utilisateur.setMotDePasse("hashed");
        utilisateur.setRole(Role.ADMIN);
        when(utilisateurRepository.findByEmail("admin@mail.com")).thenReturn(Optional.of(utilisateur));

        UserDetails userDetails = service.loadUserByUsername("admin@mail.com");

        assertEquals("admin@mail.com", userDetails.getUsername());
        assertEquals("hashed", userDetails.getPassword());
        assertEquals("ROLE_ADMIN", userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsernameShouldThrowWhenUserDoesNotExist() {
        when(utilisateurRepository.findByEmail("missing@mail.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@mail.com"));
    }
}
