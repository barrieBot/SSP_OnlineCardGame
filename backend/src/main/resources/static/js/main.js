'use strict';

const userNmPg = document.querySelector("#Login");
const responsePg = document.querySelector("#Response")

const un_input = document.querySelector("#un" )
const tok_input= document.querySelector("#token")
const card_input = document.querySelector("#card_input")
const jwt_input = document.querySelector("#jwtInput")

var sub_connect = document.querySelector('#but_connect')
var sub_gen = document.querySelector("#but_new_game")
var sub_mit = document.querySelector("#but_en")
var sub_send = document.querySelector("#but_send")
var sub_draw = document.querySelector("#but_drew")
var sub_yield = document.querySelector("#but_yield")



var resp = document.querySelector("#Rueckgabe")

var stompClient = null;
var userN = null;
var tok = null;

var CardInput = null;


function create_game(event){
    console.log(typeof SockJS);
    console.log(typeof Stomp);
    userN = un_input.value.trim();

    if(userN){
        resp.innerHTML = resp.innerHTML.toString() + "Credentials ok </br>"

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        //add info für neues Spiel
        //Muss ich hier auch die Subscriptions für die Spezifischen Pfade machen?
        // '/game.{gameID}'
        // '/game.{gameID}.{userID}' oder so? oder ist das im Backend
        //muss ich testen ob es den stompClient schon gibt? Damit es immer der gleiche bleibt?
        //spezielleren Pfad für generieren festlegen -> @MessageMapping("/game.new")

        stompClient.connect({}, onConnected, onerror);

    }

    event.preventDefault();

}

function connect(event){
    console.log(typeof SockJS);
    console.log(typeof Stomp);
    userN = un_input.value.trim();
    tok = tok_input.value.trim();

    if(userN && tok){
        resp.innerHTML = resp.innerHTML.toString() + "Credentials ok </br>"

        let socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onerror);

        //Pfad für JoinGame und andere Effekte ist -> @MessageMapping("/game.config")

    }

    event.preventDefault();

}

function onConnected(){
    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.subscribe('/user/queue/private', onMessageReceived);

    //tell User to server
    stompClient.send('/app/game.addPlayer', {}, JSON.stringify({
        id: tok, sender:userN, type: "JOIN_GAME"
    }));

    resp.innerHTML += "Connected to WS </br>"
}


function onerror(){
    resp.innerHTML += "Connection failed  </br>"
}

function onMessageReceived(payload){
    const game_event = JSON.parse(payload.body);
    console.log("TEST message recieved: "+ payload)

    if(game_event){
        switch (game_event.type){
            case 'NEW_GAME':
                resp.innerHTML += `Game-Token: ${game_event.id.toString()} </br>`
                break;
            case 'START_GAME':
                resp.innerHTML += "Game Startet! </br>"
                break;
            case 'START_TURN':
                resp.innerHTML += "Your turn: Select Card or Draw: </br>"
                break;
            case 'DRAW_CARD':
                resp.innerHTML += `Received Card: ${game_event.id.toString()}</br>`
                break;
            case 'JOIN_GAME':
                resp.innerHTML += `New Player Joined: ${game_event.id.toString()}</br>`
                break;
            default: break;
        }
    }
}
function makeGame(){
        userN = un_input.value.trim();
        if(stompClient){
            const gameState = {
                sender: userN,
                action: 'NEW_GAME'
            };

            let jwt = jwt_input.value;
            stompClient.send('/app/game.new', {
                                                Authorization: `Bearer ${jwt}`
                                              }, JSON.stringify(gameState))
        }

        event.preventDefault();
}

function  placeCard(event){

    //Methode for Selecting Card
    const Card = 's5';
    if(Card && stompClient){
        const gameState = {
            id: Card,
            sender: userN,
            action: 'PLACE_CARD'
        };

        stompClient.send('/app/game.playerAction', {}, JSON.stringify(gameState))
    }

    event.preventDefault();
}

function  drawCard(event){


    event.preventDefault();
}

function sendCard(event){
    CardInput = card_input.value.trim();

    if(CardInput){

    }

    event.preventDefault();
}

function yieldTurn(event){

    event.preventDefault();
}


sub_connect.addEventListener("click", create_game)
sub_gen.addEventListener("click", makeGame);
sub_mit.addEventListener("click", connect);
sub_send.addEventListener("click", sendCard)
sub_draw.addEventListener("click", drawCard)
sub_yield.addEventListener("click", yieldTurn)