window.host = "/api";
window.beIrre = false;

window.isWin = false;
document.addEventListener("DOMContentLoaded", ()=>{
    window.token = document.getElementById("token").innerText || "2";
    console.log("User Token gefunden:", token);

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

    const url = host+'/bilder';
    window.bilder = loadAndSortImages(url);

    async function startGame(token) {
        try {
            const response = await fetch(host+'/start-game', {
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
            const response = await fetch(host+'/stand', {
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

    let isU = false; //Balance Exportentieles Wachstum
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
        console.log("Start Konfetti!!");

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
        if(balance < 1000) {
            alert("Du bist Pleite!!! Wir akzeptieren keine Schuldner! Bitte zahle mehr Kohle ein!");
            return;
        }
        console.log("Start Game...");
        document.getElementById("play").disabled = true;

        const gamePromise = async () => {
            const {results, winAmount} = await  startGame(token);
            window.isWin = (winAmount+1000) !== 0;
            console.log("R:", {results, winAmount});
            return results;
        }
        const ergebnisPromise = gamePromise();

        const b = await bilder;
        const canvasContainers = [...document.getElementsByClassName("rad")];
        async function animateWheel(canvas, context, images, duration, finalImageIndexPromise, onIrre = ()=>{}) {
            if(beIrre) setTimeout(onIrre, 4000);

            let finalImageIndex = -1;
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

            let randomReachSpeed = Math.random()*50;

            function step(timestamp) {
                if (!startTime) startTime = timestamp;
                const elapsed = timestamp - startTime;

                let ziel = finalImageIndex*imageHeight+5;
                if(ziel < currentOffset-0.1) ziel += totalImages*imageHeight;

                if(finalImageIndex < 0 || elapsed < duration) {
                    if(speed < 100) speed += 0.5;
                    if(speed < randomReachSpeed) speed += 0.5;
                } else {
                    const strecke = ziel-currentOffset;
                    let speedProTime = strecke/1000;
                    if(Math.abs(speedProTime) < 5) speedProTime *= 10;

                    speed += speedProTime-speed > 0 ? 0.15 : -0.15;

                    if(Math.abs(strecke) < 5) {
                        speed -= strecke/10000;
                        speed -= 0.1;
                    }
                }
                //if(Math.abs(speed) > 40) speed -= Math.max(Math.sqrt(speed), 1);
                if(!speed) speed -= 2;
                if(speed < -40) speed += 20;

                if(Math.abs(ziel-currentOffset) < 0.1 && Math.abs(speed) < 0.5) {
                    speed = 0;
                    currentOffset = ziel;
                    console.log("Ende! Ziel Erriecht!!!", isWin);
                }

                if(Math.abs(ziel-currentOffset) < 5 && Math.abs(speed) < 2) {
                    if(isWin) {
                        console.log("Is Win!!!!!");
                        startConfetti();
                        updateB();
                        window.isWin = false;
                    }
                }

                currentOffset = (currentOffset+speed)%(totalImages*imageHeight);

                context.clearRect(0, 0, canvas.width, canvas.height);

                if(currentOffset < 0) currentOffset += (totalImages*imageHeight);

                const startIndex = Math.floor((currentOffset%(totalImages*imageHeight)) / imageHeight) % totalImages;

                let realOffset = currentOffset;
                for (let i = 0; i < 2; i++) {
                    try {
                        const imageIndex = (startIndex + i) % totalImages;
                        const yOffset = (realOffset%(totalImages*imageHeight)) % imageHeight - i * imageHeight;
                        if(images[imageIndex]) {
                            context.drawImage(
                                images[imageIndex],
                                0,
                                yOffset,
                                canvas.width,
                                canvas.height
                            );
                        } else {
                            console.log("Error!", imageIndex, images);
                        }
                    } catch(e) {
                        console.log(e);
                    }
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

                await animateWheel(canvas, context, b, 1000*(2+(3-i)*0.2+Math.random()), new Promise(async (resolve) => {
                    const results = await ergebnisPromise;
                    resolve(results[i]-1);
                }), ()=>{
                    canvasContainers[i].classList.add("irre");
                    play();
                });
            } catch (e) {
                console.log(e, i, b);
            }
        }
        document.getElementById("play").disabled = false;
    }
});