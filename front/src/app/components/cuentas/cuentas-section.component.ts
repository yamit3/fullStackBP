import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-cuentas-section',
  imports: [ReactiveFormsModule],
  templateUrl: './cuentas-section.component.html',
  styleUrl: './cuentas-section.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CuentasSectionComponent {
  protected readonly title = 'Cuentas';
  protected readonly searchLabel = 'Buscar cuentas';
  protected readonly searchPlaceholder = 'Buscar';
  protected readonly createButtonAriaLabel = 'Crear nueva cuenta';
  protected readonly tableAriaLabel = 'Listado de cuentas';
  protected readonly rows: readonly number[] = [1, 2, 3, 4, 5, 6];
  protected readonly searchControl = new FormControl('', { nonNullable: true });
}

