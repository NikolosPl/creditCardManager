import { tap, map } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { LoginResponse } from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private readonly TOKEN_KEY = 'token';

  constructor(private http: HttpClient){}

  login(username: string, password: string): Observable<void>{
    return this.http.post<LoginResponse>('/api/auth/login', {username, password}).pipe(
      tap(res => localStorage.setItem(this.TOKEN_KEY, res.token)),
      map(() => void 0)
    );
  }

  logout(): void{
    localStorage.removeItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean{
    return !!localStorage.getItem(this.TOKEN_KEY);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }
}
