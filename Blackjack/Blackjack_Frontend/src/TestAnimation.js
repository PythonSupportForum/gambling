let ctx;
let currentPos = {x:0,y:0};

let distx = {x1:0, x2:0};
let disty = {y1:0, y2:0};
let time = 0.25;
let moving = false;

let last = 0;
let deltaTime = 0;

let [dx,dy] = [0,0];
let mouseX, mouseY;
let backgroundImg = new Image();
backgroundImg.src = '/../assets/Blackjack.jpg';
let cardImg = new Image();
cardImg.src = '../assets/karten/club/2.svg';
let cardWidth = 66;
let cardHeight = 100;

window.onload = function (){
    const canvas = document.getElementById("canvas");
    if (canvas.getContext) {
        ctx = canvas.getContext("2d");
        canvas.addEventListener('mousemove', function (event) {
            mouseX = event.clientX - canvas.getBoundingClientRect().left;
            mouseY = event.clientY - canvas.getBoundingClientRect().top;
        });

        last = Date.now();

        newMoving(currentPos, {x:300,y:300});

        draw();
    }
}

function draw() {
    ctx.drawImage(backgroundImg, 0, 0, ctx.canvas.width, ctx.canvas.height);

    deltaTime = (Date.now() - last) / 1000;
    last = Date.now();

    if (moving) {
        currentPos = sinVel({x:currentPos.x, y:currentPos.y});
    }

    ctx.drawImage(cardImg, currentPos.x - cardWidth / 2, currentPos.y - cardHeight / 2, cardWidth, cardHeight);

    requestAnimationFrame(draw);
}

function newMoving({x:x1, y:y1}, {x:x2, y:y2}) {
    moving = true;

    distx = {x1,x2};
    disty = {y1,y2};

    dx = (distx.x2 - distx.x1) / time;
    dy = (disty.y2 - disty.y1) / time;

    console.log('ehre ' + dx + ' ' + dy);
}

function sinVel({x, y}){

    let part = 1 - ((distx.x2 - x) / (distx.x2 - distx.x1));

    part = Math.sin(part * Math.PI + 0.1);

    if(x < distx.x2 && y < disty.y2){
        x += dx * part * deltaTime;
        y += dy * part * deltaTime;
    }
    else{
        moving = false;
    }

    return {x, y};
}