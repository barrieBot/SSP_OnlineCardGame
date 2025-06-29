import { inject, Injectable } from '@angular/core';
import SockJS from 'sockjs-client';
import { Client, Stomp } from '@stomp/stompjs';
import { Subject, Observable, ReplaySubject } from 'rxjs';
import { GameInstance, LocalStorageService } from './local-storage.service';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private currentGameCode: string | null = null;

  private stompClient: Client | null = null;
  private connect_to_ws: Promise<void> | null = null;
  private disconnect_now = false
  private reconnection_tries = 0

  private gameUpdates$ = new ReplaySubject<any>(1);
  private localStorageService = inject(LocalStorageService);


  getGameUpdates(): Observable<any> {
    return this.gameUpdates$.asObservable();
  }

  setGameCode(code: string): void {
    this.currentGameCode = code;
  }

  getGameCode(): string | null {
    return this.localStorageService.getGameInstance()?.gameCode || null;
    // return this.currentGameCode;
  }

  getConnectionStatus(): boolean {
    return this.stompClient?.connected ?? false;
  }


  async connect(): Promise<void> {

    if (this.stompClient && this.stompClient.connected) {
      console.log('[WebSocket] already connected');
      return;
    }

    this.disconnect_now = false
    this.connect_to_ws = new Promise((resolve, reject) => {

      const token = this.localStorageService.getJwtToken();

      this.stompClient = new Client({

        brokerURL: undefined, // not used with SockJS
        webSocketFactory: () => new SockJS('/ws'),
        reconnectDelay: 5000,
        connectHeaders: token ? { Authorization: `Bearer ${this.localStorageService.getJwtToken()}` } : {},
        debug: (msg) => console.log('[STOMP DEBUG]', msg),
        onConnect: () => {
          console.log('[WebSocket] Connected with server');
          this.reconnection_tries = 0
          this.stompClient?.subscribe('/user/queue/private', (message) => {
            const body = JSON.parse(message.body);
            this.gameUpdates$.next(body);
          });
          this.send_reconnection_msg();
          resolve();

        },
        onStompError: (frame) => {
          console.error('[WebSocket] STOMP-Error:', frame.headers['message']);
          reject(new Error(frame.headers['message']));

        },
        onWebSocketError: (error) => {
          console.error('[WebSocket] connection failed:', error);
          reject(error);

        },
        onWebSocketClose: () => {
          if (!this.disconnect_now) {
            ///Speicher daten ins Local-Storage
            this.reconnect()
          }
        }

      });

      console.log('[WebSocket] Connecting...');
      this.stompClient.activate();
    })

    return this.connect_to_ws.finally(() => {
      this.connect_to_ws = null;
    })
  }

  disconnect(): void {
    this.disconnect_now = true
    if (this.stompClient) {
      console.log('[WebSocket] Connection closed...');
      this.stompClient.deactivate();
      this.stompClient = null;
    }
  }

  reconnect() {

    if (this.reconnection_tries >= 5) {
      ///Clear localstorage
      this.disconnect_now = true
      return
    }
    if (!this.localStorageService.getGameInstance()) {
      this.disconnect_now = true
      return
    }

    this.reconnection_tries++
    setTimeout(() => this.connect(), 5000)

  }

  private async send_reconnection_msg(): Promise<void> {

    const reconnect_token = this.localStorageService.getGameInstance()
    if (!reconnect_token) { return }

    if (Date.now() - reconnect_token.timeStamp > 15 * 60 * 1000) {
      this.localStorageService.leaveGame()
      return
    }

    try {
      await this.send_via_WS(
        '/game/reconnect',
        reconnect_token.gameCode,
        true
      )
      console.log('Attempted reconnection')

    } catch (err) {
      console.log('Error trying to reconnect: ', err)
    }

  }

  leaveGame() {
    this.localStorageService.leaveGame()
    this.disconnect()
  }


  createGame(displayName: string): void {
    const createGameDto = { displayName };
    this.send_via_WS(
      '/app/game.new',
      JSON.stringify(createGameDto),
      true
    )
  }

  joinGame(gameCode: string, displayName: string): void {
    this.setGameCode(gameCode);

    if (!this.stompClient || !this.stompClient.connected) {
      this.connect();
      const interval = setInterval(() => {
        if (this.stompClient?.connected) {
          clearInterval(interval);
          this.sendJoinGame(gameCode, displayName, true);
        }
      }, 200);
      return;
    }

    this.sendJoinGame(gameCode, displayName, true);

  }

  joinGameAnonymous(gameCode: string, displayName: string): void {
    if (!this.stompClient || !this.stompClient.connected) {
      this.connect();
      const interval = setInterval(() => {
        if (this.stompClient?.connected) {
          clearInterval(interval);
          this.setGameCode(gameCode);
          this.sendJoinGame(gameCode, displayName, false);
        }
      }, 200);
      return;
    }

    this.sendJoinGame(gameCode, displayName, false);

  }

  startGame(gameCode: string): void {
    const startGameDto = { gameCode, action: 'START_GAME' };
    this.send_via_WS(
      '/app/game.start',
      JSON.stringify(startGameDto),
      true
    )
  }

  sendCardPlayer(gameCode: string, card: { cardName: string, cardValue: number }): void {
    const playCardDto = { gameCode, action: 'PLAY_CARD', card };
    this.send_via_WS(
      '/app/game.play.card',
      JSON.stringify(playCardDto),
      true
    )
  }

  private sendJoinGame(gameCode: string, nickname: string, mode: boolean) {
    const joinGameDto = { gameCode, displayName: nickname, action: 'JOIN_GAME' };
    this.send_via_WS(
      '/app/game.join' + (mode ? '' : '.anonymous'),
      JSON.stringify(joinGameDto),
      mode
    )
  }

  async send_via_WS(adress: string, msg_body: string, authorised: boolean) {

    if (!this.stompClient || !this.stompClient.connected) {
      console.warn('[WebSocket] not connected, cannot send startGame');
      return;
    }

    if (!this.localStorageService.getJwtToken() && authorised) {
      console.warn('[WebSocket] not JWT token found, cannot authenticate');
      return;
    }

    this.stompClient!.publish({
      destination: adress,
      headers: authorised ? { Authorization: `Bearer ${this.localStorageService.getJwtToken()}` } : {},
      body: msg_body,
    });

    console.log('[WebSocket] Msg sent:', msg_body);

  }
}