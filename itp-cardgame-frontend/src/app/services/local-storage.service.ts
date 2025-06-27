import { Injectable } from '@angular/core';

export interface GameInstance{
  gameCode: string,
  username: string,
  timeStamp: Date
}

export interface User {
  id?: number;
  username: string;
  token: string;
}



@Injectable({
  providedIn: 'root'
})
export class LocalStorageService {

  private user: User | null = null
  private player: User | null = null
  private game: GameInstance | null = null
  token: string | null = null


  constructor(){
    ///Get User/Token/Game-Instance

  }


  retrieveLS(){
    this.token = this.getItem('ssp_tcg_jwt');
    this.game = this.getItem<GameInstance>('ssp_tcg_game');
    this.user = this.getItem<User>('ssp_tcg_user');
    this.player = this.getItem<User>('ssp_tcg_player');
  }


  getItem<T>(key: string): T  | null {
    const value = localStorage.getItem(key);

    if (!value) {
      return null;
    }

    try {
      return JSON.parse(value) as T;
    } catch {
      console.warn(`LocalStorageService; Invalid JSON-value for key "${key}", removing entry.`);
      localStorage.removeItem(key);
      return null;
    }
  }

  setItem<T>(key: string, value: T): void {
    localStorage.setItem(key, JSON.stringify(value));
  }

  removeItem(key: string): void {
    localStorage.removeItem(key);
  }



  setUser(user: User) {
    this.user = user;
    this.setItem('ssp_tcg_user', user);
  }

  getUser(): User | null {
    return this.user;
  }

  removeUser() {
    this.user = null;
    this.removeItem('ssp_tcg_user');
  }




  getJwtToken(): string | null {
    //return localStorage.getItem('jwt');
    console.log(this.token)
    return this.token
  }

  setJwtToken(token: string): void {
    this.token = token
    localStorage.setItem('ssp_tcg_jwt', token);
  }

  removeJwtToken(): void {
    localStorage.removeItem('ssp_tcg_jwt');
    this.token = null
  }
}