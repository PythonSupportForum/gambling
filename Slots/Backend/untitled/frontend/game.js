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
            headers: {'Content-Type': 'application/json',},
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

function startConfetti() {
    const canvas = document.createElement('canvas');
    document.body.appendChild(canvas);
    canvas.style.position = 'fixed';
    canvas.style.top = '0';
    canvas.style.left = '0';
    canvas.style.pointerEvents = 'none';
    canvas.style.zIndex = '1000';
    const ctx = canvas.getContext('2d');
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    const confettiCount = 300;
    const confetti = [];
    const colors = ['#f94144', '#f3722c', '#f9c74f', '#90be6d', '#43aa8b', '#577590'];
    function createConfettiPiece() {
        return {
            x: Math.random() * canvas.width,
            y: Math.random() * canvas.height - canvas.height,
            r: Math.random() * 6 + 2,
            d: Math.random() * confettiCount + 10,
            color: colors[Math.floor(Math.random() * colors.length)],
            tilt: Math.random() * 15,
            tiltAngleIncremental: Math.random() * 0.07 + 0.05,
            tiltAngle: 0
        };
    }
    for (let i = 0; i < confettiCount; i++) confetti.push(createConfettiPiece());
    function drawConfetti() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        confetti.forEach(p => {
            ctx.beginPath();
            ctx.lineWidth = p.r;
            ctx.strokeStyle = p.color;
            ctx.moveTo(p.x + p.tilt + p.r / 3, p.y);
            ctx.lineTo(p.x + p.tilt, p.y + p.r);
            ctx.stroke();
        });
    }
    function updateConfetti() {
        confetti.forEach(p => {
            p.tiltAngle += p.tiltAngleIncremental;
            p.y += (Math.cos(p.d) + 1 + p.r / 2) / 2;
            p.x += Math.sin(p.d);
            p.tilt = Math.sin(p.tiltAngle) * 15;
            if (p.y > canvas.height) {
                p.y = -10;
                p.x = Math.random() * canvas.width;
                p.tilt = Math.random() * 15;
            }
        });
    }
    function animateConfetti() {
        drawConfetti();
        updateConfetti();
        requestAnimationFrame(animateConfetti);
    }
    animateConfetti();
    window.addEventListener('resize', () => {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    });
    setTimeout(() => canvas.remove(), 15000); // Stop after 15 seconds
}



startGame(token).then(console.log);