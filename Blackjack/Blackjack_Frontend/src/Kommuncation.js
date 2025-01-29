window.socket = null;

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
            console.log(event);
            const msg = event.data.toString();
            if(event.data.toString().indexOf('acc') === 0) clientID = Number(msg.substring(4, msg.length));
        };

        // Fehlerbehandlung => Bei Wecgsockent kominiaitob
        socket.onerror = (error) => {
            console.error('WebSocket-Fehler:', error);
            //BNeu Verundein

        };

        // Verbindung geschlossen => Handlen
        socket.onclose = () => {
            console.log('Verbindung zum Server geschlossen.');
        };
    });
}
document.addEventListener("DOMContentLoaded", connectSocket);

//In die volgenden Methoden kommt der ganze Quatsch mit der Serbfer Komimioation
window.karteZiehen = ()=>new Promise(async resolve => {
    const socket = await connectSocket();

    resolve({
        type: "4_d",
        points: 4
    });
});
//Um ein Neues PSiel zu starten => Übergeben wird der einseatz => Muss an derver gegeben werden, Zurückgegeben wird die erste sichtbare umgedrehte karte des dellers.
window.startNewGame = (einsatz)=>new Promise(async resolve => {
    const socket = await connectSocket();

    //blub.send(seinsatz)     <= Exemplarische
    //blub.get(Info);

    resolve({
        firstDealerCard: {
            type: "4_d",
            points: 4
        }
    });
});
