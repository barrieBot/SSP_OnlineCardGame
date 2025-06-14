import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SvgIconComponent } from '../svg-icon/svg-icon.component';
import { provideIcons } from '@ng-icons/core';
import { lucideOctagon, lucideScissors, lucideStickyNote } from '@ng-icons/lucide';
import { 
  HlmCarouselComponent, 
  HlmCarouselContentComponent,
  HlmCarouselItemComponent,
  HlmCarouselNextComponent,
  HlmCarouselPreviousComponent 
} from '@spartan-ng/ui-carousel-helm';


@Component({
  selector: 'app-cards-page',
  standalone: true,
  templateUrl: './cards-page.component.html',
  styleUrls: ['./cards-page.component.css'],
  providers: [provideIcons({ lucideScissors, lucideOctagon, lucideStickyNote  })],
  imports: [
    CommonModule,
    SvgIconComponent,
    HlmCarouselComponent, 
    HlmCarouselContentComponent,
    HlmCarouselItemComponent,
    HlmCarouselNextComponent,
    HlmCarouselPreviousComponent 
  ]
})
export class CardsPageComponent {
  numericScissorCards: string[] = [];
  numericStoneCards: string[] = [];
  numericPaperCards: string[] = [];
  // variable for the clicked card
  activeCard: string | null = null;

  ngOnInit() {
    this.showNumericScissorCards();
    this.showNumericStoneCards();
    this.showNumericPaperCards();
  }

  showNumericScissorCards() {
    console.log("CardsPageComponent wird geladen.");
    this.numericScissorCards = Array.from({length: 9}, (_, i) => 
      `assets/svg/cards/numeric_cards/schere${i + 1}.svg`
    );
    console.log(this.numericScissorCards);
  }

  showNumericStoneCards() {
    console.log("CardsPageComponent wird geladen.");
    this.numericStoneCards = Array.from({length: 9}, (_, i) => 
      `assets/svg/cards/numeric_cards/stein${i + 1}.svg`
    );
    console.log(this.numericStoneCards);
  }

  showNumericPaperCards() {
    console.log("CardsPageComponent wird geladen.");
    this.numericPaperCards = Array.from({length: 9}, (_, i) => 
      `assets/svg/cards/numeric_cards/papier${i + 1}.svg`
    );
    console.log(this.numericPaperCards);
  }

  openDialog(card: string) {
    this.activeCard = card;
  }

  closeDialog() {
    this.activeCard = null;
  }

  isDialogOpen(card: string): boolean {
    return this.activeCard === card;
  }

}
