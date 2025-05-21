import { Component } from "@angular/core";
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { NgForOf, NgIf } from "@angular/common";
import { BalanceService } from "../../balance/balance.component";
import * as CryptoJS from "crypto-js";
import { Constant } from "../../conststnt";

@Component({
  selector: "app-blackjack",
  templateUrl: "./blackjack.component.html",
  standalone: true,
  imports: [NgForOf, NgIf],
  styleUrls: ["./blackjack.component.css"],
})
export class BlackjackComponent {
  gameId: number | null = null;
  playerCards: string[] = [];
  dealerCards: string[] = [];
  gameStatus: string = "";
  gameOver: boolean = false;
  betValue: number = 50;
  showResult: boolean = false;
  intervalId: any;

  constructor(
    private http: HttpClient,
    private balanceService: BalanceService
  ) {}
  private getAuthHeaders() {
    const token = localStorage.getItem("Token");
    return new HttpHeaders({
      Authorization: "Bearer " + token,
    });
  }
  startAutoUpdate() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
    this.intervalId = setInterval(() => {
      if (this.gameId) {
        this.http.get<any>(
          `http://localhost:8081/api/user/blackjack/${this.gameId}`,
          { headers: this.getAuthHeaders() }
        ).subscribe((response) => {
          this.gameStatus = response.status;
          this.playerCards = response.playerCards;
          this.dealerCards = response.dealerCards;
          this.checkIfGameOver(this.gameStatus);

          if (!this.gameOver) {
            this.playerCards = response.playerCards;
            this.dealerCards = ["spate", ...response.dealerCards.slice(1)];
          } else {
            this.playerCards = [];
            this.dealerCards = [];
          }
          if (this.gameOver) {
            this.showResult = true;
            this.playerCards = response.playerCards;
            this.dealerCards = response.dealerCards;
            this.gameStatus = response.status;
            this.checkIfGameOver(this.gameStatus);
            this.getUserBalance();
          }

          if (this.gameOver) {
            clearInterval(this.intervalId);
          }
        });
      }
    }, 1000);
  }
  decriptData(data: string): string {
    const decryptedVal = CryptoJS.AES.decrypt(data, Constant.EN_KEY);
    return decryptedVal.toString(CryptoJS.enc.Utf8);
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
          this.balanceService.updateBalance(user.balance);
        },
        error: (err) => {
          console.error("Failed to get user:", err);
        },
      });
  }

  private checkIfGameOver(status: string) {
    const finishedStatuses = ["PLAYER_WON", "PLAYER_LOST", "DRAW"];
    this.gameOver = finishedStatuses.includes(status);
  }

  startGame() {
    const body = {
      betValue: this.betValue,

    };
    this.startAutoUpdate();
    this.http
      .post<any>("http://localhost:8081/api/user/blackjack/start", body, {
        headers: this.getAuthHeaders(),
      })
      .subscribe((response) => {
        this.gameStatus = response.status;
        this.checkIfGameOver(this.gameStatus);
        this.gameId = response.gameId;

        if (!this.gameOver) {
          this.playerCards = response.playerCards;
          this.dealerCards = ["spate", ...response.dealerCards.slice(1)];
        } else {
          this.playerCards = [];
          this.dealerCards = [];
          this.getUserBalance();
        }
      });
  }

  hit() {
    if (!this.gameId) return;

    this.http
      .put<any>(
        `http://localhost:8081/api/user/blackjack/${this.gameId}/hit`,
        {},
        {
          headers: this.getAuthHeaders(),
        }
      )
      .subscribe((response) => {
        this.gameStatus = response.status;
        this.checkIfGameOver(this.gameStatus);

        if (!this.gameOver) {
          this.playerCards = response.playerCards;
          this.dealerCards = ["spate", ...response.dealerCards.slice(1)];
        } else {
          this.playerCards = [];
          this.dealerCards = [];
        }
        if (this.gameOver) {
          this.showResult = true;
          this.playerCards = response.playerCards;
          this.dealerCards = response.dealerCards;
          this.gameStatus = response.status;
          this.checkIfGameOver(this.gameStatus);
          this.getUserBalance();
        }
      });
  }

  stand() {
    if (!this.gameId) return;

    this.http
      .put<any>(
        `http://localhost:8081/api/user/blackjack/${this.gameId}/stand`,
        {},
        {
          headers: this.getAuthHeaders(),
        }
      )
      .subscribe((response) => {
        this.playerCards = response.playerCards;
        this.dealerCards = response.dealerCards;
        this.gameStatus = response.status;
        this.checkIfGameOver(this.gameStatus);
        this.showResult = true;
        this.getUserBalance();
      });
  }

  nextGame() {
    this.restartGame();
    this.showResult = false;
  }

  restartGame() {
    this.gameStatus = "";
    this.playerCards = [];
    this.dealerCards = [];
    this.gameId = null;
    this.gameOver = false;
    this.startGame();
  }
}
