import { inject, Injectable, OnDestroy, signal } from '@angular/core';
import { WebsocketService } from './websocket.service';
import { Subscription } from 'rxjs';
import { LocalStorageService } from './local-storage.service';

export type CardFace = 'Scissors' | 'Rock' | 'Paper';

export enum CardEffects {
  NONE = 'NONE',
  DRAW = 'DRAW'
}

export interface Card {
  face: string,
  value: number,
  effect: CardEffects,
  asset: string,
  id: null | number
}

export interface CardDto {
  cardName: CardFace;
  cardValue: number;
  cardEvent: CardEffects;
}

export interface CardPlacedMessage {
  responseType: 'CARD_PLACED';
  playedCard: CardDto;
  newCurrentPlayer: string;
  sender: string;
}

interface CardDrawnMessage {
  sender: string;
  responseType: 'CARD_DRAWN';
  drawnCards: CardDto[];
  drawCount: number;
}

export interface StartGameData {
  handCards: CardDto[];
  centerCard: CardDto;
  turnOrder: { [playerId: string]: string };
  sender: string;
}

export interface InGameData {
  sender: string;
  responseType: 'RECONNECT_GAME';
  turnOrder: { [playerId: string]: string };
  cardAmounts: { [playerId: string]: number };
  centerCard: CardDto;
  handCards: CardDto[];
  currentPlayer: string;
  drawCount: number
}

export interface LobbyData {
  sender: string;
  responseType: 'RECONNECT_LOBBY';
  players: string[];
}

export interface Player {
  nickname: string,
  card_count: number,
  placement: null | number
}

@Injectable({
  providedIn: 'root'
})
export class GamestateService implements OnDestroy {

  private websocketService = inject(WebsocketService)
  private localStorageService = inject(LocalStorageService);

  public readonly playerDeck = signal<Card[]>([])
  public readonly currentTopCard = signal<Card[]>([])
  public readonly players = signal<Player[]>([])
  public readonly drawModifier = signal(0)
  readonly activePlayerPos = signal(0)
  readonly activeOffsetPos = signal(0)
  readonly isHost = signal(false)
  readonly roundWinner = signal<string | null>(null);

  private gameUpdatesSub?: Subscription
  private game_host: string | null = null

  constructor() {
    this.gameUpdatesSub = this.websocketService.getGameUpdates().subscribe((data) => {
      if (!data || (!data.responseType && !data.action)) {
        console.warn('Received unexpected message:', data);
        return;
      }

      console.log("Msg: ", data)
      
      if (data.gameCode) { this.websocketService.setGameCode(data.gameCode) }

      const type = data.responseType || data.action || data.type;

      switch (type) {
        case 'NEW_GAME':
          this.game_host = data.sender
          this.isHost.set(true);
          console.log("New Game: ", data)
          break;

        case 'JOIN_GAME':
          if (!this.game_host && data.host) {
            this.game_host = data.host
            this.isHost.set(this.localStorageService.getPlayer()?.username === data.host)
          }
          if (data.jwt) { this.localStorageService.setJwtToken(data.jwt) }
          this.playerJoined(data.otherPlayers, data.sender);
          console.log("Just joined: ", data)
          break;

        case 'START_GAME':
            if (data.gameCode) { this.websocketService.setGameCode(data.gameCode); }
            this.setupGame(data);
            break;

        case 'CARD_DRAWN':
          this.drawModifier.set(0);
          const cardDrawnData = data as CardDrawnMessage;
          this.updatePlayerHand(data.sender, Number(data.drawCount))
          this.update_active_player(data.newCurrentPlayer)

          if (Array.isArray(cardDrawnData.drawnCards)) {
            cardDrawnData.drawnCards.forEach(card => this.addCardToHand(card));
          }
          break;

        case 'CARD_PLACED':
          if (data.sender === this.localStorageService.getPlayer()?.username) {
            this.removeCardFromHand(data.playedCard);
          }
          this.updatePlayerHand(data.sender, -1)
          this.updateTopCard(data.playedCard);
          this.update_active_player(data.newCurrentPlayer)
          break;

        case 'RECONNECT_GAME':
          this.setupReconnect(data)
          this.update_active_player(data.currentPlayer)
          break;

        case 'RECONNECT_LOBBY':
          this.playerJoined(data.players, data.sender)
          break;

        case 'RECONNECTION_FAILED':
          break;

        case 'GAME_FINISHED':
          if (data.playedCard) { this.updateTopCard(data.playedCard); }
          if (data.winningPlayer) { this.roundWinner.set(data.winningPlayer); }
          this.activePlayerPos.set(-1);
          break;

        case 'HOST_RESTART':
          this.roundWinner.set(null);
          this.setupGame(data);
          break;
        
        case 'HOST_CLOSE':
          //TODO
          break;

        default:
          console.warn('Unknown update type:', type);
      }
    });
  }

  init() {
    if (this.websocketService.getConnectionStatus() === false) {
      this.websocketService.connect();
    }
  }

  ngOnDestroy(): void {
    this.gameUpdatesSub?.unsubscribe();
  }

  setupGame(data: StartGameData): void {
    this.activePlayerPos.set(0)
    this.setupPlayerHand(data.handCards)
    this.setupPlayerList(data.turnOrder, { "default": 5 })
    this.setupTopCard(data.centerCard)
  }

  setupReconnect(data: InGameData) {
    this.setupPlayerHand(data.handCards)
    this.setupPlayerList(data.turnOrder, data.cardAmounts)
    this.setupTopCard(data.centerCard)

    this.drawModifier.set(data.drawCount)
    this.update_active_player(data.currentPlayer)
  }

  setupPlayerHand(handCards: CardDto[]) {
    const hand = handCards.map((cardDto, index) =>
      ({ ...this.parseCard(cardDto), id: index })
    );
    this.playerDeck.set(hand);
  }

  setupPlayerList(turnOrder: { [playerId: string]: string }, cardAmounts: { [playerId: string]: number }) {
    let placement_number = 0
    const playerList: Player[] = Object.entries(turnOrder).map(([playerId, nickname]) => (
      {
        nickname,
        card_count: cardAmounts[playerId] ?? 5,
        placement: placement_number++
      }));
    this.players.set(playerList);

    const activeOffset = playerList.findIndex(player =>
      player.nickname === this.localStorageService.getPlayer()?.username
    );

    if (activeOffset !== -1) { this.activeOffsetPos.set(activeOffset); }
  }

  setupTopCard(card: CardDto) {
    this.currentTopCard.set([]);
    this.currentTopCard.update((cards) => [this.parseCard(card), ...cards])
  }

  update_active_player(new_active_nick: string) {
    const newIndex = this.players().findIndex(p => p.nickname === new_active_nick);
    if (newIndex !== -1) {
      this.activePlayerPos.set(newIndex);
    }
  }

  updatePlayerHand(player_name: string, value: number) {
    this.players.update(players =>
      players.map(player =>
        player.nickname === player_name ? { ...player, card_count: player.card_count + value } : player))

    console.log(this.players())
  }

  playerJoined(data: any, sender: string): void {
    
    const all_players = new Set([...data, sender])

    for (const other_player of all_players) {
      if (!this.players().find(p => p.nickname === other_player)) {
        const new_player = { nickname: other_player, card_count: 0, placement: 0 }
        this.players.update(player => [...player, new_player]);
      }
    }
  }

  drawCardAction() {
    this.drawModifier.set(0)
    const gameCode = this.websocketService.getGameCode();
    if (!gameCode) { return; }
    const drawCardDto = { gameCode, action: 'DRAW_CARD' }
    this.websocketService.send_via_WS('/app/game.card.draw',
      JSON.stringify(drawCardDto),
      true
    );
  }

  placeCardAction(card: Card): boolean {
    const gameCode = this.websocketService.getGameCode();
    if (!gameCode) {
      console.log('gamecode false')
      return false;
    }

    const cardDto: CardDto = {
      cardName: card.face as CardFace,
      cardValue: card.value,
      cardEvent: card.effect
    };

    const payload = {
      gameCode,
      action: 'PLACE_CARD',
      card: cardDto
    };

    this.websocketService.send_via_WS(
      '/app/game.card.play',
      JSON.stringify(payload),
      true);

    return true;
  }

  getWinner(): Player | null {
    const winnerName = this.roundWinner();
    if (!winnerName) {
      return null;
    }

    const player = this.players().find(p => p.nickname === winnerName);
    return player ?? null;
  }

  resetRound() {
    const gameCode = this.websocketService.getGameCode();
    if (!gameCode) {
      return;
    }

    const payload = {
      gameCode,
      action: 'RESTART_GAME'
    };

    this.websocketService.send_via_WS(
      '/app/game.restart',
      JSON.stringify(payload),
      true
    );
  }

  private parseCard(card: CardDto): Card {
    return {
      face: card.cardName,
      value: card.cardValue,
      effect: card.cardEvent,
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

    const fileName = `${nameMap[card.cardName]}${card.cardValue}${(card.cardEvent === CardEffects.NONE) ? '' : '_draw'}.svg`;
    return `assets/svg/cards/numeric_cards/${fileName}`;

  }

  private addCardToHand(cardDto: CardDto) {
    const newCard = this.parseCard(cardDto);
    this.playerDeck.update(deck => [...deck, newCard]);
  }

  private removeCardFromHand(cardDto: CardDto) {
    this.playerDeck.update(deck => {
      const card_pos = deck.findIndex( c => (c.face === cardDto.cardName && c.value === cardDto.cardValue && c.effect === cardDto.cardEvent))
      if(card_pos !== -1){ 
        const updated_deck = [...deck]
        updated_deck.splice(card_pos, 1) 
        return updated_deck
      }
      return deck
    });
  }

  private updateTopCard(cardDto: CardDto) {
    const newTopCard = this.parseCard(cardDto);

    this.drawModifier.set(newTopCard.effect == CardEffects.DRAW
      ? this.drawModifier() + newTopCard.value : 0)

    this.currentTopCard.update(cards => [newTopCard, ...cards]);
  }

  checkCardValidity(card: Card): boolean {
    const topCard = this.currentTopCard()[0];
    if (!topCard) {
      return false;
    }

    const type = (card.effect === topCard.effect) ||
      (topCard.effect === CardEffects.NONE && card.effect === CardEffects.DRAW)
    const valueValid = (card.value >= topCard.value)
    const faceValid =
      (card.face === 'Scissors' && topCard.face === 'Paper') ||
      (card.face === 'Paper' && topCard.face === 'Rock') ||
      (card.face === 'Rock' && topCard.face === 'Scissors');

    if (!type) {
      return false;
    }

    const faceIdentical = (card.face === topCard.face);
    if (faceIdentical && card.effect === CardEffects.DRAW) {
      return false;
    }

    if (valueValid && faceValid) {
      return true;
    }

    if (faceValid && faceIdentical) {
      return true;
    }

    if (valueValid && type) {
      return true;
    }

    return valueValid && faceValid;
  }
}
