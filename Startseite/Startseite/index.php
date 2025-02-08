<?php
// Session starten
session_start();

$conn = new mysqli('db.ontubs.de', 'carl', 'geilo123!', 'gambling');
if ($conn->connect_error) {
    die("Verbindung fehlgeschlagen: " . $conn->connect_error);
}

function logActivity($conn, $kundeId = null) {
    $ip = $_SERVER['REMOTE_ADDR']; // IP-Adresse des Benutzers achtung, wenn Bneutzerp Poxy benutzt ist nicht die echte
    $url = $_SERVER['REQUEST_URI']; // Aufgerufene URL

    $stmt = $conn->prepare("INSERT INTO Logs (kundeId, ip, url) VALUES (?, ?, ?)");
    $stmt->bind_param("iss", $kundeId, $ip, $url);

    if (!$stmt->execute()) {
        error_log("Fehler beim Erstellen des Log-Eintrags: " . $stmt->error); //Wird nicht im forntent ausgegeben aber kommt in die Apache Logs
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
            // Benutzer in der Datenbank suchen
            $stmt = $conn->prepare("SELECT id, pwdhash FROM Kunden WHERE bn = ?");
            $stmt->bind_param("s", $bn);
            $stmt->execute();
            $stmt->store_result();
            $stmt->bind_result($id, $pwdhash);

            if ($stmt->fetch() && password_verify($pwd, $pwdhash)) {
                // Login erfolgreich
                $_SESSION['kundeId'] = $id; // Benutzer-ID in der Session speichern
            } else {
                $errors[] = "Benutzername oder Passwort falsch.";
            }

            $stmt->close();
        }
    } elseif (isset($_POST['register'])) {
        // Register-Logik
        $name = $_POST['name'] ?? '';
        $vorname = $_POST['vorname'] ?? '';
        $bn = $_POST['bn'] ?? '';
        $pwd = $_POST['password'] ?? '';
        $geburtsdatum = $_POST['geburtsdatum'] ?? '';
        $addresse = $_POST['addresse'] ?? '';

        // Validierung
        if (empty($name)) $errors[] = "Name ist erforderlich.";
        if (empty($vorname)) $errors[] = "Vorname ist erforderlich.";
        if (empty($bn)) $errors[] = "Benutzername ist erforderlich.";
        if (empty($pwd)) $errors[] = "Passwort ist erforderlich.";
        if (empty($geburtsdatum)) $errors[] = "Geburtsdatum ist erforderlich.";
        if (empty($addresse)) $errors[] = "Adresse ist erforderlich.";

        // Wenn keine Fehler vorhanden sind, Daten in die Datenbank einfügen
        if (empty($errors)) {
            // Passwort hashen
            $pwdhash = password_hash($pwd, PASSWORD_DEFAULT);

            // SQL-Query zum Einfügen des neuen Kunden
            $stmt = $conn->prepare("INSERT INTO Kunden (Name, Vorname, bn, pwdhash, Geburtsdatum, Addresse) VALUES (?, ?, ?, ?, ?, ?)");
            $stmt->bind_param("ssssss", $name, $vorname, $bn, $pwdhash, $geburtsdatum, $addresse);

            if ($stmt->execute()) {
                $_SESSION['kundeId'] = $stmt->insert_id;
            } else {
                $errors[] = "Fehler beim Anlegen des Kunden: " . $stmt->error;
            }

            $stmt->close();
        }
    }
}

if(isset($_GET['logout'])) {
    unset($_SESSION['kundeId']);
}
?>

<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Glücksspiel Startseite</title>
    <meta name="description" content="Let's Gambling – Ein sicheres Online-Casino, entwickelt im Informatik-Unterricht am MEG. Erleben Sie moderne Datenbankkommunikation und sicheres Glücksspiel.">
    <meta name="keywords" content="Glücksspiel, Online-Casino, Sicherheit, Datenbank, MEG, Informatik, Projekt">
    <meta name="author" content="Let's Gambling Team">
    <!-- Open Graph Meta Tags für Social Media -->
    <meta property="og:title" content="Let's Gambling – Sicheres Online-Casino">
    <meta property="og:description" content="Erleben Sie sicheres Glücksspiel und moderne Datenbankkommunikation, entwickelt im Informatik-Unterricht am MEG.">
    <meta property="og:image" content="https://deine-website.de/assets/cluster.png">
    <meta property="og:url" content="https://deine-website.de">
    <meta property="og:type" content="website">
    <link rel="stylesheet" href="style.css">
</head>
<body>
<div class="centerf">
    <header>
        <div class="logo">
            <img src="assets/transparent.png" alt="Gambling Logo">
        </div>
        <div class="left" onclick="location.href='./'">Q2 INFO</div>
        <div class="buttons">
            <button onclick="location.href='./login'">Login</button>
            <button onclick="location.href='./register'">Register</button>
        </div>
    </header>
    <main>
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
                <h1>Let\'s Gambling - Das sind wir</h1>
                <p>Let\'s Gambling ist ein Projekt aus dem Informatik Unterricht am MEG bei Herrn Engels! Anhand dieser Demonstration soll gezeigt werden, wie eine sichere Datenbankkommunikation in der Praxis aussehen kann. Dazu haben wir ein Account System am Beispiel eines Online Casinos gezeigt. </p>
                <p>Viel Spaß beim Ausprobieren!</p>
                <div class="starfButton">
                    <a href="./register">Jetzt Ausprobieren</a>
                </div>
            </div>
            <div class="bild">
                <img src="assets/cluster.png" alt="Hochleistung Server von Intel und Lenovo!">
                <div class="sub">Wir von Lets Gambling setzen auf sicherste Server Infrastruktur mit Hardware von HP und Lenovo!</div>
            </div>';
        }
        ?>
    </main>
</div>

<?php
// Popup für Login und Register
if (isset($_GET['login']) || isset($_GET['register'])) {
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
            <form action='./register' method='post'>
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
            <form action='./login' method='post'>
                <input type='hidden' name='register' value='1'>
                <input type='text' name='name' placeholder='Name' value='" . ($_POST['name'] ?? '') . "' required>
                <input type='text' name='vorname' placeholder='Vorname' value='" . ($_POST['vorname'] ?? '') . "' required>
                <input type='text' name='bn' placeholder='Benutzername' value='" . ($_POST['bn'] ?? '') . "' required>
                <input type='password' name='password' placeholder='Passwort' required>
                <input type='date' name='geburtsdatum' placeholder='Geburtsdatum' value='" . ($_POST['geburtsdatum'] ?? '') . "' required>
                <input type='text' name='addresse' placeholder='Addresse' value='" . ($_POST['addresse'] ?? '') . "' required>
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
<footer>
    <p>&copy; 2023 Let's Gambling | <a href="./imprint">Impressum</a></p>
</footer>
</html>