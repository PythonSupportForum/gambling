<meta charset = "UTF-8">
<meta lang="de">

<!--
CSS-Datei, Favicons und JS-Datei einbinden
-->
<head>
<link rel="stylesheet" href="Frontend.css">
<title>Blackjack | Let's go Gambling!</title>
<link rel="apple-touch-icon" sizes="180x180" href="/assets/apple-touch-icon.png">
<link rel="icon" type="image/png" sizes="32x32" href="/assets/favicon-32x32.png">
<link rel="icon" type="image/png" sizes="16x16" href="/assets/favicon-16x16.png">
<link rel="apple-touch-icon" sizes="180x180" href="/assets/apple-touch-icon.png">
<link rel="icon" type="image/png" sizes="32x32" href="/assets/favicon-32x32.png">
<link rel="icon" type="image/png" sizes="16x16" href="/assets/favicon-16x16.png">
<script src="blackjack-frontend.js"></script>
</head>
<body>
<!--
Obere Leiste mit Logo
-->
<header>
  <a href="/">
    <img id="Logo" src="/assets/Logo_Rand.png"/>
  </a>

  <!--
  Anmelde-Button
  -->
  <?php
  if(!isset($_SESSION["user_id"])){
    ?>
    <button onclick="window.location.href = '/login'" type="button" id="Anmelden">Anmelden</button>
    <?php
  }
  else{
    ?>
    <button onclick="window.location.href = '/logout'" type="button" id="Logout">Abmelden</button>
    <?php
  }
  ?>


<!-- Script für alternative Anmelde-Button-Logik (nicht fertig)
  <button onclick="window.location.href = '../../../Startseite/Startseite/index.php'" type="button" id="Anmelden">Anmelden</button>
  <script>
    const isLoggedIn = checkIfUserIsLoggedIn();
    const anmeldenButton = document.getElementById("Anmelden");
    if (isLoggedIn) {
    anmeldenButton.textContent = "Ausloggen";
    anmeldenButton.onclick = function() {
      window.location.href = "../../../Startseite/Startseite/src/logout.php";
    };
    } else {
    anmeldenButton.textContent = "Anmelden";
    anmeldenButton.onclick = function() {
      window.location.href = '../../../Startseite/Startseite/index.php'; // Oder deine Anmeldeseite
    };
  }
</script>
-->




  <div id="Kontostand">
    <img id="PB" src="/assets/default-profile.png" alt="Profilbild">
    <h2 id="Guthaben">100000</h2>
    <img src="/assets/tilotaler_rand.png" id="Taler"/>
  </div>

</header>

<main>
  <!--
      <h2 id="Connection Text" style="visibility: hidden;">Verbindung erstellt, Spiel gestartet</h2>
      <button onclick="handleStartButton()" id="StartButton">Spiel Starten</button>
    -->

  <div id="StartGame">
      <h1 id="Titel">Blackjack</h1>
      <button onclick="window.location.href = './Game.html'" type="button" class="gleichButton">Starten</button>
  </div>

</main>
  <footer class="footer">
    <div class="footer-content">
      <div class="footer-hint"><h4> >>> Für mehr Informationen hier hovern! <<< </h4></div>
	<h1 id="Footer-Title">Regelwerk:</h1>
	<div id="regelwerk">
    <ul class="liste">
      <li class="a-element">Ziel des Spiels: Erreiche eine Hand mit einem Wert von 21 oder so nah wie möglich an 21, ohne diese Zahl zu überschreiten</li>
      <li class="a-element">Kartenwerte:
        <ul class="liste">
          <li class="a-element">Zahlenkarten (2-10): Entsprechen ihrem Zahlenwert</li>
          <li class="a-element">Bildkarten (König, Dame, Bube): Jeweils 10 Punkte</li>
          <li class="a-element">Asse: Entweder 1 oder 11 Punkte, je nachdem, was für den Spieler vorteilhafter ist</li>
        </ul>
      </li>
      <li class="a-element">Spielverlauf:
        <ol class="liste">
          <li class="a-element">Jeder Spieler erhält zwei Karten, der Dealer ebenfalls, wobei eine Karte des Dealers offen ist</li>
          <li class="a-element">Spieler entscheiden, ob sie eine weitere Karte ("Hit") nehmen oder bei ihrer Hand bleiben ("Stand")</li>
          <li class="a-element">Wenn die Hand eines Spielers über 21 geht, verliert der Spieler automatisch ("Bust")</li>
          <li class="a-element">Wenn alle Spieler fertig sind, zeigt der Dealer seine zweite Karte. Der Dealer muss bei 16 oder weniger eine Karte ziehen, bei 17 oder mehr muss er stehen bleiben</li>
          <li class="a-element">Der Spieler gewinnt, wenn seine Hand näher an 21 ist als die des Dealers oder wenn der Dealer über 21 geht</li>
        </ol>
      </li>
      <li class="a-element">Blackjack: Ein Ass und eine 10-Punkte-Karte in den ersten beiden Karten, was eine sofortige Gewinnkombination ist (außer gegen ein Blackjack des Dealers)</li>
      <li class="a-element">Verdopplung (Double Down): Der Spieler kann seinen Einsatz verdoppeln und erhält nur eine weitere Karte</li>
      <li class="a-element">Teilen (Split): Wenn die ersten beiden Karten des Spielers den gleichen Wert haben, kann er sie in zwei separate Hände aufteilen und für jede Hand einen neuen Einsatz platzieren</li>
    </ul>
	
	</div>
    </div>
  </footer>


</body>
</html>