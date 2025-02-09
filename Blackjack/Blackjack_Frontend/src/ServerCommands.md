# Server Befehle 
- Karte senden: "Card:c:" + coat + ",v:" + value + ",p:" + value
- Dealer Karte senden "DealerCard:c:" + coat + ",v:" + value + ",p:" + points
- Dealer Karten senden: "DealerCards:t1:" + coat + ",v1:" + value + ";" +
                                      "t2:" + coat + ",v2:" + value + ";..."
- Verbindung akzeptiert: "acc"
- Umtauschen bestätigen: "ChipUpdate:" + chipAmount
- Kontostand übermitteln: "Bal:" + balance
- Auflösen eines Stacks: "Stack:p:" + points + ",s" + state

# Client Befehle
- Dealer Ziehen anfragen: "TakeDealer"
- Spieler Ziehen anfragen: "TakeUser"
- Einsatz senden: "Bet:" + value
- Anfrage Dealer Karten: "GetDealer"
- (ID senden: "ID:" + id)
- Umtauschen anfragen: "Exchange:" + ChipCount
- Beenden des Stacks: "EndStack:" + stackID
