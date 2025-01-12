const socket = new WebSocket('ws://127.0.0.1:8080');
window.clientID = -1;

// Verbindung geöffnet
socket.onopen = () => {};

// Nachricht vom Server empfangen
socket.onmessage = (event) => {
    console.log(event);
    const msg = event.data.toString();
    if(event.data.toString().indexOf('acc') === 0)
    {
        clientID = Number(msg.substring(4, msg.length));
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

function handleStartButton() {
    console.log('Start Button clicked');
    socket.send('start');
    const enableButton = document.getElementById('StartButton');
    enableButton.visibility = "hidden";
    enableButton.enabled = false;
    enableButton.style.visibility = "hidden";
}