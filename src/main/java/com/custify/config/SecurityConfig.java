package com.custify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Ressources statiques accessibles à tous
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/login").permitAll()

                        // Gestion des utilisateurs - ADMIN uniquement
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        // Dashboard accessible à tous les utilisateurs authentifiés
                        .requestMatchers("/dashboard").hasAnyRole("ADMIN", "COMMERCIAL")

                        // Gestion clients, prospects, opportunités, interactions - ADMIN et COMMERCIAL
                        .requestMatchers("/clients/**").hasAnyRole("ADMIN", "COMMERCIAL")
                        .requestMatchers("/prospects/**").hasAnyRole("ADMIN", "COMMERCIAL")
                        .requestMatchers("/opportunites/**").hasAnyRole("ADMIN", "COMMERCIAL")
                        .requestMatchers("/interactions/**").hasAnyRole("ADMIN", "COMMERCIAL")

                        // Toute autre requête nécessite une authentification
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied"));

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
