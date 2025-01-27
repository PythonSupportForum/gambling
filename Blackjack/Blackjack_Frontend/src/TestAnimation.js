let ctx;

let last = 0;
let deltaTime = 0;

let stationaryObjects = [];
let animationObjects = [];

let growFactor = 0.7;
let cardWidth = 200;
let cardHeight = 300;

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

let b;

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

window.gameDrawThreads = {};
window.addDrawingThread = (callback = ()=>{}) => {
    const id = Math.random().toString();
    gameDrawThreads[id] = callback;
    return {remove: ()=>delete gameDrawThreads[id]};
};

function draw() {
    deltaTime = (Date.now() - last) / 1000;
    last = Date.now();
    Object.values(gameDrawThreads).forEach(c => c(ctx));

    for (let i = 0; i < animationObjects.length; i++){
        let anim = animationObjects[i];
        if(anim.moves) sinVel(anim, {x:anim.pos.x, y:anim.pos.y});
        else {
            stationaryObjects.push(new stationaryObject(anim.img, {posx:anim.pos.x, posy:anim.pos.y}, anim.rev));
            animationObjects.splice(i, 1);
        }
        if(anim.rev) ctx.drawImage(anim.img, anim.pos.x - (growFactor * cardWidth) / 2, anim.pos.y - (growFactor * cardHeight) / 2, growFactor * cardWidth, growFactor * cardHeight);
        else ctx.drawImage(b.back, anim.pos.x - (growFactor * cardWidth) / 2, anim.pos.y - (growFactor * cardHeight) / 2, growFactor * cardWidth, growFactor * cardHeight);
    }
    for(let i = 0; i < stationaryObjects.length; i++) {
        let stat = stationaryObjects[i];
        if (stat.flipping) {
            if (stat.rev) {
                stat.width += 1000 * deltaTime;
                console.log("true",stat.width,stat.height);
                if (stat.width >= cardWidth) {
                    stat.flipping = false;
                    stat.width = cardWidth;
                }
                ctx.drawImage(stat.img, stat.pos.x - (growFactor * stat.width) / 2, stat.pos.y - (growFactor * cardHeight) / 2, growFactor * stat.width, growFactor * cardHeight);
            } else {
                stat.width -= 1000 * deltaTime;
                console.log("false",stat.width);
                if (stat.width <= 0) {
                    stat.rev = true;
                    stat.width = 1;
                }
                ctx.drawImage(b.back, stat.pos.x - (growFactor * stat.width) / 2, stat.pos.y - (growFactor * cardHeight) / 2, growFactor * stat.width, growFactor * cardHeight);
            }
        }
        else {
            if (stat.rev) {
                ctx.drawImage(stat.img, stat.pos.x - (growFactor * cardWidth) / 2, stat.pos.y - (growFactor * cardHeight) / 2, growFactor * cardWidth, growFactor * cardHeight);
            } else {
                ctx.drawImage(b.back, stat.pos.x - (growFactor * cardWidth) / 2, stat.pos.y - (growFactor * cardHeight) / 2, growFactor * cardWidth, growFactor * cardHeight);
            }
        }
    }
    requestAnimationFrame(draw);
}

// Deklariert eine neue Animation
function newMoving(anim) {
    anim.moves = true;
    anim.dx = (anim.endPoint.x - anim.startPoint.x) / anim.time;
    anim.dy = (anim.endPoint.y - anim.startPoint.y) / anim.time;
}

// Funktion zur Berechnung der nächsten Koordinaten, jedoch mit einer Beschleunigung des Objekts mithilfe einer phasenverschobenen Sinuswelle
function sinVel(anim){

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
    }
    else{
        anim.pos.x = anim.endPoint.x;
        anim.pos.y = anim.endPoint.y;
        anim.moves = false;
    }
}

function calculateDistance({x1, y1}, {x2, y2}) {
    return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
}

class animationObject {
    constructor(img, {x: startx, y: starty}, {x: endx, y: endy}, _time, rev) {
        this.moves = false;
        this.time = _time;
        this.startPoint = {x:startx, y:starty};
        this.endPoint = {x:endx,y:endy};
        this.pos = {x:startx,y:starty};
        this.img = img;
        this.rev = rev;
    }
    update() {

    }
}

class stationaryObject{
    img;
    pos = {x:0,y:0}
    rev;
    flipping = false;
    width = cardWidth;
    height = cardHeight;

    constructor(img, {posx, posy}, rev) {
        this.img = img;
        this.pos = {x:posx, y:posy};
        this.rev = rev;
    }
}

window.GameCard = class GameCard {

    constructor(img, coat, val, pos = {x: -200, y: -200}) {
        this.img = img;
        this.coat = coat;
        this.val = val;
        this.pos = pos
    }
    moveTo(posTo, time = 0.5) {
        const a = new animationObject(this.img, this.pos, posTo, time, false);
        newMoving(a);
        animationObjects.push(a);
    }
}