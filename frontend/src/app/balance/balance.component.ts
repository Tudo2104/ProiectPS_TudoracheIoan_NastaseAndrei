import { Injectable } from "@angular/core";
import { BehaviorSubject } from "rxjs";

@Injectable({ providedIn: "root" })
export class BalanceService {
  private balanceSubject = new BehaviorSubject<number>(0);

  balance$ = this.balanceSubject.asObservable();
  updateBalance(newBalance: number) {
    this.balanceSubject.next(newBalance);
  }
  get currentBalance(): number {
    return this.balanceSubject.getValue();
  }
}
