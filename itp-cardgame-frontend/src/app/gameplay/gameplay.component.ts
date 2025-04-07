import { Component } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideOctagon, lucideScissors, lucideStickyNote } from '@ng-icons/lucide';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';

@Component({
  selector: 'app-gameplay',
  imports: [
    NgIcon,
    HlmButtonDirective
  ],
  providers: [provideIcons({ lucideOctagon, lucideScissors, lucideStickyNote })],
  templateUrl: './gameplay.component.html',
  styleUrl: './gameplay.component.css'
})
export class GameplayComponent {

}
