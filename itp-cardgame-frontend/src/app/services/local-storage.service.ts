import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LocalStorageService {
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

  getJwtToken(): string | null {
    return localStorage.getItem('jwt');
  }

  setItem<T>(key: string, value: T): void {
    localStorage.setItem(key, JSON.stringify(value));
  }

  setJwtToken(token: string): void {
    localStorage.setItem('jwt', token);
  }

  removeItem(key: string): void {
    localStorage.removeItem(key);
  }

  removeJwtToken(): void {
    localStorage.removeItem('jwt');
  }
}