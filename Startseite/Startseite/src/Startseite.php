<?php
session_start();
$isLoggedIn = isset($_SESSION['user']);
$error = null;
$username = $_POST['username'] ?? '';
$password = $_POST['password'] ?? '';

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_POST['username'])) {
    // Überprüfen, ob die Anmeldedaten gesendet wurden


    if ($username === 'BENUTZERnarme' && $password === 'DRIN Passwormt') {
        $_SESSION['user'] = $username;
        $isLoggedIn = true;

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
        <div id="Anmelden" class="<?php if( isset($_POST['username'])) echo "form"; ?>">
            <span class="text">Anmelden</span>
            <div class="form-container">
                <form action="" method="POST">
                    <h1 style="color: white;">Anedlnen bei LETTS' GMLE</h1>
                    <?php if (isset($error) && $error): ?>
                        <p class="error" style="font-size: 16px;"><?= htmlspecialchars($error) ?></p>
                    <?php endif; ?>
                    <div class="form-group">
                        <label for="username" style="color: white;">Benutzername</label>
                        <input type="text" id="username" name="username" value="<?php echo htmlspecialchars($username); ?>" required placeholder="Benutzername">
                    </div>
                    <div class="form-group">
                        <label for="password" style="color: white;">Passwort</label>
                        <input type="password" id="password" name="password" value="<?php echo htmlspecialchars($password); ?>" required placeholder="Passwort">
                    </div>
                    <button type="submit" class="btn-submit">Einloggen</button>
                </form>
            </div>
        </div>
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