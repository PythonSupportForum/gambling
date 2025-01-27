console.log("Run Script...");

document.addEventListener("DOMContentLoaded", () => {
    console.log("DOM Loaded!!!");
    document.getElementById("startGameButton").onclick = (event) => {
        event.preventDefault();
        event.stopPropagation();
        console.log("Gami Stariiii!!!!");
        document.getElementById("GameContainer").classList.add("show");

        window.messageQueue = new MessageQueue('messageBox'); //Darmit weil continaer erst nach dom da

        setTimeout(welcome, 1000);
    };
});



let b;
const welcome = async ()=>{
    await messageQueue.mehrneMesagesAnzeigen([
        "Hallo!",
        "Willkommen bei Blackjack!",
        "Von Carli und Florian!",
        "Der Dealer ist dem Spiel beigetreten...",
        "Die Karfen werden gemischt...",
        "Du wirst sterben!"
    ]);
    b = (await bilder);

    let temp = new GameCard(b.a_s, "spades", "a")


    addDrawingThread((ctx)=>{
        ctx.drawImage(b.table, 0, 0, ctx.canvas.width, ctx.canvas.height);

    });
}