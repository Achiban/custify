package com.custify.controller;

import com.custify.dto.CreerUtilisateurRequest;
import com.custify.dto.UtilisateurResponse;
import com.custify.service.UtilisateurService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping
    public ResponseEntity<UtilisateurResponse> creer(@Valid @RequestBody CreerUtilisateurRequest requete) {
        UtilisateurResponse cree = utilisateurService.creer(requete);
        return ResponseEntity.created(URI.create("/api/users/" + cree.getId())).body(cree);
    }

    @GetMapping
    public ResponseEntity<List<UtilisateurResponse>> lister() {
        return ResponseEntity.ok(utilisateurService.listerTous());
    }
}
