import { type ReactNode } from "react";
import { BrandMark, Button, Icon } from "../components";

export function BusinessShell({
  businessName,
  children,
  onLogout,
  active = "discounts",
  onNew,
}: {
  businessName: string;
  children: ReactNode;
  onLogout: () => void;
  active?: "discounts" | "new";
  onNew?: () => void;
}) {
  return (
    <div className="admin-shell">
      <header className="admin-header">
        <div className="admin-brand">
          <BrandMark className="home__mark" />
          <span className="admin-brand__name">PromoU</span>
        </div>
        <div className="admin-header__actions">
          {businessName && <span className="admin-brand__name">{businessName}</span>}
          <span className="role-badge">Establecimiento</span>
          <Button variant="secondary" onClick={onLogout}>
            Cerrar sesión
          </Button>
        </div>
      </header>
      <div className="admin-layout">
        <aside className="admin-nav" aria-label="Navegación del establecimiento">
          <span className="admin-nav__label">GESTIÓN</span>
          <button
            type="button"
            className={`admin-nav__item ${active === "discounts" ? "admin-nav__item--active" : ""}`}
            onClick={() => { window.location.hash = "#mis-descuentos"; }}
          >
            <Icon name="tag" />
            <span>Mis descuentos</span>
          </button>
          <button
            type="button"
            className={`admin-nav__item ${active === "new" ? "admin-nav__item--active" : ""}`}
            onClick={onNew}
          >
            <span aria-hidden="true" style={{ fontSize: "1.1rem", lineHeight: 1 }}>+</span>
            <span>Publicar descuento</span>
          </button>
        </aside>
        <main className="admin-main">{children}</main>
      </div>
    </div>
  );
}
