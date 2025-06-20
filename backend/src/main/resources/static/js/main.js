'use strict';

const userNmPg = document.querySelector("#Login");
const responsePg = document.querySelector("#Response")

const usernameInput = document.querySelector("#usernameInput" )
const emailInput = document.querySelector("#emailInput")
const passwordInput = document.querySelector("#passwordInput")
const jwtInput = document.querySelector("#jwtInput")
const tok_input= document.querySelector("#token")
const card_input = document.querySelector("#card_input")
const gameCodeInput = document.querySelector("#gameCodeInput")
const cardNameInput = document.querySelector("#cardNameInput")
const cardValueInput = document.querySelector("#cardValueInput")
const cardEventInput = document.querySelector("#cardEventInput")


var sub_connect = document.querySelector('#connectWSButton')
var sub_gen = document.querySelector("#but_new_game")
var sub_join_game = document.querySelector("#but_join_game")
var sub_start_game = document.querySelector("#startGameButton")
var sub_send = document.querySelector("#but_send")
var sub_draw = document.querySelector("#but_drew")
var sub_yield = document.querySelector("#but_yield")
var sub_register = document.querySelector("#registerButton")
var sub_login = document.querySelector("#loginButton")
var sub_play_card = document.querySelector("#playCardButton")



var resp = document.querySelector("#Rueckgabe")

var stompClient = null;
var userN = null;
var tok = null;

var CardInput = null;


function register() {
    fetch("http://localhost:8080/api/auth/signup", {
                                                   method: 'POST',
                                                   headers: {
                                                     'Accept': 'application/json',
                                                     'Content-Type': 'application/json'
                                                   },
                                                   body: JSON.stringify(
                                                   {
                                                        username: usernameInput.value,
                                                        email: emailInput.value,
                                                        password: passwordInput.value
                                                   })
                                                 })
        .then((res) => res.json())
        .then((data) => {
            console.log(data);
        });
}


function login() {
    fetch("http://localhost:8080/api/auth/login", {
                                                   method: 'POST',
                                                   headers: {
                                                     'Accept': 'application/json',
                                                     'Content-Type': 'application/json'
                                                   },
                                                   body: JSON.stringify(
                                                   {
                                                        username: usernameInput.value,
                                                        password: passwordInput.value
                                                   })
                                                 })
        .then((res) => res.json())
        .then((data) => {
            console.log(data);
        });
}


function connect(event){
    userN = usernameInput.value.trim();

    if(userN){
        let socket = new SockJS('/api/ws');
        stompClient = Stomp.over(socket);
        stompClient.connect({}, onConnected, onerror);
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
        userN = usernameInput.value.trim();
        if(stompClient){
            const createGameDto = {
                displayName: userN
            };

            let jwt = jwtInput.value;
            stompClient.send('/app/game.new', {
                                                Authorization: `Bearer ${jwt}`
                                              }, JSON.stringify(createGameDto))
        }

        event.preventDefault();
}


function joinGame(){
        let gameCodeValue = gameCodeInput.value;
        userN = usernameInput.value.trim();
        if(stompClient){
            const gameState = {
                gameCode: gameCodeValue,
                displayName: userN,
                action: 'JOIN_GAME'
            };

            let jwt = jwtInput.value;
            stompClient.send('/app/game.join', {
                                                Authorization: `Bearer ${jwt}`
                                              }, JSON.stringify(gameState))
        }

        event.preventDefault();
}

function startGame() {
    let gameCodeValue = gameCodeInput.value;
    if(stompClient){
        const gameState = {
            gameCode: gameCodeValue,
            action: 'START_GAME'
        };

        let jwt = jwtInput.value;
        stompClient.send('/app/game.start', {
                                            Authorization: `Bearer ${jwt}`
                                          }, JSON.stringify(gameState))
    }

    event.preventDefault();
}


function playCard() {
    let gameCodeContent = gameCodeInput.value;
    let cardNameContent = cardNameInput.value;
    let cardValueContent = cardValueInput.value;
    let cardEventContent = cardEventInput.value;
    if(stompClient){
        const gameState = {
            gameCode: gameCodeContent,
            cardName: cardNameContent,
            cardValue: cardValueContent,
            cardEvent: cardEventContent,
            action: 'PLAY_CARD'
        };

        let jwt = jwtInput.value;
        stompClient.send('/app/game.card.play', {
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


sub_connect.addEventListener("click", connect)
sub_gen.addEventListener("click", makeGame);
sub_join_game.addEventListener("click", joinGame);
sub_start_game.addEventListener("click", startGame);
//sub_send.addEventListener("click", sendCard)
//sub_draw.addEventListener("click", drawCard)
//sub_yield.addEventListener("click", yieldTurn)
sub_register.addEventListener("click", register);
sub_login.addEventListener("click", login);
sub_play_card.addEventListener("click", playCard);