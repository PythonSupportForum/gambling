<?php
session_start();
$isLoggedIn = isset($_SESSION['user']);

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Überprüfen, ob die Anmeldedaten gesendet wurden
    $username = $_POST['username'] ?? '';
    $password = $_POST['password'] ?? '';

    if ($username === 'BENUTZERnarme' && $password === 'DRIN Passwormt') {
        $_SESSION['user'] = $username;
        $isLoggedIn = true;
        header('Location: /login.php');
        exit();
    } else {
        $error = 'Ungültige Anmeldedaten!';
    }
}

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
            <div class="form-container">
                <form action="./" method="POST">
                    <div class="form-group">
                        <label for="username">Benutzername</label>
                        <input type="text" id="username" name="username" required placeholder="Benutzername">
                    </div>
                    <div class="form-group">
                        <label for="password">Passwort</label>
                        <input type="password" id="password" name="password" required placeholder="Passwort">
                    </div>
                    <button type="submit" class="btn-submit">Einloggen</button>
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
<script>
    document.getElementById("Anmelden").onclick = ()=>{
        document.getElementById("Anmelden").classList.add("form");
    }
    document.addEventListener("click", (event) => {
        const anmeldenButton = document.getElementById("Anmelden");
        if (!anmeldenButton.contains(event.target)) {
            anmeldenButton.classList.remove("form");
        }
    });
</script>
</body>
</html>