import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { map, startWith } from 'rxjs';
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
  protected readonly createForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    identification: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    address: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    phone: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    gender: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    age: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0), Validators.max(120)] }),
  });
  protected readonly editForm = new FormGroup({
    name: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    identification: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    address: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    phone: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    gender: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    age: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0), Validators.max(120)] }),
    active: new FormControl(true, { nonNullable: true }),
  });
  protected readonly clients = signal<readonly Client[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly isCreating = signal(false);
  protected readonly showCreateModal = signal(false);
  protected readonly isEditing = signal(false);
  protected readonly showEditModal = signal(false);
  protected readonly editingClientId = signal<number | null>(null);
  protected readonly loadError = signal<ApiError | null>(null);
  protected readonly createError = signal<ApiError | null>(null);
  protected readonly editError = signal<ApiError | null>(null);
  protected readonly searchTerm = toSignal(
    this.searchControl.valueChanges.pipe(
      startWith(this.searchControl.value),
      map((value) => value.trim().toLowerCase()),
    ),
    { initialValue: '' },
  );
  protected readonly filteredClients = computed(() => {
    const term = this.searchTerm();

    if (!term) {
      return this.clients();
    }

    return this.clients().filter((client) => this.clientMatchesSearch(client, term));
  });

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

  protected openCreateModal(): void {
    this.createError.set(null);
    this.createForm.reset({
      name: '',
      identification: '',
      address: '',
      phone: '',
      password: '',
      gender: '',
      age: null,
    });
    this.showCreateModal.set(true);
  }

  protected closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  protected submitCreateClient(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const formValue = this.createForm.getRawValue();
    const payload: Omit<Client, 'id'> = {
      active: true,
      address: formValue.address.trim(),
      age: formValue.age,
      gender: formValue.gender.trim(),
      identification: formValue.identification.trim(),
      name: formValue.name.trim(),
      password: formValue.password.trim(),
      phone: formValue.phone.trim(),
    };

    this.isCreating.set(true);
    this.createError.set(null);

    this.clientService
      .create(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isCreating.set(false);
          this.closeCreateModal();
          this.loadClients();
        },
        error: (error: ApiError) => {
          this.isCreating.set(false);
          this.createError.set(error);
        },
      });
  }

  protected openEditModal(client: Client): void {
    this.editError.set(null);
    this.editingClientId.set(client.id);
    this.editForm.reset({
      name: client.name,
      identification: client.identification,
      address: client.address,
      phone: client.phone,
      password: client.password ?? '',
      gender: client.gender,
      age: client.age,
      active: client.active,
    });
    this.showEditModal.set(true);
  }

  protected closeEditModal(): void {
    this.showEditModal.set(false);
    this.editingClientId.set(null);
  }

  protected submitEditClient(): void {
    if (this.editForm.invalid || this.editingClientId() === null) {
      this.editForm.markAllAsTouched();
      return;
    }

    const clientId = this.editingClientId()!;
    const formValue = this.editForm.getRawValue();
    const payload: Partial<Omit<Client, 'id'>> = {
      active: formValue.active,
      address: formValue.address.trim(),
      age: formValue.age,
      gender: formValue.gender.trim(),
      identification: formValue.identification.trim(),
      name: formValue.name.trim(),
      password: formValue.password.trim(),
      phone: formValue.phone.trim(),
    };

    this.isEditing.set(true);
    this.editError.set(null);

    this.clientService
      .update(clientId, payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isEditing.set(false);
          this.closeEditModal();
          this.loadClients();
        },
        error: (error: ApiError) => {
          this.isEditing.set(false);
          this.editError.set(error);
        },
      });
  }

  private clientMatchesSearch(client: Client, term: string): boolean {
    const searchableValues = [
      client.id,
      client.name,
      client.identification,
      client.address,
      client.phone,
      client.gender,
      client.age,
      client.active ? 'activo' : 'inactivo',
      client.password ?? '',
    ];

    return searchableValues
      .map((value) => String(value).toLowerCase())
      .some((value) => value.includes(term));
  }

}

