import { inject, Injectable, signal } from '@angular/core';
import { WebsocketService } from './websocket.service';
import { Subscription } from 'rxjs';


export const allCards: string[] = [
    'assets/svg/cards/numeric_cards/schere1.svg',
    'assets/svg/cards/numeric_cards/schere2.svg',
    'assets/svg/cards/numeric_cards/schere3.svg',
    'assets/svg/cards/numeric_cards/schere4.svg',
    'assets/svg/cards/numeric_cards/schere5.svg',
    'assets/svg/cards/numeric_cards/schere6.svg',
    'assets/svg/cards/numeric_cards/schere7.svg',
    'assets/svg/cards/numeric_cards/schere8.svg',
    'assets/svg/cards/numeric_cards/schere9.svg',
    'assets/svg/cards/numeric_cards/stein1.svg',
    'assets/svg/cards/numeric_cards/stein2.svg',
    'assets/svg/cards/numeric_cards/stein3.svg',
    'assets/svg/cards/numeric_cards/stein4.svg',
    'assets/svg/cards/numeric_cards/stein5.svg',
    'assets/svg/cards/numeric_cards/stein6.svg',
    'assets/svg/cards/numeric_cards/stein7.svg',
    'assets/svg/cards/numeric_cards/stein8.svg',
    'assets/svg/cards/numeric_cards/stein9.svg',
    'assets/svg/cards/numeric_cards/papier1.svg',
    'assets/svg/cards/numeric_cards/papier2.svg',
    'assets/svg/cards/numeric_cards/papier3.svg',
    'assets/svg/cards/numeric_cards/papier4.svg',
    'assets/svg/cards/numeric_cards/papier5.svg',
    'assets/svg/cards/numeric_cards/papier6.svg',
    'assets/svg/cards/numeric_cards/papier7.svg',
    'assets/svg/cards/numeric_cards/papier8.svg',
    'assets/svg/cards/numeric_cards/papier9.svg',
  ];


export interface StartGameData {
  handCards: { cardName: string; cardValue: number }[];
  centerCard: { cardName: string; cardValue: number };
  turnOrder: { [key: string]: string };
  sender: string;
}


export interface Player{
  nick: string,
  card_count: number,
  placement: null | number
}

export interface Card{
  face: string,
  value: number,
  effect: card_effects,
  asset: string,
  id: null | number
}

export enum card_effects{ 
  NORMAL,
  DRAW
}


@Injectable({
  providedIn: 'root'
})
export class GamestateService {


  connection = inject(WebsocketService)
  
  public readonly player_deck = signal<Card[]>([])
  public readonly current_top_card = signal<Card[]>([])

  public readonly players = signal<Player[]>([])

  public readonly draw_modifier = signal(0)
  public readonly active_player_pos = signal(0)

  ///Inject web-socket? oder umgekehrt? inject handler von hier in WS

  gameUpdatesSub?: Subscription;

  constructor() { 

    this.gameUpdatesSub = this.connection.getGameUpdates().subscribe((data) => {
      if ((data.responseType === 'START_GAME' || data.action === 'START_GAME')) {
        //this.setupGame(data);
      }

      ////Switch-Case für die ganzen Response-types/Actions 

    });

  }


  setupGame(data: StartGameData){
    ///Set Up Players
    ///Set Up Cards? 
    
    this.player_deck.set( data.handCards.map((card: any) => {
      const parsed = this.parseCard(card);
      return parsed;
    })) ;

    this.current_top_card.set([]) /// Then add newest 
    this.current_top_card.update((cards) => [this.parseCard(data.centerCard), ...cards])

  }



  drawCardAction(){
    this.draw_modifier.set(0)



  }

  placeCardAction(card: Card){

    if(this.checkCardValiditiy(card)){
      ///Push to websocket here 

    }



  }

  pushAction(){

  }



  checkCardValiditiy(card: Card): boolean{
    


    const value = card.value >= this.current_top_card()[0].value
    const face = (card.face === this.current_top_card()[0].face) 
                || (card.face === "Scissors" && this.current_top_card()[0].face === "Paper")
                || (card.face === "Paper" && this.current_top_card()[0].face === "Rock")
                || (card.face === "Rock" && this.current_top_card()[0].face === "Scissors")


    if(value && face) { return true}
    return false;
  }

  parseCard(card: {cardName: string, cardValue: number }): Card{

    const new_card = {
      face: card.cardName,
      value: card.cardValue, 
      effect: card_effects.NORMAL,
      asset: this.mapCardToAsset(card),
      id: null
    }

    return new_card
  }


  mapCardToAsset(card: { cardName: string, cardValue: number }): string {
    const nameMap: { [key: string]: string } = {
      'Scissors': 'schere',
      'Rock': 'stein',
      'Paper': 'papier'
    };
    const fileName = `${nameMap[card.cardName]}${card.cardValue}.svg`;
    return `assets/svg/cards/numeric_cards/${fileName}`;
  }
  
}
