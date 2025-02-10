<!DOCTYPE html>
<html lang="de">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="index.css">
        <title>Gambling Website | Slot-Spiel</title>
        <script>
            window.token = "2";
        </script>
        <script src="game.js"></script>

    </head>
    <body>
        <main>

            <header>
                <a href="../../../Startseite/Startseite/index.php">
                    <img id="Logo" src="../assets/Logo_Rand.png"/>
                </a>
                <div class="u">
                    <h1>Slot-Machine - Play and WIN!</h1>
                </div>



                <?php
                if(isset($_SESSION["user_id"])){
                    ?>
                    <button onclick="window.location.href = '../../../Startseite/Startseite/index.php'" type="button" id="Anmelden">Anmelden</button>
                    <?php
                }
                else{
                    ?>
                    <button onclick="window.location.href = '../../../Startseite/Startseite/src/logout.php'" type="button" id="Logout">Anmelden</button>
                    <?php
                }
                ?>






            </header>
            <div class="machine-container">
                <div class="machine">
                    <div class="t">
                        <span class="a">SLOTS</span>
                        <span class="b">One Round 4TT</span>
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