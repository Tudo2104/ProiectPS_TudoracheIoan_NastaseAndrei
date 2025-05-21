import { HttpClient } from "@angular/common/http";
import { Component, inject, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";

import * as CryptoJS from "crypto-js";
import { Constant } from "../../conststnt";
import { NgIf } from "@angular/common";

@Component({
  selector: "app-login",
  standalone: true,
  imports: [FormsModule, RouterLink, NgIf],
  templateUrl: "./login.component.html",
  styleUrl: "./login.component.css",
})
export class LoginComponent {
  loginForm = {
    name: "",
    password: "",
  };

  successMessage: string = "";
  errorMessage: string = "";

  http = inject(HttpClient);
  router = inject(Router);

  encriptData(data: any) {
    return CryptoJS.AES.encrypt(data, Constant.EN_KEY).toString();
  }

  Login() {
    this.errorMessage = "";
    this.successMessage = "";

    this.http
      .post("http://localhost:8081/api/user/login", this.loginForm, {
        responseType: "text",
      })
      .subscribe({
        next: (response: string) => {
          const match = response.match(
            /eyJ[a-zA-Z0-9\-_]+\.[a-zA-Z0-9\-_]+\.[a-zA-Z0-9\-_]+/
          );
          const token = match ? match[0] : null;

          if (token) {
            const encryptedName = this.encriptData(this.loginForm.name);
            localStorage.setItem("Name", encryptedName);
            localStorage.setItem("Token", token);
            this.successMessage = "Login successful!";
          } else {
            this.errorMessage = "Token not found in response.";
          }
        },
        error: (err) => {
          try {
            const parsedError = JSON.parse(err.error);
            this.errorMessage = parsedError.message || "Login failed.";
          } catch {
            this.errorMessage = "Login failed. Please try again.";
          }
        },
      });
  }

  successPopup() {
    this.successMessage = "";
    this.router.navigateByUrl("/dashboard", { replaceUrl: true });
  }

  closeErrorPopup() {
    this.errorMessage = "";
  }
}
