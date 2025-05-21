import { DecimalPipe, JsonPipe } from "@angular/common";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Component, OnInit, inject } from "@angular/core";
import * as CryptoJS from "crypto-js";
import { Constant } from "../../conststnt";
import { RouterOutlet } from "@angular/router";
import { ChatComponent } from "../chat/chat.component";
import { BlackjackComponent } from "../blackjack/blackjack.component";
import { BalanceService } from "../../balance/balance.component";

@Component({
  selector: "app-dashboard",
  standalone: true,
  imports: [
    JsonPipe,
    RouterOutlet,
    ChatComponent,
    BlackjackComponent,
    DecimalPipe,
  ],
  templateUrl: "./dashboard.component.html",
  styleUrl: "./dashboard.component.css",
})
export class DashboardComponent implements OnInit {
  balance: number = 0;
  http = inject(HttpClient);
  constructor(private balanceService: BalanceService) {}
  ngOnInit(): void {
    this.balanceService.balance$.subscribe((bal) => {
      this.balance = bal;
    });
  }
}
