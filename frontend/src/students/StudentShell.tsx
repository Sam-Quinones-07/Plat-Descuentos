import { type ReactNode } from "react";
import { BrandMark, Button, Icon } from "../components";

export type StudentShellProps = {
  studentName: string;
  onLogout: () => void;
};

/** Marco de las pantallas del estudiante. "Cerrar sesión" siempre visible (HU 1.1.4 esc. 1). */
export function StudentShell({
  children,
  active = "discounts",
  studentName,
  onLogout,
}: StudentShellProps & {
  children: ReactNode;
  active?: "discounts" | "profile";
}) {
  const initials = studentName
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("");

  return (
    <div className="admin-shell">
      <header className="admin-header">
        <div className="admin-brand">
          <BrandMark className="home__mark" />
          <span className="admin-brand__name">PromoU</span>
        </div>
        <div className="admin-header__actions">
          <button
            type="button"
            className={`header-user ${active === "profile" ? "header-user--active" : ""}`}
            onClick={() => {
              window.location.hash = "#mi-perfil";
            }}
            aria-label="Ir a mi identificación digital"
          >
            <span className="header-user__avatar" aria-hidden="true">
              {initials || <Icon name="user" />}
            </span>
            <span className="header-user__text">
              <span className="header-user__name">{studentName}</span>
              <span className="header-user__role">Estudiante</span>
            </span>
          </button>
          <Button variant="secondary" onClick={onLogout}>
            Cerrar sesión
          </Button>
        </div>
      </header>
      <div className="admin-layout">
        <aside className="admin-nav student-nav" aria-label="Navegación del estudiante">
          <span className="admin-nav__label">EXPLORAR</span>
          <button
            type="button"
            className={`admin-nav__item ${active === "discounts" ? "admin-nav__item--active" : ""}`}
            onClick={() => {
              window.location.hash = "#descuentos";
            }}
          >
            <Icon name="tag" />
            <span>Descuentos</span>
          </button>
          <button
            type="button"
            className={`admin-nav__item ${active === "profile" ? "admin-nav__item--active" : ""}`}
            onClick={() => {
              window.location.hash = "#mi-perfil";
            }}
          >
            <Icon name="qr" />
            <span>Mi identificación</span>
          </button>
        </aside>
        <main className="admin-main">{children}</main>
      </div>
    </div>
  );
}
