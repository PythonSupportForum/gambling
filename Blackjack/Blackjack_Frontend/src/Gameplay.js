console.log("Run Script...");

const addierenStappelFaecherSteps = 30;
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





const getEinsatz = ()=>new Promise(resolve => {
   resolve(10000);
});
const userKarfenZiehen = async (count = 1) => {
    console.log("User Karten ziehen!");
    const e = [];
    while(count > 0) {
        e.push(karteZiehen());
        count--;
    }
    const kartenWertePromise = Promise.all(e);
    console.log("Zeige Karten..");
    const {end, cards, promise} = zeigeKartenInderMitteMitAuswahl(ziehenStack.karfenZiehen(e.length));
    console.log("Karen Gezeigt!", cards);
    await promise;
    const flip = await kartenWertePromise;
    //Karten gezogen aber noch nicht umgedreht und Rückseitenwert steht bereit weilserver anronw
    console.log("Gezogen:",flip);
    //Gezogene Karfen umdrehen
    let a = [];
    for (let i = 0; i < flip.length; i++) {
        console.log("i:",i,flip[i],cards[i]);
        a.push(cards[i].aufdecken(flip[i]));
    }
    await Promise.all(a);
    //Umdrehen Beended

    const aktuelleUserCarten = [...Object.values(userStack[runningStackId].cards), ...cards];
    const canSplitten = aktuelleUserCarten.length === 2 && aktuelleUserCarten[0].kartenwert === aktuelleUserCarten[1].kartenwert && userStack.length < 4; //Kann nur Spilitten bei Zwei gleichen Karten und nur maximal 4 mal spittem
    const eingabe = await new Promise(resolve => buttons.show({
        weiter: ()=>resolve("w"),
        ...(canSplitten ? {
            split: ()=>resolve("s")
        } : {})
    }));
    buttons.hide();

    end(); //Um Overlay u schließen => Karfenn aus dem Vordergurnf
    if(eingabe === "w") {
        const p = [];
        Promise.all(cards.map(card => p.push(userStack[runningStackId].add(card)))).then(()=>{
            userStack[runningStackId].startShowPoits();
        });
        await Promise.all(p);
        if(userStack[runningStackId].wert() > 21) await closeUserStappel(runningStackId);
        return true;
    } else if(eingabe === "s") {
        adduserStack();
        await Promise.all([
            ...(cards.length > 1 ? [userStack[runningStackId].add(cards[0])] : []),
            userStack[userStack.length-1].add(cards.pop()) //Hintere Karfe auf neien Stappel
        ]); //Auf die Entsprhecnen Stpapel verteilen mit Promise für Warten
        userStack[runningStackId].startShowPoits();
        userStack[userStack.length-1].startShowPoits();
        return true;
    }

    console.log("Error! Unbekannzr Eingabe:", eingabe);
}


const welcome = async ()=> {
    const einsatzPromise = getEinsatz();

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
    initDealerStappel();

    const gameInfoPromise = einsatzPromise.then(einsatz => startNewGame(einsatz));

    await new Promise(resolve => setTimeout(resolve, 2000));
    await showDealerKarten(gameInfoPromise.then(g => g.firstDealerCard), 0);

    await new Promise(resolve => setTimeout(resolve, 1000));
    
    await adduserStack(); //Ersten User Stack vor ertem Spitten

    await userKarfenZiehen(2);

    let running = true;
    console.log("Gameplay loop");
    while(running){
        await new Promise(resolve => setTimeout(resolve, 100));
        const eingabe = await new Promise(resolve => buttons.show({
            ziehen: ()=>resolve("z"),
            stop: ()=>resolve("s"),
            verdoppeln: ()=>resolve("v")
        }));
        if(eingabe === "z") {
            await userKarfenZiehen(1);
        } else if(eingabe === "s") {
            if(runningStackId < userStack.length) runningStackId++;
            await messageQueue.mehrneMesagesAnzeigen(["Der Stappel wurd abgeschlossen!"]);
            overlaySetStatus(false);
        } else if(eingabe === "v") {

        }
    }
}




const showDealerKarten = async (gameInfoPromise = null, countVerschlosseneKarten = 1)=>{
    if(!gameInfoPromise) gameInfoPromise = await karteZiehenDealer();
    await ziehenStack.copyStack(dealerLeftStack, 1+countVerschlosseneKarten);
    const gameInfo = await gameInfoPromise; //Wichtig: Mischen und Ziehen Animation auch Bevor einsatz abgeben, erst vor dem Umdrehen muss auf einSatz + Server Antwort gewartet werden
    console.log("Dealer First:", gameInfo.firstDealerCard);
    await dealerLeftStack.getOberste().aufdecken(gameInfo);

    console.log("Dealer hat gezogen!");
}


window.ziehenStack = null;
const initVariables = ()=>{
    window.ziehenStack = new Stack({x: window.innerWidth-cardWidth/2, y: 120}, "normal");
    window.userStack = [];
    window.runningStackId = 0;
}