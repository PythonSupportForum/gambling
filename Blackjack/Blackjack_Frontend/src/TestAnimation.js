let ctx;

let growFactor = 0.7;
let cardWidth = 200;
let cardHeight = 300;
let flippingTime = 1000;

// region Assets
let toLoad = {
    "a_c": "https://gambling.megdb.de/assets/karten/club/A.svg",
    "2_c": "https://gambling.megdb.de/assets/karten/club/2.svg",
    "3_c": "https://gambling.megdb.de/assets/karten/club/3.svg",
    "4_c": "https://gambling.megdb.de/assets/karten/club/4.svg",
    "5_c": "https://gambling.megdb.de/assets/karten/club/5.svg",
    "6_c": "https://gambling.megdb.de/assets/karten/club/6.svg",
    "7_c": "https://gambling.megdb.de/assets/karten/club/7.svg",
    "8_c": "https://gambling.megdb.de/assets/karten/club/8.svg",
    "9_c": "https://gambling.megdb.de/assets/karten/club/9.svg",
    "10_c": "https://gambling.megdb.de/assets/karten/club/10.svg",
    "j_c": "https://gambling.megdb.de/assets/karten/club/J.svg",
    "q_c": "https://gambling.megdb.de/assets/karten/club/Q.svg",
    "k_c": "https://gambling.megdb.de/assets/karten/club/K.svg",

    "a_d": "https://gambling.megdb.de/assets/karten/diamond/A.svg",
    "2_d": "https://gambling.megdb.de/assets/karten/diamond/2.svg",
    "3_d": "https://gambling.megdb.de/assets/karten/diamond/3.svg",
    "4_d": "https://gambling.megdb.de/assets/karten/diamond/4.svg",
    "5_d": "https://gambling.megdb.de/assets/karten/diamond/5.svg",
    "6_d": "https://gambling.megdb.de/assets/karten/diamond/6.svg",
    "7_d": "https://gambling.megdb.de/assets/karten/diamond/7.svg",
    "8_d": "https://gambling.megdb.de/assets/karten/diamond/8.svg",
    "9_d": "https://gambling.megdb.de/assets/karten/diamond/9.svg",
    "10_d": "https://gambling.megdb.de/assets/karten/diamond/10.svg",
    "j_d": "https://gambling.megdb.de/assets/karten/diamond/J.svg",
    "q_d": "https://gambling.megdb.de/assets/karten/diamond/Q.svg",
    "k_d": "https://gambling.megdb.de/assets/karten/diamond/K.svg",

    "a_h": "https://gambling.megdb.de/assets/karten/heart/A.svg",
    "2_h": "https://gambling.megdb.de/assets/karten/heart/2.svg",
    "3_h": "https://gambling.megdb.de/assets/karten/heart/3.svg",
    "4_h": "https://gambling.megdb.de/assets/karten/heart/4.svg",
    "5_h": "https://gambling.megdb.de/assets/karten/heart/5.svg",
    "6_h": "https://gambling.megdb.de/assets/karten/heart/6.svg",
    "7_h": "https://gambling.megdb.de/assets/karten/heart/7.svg",
    "8_h": "https://gambling.megdb.de/assets/karten/heart/8.svg",
    "9_h": "https://gambling.megdb.de/assets/karten/heart/9.svg",
    "10_h": "https://gambling.megdb.de/assets/karten/heart/10.svg",
    "j_h": "https://gambling.megdb.de/assets/karten/heart/J.svg",
    "q_h": "https://gambling.megdb.de/assets/karten/heart/Q.svg",
    "k_h": "https://gambling.megdb.de/assets/karten/heart/K.svg",

    "a_s": "https://gambling.megdb.de/assets/karten/spade/A.svg",
    "2_s": "https://gambling.megdb.de/assets/karten/spade/2.svg",
    "3_s": "https://gambling.megdb.de/assets/karten/spade/3.svg",
    "4_s": "https://gambling.megdb.de/assets/karten/spade/4.svg",
    "5_s": "https://gambling.megdb.de/assets/karten/spade/5.svg",
    "6_s": "https://gambling.megdb.de/assets/karten/spade/6.svg",
    "7_s": "https://gambling.megdb.de/assets/karten/spade/7.svg",
    "8_s": "https://gambling.megdb.de/assets/karten/spade/8.svg",
    "9_s": "https://gambling.megdb.de/assets/karten/spade/9.svg",
    "10_s": "https://gambling.megdb.de/assets/karten/spade/10.svg",
    "j_s": "https://gambling.megdb.de/assets/karten/spade/J.svg",
    "q_s": "https://gambling.megdb.de/assets/karten/spade/Q.svg",
    "k_s": "https://gambling.megdb.de/assets/karten/spade/K.svg",

    "back": "https://gambling.megdb.de/assets/Kartenrücken.png",
    "table": "https://gambling.megdb.de/assets/table.png"
}
// endregion

window.imgData = {};
const loader = async ()=>{
    if(Object.keys(toLoad).length === 0) return;

    return await new Promise((resolve)=>{
        const i = new Image();
        const k = Object.keys(toLoad).shift();
        i.src = toLoad[k];
        i.onload = async ()=>{
            console.log("Loaded:",i.src,"as",k,"successfull!");
            imgData[k] = i;
            delete toLoad[k];
            await loader();
            resolve(imgData);
        };
        i.onerror = async()=>{
            console.log("Müllen", i.src);
            await loader();
            resolve(imgData);
        }
    });
}

window.bilder = loader();

let canvas;
let b; //Enthält Aufgelöstes promise oBjekt => Nach abgeschlosenen Laden

const socket = new WebSocket('ws://127.0.0.1:8080');
let clientID = -1;

// Verbindung geöffnet
socket.onopen = () => {
    const text = document.getElementById('Connection Text');
    text.style.visibility = "visible";
};

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


window.addEventListener('resize', resizeCanvas);
window.onload = async function (){
    canvas = document.getElementById("canvas");
    resizeCanvas();

    document.getElementById("button").addEventListener("click", reveal);

    b = await bilder; //Warten auf Promise => Alle Bilder geladen

    const card = new GameCard(null, b.back, {x: 400, y: 200});


    if (canvas.getContext) {
        ctx = canvas.getContext("2d");
        last = Date.now();

        draw();
    }
}

function reveal(){
    for (let index = 0; index < stationaryObjects.length; ++index) {
        stationaryObjects[index].flipping = true;
    }
}

function resizeCanvas(){
    canvas.style.width = "100%";
    canvas.style.height = "100%";
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
}

// Frame Generation



function parabelfunktion(x, xMax) { //Die Funktion erstellt eine gestrauchte Parabel => Damit Dreh Bewegung der karte, F(x) gibt Anteil der Normalbreite an. Bei  = 1000 und bei x = 1 ist sie 1. Der zweite parameter gibt die Stauchung in Millisekunden an
    const x0 = xMax / 2; // Der Scheitelpunkt liegt in der Mitte zwischen 0 und xMax
    const a = -4 / (xMax ** 2); // Der Streckungsfaktor der Parabel
    return Math.max(0, Math.min(1, a * (x - x0) ** 2 + 1));
}

function sinVel(anim){
    function calculateDistance({x1, y1}, {x2, y2}) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }
    // Prozentualer Anteil des Weges der zurückgelegt wurde
    let part = 1 - ((anim.endPoint.x - anim.pos.x) / (anim.endPoint.x - anim.startPoint.x));

    // Faktor zur Beschleunigung: Die Geschwindigkeit verändert sich im Verlauf einer um 0,1 phasenverschobenen Sinuskurve, ermöglicht weichere Animation
    part = Math.sin(part * Math.PI) + 0.1;

    let currentDistance = calculateDistance({x1:anim.pos.x, y1:anim.pos.y}, {x2:anim.startPoint.x, y2:anim.startPoint.y});
    let netDistance = calculateDistance({x1:anim.startPoint.x, y1:anim.startPoint.y}, {x2:anim.endPoint.x, y2:anim.endPoint.y});

    // Test nach Ende der Animation, sonst Verschiebung des Objekts um den Geschwindigkeitswert, angeglichen mit der Zeit zwischen den Frames
    if(currentDistance < netDistance){
        anim.pos.x += anim.dx * part * deltaTime;
        anim.pos.y += anim.dy * part * deltaTime;
    } else{
        anim.pos.x = anim.endPoint.x;
        anim.pos.y = anim.endPoint.y;
        anim.isMoving = false;
    }
}

class AnimationObject { // @Carl Klassennamen schreibt man immer groß xD
    constructor(img, pos = {x: -100, y: -100}) {
        this.isMoving = false;
        this.isFlipping = false;
        this.pos = pos;
        this.img = img;
        this.widthAnteil = 1;
        animationObjects.push(this);
    }
    moveTo(posB, _time) {
        this.startPoint = {...this.pos}; //Gleifsetzen reicht nicht, da sont nur Object Poiter und nicht Werte kopiert. Durch JS Funktion werden alle werte einzelnt geklont also x und y
        this.time = _time;
        this.endPoint = posB;
        this.dx = (this.endPoint.x - this.startPoint.x) / this.time;
        this.dy = (this.endPoint.y - this.startPoint.y) / this.time;
        this.isMoving = true; // Updater Erkennt Handlungsgedarf
    }
    async changeSide(img = null) {
        if(!img) img = b.back;
        this.imgVorher = this.img;
        this.imgNacher = img;
        this.isFlipping = true;
        this.startFlippingTime = Date.now();
    }
    update(ctx, deltaTime) {
        if(this.isMoving) sinVel(this, {...this.pos});
        if(this.isFlipping) {
            const timeVerstrichen = Date.now()-this.startFlippingTime; //Seid beginn des Drehens der Karte
            this.widthAnteil = parabelfunktion(timeVerstrichen, flippingTime);  //Weil Wenn noch das alte dann schmaler werden wenn schon neue wieder breiter
            if(this.widthAnteil >= 1) { //Drehen Abgeschlossen!
                this.isFlipping = false;
                this.widthAnteil = 1;
                console.log("Drehen Abgeschlossen!");
            }
            if(this.img === this.imgVorher && timeVerstrichen >= flippingTime/2) this.img = this.imgNacher;
        }
        ctx.drawImage(this.img, this.pos.x - (growFactor * this.widthAnteil * cardWidth) / 2, this.pos.y - (growFactor * cardHeight) / 2, growFactor * cardWidth * this.widthAnteil, growFactor * cardHeight);
    }
}

window.animationObjects = [];
window.gameDrawThreads = {};

window.addDrawingThread = (callback = ()=>{}) => {
    const id = Math.random().toString();
    gameDrawThreads[id] = callback;
    return {remove: ()=>delete gameDrawThreads[id]};
};

let last = 0;
let deltaTime = 0;
function draw() {
    deltaTime = (Date.now() - last) / 1000;
    last = Date.now();
    Object.values(gameDrawThreads).forEach(c => c(ctx));
    animationObjects.forEach(o => o.update(ctx, deltaTime));
    requestAnimationFrame(draw);
}

// Funktion zur Berechnung der nächsten Koordinaten, jedoch mit einer Beschleunigung des Objekts mithilfe einer phasenverschobenen Sinuswelle


window.GameCard = class GameCard extends AnimationObject {
    constructor(kartenwert = null, img, pos = {x: -200, y: -200}) {
        super(img, pos);
        this.kartenwert = kartenwert; //Um was für eine Karte hanelt es  sich ist Null wenn Srerver noch nicht geantowrtet.
    }
    moveTo(...p) {
        super.moveTo(...p); //Einfach alle Paramzer Kopieren, weil eh gleich
    }
}