<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connexion | Custify CRM</title>
    <link rel="stylesheet" href="<c:url value='/css/auth.css' />">
</head>
<body>
<main class="auth-shell">
    <section class="auth-brand">
        <span class="eyebrow">Custify CRM</span>
        <h1>Connectez-vous en toute securite a votre espace CRM.</h1>
        <p>
            Accedez a vos fonctionnalites selon votre role: administration
            ou gestion commerciale.
        </p>

        <div class="role-preview">
            <article>
                <h2>Administrateur</h2>
                <p>Gestion des acces, supervision et parametres du CRM.</p>
            </article>
            <article>
                <h2>Commercial</h2>
                <p>Suivi des prospects, opportunites et relation client.</p>
            </article>
        </div>
    </section>

    <section class="auth-card">
        <div class="card-header">
            <h2>Connexion</h2>
            <p>Utilisez votre email professionnel et votre mot de passe.</p>
        </div>

        <c:if test="${param.error != null}">
            <div class="flash flash-error">
                Email ou mot de passe invalide.
            </div>
        </c:if>
        <c:if test="${param.logout != null}">
            <div class="flash flash-success">
                Votre session a ete fermee avec succes.
            </div>
        </c:if>

        <form action="<c:url value='/login' />" method="post" class="auth-form">
            <label for="username">Adresse email</label>
            <input id="username" name="username" type="email" placeholder="vous@custify.local" required autofocus>

            <label for="password">Mot de passe</label>
            <input id="password" name="password" type="password" placeholder="Votre mot de passe" required>

            <button type="submit">Se connecter</button>
        </form>

        <div class="demo-box">
            <h3>Comptes de demonstration</h3>
            <p><strong>Admin:</strong> admin@custify.local / Admin123!</p>
            <p><strong>Commercial:</strong> commercial@custify.local / Commercial123!</p>
        </div>
    </section>
</main>
</body>
</html>
