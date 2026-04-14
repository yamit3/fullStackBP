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
import { Account, CreateAccountPayload } from '../../interfaces/account.interface';
import { Client } from '../../interfaces/client.interface';
import { Error as ApiError } from '../../interfaces/error.interface';
import { AccountService } from '../../services/account.service';
import { ClientService } from '../../services/client.service';

@Component({
  selector: 'app-cuentas-section',
  imports: [ReactiveFormsModule],
  templateUrl: './cuentas-section.component.html',
  styleUrl: './cuentas-section.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CuentasSectionComponent implements OnInit {
  private readonly accountService = inject(AccountService);
  private readonly clientService = inject(ClientService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly title = 'Cuentas';
  protected readonly searchLabel = 'Buscar cuentas';
  protected readonly searchPlaceholder = 'Buscar';
  protected readonly createButtonAriaLabel = 'Crear nueva cuenta';
  protected readonly tableAriaLabel = 'Listado de cuentas';
  protected readonly searchControl = new FormControl('', { nonNullable: true });
  protected readonly createForm = new FormGroup({
    clientId: new FormControl<number | null>(null, {
      validators: [Validators.required],
    }),
    type: new FormControl<'CHECKING' | 'SAVINGS' | ''>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    initialBalance: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(0)],
    }),
  });
  protected readonly accounts = signal<readonly Account[]>([]);
  protected readonly clients = signal<readonly Client[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly loadError = signal<ApiError | null>(null);
  protected readonly showCreateModal = signal(false);
  protected readonly isCreating = signal(false);
  protected readonly createError = signal<ApiError | null>(null);
  protected readonly editForm = new FormGroup({
    type: new FormControl<'CHECKING' | 'SAVINGS' | ''>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    initialBalance: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(0)],
    }),
  });
  protected readonly showEditModal = signal(false);
  protected readonly isEditing = signal(false);
  protected readonly editingAccountId = signal<number | null>(null);
  protected readonly editingClientName = signal<string | null>(null);
  protected readonly editError = signal<ApiError | null>(null);
  protected readonly showDeleteModal = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly deletingAccountId = signal<number | null>(null);
  protected readonly deletingAccountNumber = signal<string | null>(null);
  protected readonly deleteError = signal<ApiError | null>(null);
  protected readonly searchTerm = toSignal(
    this.searchControl.valueChanges.pipe(
      startWith(this.searchControl.value),
      map((value) => value.trim().toLowerCase()),
    ),
    { initialValue: '' },
  );
  protected readonly filteredAccounts = computed(() => {
    const term = this.searchTerm();

    if (!term) {
      return this.accounts();
    }

    return this.accounts().filter((account) => this.accountMatchesSearch(account, term));
  });

  ngOnInit(): void {
    this.loadAccounts();
    this.loadClients();
  }

  protected loadAccounts(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.accountService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (accounts) => {
          this.accounts.set(accounts);
          this.isLoading.set(false);
        },
        error: (error: ApiError) => {
          this.accounts.set([]);
          this.loadError.set(error);
          this.isLoading.set(false);
        },
      });
  }

  protected loadClients(): void {
    this.clientService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (clients) => {
          this.clients.set(clients);
        },
        error: () => {
          this.clients.set([]);
        },
      });
  }

  protected openCreateModal(): void {
    this.createError.set(null);
    this.createForm.reset({
      clientId: null,
      type: '',
      initialBalance: null,
    });
    this.showCreateModal.set(true);
  }

  protected closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  protected submitCreateAccount(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const formValue = this.createForm.getRawValue();

    if (formValue.clientId === null || formValue.type === '' || formValue.initialBalance === null) {
      return;
    }

    const payload: CreateAccountPayload = {
      clientId: formValue.clientId,
      type: formValue.type,
      initialBalance: formValue.initialBalance,
    };

    this.isCreating.set(true);
    this.createError.set(null);

    this.accountService
      .create(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isCreating.set(false);
          this.closeCreateModal();
          this.loadAccounts();
        },
        error: (error: ApiError) => {
          this.isCreating.set(false);
          this.createError.set(error);
        },
      });
  }

  protected openEditModal(account: Account): void {
    this.editError.set(null);
    this.editingAccountId.set(account.id);
    this.editingClientName.set(account.clientName);
    this.editForm.reset({
      type: account.type,
      initialBalance: account.initialBalance,
    });
    this.showEditModal.set(true);
  }

  protected closeEditModal(): void {
    this.showEditModal.set(false);
    this.editingAccountId.set(null);
    this.editingClientName.set(null);
  }

  protected submitEditAccount(): void {
    if (this.editForm.invalid || this.editingAccountId() === null) {
      this.editForm.markAllAsTouched();
      return;
    }

    const formValue = this.editForm.getRawValue();

    if (formValue.type === '' || formValue.initialBalance === null) {
      return;
    }

    const accountIdToUpdate = this.editingAccountId();

    if (accountIdToUpdate === null) {
      return;
    }

    const payload: Partial<Omit<Account, 'id'>> = {
      type: formValue.type,
      initialBalance: formValue.initialBalance,
    };

    this.isEditing.set(true);
    this.editError.set(null);

    this.accountService
      .update(accountIdToUpdate, payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isEditing.set(false);
          this.closeEditModal();
          this.loadAccounts();
        },
        error: (error: ApiError) => {
          this.isEditing.set(false);
          this.editError.set(error);
        },
      });
  }

  protected openDeleteModal(account: Account): void {
    this.deleteError.set(null);
    this.deletingAccountId.set(account.id);
    this.deletingAccountNumber.set(account.number);
    this.showDeleteModal.set(true);
  }

  protected closeDeleteModal(): void {
    this.showDeleteModal.set(false);
    this.deletingAccountId.set(null);
    this.deletingAccountNumber.set(null);
  }

  protected confirmDeleteAccount(): void {
    const accountId = this.deletingAccountId();

    if (accountId === null) {
      return;
    }

    this.isDeleting.set(true);
    this.deleteError.set(null);

    this.accountService
      .remove(accountId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isDeleting.set(false);
          this.closeDeleteModal();
          this.loadAccounts();
        },
        error: (error: ApiError) => {
          this.isDeleting.set(false);
          this.deleteError.set(error);
        },
      });
  }

  private accountMatchesSearch(account: Account, term: string): boolean {
    const searchableValues = [
      account.number,
      account.type,
      account.initialBalance,
      account.active ? 'activo' : 'inactivo',
      account.clientName,
    ];

    return searchableValues
      .map((value) => String(value).toLowerCase())
      .some((value) => value.includes(term));
  }
}

