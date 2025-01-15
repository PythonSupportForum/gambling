let ctx;
let x,y;
let mouseX, mouseY;
let backgroundImg = new Image();
backgroundImg.src = '/../assets/Blackjack.jpg';
let cardImg = new Image();
cardImg.src = '/../assets/karten/club/2.svg';
let cardWidth = 132;
let cardHeight = 200;

window.onload = function (){
    const canvas = document.getElementById("canvas");
    if (canvas.getContext) {
        ctx = canvas.getContext("2d");
        [x,y] = [0,ctx.canvas.height/2];
        canvas.addEventListener('mousemove', function (event) {
            mouseX = event.clientX - canvas.getBoundingClientRect().left;
            mouseY = event.clientY - canvas.getBoundingClientRect().top;
        });
        ctx.moveTo(x,y);
        draw();
    }
}

function draw() {
    ctx.beginPath();
    ctx.moveTo(x,y);
    [x,y] = [x + 1, sinVel(x,1)];
    ctx.lineTo(x, y);
    ctx.stroke();
    ctx.closePath();

    requestAnimationFrame(draw);
}

function sinVel({x:x1, y:y1}, xstep){
    let dx = xstep + x1/600;

    let rety = Math.sin(dx * Math.PI);

    return rety;
}