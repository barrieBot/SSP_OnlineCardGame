import { inject, Injectable, OnDestroy, signal } from '@angular/core';
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

export type CardFace = 'Scissors' | 'Rock' | 'Paper';

export enum CardEffects{ 
  NORMAL = 'NORMAL',
  DRAW = 'DRAW'
}

export interface Card{
  face: string,
  value: number,
  effect: CardEffects,
  asset: string,
  id: null | number
}

export interface CardDto {
  cardName: CardFace;
  cardValue: number;
}

export interface StartGameData {
  handCards: CardDto[];
  centerCard: CardDto;
  turnOrder: { [playerId: string]: string };
  sender: string;
}

export interface Player{
  nickname: string,
  card_count: number,
  placement: null | number
}

@Injectable({
  providedIn: 'root'
})
export class GamestateService implements OnDestroy {

  private connection = inject(WebsocketService)
  
  public readonly playerDeck = signal<Card[]>([])
  public readonly currentTopCard = signal<Card[]>([])
  public readonly players = signal<Player[]>([])
  public readonly drawModifier = signal(0)
  public readonly activePlayerPos = signal(0)

  ///Inject web-socket? oder umgekehrt? inject handler von hier in WS

  private gameUpdatesSub?: Subscription;

  constructor() { 
    this.gameUpdatesSub = this.connection.getGameUpdates().subscribe((data) => {
      if ((data.responseType === 'START_GAME' || data.action === 'START_GAME')) {
        this.setupGame(data);
      }

      if ((data.responseType === 'DRAW_CARD' || data.action === 'DRAW_CARD')) {
        this.addCardToHand(data);
      }

      if ((data.responseType === 'PLACE_CARD' || data.action === 'PLACE_CARD')) {
        this.updateTopCard(data);
      }
      // switch (data.action) {
      //   case 'START_GAME':
      //       this.setupGame(data);
      //       break;
          
      //   case 'DRAW_CARD':
      //     this.addCardToHand(data.card);
      //     break;
        
      //   case 'PLACE_CARD':
      //     this.updateTopCard(data.card);
      //     break;
      // }
      //Switch-Case für die ganzen Response-types/Actions 
    });
  }
  
  ngOnDestroy(): void {
    this.gameUpdatesSub?.unsubscribe;
  }

  setupGame(data: StartGameData): void {
    const hand = data.handCards.map(this.parseCard.bind(this));
    this.playerDeck.set(hand);

    // set top card
    this.currentTopCard.set([]);
    this.currentTopCard.update((cards) => [this.parseCard(data.centerCard), ...cards])

    const playerList: Player[] = Object.entries(data.turnOrder).map(([playerId, nickname]) => ({
      nickname,
      card_count: 0,
      placement: null
    }));
    this.players.set(playerList);

    // set active player based on sender value
    const activeIndex = playerList.findIndex(player => player.nickname === data.sender);
    if (activeIndex !== -1) {
      this.activePlayerPos.set(activeIndex);
    }

  }

  drawCardAction() {
    this.drawModifier.set(0)

    const gameCode = this.connection.getGameCode();
    if (!gameCode) {
      return;
    }

    this.connection.sendMessage('/app/game.draw.card', {
      gameCode,
      action: 'DRAW_CARD'
    });
  }

  placeCardAction(card: Card) {
    if (!this.checkCardValidity(card)) {
      return;
    }
    
    const gameCode = this.connection.getGameCode();
    if (!gameCode) {
      return;
    }

    this.connection.sendMessage('/app/game.play.card', {
      gameCode,  
      action: 'PLACE_CARD',
      card: {
        face: card.face,
        value: card.value,
        id: card.id
      }
    });
  }

  // pushAction(){

  // }

  checkCardValidity(card: Card): boolean {
    const topCard = this.currentTopCard()[0];
    if (!topCard) {
      return false;
    }

    const valueValid = card.value >= topCard.value;
    const faceValid =
      card.face === topCard.face ||
      (card.face === 'Scissors' && topCard.face === 'Paper') ||
      (card.face === 'Paper' && topCard.face === 'Rock') ||
      (card.face === 'Rock' && topCard.face === 'Scissors');

    return valueValid && faceValid;
  }

  private parseCard(card: CardDto): Card {
    return {
      face: card.cardName,
      value: card.cardValue, 
      effect: CardEffects.NORMAL,
      asset: this.mapCardToAsset(card),
      id: null
    };
  }

  private mapCardToAsset(card: CardDto): string {
    const nameMap: { [key: string]: string } = {
      'Scissors': 'schere',
      'Rock': 'stein',
      'Paper': 'papier'
    };
    const fileName = `${nameMap[card.cardName]}${card.cardValue}.svg`;
    return `assets/svg/cards/numeric_cards/${fileName}`;
  }

  private addCardToHand(cardDto: CardDto) {
    const newCard = this.parseCard(cardDto);
    this.playerDeck.update(deck => [...deck, newCard]);
  }

  private updateTopCard(cardDto: CardDto) {
    const newTopCard = this.parseCard(cardDto);
    this.currentTopCard.update(cards => [newTopCard, ...cards]);
  }
}
