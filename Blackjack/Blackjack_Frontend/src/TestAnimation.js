let ctx;

let last = 0;
let deltaTime = 0;

let animationObjects = [];

let mouseX, mouseY;
let backgroundImg = new Image();
backgroundImg.src = '/../assets/Blackjack.jpg';
let cardImg2 = new Image();
cardImg2.src = '../assets/karten/club/2.svg';
let cardImg3 = new Image();
cardImg3.src = '../assets/karten/club/3.svg';
let cardWidth = 66;
let cardHeight = 100;

window.onload = function (){
    const canvas = document.getElementById("canvas");
    if (canvas.getContext) {
        ctx = canvas.getContext("2d");

        last = Date.now();

        animationObjects.push(new animationObject("#ff0000", {startx:500, starty:500}, {endx:300, endy:300}, 0.15));
        animationObjects.push(new animationObject("#00ffd7", {startx:100, starty:100}, {endx:300, endy:300}, 0.15));

        for(let anim of animationObjects){
            newMoving(anim, {x1:anim.start.x, y1:anim.start.y}, {x2:anim.end.x, y2:anim.end.y});
        }

        draw();
    }
}

// Frame Generation
function draw() {
    // Malt Hintergrund auf den vorherigen Frame
    ctx.fillStyle = "#000000";
    ctx.fillRect(0, 0, ctx.canvas.width, ctx.canvas.height);

    // Zeit zwischen den Frames in Sekunden
    deltaTime = (Date.now() - last) / 1000;
    last = Date.now();

    for (let anim of animationObjects){
        if(anim.moving){
            anim.currentPos = sinVel(anim, {x:anim.currentPos.x, y:anim.currentPos.y});
        }

        ctx.beginPath();
        ctx.arc(anim.currentPos.x, anim.currentPos.y, 20 , 0, Math.PI * 2);
        ctx.fillStyle = anim.color;
        ctx.fill();
        ctx.closePath();
    }

    requestAnimationFrame(draw);
}

// Deklariert eine neue Animation
function newMoving(anim, {x:x1, y:y1}, {x:x2, y:y2}) {
    anim.moving = true;

    anim.start = {x:x1,y:y1};
    anim.end = {x:x2,y:y2};

    anim.dx = (anim.end.x - anim.start.x) / anim.time;
    anim.dy = (anim.end.y - anim.start.y) / anim.time;
}

// Funktion zur Berechnung der nächsten Koordinaten, jedoch mit einer Beschleunigung des Objekts mithilfe einer phasenverschobenen Sinuswelle
function sinVel(anim, {x, y}){

    // Prozentualer Anteil des Weges der zurückgelegt wurde
    let part = 1 - ((anim.end.x - x) / (anim.end.x - anim.start.x));

    // Faktor zur Beschleunigung: Die Geschwindigkeit verändert sich im Verlauf einer um 0,1 phasenverschobenen Sinuskurve, ermöglicht weichere Animation
    part = Math.sin(part * Math.PI + 0.1);

    let currentDistance = calculateDistance({x1:anim.currentPos.x, y1:anim.currentPos.y}, {x2:anim.start.x, y2:anim.start.y});
    let netDistance = calculateDistance({x1:anim.start.x, y1:anim.start.y}, {x2:anim.end.x, y2:anim.end.y});

    // Test nach Ende der Animation, sonst Verschiebung des Objekts um den Geschwindigkeitswert, angeglichen mit der Zeit zwischen den Frames
    if(currentDistance < netDistance){
        x += anim.dx * part * deltaTime;
        y += anim.dy * part * deltaTime;
    }
    else{
        anim.moving = false;
    }

    return {x, y};
}

function calculateDistance({x1, y1}, {x2, y2}) {
    return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
}

class animationObject{

    colour;
    moving = false;
    time = 0;
    start = {x:0, y:0};
    end = {x:0,y:0};
    currentPos = {x:0,y:0};
    dx = 0;
    dy = 0;

    constructor(colour, {startx, starty}, {endx, endy}, _time) {
        this.moving = false;
        this.time = _time;
        this.start = {x:startx, y:starty};
        this.end = {x:endx,y:endy};
        this.currentPos = {x:0,y:0};
        this.colour = colour;
    }
}