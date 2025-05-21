import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class LoggedInRedirectGuard implements CanActivate {

  constructor(private router: Router) {}

  canActivate(): boolean {
    const token = localStorage.getItem('Token');

    if (token) {
      this.router.navigate(['/dashboard'], { replaceUrl: true });
      return false;
    }

    return true;
  }
}
