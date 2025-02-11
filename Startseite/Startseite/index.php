<?php

error_reporting(E_ALL); // Alle Fehler anzeigen (Notices, Warnings, Fatal Errors usw.)
ini_set('display_errors', 1); // Fehler direkt auf der Webseite ausgeben
ini_set('display_startup_errors', 1); // Start-Fehler ebenfalls ausgeben

// Session starten
session_start();

header("Cache-Control: no-store, no-cache, must-revalidate, max-age=0");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");
header("Expires: Thu, 01 Jan 1970 00:00:00 GMT");


$conn = new mysqli('db.ontubs.de', 'carl', 'geilo123!', 'gambling');
if ($conn->connect_error) {
    die("Verbindung fehlgeschlagen: " . $conn->connect_error);
}

function generateAsciiToken($length = 64) {
    $characters = '';
    for ($i = 32; $i <= 126; $i++) $characters .= chr($i);
    $token = '';
    $maxIndex = strlen($characters) - 1;
    for ($i = 0; $i < $length; $i++) $token .= $characters[random_int(0, $maxIndex)];
    return $token;
}

function logActivity($conn, $kundeId = null) {
    $ip = $_SERVER['REMOTE_ADDR']; // IP-Adresse des Benutzers achtung, wenn Benutzer Proxy benutzt ist nicht die echte
    $url = $_SERVER['REQUEST_URI']; // Aufgerufene URL

    $stmt = $conn->prepare("INSERT INTO Logs (kundeId, ip, url) VALUES (?, ?, ?)");
    $stmt->bind_param("iss", $kundeId, $ip, $url);

    if (!$stmt->execute()) {
        error_log("Fehler beim Erstellen des Log-Eintrags: " . $stmt->error); //Wird nicht im Frontend ausgegeben aber kommt in die Apache Logs
    }

    $stmt->close();
}
logActivity($conn, isset($_SESSION['kundeId']) ? $_SESSION['kundeId'] : -1);

$errors = [];

if ($_SERVER['REQUEST_METHOD'] === 'POST' && !isset($_SESSION['kundeId'])) {
    if (isset($_POST['login'])) {
        // Login-Logik
        $bn = $_POST['bn'] ?? '';
        $pwd = $_POST['password'] ?? '';
        if (empty($bn) || empty($pwd)) {
            $errors[] = "Benutzername und Passwort sind erforderlich.";
        } else {
            $stmt = $conn->prepare("SELECT id, pwdhash, pwdsalt FROM Kunden WHERE bn = ?");
            $stmt->bind_param("s", $bn);
            $stmt->execute();
            $stmt->store_result();
            $stmt->bind_result($id, $pwdhash, $salt);
            if ($stmt->fetch() && password_verify($pwd . $salt, $pwdhash)) $_SESSION['kundeId'] = $id; // Benutzer-ID in der Session speichern
            else $errors[] = "Benutzername oder Passwort falsch.";

            echo "P:".$pwd . $salt." : ".$pwdhash;

            $stmt->close();
        }
    } elseif (isset($_POST['register'])) {
        // Register-Logik
        $name = $_POST['name'] ?? '';
        $vorname = $_POST['vorname'] ?? '';
        $bn = $_POST['bn'] ?? '';
        $pwd = $_POST['password'] ?? '';
        $geburtsdatum = $_POST['geburtsdatum'] ?? '';
        $stadt = $_POST['stadt'] ?? '';
        $postleitzahl = $_POST['postleitzahl'] ?? '';
        $straße = $_POST['straße'] ?? '';
        $hausnummer = $_POST['hausnummer'] ?? '';

        $errors = [];

        // Überprüfen, ob alle Felder ausgefüllt sind
        if (empty($name)) $errors[] = "Name ist erforderlich.";
        if (empty($vorname)) $errors[] = "Vorname ist erforderlich.";
        if (empty($bn)) $errors[] = "Benutzername ist erforderlich.";
        if (empty($pwd)) $errors[] = "Passwort ist erforderlich.";
        if (empty($geburtsdatum)) $errors[] = "Geburtsdatum ist erforderlich.";
        if (empty($stadt)) $errors[] = "Stadt ist erforderlich.";
        if (empty($postleitzahl)) $errors[] = "Postleitzahl ist erforderlich.";
        if (empty($straße)) $errors[] = "Straße ist erforderlich.";
        if (empty($hausnummer)) $errors[] = "Hausnummer ist erforderlich.";

        if (strlen($name) > 64 || preg_match('/\s/', $name)) {
            $errors[] = "Nachname darf keine Leerzeichen enthalten und maximal 64 Zeichen lang sein.";
        }

        if (strlen($vorname) > 64) {
            $errors[] = "Vorname darf maximal 64 Zeichen lang sein.";
        }

        if (strlen($bn) < 8 || strlen($bn) > 32) {
            $errors[] = "Benutzername muss zwischen 8 und 32 Zeichen lang sein.";
        }
        $today = new DateTime();
        $birthdate = new DateTime($geburtsdatum);
        $age = $today->diff($birthdate)->y;
        if ($age < 18 || $age > 120) {
            $errors[] = "Das Alter muss zwischen 18 und 120 Jahren liegen.";
        }
        if (empty($errors)) {
            // Salt generieren
            $salt = bin2hex(random_bytes(16));

            // Passwort mit Salt hashen
            $pwdhash = password_hash($pwd . $salt, PASSWORD_DEFAULT);
            echo "P:".$pwd . $salt." : ".$pwdhash;

            $t = generateAsciiToken();

            // Stadt in die City-Tabelle einfügen, falls sie noch nicht existiert
            $stmt = $conn->prepare("SELECT id FROM City WHERE name = ? AND postcode = ?");
            $stmt->bind_param("si", $stadt, $postleitzahl);
            $stmt->execute();
            $stmt->store_result();
            if ($stmt->num_rows === 0) {
                $stmt = $conn->prepare("INSERT INTO City (name, postcode) VALUES (?, ?)");
                $stmt->bind_param("si", $stadt, $postleitzahl);
                $stmt->execute();
                $cityId = $stmt->insert_id;
            } else {
                $stmt->bind_result($cityId);
                $stmt->fetch();
            }
            $stmt->close();

            // Adresse in die Adressen-Tabelle einfügen
            $stmt = $conn->prepare("SELECT id FROM Adressen WHERE straße = ? AND number = ? AND cityId = ?");
            $stmt->bind_param("sii", $straße, $hausnummer, $cityId);
            $stmt->execute();
            $stmt->store_result();
            if ($stmt->num_rows === 0) {
                // Adresse existiert noch nicht, also einfügen
                $stmt = $conn->prepare("INSERT INTO Adressen (straße, number, cityId) VALUES (?, ?, ?)");
                $stmt->bind_param("sii", $straße, $hausnummer, $cityId);
                $stmt->execute();
                $adresseId = $stmt->insert_id;
            } else {
                // Adresse existiert bereits, die ID abrufen
                $stmt->bind_result($adresseId);
                $stmt->fetch();
            }
            $stmt->close();
            // SQL-Query zum Einfügen des neuen Kunden
            $stmt = $conn->prepare("INSERT INTO Kunden (Name, Vorname, bn, pwdhash, pwdsalt, Geburtsdatum, adresseId, token) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            $stmt->bind_param("ssssssis", $name, $vorname, $bn, $pwdhash, $salt, $geburtsdatum, $adresseId, $t);
            if ($stmt->execute()) $_SESSION['kundeId'] = $stmt->insert_id;
            else $errors[] = "Fehler beim Anlegen des Kunden: " . $stmt->error;
            $stmt->close();
        }
    }
}

if(isset($_GET['logout'])) {
    unset($_SESSION['kundeId']);
}

$userData = null;
if (isset($_SESSION['kundeId'])) {
    $kundeId = $_SESSION['kundeId'];
    $stmt = $conn->prepare("SELECT Kunden.id, Name, Vorname, bn, Geburtsdatum, adresseId, SUM(t.Betrag) as amount FROM Kunden LEFT JOIN Transaktionen as t ON t.Kunden_ID = Kunden.id WHERE Kunden.id = ?;");
    $stmt->bind_param("i", $kundeId);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows === 1) $userData = $result->fetch_assoc();
    $stmt->close();
}

$runGame = $userData ? "./play" : "./register";
?>

<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Let's Gambling - Info Q2</title>
    <meta name="description" content="Let's Gambling – Ein sicheres Online-Casino, entwickelt im Informatik-Unterricht am MEG. Erleben Sie moderne Datenbankkommunikation und sicheres Glücksspiel.">
    <meta name="keywords" content="Glücksspiel, Online-Casino, Sicherheit, Datenbank, MEG, Informatik, Projekt">
    <meta name="author" content="Let's Gambling Team">
    <meta property="og:title" content="Let's Gambling – Sicheres Online-Casino">
    <meta property="og:description" content="Erleben Sie sicheres Glücksspiel und moderne Datenbankkommunikation, entwickelt im Informatik-Unterricht am MEG.">
    <meta property="og:image" content="https://deine-website.de/assets/cluster.png">
    <meta property="og:url" content="https://deine-website.de">
    <meta property="og:type" content="website">
    <link rel="stylesheet" href="<?php echo isset($_GET['no_frame']) ? "src/Startseite.css" : "style.css"; ?>">
    <link rel="stylesheet" href="popup.css">
    <link rel="apple-touch-icon" sizes="180x180" href="assets/apple-touch-icon.png">
    <link rel="icon" type="image/png" sizes="32x32" href="assets/favicon-32x32.png">
    <link rel="icon" type="image/png" sizes="16x16" href="assets/favicon-16x16.png">
    <link rel="apple-touch-icon" sizes="180x180" href="assets/apple-touch-icon.png">
    <link rel="icon" type="image/png" sizes="32x32" href="assets/favicon-32x32.png">
    <link rel="icon" type="image/png" sizes="16x16" href="assets/favicon-16x16.png">
</head>
<body>
<?php
if(!isset($_GET["no_frame"])) {
?>
<div class="centerf">
    <header>
        <div class="logo">
            <img src="assets/transparent.png" alt="Gambling Logo">
        </div>
        <div class="left" onclick="location.href='./'">Q2 INFO</div>
        <div class="buttons">
            <?php
            if(isset($_SESSION['kundeId'])) {
                ?>
                <h2><?php echo $userData["Name"]; ?></h2>
                <div class="amount"><?php echo htmlspecialchars(str_pad($userData["amount"]."" ?? 0, 5, "0", STR_PAD_LEFT)); ?> TT</div>
                <?php
            } else {
                ?>
                <button onclick="location.href='./?login=true'">Login</button>
                <button onclick="location.href='./?register=true'">Register</button>
                <?php
            }
            ?>
        </div>
    </header>
    <main>
        <?php
        }
        ?>
        <?php
        // Dynamisches Einbinden von Unterseiten
        if (isset($_GET['page'])) {
            $page = $_GET['page'];
            $pagePath = "pages/$page.php";
            if (file_exists($pagePath)) {
                require $pagePath;
            } else {
                echo "<p>Die angeforderte Seite existiert nicht.</p>";
            }
        } else {
            // Standardinhalt, wenn keine spezifische Seite angefordert wird
            echo '
            <div class="text">
                <h1>Lets go Gambling - Das sind wir</h1>
                <p>Lets go Gambling ist ein Projekt aus dem Informatik Unterricht am MEG bei Herrn Engels! Anhand dieser Demonstration soll gezeigt werden, wie eine sichere Datenbankkommunikation in der Praxis aussehen kann. Dazu haben wir ein Account System am Beispiel eines Online Casinos gezeigt. </p>
                <p>Viel Spaß beim Ausprobieren!</p>
                <div class="starfButton">
                    <a href="'.$runGame.'">Jetzt Ausprobieren</a>
                </div>
            </div>
            <div class="bild">
                <img src="assets/cluster.png" alt="Hochleistung Server von Intel und Lenovo!">
                <div class="sub">Wir von Lets go Gambling setzen auf sicherste Server Infrastruktur mit Hardware von HP und Lenovo!</div>
            </div>';
        }
        ?>

        <?php
        if(!isset($_GET["no_frame"])) {
        ?>
    </main>
</div>
    <footer>
        <p>&copy; 2023 Let's Gambling | <a href="./imprint">Impressum</a></p>
    </footer>
<?php
}
?>

<?php
// Popup für Login und Register
if ((isset($_GET['login']) || isset($_GET['register'])) && !$userData) {
    $type = isset($_GET['login']) ? 'Anmelden' : 'Account erstellen';
    echo "
    <div class='overlay' id='overlay'>
        <div class='popup'>
            <h2>$type</h2>";

    // Fehlermeldungen anzeigen
    if (!empty($errors)) {
        echo "<div class='errors'><ul>";
        foreach ($errors as $error) {
            echo "<li>$error</li>";
        }
        echo "</ul></div>";
    }

    // Formular für Login oder Register
    if (isset($_GET['login'])) {
        echo "
            <form action='./?login=true' method='post'>
                <input type='hidden' name='login' value='1'>
                <input type='text' name='bn' placeholder='Benutzername' value='" . ($_POST['bn'] ?? '') . "' required>
                <input type='password' name='password' placeholder='Passwort' required>
                <div class='buttons'>
                    <button onclick='event.preventDefault(); closePopup()'>Abbrechen</button>
                    <button type='submit'>Anmelden</button>
                </div>
            </form>";
    } else {
        echo "
            <form action='./?register=true' method='post'>
                <input type='hidden' name='register' value='1'>
                <input type='text' name='name' placeholder='Name' value='" . ($_POST['name'] ?? '') . "' required>
                <input type='text' name='vorname' placeholder='Vorname' value='" . ($_POST['vorname'] ?? '') . "' required>
                <input type='text' name='bn' placeholder='Benutzername' value='" . ($_POST['bn'] ?? '') . "' required>
                <input type='password' name='password' placeholder='Passwort' required>
                <input type='date' name='geburtsdatum' placeholder='Geburtsdatum' value='" . ($_POST['geburtsdatum'] ?? '') . "' required>
                <input type='text' name='stadt' placeholder='Stadt' value='" . ($_POST['stadt'] ?? '') . "' required>
                <input type='text' name='postleitzahl' placeholder='Postleitzahl' value='" . ($_POST['postleitzahl'] ?? '') . "' required>
                <input type='text' name='straße' placeholder='Straße' value='" . ($_POST['straße'] ?? '') . "' required>
                <input type='text' name='hausnummer' placeholder='Hausnummer' value='" . ($_POST['hausnummer'] ?? '') . "' required>
                <div class='buttons'>
                    <button onclick='event.preventDefault(); closePopup()'>Abbrechen</button>
                    <button type='submit'>Account erstellen</button>
                </div>
            </form>";
    }

    echo "
        </div>
    </div>
    <script>
        document.getElementById('overlay').style.display = 'flex';
        function closePopup() {
            document.getElementById('overlay').style.display = 'none';
        }
    </script>";
}
?>
</body>
</html>