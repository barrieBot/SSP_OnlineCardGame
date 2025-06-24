import { Injectable, inject } from '@angular/core';
import { LocalStorageService } from './local-storage.service';

export interface User {
  id?: number;
  username: string;
  token: string;
}

@Injectable({
  providedIn: 'root'
})

export class UserService {
  private user: User | null = null;
  private localStorageService = inject(LocalStorageService);

  constructor() {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage() {
    this.user = this.localStorageService.getItem<User>('user');
  }

  setUser(user: User) {
    this.user = user;
    this.localStorageService.setItem('user', user);
  }

  getUser(): User | null {
    return this.user;
  }

  getToken(): string | null {
    return this.user?.token ?? null;
  }

  //logout
  clearUser() {
    this.user = null;
    this.localStorageService.removeItem('user');
  }
}