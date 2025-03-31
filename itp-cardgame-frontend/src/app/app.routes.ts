import { Routes } from '@angular/router';
import { MainMenuComponent } from './main-menu/main-menu.component';
import { LoginComponent } from './login/login.component';
import { CardsPageComponent } from './cards-page/cards-page.component';
import { RegisterComponent } from './register/register.component';

export const routes: Routes = [
    { path: '', component: MainMenuComponent },
    { path: 'login', component:  LoginComponent },
    { path: 'register', component: RegisterComponent},
    { path: 'cards', component: CardsPageComponent },
];
