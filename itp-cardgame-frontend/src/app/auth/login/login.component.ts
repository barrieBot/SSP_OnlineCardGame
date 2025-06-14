import { Component } from '@angular/core';
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
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';


@Component({
  selector: 'app-login',
  standalone: true,
  providers: [provideIcons({ lucideEye, lucideEyeClosed })],
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
    FormsModule
  ],
  templateUrl: './login.component.html',
  
  styleUrl: './login.component.css'
})
export class LoginComponent {
  showPassword: boolean = false;
  username: string = '';
  password: string = '';

  constructor(private http: HttpClient, private router: Router) {}

  login() {
    const payload = {
      username: this.username,
      password: this.password
    };

    this.http.post('http://localhost:80/api/auth/login', payload, { withCredentials: true }).subscribe({
      next: () => {
        alert("Login success");
        this.router.navigate(['/lobby']);
      },
      error: (err) => {
        console.error(err);
        alert("Login failed.");
      }
    });
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
    const input = document.getElementById('password') as HTMLInputElement;
    input.type = this.showPassword ? 'text': 'password';
  }
}
