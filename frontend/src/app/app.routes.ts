import { Routes } from '@angular/router';
import { MainMenuComponent } from './main-menu/main-menu.component';
import { LoginComponent } from './auth/login/login.component';
import { CardsPageComponent } from './cards-page/cards-page.component';
import { RegisterComponent } from './auth/register/register.component';
import { LobbyComponent } from './lobby/lobby.component';
import { GameplayComponent } from './gameplay/gameplay.component';
import { UserProfileComponent } from './user/user-profile/user-profile.component';
import { UserSettingsComponent } from './user/user-settings/user-settings.component';

export const routes: Routes = [
    { path: '', component: MainMenuComponent },
    { path: 'login', component:  LoginComponent },
    { path: 'register', component: RegisterComponent},
    { path: 'cards', component: CardsPageComponent },
    { path: 'lobby', component: LobbyComponent },
    { path: 'gameplay', component: GameplayComponent},
    { path: 'profile', component: UserProfileComponent },
    { path: 'settings', component: UserSettingsComponent },
    { path: 'lobby/:id', component: LobbyComponent },
    { path: 'gameplay/:id', component: GameplayComponent }
];
