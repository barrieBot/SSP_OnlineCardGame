import { inject, Injectable } from '@angular/core';
import SockJS from 'sockjs-client';
import { Client, Stomp } from '@stomp/stompjs';
import { Subject, Observable, ReplaySubject } from 'rxjs';
import { LocalStorageService } from './local-storage.service';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private currentGameCode: string | null = null;
  private stompClient: Client | null = null;
  private gameUpdates$ = new ReplaySubject<any>(1);
  private localStorageService = inject(LocalStorageService);

  connect(): void {
    if (this.stompClient && this.stompClient.connected) {
      console.log('[WebSocket] already connected');
      return;
    }

    const socketUrl = '/ws';
    const socket = new SockJS(socketUrl);

    this.stompClient = Stomp.over(socket);
    this.stompClient.debug = (msg) => console.log(msg);
    this.stompClient.reconnectDelay = 5000;

    this.stompClient.onConnect = () => {
      console.log('[WebSocket] Connected with serve');
      this.stompClient?.subscribe('/user/queue/private', (message) => {
        const body = JSON.parse(message.body);
        console.log('[WebSocket] received message:', body);
        this.gameUpdates$.next(body);
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('[WebSocket] STOMP-Error:', frame.headers['message']);
    };

    this.stompClient.onWebSocketError = (error) => {
      console.error('[WebSocket] connection failed:', error);
    };

    console.log('[WebSocket] Connect with:', socketUrl);
    this.stompClient.activate();
  }

  getGameUpdates(): Observable<any> {
    return this.gameUpdates$.asObservable();
  }

  disconnect(): void {
    if (this.stompClient) {
      console.log('[WebSocket] Connection closed...');
      this.stompClient.deactivate();
      this.stompClient = null;
    }
  }

  setGameCode(code: string): void {
    this.currentGameCode = code;
  }

  getGameCode(): string | null {
    return this.currentGameCode;
  }

  createGame(displayName: string): void {
    if (!this.stompClient || !this.stompClient.connected) {
      console.warn('[WebSocket] not connected, cannot send createGame');
      return;
    }

    const token = this.localStorageService.getJwtToken();
    if (!token) {
      console.warn('[WebSocket] no JWT token found, cannot authenticate');
      return;
    }

    const createGameDto = { displayName };

    this.stompClient.publish({
      destination: '/app/game.new',
      headers: {
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(createGameDto),
    });

    console.log('[WebSocket] Sent createGameDto:', createGameDto);
  }
  
  joinGame(gameCode: string, displayName: string): void {
    this.setGameCode(gameCode);

    if (!this.stompClient || !this.stompClient.connected) {
      this.connect();
      const interval = setInterval(() => {
        if (this.stompClient?.connected) {
          clearInterval(interval);
          this.joinGame(gameCode, displayName);
        }
      }, 200);
      return;
    }

    const token = this.localStorageService.getJwtToken();
    if (!token) {
      console.warn('[WebSocket] no JWT token found, cannot authenticate');
      return;
    }

    const joinGameDto = {
      gameCode,
      displayName,
      action: 'JOIN_GAME'
    };

    this.stompClient.publish({
      destination: '/app/game.join',
      headers: {
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(joinGameDto),
    });

    console.log('[WebSocket] sent joinGameDto:', joinGameDto);
  }

  startGame(gameCode: string): void {
    if (!this.stompClient || !this.stompClient.connected) {
      console.warn('[WebSocket] not connected, cannot send startGame');
      return;
    }

    const token = this.localStorageService.getJwtToken();
    if (!token) {
      console.warn('[WebSocket] not JWT token found, cannot authenticate');
      return;
    }

    const startGameDto = {
      gameCode,
      action: 'START_GAME'
    };

    this.stompClient.publish({
      destination: '/app/game.start',
      headers: {
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(startGameDto),
    });

    console.log('[WebSocket] sent startGameDto:', startGameDto);
  }

  sendCardPlayer(gameCode: string, card: { cardName: string, cardValue: number }): void {
    if (!this.stompClient || !this.stompClient.connected) {
      console.warn('[WebSocket] not connected, cannot send played card');
      return;
    }

    const token = this.localStorageService.getJwtToken();
    if (!token) {
      console.warn('[WebSocket] no JWT token found, cannot authenticate');
      return;
    }

    const playCardDto = {
      gameCode,
      action: 'PLAY_CARD',
      card
    };

    this.stompClient.publish({
      destination: '/app/game.play.card',
      headers: {
        Authorization: `Bearer ${token}`
      },
      body: JSON.stringify(playCardDto),
    });

    console.log('[WebSocket] sent playCardDto:', playCardDto);
  }
}