import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { SvgIconComponent } from '../svg-icon/svg-icon.component';
import { Subscription } from 'rxjs';
import { WebsocketService } from '../services/websocket.service';

interface StartGameData {
  handCards: { cardName: string; cardValue: number }[];
  centerCard: { cardName: string; cardValue: number };
  turnOrder: { [key: string]: string };
  sender: string;
}

@Component({
  selector: 'app-gameplay',
  imports: [
    CommonModule,
    HlmButtonDirective,
    SvgIconComponent
  ],
  providers: [],
  templateUrl: './gameplay.component.html',
  styleUrl: './gameplay.component.css'
})

export class GameplayComponent implements OnInit, OnDestroy {
  readonly allCards: string[] = [
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

  private websocketService = inject(WebsocketService);

  playerCards: string[] = [];
  middleCard: string = '';
  playerNames: { [key: string]: string } = {};
  currentPlayer: string = '';
  
  myPlayerName: string = '';
  activePlayer: string = '';

  gameUpdatesSub?: Subscription;
  selectedCardIndex: number | null = null;
  cardSpacing = 60;

  ngOnInit(): void {
    this.gameUpdatesSub = this.websocketService.getGameUpdates().subscribe((data) => {
      if ((data.responseType === 'START_GAME' || data.action === 'START_GAME')) {
        this.handleStartGame(data);
      }
    });
  }

  ngOnDestroy(): void {
    this.gameUpdatesSub?.unsubscribe();
  }

  handleStartGame(data: StartGameData) {
    const mappedCards = data.handCards.map((card: any) => {
      const mapped = this.mapCardToAsset(card);
      return mapped;
    });
    this.middleCard = this.mapCardToAsset(data.centerCard);
    this.playerCards = mappedCards;
    this.playerNames = data.turnOrder;
    this.currentPlayer = data.sender;
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

  getRandomCards(count: number): string[] {
    const shuffled = [...this.allCards].sort(() => 0.5 - Math.random());
    return shuffled.slice(0, count);
  }

  drawCard() {
    const remainingCards = this.allCards.filter(card => !this.playerCards.includes(card));

    if (remainingCards.length === 0) {
      return;
    }

    const randomCard = remainingCards[Math.floor(Math.random() * remainingCards.length)];
    this.playerCards.push(randomCard);
  }

  placeCard(index: number) {
    if (this.myPlayerName !== this.activePlayer) {
      console.log('Not your turn');
      return;
    }

    const cardAssetPath = this.playerCards[index];
    const card = this.extractCardFromAsset(cardAssetPath);
    this.middleCard = cardAssetPath;
    this.playerCards.splice(index, 1);

    const gameCode = this.websocketService.getGameCode();
    //TODO: grad noch kein game code found
    if (!gameCode) {
      console.warn('No game code found - cannot send card');
      return;
    }

    this.websocketService.sendCardPlayer(gameCode, card);

  }

  extractCardFromAsset(assetPath: string): { cardName: string, cardValue: number } {
    const fileName = assetPath.split('/').pop()?.replace('.svg', '') ?? '';
    const match = fileName.match(/(schere|stein|papier)(\d)/);

    const nameMap: { [key: string]: string } = {
      'schere': 'Scissors',
      'stein': 'Rock',
      'papier': 'Paper'
    };

    if (!match) {
      throw new Error('Invalid card asset path');
    }

    return {
      cardName: nameMap[match[1]],
      cardValue: parseInt(match[2], 10)
    };
  }

  validCardCheck(index:number) {

    //Card[index] beats middleCard

    return true;

    // else return false
  }

  getCardStyle(index: number, total: number): { [key: string]: string } {
    const spread = 20;
    const offset = (index - (total - 1) / 2);
    const angle = offset * (spread / total);
    const x = offset * this.cardSpacing;
    const y = Math.abs(offset) * 8;

    return {
      transform: `rotate(${angle}deg) translate(${x}px, ${y}px)`,
      transformOrigin: 'bottom center',
      zIndex: `${10 + (total - index)}`
    };
  }
}
