import { Component, inject, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { SvgIconComponent } from '../svg-icon/svg-icon.component';
import { GamestateService, Card, CardDto, CardEffects } from '../services/gamestate.service';


@Component({
  selector: 'app-gameplay',
  imports: [
    CommonModule,
    HlmButtonDirective,
    SvgIconComponent,
  ],
  providers: [],
  templateUrl: './gameplay.component.html',
  styleUrl: './gameplay.component.css'
})

export class GameplayComponent {
  gameState = inject(GamestateService)
  
  playerCards: Card[] = [];
  topCard: Card | null = null;

  cardSpacing = 60;

  constructor() {
    effect(() => {
      this.playerCards = this.gameState.playerDeck();
      this.topCard = this.gameState.currentTopCard()[0] ?? null;
    });
  }

  drawCard() {
    this.gameState.drawCardAction();
  }

  placeCard(index: number): boolean {
    const card = this.playerCards[index];
    return this.gameState.placeCardAction(card);
  }


  getCardStyle(index: number, total: number): { [key: string]: string } {
    const spread = 20;
    const offset = (index - (total - 1) / 2);
    const angle = offset * (spread / total);
    const x = offset * this.cardSpacing;
    const y = Math.abs(offset) * 8;

    return {
      transform: `rotate(${angle}deg) translate(${x}px, ${y}px)`,
      transformOrigin: 'bottom center',
      zIndex: `${10 + (total - index)}`
    };
  }

  asArray(i: number) {
    return [...Array(i).keys()]
  }

}
