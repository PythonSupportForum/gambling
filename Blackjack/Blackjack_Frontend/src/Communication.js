window.socket = null;

window.listener = [];

window.connectSocket = ()=>{
    return new Promise(resolve => {
        if(socket) return resolve(socket); //Es gibt schon socket!
        window.socket = new WebSocket('ws://127.0.0.1:8080');
        let clientID = -1; //Hat Carl Implemennteirt

        // Verbindung geöffnet
        socket.onopen = () => {
            resolve(socket); //Promise wirrd aufgelöst => For Sachen doe von Verbindung Abhängen und auf Vebrindung warten müssen
        };

         // Nachricht vom Server empfangen => Allgemein für alle Nachenacihtne (Websocket protoikkoll)
        socket.onmessage = (event) => {
            console.log(event.data);
            const msg = event.data.toString();
            if(event.data.toString().indexOf('acc') === 0) clientID = Number(msg.substring(4, msg.length));

            let card;
            let value;
            if(msg.startsWith("Card: ")){
                let sub = msg.substring(5);

                let part = sub.split(",");

                card = part[0].substring(1);

                value = part[1].substring(1);
                listener.shift()({
                    type: card,
                    points: value
                });
            }

            if(msg.startsWith("DealerCards,")){
                let sub = msg.substring(5);

                let part = sub.split(",");

                card = part[0].substring(1);

                value = part[1].substring(1);
                listener.shift()({
                    type: card,
                    points: value
                });
            }
        };

        // Fehlerbehandlung => Bei Wecgsockent kominiaitob
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
//Um ein Neues PSiel zu starten => Übergeben wird der einseatz => Muss an derver gegeben werden, Zurückgegeben wird die erste sichtbare umgedrehte karte des dellers.
window.startNewGame = (bet)=>new Promise(async resolve => {
    const socket = await connectSocket();
    socket.send("Bet:"+bet);

    listener.push(resolve);
});

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

//Um einen Stappel zu schlicßen
window.endStackServer = (stappelIndex)=>new Promise(async resolve => {
    const socket = await connectSocket();

    //blub.send(seinsatz)     <= Exemplarische
    //blub.get(Info);

    resolve({

    });
});
