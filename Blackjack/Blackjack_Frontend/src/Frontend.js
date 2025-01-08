const socket = new WebSocket('ws://127.0.0.1:8080');

// Verbindung geöffnet
socket.onopen = () => {
    const clientID = '1234';
    console.log(`Sende client_ID: ${clientID}`);
    socket.send(clientID);
};

// Nachricht vom Server empfangen
socket.onmessage = (event) => {
    if(event.data == 'acc'){
        const clientID = '1234';
        console.log(`Sende client_ID: ${clientID}`);
        socket.send(clientID);
    }
};

// Fehlerbehandlung
socket.onerror = (error) => {
    console.error('WebSocket-Fehler:', error);
};

// Verbindung geschlossen
socket.onclose = () => {
    console.log('Verbindung zum Server geschlossen.');
};
