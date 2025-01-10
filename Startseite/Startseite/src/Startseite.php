<?php
session_start();
$isLoggedIn = isset($_SESSION['user']);
?>

<html>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" href="Startseite.css">
<head>
    <title>LetsGoGambling</title>
</head>
<body>

<div class="Header">
    <img id="Logo" src="../assets/Logo_Rand.png"/>

    <?php if (!$isLoggedIn): ?>
        <!-- Anmelden-Button nur anzeigen, wenn der Benutzer NICHT angemeldet ist -->
        <button id="Anmelden" onclick="window.location.href='/login.php'">
            <span class="text">Anmelden</span>
            <div class="form">
                <form action="/" method="POST">

                </form>
            </div>

        </button>
    <?php else: ?>
        <!-- Konto-Daten anzeigen, wenn der Benutzer angemeldet ist -->
        <div id="Account">
            <img id="PB" src="../assets/default-profile.png" alt="Profilbild">
            <h2 id="Guthaben">100000</h2>
            <img src="../assets/tilotaler_rand.png" id="Taler"/>
        </div>
    <?php endif; ?>
</div>

</body>
</html>