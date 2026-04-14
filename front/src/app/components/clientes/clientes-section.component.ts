import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ClientService } from '../../services/client.service';
import { Client } from '../../interfaces/client.interface';
import { Error as ApiError } from '../../interfaces/error.interface';

@Component({
  selector: 'app-clientes-section',
  imports: [ReactiveFormsModule],
  templateUrl: './clientes-section.component.html',
  styleUrl: './clientes-section.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ClientesSectionComponent implements OnInit {
  private readonly clientService = inject(ClientService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly title = 'Clientes';
  protected readonly searchLabel = 'Buscar clientes';
  protected readonly searchPlaceholder = 'Buscar';
  protected readonly createButtonAriaLabel = 'Crear nuevo cliente';
  protected readonly tableAriaLabel = 'Listado de clientes';
  protected readonly searchControl = new FormControl('', { nonNullable: true });
  protected readonly clients = signal<readonly Client[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly loadError = signal<ApiError | null>(null);

  ngOnInit(): void {
    this.loadClients();
  }
  protected loadClients(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.clientService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (clients) => {
          this.clients.set(clients);
          this.isLoading.set(false);
        },
        error: (error: ApiError) => {
          this.clients.set([]);
          this.loadError.set(error);
          this.isLoading.set(false);
        },
      });
  }

}

