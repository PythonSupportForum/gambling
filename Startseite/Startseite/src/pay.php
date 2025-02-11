<?php

error_reporting(E_ALL); // Alle Fehler anzeigen (Notices, Warnings, Fatal Errors usw.)
ini_set('display_errors', 1); // Fehler direkt auf der Webseite ausgeben
ini_set('display_startup_errors', 1); // Start-Fehler ebenfalls ausgeben


if (!isset($_SESSION['kundeId'])) {
    header("Location: /login");
    exit();
}

$conn = new mysqli('db.ontubs.de', 'carl', 'geilo123!', 'gambling');
if ($conn->connect_error) {
    die("Verbindung fehlgeschlagen: " . $conn->connect_error);
}


$text = "";

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $betrag = $_POST['betrag'] ?? 0;
    $kundeId = $_SESSION['kundeId'];

    // Überprüfen, ob der Betrag gültig ist
    if ($betrag <= 0) {
        die("Ungültiger Betrag.");
    }

    // Neue Transaktion in die Datenbank einfügen
    $stmt = $conn->prepare("INSERT INTO Transaktionen (Kunden_ID, Betrag, type) VALUES (?, ?, 'aufladen')");
    $stmt->bind_param("id", $kundeId, $betrag);

    if ($stmt->execute()) $text = "Guthaben erfolgreich aufgeladen: ".$betrag."TT";
    else $text = "Fehler beim Aufladen: " . $stmt->error;

    $stmt->close();
}

$userData = null;

$kundeId = $_SESSION['kundeId'];
$stmt = $conn->prepare("SELECT Kunden.id, Name, Vorname, SUM(t.Betrag) as amount FROM Kunden LEFT JOIN Transaktionen as t ON t.Kunden_ID = Kunden.id WHERE Kunden.id = ?;");
if($stmt) {
    $stmt->bind_param("i", $kundeId);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows === 1) $userData = $result->fetch_assoc();
    $stmt->close();
} else echo "Fehler bei SQL Prepare: ".$conn->error;

?>

<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Guthaben aufladen</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #0073e6, #00b8e6);
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
        }
        .form-container {
            background: white;
            padding: 2rem;
            border-radius: 10px;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
            transform: rotate(-2deg);
            max-width: 400px;
            width: 100%;
        }
        .form-container h2 {
            color: #0073e6;
            text-align: center;
            margin-bottom: 1.5rem;
        }
        .form-container input[type="number"] {
            width: 100%;
            padding: 0.75rem;
            margin-bottom: 1rem;
            border: 1px solid #ccc;
            border-radius: 5px;
            font-size: 1rem;
        }
        .form-container button {
            width: 100%;
            padding: 0.75rem;
            background: #0073e6;
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 1rem;
            cursor: pointer;
        }
        .form-container button:hover {
            background: #005bb5;
        }

        p {
            color: red;
        }
    </style>
</head>
<body>
<div class="form-container">
    <h2>Guthaben aufladen - <?php echo htmlspecialchars($userData["Vorname"])." ".htmlspecialchars($userData["Name"]); ?></h2>
    <form action="/pay" method="POST">
        <p><?php echo htmlspecialchars( $text); ?></p>
        <input type="number" name="betrag" placeholder="Betrag in TT" required min="1">
        <button type="submit">Jetzt aufladen</button>
    </form>
</div>
</body>
</html>