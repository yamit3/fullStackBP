import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ClientService } from '../../services/client.service';
import { Client } from '../../interfaces/client.interface';
import { AccountReportItem } from '../../interfaces/account-report.interface';

@Component({
  selector: 'app-reportes-section',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './reportes-section.component.html',
  styleUrl: './reportes-section.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReportesSectionComponent implements OnInit {
  private readonly clientService = inject(ClientService);
  protected readonly title = 'Reportes';
  protected readonly createButtonAriaLabel = 'Crear nuevo reporte';

  protected clients: Client[] = [];
  protected reportResults: AccountReportItem[] = [];
  protected showResults = false;

  protected reportForm = new FormGroup({
    client: new FormControl<string>('', Validators.required),
    startDate: new FormControl<string>('', Validators.required),
    endDate: new FormControl<string>('', Validators.required),
  });

  ngOnInit(): void {
    this.loadClients();
  }

  private loadClients(): void {
    this.clientService.getAll().subscribe({
      next: (clients) => {
        this.clients = clients;
      },
      error: (error) => {
        console.error('Error loading clients:', error);
      },
    });
  }

  protected generateReport(): void {
    if (!this.reportForm.valid) {
      return;
    }

    const { client, startDate, endDate } = this.reportForm.value;

    if (!client || !startDate || !endDate) {
      return;
    }

    const startDateTimestamp = new Date(startDate).getTime();
    const endDateTimestamp = new Date(endDate).getTime();

    this.clientService.getReport({
      identification: client,
      startDate: startDateTimestamp,
      endDate: endDateTimestamp,
      pdf: false,
    }).subscribe({
      next: (result) => {
        this.reportResults = [...result.accounts];
        this.showResults = true;
      },
      error: (error) => {
        console.error('Error generating report:', error);
      },
    });
  }

  protected downloadPdf(): void {
    if (!this.reportForm.valid) {
      return;
    }

    const { client, startDate, endDate } = this.reportForm.value;

    if (!client || !startDate || !endDate) {
      return;
    }

    const startDateTimestamp = new Date(startDate).getTime();
    const endDateTimestamp = new Date(endDate).getTime();

    this.clientService.getReport({
      identification: client,
      startDate: startDateTimestamp,
      endDate: endDateTimestamp,
      pdf: true,
    }).subscribe({
      next: (result) => {
        if (result.pdf) {
          const link = document.createElement('a');
          link.href = `data:application/pdf;base64,${result.pdf}`;
          link.download = `reporte-${client}-${new Date().getTime()}.pdf`;
          link.click();
        }
      },
      error: (error) => {
        console.error('Error downloading PDF:', error);
      },
    });
  }

  protected isEndDateInvalid(): boolean {
    const startDate = this.reportForm.get('startDate')?.value;
    const endDate = this.reportForm.get('endDate')?.value;

    if (!startDate || !endDate) {
      return false;
    }

    return new Date(endDate) < new Date(startDate);
  }
}
