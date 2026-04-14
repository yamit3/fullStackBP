export interface Account {
  active: boolean;
  clientId: number;
  clientName: string;
  currentBalance: number;
  id: number;
  initialBalance: number;
  number: string;
  type: 'CHECKING' | 'SAVINGS';
}

export interface CreateAccountPayload {
  clientId: number;
  type: 'CHECKING' | 'SAVINGS';
  initialBalance: number;
}

