import { HttpClient } from "@angular/common/http";
import { Component, inject, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { Router, RouterLink } from "@angular/router";

import { Constant } from "../../conststnt";
import { NgIf } from "@angular/common";
import * as CryptoJS from "crypto-js";

@Component({
  selector: "app-register",
  standalone: true,
  imports: [FormsModule, RouterLink, NgIf],
  templateUrl: "./register.component.html",
  styleUrl: "./register.component.css",
})
export class RegisterComponent {
  registerForm = {
    name: "",
    email: "",
    password: "",
  };

  http = inject(HttpClient);
  router = inject(Router);

  successMessage: string = "";
  errorMessage: string = "";
  encriptData(data: any) {
    return CryptoJS.AES.encrypt(data, Constant.EN_KEY).toString();
  }
  Register() {
    this.errorMessage = "";
    this.successMessage = "";

    this.http
      .post("http://localhost:8081/api/user/create", this.registerForm, {
        responseType: "text",
      })
      .subscribe({
        next: (response: string) => {
          const match = response.match(
            /eyJ[a-zA-Z0-9\-_]+\.[a-zA-Z0-9\-_]+\.[a-zA-Z0-9\-_]+/
          );
          const token = match ? match[0] : null;

          if (token) {
            const encryptedName = this.encriptData(this.registerForm.name);
            localStorage.setItem("Name", encryptedName);
            localStorage.setItem("Token", token);
            this.successMessage = "Register successful!";
          } else {
            this.errorMessage = "Token not found in response.";
          }
        },
        error: (err) => {
          try {
            const parsedError = JSON.parse(err.error);
            this.errorMessage = parsedError.message || "Something went wrong.";
          } catch {
            this.errorMessage = "Something went wrong. Please try again.";
          }
        },
      });
  }
  closeErrorPopup() {
    this.errorMessage = "";
  }
  successPopup() {
    this.errorMessage = "";
    this.router.navigateByUrl("/dashboard", { replaceUrl: true });
  }
}
