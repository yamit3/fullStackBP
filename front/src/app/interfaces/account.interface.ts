export interface Account {
  active: boolean;
  clientId: number;
  currentBalance: number;
  id: number;
  initialBalance: number;
  number: string;
  type: 'CHECKING' | 'SAVINGS';
}

