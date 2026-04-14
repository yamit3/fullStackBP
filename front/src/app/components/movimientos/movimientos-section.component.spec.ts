import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of, throwError } from 'rxjs';
import { MovimientosSectionComponent } from './movimientos-section.component';
import { MovementService } from '../../services/movement.service';
import { AccountService } from '../../services/account.service';
import { Movement } from '../../interfaces/movement.interface';
import { Account } from '../../interfaces/account.interface';
import { Error as ApiError } from '../../interfaces/error.interface';

describe('MovimientosSectionComponent', () => {
  let component: MovimientosSectionComponent;
  let fixture: ComponentFixture<MovimientosSectionComponent>;

  const mockMovements: Movement[] = [
    {
      id: 1,
      accountId: 1,
      accountNumber: '100001',
      accountType: 'CHECKING',
      balance: 1500,
      date: 1735689600000,
      type: 'DEPOSIT',
      value: 100,
      active: true,
    },
    {
      id: 2,
      accountId: 2,
      accountNumber: '100002',
      accountType: 'SAVINGS',
      balance: 1200,
      date: 1735776000000,
      type: 'WITHDRAW',
      value: -20,
      active: true,
    },
  ];

  const mockAccounts: Account[] = [
    {
      id: 1,
      number: '100001',
      type: 'CHECKING',
      initialBalance: 1000,
      currentBalance: 1500,
      active: true,
      clientId: 1,
      clientName: 'John Doe',
    },
    {
      id: 2,
      number: '100002',
      type: 'SAVINGS',
      initialBalance: 1200,
      currentBalance: 1200,
      active: true,
      clientId: 2,
      clientName: 'Jane Doe',
    },
  ];

  const mockApiError: ApiError = {
    message: 'Request failed',
    status: 400,
  };

  const movementServiceMock = {
    getAll: vi.fn(() => of(mockMovements)),
    create: vi.fn(() => of(mockMovements[0])),
    update: vi.fn(() => of(mockMovements[0])),
    remove: vi.fn(() => of(void 0)),
  };

  const accountServiceMock = {
    getAll: vi.fn(() => of(mockAccounts)),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [MovimientosSectionComponent],
      providers: [
        { provide: MovementService, useValue: movementServiceMock },
        { provide: AccountService, useValue: accountServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MovimientosSectionComponent);
    component = fixture.componentInstance;
  });

  it('should create and load movements/accounts on init', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(movementServiceMock.getAll).toHaveBeenCalled();
    expect(accountServiceMock.getAll).toHaveBeenCalled();
    expect(component['movements']()).toEqual(mockMovements);
    expect(component['accounts']()).toEqual(mockAccounts);
  });

  it('should handle movement load error', () => {
    movementServiceMock.getAll.mockReturnValueOnce(throwError(() => mockApiError));

    component['loadMovements']();

    expect(component['loadError']()).toEqual(mockApiError);
    expect(component['movements']()).toEqual([]);
    expect(component['isLoading']()).toBe(false);
  });

  it('should filter movements by search term', () => {
    fixture.detectChanges();

    component['searchControl'].setValue('100002');

    expect(component['filteredMovements']().length).toBe(1);
    expect(component['filteredMovements']()[0].id).toBe(2);
  });

  it('should open create modal and reset create form', () => {
    component['openCreateModal']();

    expect(component['showCreateModal']()).toBe(true);
    expect(component['createForm'].getRawValue()).toEqual({
      accountId: null,
      type: '',
      value: null,
    });
  });

  it('should not create movement when create form is invalid', () => {
    component['submitCreateMovement']();

    expect(movementServiceMock.create).not.toHaveBeenCalled();
    expect(component['createForm'].touched).toBe(true);
  });

  it('should create movement with valid payload and reload list', () => {
    fixture.detectChanges();
    const loadSpy = vi.spyOn(component as any, 'loadMovements');

    component['openCreateModal']();
    component['createForm'].setValue({
      accountId: 1,
      type: 'DEPOSIT',
      value: 100,
    });

    component['submitCreateMovement']();

    expect(movementServiceMock.create).toHaveBeenCalledWith({
      accountId: 1,
      type: 'DEPOSIT',
      value: 100,
    });
    expect(component['showCreateModal']()).toBe(false);
    expect(loadSpy).toHaveBeenCalled();
  });

  it('should open edit modal with movement values', () => {
    component['openEditModal'](mockMovements[1]);

    expect(component['showEditModal']()).toBe(true);
    expect(component['editingMovementId']()).toBe(2);
    expect(component['editingAccountNumber']()).toBe('100002');
    expect(component['editForm'].getRawValue()).toEqual({
      type: 'WITHDRAW',
      value: 20,
    });
  });

  it('should update movement with valid payload', () => {
    fixture.detectChanges();

    component['openEditModal'](mockMovements[0]);
    component['editForm'].setValue({
      type: 'WITHDRAW',
      value: 80,
    });

    component['submitEditMovement']();

    expect(movementServiceMock.update).toHaveBeenCalledWith(1, {
      type: 'WITHDRAW',
      value: 80,
    });
    expect(component['showEditModal']()).toBe(false);
  });

  it('should open delete modal and confirm delete', () => {
    fixture.detectChanges();

    component['openDeleteModal'](mockMovements[0]);
    expect(component['showDeleteModal']()).toBe(true);
    expect(component['deletingMovementId']()).toBe(1);

    component['confirmDeleteMovement']();

    expect(movementServiceMock.remove).toHaveBeenCalledWith(1);
    expect(component['showDeleteModal']()).toBe(false);
  });

  it('should not delete when deletingMovementId is null', () => {
    component['deletingMovementId'].set(null);

    component['confirmDeleteMovement']();

    expect(movementServiceMock.remove).not.toHaveBeenCalled();
  });

  it('should handle delete error and keep modal open', () => {
    movementServiceMock.remove.mockReturnValueOnce(throwError(() => mockApiError));

    component['openDeleteModal'](mockMovements[0]);
    component['confirmDeleteMovement']();

    expect(component['deleteError']()).toEqual(mockApiError);
    expect(component['showDeleteModal']()).toBe(true);
  });

  it('should format date as dd/mm/yyyy', () => {
    const localMidday = new Date(2025, 0, 1, 12, 0, 0).getTime();
    expect(component['formatDate'](localMidday)).toBe('01/01/2025');
  });

  it('should return absolute value', () => {
    expect(component['abs'](-25)).toBe(25);
    expect(component['abs'](25)).toBe(25);
  });
});

