export interface AccountReportItem {
  balance: number;
  deposits: number;
  number: string;
  withdraws: number;
}

export interface AccountReport {
  accounts: readonly AccountReportItem[];
  pdf: string | null;
}

export interface AccountReportRequest {
  identification: string;
  startDate: number;
  endDate: number;
  pdf: boolean;
}

