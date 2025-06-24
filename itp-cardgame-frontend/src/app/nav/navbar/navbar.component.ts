import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  lucideCircleUserRound,
  lucideLogOut,
  lucideSettings,
  lucideUser } from '@ng-icons/lucide';
import { BrnMenuTriggerDirective } from '@spartan-ng/brain/menu';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmIconDirective } from '@spartan-ng/ui-icon-helm';
import {
  HlmMenuComponent,
  HlmMenuGroupComponent,
  HlmMenuItemDirective,
  HlmMenuItemIconDirective,
  HlmMenuLabelComponent,
  HlmMenuSeparatorComponent
} from '@spartan-ng/ui-menu-helm';
import { UserService } from 'src/app/services/user.service';

@Component({
  selector: 'app-navbar',
  providers: [provideIcons({ lucideSettings, lucideCircleUserRound, lucideUser, lucideLogOut })],
  standalone: true,
  imports: [
      HlmIconDirective,
      BrnMenuTriggerDirective,
      HlmButtonDirective,
      NgIcon,
      HlmMenuComponent,
      HlmMenuGroupComponent,
      HlmMenuItemDirective,
      HlmMenuItemIconDirective,
      HlmMenuLabelComponent,
      HlmMenuSeparatorComponent,
      CommonModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {

  private router = inject(Router)
  private user_status = inject(UserService)

  readonly loggedIn = signal(false)
  readonly nav_visible = signal(true)

  constructor(){
    this.router.events.subscribe(event => {

      if(this.user_status.getUser()) { this.loggedIn.set(true) }
      if (event instanceof NavigationEnd){

      }
    })
  }

  
}
