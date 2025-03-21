import { Component } from '@angular/core';
import { Router } from '@angular/router';
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


@Component({
  selector: 'app-main-menu',
  templateUrl: './main-menu.component.html',
  styleUrls: ['./main-menu.component.css'],
  imports: [
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
export class MainMenuComponent {
  constructor(private router: Router) {}

  onLoginClick() {
    console.log("Login button clicked");
    this.router.navigate(['/login']);
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
