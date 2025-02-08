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
            const msg = event.data.toString();
            if(event.data.toString().toLowerCase().indexOf('acc') === 0) {
                console.log("ID: " + clientID.toString());
                socket.send("ID:" + clientID.toString());
                window.approved = true;
            }

            let coat;
            let value;
            let points;

            if(msg.startsWith("Card:")){
                let sub = msg.substring(5);

                let part = sub.split(",");

                coat = part[0].substring(1);

                value = part[1].substring(1);
                listener.shift()({
                    type: coat,
                    points: value
                });
            }

            if(msg.startsWith("DealerCard:")){
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

            if(msg.startsWith("DealerCards,")){
                let cardObjects = [];
                let sub = msg.substring(5);

                let cards = sub.split(";");
                for(let singleCard of cards){
                    let part = singleCard.split(",");

                    coat = part[0].substring(1);

                    value = part[1].substring(1);
                    cardObjects.push({type: coat, points: value});
                }

                listener.shift()({
                    cards: cardObjects
                })
            }
        };

        // Fehlerbehandlung → Bei Wecgsockent kominiaitob
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

    listener.push(resolve);
});
window.startBlackJack = ()=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("Start");
    resolve();
})
window.getGameResults = ()=>new Promise(resolve => {
   resolve("Hallo");
});
window.getDealerCards = ()=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("GetDealer");

    listener.push(resolve);
});

window.serverDoubleDown = ()=>{

}

//Um einen Stack zu schließen
window.endStackServer = (stackIndex)=>new Promise(async resolve => {
    const socket = await connectSocket();

    resolve({

    });
});
