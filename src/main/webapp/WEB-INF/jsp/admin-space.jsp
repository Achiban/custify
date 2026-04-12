<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Espace administrateur | Custify CRM</title>
    <link rel="stylesheet" href="<c:url value='/css/auth.css' />">
</head>
<body class="app-body">
<main class="app-shell">
    <header class="topbar">
        <div>
            <span class="eyebrow">ADMIN</span>
            <h1>Espace administrateur</h1>
            <p>Pilotez les acces et la supervision globale du CRM.</p>
        </div>
        <div class="topbar-actions">
            <a class="secondary-link" href="<c:url value='/dashboard' />">Retour au tableau de bord</a>
            <form action="<c:url value='/logout' />" method="post">
                <button class="secondary" type="submit">Se deconnecter</button>
            </form>
        </div>
    </header>

    <section class="dashboard-card">
        <h2>Utilisateur connecte: ${utilisateur.nom}</h2>
        <p>Email: ${utilisateur.email}</p>
        <p>Role applicatif: ADMIN</p>
    </section>
</main>
</body>
</html>
