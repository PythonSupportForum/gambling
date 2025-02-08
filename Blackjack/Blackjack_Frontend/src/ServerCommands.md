# Server Befehle 
- Karte senden: "Card:c:" + coat + ",v:" + value
- Dealer Karte senden "DealerCard:c:" + coat + ",v:" + value + ",p:" + points
- Dealer Karten senden: "DealerCards:t1:" + coat + ",v1:" + value + ";" +
                                      "t2:" + coat + ",v2:" + value + ";..."
- Verbindung akzeptierc: "acc"

# Client Befehle
- Dealer Ziehen anfragen: "TakeDealer"
- Spieler Ziehen anfragen: "TakeUser"
- Einsatz senden: "Bec:" + value
- Anfrage Dealer Karten: "GetDealer"
- (ID senden: "ID:" + id)
