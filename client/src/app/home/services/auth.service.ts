import { Injectable } from '@angular/core';
import { BaseService } from '../../shared/services/base.service';
import { User } from '../../models/user.model';
import { Http, Response } from '@angular/http';
import { LoginModel } from '../models/login.model';
import { RegisterModel } from '../models/register.model';
import { map } from 'rxjs/operators';
import {Router} from '@angular/router';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';

const ENDPOINTS = {
  LOGIN: '/auth/login',
  REGISTER: '/auth/register',
  LOGOUT: '/auth/logout'
};

@Injectable({
  providedIn: 'root'
})
export class AuthService extends BaseService {
  activeUser: User;

  constructor(private http: HttpClient, private router: Router, private keycloakService: KeycloakService) {
    super();
    const user = JSON.parse( localStorage.getItem('user'));
    if (user) {
      this.activeUser = new User().deserialize(user);
      return;
    }

    Promise.all([keycloakService.loadUserProfile(), keycloakService.getToken(), keycloakService.getUserRoles()]).then(values => {
      let [profile, token, authorities] = values;
      let user = {...profile, token, authorities};
      localStorage.setItem('user', JSON.stringify(user));
      this.activeUser = new User().deserialize(user);
    })

  }

  login(userData: LoginModel): void {
    this.http.post(`${this.baseUrl}${ENDPOINTS.LOGIN}`, userData)
      .pipe(
        map((res: HttpResponse<void>) =>  new User().deserialize(res))
      ).subscribe((user: User) => {
        this.activeUser = user;
        localStorage.setItem('user', JSON.stringify(user));
        this.router.navigateByUrl('/');
      });
  }

  register(userData: RegisterModel): void {
    this.http.post(`${this.baseUrl}${ENDPOINTS.REGISTER}`, userData)
      .pipe(
        map((res: HttpResponse<void>) =>  new User().deserialize(res))
      ).subscribe(() => {
        this.router.navigateByUrl('/login');
      });
  }

  logOut(): void {
    this.keycloakService.logout().then(() => {
      localStorage.removeItem('user');
      this.router.navigateByUrl('/');
    })
  }

  isAdmin(): boolean {
    if (this.activeUser) {
      return this.activeUser.authorities.includes("ADMIN")
    }
    return false;
  }

  isUser(): boolean {
    if (this.activeUser && this.activeUser.authorities) {
      return this.activeUser.authorities.includes("USER")
    }
    return false;
  }

}
