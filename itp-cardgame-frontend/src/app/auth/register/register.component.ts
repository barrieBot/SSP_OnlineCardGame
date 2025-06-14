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
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-register',
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
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  showPassword: boolean = false;
  username: string = '';
  email: string = '';
  password: string = '';
  // confirmPassword: string = '';

  constructor(private http: HttpClient, private router: Router) {}

  register() {
    // if (this.password != this.confirmPassword) {
    //   alert("Passwords do not match!");
    //   return;
    // }

    const payload = {
      email: this.email,
      username: this.username,
      password: this.password
    };

    this.http.post('http://localhost:80/api/auth/signup', payload, { withCredentials: true }).subscribe({
      next: () => {
        alert("Registration success");
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error(err);
        alert("Registration failed.");
      }
    });
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
    const input = document.getElementById('password') as HTMLInputElement;
    input.type = this.showPassword ? 'text': 'password';
  }
}
