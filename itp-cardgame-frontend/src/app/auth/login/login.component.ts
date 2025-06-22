import { Component, inject } from '@angular/core';
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
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/services/auth.service';
import { UserService } from 'src/app/services/user.service';

function decodeJwt(token: string): any {
  const payload = token.split('.')[1];
  const decoded = atob(payload);
  return JSON.parse(decoded);
}


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
  private authService = inject(AuthService);
  private userService = inject(UserService);

  constructor(private router: Router) {}

  login() {
    this.authService.login(this.username, this.password).subscribe({
      next: (response) => {
        alert("Login success");

        const token = response.token;
        localStorage.setItem('jwt', token);

        const payload = decodeJwt(token);
        console.log('Decoded JWT payload:', payload);

        const userId = payload.userId;
        const username = payload.sub;

        this.userService.setUser({ id: userId, username: username, token: token });
        this.router.navigate(['/profile']);
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
