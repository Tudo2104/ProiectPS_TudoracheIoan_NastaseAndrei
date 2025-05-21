import {inject, Injectable} from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {catchError, map, Observable, of, shareReplay, take} from "rxjs";
@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  private http = inject(HttpClient);

  constructor(private router: Router) {}

  canActivate(): Observable<boolean> {
    const token = localStorage.getItem('Token');
    if (!token) {
      this.router.navigate(['/login'], { replaceUrl: true });
      return of(false);
    }

    const headers = new HttpHeaders().set(
      'Authorization',
      `Bearer ${token}`
    );

    return this.http
      .post(
        'http://localhost:8081/api/user/verifyLogin',
        {},
        {responseType: 'text' }
      )
      .pipe(
        take(1),
        map(() => true),
        catchError(() => {
          localStorage.removeItem('Token');
          this.router.navigate(['/login'], { replaceUrl: true });
          return of(false);
        })
      );
  }
}
