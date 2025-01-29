console.log("Run Script...");

document.addEventListener("DOMContentLoaded", () => {
    console.log("DOM Loaded!!!");
    document.getElementById("startGameButton").onclick = async (event) => {
        event.preventDefault();
        event.stopPropagation();
        console.log("Gami Stariiii!!!!");
        initVariables();
        initBackground();
        document.getElementById("GameContainer").classList.add("show");

        window.messageQueue = new MessageQueue('messageBox'); //Darmit weil continaer erst nach dom da

        setTimeout(welcome, 1000);
    };
});


const initBackground = async ()=> {
    const t = (await getGrafiksData()).table;
    addDrawingThread((ctx)=>{
        ctx.drawImage(t, 0, 0, ctx.canvas.width, ctx.canvas.height);
    });
}

window.ziehenStappel = null;


const mischeAnimationDelaler = () => {
    function calculateRowCoordinates(screenHeight, cardHeight) { //Um Beide reihen Mittäg zu platzuieren
        const spacing = (screenHeight - 2 * cardHeight) / 3;
        const y1 = spacing;
        const y2 = 2 * spacing + cardHeight;
        return [y1, y2];
    }
    return new Promise(async resolve => {
        const b = await getGrafiksData();

        const [y1, y2] = calculateRowCoordinates(window.innerHeight, cardHeight);
        const aS = new Stappel({x: 100, y: y1+cardHeight/2});
        const bS = new Stappel({x: 100, y: y2+cardHeight/2});

        console.log("Add Cards:", b.back);
        for(let i = 0; i < 10; i++) await aS.add(new GameCard(null, b.back), 0.2);
        console.log("Added Cards!");

        setTimeout(async ()=> {
            console.log("Start Copy!");
            await aS.copyStappel(bS, -1, true, 0.2);
            console.log("Copy to Ziehen!");
            await bS.copyStappel(ziehenStappel, -1, false); //Am Ende der Einleitungs Animation fliegen alle Karten zu dem Ziehstappel
            console.log("Cpoied to ziehen!");
            resolve();
        }, 1000);
    });
}

const welcome = async ()=>{
    await Promise.all([
        messageQueue.mehrneMesagesAnzeigen([
            "Hallo!",
            "Willkommen bei Blackjack!",
            "Von Carli und Florian!",
            "Der Dealer ist dem Spiel beigetreten...",
            "Die Karfen werden gemischt...",
            "Du wirst sterben!"
        ]),
        mischeAnimationDelaler()
    ]);
    console.log("Einleitungs Animation fertig!");

    //let temp = new GameCard(b.a_s, "spades", "a")

   // window.card = new GameCard(null, b.back, {x: 400, y: 200});

    overlaySetStatus(false); //Hell Machen, Scwarzes Di weg bzw. Unsichtbar opayisty:0

    await new Promise(resolve => setTimeout(resolve, 2000));

    await showZweiDealerKarten();

   // card.moveTo(700, 400);

    await addUserStappel(); //Ersten User Stappel vor ertem Spitten

}

const showZweiDealerKarten = async ()=>{
    function calculateCardPositions(screenWidth = window.innerWidth, screenHeight = window.innerHeight) { //Um Posotion für Dealer Karten auf dem Bildschirm
        const upperHalfHeight = screenHeight / 2;
        const y = upperHalfHeight / 2; // Mittig in der oberen Hälfte
        const gap = cardWidth + 30; // Abstand zwischen den Karten
        return [
            { x: screenWidth / 2 - gap / 2, y: y }, // Position der linken Karte
            { x: screenWidth / 2 + gap / 2, y: y }, // Position der rechten Karte
        ];
    }
    const positions = calculateCardPositions();

    const links = positions[0];
    window.dealerLinkerStappel = new Stappel(links);

    await ziehenStappel.copyStappel(dealerLinkerStappel, 2);

    const b = await getGrafiksData();

    console.log("SHOW:", b["4_d"]);

    dealerLinkerStappel.getOberste().changeSide(b["4_d"]);

    console.log("Dealer hat gezogen!");


}


const addUserStappel = ()=>{
    function calculateCardPositions(count = userStappel.length+1, screenWidth = window.innerWidth, screenHeight = window.innerHeight, cardWidth = 100) { //Um die Player Stappel gleihmäßig auf der unterhälfte des bildschirms zu verteilen => Berechnet Koords der Spappel
        const lowerHalfHeight = screenHeight / 2;
        const y = screenHeight - lowerHalfHeight / 2; // Mittig in der unteren Hälfte, von der höhe her
        // Berechne den Gesamtabstand, den alle Karten einnehmen
        const totalGap = (count - 1) * (cardWidth + 30); // 30 ist der Abstand zwischen den Karten
        const startX = (screenWidth - totalGap) / 2; // Startposition => Position der Katen
        const positions = [];
        for (let i = 0; i < count; i++) {
            const x = startX + i * (cardWidth + 30); // Berechne lassen die xPosition für jede Karte
            positions.push({ x, y });
        }
        return positions;
    }
    const neuePositions = calculateCardPositions();
    for(let i = 0; i < userStappel.length; i++) userStappel[i].moveTo(neuePositions[i]);
    userStappel.push(new Stappel(neuePositions[neuePositions.length-1])); //Letes Element für neuen Stappel => Rechts angehangen
}
const initVariables = ()=>{
    window.ziehenStappel = new Stappel({x: window.innerWidth-cardWidth, y: 120}, "normal");
    window.userStappel = [];
    window.runningStappelId = 0;
}