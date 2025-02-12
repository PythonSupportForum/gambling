<?php

error_reporting(E_ALL); // Alle Fehler anzeigen (Notices, Warnings, Fatal Errors usw.)
ini_set('display_errors', 1); // Fehler direkt auf der Webseite ausgeben
ini_set('display_startup_errors', 1); // Start-Fehler ebenfalls ausgeben

session_start();

if (!isset($_SESSION['kundeId'])) {
    header("Location: /register");
    exit();
}

header("Cache-Control: no-store, no-cache, must-revalidate, max-age=0");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");
header("Expires: Thu, 01 Jan 1970 00:00:00 GMT");

$conn = new mysqli('db.ontubs.de', 'carl', 'geilo123!', 'gambling');
if ($conn->connect_error) {
    die("Verbindung fehlgeschlagen: " . $conn->connect_error);
}

$userData = null;
if (isset($_SESSION['kundeId'])) {
    $kundeId = $_SESSION['kundeId'];
    $stmt = $conn->prepare("SELECT Kunden.id, Name, Vorname, bn, Geburtsdatum, token, SUM(t.Betrag) as amount FROM Kunden LEFT JOIN Transaktionen as t ON t.Kunden_ID = Kunden.id WHERE Kunden.id = ?;");
    if($stmt) {
        $stmt->bind_param("i", $kundeId);
        $stmt->execute();
        $result = $stmt->get_result();
        if ($result->num_rows === 1) $userData = $result->fetch_assoc();
        $stmt->close();
    } else echo "Error! ".$conn->error;
}

?>

<!doctype html>
<html lang="en-US">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Blackjack | Let's go Gambling!</title>
  <link rel="stylesheet" href="Game.css">
  <link rel="stylesheet" href="../../Startseite/Startseite/src/Startseite.css">
  <link rel="apple-touch-icon" sizes="180x180" href="../../Startseite/Startseite/assets/apple-touch-icon.png">
  <link rel="icon" type="image/png" sizes="32x32" href="../../Startseite/Startseite/assets/favicon-32x32.png">
  <link rel="icon" type="image/png" sizes="16x16" href="../../Startseite/Startseite/assets/favicon-16x16.png">
  <script src="Graphics.js"></script>
  <script src="GameHelper.js"></script>
  <script src="Gameplay.js"></script>
  <script src="Messages.js"></script>
  <script src="Communication.js"></script>
</head>
  <body>
    <div class="hidden" id="token"><?php echo htmlspecialchars($userData["token"]); ?></div>
    <div class="container">
      <h1>Blackjack Online</h1>
      <button id="startGameButton">Start Game</button>
    </div>
    <div class="GameContainer" id="GameContainer">
      <div class="ui">
        <button id="ExchangeButton">Umtauschen</button>
        <p id="ChipCount">Chips: 0¢</p>
      </div>
      <div class="elements-container">
        <div class="overlay" id="overlay"></div>
        <div class="message-container">
          <div class="message-box" id="messageBox"></div>
        </div>
        <canvas id="canvas"></canvas>
      </div>
      <div class="betPopupContainer" id="exchangePopupContainer">
        <div class="betPopup">
          <h2>Wie viele Chips willst du erwerben?</h2>
          <p>Du kannst deine TiloTaler in Chips umtauschen</p>
          <div class="slider-container">
            <input type="range" min="1" max="1" id="exchangeValue" value="1" class="slider">
            <div class="slider-value" id="sValue">1 ¢hips</div>
          </div>
          <div class="bb" id="exchangeButtons">
            <button id="abortExchangeButton">Abbrechen</button>
            <button id="setExchangeButton">Umtauschen</button>
          </div>
        </div>
      </div>
      <div class="betPopupContainer" id="wantsExchangePopupContainer">
      <div class="betPopup">
        <h2>Willst du Chips erwerben?</h2>
        <div class="bb">
          <button id="wantsButtonYes">Umtauschen</button>
          <button id="wantsButtonNo">Nicht Umtauschen</button>
        </div>
      </div>
    </div>
      <div class="betPopupContainer" id="betPopupContainer">
        <div class="betPopup">
          <h2>Welchen Betrag willst du setzen</h2>
          <div class="slider-container">
            <input type="range" min="1" max="100000" id="betValue" value="1" class="slider">
            <div class="slider-value" id="sliderValue">1 ¢hips</div>
          </div>
          <div class="bb">
            <button id="setBetButton">Setzen</button>
          </div>
        </div>
      </div>
      <div class="betPopupContainer" id="insuranceBetPopupContainer">
        <div class="betPopup">
          <h2>Insurance Setzen</h2>
          <p>Der Dealer hat als Erste Karte ein Ass gelegt. Du kannst eine zweite Nebenwette darauf abschließen, ob der Dealer ein Blackjack gezogen hat, also mit 2 karten genau 21 erreichen wird.</p>
          <div class="slider-container">
            <input type="range" min="1" max="100000" id="betIValue" value="1" class="slider">
            <div class="slider-value" id="sliderIValue">1 ¢hips</div>
          </div>
          <p>Sobald du den Button gedrückt hast Buchen wir das Geld von deinem Konto ab!</p>
          <div class="bb c">
            <button id="setBetButtonNo">Auf Insurance verzichten</button>
            <button id="setBetButtonYes">Nebenwette Abschließen</button>
          </div>
        </div>
      </div>
      <div class="betPopupContainer" id="result">
        <div class="betPopup">
          <h2>Herzlichen Glückwunsch</h2>
          <p>Das Spiel ist zu Ende!</p>
          <p id="resultText"></p>
          <div class="bb c">
            <button id="dontEnd">Noch eine Runde</button>
            <button id="end">Aufhören</button>
          </div>
        </div>
      </div>
    </div>
    <div class="buttons" id="buttonsDiv">
      <button class="proceed">Weiter</button>
      <button class="split">Split</button>
      <button class="take">Karte nehmen</button>
      <button class="stop">Bleiben</button>
      <button class="double">Double Down</button>
    </div>
  </body>
</html>