<?php

error_reporting(E_ALL); // Alle Fehler anzeigen (Notices, Warnings, Fatal Errors usw.)
ini_set('display_errors', 1); // Fehler direkt auf der Webseite ausgeben
ini_set('display_startup_errors', 1); // Start-Fehler ebenfalls ausgeben

session_start();

if (!isset($_SESSION['kundeId'])) {
    header("Location: /");
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
<!DOCTYPE html>
<html lang="de">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="index.css">
        <title>Gambling Website | Slot-Spiel</title>
        <script>
            window.token = null;
        </script>
        <script src="game.js"></script>
    </head>
    <body>
        <div class="hidden" id="token"><?php echo htmlspecialchars($userData["token"]); ?></div>
        <main>
            <header>
                <a href="/">
                    <img id="Logo" src="assets/Logo_Rand.png"/>
                </a>
                <div class="u">
                    <h1>Slot-Machine - Play and WIN!</h1>
                </div>
            </header>
            <div class="machine-container">
                <div class="machine">
                    <div class="t">
                        <span class="a">SLOTS</span>
                        <span class="b">One Round 1000TT</span>
                    </div>
                    <div class="row">
                        <div class="rad">
                            <span>x</span>
                        </div>
                        <div class="rad">
                            <span>x</span>
                        </div>
                        <div class="rad">
                            <span>x</span>
                        </div>
                    </div>
                    <div class="c">
                        <button id="play" onclick="play();">🎰 SPIN</button>
                    </div>
                </div>
                <div class="balance" id="balance">---</div>
            </div>
        </main>
        <div class="info">

        </div>
    </body>
</html>