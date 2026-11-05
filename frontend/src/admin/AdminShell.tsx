import { type ReactNode } from "react";
import { BrandMark, Button, Icon } from "../components";

export function AdminShell({
  children,
  onLogout,
}: {
  children: ReactNode;
  onLogout: () => void;
}) {
  return (
    <div className="admin-shell">
      <header className="admin-header">
        <div className="admin-brand">
          <BrandMark className="home__mark" />
          <span className="admin-brand__name">PromoU</span>
        </div>
        <div className="admin-header__actions">
          <span className="role-badge">Administrador</span>
          <Button variant="secondary" onClick={onLogout}>
            Cerrar sesión
          </Button>
        </div>
      </header>
      <div className="admin-layout">
        <aside className="admin-nav" aria-label="Navegación de administración">
          <span className="admin-nav__label">GESTIÓN</span>
          <div className="admin-nav__item admin-nav__item--active">
            <Icon name="shield" />
            <span>Solicitudes de afiliación</span>
          </div>
        </aside>
        <main className="admin-main">{children}</main>
      </div>
    </div>
  );
}
