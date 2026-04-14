export interface Movement {
  accountId: number;
  balance: number;
  date: number;
  id: number;
  type: 'DEPOSIT' | 'WITHDRAW';
  value: number;
}

