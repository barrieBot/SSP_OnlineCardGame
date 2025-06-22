import { Component } from '@angular/core';
import { HlmButtonDirective } from '@spartan-ng/ui-button-helm';
import { HlmLabelDirective } from '@spartan-ng/ui-label-helm';
import { HlmFormFieldComponent } from '@spartan-ng/ui-formfield-helm';
import { HlmInputDirective } from '@spartan-ng/ui-input-helm';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-user-settings',
  imports: [
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

}
