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
import { Movement } from '../../interfaces/movement.interface';
import { Account } from '../../interfaces/account.interface';
import { Error as ApiError } from '../../interfaces/error.interface';
import { MovementService, CreateMovementPayload } from '../../services/movement.service';
import { AccountService } from '../../services/account.service';

@Component({
  selector: 'app-movimientos-section',
  imports: [ReactiveFormsModule],
  templateUrl: './movimientos-section.component.html',
  styleUrl: './movimientos-section.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MovimientosSectionComponent implements OnInit {
  private readonly movementService = inject(MovementService);
  private readonly accountService = inject(AccountService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly title = 'Movimientos';
  protected readonly searchLabel = 'Buscar movimientos';
  protected readonly searchPlaceholder = 'Buscar';
  protected readonly createButtonAriaLabel = 'Crear nuevo movimiento';
  protected readonly tableAriaLabel = 'Listado de movimientos';
  protected readonly searchControl = new FormControl('', { nonNullable: true });

  protected readonly createForm = new FormGroup({
    accountId: new FormControl<number | null>(null, {
      validators: [Validators.required],
    }),
    type: new FormControl<'DEPOSIT' | 'WITHDRAW' | ''>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    value: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(0.01)],
    }),
  });

  protected readonly editForm = new FormGroup({
    type: new FormControl<'DEPOSIT' | 'WITHDRAW' | ''>('', {
      nonNullable: true,
      validators: [Validators.required],
    }),
    value: new FormControl<number | null>(null, {
      validators: [Validators.required, Validators.min(0.01)],
    }),
  });

  protected readonly movements = signal<readonly Movement[]>([]);
  protected readonly accounts = signal<readonly Account[]>([]);
  protected readonly isLoading = signal(false);
  protected readonly loadError = signal<ApiError | null>(null);

  protected readonly showCreateModal = signal(false);
  protected readonly isCreating = signal(false);
  protected readonly createError = signal<ApiError | null>(null);

  protected readonly showEditModal = signal(false);
  protected readonly isEditing = signal(false);
  protected readonly editingMovementId = signal<number | null>(null);
  protected readonly editingAccountNumber = signal<string | null>(null);
  protected readonly editError = signal<ApiError | null>(null);

  protected readonly showDeleteModal = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly deletingMovementId = signal<number | null>(null);
  protected readonly deleteError = signal<ApiError | null>(null);

  protected readonly searchTerm = toSignal(
    this.searchControl.valueChanges.pipe(
      startWith(this.searchControl.value),
      map((value) => value.trim().toLowerCase()),
    ),
    { initialValue: '' },
  );

  protected readonly filteredMovements = computed(() => {
    const term = this.searchTerm();

    if (!term) {
      return this.movements();
    }

    return this.movements().filter((m) => this.movementMatchesSearch(m, term));
  });

  ngOnInit(): void {
    this.loadMovements();
    this.loadAccounts();
  }

  protected loadMovements(): void {
    this.isLoading.set(true);
    this.loadError.set(null);

    this.movementService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (movements) => {
          this.movements.set(movements);
          this.isLoading.set(false);
        },
        error: (error: ApiError) => {
          this.movements.set([]);
          this.loadError.set(error);
          this.isLoading.set(false);
        },
      });
  }

  protected loadAccounts(): void {
    this.accountService
      .getAll()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (accounts) => this.accounts.set(accounts),
        error: () => this.accounts.set([]),
      });
  }

  protected openCreateModal(): void {
    this.createError.set(null);
    this.createForm.reset({ accountId: null, type: '', value: null });
    this.showCreateModal.set(true);
  }

  protected closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  protected submitCreateMovement(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const formValue = this.createForm.getRawValue();

    if (formValue.accountId === null || formValue.type === '' || formValue.value === null) {
      return;
    }

    const payload: CreateMovementPayload = {
      accountId: formValue.accountId,
      type: formValue.type,
      value: formValue.value,
    };

    this.isCreating.set(true);
    this.createError.set(null);

    this.movementService
      .create(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isCreating.set(false);
          this.closeCreateModal();
          this.loadMovements();
        },
        error: (error: ApiError) => {
          this.isCreating.set(false);
          this.createError.set(error);
        },
      });
  }

  protected openEditModal(movement: Movement): void {
    this.editError.set(null);
    this.editingMovementId.set(movement.id);
    this.editingAccountNumber.set(movement.accountNumber);
    this.editForm.reset({ type: movement.type, value: this.abs(movement.value) });
    this.showEditModal.set(true);
  }

  protected closeEditModal(): void {
    this.showEditModal.set(false);
    this.editingMovementId.set(null);
    this.editingAccountNumber.set(null);
  }

  protected submitEditMovement(): void {
    if (this.editForm.invalid || this.editingMovementId() === null) {
      this.editForm.markAllAsTouched();
      return;
    }

    const formValue = this.editForm.getRawValue();

    if (formValue.type === '' || formValue.value === null) {
      return;
    }

    const movementId = this.editingMovementId();

    if (movementId === null) {
      return;
    }

    this.isEditing.set(true);
    this.editError.set(null);

    this.movementService
      .update(movementId, { type: formValue.type, value: formValue.value })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isEditing.set(false);
          this.closeEditModal();
          this.loadMovements();
        },
        error: (error: ApiError) => {
          this.isEditing.set(false);
          this.editError.set(error);
        },
      });
  }

  protected openDeleteModal(movement: Movement): void {
    this.deleteError.set(null);
    this.deletingMovementId.set(movement.id);
    this.showDeleteModal.set(true);
  }

  protected closeDeleteModal(): void {
    this.showDeleteModal.set(false);
    this.deletingMovementId.set(null);
  }

  protected confirmDeleteMovement(): void {
    const movementId = this.deletingMovementId();

    if (movementId === null) {
      return;
    }

    this.isDeleting.set(true);
    this.deleteError.set(null);

    this.movementService
      .remove(movementId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isDeleting.set(false);
          this.closeDeleteModal();
          this.loadMovements();
        },
        error: (error: ApiError) => {
          this.isDeleting.set(false);
          this.deleteError.set(error);
        },
      });
  }

  protected abs(value: number): number {
    return Math.abs(value);
  }

  protected formatDate(millis: number): string {
    const date = new Date(millis);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    return `${day}/${month}/${year}`;
  }

  private movementMatchesSearch(movement: Movement, term: string): boolean {
    return [
      movement.accountNumber,
      movement.accountType,
      movement.balance,
      movement.active ? 'activo' : 'inactivo',
      movement.type,
      movement.value,
    ]
      .map((v) => String(v).toLowerCase())
      .some((v) => v.includes(term));
  }
}
