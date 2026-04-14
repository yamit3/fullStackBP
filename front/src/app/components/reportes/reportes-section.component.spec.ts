import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { of, throwError } from 'rxjs';
import { ReportesSectionComponent } from './reportes-section.component';
import { ClientService } from '../../services/client.service';
import { Client } from '../../interfaces/client.interface';
import { AccountReport, AccountReportItem } from '../../interfaces/account-report.interface';
import { Error as ApiError } from '../../interfaces/error.interface';

describe('ReportesSectionComponent', () => {
  let component: ReportesSectionComponent;
  let fixture: ComponentFixture<ReportesSectionComponent>;

  const mockClients: Client[] = [
    {
      id: 1,
      name: 'John Doe',
      identification: '0102030405',
      address: 'Main St',
      phone: '555-111',
      gender: 'MALE',
      age: 30,
      active: true,
    },
    {
      id: 2,
      name: 'Jane Doe',
      identification: '1112131415',
      address: 'Oak St',
      phone: '555-222',
      gender: 'FEMALE',
      age: 28,
      active: true,
    },
  ];

  const mockAccounts: AccountReportItem[] = [
    {
      number: '100001',
      balance: 1000,
      deposits: 300,
      withdraws: 50,
    },
  ];

  const mockApiError: ApiError = {
    message: 'Service failed',
    status: 500,
  };

  const clientServiceMock = {
    getAll: vi.fn(() => of(mockClients)),
    getReport: vi.fn(() =>
      of({
        accounts: mockAccounts,
        pdf: null,
      } as AccountReport),
    ),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [ReportesSectionComponent],
      providers: [{ provide: ClientService, useValue: clientServiceMock }],
    }).compileComponents();

    fixture = TestBed.createComponent(ReportesSectionComponent);
    component = fixture.componentInstance;
  });

  it('should create and load clients on init', () => {
    fixture.detectChanges();

    expect(component).toBeTruthy();
    expect(clientServiceMock.getAll).toHaveBeenCalled();
    expect(component['clients']).toEqual(mockClients);
  });

  it('should handle load clients error', () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    clientServiceMock.getAll.mockReturnValueOnce(throwError(() => mockApiError));

    fixture.detectChanges();

    expect(clientServiceMock.getAll).toHaveBeenCalled();
    expect(consoleErrorSpy).toHaveBeenCalled();
    consoleErrorSpy.mockRestore();
  });

  it('should not generate report when form is invalid', () => {
    component['generateReport']();

    expect(clientServiceMock.getReport).not.toHaveBeenCalled();
  });

  it('should generate report with pdf=false and map accounts', () => {
    fixture.detectChanges();
    component['reportForm'].setValue({
      client: '0102030405',
      startDate: '2026-01-01',
      endDate: '2026-01-31',
    });

    component['generateReport']();

    expect(clientServiceMock.getReport).toHaveBeenCalledWith({
      identification: '0102030405',
      startDate: new Date('2026-01-01').getTime(),
      endDate: new Date('2026-01-31').getTime(),
      pdf: false,
    });
    expect(component['reportResults']).toEqual(mockAccounts);
    expect(component['showResults']).toBe(true);
  });

  it('should download pdf when response includes base64 data', () => {
    const clickSpy = vi.fn();
    const fakeAnchor = {
      href: '',
      download: '',
      click: clickSpy,
    } as unknown as HTMLAnchorElement;
    const createElementSpy = vi
      .spyOn(document, 'createElement')
      .mockImplementation(() => fakeAnchor as unknown as HTMLElement);

    clientServiceMock.getReport.mockReturnValueOnce(
      of({
        accounts: [],
        pdf: 'JVBERi0xLjQKJ...',
      } as AccountReport),
    );

    component['reportForm'].setValue({
      client: '0102030405',
      startDate: '2026-01-01',
      endDate: '2026-01-31',
    });

    component['downloadPdf']();

    expect(clientServiceMock.getReport).toHaveBeenCalledWith({
      identification: '0102030405',
      startDate: new Date('2026-01-01').getTime(),
      endDate: new Date('2026-01-31').getTime(),
      pdf: true,
    });
    expect(createElementSpy).toHaveBeenCalledWith('a');
    expect(fakeAnchor.href).toContain('data:application/pdf;base64,JVBERi0xLjQKJ...');
    expect(fakeAnchor.download).toContain('reporte-0102030405-');
    expect(clickSpy).toHaveBeenCalled();

    createElementSpy.mockRestore();
  });

  it('should return false for end date validation when dates are empty', () => {
    component['reportForm'].setValue({ client: '', startDate: '', endDate: '' });

    expect(component['isEndDateInvalid']()).toBe(false);
  });

  it('should return true when end date is before start date', () => {
    component['reportForm'].setValue({
      client: '0102030405',
      startDate: '2026-01-31',
      endDate: '2026-01-01',
    });

    expect(component['isEndDateInvalid']()).toBe(true);
  });

  it('should return false when end date is same or after start date', () => {
    component['reportForm'].setValue({
      client: '0102030405',
      startDate: '2026-01-31',
      endDate: '2026-01-31',
    });

    expect(component['isEndDateInvalid']()).toBe(false);
  });
});

