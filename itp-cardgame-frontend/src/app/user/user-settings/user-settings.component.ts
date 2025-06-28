import { Component, inject } from '@angular/core';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmLabelDirective } from '@spartan-ng/ui-label-helm';
import { HlmFormFieldComponent } from '@spartan-ng/ui-formfield-helm';
import { HlmInputDirective } from '@spartan-ng/ui-input-helm';
import { RouterModule } from '@angular/router';
import { LocalStorageService, User } from 'src/app/services/local-storage.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-user-settings',
  imports: [
    FormsModule,
    HlmButtonDirective,
    HlmLabelDirective,
    HlmFormFieldComponent,
    HlmInputDirective,
    RouterModule
  ],
  templateUrl: './user-settings.component.html',
  styleUrl: './user-settings.component.css'
})
export class UserSettingsComponent {
  user: User | null = null;
  newUsername: string = '';
  newEmail: string = '';

  private localStorage = inject(LocalStorageService);

  onChangeUsername() {
    if (!this.newUsername.trim()) {
      return;
    }

    this.localStorage.changeUsername(this.newUsername).subscribe({
      next: () => {
        alert('Username updated successfully');
        if (this.user) {
          this.user.username = this.newUsername;
          this.localStorage.setUser(this.user);
        }
      },
      error: (err) => {
        console.error('Failed to update username:', err);
        alert('Failed to update username');
      }
    });
  }

  onChangeEmail() {
    if (!this.newEmail.trim()) {
      return;
    }

    this.localStorage.changeEmail(this.newEmail).subscribe({
      next: () => {
        alert('Email updated successfully');
      },
      error: (err) => {
        console.error('Failed to update email:', err);
        alert('Failed to update email');
      }
    });
   }

   saveChanges() {
    if (this.newUsername.trim()) {
      this.localStorage.changeUsername(this.newUsername).subscribe({
        next: () => {
          alert('Username updated successfully!');
          if (this.user) {
            this.user.username = this.newUsername;
            this.localStorage.setUser(this.user);
          }
        },
        error: (err) => alert('Failed to update username.'),
      });
    }

    if (this.newEmail.trim()) {
      this.localStorage.changeEmail(this.newEmail).subscribe({
        next: () => alert('Email updated successfully!'),
        error: (err) => alert('Failed to update email.'),
      });
    }
   }
}
