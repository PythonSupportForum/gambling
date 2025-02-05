# Server Befehle 
- Karte senden: "Card: t:" + type + ",v:" + value
- Dealer Karten senden: "DealerCards: t1:" + type + ",v1:" + value + ";" +
                                      "t2:" + type + ",v2:" + value + ";..."

# Client Befehle
- Dealer Ziehen anfragen: "TakeDealer"
- Spieler Ziehen anfragen: "TakeUser"
- Einsatz senden: "Bet:" + value
- Anfrage Dealer Karten: "GetDealer"
