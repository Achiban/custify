package com.custify.security;

import com.custify.model.Utilisateur;
import com.custify.repository.UtilisateurRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustifyUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public CustifyUserDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable: " + username));

        return User.withUsername(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .authorities(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name()))
                .build();
    }
}
