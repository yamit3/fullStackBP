import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { ClientesSectionComponent } from './components/clientes/clientes-section.component';
import { CuentasSectionComponent } from './components/cuentas/cuentas-section.component';
import { MovimientosSectionComponent } from './components/movimientos/movimientos-section.component';
import { ReportesSectionComponent } from './components/reportes/reportes-section.component';

type MenuSection = 'Clientes' | 'Cuentas' | 'Movimientos' | 'Reportes';

@Component({
  selector: 'app-root',
  imports: [
    ClientesSectionComponent,
    CuentasSectionComponent,
    MovimientosSectionComponent,
    ReportesSectionComponent,
  ],
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

  protected setActiveSection(section: MenuSection): void {
    this.activeSection.set(section);
  }
}
