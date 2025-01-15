async function startGame(token) {
    try {
        const response = await fetch('http://localhost:8080/start-game', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ token }),
        });

        if (!response.ok) {
            const errorData = await response.json();
            console.error('Fehler beim Starten des Spiels:', errorData);
            return;
        }

        const data = await response.json();
        console.log('Spiel gestartet:', data);
    } catch (error) {
        console.error('Fehler:', error);
    }
}

async function fetchBalance(token) {
    try {
        const response = await fetch('http://localhost:8080/stand', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ token }),
        });

        if (!response.ok) {
            const errorData = await response.json();
            console.error('Fehler beim Abfragen des Kontostands:', errorData);
            return;
        }

        const data = await response.json();
        console.log('Kontostand:', data.balance);
    } catch (error) {
        console.error('Fehler:', error);
    }
}

startGame(token).then(console.log);