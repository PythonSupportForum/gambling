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

        animationObjects.push(new animationObject("rgba(255,0,0,0.5)", {startx:500, starty:500}, {endx:300, endy:300}, 1));
        animationObjects.push(new animationObject("rgba(0,102,255,0.5)", {startx:100, starty:100}, {endx:300, endy:300}, 1));
        animationObjects.push(new animationObject("rgba(255,250,0,0.5)", {startx:500, starty:100}, {endx:300, endy:300}, 1));
        animationObjects.push(new animationObject("rgba(255,255,255,0.5)", {startx:100, starty:500}, {endx:300, endy:300}, 1));

        for(let anim of animationObjects){
            newMoving(anim);
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
        if(anim.moves){
            sinVel(anim, {x:anim.pos.x, y:anim.pos.y});
        }
        else{
            let temp = anim.startPoint;
            anim.startPoint = anim.endPoint;
            anim.endPoint = temp;
            newMoving(anim);
        }

        ctx.beginPath();
        ctx.arc(anim.pos.x, anim.pos.y, 20 , 0, Math.PI * 2);
        ctx.fillStyle = anim.colour;
        ctx.fill();
        ctx.closePath();
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
        console.log(currentDistance, netDistance, anim.dx * part * deltaTime);
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

class animationObject{

    colour;
    moves = false;
    time = 0;
    startPoint = {x:0,y:0};
    endPoint = {x:0,y:0};
    pos = {x:0,y:0};
    dx = 0;
    dy = 0;

    constructor(colour, {startx, starty}, {endx, endy}, _time) {
        this.moves = false;
        this.time = _time;
        this.startPoint = {x:startx, y:starty};
        this.endPoint = {x:endx,y:endy};
        this.pos = {x:startx,y:starty};
        this.colour = colour;
    }
}