import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CuentasSectionComponent } from './cuentas-section.component';
import { AccountService } from '../../services/account.service';
import { ClientService } from '../../services/client.service';
import { Account } from '../../interfaces/account.interface';
import { Client } from '../../interfaces/client.interface';
import { Error as ApiError } from '../../interfaces/error.interface';

describe('CuentasSectionComponent', () => {
  let component: CuentasSectionComponent;
  let fixture: ComponentFixture<CuentasSectionComponent>;

  const mockAccounts: Account[] = [
    {
      id: 1,
      number: '100001',
      type: 'CHECKING',
      initialBalance: 5000,
      currentBalance: 5500,
      active: true,
      clientId: 1,
      clientName: 'John Doe',
    },
    {
      id: 2,
      number: '100002',
      type: 'SAVINGS',
      initialBalance: 2000,
      currentBalance: 2100,
      active: false,
      clientId: 2,
      clientName: 'Jane Smith',
    },
  ];

  const mockClients: Client[] = [
    {
      id: 1,
      name: 'John Doe',
      identification: '1234567890',
      address: 'Main St',
      phone: '555-0001',
      password: 'pass123',
      gender: 'MALE',
      age: 30,
      active: true,
    },
    {
      id: 2,
      name: 'Jane Smith',
      identification: '0987654321',
      address: 'Oak St',
      phone: '555-0002',
      password: 'pass456',
      gender: 'FEMALE',
      age: 28,
      active: true,
    },
  ];

  const mockError: ApiError = {
    message: 'Error de prueba',
    status: 400,
  };

  const accountServiceMock = {
    getAll: vi.fn(() => of(mockAccounts)),
    create: vi.fn(() => of(mockAccounts[0])),
    update: vi.fn(() => of(mockAccounts[0])),
    remove: vi.fn(() => of(void 0)),
  };

  const clientServiceMock = {
    getAll: vi.fn(() => of(mockClients)),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [CuentasSectionComponent],
      providers: [
        { provide: AccountService, useValue: accountServiceMock },
        { provide: ClientService, useValue: clientServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CuentasSectionComponent);
    component = fixture.componentInstance;
  });

  it('should create and load accounts/clients on init', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(accountServiceMock.getAll).toHaveBeenCalled();
    expect(clientServiceMock.getAll).toHaveBeenCalled();
    expect(component['accounts']()).toEqual(mockAccounts);
    expect(component['clients']()).toEqual(mockClients);
  });

  it('should filter accounts by search term', () => {
    fixture.detectChanges();

    component['searchControl'].setValue('100002');

    expect(component['filteredAccounts']().length).toBe(1);
    expect(component['filteredAccounts']()[0].number).toBe('100002');
  });

  it('should open create modal and reset form', () => {
    component['openCreateModal']();

    expect(component['showCreateModal']()).toBeTruthy();
    expect(component['createForm'].getRawValue()).toEqual({
      clientId: null,
      type: '',
      initialBalance: null,
    });
  });

  it('should create account with selected client', () => {
    fixture.detectChanges();
    component['openCreateModal']();
    component['createForm'].setValue({
      clientId: 1,
      type: 'CHECKING',
      initialBalance: 1000,
    });

    component['submitCreateAccount']();

    expect(accountServiceMock.create).toHaveBeenCalledWith({
      clientId: 1,
      type: 'CHECKING',
      initialBalance: 1000,
    });
    expect(component['showCreateModal']()).toBeFalsy();
  });

  it('should not create when form is invalid', () => {
    component['submitCreateAccount']();

    expect(accountServiceMock.create).not.toHaveBeenCalled();
    expect(component['createForm'].touched).toBeTruthy();
  });

  it('should open edit modal with account data and non-editable client name context', () => {
    component['openEditModal'](mockAccounts[0]);

    expect(component['showEditModal']()).toBeTruthy();
    expect(component['editingClientName']()).toBe('John Doe');
    expect(component['editForm'].getRawValue()).toEqual({
      type: 'CHECKING',
      initialBalance: 5000,
    });
  });

  it('should update account without status/client fields', () => {
    component['openEditModal'](mockAccounts[0]);
    component['editForm'].setValue({
      type: 'SAVINGS',
      initialBalance: 3000,
    });

    component['submitEditAccount']();

    expect(accountServiceMock.update).toHaveBeenCalledWith(
      1,
      {
        type: 'SAVINGS',
        initialBalance: 3000,
      },
    );
  });

  it('should open and confirm delete account', () => {
    component['openDeleteModal'](mockAccounts[0]);

    expect(component['showDeleteModal']()).toBeTruthy();
    expect(component['deletingAccountNumber']()).toBe('100001');

    component['confirmDeleteAccount']();

    expect(accountServiceMock.remove).toHaveBeenCalledWith(1);
    expect(component['showDeleteModal']()).toBeFalsy();
  });

  it('should handle load error state', () => {
    accountServiceMock.getAll.mockReturnValueOnce(throwError(() => mockError));

    component['loadAccounts']();

    expect(component['loadError']()).toEqual(mockError);
    expect(component['accounts']()).toEqual([]);
  });

  it('should handle delete error state', () => {
    accountServiceMock.remove.mockReturnValueOnce(throwError(() => mockError));

    component['openDeleteModal'](mockAccounts[0]);
    component['confirmDeleteAccount']();

    expect(component['deleteError']()).toEqual(mockError);
    expect(component['showDeleteModal']()).toBeTruthy();
  });
});

