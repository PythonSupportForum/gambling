window.socket = null;
window.approved = false;
window.listener = [];

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

                listener.shift()({
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

                listener.shift()({
                    type: value + "_" + coat,
                    points: points
                });
            }
            else if(msg.startsWith("dealercards:")){
                let cardObjects = [];
                let sub = msg.substring("DealerCards:".length);

                let cards = sub.split(";");
                for(let singleCard of cards){
                    let part = singleCard.split(",");

                    coat = part[0].substring(1);

                    value = part[1].substring(1);
                    cardObjects.push({type: coat, points: value});
                }

                listener.shift()(cardObjects);
            }
            else if(msg.startsWith("chipupdate:")){
                let updatedChipCount = parseInt(msg.substring("DealerCards:".length - 1));
                if(updatedChipCount >= 0){
                    console.log("Chip updated for: " + updatedChipCount);
                    document.getElementById("ChipCount").innerText = updatedChipCount + "¢";
                    console.log(updatedChipCount);
                    listener.shift()(updatedChipCount);
                }
                else{
                    document.getElementById("ChipCount").innerText = chipCount + "¢";
                    listener.shift()(chipCount);
                    console.error("Auszahlung nicht erfolgt");
                }
            }
            else if(msg.startsWith("bal:")){
                window.balance = parseFloat(msg.substring("bal:".length));
                console.log(balance);
                listener.shift()(balance);
            }
            else if(msg.startsWith("stack:")){
                let part = msg.substring("stack:".length).split(",");

                let points = parseInt(part[0].substring("p:".length));

                let state;
                state = part[1].substring("s:".length).includes("true");

                listener.shift()({
                    points: points,
                    state: state
                });
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

    listener.push(resolve);
});
window.dealerTakes = ()=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("TakeDealer");

    listener.push(resolve);
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
    listener.push(resolve);
})
window.getGameResults = ()=>new Promise(resolve => {
   resolve("Hallo");
});
window.getDealerCards = ()=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("GetDealer");

    listener.push(resolve);
});
window.exchange = (chipAmount) =>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("Exchange:" + chipAmount);

    listener.push(resolve);
})

window.serverDoubleDown = ()=>{

}

//Um einen Stack zu schließen
window.endStackServer = (stackIndex)=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("EndStack:" + stackIndex);
    listener.push(resolve);
});
