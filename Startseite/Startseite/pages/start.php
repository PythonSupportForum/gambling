<?php

error_reporting(E_ALL); // Alle Fehler anzeigen (Notices, Warnings, Fatal Errors usw.)
ini_set('display_errors', 1); // Fehler direkt auf der Webseite ausgeben
ini_set('display_startup_errors', 1); // Start-Fehler ebenfalls ausgeben

// Session starten
session_start();

$conn = new mysqli('db.ontubs.de', 'carl', 'geilo123!', 'gambling');
if ($conn->connect_error) {
    die("Verbindung fehlgeschlagen: " . $conn->connect_error);
}

$userData = null;
if (isset($_SESSION['kundeId'])) {
    $kundeId = $_SESSION['kundeId'];
    $stmt = $conn->prepare("SELECT Kunden.id, Name, Vorname, bn, Geburtsdatum, Addresse, SUM(t.Betrag) as amount FROM Kunden LEFT JOIN Transaktionen as t ON t.Kunden_ID = Kunden.id WHERE Kunden.id = ?;");
    $stmt->bind_param("i", $kundeId);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows === 1) $userData = $result->fetch_assoc();
    $stmt->close();
}

?>

<header>
    <a href="./">
        <img id="Logo" src="assets/Logo_Rand.png"/>
    </a>
    <?php
    if(!$userData) {
        ?>
        <button onclick="window.location.href = './?login=true';" type="button" class="Anmelden">Anmelden</button>
        <button onclick="window.location.href = './?register=true';" type="button" class="Anmelden">Konto Erstellen</button>
        <?php
    } else {
        ?>
        <div id="Account">
            <h2 id="Guthaben">100000</h2>
            <img src="assets/tilotaler_rand.png" id="Taler"/>
        </div>
        <?php
    }
    ?>
</header>
<main>
    <div id="Blackjack">
        <button onclick="window.location.href = './blackjack'" class="gleichButton">Blackjack</button>
    </div>
    <div  id="Slots">
        <button onclick="window.location.href = './slots'" class="gleichButton">Slots</button>
    </div>
</main>
<footer class="footer">
    <div class="footer-content">
        <div class="footer-hint"> >>> Für mehr Informationen hier hovern! <<< </div>
        <h1>Impressum</h1>
        <p><strong>Verantwortlich für das Projekt "Let's go Gambling":</strong></p>
        <p>Tilo Behnke, Carl Göb, Silas Hoffmann, Florian Reschke, Lars Ashauer, Atal Raufi, Mina Henke, Alexander Spürck</p>
        <p><strong>Projekt des Q2 Informatik-Kurses</strong></p>
        <p>Max Ernst Gymnasium</p><br>
        <p><strong>Rechtsvertretung:</strong></p>
        <p>Tilo Ulf Behnke</p>
        <p>Berliner Ring 51, 50321 Brühl, Deutschland</p>
        <p><strong>E-Mail:</strong> <a class = "links" href="mailto:kontakt@tilo-behnke.de">kontakt@tilo-behnke.de</a></p>
        <p><strong>Telefon:</strong> <a class = "links" href="tel:+4915752250315">+49 1575 2250315</a></p>
        <p><strong></strong>Haftungsausschluss</strong></p>
        <p>Wir übernehmen keine Haftung für externe Inhalte, auf die unser Projekt verweist. Die Verantwortung für verlinkte Seiten liegt bei den jeweiligen Betreibern.</p><br>
        <p><strong>Urheberrecht</strong></p>
        <p>Für Urheberrechtsbeschwerden ist das Max Ernst Gymnasium verantwortlich.</p>
        <p>© 2025 Let's Gambling - Schülerprojekt</p>
    </div>
</footer>