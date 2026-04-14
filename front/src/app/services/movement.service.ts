import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { Movement } from '../interfaces/movement.interface';
import { Error as ApiError } from '../interfaces/error.interface';

export interface CreateMovementPayload {
  accountId: number;
  type: 'DEPOSIT' | 'WITHDRAW';
  value: number;
}

export type UpdateMovementPayload = Partial<Omit<Movement, 'id'>>;

@Injectable({ providedIn: 'root' })
export class MovementService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/movimientos`;
  private readonly handleError = (error: HttpErrorResponse) =>
    throwError(() => this.normalizeError(error));

  getAll(): Observable<Movement[]> {
    return this.http.get<Movement[]>(this.baseUrl).pipe(catchError(this.handleError));
  }

  getById(id: number): Observable<Movement> {
    return this.http.get<Movement>(`${this.baseUrl}/${id}`).pipe(catchError(this.handleError));
  }

  create(payload: CreateMovementPayload): Observable<Movement> {
    return this.http.post<Movement>(this.baseUrl, payload).pipe(catchError(this.handleError));
  }

  update(id: number, payload: UpdateMovementPayload): Observable<Movement> {
    return this.http.put<Movement>(`${this.baseUrl}/${id}`, payload).pipe(catchError(this.handleError));
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

