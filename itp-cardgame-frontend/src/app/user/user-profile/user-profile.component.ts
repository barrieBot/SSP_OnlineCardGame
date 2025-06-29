import { Component, inject, OnInit } from '@angular/core';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { provideIcons } from '@ng-icons/core';
import { lucidePen, lucideTrophy } from '@ng-icons/lucide';
import { HlmSeparatorDirective } from '@spartan-ng/ui-separator-helm';
import { BrnSeparatorComponent } from '@spartan-ng/brain/separator';
import { HlmInputDirective } from '@spartan-ng/ui-input-helm';
import { BrnDialogContentDirective, BrnDialogTriggerDirective } from '@spartan-ng/brain/dialog';
import {
  HlmDialogComponent,
  HlmDialogContentComponent,
  HlmDialogDescriptionDirective,
  HlmDialogHeaderComponent,
  HlmDialogTitleDirective,
} from '@spartan-ng/ui-dialog-helm';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WebsocketService } from 'src/app/services/websocket.service';
import { GameInstance, LocalStorageService, User } from 'src/app/services/local-storage.service';

@Component({
  selector: 'app-user-profile',
  providers: [provideIcons({ lucidePen, lucideTrophy })],
  imports: [
    HlmButtonDirective,
    HlmSeparatorDirective,
    BrnSeparatorComponent,
    HlmInputDirective,
    BrnDialogContentDirective,
    BrnDialogTriggerDirective,
    HlmDialogComponent,
    HlmDialogContentComponent,
    HlmDialogDescriptionDirective,
    HlmDialogHeaderComponent,
    HlmDialogTitleDirective,
    FormsModule,
    RouterModule
  ],
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.css'
})
export class UserProfileComponent implements OnInit {
  user: User | null = null;
  lobbyCode: string = '';
  gameCode: string = '';
  gamesWon: number = 0;
  gamesLost: number = 0;

  private localStorage = inject(LocalStorageService);
  private webSocketService = inject(WebsocketService);
  private router = inject(Router);

  ngOnInit(): void {
    ///this.user = this.localStorage.getUser();
    const token = this.localStorage.getJwtToken();

    if (token) {
      this.webSocketService.connect();
    }

    this.localStorage.getUserStats().subscribe({
      next: (stats) => {
        this.gamesWon = stats.gamesWon;
        this.gamesLost = stats.gamesLost;
      },
      error: (err) => console.error('Error while loading stats:', err)
    });

    this.webSocketService.getGameUpdates().subscribe((update) => {
      console.log('WebSocket-Update:', update);

      if (update?.responseType === 'NEW_GAME' && update?.gameCode) {
        console.log("Created game successfully: ")
        this.localStorage.setGameInstance({
          gameCode: update.gameCode,
          username: update.sender,
          timeStamp: Date.now(),
          player: this.localStorage.getUser()
        })
        this.localStorage.setPlayer(null)
        this.router.navigate(['/lobby', update.gameCode]);
      }

      if (update?.responseType === 'JOIN_GAME') {
        ///Wie oft wird das ausgeführt? Nur einmal, oder jedes mal wenn JOIN_GAME kommt
        console.log('Joined Game successfully: ', update)
        this.localStorage.setGameInstance({
          gameCode: this.gameCode,
          username: update.sender,
          timeStamp: Date.now(),
          player: this.localStorage.getUser()
        })
        this.localStorage.setPlayer(null)

        this.router.navigate(['/lobby', this.lobbyCode]);
      }
    });
  }

  createMatch() {
    if (this.user) {
      this.webSocketService.createGame(this.user.username);
      console.log('Match creation requested via WebSocket');
    }
  }

  joinLobby() {
    if (!this.user || !this.lobbyCode) {
      return;
    }

    this.gameCode = this.lobbyCode
    this.webSocketService.joinGame(this.lobbyCode, this.user.username);
    ///this.router.navigate(['/lobby', this.lobbyCode]);
    console.log(`Join lobby requested via WebSocket: lobbyCode=${this.lobbyCode}`);    
  }
}

