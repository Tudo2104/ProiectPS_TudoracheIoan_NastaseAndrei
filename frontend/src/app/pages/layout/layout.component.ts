import { Component, inject, OnInit } from "@angular/core";
import { Router, RouterOutlet } from "@angular/router";
import { BlackjackComponent } from "../blackjack/blackjack.component";
import { ChatComponent } from "../chat/chat.component";
import { HttpHeaders, HttpClient } from "@angular/common/http";
import { FormsModule } from "@angular/forms";
import { DecimalPipe, NgForOf, NgIf } from "@angular/common";
import * as CryptoJS from "crypto-js";
import { Constant } from "../../conststnt";
import { BalanceService } from "../../balance/balance.component";

@Component({
  selector: "app-layout",
  standalone: true,
  imports: [
    RouterOutlet,
    BlackjackComponent,
    ChatComponent,
    FormsModule,
    DecimalPipe,
    NgIf,
    NgForOf,
  ],
  templateUrl: "./layout.component.html",
  styleUrl: "./layout.component.css",
})
export class LayoutComponent implements OnInit {
  ngOnInit() {
    this.getUserBalance();
  }
  constructor(private router: Router, private balanceService: BalanceService) {}
  depositMessage: string = "";
  showDepositPopup = false;
  depositAmount: number = 0;
  balance: number = 0;
  showHistoryPopup = false;
  historyList: any[] = [];
  http = inject(HttpClient);

  logout() {
    localStorage.removeItem("Token");
    localStorage.removeItem("Name");
    this.router.navigateByUrl("/login", { replaceUrl: true });
  }

  logoutOnClose() {
    localStorage.removeItem("Token");
    localStorage.removeItem("Name");
  }
  decriptData(data: string): string {
    const decryptedVal = CryptoJS.AES.decrypt(data, Constant.EN_KEY);
    return decryptedVal.toString(CryptoJS.enc.Utf8);
  }
  private getAuthHeaders() {
    const token = localStorage.getItem("Token");
    return new HttpHeaders({
      Authorization: "Bearer " + token,
    });
  }
  getUserBalance() {
    const encryptedName = localStorage.getItem("Name");
    const token = this.getAuthHeaders();

    if (!encryptedName || !token) return;

    const decryptedName = this.decriptData(encryptedName);

    this.http
      .get<any>(
        `http://localhost:8081/api/user/getUserByName/${decryptedName}`,
        { headers: this.getAuthHeaders() }
      )
      .subscribe({
        next: (user) => {
          this.balance = user.balance;
          this.balanceService.updateBalance(user.balance);
        },
        error: (err) => {
          console.error("Failed to get user:", err);
        },
      });
  }

  submitDeposit() {
    this.http
      .post<any>(
        `http://localhost:8081/api/user/addAmount/${this.depositAmount}`,
        null,
        {
          headers: this.getAuthHeaders(),
          responseType: "text" as "json",
        }
      )
      .subscribe({
        next: (response) => {
          console.log("Server response:", response);
          this.getUserBalance();
          this.showDepositPopup = false;
        },
        error: (err) => {
          this.depositMessage = "Deposit failed: " + err.error;
          console.error("Failed to get user:", err);
        },
      });
  }

  getHistory() {
    this.http
      .get<any>(`http://localhost:8081/api/user/history`, {
        headers: this.getAuthHeaders(),
      })
      .subscribe({
        next: (history) => {
          this.historyList = history;
          this.showHistoryPopup = true;
        },
        error: (err) => {
          console.error("Failed to get history:", err);
          this.historyList = [];
          this.showHistoryPopup = true;
        },
      });
  }
}
