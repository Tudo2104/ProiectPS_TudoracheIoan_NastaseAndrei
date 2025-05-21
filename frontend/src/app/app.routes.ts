import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { LayoutComponent } from './pages/layout/layout.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import {RegisterComponent} from "./pages/register/register.component";
import {AuthGuard} from "./auth.guard";
import {LoggedInRedirectGuard} from "./logged-in-redirect-guard.guard";
import {ChatComponent} from "./pages/chat/chat.component";

export const routes: Routes = [
    {
      path:'',
      redirectTo: 'login',
      pathMatch:'full'
    },
    {
      path:'login',
      component:LoginComponent,
      canActivate: [LoggedInRedirectGuard]
    },
    {
      path:'',
      component:LayoutComponent,
        children: [
          {
            path:'dashboard',
            component:DashboardComponent,
            canActivate: [AuthGuard]

          }
        ]
    },
    {
      path:'register',
      component:RegisterComponent,
      canActivate: [LoggedInRedirectGuard]
    },

];
