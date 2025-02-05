const userStappelAbstandSplit = 400;

const showCardsInCenter = (cards, time = normalMoveTime)=>{
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
const mixAnimationDealer = () => {
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
        for(let i = 0; i < 40; i++) await aS.add(new GameCard(null, b.back), 0.2);
        console.log("Added Cards!");

        setTimeout(async ()=> {
            console.log("Start Copy!");
            await aS.copyStack(bS, -1, true, 0.2);
            console.log("Copy to Ziehen!");
            await bS.copyStack(ziehenStack, -1, false); //Am Ende der Einleitungs Animation fliegen alle Karten zu dem ZiehStack
            console.log("Copied to ziehen!");
            resolve();
        }, 1000);
    });
}
const initBackground = async ()=> {
    const t = (await getGraphicsData()).table;
    addDrawingThread((ctx)=>{
        ctx.drawImage(t, 0, 0, ctx.canvas.width, ctx.canvas.height);
    });
}
const addUserStack = ()=>{
    function calculateCardPositions(count = userStack.length+1, screenWidth = window.innerWidth, screenHeight = window.innerHeight, cardWidth = 100) { //Um die Player Stack gleihmäßig auf der unterhälfte des bildschirms zu verteilen => Berechnet Koords der Spappel
        const lowerHalfHeight = screenHeight / 2;
        const y = screenHeight - lowerHalfHeight / 2; // Mittig in der unteren Hälfte, von der höhe her
        // Berechne den Gesamtabstand, den alle Karten einnehmen
        const verfuegbarteBreie = screenWidth-count*cardWidth; //Wie viel Breie is abzuüglich von der Kerten breite noch na, Um die Lücken aufzteien zwischen den dingern
        console.log("B:",verfuegbarteBreie, count);
        const breteProGap = verfuegbarteBreie/(count+1); //Plus ein weil rechts und links noch platz
        console.log("G:", breteProGap);
        const positions = [];
        for (let i = 0; i < count; i++) {
            const x = breteProGap*(i+1)+cardWidth*(i+0.5);
            positions.push({ x, y });
        }
        return positions;
    }
    const newPositions = calculateCardPositions();
    console.log("New Positions:", newPositions);
    for(let i = 0; i < userStack.length; i++) userStack[i].moveTo(newPositions[i]);
    userStack.push(new Stack(newPositions[newPositions.length-1], "faecher", addStackMarginSteps)); //Letes Element für neuen Stack => Rechts angehangen
}
const initDealerStack = ()=>{
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
    window.dealerLeftStack = new Stack(positions[0], "faecher", addStackMarginSteps);
    window.dealerRightStack = new Stack(positions[1], "faecher", addStackMarginSteps);
}
const closeUserStack = async (index) => {
    const card = ziehenStack.takeCard(1)[0];
    userStack[index].showInfo = false;
    await Promise.all([
        userStack[index].add(card),
        card.changeSide("end")
    ]);
}
const closeDealerStack = async (s) => {
    await new Promise(resolve => setTimeout(resolve, 500)); //Um den Fail zu sehen
    const card = ziehenStack.takeCard(1)[0];
    s.showInfo = false;
    await Promise.all([
        s.add(card),
        card.changeSide("end")
    ]);
}