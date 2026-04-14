import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-reportes-section',
  imports: [ReactiveFormsModule],
  templateUrl: './reportes-section.component.html',
  styleUrl: './reportes-section.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ReportesSectionComponent {
  protected readonly title = 'Reportes';
  protected readonly searchLabel = 'Buscar reportes';
  protected readonly searchPlaceholder = 'Buscar';
  protected readonly createButtonAriaLabel = 'Crear nuevo reporte';
  protected readonly tableAriaLabel = 'Listado de reportes';
  protected readonly rows: readonly number[] = [1, 2, 3, 4, 5, 6];
  protected readonly searchControl = new FormControl('', { nonNullable: true });
}

