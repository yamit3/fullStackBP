import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { Client } from '../interfaces/client.interface';
import { Error as ApiError } from '../interfaces/error.interface';
import { environment } from '../../environments/environment';
import {AccountReport, AccountReportRequest} from '../interfaces/account-report.interface';

@Injectable({ providedIn: 'root' })
export class ClientService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/clientes`;
  private readonly handleError = (error: HttpErrorResponse) =>
    throwError(() => this.normalizeError(error));

  getAll(): Observable<Client[]> {
    return this.http.get<Client[]>(this.baseUrl).pipe(catchError(this.handleError));
  }

  getByIdentification(id: string): Observable<Client> {
    return this.http.get<Client>(`${this.baseUrl}/${id}`).pipe(catchError(this.handleError));
  }

  create(client: Omit<Client, 'id'>): Observable<Client> {
    return this.http.post<Client>(this.baseUrl, client).pipe(catchError(this.handleError));
  }

  update(id: number, client: Partial<Omit<Client, 'id'>>): Observable<Client> {
    return this.http.put<Client>(`${this.baseUrl}/${id}`, client).pipe(catchError(this.handleError));
  }

  remove(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`).pipe(catchError(this.handleError));
  }

  getReport(payload: AccountReportRequest): Observable<AccountReport> {
    return this.http.post<AccountReport>(`${this.baseUrl}/reporte`, payload).pipe(catchError(this.handleError));
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

