import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface GameInstance {
  gameCode: string;
  username: string;
  timeStamp: number;
  player: User | null;
}

export interface User {
  id?: number;
  username: string;
  token: string;
}

export interface UserStats {
  gamesWon: number;
  gamesLost: number;
}

@Injectable({
  providedIn: 'root'
})
export class LocalStorageService {

  private user: User | null = null
  private player: User | null = null
  private game: GameInstance | null = null
  private token: string | null = null

  private SessionID: string | null = null
  private User_Sessions: string[] = []
  private User_Games: GameInstance[] = []

  constructor(private http: HttpClient) {
    ///Get User/Token/Game-Instance
    this.setupSessionStore()
    this.retrieveLS()

  }

  setupSessionStore() {
    this.SessionID = sessionStorage.getItem('ssp_tcg_session');
    if (!this.SessionID) {
      this.SessionID = crypto.randomUUID()
      sessionStorage.setItem('ssp_tcg_session', this.SessionID)
    }
  }

  retrieveLS() {
    this.token = this.getItem('ssp_tcg_jwt');
    const session_user = sessionStorage.getItem('ssp_tcg_user')
    this.user = session_user ? JSON.parse(session_user) as User : this.getItem<User>('ssp_tcg_user');

    this.User_Sessions = this.getItem<string[]>(`ssp_tcg_${this.user?.username}`) || [];

    this.retrieveSessions(session_user)

    for (const session in this.User_Sessions) {
      const found_game = this.getItem<GameInstance>(`ssp_tcg_game_${this.user?.username}_${session}}`)
      if (found_game) { this.User_Games.push(found_game) }
    }

    this.game = this.getItem<GameInstance>(`ssp_tcg_game_${this.user?.username}_${this.SessionID}`);
    this.player = this.game?.player || null
  }


  addSession() {
    if (this.user && this.SessionID && !this.User_Sessions.includes(this.SessionID)) {
      this.User_Sessions.push(this.SessionID)
      this.setItem<string[]>(`ssp_tcg_${this.user.username}`, this.User_Sessions)
    }
  }

  retrieveSessions(session_user: string | null) {
    if (this.user) {
      if (!session_user) { sessionStorage.setItem('ssp_tcg_user', JSON.stringify(this.user)) }
      this.User_Sessions = this.User_Sessions.filter(session => {
        const saved_game = this.getItem<GameInstance>(`ssp_tcg_game_${this.user?.username}_${session}`)
        if (saved_game && ((saved_game?.timeStamp - Date.now()) > 1000 * 60 * 60)) {
          this.removeItem(`ssp_tcg_game_${this.user?.username}_${session}`)
          return false
        }
        return true
      })
      if (this.SessionID && !this.User_Sessions.includes(this.SessionID)) {
        this.User_Sessions.push(this.SessionID)
      }
      this.setItem<string[]>(`ssp_tcg_${this.user?.username}`, this.User_Sessions)
    }
  }

  removeSession() {
    if (this.user && this.SessionID && this.User_Sessions.includes(this.SessionID)) {
      const index = this.User_Sessions.findIndex(id => id === this.SessionID)
      if (index !== -1) {
        this.User_Sessions.splice(index, 1)
        this.setItem<string[]>(`ssp_tcg_${this.user.username}`, this.User_Sessions)
      }
    }
  }



  logout() {
    this.removeGameInstance()
    this.removeSession()
    this.removeUser()
    this.removeJwtToken()
    this.removeItem('ssp_tcg_player')
  }

  leaveGame() {
    this.removeGameInstance()
    this.removeItem('ssp_tcg_player')
  }



  getItem<T>(key: string): T | null {
    const value = localStorage.getItem(key);

    if (!value) { return null; }

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
    console.log("Login - Set User: ", user)
    this.user = user;
    this.setItem('ssp_tcg_user', user);
    sessionStorage.setItem('ssp_tcg_user', JSON.stringify(user));
  }

  getUser(): User | null {
    if(!this.user) { 
      const check_active_user = sessionStorage.getItem('ssp_tcg_user')
        try{
          if(check_active_user){this.user = JSON.parse(check_active_user) as User }
        } catch {
          console.log("Kein Valider User gefunden")
        }
    }
    return this.user
  }

  removeUser() {
    this.user = null;
    this.removeItem('ssp_tcg_user');
  }




  setPlayer(u: User | null) {
    const p = u ?? this.user;
    if (p) {
      this.player = p
      this.setItem('ssp_tcg_player', p);
      if (this.game) {
        this.game.player = p;
        this.setGameInstance(this.game)
      }
    } else {
      console.warn('Error setting up Player-Instance')
    }
  }

  getPlayer() {
    return this.game?.player ?? this.player ?? this.getItem<User>('ssp_tcg_player');
  }


  setGameInstance(game_instance: GameInstance) {
    this.game = game_instance;
    //Setze für Angemeldete Localstorage
    if (this.user && this.SessionID) {
      this.setItem<GameInstance>(`ssp_tcg_game_${this.user?.username}_${this.SessionID}`, game_instance);
    }
    //Setze aktives Spiel in SessionStorage
    sessionStorage.setItem('ssp_tcg_game', JSON.stringify(game_instance))
  }

  getGameInstance(): GameInstance | null {
    ///Game in Instance existiert  
    console.log("Attempt getGameInstance: ", this.game)
    if (this.game) { return this.game }

    //Game in Session existiert
    const session_game = sessionStorage.getItem('ssp_tcg_game')
    if (session_game) {
      try {
        console.log("Attempt getGameInstance form SS: ", session_game)
        return JSON.parse(session_game) as GameInstance
      } catch {
        sessionStorage.removeItem('ssp_tcg_game')
        console.log('Session-Game corrupted')
      }
    }

    //Game kann nicht im LS gefunden werden
    if (!this.user || !this.SessionID) { return null }

    //Suche game im LS
    this.game = this.getItem<GameInstance>(`ssp_tcg_game_${this.user?.username}_${this.SessionID}`)
    if (this.game) { sessionStorage.setItem('ssp_tcg_game', JSON.stringify(this.game)) }
    console.log("Final Attempt getGameState from LS: ", this.game)
    return this.game

  }

  removeGameInstance() {
    ///Bin mir noch nicht ganz sicher
    ///Löscht die Game-Instance aus dem LS und SS
  }

  activateGameInstance(game_id: string) {
    ///Setzt die SessionID auf die SessionID (game_id)
    ///Läd dann die Game-Instance in die SS
    ///Updated den Player.token der Game-Instance
    ///Update an LS und SS 
  }


  getJwtToken(): string | null {
    return (
      this.token 
      ?? this.player?.token 
      ?? this.user?.token 
      ?? this.getItem<string>('ssp_tcg_jwt')
    )
  }

  setJwtToken(token: string): void {
    //Mit legacy fürs erste 
    this.token = token
    if (this.user) {
      this.user.token = token
      this.setItem('ssp_tcg_user', this.user)
      sessionStorage.setItem('ssp_tcg_user', JSON.stringify(this.user))
    }

    if(this.player){
      this.player.token = token
      this.setPlayer(this.player)
    }
  }

  //Legacy, wahrscheinlich kann nam das einfach entfernen
  removeJwtToken(): void {
    localStorage.removeItem('ssp_tcg_jwt');
    this.token = null
  }



  getUserStats(): Observable<UserStats> {
    const headers = new HttpHeaders().set(
      'Authorization',
      `Bearer ${this.getJwtToken()}`
    );
    return this.http.get<UserStats>('/api/general/getUserStats', { headers });
  }

  changeUsername(newUsername: string): Observable<any> {
    const headers = new HttpHeaders().set(
      'Authorization',
      `Bearer ${this.getJwtToken()}`
    );
    return this.http.post(`/api/general/changeUsername?newUsername=${encodeURIComponent(newUsername)}`, {}, { headers });
  }

  changeEmail(newEmail: string): Observable<any> {
    const headers = new HttpHeaders().set(
      'Authorization',
      `Bearer ${this.getJwtToken()}`
    );
    return this.http.post(`/api/general/changeEmail?newEmail=${encodeURIComponent(newEmail)}`, {}, { headers });
  }
}