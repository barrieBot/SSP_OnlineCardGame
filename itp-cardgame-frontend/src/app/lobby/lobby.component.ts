import { Component, inject, OnInit } from '@angular/core';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideCircleUserRound, lucideSettings, lucideUser, lucideLogOut } from '@ng-icons/lucide';
import { BrnMenuTriggerDirective } from '@spartan-ng/brain/menu';
import { 
  HlmMenuComponent,
  HlmMenuGroupComponent,
  HlmMenuItemDirective,
  HlmMenuItemIconDirective,
  HlmMenuLabelComponent,
  HlmMenuSeparatorComponent
} from 'libs/ui/ui-menu-helm/src';
import { HlmIconDirective } from '@spartan-ng/ui-icon-helm';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService, User } from '../services/user.service';
import { WebsocketService } from '../services/websocket.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-lobby',
  providers: [provideIcons({ lucideSettings, lucideCircleUserRound, lucideUser, lucideLogOut })],
  imports: [ CommonModule ],
  templateUrl: './lobby.component.html',
  styleUrl: './lobby.component.css'
})
export class LobbyComponent implements OnInit {
  gameId: string | null = null;
  currentUser: User | null = null;
  hostUsername: string | null = null;
  players: { username: string }[] = [];

  private userService = inject(UserService);
  private route = inject(ActivatedRoute);
  private webSocketService = inject(WebsocketService);
  private router = inject(Router);

  ngOnInit() {
    this.currentUser = this.userService.getUser();
    this.gameId = this.route.snapshot.paramMap.get('id');
    console.log('Joined lobby-id: ', this.gameId);

    if (this.currentUser && this.gameId) {
      this.players = [{ username: this.currentUser.username }];
    }

    this.webSocketService.getGameUpdates().subscribe(update => {
      // new game
      if (update?.responseType === 'NEW_GAME' && update.id) {
        if (!this.hostUsername && this.currentUser) {
          this.hostUsername = this.currentUser?.username;
        }
      }

      // join game
      if (update?.responseType === 'JOIN_GAME') {
        const newPlayerUsername = update.sender;

        if (Array.isArray(update.otherPlayers)) {
          this.players = update.otherPlayers.map((username: string) => ({ username }));

          if (update.host) {
            this.hostUsername = update.host;
          }
        }

        // add new player to array players
        if (!this.players.find(p => p.username === newPlayerUsername)) {
          this.players.push({ username: newPlayerUsername });
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