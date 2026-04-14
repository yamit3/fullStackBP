import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { ClientesSectionComponent } from './clientes-section.component';
import { ClientService } from '../../services/client.service';
import { Client } from '../../interfaces/client.interface';
import { Error as ApiError } from '../../interfaces/error.interface';

describe('ClientesSectionComponent', () => {
  let component: ClientesSectionComponent;
  let fixture: ComponentFixture<ClientesSectionComponent>;
  let clientService: Partial<ClientService>;

  const mockClients: Client[] = [
    {
      id: 1,
      name: 'John Doe',
      identification: '1234567890',
      address: '123 Main St',
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
      address: '456 Oak Ave',
      phone: '555-0002',
      password: 'pass456',
      gender: 'FEMALE',
      age: 28,
      active: true,
    },
  ];

  const mockError: ApiError = {
    message: 'Test error occurred',
    status: 400,
  };

  beforeEach(async () => {
    const mockClientService: Partial<ClientService> = {
      getAll: vi.fn(() => of(mockClients)),
      create: vi.fn(),
      update: vi.fn(),
      remove: vi.fn(),
      getByIdentification: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [ClientesSectionComponent, ReactiveFormsModule],
      providers: [{ provide: ClientService, useValue: mockClientService }],
    }).compileComponents();

    clientService = TestBed.inject(ClientService);
    fixture = TestBed.createComponent(ClientesSectionComponent);
    component = fixture.componentInstance;
  });

  describe('Initialization', () => {
    it('should create the component', () => {
      expect(component).toBeTruthy();
    });

    it('should load clients on init', () => {
      (clientService.getAll as any) = vi.fn(() => of(mockClients));
      fixture.detectChanges();
      expect(clientService.getAll).toHaveBeenCalled();
    });

    it('should handle load error', async () => {
      (clientService.getAll as any) = vi.fn(() => throwError(() => mockError));
      fixture.detectChanges();
      await new Promise((resolve) => setTimeout(resolve, 100));
      expect(component['loadError']()).toEqual(mockError);
    });
  });

  describe('Search Filtering', () => {
    beforeEach(() => {
      (clientService.getAll as any) = vi.fn(() => of(mockClients));
      fixture.detectChanges();
    });

    it('should filter clients by name', async () => {
      component['searchControl'].setValue('John');
      await new Promise((resolve) => setTimeout(resolve, 100));
      const filtered = component['filteredClients']();
      expect(filtered.length).toBe(1);
      expect(filtered[0].name).toBe('John Doe');
    });

    it('should return all clients when search is empty', async () => {
      component['searchControl'].setValue('');
      await new Promise((resolve) => setTimeout(resolve, 100));
      const filtered = component['filteredClients']();
      expect(filtered.length).toBe(2);
    });
  });

  describe('Create Modal', () => {
    it('should open create modal', () => {
      component['openCreateModal']();
      expect(component['showCreateModal']()).toBeTruthy();
      expect(component['createError']()).toBeNull();
    });

    it('should close create modal', () => {
      component['showCreateModal'].set(true);
      component['closeCreateModal']();
      expect(component['showCreateModal']()).toBeFalsy();
    });

    it('should submit create form successfully', async () => {
      (clientService.create as any) = vi.fn(() => of(mockClients[0]));
      (clientService.getAll as any) = vi.fn(() => of(mockClients));

      component['createForm'].patchValue({
        name: 'New Client',
        identification: '1111111111',
        address: '123 Test St',
        phone: '555-0003',
        password: 'test123',
        gender: 'MALE',
        age: 25,
      });

      component['submitCreateClient']();
      await new Promise((resolve) => setTimeout(resolve, 100));
      expect(clientService.create).toHaveBeenCalled();
    });
  });

  describe('Edit Modal', () => {
    it('should open edit modal with client data', () => {
      component['openEditModal'](mockClients[0]);
      expect(component['showEditModal']()).toBeTruthy();
      expect(component['editingClientId']()).toBe(1);
      expect(component['editForm'].get('name')?.value).toBe('John Doe');
    });

    it('should close edit modal', () => {
      component['showEditModal'].set(true);
      component['editingClientId'].set(1);
      component['closeEditModal']();
      expect(component['showEditModal']()).toBeFalsy();
      expect(component['editingClientId']()).toBeNull();
    });

    it('should submit edit form successfully', async () => {
      (clientService.update as any) = vi.fn(() => of(mockClients[0]));
      (clientService.getAll as any) = vi.fn(() => of(mockClients));

      component['openEditModal'](mockClients[0]);
      component['editForm'].patchValue({
        name: 'Updated Name',
        active: false,
      });

      component['submitEditClient']();
      await new Promise((resolve) => setTimeout(resolve, 100));
      expect(clientService.update).toHaveBeenCalled();
    });
  });

  describe('Delete Modal', () => {
    it('should open delete modal with client data', () => {
      component['openDeleteModal'](mockClients[0]);
      expect(component['showDeleteModal']()).toBeTruthy();
      expect(component['deletingClientId']()).toBe(1);
      expect(component['deletingClientName']()).toBe('John Doe');
    });

    it('should close delete modal', () => {
      component['showDeleteModal'].set(true);
      component['deletingClientId'].set(1);
      component['deletingClientName'].set('John Doe');
      component['closeDeleteModal']();
      expect(component['showDeleteModal']()).toBeFalsy();
      expect(component['deletingClientId']()).toBeNull();
    });

    it('should confirm delete successfully', async () => {
      (clientService.remove as any) = vi.fn(() => of(void 0));
      (clientService.getAll as any) = vi.fn(() => of([mockClients[1]]));

      component['deletingClientId'].set(1);
      component['deletingClientName'].set('John Doe');
      component['confirmDeleteClient']();

      await new Promise((resolve) => setTimeout(resolve, 100));
      expect(clientService.remove).toHaveBeenCalledWith(1);
    });

    it('should not delete if clientId is null', () => {
      component['deletingClientId'].set(null);
      component['confirmDeleteClient']();

      expect(clientService.remove).not.toHaveBeenCalled();
    });
  });

  describe('Form Validations', () => {
    it('should require all fields in create form', () => {
      expect(component['createForm'].get('name')?.hasError('required')).toBeTruthy();
      expect(component['createForm'].get('identification')?.hasError('required')).toBeTruthy();
      expect(component['createForm'].get('address')?.hasError('required')).toBeTruthy();
    });

    it('should validate age range in create form', () => {
      const ageControl = component['createForm'].get('age');
      ageControl?.setValue(-1);
      expect(ageControl?.hasError('min')).toBeTruthy();

      ageControl?.setValue(121);
      expect(ageControl?.hasError('max')).toBeTruthy();

      ageControl?.setValue(30);
      expect(ageControl?.valid).toBeTruthy();
    });

    it('should mark form as touched when submitting invalid form', () => {
      component['submitCreateClient']();

      expect(component['createForm'].touched).toBeTruthy();
    });
  });
});

