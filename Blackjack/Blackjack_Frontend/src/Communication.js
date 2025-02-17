const serverURL = "ws://127.0.0.1:8082";

window.socket = null;
window.approved = false;
window.listener = {};
window.results = {};

function addListener(type, callback = ()=>{}) {
    console.log("Add Listner:", type, callback);
    if(!(type in listener)) listener[type] = [];
    listener[type].push(callback);
    while((type in results) && listener[type].length > 0 ) listener[type].shift()(...results[type]);
}
function getListener(type, save) {
    console.log("Get Listner:", type, save);
    if(save) return (...a)=>{
        results[type] = a;
        if(type in listener) listener[type].forEach(c => c(...a));
        listener[type] = [];
    }
    if(type in listener) return listener[type].shift();
    console.log("Error! Listener Array leer!", type);
    return ()=>{};
}

window.connectSocket = async ()=>{
    console.log("Connect Socket!");
    if(!("socketPromise" in window)) window.socketPromise = new Promise(resolve => {
        console.log("Create Socket!");

        const socket = new WebSocket(serverURL);

        let clientToken = document.getElementById("token").innerText || "2"; //Fals PHP keinen Token übergeben hat als Backup!

        // Verbindung geöffnet
        socket.onopen = () => {
            console.log("On Open!");
            setTimeout(()=>{
                resolve(socket); //Promise wird aufgelöst => For Sachen die von Verbindung Abhängen und auf Vebrindung warten müssen
            }, 500);
        };

         // Nachricht vom Server empfangen → Allgemein für alle Nachrichten (Websocket protokoll)
        socket.onmessage = async (event) => {
            console.log("Nachricht vom Server:", event.data);
            const msg = event.data.toString().toLowerCase();
            if (event.data.toString().toLowerCase().indexOf('acc') === 0) {
                console.log("ID: " + clientToken.toString());
                socket.send("ID:" + clientToken.toString());
                window.approved = true;
            }

            let coat;
            let value;
            let points;

            if (msg.startsWith("card:")) {
                let sub = msg.substring("card:".length);

                let part = sub.split(",");

                coat = part[0].substring("c:".length);

                value = part[1].substring("v:".length);

                points = part[2].substring("p:".length);

                console.log("God Cart:", value, coat, points);

                getListener("card")({
                    type: value + "_" + coat,
                    points: points
                });
            } else if (msg.startsWith("dealercard:")) {
                let sub = msg.substring("DealerCard:".length);

                let part = sub.split(",");

                coat = part[0].substring("c:".length);

                value = part[1].substring("v:".length);

                points = part[2].substring("p:".length);

                console.log("points", points, "card", value + "_" + coat);

                getListener("dealercard")({
                    type: value + "_" + coat,
                    points: points
                });
            } else if (msg.startsWith("dealercards:")) {
                console.log("Empfängt Dealer Karten");

                let cardObjects = [];
                let sub = msg.substring("DealerCards:".length).split(">");

                let cards = sub[0].split(";");
                for (let singleCard of cards) {
                    let part = singleCard.split(",");

                    coat = part[0].substring("c:".length);

                    value = part[1].substring("v:".length);
                    cardObjects.push({type: value + "_" + coat, points: 0});
                }

                getListener("dealercards")({objects: cardObjects, stackValue: parseInt(sub[1])}, true);
            } else if (msg.startsWith("chipupdate:")) {
                let updatedChipCount = parseInt(msg.substring("DealerCards:".length - 1));
                if (updatedChipCount >= 0) {
                    console.log("Chip updated for: " + updatedChipCount);
                    document.getElementById("ChipCount").innerText = "Chips: " + updatedChipCount + "¢";
                    console.log(updatedChipCount);
                    getListener("chipupdate")(updatedChipCount);
                } else {
                    document.getElementById("ChipCount").innerText = "Chips: " + chipCount + "¢";
                    getListener("chipupdate")(chipCount);
                    console.log("Got Chipupdate!");
                    console.error("Auszahlung nicht erfolgt");
                }
            } else if (msg.startsWith("text:")) {
                setGameResultText(msg.substring("text:".length));
                window.endGame = true; // Falls noch nicht true zum Beispiel, weil vorzeitiger Abbruch durch Double Down
            }


            else if (msg.startsWith("bal:")) {
                window.balance = parseFloat(msg.substring("bal:".length));
                console.log(balance);
                getListener("bal")(balance);
            } else if (msg.startsWith("stack:")) {
                let part = msg.substring("stack:".length).split(",");

                let points = parseInt(part[0].substring("p:".length));

                let state;
                state = part[1].substring("s:".length).includes("true");

                getListener("stack")({
                    points: points,
                    state: state
                });
            } else if (msg.startsWith("ask:")) {
                const textArray = msg.substring("ask:".length).split(":");
                const text = textArray[0];
                console.log("Server Asked Frontend: ", text);
                let answered = false;
                const answer = (t) => {
                    if(answered) return; //Nur einmal debugging
                    answered = true;
                    console.log("Send Answer:", text, t); //Anfrage und Antwort darauf ausgeben
                    socket.send("answer:" + t);
                }
                switch (text) {
                    case "insurance":
                        const setInsurance = await getInsuranceBet();
                        if(!setInsurance) answer("false;0");
                        else {
                            await gameInfoPromise; //Sicherstellen, dass schon gesetzt wurde, eig. Unnötig aber aus Prinzip
                            answer("true;"+(setInsurance).toString()); //Wie viel Insurance
                        }
                        break;
                    case "double":
                        console.log("Frage nach Double..");
                        const r = await buttons.addDynamicYesOrNoButton("double");
                        console.log("Double Answer erhalten:", r);
                        answer(r?"true":"false");
                        break;
                    case "split":
                        console.log("Frage nach Spilt..");
                        const doSplit = await buttons.addDynamicYesOrNoButton("split");
                        console.log("Split Answer erhalten:", doSplit);
                        answer(doSplit?"true":"false");
                        break;
                    case "end":
                        console.log("Frage nach Ende..");
                        document.getElementById("dontEnd").onclick = async () => {
                            answer("false");

                            userStack.forEach(s => {
                                s.stopZeigenPunkt();
                            });

                            document.getElementById("result").classList.remove("show");

                            dealerLeftStack.stopZeigenPunkt();
                            await stackToDefaultPosition([...userStack, dealerLeftStack]);
                            window.userStack = [];
                            window.runningStackId = 0; //Wichtig!!!

                            overlaySetStatus(false);
                            await startGame(false);
                        }
                        document.getElementById("end").onclick = () => {
                            answer("true");
                            window.location.href = "/";
                        }
                        break;
                    case "coins":
                        answer("ok"); // Server wartet auf OK!
                        const newCoinsCount = Number(textArray[1]);
                        console.log("Got New Coins Count!!!", chipCount, newCoinsCount);
                        window.chipCount = newCoinsCount;
                        document.getElementById("ChipCount").innerText = "Chips: " + chipCount + "¢";
                        break;
                    case "bet":
                        const bet = await getBet();
                        answer(bet);
                        getListener("awaitBet")(bet);
                        console.log("Fertig");
                        break;
                    default:
                        console.log("Error! Server labert Müll!", text);
                        break;
                }
            } else if (msg.startsWith("blackjack:")) {
                console.log("Blackjack!!!!");
                window.endProcess = true;
                window.endGame = true;
            } else if (msg.startsWith("bust:")) {
                let stackId = parseInt(msg.substring("bust:".length));
                endStack().then();
                if (userStack.length === 1) {
                    window.endProcess = true;
                    window.endGame = true;
                }
                console.log("Ehrenlos", stackId);
            } else {
                console.log("Error! Unknown Server Message:", msg);
            }
        };

        socket.onerror = (error) => {
            console.error('WebSocket-Fehler:', error);
            // Crash
            setTimeout(()=>{
                window.socket = null;
                connectSocket();
            }, 10000);
        };

        // Verbindung geschlossen => Handlen
        socket.onclose = () => {
            console.log('Verbindung zum Server geschlossen.');
        };

        console.log("Start Wait Intervall!");
        const checkConnection = setInterval(() => {
            if (socket.readyState !== WebSocket.CONNECTING) {
                console.log("Interval ist nicht mehr connecting!");
                clearInterval(checkConnection);
                if (socket.readyState === WebSocket.OPEN) {
                    resolve(socket);
                } else console.error("Error! Error connecting Websocket!", socket.readyState);
            }
        }, 50);
    });
    console.log("Return Socket Promise!");
    return await socketPromise;
}
document.addEventListener("DOMContentLoaded", connectSocket);

// Server Kommunikation, Annahme der Karten
window.takeCard = ()=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("TakeUser");

    addListener("card", resolve);
});
window.dealerTakes = ()=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("TakeDealer");

    addListener("dealercard", resolve);
});
//Um ein neues Spiel zu starten → Übergeben wird der einseatz => Muss an derver gegeben werden, Zurückgegeben wird die erste sichtbare umgedrehte karte des dealers.
window.startNewBidding = (bet)=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("Bet:"+bet);

    resolve();
});
window.startBlackJack = ()=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("Start");

    addListener("bal", resolve);
});

let gameResult = null;
let onGameResult = [];
window.setGameResultText = (t) => {
    try {
        let part = t.split(">");
        for(let a in part){
            console.log(a);
        }
        let displayText = part[0];
        let chipAmount = part[1];
        window.chipCount = Number(chipAmount);
        console.log("Got Game Result Text:", displayText);
        if(!t) console.trace("Error! No Game Result Text empfangen!", displayText);
        gameResult = displayText;
        onGameResult.forEach(c => c(displayText));
        document.getElementById("ChipCount").innerText = "Chips: " + chipAmount;
        onButtonsAbbruch();
    } catch(e) {
        console.log(e);
    }
}
window.getGameResults = ()=>new Promise(async resolve => {
    const socket = await connectSocket();
    if(gameResult) return resolve(gameResult);
    socket.send("GetResult");
    onGameResult.push(resolve);
});
window.getDealerCards = ()=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("GetDealer");

    addListener("dealercards", resolve);
});
window.exchange = (chipAmount) =>new Promise(async resolve => {
    console.log("Run Exchange:", chipAmount);

    const socket = await connectSocket();
    socket.send("Exchange:" + chipAmount);

    addListener("chipupdate", resolve);
})

//Um einen Stack zu schließen
window.endStackServer = (stackIndex)=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("endstack:" + stackIndex);
    resolve();
});

