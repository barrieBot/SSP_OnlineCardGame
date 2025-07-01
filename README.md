# SSP_OnlineCardGame
Das ist der deployment-build der Schere-Stein-Papier Online-Karten-Spiel

## Deployment

### CORS-Configutation
Fürs Deployment muss in der ApplicationConfiguration.java die Domaine des Servers als Remote-Origin freigegeben werde.
Die Datei ist zufinden unter:

[backend/src/main/java/game/CardGame/configs/ApplicationConfiguration.java](/backend/src/main/java/game/CardGame/configs/ApplicationConfiguration.java)

### Starting the Server

```docker-compose up```
