class MessageQueue {
    constructor(containerId) {
        this.queue = []; // Array für Nachrichten
        this.container = document.getElementById(containerId); // Container für die Nachrichten
        this.isProcessing = false; // Gibt an, ob gerade eine Nachricht angezeigt wird
    }
    addMessage(text) {
        overlaySetStatus(true);
        return new Promise((resolve) => {
            this.queue.push({ text, resolve });
            this.processQueue(); // Versuche die Warteschlange zu verarbeiten
        });
    }
    displayMultipleMessages(list) {
        return Promise.all(list.map(l => this.addMessage(l)));
    }
    async processQueue() {
        if (this.isProcessing || this.queue.length === 0) return; // Verlasse die Methode, wenn gerade eine Nachricht verarbeitet wird oder die Warteschlange leer ist
        this.isProcessing = true; // Verarbeitung starten
        const { text, resolve } = this.queue.shift(); // Erste Nachricht aus der Warteschlange nehmen
        const messageElement = document.createElement('h2');
        messageElement.textContent = text;
        this.container.appendChild(messageElement); // Element zum Container hinzufügen

        await new Promise((done) => setTimeout(done, 3000));
        this.container.removeChild(messageElement);
        resolve();
        this.isProcessing = false;
        this.processQueue().then(()=>{});
    }
}