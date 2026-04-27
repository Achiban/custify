## 🎯 Fonctionnalité : Enregistrement des Interactions

### 📋 Résumé
Cette nouvelle fonctionnalité permet aux commerciaux d'enregistrer et de suivre toutes les interactions (appels, emails, réunions, notes) liées à un client. Cela permet une traçabilité complète des communications et facilite la collaboration entre les équipes.

### ✨ Caractéristiques

#### Types d'interactions supportées
- **APPEL** : Appels téléphoniques
- **EMAIL** : Communications par email
- **REUNION** : Réunions en personne ou vidéo
- **MESSAGE** : Notes internes ou messages

#### Fonctionnalités principales
1. **Créer une interaction** : Enregistrer une nouvelle interaction avec un client
2. **Consulter l'historique** : Voir toutes les interactions passées avec un client
3. **Consulter les détails** : Accéder aux informations détaillées d'une interaction
4. **Modifier une interaction** : Mettre à jour une interaction existante
5. **Supprimer une interaction** : Archiver ou supprimer une interaction

#### Contrôle d'accès
- **Admin** : Accès complet à toutes les interactions
- **Commercial** : Accès limité à ses propres interactions et clients

### 📂 Fichiers créés

#### Backend
- `src/main/java/com/custify/dto/CreerInteractionRequest.java` - DTO pour créer une interaction
- `src/main/java/com/custify/service/InteractionService.java` - Service métier
- `src/main/java/com/custify/controller/InteractionController.java` - Contrôleur web

#### Frontend (Templates Thymeleaf)
- `src/main/resources/templates/interactions/form.html` - Formulaire de création/modification
- `src/main/resources/templates/interactions/list.html` - Liste des interactions avec timeline
- `src/main/resources/templates/interactions/details.html` - Détails d'une interaction

### 🛠 Installation

#### Étape 1 : Vérifier la base de données
Assurez-vous que la table `interaction` existe avec les colonnes suivantes :
```sql
CREATE TABLE interaction (
    id_inter BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(30) NOT NULL,
    date_heure DATETIME NOT NULL,
    resume TEXT,
    id_client BIGINT NOT NULL,
    id_user BIGINT NOT NULL,
    FOREIGN KEY (id_client) REFERENCES client(id_client),
    FOREIGN KEY (id_user) REFERENCES utilisateur(id_user)
);
```

#### Étape 2 : Compiler le projet
```bash
./mvnw clean compile
```

#### Étape 3 : Tester l'application
```bash
./mvnw spring-boot:run
```

### 📖 Guide d'utilisation

#### Pour un Commercial
1. Se connecter avec ses identifiants commerciaux
2. Aller dans "Clients" et sélectionner un client
3. Cliquer sur "Nouvelle interaction"
4. Remplir le formulaire :
   - Sélectionner le type (APPEL, EMAIL, REUNION, MESSAGE)
   - Saisir la date et heure
   - Décrire le contenu de l'interaction
5. Cliquer sur "Enregistrer l'interaction"
6. Consulter l'historique complet via "Historique des interactions"

#### Pour un Admin
- Accès complet à toutes les interactions de tous les clients
- Peut modifier ou supprimer les interactions de n'importe quel utilisateur
- Peut consulter l'historique complet des interactions du système

### 🔗 Routes disponibles

| Route | Méthode | Description |
|-------|---------|-------------|
| `/interactions/nouveau/{clientId}` | GET | Affiche le formulaire de création |
| `/interactions/creer` | POST | Enregistre une nouvelle interaction |
| `/interactions/client/{clientId}` | GET | Affiche l'historique des interactions d'un client |
| `/interactions/details/{id}` | GET | Affiche les détails d'une interaction |
| `/interactions/modifier/{id}` | GET | Affiche le formulaire de modification |
| `/interactions/supprimer/{id}` | POST | Supprime une interaction |

### 🔐 Permissions

- Les commerciaux ne peuvent créer que des interactions pour leurs propres clients
- Les commerciaux ne peuvent modifier que leurs propres interactions
- Les admins ont accès à toutes les interactions

### 📊 Données conservées

Pour chaque interaction, les informations suivantes sont enregistrées :
- Type d'interaction
- Date et heure
- Résumé/contenu de l'interaction
- Client associé
- Utilisateur qui a créé l'interaction

### 🎨 Interface utilisateur

#### Formulaire de création
- Dropdown pour sélectionner le type
- Date/heure picker (défault à l'heure actuelle)
- Textarea pour le résumé (max 1000 caractères)
- Boutons Enregistrer/Annuler

#### Liste des interactions (Timeline)
- Affichage chronologique des interactions
- Icônes spécifiques par type
- Actions rapides (voir, modifier, supprimer)
- Message si aucune interaction

#### Détails d'une interaction
- Informations complètes de l'interaction
- Détails du client
- Informations de l'utilisateur qui a créé
- Boutons d'action (modifier, supprimer)

### 🚀 Améliorations futures possibles

1. Support des interactions liées aux prospects
2. Ajout de pièces jointes (documents, images)
3. Ajout de tags/catégories
4. Notifications de rappel
5. Export en PDF/Excel
6. Intégration avec calendrier
7. Statistiques et analytics
8. Recherche et filtrage avancé

### ❓ FAQ

**Q: Puis-je modifier une interaction créée par quelqu'un d'autre?**
A: Non, sauf si vous êtes admin. Les commerciaux peuvent seulement modifier leurs propres interactions.

**Q: Les interactions supprimées peuvent-elles être récupérées?**
A: Non, elles sont définitivement supprimées. Pour archiver sans perdre les données, vous pouvez envisager d'ajouter un statut "archivé" au modèle.

**Q: Puis-je créer une interaction sans client?**
A: Non, chaque interaction doit être associée à un client.

### 📞 Support

Pour des questions ou des problèmes avec cette fonctionnalité, contactez l'équipe de développement.
