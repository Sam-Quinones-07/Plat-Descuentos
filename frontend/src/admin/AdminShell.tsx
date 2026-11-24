import { type ReactNode } from "react";
import { BrandMark, Button, Icon, LegalFooter } from "../components";

export function AdminShell({
  children,
  onLogout,
  active = "requests",
}: {
  children: ReactNode;
  onLogout: () => void;
  active?: "requests" | "reviews";
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
          <button
            type="button"
            className={`admin-nav__item ${active === "requests" ? "admin-nav__item--active" : ""}`}
            onClick={() => {
              window.location.hash = "#administracion";
            }}
          >
            <Icon name="shield" />
            <span>Solicitudes de afiliación</span>
          </button>
          <button
            type="button"
            className={`admin-nav__item ${active === "reviews" ? "admin-nav__item--active" : ""}`}
            onClick={() => {
              window.location.hash = "#moderar-resenas";
            }}
          >
            <Icon name="star" />
            <span>Reseñas</span>
          </button>
        </aside>
        <main className="admin-main">
          {children}
          <LegalFooter />
        </main>
      </div>
    </div>
  );
}
