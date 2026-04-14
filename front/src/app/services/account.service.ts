import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { Account, CreateAccountPayload } from '../interfaces/account.interface';
import { Error as ApiError } from '../interfaces/error.interface';
import { AccountReport, AccountReportRequest } from '../interfaces/account-report.interface';


export type UpdateAccountPayload = Partial<Omit<Account, 'id'>>;

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/cuentas`;
  private readonly handleError = (error: HttpErrorResponse) =>
    throwError(() => this.normalizeError(error));

  getAll(): Observable<Account[]> {
    return this.http.get<Account[]>(this.baseUrl).pipe(catchError(this.handleError));
  }

  getById(id: number): Observable<Account> {
    return this.http.get<Account>(`${this.baseUrl}/${id}`).pipe(catchError(this.handleError));
  }

  create(payload: CreateAccountPayload): Observable<Account> {
    return this.http.post<Account>(this.baseUrl, payload).pipe(catchError(this.handleError));
  }

  update(id: number, payload: UpdateAccountPayload): Observable<Account> {
    return this.http.put<Account>(`${this.baseUrl}/${id}`, payload).pipe(catchError(this.handleError));
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(catchError(this.handleError));
  }

  private normalizeError(error: HttpErrorResponse): ApiError {
    const payload = error.error as Partial<ApiError> | null;

    return {
      message:
        typeof payload?.message === 'string' ? payload.message : 'Unexpected error occurred.',
      status: typeof payload?.status === 'number' ? payload.status : (error.status || 500),
    };
  }
}

