console.log("Run Script...");

const showIntro = false;

document.addEventListener("DOMContentLoaded", () => {
    console.log("DOM Loaded!!!");
    document.getElementById("startGameButton").onclick = async (event) => {
        event.preventDefault();
        event.stopPropagation();
        console.log("Game Start!!!!");
        initVariables();
        initBackground();
        document.getElementById("GameContainer").classList.add("show");

        window.messageQueue = new MessageQueue('messageBox'); //Darmit weil continaer erst nach dom da

        setTimeout(welcome, 1000);
    };
});


const initBackground = async ()=> {
    const t = (await getGraphicsData()).table;
    addDrawingThread((ctx)=>{
        ctx.drawImage(t, 0, 0, ctx.canvas.width, ctx.canvas.height);
    });
}

window.ziehenStack = null;


const mischeAnimationDelaler = () => {
    function calculateRowCoordinates(screenHeight, cardHeight) { //Um Beide reihen Mittäg zu platzuieren
        const spacing = (screenHeight - 2 * cardHeight) / 3;
        const y1 = spacing;
        const y2 = 2 * spacing + cardHeight;
        return [y1, y2];
    }
    return new Promise(async resolve => {
        const b = await getGraphicsData();

        const [y1, y2] = calculateRowCoordinates(window.innerHeight, cardHeight);
        const aS = new Stack({x: 100, y: y1+cardHeight/2});
        const bS = new Stack({x: 100, y: y2+cardHeight/2});

        console.log("Add Cards:", b.back);
        for(let i = 0; i < 10; i++) await aS.add(new GameCard(null, b.back), 0.2);
        console.log("Added Cards!");

        setTimeout(async ()=> {
            console.log("Start Copy!");
            await aS.copyStack(bS, -1, true, 0.2);
            console.log("Copy to Ziehen!");
            await bS.copyStack(ziehenStack, -1, false); //Am Ende der Einleitungs Animation fliegen alle Karten zu dem ZiehStack
            console.log("Cpoied to ziehen!");
            resolve();
        }, 1000);
    });
}


const zeigeKartenInderMitteMitAuswahl = (cards, time = normalMoveTime)=>{
    function calculateCardPositions(cardCount, screenWidth = window.innerWidth, screenHeight = window.innerHeight, cardWidth = 100) {
        const y = screenHeight / 2; // Karten werden vertikal zentriert
        const gap = cardWidth + 60; // Abstand zwischen den Karten
        const startX = screenWidth / 2 - ((cardCount - 1) * gap) / 2; // Startposition für die erste Karte
        let positions = [];
        for (let i = 0; i < cardCount; i++) positions.push({ x: startX + i * gap, y: y });
        return positions;
    }
    const positions = calculateCardPositions(cards.length);
    const p = [];
    for(let i = 0; i < cards.length; i++) p.push(cards[i].moveTo(positions[i], time));
    return {...focusElementWithOverlay(cards), cards, promise: Promise.all(p)};
}
const welcome = async ()=>{
    if(showIntro) {
        await Promise.all([
            messageQueue.mehrneMesagesAnzeigen([
                "Hallo!",
                "Willkommen bei Blackjack!",
                "Von Carl und Florian!",
                "Der Dealer ist dem Spiel beigetreten...",
                "Die Karten werden gemischt..."
            ]),
            mischeAnimationDelaler()
        ]);
        console.log("Einleitungs Animation fertig!");
    } else { //Wenn kein Intro Karten so füllen
        const z = await getGraphicsData();
        for(let i = 0; i < 10; i++) await ziehenStack.add(new GameCard(null, z["back"]), 0.05);
    }
    overlaySetStatus(false); //Hell Machen, Scwarzes Di weg bzw. Unsichtbar opayisty:0


    //let temp = new GameCard(b.a_s, "spades", "a")

   // window.card = new GameCard(null, b.back, {x: 400, y: 200});

    await new Promise(resolve => setTimeout(resolve, 2000));
    await showZweiDealerKarten();

   // card.moveTo(700, 400);

    await new Promise(resolve => setTimeout(resolve, 1000));
    
    await adduserStack(); //Ersten User Stack vor ertem Spitten

    const {end, cards, promise} = zeigeKartenInderMitteMitAuswahl(ziehenStack.karfenZiehen(2));
    await promise;

    let running = true;
    const flip = ["10_c", "8_h"];

    let deckValues = [0,0,0,0];

    console.log("hier:",flip);

   // await ziehenStack.copyStack(userStack[runningStackId], 2); //Zwei Karten auf den Ersten user Stack zuehen


    console.log("g!")
    let a = [];
    for (let i = 0; i < flip.length; i++) {
        console.log("i:",i,flip[i],cards[i]);
        a.push(cards[i].changeSide(flip[i]));
    }
    await Promise.all(a);


    console.log("Gameplay loop");

    while(running){
        await new Promise(resolve => setTimeout(resolve, 100));
    }
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
    window.dealerLeftStack = new Stack(links);

    await ziehenStack.copyStack(dealerLeftStack, 2);

    await dealerLeftStack.getOberste().changeSide("4_d");

    console.log("Dealer hat gezogen!");
}


const adduserStack = ()=>{
    function calculateCardPositions(count = userStack.length+1, screenWidth = window.innerWidth, screenHeight = window.innerHeight, cardWidth = 100) { //Um die Player Stack gleihmäßig auf der unterhälfte des bildschirms zu verteilen => Berechnet Koords der Spappel
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
    const newPositions = calculateCardPositions();
    for(let i = 0; i < userStack.length; i++) userStack[i].moveTo(neuePositions[i]);
    userStack.push(new Stack(newPositions[newPositions.length-1])); //Letes Element für neuen Stack => Rechts angehangen
}
const initVariables = ()=>{
    window.ziehenStack = new Stack({x: window.innerWidth-cardWidth, y: 120}, "normal");
    window.userStack = [];
    window.runningStackId = 0;
}