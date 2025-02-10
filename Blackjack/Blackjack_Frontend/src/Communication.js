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
    console.log("Error! Versuche Listner auszuführen von dem es keinen meher gibt!", type);
    return ()=>{};
}

window.connectSocket = ()=>{
    return new Promise(resolve => {
        if(socket) return resolve(socket); //Es gibt schon socket!
        window.socket = new WebSocket('ws://127.0.0.1:8080');
        let clientID = -1;

        // Verbindung geöffnet
        socket.onopen = () => {
            resolve(socket); //Promise wird aufgelöst => For Sachen die von Verbindung Abhängen und auf Vebrindung warten müssen
        };

         // Nachricht vom Server empfangen → Allgemein für alle Nachrichten (Websocket protokoll)
        socket.onmessage = (event) => {
            console.log("Nachricht vom Server:",event.data);
            const msg = event.data.toString().toLowerCase();
            if(event.data.toString().toLowerCase().indexOf('acc') === 0) {
                console.log("ID: " + clientID.toString());
                socket.send("ID:" + clientID.toString());
                window.approved = true;
            }

            let coat;
            let value;
            let points;

            if(msg.startsWith("card:")){
                let sub = msg.substring("card:".length);

                let part = sub.split(",");

                coat = part[0].substring("c:".length);

                value = part[1].substring("v:".length);

                points = part[2].substring("p:".length);

                getListener("card")({
                    type: value + "_" + coat,
                    points: points
                });
            }
            else if(msg.startsWith("dealercard:")){
                let sub = msg.substring("DealerCard:".length);

                let part = sub.split(",");

                coat = part[0].substring("c:".length);

                value = part[1].substring("v:".length);

                points = part[2].substring("p:".length);

                console.log("points", points,"card",value + "_" + coat);

                getListener("dealercard")({
                    type: value + "_" + coat,
                    points: points
                });
            }
            else if(msg.startsWith("dealercards:")){
                console.log("Empfängt Dealer Karten");

                let cardObjects = [];
                let sub = msg.substring("DealerCards:".length).split(">");

                let cards = sub[0].split(";");
                for(let singleCard of cards){
                    let part = singleCard.split(",");

                    coat = part[0].substring("c:".length);

                    value = part[1].substring("v:".length);
                    cardObjects.push({type: value + "_" + coat, points: 0});
                }

                getListener("dealercards")({objects: cardObjects, stackValue: parseInt(sub[1])}, true);
            }
            else if(msg.startsWith("chipupdate:")){
                let updatedChipCount = parseInt(msg.substring("DealerCards:".length - 1));
                if(updatedChipCount >= 0){
                    console.log("Chip updated for: " + updatedChipCount);
                    document.getElementById("ChipCount").innerText = updatedChipCount + "¢";
                    console.log(updatedChipCount);
                    getListener("chipupdate")(updatedChipCount);
                }
                else{
                    document.getElementById("ChipCount").innerText = chipCount + "¢";
                    getListener("chipupdate")(chipCount);
                    console.error("Auszahlung nicht erfolgt");
                }
            }
            else if(msg.startsWith("text:")) setGameResultText(msg.substring("text:".length));


            else if(msg.startsWith("bal:")){
                window.balance = parseFloat(msg.substring("bal:".length));
                console.log(balance);
                getListener("bal")(balance);
            }
            else if(msg.startsWith("stack:")){
                let part = msg.substring("stack:".length).split(",");

                let points = parseInt(part[0].substring("p:".length));

                let state;
                state = part[1].substring("s:".length).includes("true");

                getListener("stack")({
                    points: points,
                    state: state
                });
            }
            else if(msg.startsWith("bust:")){
                let stackId = parseInt(msg.substring("bust:".length));
                endStack().then();
                if(userStack.length === 1){
                    window.endProcess = true;
                }
                console.log("Ehrenlos",stackId);
            }
        };

        socket.onerror = (error) => {
            console.error('WebSocket-Fehler:', error);
            // Crash
        };

        // Verbindung geschlossen => Handlen
        socket.onclose = () => {
            console.log('Verbindung zum Server geschlossen.');
        };
    });
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
    console.log("Got Game Result Text:", t);
    if(!t) console.trace("Error! No Game Result Text empfangen!",t);
    gameResult = t;
    onGameResult.forEach(c => c(t));
}
window.getGameResults = ()=>new Promise(resolve => {
    if(gameResult) return resolve(gameResult);
    onGameResult.push(resolve);
});
window.getDealerCards = ()=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("GetDealer");

    addListener("dealercards", resolve);
});
window.exchange = (chipAmount) =>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("Exchange:" + chipAmount);

    addListener("chipupdate", resolve);
})

window.serverDoubleDown = ()=>{

}

//Um einen Stack zu schließen
window.endStackServer = (stackIndex)=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("EndStack:" + stackIndex);
});

