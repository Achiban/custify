<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tableau de bord | Custify CRM</title>
    <link rel="stylesheet" href="<c:url value='/css/auth.css' />">
</head>
<body class="app-body">
<main class="app-shell">
    <header class="topbar">
        <div>
            <span class="eyebrow">Session active</span>
            <h1>Bienvenue, ${utilisateur.nom}</h1>
            <p>Vous etes connecte en tant que ${utilisateur.role}.</p>
        </div>
        <form action="<c:url value='/logout' />" method="post">
            <button class="secondary" type="submit">Se deconnecter</button>
        </form>
    </header>
</main>
</body>
</html>
