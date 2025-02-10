console.log("Loaded Test Animation.js!");


let ctx;

const growFactor = 0.7;
const cardWidth = 200;
const cardHeight = 300;
const flippingTime = 500;
const faecherStackCardAbstand = 20;
const overlaySpeed = 0.01; //Wenn Element im Vordergrund, wie schnell schwarz
const overlayStaerke = 0.6;

const normalMoveTime = 0.5; //Pre vorher Einstellung → einezlen Anis weischen ab

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
    "0_c": "https://gambling.megdb.de/assets/karten/club/10.svg",
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
    "0_d": "https://gambling.megdb.de/assets/karten/diamond/10.svg",
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
    "0_h": "https://gambling.megdb.de/assets/karten/heart/10.svg",
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
    "0_s": "https://gambling.megdb.de/assets/karten/spade/10.svg",
    "j_s": "https://gambling.megdb.de/assets/karten/spade/J.svg",
    "q_s": "https://gambling.megdb.de/assets/karten/spade/Q.svg",
    "k_s": "https://gambling.megdb.de/assets/karten/spade/K.svg",

    "back": "https://gambling.megdb.de/assets/Kartenrücken.png",
    "table": "https://gambling.megdb.de/assets/table.png",
    "end": "https://gambling.megdb.de/assets/karten/end.png"
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

window.getGraphicsData = async ()=>{
    if(b) return b;
    if(!("bilder" in window)) window.bilder = loader(); //Erst Loader, dann auch bei vielen Aufrufen
    b = await bilder;
    console.log("Alle Assets (Bilder) Sehen breit!");
    return b;
}


let canvas;
let b; //Enthält Aufgelöstes promise oBjekt => Nach abgeschlosenen Laden

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
    getGraphicsData().then(()=>{});
});

function resizeCanvas(){
    canvas.style.width = "100%";
    canvas.style.height = "100%";
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
}

// Frame Generation

let nextButtons = {};
let resolveJaOrNo = [];
window.buttons = {
    addDynamicYesOrNeinButton: (name)=>new Promise(r => {
        console.log("Add Dynmaic button:", name);
        let resolved = false;
        const resolve = (...p)=>{
            console.log("Resolve Frage nach!");
            if(resolved) return;
            resolved = true;
            r(...p);
        }
        resolveJaOrNo.push(resolve);
        console.log(document.getElementById("buttonsDiv").classList.contains("show"));
        if(!document.getElementById("buttonsDiv").classList.contains("show")) nextButtons[name] = ()=>resolve(true);
        else {
            console.log("Show Button:", name, document.getElementById("buttonsDiv").querySelectorAll("."+name));
            [...document.getElementById("buttonsDiv").querySelectorAll("."+name)].forEach(b => {
                let old = b.onclick;
                b.onclick = ()=>{
                    if(old) old();
                    resolve(true);
                }
                b.classList.add("show");
            });
        }
    }),
    show: (types) =>{
        console.log("Show Buttons:", types, nextButtons);
        types = {...types};
        Object.keys(nextButtons).forEach(k => {
           const old = types[k];
           types[k] = ()=>{
               if(old) old();
               nextButtons[k]();
           }
        });
        nextButtons = {};

        document.getElementById("buttonsDiv").classList.add("show");
        [...document.getElementById("buttonsDiv").querySelectorAll("button")].forEach(b => b.classList.remove("show"))
        Object.keys(types).forEach(t => {
            [...document.getElementById("buttonsDiv").querySelectorAll("."+t.replace("_", ""))].forEach(b => {
                let old = b.onclick;
                b.onclick = ()=>{
                    if(old) old();
                    types[t]();
                }
                if(!t.startsWith("_")) b.classList.add("show"); //Damit nur als Platzhalter fürs Frontent
            });
        });
    },
    hide: ()=>{
        resolveJaOrNo.forEach(c => c(false));
        resolveJaOrNo = [];
        document.getElementById("buttonsDiv").classList.remove("show");
    }
}

function parabolaFunction(x, xMax) { /**Die Funktion erstellt eine gestauchte Parabel =>
                        Damit Dreh Bewegung der karte, F(x) gibt Anteil der Normalbreite an.
                        Bei  = 1000 und bei x = 1 ist sie 1. Der zweite Parameter gibt die Stauchung in Millisekunden an.
                        */
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
function getUniqueAttributes(obj1, obj2) { //Um die Elemente übern dem Overlay nicht vorher zu zihcne => ID Vergleich
    let result = {};
    for (let key in obj1) {
        if (!(key in obj2)) result[key] = obj1[key];
    }
    return result;
}

class AnimationObject { // @Carl Klassennamen schreibt man immer groß xD
    constructor(img, pos = {x: -100, y: -100}) {
        this.isMoving = false;
        this.isFlipping = false;
        this.onMoveEnd = [];
        this.pos = pos;
        this.img = img ? img.cloneNode(true) : null;
        this.widthAnteil = 1;
        this.id = Math.random().toString(); //Um Effizient key => Vlaue zugriff in objects
        animationObjects[this.id] = this;
        this.allgemeinScale = 1; //Um Karte in Vordergrund bringen zu kömnen
    }
    moveTo(posB, _time = 0.5) {
        return new Promise((resolve)=>{ //Wenn Bewegung Beended ist, damit für Gameplay
            this.onMoveEnd.push(resolve);
            this.startPoint = {...this.pos}; //Gleifsetzen reicht nicht, da sont nur Object Pointer und nicht Werte kopiert. Durch JS Funktion werden alle werte einzelnt geklont also x und y
            this.time = _time;
            this.endPoint = posB;
            this.dx = (this.endPoint.x - this.startPoint.x) / this.time;
            this.dy = (this.endPoint.y - this.startPoint.y) / this.time;
            this.isMoving = true; // Updater Erkennt Handlungsbedarf

            delete animationObjects[this.id];
            animationObjects[this.id] = this; //an oberste Stelle, über die anderen Karten!
        });
    }
    endMove() {
        this.onMoveEnd.forEach(c => c()); //Promise Auflösen
        this.isMoving = false;
    }
    changeSide(img = null) {
        return new Promise(async resolve => {
            if(!img) img = b.back;
            if(typeof img === "string") img = (await getGraphicsData())[img];
            this.imgNacher = img.cloneNode(true);
            this.isFlipping = true;
            this.startFlippingTime = Date.now();
            console.log("Drehen angefangen", this);
            this.onMoveEnd.push(resolve);
        });
    }
    update(ctx, deltaTime) {
        if(this.isMoving) sinVel(this, {...this.pos});
        if(this.isFlipping) {
            const timePassed = Date.now()-this.startFlippingTime; //Seid beginn des Drehens der Karte
            this.widthAnteil = parabolaFunction(timePassed, flippingTime);  //Weil Wenn noch das alte dann schmaler werden wenn schon neue wieder breiter
            if(this.img !== this.imgNacher && timePassed >= flippingTime/2) this.img = this.imgNacher; //Die hälfte also ganz weg neues bild zeichen
            if(this.widthAnteil >= 1 && timePassed >= flippingTime/2) { //Drehen Abgeschlossen!
                this.isFlipping = false;
                this.widthAnteil = 1;
                this.img = this.imgNacher;
                console.log("Drehen Abgeschlossen!");
                this.endMove();
            }
        }
        ctx.drawImage(this.img, this.pos.x - (growFactor * this.widthAnteil * cardWidth * this.allgemeinScale) / 2, this.pos.y - (growFactor * cardHeight * this.allgemeinScale) / 2, growFactor * cardWidth * this.allgemeinScale * this.widthAnteil, growFactor * cardHeight * this.allgemeinScale);
    }
}

window.animationObjects = {};
window.higherAnimationObjectsTop = [];
window.gameDrawThreads = {};

window.addDrawingThread = (callback = ()=>{}) => {
    const id = Math.random().toString();
    gameDrawThreads[id] = callback;
    return {remove: ()=>delete gameDrawThreads[id]};
};

window.focusElements = {}; //Elemente die vor dem Overlay schweben => Für Draw Loop
let overlayAlpha = 0; //Aktuelle Overlay Stärke => 0 = Kein Overlay
let overlayStatus = false;
window.focusElementWithOverlay = (elements)=>{
    overlayStatus = true;
    console.log("Focus Elements:", elements);
    elements.forEach(key => focusElements[typeof key === "string" ? key : key.id] = typeof key === "string" ? animationObjects[key] : key);
    return {
        end: ()=>{
            window.focusElements = {};
            overlayStatus = false;
        }
    }
}

let last = 0;
let deltaTime = 0;

function draw() {
    deltaTime = (Date.now() - last) / 1000;
    last = Date.now();
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    try {
        Object.values(gameDrawThreads).forEach(c => c(ctx, deltaTime));
        Object.values(getUniqueAttributes(animationObjects, focusElements)).forEach(o => o.update(ctx, deltaTime));
        higherAnimationObjectsTop.forEach(o => o.update(ctx, deltaTime)); //Damit die Kreise höher wiegen, über dem rest geuecgen werden
        if(overlayAlpha > 0) {
            ctx.fillStyle = `rgba(0, 0, 0, ${overlayAlpha})`;
            ctx.fillRect(0, 0, canvas.width, canvas.height);
        }
        if (overlayAlpha < overlayStaerke && overlayStatus) overlayAlpha += overlaySpeed;
        else if(overlayAlpha > 0 && !overlayStatus) overlayAlpha -= overlaySpeed;

        Object.values(focusElements).forEach(o => o.update(ctx, deltaTime));
    } catch(e) {
        console.log(e);
    }
    requestAnimationFrame(draw);
}

// Funktion zur Berechnung der nächsten Koordinaten, jedoch mit einer Beschleunigung des Objekts mithilfe einer phasenverschobenen Sinuswelle


window.GameCard = class GameCard extends AnimationObject {
    constructor(cardValue = 0, img, pos = {x: -200, y: -200}) {
        super(img, pos);
        this.cardValue = cardValue; //Um was für eine Karte hanelt es  sich ist Null wenn Srerver noch nicht geantowrtet.
        this.wertZaehlen = true; //Der Anteil des Kartenstappelwertes, der noch nicht gerednert wird, da die Animation noch running ist.
        this.StackReference = null; //Auf welchem Stack ist die Karte => Hat nur Grafische Auswirkungne nix Gamepla
    }
    moveTo(...p) {
        return super.moveTo(...p); //Einfach alle Parameter Kopieren, weil eh gleich
    }
    removeFromStack() {
        if(this.StackReference) this.StackReference.remove(); //Falls auf Altem Erst Weg!
    }
    putOnStack(Stack, time = normalMoveTime) { //Auf einen Stack bewegen => Fächereffekt Möglich!
        this.removeFromStack();
        this.StackReference = Stack._add(this, time);
        return this.StackReference.promise;
    }
    aufdecken({type, points}) {
        this.cardValue = points;
        return this.changeSide(type);
    }
}

window.Stack = class Stack {
    constructor(pos = {x: 0, y: 0}, type = "faecher", faecherSteps = faecherStackCardAbstand) {
        this.pos = pos;
        this.cards = {};
        this.type = type;
        this.faecherSteps = faecherSteps; //Nur relevant für Type = Faecher
        this.einsatz = -1;
        this.showPoints = false;
        this.showInfo = false;
        this.restMaxCount = -1;
        this.direktWertUpdate = true; //Soll der angezeigte Wert schon beim Losfliegen aktualisiert werden
    }
    startShowPoints() {
        this.showInfo = true;
        if(this.showPoints) return;
        this.showPoints = true;
        higherAnimationObjectsTop.push(this);
    }
    update(ctx, deltaTime) {
        if(!this.showInfo) return;
        const y = this.pos.y;
        const x = this.pos.x + (((this.length() - 1) * this.faecherSteps) / 2);
        // Kreis, in welchem der Wert des aktuellen Stacks angezeigt wird
        const radius = 50; // Größe des Kreises
        // Schwarzen Kreis zeichnen
        ctx.fillStyle = "black";
        ctx.beginPath();
        ctx.arc(x, y, radius, 0, Math.PI * 2);
        ctx.fill();
        // Punktzahl zeichnen (rot)
        ctx.font = "bold "+(this.einsatz === -1 ? 55 : 40).toString()+"px Arial"; //Wenn Dealer Stappel dann fetterer text
        ctx.fillStyle = "red";
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        ctx.fillText(this.wert().toString(), x, this.einsatz === -1 ? y+5 : y-10);
        // Einsatz zeichnen (gold) unterhalb der Punktzahl
        if (this.einsatz && this.einsatz >= 0) {
            ctx.font = "bold 20px Arial";
            ctx.fillStyle = "gold";
            ctx.fillText(this.einsatz.toString() + "¢", x, y + 20);
        }
    }
    length() {
        return Object.keys(this.cards).length;
    }
    wert() {
        let w = 0;
        Object.values(this.cards).forEach(c =>{
            if(!isNaN(Number(c.wertZaehlen?c.cardValue:0))){
                w += Number(c.wertZaehlen?c.cardValue:0)
            }
        });
        return w;
    }
    getOberste() {
        console.log("Get Oberste:", this, this.cards);
        return Object.keys(this.cards).length === 0 ? null : Object.values(this.cards)[Object.values(this.cards).length-1];
    }
    takeCard(count = 1) { //Macht Dasselbe wie get Oberste nur entfern gleichezig!
        const c = this.getObersteViele(count);
        c.forEach(c => c.removeFromStack());
        return c;
    }
    getObersteViele(count = 1) {
        console.log("Get Oberste viele:", this, this.cards);

        return Object.keys(this.cards).length === 0 ? [] : Object.values(this.cards).slice(-count);
    }
    moveTo(posB, time = normalMoveTime) { //Um Stack mit allen Karten zu bewegen
        const diff = {x: posB.x-this.pos.x, y:  posB.y-this.pos.y}; //Relwtive Befweung des Stappels
        console.log("Mve S:", diff);
        this.showInfo = false;
        this.pos = posB;
        setTimeout(()=>this.showInfo=true, 700);
        return Promise.all(Object.values(this.cards).map(c => c.moveTo({x: c.pos.x+diff.x, y: c.pos.y+diff.y})));
    }
    add(card, time = normalMoveTime) {
        return card.putOnStack(this, time);
    }
    _add(card, time = normalMoveTime) {
        const id = Math.random().toString();
        const cardPos = {y: this.pos.y, x: this.type === "faecher" ? this.pos.x+this.faecherSteps*Object.keys(this.cards).length : this.pos.x}
        if(!this.direktWertUpdate) card.wertZaehlen = false;
        this.cards[id] = card;
        const promise = card.moveTo(cardPos, time);
        (async ()=>{
            await promise;
            card.wertZaehlen = true;
        })().then(()=>{});
        return {remove: ()=>{
            if(id in this.cards) delete this.cards[id]
        }, promise};
    }
    async copyStack(andererStack, count = -1, reverse = true, time = normalMoveTime) { //Um Ganzen Stack auf anderen Stack zu bewegen. Reverse Gibt an ob der Stack umgedreht werden soll  oder niht
        if(andererStack === this) return; //Soll nicht auf sich selber sondt => Unsendlich Loop
        console.log("Copy:", this, andererStack);
        if(reverse) {
            while(this.getOberste() && count !== 0) {
                console.log("Put One...");
                const p = this.getOberste().putOnStack(andererStack, time);
                await p;
                count--;
                await new Promise(resolve => setTimeout(resolve, 1000));
            }
        } else {
            const pList = (count < 0 ? Object.values(this.cards) : (Object.values(this.cards).slice(-count))).map(card => card.putOnStack(andererStack, time));
            console.log("Copy without Reverse..", count, pList, this.cards);

            await Promise.all(pList);
        } //Warten bis alle Zielort erreicht haben
    }
}