import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-movimientos-section',
  imports: [ReactiveFormsModule],
  templateUrl: './movimientos-section.component.html',
  styleUrl: './movimientos-section.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MovimientosSectionComponent {
  protected readonly title = 'Movimientos';
  protected readonly searchLabel = 'Buscar movimientos';
  protected readonly searchPlaceholder = 'Buscar';
  protected readonly createButtonAriaLabel = 'Crear nuevo movimiento';
  protected readonly tableAriaLabel = 'Listado de movimientos';
  protected readonly rows: readonly number[] = [1, 2, 3, 4, 5, 6];
  protected readonly searchControl = new FormControl('', { nonNullable: true });
}

