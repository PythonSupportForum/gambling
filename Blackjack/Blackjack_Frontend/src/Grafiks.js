console.log("Loaded Test Animation.js!");


let ctx;

let growFactor = 0.7;
let cardWidth = 200;
let cardHeight = 300;
let flippingTime = 1000;
let faecherStappelCardAbstand = 20;

let normalMoveTime = 0.5; //Pre Einstellung => einezlen Anis weischen ab

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
const loader = ()=>{
    if(Object.keys(toLoad).length === 0) return;
    return new Promise((resolve)=>{
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

window.getGrafiksData = async ()=>{
    if(b) return b;
    if(!("bilder" in window)) window.bilder = loader(); //Erst Loader, dann auch bei vielen Aufrufen
    b = await bilder;
    console.log("Alle Assets (ilder) Sehen breit!");
    return b;
}


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
    if(event.data.toString().indexOf('acc') === 0) clientID = Number(msg.substring(4, msg.length));
};

// Fehlerbehandlung
socket.onerror = (error) => {
    console.error('WebSocket-Fehler:', error);
};

// Verbindung geschlossen
socket.onclose = () => {
    console.log('Verbindung zum Server geschlossen.');
};

window.overlaySetStatus = (s = true) => document.getElementById("overlay").classList[s ? "remove" : "add"]("hide");


window.addEventListener('resize', resizeCanvas);
window.addEventListener("load", async function () {
    console.log("Loaded Game Content & Variables!");
    canvas = document.getElementById("canvas");
    resizeCanvas();
    if (canvas.getContext) {
        ctx = canvas.getContext("2d");
        last = Date.now();
        draw();
        console.log("Started Drawing...");
    } else console.log("Error! No Canvas!");
    getGrafiksData().then(()=>{});
});

function resizeCanvas(){
    canvas.style.width = "100%";
    canvas.style.height = "100%";
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
}

// Frame Generation


function parabelfunktion(x, xMax) { //Die Funktion erstellt eine gestrauchte Parabel => Damit Dreh Bewegung der karte, F(x) gibt Anteil der Normalbreite an. Bei  = 1000 und bei x = 1 ist sie 1. Der zweite parameter gibt die Stauchung in Millisekunden an
    // Der Scheitelpunkt liegt in der Mitte zwischen 0 und xMax
    const x0 = xMax / 2;

    // Der Streckungsfaktor der Parabel (positiv, damit sie nach oben geöffnet ist)
    const a = 4 / (xMax ** 2);

    // Berechne den Wert der Parabelgleichung
    const y = a * (x - x0) ** 2;

    // Stelle sicher, dass der Wert zwischen 0 und 1 liegt
    return Math.max(0, Math.min(1, y));
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
        anim.endMove();
    }
}

class AnimationObject { // @Carl Klassennamen schreibt man immer groß xD
    constructor(img, pos = {x: -100, y: -100}) {
        this.isMoving = false;
        this.isFlipping = false;
        this.onMoveEnd = [];
        this.pos = pos;
        this.img = img;
        this.widthAnteil = 1;
        this.id = Math.random().toString(); //Um Effizient key => Vlaue zugriff in objects
        animationObjects[this.id] = this;
        this.allgemeinScale = 1; //Um Karte in Vordergrund bringen zu kömnen
    }
    moveTo(posB, _time = 0.5) {
        return new Promise((resolve)=>{ //Wenn Bewegung Beended ist, damit für Gameplay
            this.onMoveEnd.push(resolve);
            this.startPoint = {...this.pos}; //Gleifsetzen reicht nicht, da sont nur Object Poiter und nicht Werte kopiert. Durch JS Funktion werden alle werte einzelnt geklont also x und y
            this.time = _time;
            this.endPoint = posB;
            this.dx = (this.endPoint.x - this.startPoint.x) / this.time;
            this.dy = (this.endPoint.y - this.startPoint.y) / this.time;
            this.isMoving = true; // Updater Erkennt Handlungsgedarf

            delete animationObjects[this.id];
            animationObjects[this.id] = this; //an oberste Stelle, über die anderen Karten!
        });
    }
    endMove() {
        console.log("Move End! => Promise!");
        this.onMoveEnd.forEach(c => c()); //Promise Auflösen
        this.isMoving = false;
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
            if(this.img === this.imgVorher && timeVerstrichen >= flippingTime/2) this.img = this.imgNacher; //Die hälfe also ganz weg neues bild zeichen
            if(this.widthAnteil >= 1) { //Drehen Abgeschlossen!
                this.isFlipping = false;
                this.widthAnteil = 1;
                console.log("Drehen Abgeschlossen!");
            }
        }
        ctx.drawImage(this.img, this.pos.x - (growFactor * this.widthAnteil * cardWidth * this.allgemeinScale) / 2, this.pos.y - (growFactor * cardHeight * this.allgemeinScale) / 2, growFactor * cardWidth * this.allgemeinScale * this.widthAnteil, growFactor * cardHeight * this.allgemeinScale);
    }
}

window.animationObjects = {};
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
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    try {
        Object.values(gameDrawThreads).forEach(c => c(ctx));
        Object.values(animationObjects).forEach(o => o.update(ctx, deltaTime));
    } catch(e) {
        console.log(e);
    }
    requestAnimationFrame(draw);
}

// Funktion zur Berechnung der nächsten Koordinaten, jedoch mit einer Beschleunigung des Objekts mithilfe einer phasenverschobenen Sinuswelle


window.GameCard = class GameCard extends AnimationObject {
    constructor(kartenwert = null, img, pos = {x: -200, y: -200}) {
        super(img, pos);
        this.kartenwert = kartenwert; //Um was für eine Karte hanelt es  sich ist Null wenn Srerver noch nicht geantowrtet.
        this.stappelReferenze = null; //Auf welchem Stappel ist die Karte => Hat nur Grafische Auswirkungne nix Gameplay
    }
    moveTo(...p) {
        return super.moveTo(...p); //Einfach alle Paramzer Kopieren, weil eh gleich
    }
    putOnStappel(stappel, time = normalMoveTime) { //Auf einen Stappel bewegen => Fächereffekt Möglich!
        console.log("PUT ON STAPEL:", this.stappelReferenze);
        if(this.stappelReferenze) this.stappelReferenze.remove(); //Falls auf Altem Erst Weg!
        this.stappelReferenze = stappel._add(this, time);
        return this.stappelReferenze.promise;
    }
}

window.Stappel = class Stappel {
    constructor(pos = {x: 0, y: 0}, type = "faecher") {
        this.pos = pos;
        this.cards = {};
        this.type = type;
    }
    getOberste() {
        console.log("Get Oberste:", this, this.cards);
        return Object.keys(this.cards).length === 0 ? null : Object.values(this.cards)[Object.values(this.cards).length-1];
    }
    moveTo(posB, time = normalMoveTime) { //Um Stappel mit allen Karten zu bewegen
        const old = this.cards;
        this.cards = {};
        return Promise.all(Object.values(old).map(c => this.add(c, time)));
    }
    add(card, time = normalMoveTime) {
        return card.putOnStappel(this, time);
    }
    _add(card, time = normalMoveTime) {
        const id = Math.random().toString();
        const cardPos = {y: this.pos.y, x: this.type === "faecher" ? this.pos.x+faecherStappelCardAbstand*Object.keys(this.cards).length : this.pos.x}
        console.log("Add To Stappel:", this, id);
        this.cards[id] = card;
        const promise = card.moveTo(cardPos, time);
        return {remove: ()=>{
            if(id in this.cards) delete this.cards[id]
        }, promise};
    }
    async copyStappel(andererStappel, count = -1, reverse = true, time = normalMoveTime) { //Um Ganzen Stappel auf anderen Stappel zu bewegen. Reverse Gibt an ob der Stappel umgedreht werden soll  oder niht
        if(andererStappel === this) return; //Soll nicht auf sich selber sondt => Unsendlich Loop
        console.log("Copy:", this, andererStappel);
        if(reverse) {
            while(this.getOberste() && count !== 0) {
                console.log("Put One...");
                const p = this.getOberste().putOnStappel(andererStappel, time);
                await p;
                count--;
                await new Promise(resolve => setTimeout(resolve, 1000));
            }
        } else {
            const pList = (count < 0 ? Object.values(this.cards) : (Object.values(this.cards).slice(-count))).map(card => card.putOnStappel(andererStappel, time));
            console.log("Copy without Reverse..", count, pList, this.cards);

            await Promise.all(pList);
        } //Warten bis alle Zielort erreicht haben
    }
}