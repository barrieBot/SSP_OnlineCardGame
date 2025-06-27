import { Component, inject, OnInit, signal } from '@angular/core';
import { provideIcons } from '@ng-icons/core';
import { lucideCircleUserRound, lucideSettings, lucideUser, lucideLogOut } from '@ng-icons/lucide';

import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../services/user.service';
import { WebsocketService } from '../services/websocket.service';
import { CommonModule } from '@angular/common';
import { GamestateService } from '../services/gamestate.service';
import { LocalStorageService, User } from '../services/local-storage.service';

@Component({
  selector: 'app-lobby',
  providers: [provideIcons({ lucideSettings, lucideCircleUserRound, lucideUser, lucideLogOut })],
  imports: [CommonModule],
  templateUrl: './lobby.component.html',
  styleUrl: './lobby.component.css'
})
export class LobbyComponent implements OnInit {
  gameId: string | null = null;
  currentUser: User | null = null;
  hostUsername: string | null = null;

  private userService = inject(UserService);
  private route = inject(ActivatedRoute);
  private webSocketService = inject(WebsocketService);
  private router = inject(Router);
  private localStorage = inject(LocalStorageService);
  game = inject(GamestateService);

  isHost = signal(true)

  ngOnInit() {
    this.currentUser = this.userService.getUser();
    this.gameId = this.route.snapshot.paramMap.get('id');
    console.log('Joined lobby-id: ', this.gameId);


    /// Das muss so auch in die gameplay
    if (this.webSocketService.getConnectionStatus() === false) {
      this.webSocketService.connect();
    }

    ///Irgendwo hier muss die Game-Instance im LocalStorage erstellt werden
    this.webSocketService.getGameUpdates().subscribe(update => {
      // new game
      if (update?.responseType === 'NEW_GAME' && update.id) {
        if (!this.hostUsername && this.currentUser) {
          this.hostUsername = this.currentUser?.username;
        }
      }

      // join game
      if (update?.responseType === 'JOIN_GAME') {
        if (update.sender === this.currentUser?.username) {
          this.localStorage.setItem('ssp_tcg_reconnect_data', JSON.stringify({
            gameCode: update.gameCode,
            username: update.sender,
            timeStamp: Date.now()
          }))
        }
        if (Array.isArray(update.otherPlayers)) {
          if (update.host) {
            this.hostUsername = update.host;
          }
        }
      }

      // start game
      if ((update?.action === 'START_GAME' || update?.responseType === 'START_GAME')) {
        const gameId = update.gameCode ?? this.gameId;
        console.log('Game started, navigating to gameplay...');
        if (gameId) {
          this.router.navigate(['/gameplay', gameId]);
        } else {
          console.error('Cannot navigate: gameId is missing');
        }
      }
    });
  }

  onStartGame(): void {
    if (this.gameId) {
      this.webSocketService.startGame(this.gameId);
    }
  }
}