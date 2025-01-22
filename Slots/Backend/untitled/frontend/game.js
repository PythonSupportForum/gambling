async function loadAndSortImages(url) {
    try {
        const response = await fetch(url);
        const data = await response.json();

        // Extrahiere und dekodiere die Bild-Data-URLs
        const images = await Promise.all(data.map(async (item, index) => {
            const img = new Image();
            img.src = item.bild;

            // Warte, bis das Bild vollständig geladen ist
            await new Promise((resolve) => {
                img.onload = resolve;
            });

            return { img, index };
        }));

        // Sortiere die Bilder nach Index
        images.sort((a, b) => a.index - b.index);

        return images.map(item => item.img);
    } catch (error) {
        console.error('Fehler beim Laden der Bilder:', error);
        throw error;
    }
}

const url = 'http://localhost:8080/bilder';
window.bilder = loadAndSortImages(url);


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
    isU = false;
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

    const gamePromise = async () => {
        const {results, winAmount} = await  startGame(token);
        if(winAmount > 0) startConfetti();
        console.log("R:", {results, winAmount});
        await updateB();
        return results;
    }
    const ergebnisPromise = gamePromise();

    const b = await bilder;
    const canvasContainers = [...document.getElementsByClassName("rad")];
    async function animateWheel(canvas, context, images, duration, finalImageIndexPromise) {
        let finalImageIndex = null;
        finalImageIndexPromise.then(i => {
            finalImageIndex = i;
            console.log("GOT INDEX:", i);
        });
        const imageHeight = canvas.height;
        const totalImages = images.length;
        let startTime = null;
        let currentOffset = 0;
        const maxSpeed = 20;
        let speed = 0;
        let fertig = false;

        function step(timestamp) {
            if (!startTime) startTime = timestamp;
            const elapsed = timestamp - startTime;
            if((!finalImageIndex || elapsed < duration) && speed < maxSpeed) {
                speed += Math.min(Math.max(Math.abs(duration-elapsed)/50000, 0.2), 1);
            } else if(finalImageIndex) {
                const zielOffset = finalImageIndex*imageHeight;
                speed += currentOffset > zielOffset ? -0.3 : 0.2;
                if(speed < 0) speed += 0.1;
                if((elapsed/duration > 3) && speed > 0 && currentOffset > zielOffset) speed -= Math.max(speed/(50*(elapsed/duration)), 0.5);
                if((elapsed/duration > 2) && speed > 0 && currentOffset > zielOffset) speed -= speed/(50*(elapsed/duration));
                if(Math.abs(speed) < 1 && Math.round(currentOffset) < Math.round(zielOffset)+1 && Math.round(currentOffset) > Math.round(zielOffset)-1) {
                    setTimeout(()=>{
                        fertig = true;
                    }, 5000);
                }
            } else speed -= (speed/100);

            currentOffset = (currentOffset+speed)%(totalImages*imageHeight);

            context.clearRect(0, 0, canvas.width, canvas.height);

            const startIndex = Math.floor(currentOffset / imageHeight) % totalImages;

            for (let i = 0; i < 2; i++) {
                const imageIndex = (startIndex + i) % totalImages;
                const yOffset = currentOffset % imageHeight - i * imageHeight;
                context.drawImage(
                    images[imageIndex],
                    0,
                    yOffset,
                    canvas.width,
                    canvas.height
                );
            }

            if (!fertig) {
                requestAnimationFrame(step);
            } else {
                context.clearRect(0, 0, canvas.width, canvas.height);
                context.drawImage(
                    images[finalImageIndex],
                    0,
                    0,
                    canvas.width,
                    canvas.height
                );
            }
        }

        requestAnimationFrame(step);
    }
    for (let i = 0; i < canvasContainers.length && i < b.length; i++) {
        let canvas = canvasContainers[i].querySelector("canvas");
        if (!canvas) {
            canvas = document.createElement("canvas");
            canvasContainers[i].appendChild(canvas);
        }
        try {
            const context = canvas.getContext("2d");
            canvas.width = canvasContainers[i].clientWidth;
            canvas.height = canvasContainers[i].clientHeight;

            await animateWheel(canvas, context, b, 2000*(1+i+Math.random()), new Promise(async (resolve) => {
                const results = await ergebnisPromise;
                resolve(results[i]-1);
            }));
        } catch (e) {
            console.log(e, i, b);
        }
    }

    document.getElementById("play").disabled = false;
}

