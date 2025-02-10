console.log("Run Script...");

const addStackMarginSteps = 30;
const showIntro = false;

document.addEventListener("DOMContentLoaded", () => {
    console.log("DOM Loaded");
    document.getElementById("startGameButton").onclick = async (event) => {
        event.preventDefault();
        event.stopPropagation();
        console.log("Game Start!");
        initVariables();
        initBackground().then();
        document.getElementById("GameContainer").classList.add("show");

        window.messageQueue = new MessageQueue('messageBox'); // GameContainer wird nach dem Dokument initialisiert

        await startBlackJack();
        console.log(balance);

        if(window.chipCount === 0){
            window.chipCount = await getExchange();
        }

        setTimeout(startGame, 1000);
    };
    document.getElementById("ExchangeButton").onclick = async (event) => {
        window.chipCount = await getExchange();
    };
});

const getExchange = ()=>new Promise(resolve => {
   document.getElementById("exchangePopupContainer").classList.add("show");
   document.getElementById("setExchangeButton").onclick = ()=>{
       const v = document.getElementById("exchangeValue").value;
       if(v < 1 || v > balance * 100) return;
       document.getElementById("exchangePopupContainer").classList.remove("show");
       return exchange(v).then(chipAmount => {
           if(chipAmount !== -1){
               resolve(chipAmount);
           }
       });
   }
   document.getElementById("abortExchangeButton").onclick = ()=>{
        document.getElementById("exchangePopupContainer").classList.remove("show");
        return chipCount;
    }
   const slider = document.getElementById("exchangeValue");
   slider.max = balance * 100;
   const output = document.getElementById("sValue");

   slider.oninput = function() {
       output.innerHTML = this.value + " ¢hips";
   };
});

const getBet = ()=> new Promise(resolve => {
    document.getElementById("betPopupContainer").classList.add("show");
    document.getElementById("setBetButton").onclick = ()=>{
        const v = document.getElementById("betValue").value;
        if(v < 1 || v > chipCount) return;
        document.getElementById("betPopupContainer").classList.remove("show");
        setTimeout(()=>resolve(Number(v)), 1500); //Weil Karten sonst schon umgedreht werden bevor transition von css verschwinden noch nicht fertig ist
    }
    const slider = document.getElementById("betValue");
    slider.max = chipCount;
    const output = document.getElementById("sliderValue");

    slider.oninput = function() {
        output.innerHTML = this.value + " ¢hips";
    };
});

const getInsuranceBet = ()=>new Promise(resolve => {
    overlaySetStatus(true);
    document.getElementById("insuranceBetPopupContainer").classList.add("show");
    document.getElementById("setBetButton").onclick = ()=>{
        document.getElementById("insuranceBetPopupContainer").classList.remove("show");
        overlaySetStatus(false);
        resolve(true);
    }
    document.getElementById("setBetButtonNo").onclick = ()=>{
        document.getElementById("insuranceBetPopupContainer").classList.remove("show");
        overlaySetStatus(false);
        resolve(false);
    }
});
const showResults  = (text)=>new Promise(resolve => { //Nicht wirklich Einsatz, sondern nur ja nein popup aber weil grad kein besserer name da war, besser als in den informatik klausuren, wo die methoden einfahc nur "ichmacheetwas" heißen
    document.getElementById("resultText").innerText = text;
    document.getElementById("result").classList.add("show");
    document.getElementById("playAgain").onclick = ()=>{
        window.location.reload();
        resolve();
    }
});
const userTakeCard = async (count = 1) => {
    if(userStack[runningStackId].restMaxCount !== -1) {
        if(count > userStack[runningStackId].restMaxCount) {
            console.log("Error! Try to Ziehen mehr als erlaubt!");
        } else userStack[runningStackId].restMaxCount -= count;
    }
    console.log("User Karten ziehen!");
    const cardsFromServer = [];
    let i = count;
    while(i > 0) {
        cardsFromServer.push(takeCard());
        i--;
    }
    console.log("Zeige Karten..");
    const {end, cards, promise} = showCardsInCenter(ziehenStack.takeCard(count));
    console.log("Karten Gezeigt!", cards);
    await promise;
    const flip = await Promise.all(cardsFromServer);
    //Karten gezogen aber noch nicht umgedreht und Rückseitenwert steht bereit weil server antwort
    console.log("Gezogen:",flip);
    //Gezogene Karten umdrehen
    let a = [];
    for (let i = 0; i < flip.length; i++) {
        console.log("i:",i,flip[i],cards[i]);
        a.push(cards[i].aufdecken(flip[i]));
    }
    await Promise.all(a);
    //Umdrehen Beendet

    const currentUserCard = [...Object.values(userStack[runningStackId].cards), ...cards];
    const canSplit = currentUserCard.length === 2 && currentUserCard[0].cardValue === currentUserCard[1].cardValue && userStack.length < 4; //Kann nur Splitten bei Zwei gleichen Karten und nur maximal 4 mal spittem
    const canDoubleDown = currentUserCard.length === 2 && userStack[runningStackId].restMaxCount === -1;
    const input = await new Promise(resolve => buttons.show({
        proceed: ()=>resolve("p"),
        ...(canSplit ? {
            split: ()=>resolve("s")
        } : {}),
        ...(canDoubleDown ? {
            double: ()=>resolve("d")
        } : {}),
    }));
    buttons.hide();

    end(); //Um Overlay schließen → Karten aus dem Vordergrund
    if(input === "d") {
        userStack[runningStackId].einsatz*=2;
        userStack[runningStackId].restMaxCount = 1;
        await serverDoubleDown();
    }
    if(input === "p" || input === "d") {
        const p = [];
        Promise.all(cards.map(card => p.push(userStack[runningStackId].add(card)))).then(()=>{
            userStack[runningStackId].startShowPoints();
        });
        await Promise.all(p);
        return true;
    }
    // else if(input === "s") {
    //     addUserStack();
    //     await Promise.all([
    //         ...(cards.length > 1 ? [userStack[runningStackId].add(cards[0])] : []),
    //         userStack[userStack.length-1].add(cards.pop()) //Hintere Karte auf neuen Stack
    //     ]); //Auf die entsprechenden Stapel verteilen mit Promise für Warten
    //     userStack[runningStackId].startShowPoints();
    //     userStack[userStack.length-1].startShowPoints();
    //
    //     userStack[userStack.length-1].einsatz = userStack[runningStackId].einsatz/2;
    //     userStack[runningStackId].einsatz = userStack[runningStackId].einsatz/2;
    //
    //     if(cards.length > 0 && cards[0].cardValue === 11) {
    //         userStack[runningStackId].restMaxCount = 1;
    //         userStack[runningStackId-1].restMaxCount = 1;
    //     }
    //     return true;
    // }

    console.log("Error! Unbekannte Eingabe:", input);
}

window.balance = 0;
window.chipCount = 0;
window.insuranceBet = 0; // Einsatz der auf Dealer Blackjack gewettet wurde
const startGame = async ()=> {

    const betPromise = getBet();

    if(showIntro) {
        await Promise.all([
            messageQueue.displayMultipleMessages([
                "Hallo!",
                "Willkommen bei Blackjack!",
                "Der Dealer ist dem Spiel beigetreten...",
                "Die Karten werden gemischt..."
            ]),
            mixAnimationDealer()
        ]);
        console.log("Einleitungs Animation fertig!");
    } else { // Wenn kein Intro Karten so füllen
        const z = await getGraphicsData();
        for(let i = 0; i < 40; i++) await ziehenStack.add(new GameCard(null, z["back"]), 0.05);
    }
    overlaySetStatus(false); // Overlay Kontrolle
    initDealerStack();

    addUserStack(); // Ersten User Stack vor erstem Splitten

    const gameInfoPromise = betPromise.then(bet => {
        userStack[0].einsatz = bet;
        return startNewBidding(bet);
    });

    await new Promise(resolve => setTimeout(resolve, 2000));
    await showDealerCards(null, 1);

    await new Promise(resolve => setTimeout(resolve, 1000));


    // if(dealerLeftStack.getOberste().cardValue === 11) {
    //     if((await getInsuranceBet())) {
    //         console.log("Set insurance...");
    //         await gameInfoPromise;
    //         console.log("Start Game");
    //         window.insuranceBet = (await betPromise)/2;
    //     }
    // }

    await userTakeCard(2);

    console.log("Gameplay loop");
    while(!endGame){
        await new Promise(resolve => setTimeout(resolve, 100));
        const eingabe = await new Promise(resolve => buttons.show({
            take: ()=>resolve("t"),
            stop: ()=>resolve("s")
        }));
        if(eingabe === "t") {
            await userTakeCard(1);
            if(userStack[runningStackId].restMaxCount === 0) await endStack();
        } else if(eingabe === "s") {
            buttons.hide();
            await messageQueue.displayMultipleMessages(["Der Stack wurde geschlossen!"]);
            overlaySetStatus(false);
            await endStack();
        }
    }

    const gameResultsPromise = getGameResults();
    await new Promise(resolve => setTimeout(resolve, 1000)); //Es soll gewartet werden, bis der Server die Ergebnisse geschickt hat, dieses warten soll mindetsens 500ms gehen, damit man auch den spielstand sehen kann
    const resultText = await gameResultsPromise;
    await Promise.all([
        messageQueue.displayMultipleMessages(["ENDE"]),
        showResults(resultText)
    ]);
}

const calculateResult = async (dealerCards, stackPoints)=>{
    console.log("Dealer Zieht:",dealerCards,stackPoints);
    await Object.values(dealerLeftStack.cards)[0].aufdecken(dealerCards.shift());
    await new Promise(resolve => setTimeout(resolve, 200));
    dealerCards.shift();
    for (const c of dealerCards) {
        await showDealerCards(c, 0);
    }
    Object.values(dealerLeftStack.cards).forEach(c => c.cardValue = 0);
    dealerLeftStack.getOberste().cardValue = stackPoints;
    dealerLeftStack.startShowPoints();
    console.log("All Fertig!");
}

window.endGame = false;
const endStack = ()=> new Promise(resolve => {
    buttons.hide();
    endStackServer(runningStackId).then();
    const continues = runningStackId < userStack.length-1;
    if(continues) runningStackId++;
    const {end} = focusElementWithOverlay(Object.values(userStack[runningStackId].cards));
    const dealerCardsPromise = continues ? null : getDealerCards();
    console.log("continues",continues);
    setTimeout(async ()=>{
        await end();
        console.log("ehre");
        if(continues) {
            setTimeout(async ()=>{
                resolve();
            }, 1000);
        } else {
            window.endGame = true;
            let delInfo = await dealerCardsPromise;
            console.log("delInfo:");
            await calculateResult(delInfo.objects,delInfo.stackValue);
            console.log("calculateResult");
            resolve();
        }
    }, 2500);
});

const showDealerCards = async (gameInfoPromise = null, countCoveredCards = 1, fastFlip = false)=>{
    dealerLeftStack.direktWertUpdate = false;
    if(!gameInfoPromise) gameInfoPromise = await dealerTakes();
    if(!fastFlip) {
        await ziehenStack.copyStack(dealerLeftStack, 1 + countCoveredCards);
        const gameInfo = await gameInfoPromise; //Wichtig: Mischen und Ziehen Animation auch vor einsatz abgeben, erst vor dem Umdrehen muss auf einSatz + Server Antwort gewartet werden
        console.log("Dealer First:", gameInfo);
        await dealerLeftStack.getOberste().aufdecken(gameInfo);
    } else {
        const card = ziehenStack.takeCard(1)[0];
        await Promise.all([dealerLeftStack.add(card), card.aufdecken(await gameInfoPromise)]);
    }
    console.log("Dealer hat gezogen!");
}


window.ziehenStack = null;
const initVariables = ()=>{
    window.ziehenStack = new Stack({x: window.innerWidth-cardWidth/2, y: 120}, "normal");
    window.userStack = [];
    window.runningStackId = 0;
}