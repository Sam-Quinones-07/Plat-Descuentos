import { type ReactNode } from "react";
import { BrandMark } from "./BrandMark";
import { Icon } from "./Icon";

export function AuthLayout({
  eyebrow,
  title,
  description,
  children,
  compact = false,
  wide = false,
  audience = "student",
}: {
  eyebrow: string;
  title: string;
  description: string;
  children: ReactNode;
  compact?: boolean;
  wide?: boolean;
  audience?: "student" | "business";
}) {
  return (
    <main className="auth-layout">
      <section
        className="brand-panel"
        aria-label={audience === "student" ? "Acceso para estudiantes" : "Acceso para establecimientos"}
      >
        <BrandMark className="brand-mark" />
        <div className="brand-copy">
          <span className="brand-kicker">
            {audience === "student"
              ? "EXCLUSIVO PARA ESTUDIANTES"
              : "COMERCIOS ALIADOS"}
          </span>
          <p className="brand-title">
            {audience === "student" ? (
              <>
                Tu etapa universitaria,
                <br />
                mejor aprovechada.
              </>
            ) : (
              <>
                Dale a los estudiantes una
                <br />
                razón más para elegirte.
              </>
            )}
          </p>
          <p className="brand-description">
            {audience === "student"
              ? "Regístrate con tu correo institucional y accede a descuentos exclusivos en comercios aliados."
              : "Solicita la afiliación de tu establecimiento. La información será revisada antes de tomar una decisión."}
          </p>
        </div>
        <div className="trust-note">
          <Icon name="shield" />
          <span>
            {audience === "student"
              ? "Acceso exclusivo con correo institucional"
              : "Afiliación segura sujeta a revisión"}
          </span>
        </div>
      </section>

      <section className="auth-panel">
        <div
          className={`auth-card ${compact ? "auth-card--compact" : ""} ${wide ? "auth-card--wide" : ""}`}
        >
          <BrandMark className="mobile-mark" />
          <span className="eyebrow">{eyebrow}</span>
          <p className="page-title">{title}</p>
          <p className="page-description">{description}</p>
          {children}
        </div>
      </section>
    </main>
  );
}
