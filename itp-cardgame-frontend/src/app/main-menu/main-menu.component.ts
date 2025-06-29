import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterModule} from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmInputDirective } from '@spartan-ng/ui-input-helm';
import { BrnDialogContentDirective, BrnDialogTriggerDirective } from '@spartan-ng/brain/dialog';
import {
  HlmDialogComponent,
  HlmDialogContentComponent,
  HlmDialogDescriptionDirective,
  HlmDialogHeaderComponent,
  HlmDialogTitleDirective,
} from '@spartan-ng/ui-dialog-helm';
import { WebsocketService } from '../services/websocket.service';
import { GamestateService } from '../services/gamestate.service';
import { LocalStorageService, User } from '../services/local-storage.service';


@Component({
  selector: 'app-main-menu',
  templateUrl: './main-menu.component.html',
  styleUrls: ['./main-menu.component.css'],
  imports: [
    RouterModule,
    FormsModule,
    HlmButtonDirective,
    HlmInputDirective,
    BrnDialogContentDirective, 
    BrnDialogTriggerDirective,
    HlmDialogComponent,
    HlmDialogContentComponent,
    HlmDialogDescriptionDirective,
    HlmDialogHeaderComponent,
    HlmDialogTitleDirective,
  ]
})
export class MainMenuComponent implements OnInit {
  joinCode: string = '';
  gameCode: string = '';
  nickname: string = '';
  
  private router = inject(Router);
  private websocketService = inject(WebsocketService);
  private userService = inject(LocalStorageService);
  private gamestate = inject(GamestateService);

  ngOnInit(): void {
    this.websocketService.getGameUpdates().subscribe((update) => {
      if (update?.responseType === 'JOIN_GAME') {
        //const code = update.gameCode ?? this.websocketService.getGameCode();
        const user: User = {
          username: update.sender,
          token: update.jwt
        };

        console.log("Joined game via Anon. GameCode: ", update.gameCode)
        this.userService.setGameInstance({
          gameCode: this.gameCode,
          username: update.sender,
          player: null,
          timeStamp: Date.now()
        })
        
        this.userService.setPlayer(user);

        if (this.gameCode !=='') {

          console.log('[MainMenu] navigating to lobby:', this.gameCode);
          this.router.navigate(['/lobby', this.gameCode]);
          this.gameCode = ''
          
        } else {
          console.warn('[MainMenu] gameCode not set in WebSocketService');
        }
      }
    });
  }


  joinAnonymously() {
    if (!this.joinCode || !this.nickname) {
      alert('Please enter gamecode and nickname');
      return;
    }
    
    this.gameCode = this.joinCode
    this.websocketService.joinGameAnonymous(this.joinCode.trim(), this.nickname.trim());
    console.log('[MainMenu] joinGameAnonymous sent');
  }


  onLoginClick() {
    console.log("Login button clicked");
    this.router.navigate(['/login']);
  }

  onLogoutClick(){
    this.userService.logout()
    this.router.navigate(['/'])
  }

  onRegisterClick() {
    console.log("Register button clicked");
    this.router.navigate(['/register']);
  }

  onJoinLobbyClick() {
    console.log("Join lobby button clicked");
  }

  onAllCardsClick() {
    console.log("all cards button clicked");
    this.router.navigate(['/cards']);
  }


}
