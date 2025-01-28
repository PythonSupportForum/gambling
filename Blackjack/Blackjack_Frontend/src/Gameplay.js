console.log("Run Script...");

document.addEventListener("DOMContentLoaded", () => {
    console.log("DOM Loaded!!!");
    document.getElementById("startGameButton").onclick = async (event) => {
        event.preventDefault();
        event.stopPropagation();
        console.log("Gami Stariiii!!!!");
        initVariables();
        initBackground();
        document.getElementById("GameContainer").classList.add("show");

        window.messageQueue = new MessageQueue('messageBox'); //Darmit weil continaer erst nach dom da

        setTimeout(welcome, 1000);
    };
});


const initBackground = async ()=> {
    const t = (await getGrafiksData()).table;
    addDrawingThread((ctx)=>{
        ctx.drawImage(t, 0, 0, ctx.canvas.width, ctx.canvas.height);
    });
}

window.ziehenStappel = null;


const mischeAnimationDelaler = () => {
    function calculateRowCoordinates(screenHeight, cardHeight) { //Um Beide reihen Mittäg zu platzuieren
        const spacing = (screenHeight - 2 * cardHeight) / 3;
        const y1 = spacing;
        const y2 = 2 * spacing + cardHeight;
        return [y1, y2];
    }
    return new Promise(async resolve => {
        const b = await getGrafiksData();

        const [y1, y2] = calculateRowCoordinates(window.innerHeight, cardHeight);
        const aS = new Stappel({x: 100, y: y1+cardHeight/2});
        const bS = new Stappel({x: 100, y: y2+cardHeight/2});

        console.log("Add Cards:", b.back);
        for(let i = 0; i < 10; i++) await aS.add(new GameCard(null, b.back), 0.2);
        console.log("Added Cards!");

        setTimeout(async ()=> {
            console.log("Start Copy!");
            await aS.copyStappel(bS);
            console.log("Copy to Ziehen!");
            await aS.copyStappel(ziehenStappel, -1, false); //Am Ende der Einleitungs Animation fliegen alle Karten zu dem Ziehstappel
            console.log("Cpoied to ziehen!");
            resolve();
        }, 1000);
    });
}

const welcome = async ()=>{

    await Promise.all([
        messageQueue.mehrneMesagesAnzeigen([
            "Hallo!",
            "Willkommen bei Blackjack!",
            "Von Carli und Florian!",
            "Der Dealer ist dem Spiel beigetreten...",
            "Die Karfen werden gemischt...",
            "Du wirst sterben!"
        ]),
        mischeAnimationDelaler()
    ]);
    console.log("Einleitungs Animation fertig!");

    //let temp = new GameCard(b.a_s, "spades", "a")

   // window.card = new GameCard(null, b.back, {x: 400, y: 200});



    overlaySetStatus(false);

   // card.moveTo(700, 400);

}

const initVariables = ()=>{
    window.ziehenStappel = new Stappel({x: window.innerWidth-cardWidth-30, y: 30}, "normal");
}