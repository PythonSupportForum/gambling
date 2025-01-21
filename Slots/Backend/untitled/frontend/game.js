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
        return data;
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
        return data.balance;
    } catch (error) {
        console.error('Fehler:', error);
    }
}

let isU = false;
let nU = false;
window.balance = -1;
window.showBalance = 0;
let lastR = Date.now();
window.updateBRunner = ()=>{
    if(Date.now()-lastR < 50) return setTimeout(updateBRunner, 50-(Date.now()-lastR));
    lastR = Date.now();
    window.showBalance += (balance>showBalance) ? Math.ceil((balance-showBalance)/10) : -Math.ceil((showBalance-balance)/10);
    document.getElementById("balance").innerText = showBalance.toString()+" TT";
    if(showBalance === balance) return;
    updateBRunner();
}
window.updateB = async ()=>{
    if(isU) {
        nU = true;
        return;
    }
    isU = true;
    nU = false;
    try {
        const r = await fetchBalance(token);
        if(r && r !== balance) {
            window.balance = r;
            document.getElementById("balance").innerText = showBalance.toString()+" TT";
            updateBRunner();
        }
        else nU = true;
    } catch(e) {
        console.log(e);
    }
    if(nU) updateB().then(()=>{});
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

updateB().then(()=>{});
setInterval(updateB, 5*1000);

window.play = async ()=>{
    console.log("Start Game...");
    document.getElementById("play").disabled = true;

    const {results, winAmount} = await  startGame(token);

    if(winAmount > 0) {
        startConfetti();
    }

    console.log("R:", {results, winAmount});

    await updateB();


    document.getElementById("play").disabled = false;
}

