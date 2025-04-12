import { Component } from '@angular/core';
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

@Component({
  selector: 'app-lobby',
  providers: [provideIcons({ lucideSettings, lucideCircleUserRound, lucideUser, lucideLogOut })],
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
    HlmMenuSeparatorComponent
  ],
  templateUrl: './lobby.component.html',
  styleUrl: './lobby.component.css'
})
export class LobbyComponent {

}
