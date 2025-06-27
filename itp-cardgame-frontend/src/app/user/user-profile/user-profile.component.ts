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
import { UserService } from 'src/app/services/user.service';
import { WebsocketService } from 'src/app/services/websocket.service';
import { User } from 'src/app/services/local-storage.service';


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
  private userService = inject(UserService);
  private webSocketService = inject(WebsocketService);
  private router = inject(Router);

  ngOnInit(): void {
    this.user = this.userService.getUser();
    const token = this.userService.getToken();

    if (token) {
      this.webSocketService.connect();
    }

    this.webSocketService.getGameUpdates().subscribe((update) => {
      console.log('WebSocket-Update:', update);

      if (update?.responseType === 'NEW_GAME' && update?.gameCode) {
        this.router.navigate(['/lobby', update.gameCode]);
      }

      if (update?.responseType === 'JOIN_GAME' && update?.gameCode) {
        this.router.navigate(['/lobby', update.gameCode]);
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

    this.webSocketService.joinGame(this.lobbyCode, this.user.username);
    this.router.navigate(['/lobby', this.lobbyCode]);
    console.log(`Join lobby requested via WebSocket: lobbyCode=${this.lobbyCode}`);
    
  }
}

