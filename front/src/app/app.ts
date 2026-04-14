import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import { ReactiveFormsModule, FormControl } from '@angular/forms';

type MenuSection = 'Clientes' | 'Cuentas' | 'Movimientos' | 'Reportes';

@Component({
  selector: 'app-root',
  imports: [ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  protected readonly menuItems: readonly MenuSection[] = [
    'Clientes',
    'Cuentas',
    'Movimientos',
    'Reportes',
  ];
  protected readonly activeSection = signal<MenuSection>('Clientes');
  protected readonly searchControl = new FormControl('', { nonNullable: true });
  protected readonly pageTitle = computed(() => this.activeSection());

  protected setActiveSection(section: MenuSection): void {
    this.activeSection.set(section);
  }
}
