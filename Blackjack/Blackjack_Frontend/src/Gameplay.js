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
   document.getElementById("seztenPoupupContainer").classList.add("show");
   document.getElementById("setEinsatzButton").onclick = ()=>{
       const v = document.getElementById("einsatzValue").value;
       if(v < 100 || v > 100000) return;
       document.getElementById("seztenPoupupContainer").classList.remove("show");
       setTimeout(()=>resolve(Number(v)), 1500); //Weil Karten SOnst schon umgedreht werden bevor transition von css verschwinden noch nicht fertig ist
   }
   const slider = document.getElementById("einsatzValue");
   const output = document.getElementById("sliderValue");

   slider.oninput = function() {
       output.innerHTML = this.value + " €";
   };
});
const getInsuranzeEinsatz = ()=>new Promise(resolve => { //Nicht wirklich eistaz sondern nur ja nein popup aber weil grad kein besserer name da war, besser als in den informatik klausuren, wo die methoden einfahc nur "ichmacheetwas" heißen
    overlaySetStatus(true);
    document.getElementById("seztenPoupupContainerInsuranze").classList.add("show");
    document.getElementById("setEinsatzIButton").onclick = ()=>{
        document.getElementById("seztenPoupupContainerInsuranze").classList.remove("show");
        overlaySetStatus(false);
        resolve(true);
    }
    document.getElementById("setEinsatzIButtonNo").onclick = ()=>{
        document.getElementById("seztenPoupupContainerInsuranze").classList.remove("show");
        overlaySetStatus(false);
        resolve(false);
    }
});
const showErgebisse = (text)=>new Promise(resolve => { //Nicht wirklich eistaz sondern nur ja nein popup aber weil grad kein besserer name da war, besser als in den informatik klausuren, wo die methoden einfahc nur "ichmacheetwas" heißen
    document.getElementById("ergebnissText").innerText = text;
    document.getElementById("ergebniss").classList.add("show");
    document.getElementById("playAgain").onclick = ()=>{
        window.location.reload();
        resolve();
    }
});
const userKarfenZiehen = async (count = 1) => {
    if(userStack[runningStackId].restMaxCount !== -1) {
        if(count > userStack[runningStackId].restMaxCount) {
            console.log("Error! Try to Ziehen mehr als erlaubt!");
        } else userStack[runningStackId].restMaxCount -= count;
    }
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
    const canVerdopeln = aktuelleUserCarten.length === 2 && userStack[runningStackId].restMaxCount === -1;
    const eingabe = await new Promise(resolve => buttons.show({
        weiter: ()=>resolve("w"),
        ...(canSplitten ? {
            split: ()=>resolve("s")
        } : {}),
        ...(canVerdopeln ? {
            verdoppeln: ()=>resolve("v")
        } : {}),
    }));
    buttons.hide();

    end(); //Um Overlay u schließen => Karfenn aus dem Vordergurnf
    if(eingabe === "v") {
        userStack[runningStackId].einsatz*=2;
        userStack[runningStackId].restMaxCount = 1;
        serverDoubleDown();
    }
    if(eingabe === "w" || eingabe === "v") {
        const p = [];
        Promise.all(cards.map(card => p.push(userStack[runningStackId].add(card)))).then(()=>{
            userStack[runningStackId].startShowPoits();
        });
        await Promise.all(p);
        if(userStack[runningStackId].wert() > 21) {
            await closeUserStappel(runningStackId);
            await endStappel();
        }
        return true;
    } else if(eingabe === "s") {
        adduserStack();
        await Promise.all([
            ...(cards.length > 1 ? [userStack[runningStackId].add(cards[0])] : []),
            userStack[userStack.length-1].add(cards.pop()) //Hintere Karfe auf neien Stappel
        ]); //Auf die Entsprhecnen Stpapel verteilen mit Promise für Warten
        userStack[runningStackId].startShowPoits();
        userStack[userStack.length-1].startShowPoits();

        userStack[userStack.length-1].einsatz = userStack[runningStackId].einsatz/2;
        userStack[runningStackId].einsatz = userStack[runningStackId].einsatz/2;

        if(cards.length > 0 && cards[0].kartenwert === 11) {
            userStack[runningStackId].restMaxCount = 1;
            userStack[runningStackId-1].restMaxCount = 1;
        }
        return true;
    }

    console.log("Error! Unbekannzr Eingabe:", eingabe);
}


window.insuranceEinsatz = 0; //Geld das auf dealer Blakcjack gewettet wurde
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
        for(let i = 0; i < 40; i++) await ziehenStack.add(new GameCard(null, z["back"]), 0.05);
    }
    overlaySetStatus(false); //Hell Machen, Scwarzes Di weg bzw. Unsichtbar opayisty:0
    initDealerStappel();

    adduserStack(); //Ersten User Stack vor ertem Spitten

    const gameInfoPromise = einsatzPromise.then(einsatz => {
        userStack[0].einsatz = einsatz;
        return startNewGame(einsatz);
    });

    await new Promise(resolve => setTimeout(resolve, 2000));
    await showDealerKarten(gameInfoPromise.then(g => g.firstDealerCard), 0);

    await new Promise(resolve => setTimeout(resolve, 1000));

    if(dealerLeftStack.getOberste().kartenwert === 11) {
        if((await getInsuranzeEinsatz())) {
            console.log("Set nsurnce,..");
            await gameInfoPromise;
            console.log("Startet Game,,");
            userStack.forEach(s => s.einsatz *= 0.5); //Alle User Stappel EInsatz halbieren => Gesmmt einsatz wir dhalbiert
            window.insuranceEinsatz = (await einsatzPromise)/2;
        }
    }

    await userKarfenZiehen(2);

    console.log("Gameplay loop");
    while(!jetztIstAllesVorbei){
        await new Promise(resolve => setTimeout(resolve, 100));
        const eingabe = await new Promise(resolve => buttons.show({
            ziehen: ()=>resolve("z"),
            stop: ()=>resolve("s")
        }));
        if(eingabe === "z") {
            await userKarfenZiehen(1);
            if(userStack[runningStackId].restMaxCount === 0) await endStappel();
        } else if(eingabe === "s") {
            buttons.hide();
            await messageQueue.mehrneMesagesAnzeigen(["Der Stappel wurd abgeschlossen!"]);
            overlaySetStatus(false);
            await endStappel();
        }
    }

    const gameResultsPromise = getGameResults();
    await new Promise(resolve => setTimeout(resolve, 1000)); //Es soll gewartet werden, bis der Server die Ergebnisse geschickt hat, dieses warten soll mindetsens 500ms gehen, damit man auch den spielstand sehen kann
    const ergebisText = await gameResultsPromise;
    await Promise.all([
        messageQueue.mehrneMesagesAnzeigen(["ENDE"]),
        showErgebisse(ergebisText)
    ]);
}

const berechneErgebiss = async (dealerKartenPromise)=>{
    const dealerCarten = await dealerKartenPromise;
    console.log("Dealer Zieht:", dealerCarten);
    dealerLeftStack.startShowPoits();
    for (const c of dealerCarten) {
        await showDealerKarten(c, 0, true);
        if(dealerLeftStack.wert() > 21) break; //Breche Ab, sobald der dealer mehr als 21 hat
    }
    console.log("All Fertig!");
}

window.jetztIstAllesVorbei = false;
const endStappel = ()=>new Promise(resolve => {
    buttons.hide();
    endStappelServer(runningStackId).then(()=>{});
    const esGehtWeter = runningStackId < userStack.length-1; //Ob noch win weitere Stappel durch Spiltlen vegübar ist
    if(esGehtWeter) runningStackId++;
    const {end} = focusElementWithOverlay(Object.values(userStack[runningStackId].cards));
    const dealerCardsPromise = esGehtWeter ? null : getDealerKarten();
    setTimeout(async ()=>{
        await end();
        if(esGehtWeter) {
            setTimeout(async ()=>{
                await userKarfenZiehen(1);
                resolve();
            }, 1000);
        } else {
            window.jetztIstAllesVorbei = true;
            await berechneErgebiss(dealerCardsPromise);
            resolve();
        }
    }, 2500);
});

const showDealerKarten = async (gameInfoPromise = null, countVerschlosseneKarten = 1, schnellDreien = false)=>{
    dealerLeftStack.direktWertUpdate = false;
    if(!gameInfoPromise) gameInfoPromise = await karteZiehenDealer();
    if(!schnellDreien) {
        await ziehenStack.copyStack(dealerLeftStack, 1+countVerschlosseneKarten);
        const gameInfo = await gameInfoPromise; //Wichtig: Mischen und Ziehen Animation auch Bevor einsatz abgeben, erst vor dem Umdrehen muss auf einSatz + Server Antwort gewartet werden
        console.log("Dealer First:", gameInfo.firstDealerCard);
        await dealerLeftStack.getOberste().aufdecken(gameInfo);
    } else {
        const card = ziehenStack.karfenZiehen(1)[0];
        await Promise.all([dealerLeftStack.add(card), card.aufdecken(await gameInfoPromise)]);
    }
    if(dealerLeftStack.wert() > 21) await closeDellerStappel(dealerLeftStack);
    console.log("Dealer hat gezogen!");
}


window.ziehenStack = null;
const initVariables = ()=>{
    window.ziehenStack = new Stack({x: window.innerWidth-cardWidth/2, y: 120}, "normal");
    window.userStack = [];
    window.runningStackId = 0;
}