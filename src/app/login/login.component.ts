import { Component, OnInit } from '@angular/core';
import { HlmFormFieldModule } from '@spartan-ng/ui-formfield-helm';
import { HlmInputDirective } from '@spartan-ng/ui-input-helm';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmLabelDirective } from '@spartan-ng/ui-label-helm';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideEye, lucideEyeClosed } from '@ng-icons/lucide';
import {
  HlmCardContentDirective,
  HlmCardDescriptionDirective,
  HlmCardDirective,
  HlmCardFooterDirective,
  HlmCardHeaderDirective,
  HlmCardTitleDirective,
} from '@spartan-ng/ui-card-helm';
import { ReactiveFormsModule } from '@angular/forms';


@Component({
  selector: 'app-login',
  providers: [provideIcons({ lucideEye, lucideEyeClosed })],
  standalone: true,
  imports: [
    HlmInputDirective, 
    HlmFormFieldModule,
    HlmCardContentDirective,
    HlmCardDescriptionDirective,
    HlmCardDirective,
    HlmCardFooterDirective,
    HlmCardHeaderDirective,
    HlmCardTitleDirective,
    HlmButtonDirective,
    HlmLabelDirective,
    NgIcon,
    ReactiveFormsModule
  ],
  templateUrl: './login.component.html',
  
  styleUrl: './login.component.css'
})
export class LoginComponent {
  showPassword: boolean = false;


  togglePassword() {
    this.showPassword = !this.showPassword;
    const input = document.getElementById('password') as HTMLInputElement;
    input.type = this.showPassword ? 'text': 'password';
  }
}
