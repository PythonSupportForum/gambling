const socket = new WebSocket('ws://127.0.0.1:8080');

// Verbindung geöffnet
socket.onopen = () => {
    console.log('Verbindung zum Server hergestellt.');

    // Nachricht an den Server senden, z. B. die client_ID
    const clientID = '1234';
    console.log(`Sende client_ID: ${clientID}`);
    socket.send(clientID);
};

// Nachricht vom Server empfangen
socket.onmessage = (event) => {
    console.log('Nachricht vom Server: ' + event.data);

    // Beenden, wenn "exit" empfangen
    if (event.data.includes('Verbindung beendet!')) {
        console.log('Verbindung wird geschlossen...');
        socket.close();
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