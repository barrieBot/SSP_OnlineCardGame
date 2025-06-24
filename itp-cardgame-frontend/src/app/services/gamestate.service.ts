import { inject, Injectable, OnDestroy, signal } from '@angular/core';
import { WebsocketService } from './websocket.service';
import { Subscription } from 'rxjs';

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

  private websocketService = inject(WebsocketService)
  
  public readonly playerDeck = signal<Card[]>([])
  public readonly currentTopCard = signal<Card[]>([])
  public readonly players = signal<Player[]>([])
  public readonly drawModifier = signal(0)
  public readonly activePlayerPos = signal(0)

  private gameUpdatesSub?: Subscription;

  constructor() { 
    this.gameUpdatesSub = this.websocketService.getGameUpdates().subscribe((data) => {
      const type = data.responseType || data.action;

      switch (type) {
        case 'START_GAME':
          this.setupGame(data);
          break;
        
        case 'DRAW_CARD':
          this.addCardToHand(data);
          break;
        
        case 'PLACE_CARD':
          this.updateTopCard(data);
          break;
      }

    });
  }
  
  ngOnDestroy(): void {
    this.gameUpdatesSub?.unsubscribe();
  }

  setupGame(data: StartGameData): void {
    const hand = data.handCards.map((cardDto, index) => 
      ({ ...this.parseCard(cardDto), id: index })
    );
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

    const gameCode = this.websocketService.getGameCode();
    if (!gameCode) {
      return;
    }

    this.websocketService.sendMessage('/app/game.card.draw', {
      gameCode,
      action: 'DRAW_CARD'
    });
  }

  placeCardAction(card: Card) {
    if (!this.checkCardValidity(card)) {
      return;
    }
    
    const gameCode = this.websocketService.getGameCode();
    if (!gameCode) {
      return;
    }

    this.websocketService.sendMessage('/app/game.card.play', {
      gameCode,  
      action: 'PLACE_CARD',
      card: {
        face: card.face,
        value: card.value,
        id: card.id
      }
    });
  }

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
