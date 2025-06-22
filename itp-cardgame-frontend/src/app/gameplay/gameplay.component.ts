import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { SvgIconComponent } from '../svg-icon/svg-icon.component';

@Component({
  selector: 'app-gameplay',
  imports: [
    CommonModule,
    HlmButtonDirective,
    SvgIconComponent
  ],
  providers: [],
  templateUrl: './gameplay.component.html',
  styleUrl: './gameplay.component.css'
})
export class GameplayComponent implements OnInit {
  allCards: string[] = [
    'assets/svg/cards/numeric_cards/schere1.svg',
    'assets/svg/cards/numeric_cards/schere2.svg',
    'assets/svg/cards/numeric_cards/schere3.svg',
    'assets/svg/cards/numeric_cards/schere4.svg',
    'assets/svg/cards/numeric_cards/schere5.svg',
    'assets/svg/cards/numeric_cards/schere6.svg',
    'assets/svg/cards/numeric_cards/schere7.svg',
    'assets/svg/cards/numeric_cards/schere8.svg',
    'assets/svg/cards/numeric_cards/schere9.svg',
    'assets/svg/cards/numeric_cards/stein1.svg',
    'assets/svg/cards/numeric_cards/stein2.svg',
    'assets/svg/cards/numeric_cards/stein3.svg',
    'assets/svg/cards/numeric_cards/stein4.svg',
    'assets/svg/cards/numeric_cards/stein5.svg',
    'assets/svg/cards/numeric_cards/stein6.svg',
    'assets/svg/cards/numeric_cards/stein7.svg',
    'assets/svg/cards/numeric_cards/stein8.svg',
    'assets/svg/cards/numeric_cards/stein9.svg',
    'assets/svg/cards/numeric_cards/papier1.svg',
    'assets/svg/cards/numeric_cards/papier2.svg',
    'assets/svg/cards/numeric_cards/papier3.svg',
    'assets/svg/cards/numeric_cards/papier4.svg',
    'assets/svg/cards/numeric_cards/papier5.svg',
    'assets/svg/cards/numeric_cards/papier6.svg',
    'assets/svg/cards/numeric_cards/papier7.svg',
    'assets/svg/cards/numeric_cards/papier8.svg',
    'assets/svg/cards/numeric_cards/papier9.svg',
  ];

  playerCards: string[] = [];
  //playerCards vielleicht nicht nur string sondern ein Card-Type oder so
  middleCard: string = 'assets/svg/cards/numeric_cards/papier5.svg';
  //vielleicht auch hier ein Card-Type
  selectedCardIndex: number | null = null;
  cardSpacing = 60;

  ngOnInit(): void {
    this.playerCards = this.getRandomCards(7);
  }

  getRandomCards(count: number): string[] {
    const shuffled = [...this.allCards].sort(() => 0.5 - Math.random());
    return shuffled.slice(0, count);
  }

  drawCard() {
    const remainingCards = this.allCards.filter(card => !this.playerCards.includes(card));

    if (remainingCards.length === 0) {
      return;
    }

    const randomCard = remainingCards[Math.floor(Math.random() * remainingCards.length)];
    this.playerCards.push(randomCard);
  }

  placeCard(index: number) {
    const card = this.playerCards[index];
    this.middleCard = card;
    this.playerCards.splice(index, 1);
  }

  validCardCheck(index:number) {
    //Card[index] beats middleCard
    return true;
    // else return false
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
}
