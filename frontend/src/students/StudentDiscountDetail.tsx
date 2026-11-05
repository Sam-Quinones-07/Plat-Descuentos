import { useEffect, useState } from "react";
import { api } from "../api";
import { toApiError, toDiscount } from "../api/mappers";
import { Button, CategoryIcon, DetailItem, Icon } from "../components";
import { type Discount, formatDiscountDate, getDiscountCategory, normalizeWebsite } from "../types";
import { StudentShell, type StudentShellProps } from "./StudentShell";

type State =
  | { kind: "loading" }
  | { kind: "ready"; discount: Discount }
  | { kind: "unavailable"; message: string }
  | { kind: "error"; message: string };

/**
 * HU 2.2.2 Consultar detalle de un descuento. Se consulta al servidor cada vez, así un descuento que se
 * desactivó o venció después de cargar el directorio se informa como no disponible (esc. 2).
 * (Guardar, copiar código y reseñas son de Releases 2 y 3.)
 */
export function StudentDiscountDetail({
  discountId,
  onBack,
  onOpenId,
  shell,
}: {
  discountId: string;
  onBack: () => void;
  onOpenId: () => void;
  shell: StudentShellProps;
}) {
  const [state, setState] = useState<State>({ kind: "loading" });

  useEffect(() => {
    let cancelled = false;
    setState({ kind: "loading" });
    api
      .discountDetail(discountId)
      .then((value) => {
        if (!cancelled) setState({ kind: "ready", discount: toDiscount(value) });
      })
      .catch((error) => {
        if (cancelled) return;
        const apiError = toApiError(error);
        if (apiError.status === 410 || apiError.status === 404 || apiError.status === 400) {
          setState({ kind: "unavailable", message: "Este descuento ya no está disponible." });
        } else if (apiError.status !== 401) {
          setState({ kind: "error", message: apiError.message });
        }
      });
    return () => {
      cancelled = true;
    };
  }, [discountId]);

  return (
    <StudentShell {...shell}>
      <button type="button" className="back-button" onClick={onBack}>
        <span aria-hidden="true">←</span> Volver a descuentos
      </button>

      {state.kind === "loading" && (
        <div className="student-detail__main skeleton-card detail-skeleton" aria-busy="true">
          <span className="skeleton skeleton--short" />
          <span className="skeleton skeleton--title" />
          <span className="skeleton" />
          <span className="skeleton" />
        </div>
      )}

      {(state.kind === "unavailable" || state.kind === "error") && (
        <div className="admin-section empty-requests">
          <span className="empty-requests__icon empty-requests__icon--warning">
            <Icon name="warning" />
          </span>
          <p className="empty-requests__title">{state.message}</p>
          <p className="empty-requests__description">
            {state.kind === "unavailable"
              ? "Puede que el comercio lo haya desactivado o que su vigencia haya terminado."
              : "Inténtalo de nuevo en unos segundos."}
          </p>
          <Button variant="secondary" onClick={onBack}>
            Ver descuentos disponibles
          </Button>
        </div>
      )}

      {state.kind === "ready" && <DetailBody discount={state.discount} onOpenId={onOpenId} />}
    </StudentShell>
  );
}

function DetailBody({ discount, onOpenId }: { discount: Discount; onOpenId: () => void }) {
  const category = getDiscountCategory(discount.category);
  return (
    <div className="student-detail">
      <section className="student-detail__main">
        <div className="student-detail__heading">
          <div>
            <span className="eyebrow">{discount.businessName}</span>
            <p className="admin-page-title">{discount.benefit}</p>
          </div>
          {category && (
            <span className={`discount-category category-chip--${category.id}`}>
              <CategoryIcon name={category.icon} />
              {category.name}
            </span>
          )}
        </div>
        <div className="student-detail__section">
          <span className="detail-item__label">CONDICIONES</span>
          <p>{discount.conditions}</p>
        </div>
        <div className="detail-grid">
          <DetailItem label="Ubicación" value={discount.location} />
          <DetailItem
            label="Vigencia"
            value={`${formatDiscountDate(discount.startDate)} — ${formatDiscountDate(discount.endDate)}`}
          />
        </div>
        {discount.website && (
          <div className="student-detail__section">
            <span className="detail-item__label">SITIO WEB</span>
            <a className="website-link" href={normalizeWebsite(discount.website)} target="_blank" rel="noreferrer">
              {discount.website.replace(/^https?:\/\//i, "")} <span aria-hidden="true">↗</span>
            </a>
          </div>
        )}
      </section>
      <aside className="code-card">
        <span className="status-badge status-badge--approved">
          <span className="status-badge__dot" />
          Disponible
        </span>
        <p className="code-card__title">¿Cómo lo uso?</p>
        <p className="code-card__description">
          Acércate al comercio y presenta tu identificación digital con código QR para acceder al beneficio.
        </p>
        <Button onClick={onOpenId}>
          <Icon name="qr" /> Mostrar mi identificación
        </Button>
      </aside>
    </div>
  );
}
