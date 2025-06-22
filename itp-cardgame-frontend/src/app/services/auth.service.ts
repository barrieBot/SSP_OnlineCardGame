import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  constructor(private http: HttpClient) { }

  login(username: string, password: string): Observable<any> {
    const payload = { username, password };
    return this.http.post('http://localhost:80/api/auth/login', payload, {
      withCredentials: true
    });
  }

  register(email: string, username: string, password: string): Observable<any> {
    const payload = { email, username, password };
    return this.http.post('http://localhost:80/api/auth/signup', payload, {
      withCredentials: true
    });
  }
}
