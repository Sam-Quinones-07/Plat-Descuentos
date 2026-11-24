import { useState } from "react";
import { Alert, Button, ConfirmDialog, Icon } from "../components";
import { BusinessShell } from "./BusinessShell";
import { type Discount, formatDiscountDate } from "../types";

/** Estado visible para el comercio: publicado en el directorio hoy, programado, vencido o desactivado. */
function publicationState(discount: Discount): { label: string; tone: "approved" | "pending" | "rejected" } {
  if (discount.status !== "active") return { label: "Desactivado", tone: "rejected" };
  const now = new Date();
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
  if (discount.startDate > today) return { label: "Programado", tone: "pending" };
  if (discount.endDate < today) return { label: "Vencido", tone: "rejected" };
  return { label: "Publicado", tone: "approved" };
}

/** Tablero "Mis descuentos": HU 2.1.1 (publicar), 2.1.2 (editar) y 2.1.3 (desactivar con confirmación). */
export function DiscountsDashboard({
  businessName,
  discounts,
  loading,
  notice,
  noticeTone,
  onNew,
  onEdit,
  onDeactivate,
  onLogout,
}: {
  businessName: string;
  discounts: Discount[];
  loading: boolean;
  notice: string;
  noticeTone: "success" | "error";
  onNew: () => void;
  onEdit: (discount: Discount) => void;
  onDeactivate: (discount: Discount) => Promise<void>;
  onLogout: () => void;
}) {
  const [toDeactivate, setToDeactivate] = useState<Discount | null>(null);
  const [busy, setBusy] = useState(false);

  async function confirmDeactivate() {
    if (!toDeactivate) return;
    setBusy(true);
    try {
      await onDeactivate(toDeactivate);
    } finally {
      setBusy(false);
      setToDeactivate(null);
    }
  }

  return (
    <BusinessShell businessName={businessName} onLogout={onLogout} active="discounts" onNew={onNew}>
      <div className="discount-page-heading">
        <div>
          <span className="eyebrow">GESTIÓN DE BENEFICIOS</span>
          <p className="admin-page-title">Mis descuentos</p>
          <p className="page-description">Publica, edita o desactiva los descuentos de tu establecimiento.</p>
        </div>
        <Button onClick={onNew}>
          <span aria-hidden="true">+</span> Publicar descuento
        </Button>
      </div>

      {notice && <Alert tone={noticeTone}>{notice}</Alert>}

      {loading && discounts.length === 0 ? (
        <div className="discount-list" aria-busy="true">
          {[0, 1].map((item) => (
            <div className="discount-card skeleton-card" key={item}>
              <span className="skeleton skeleton--short" />
              <span className="skeleton skeleton--title" />
              <span className="skeleton" />
            </div>
          ))}
        </div>
      ) : discounts.length ? (
        <div className="discount-list">
          {discounts.map((discount) => {
            const state = publicationState(discount);
            const active = discount.status === "active";
            return (
              <article className="discount-card" key={discount.id}>
                <div className="discount-card__top">
                  <span className={`status-badge status-badge--${state.tone}`}>
                    <span className="status-badge__dot" />
                    {state.label}
                  </span>
                  {discount.code && <span className="discount-code">Código: {discount.code}</span>}
                </div>
                <p className="discount-card__benefit">{discount.benefit}</p>
                <p className="discount-card__conditions">{discount.conditions}</p>
                <p className="discount-card__location">
                  <span className="detail-item__label">UBICACIÓN</span>
                  {discount.location}
                </p>
                <div className="discount-card__validity">
                  <span className="detail-item__label">VIGENCIA</span>
                  <span>
                    {formatDiscountDate(discount.startDate)} — {formatDiscountDate(discount.endDate)}
                  </span>
                </div>
                {active && (
                  <div className="discount-card__actions">
                    <Button variant="secondary" onClick={() => onEdit(discount)}>
                      Editar
                    </Button>
                    <Button variant="danger" onClick={() => setToDeactivate(discount)}>
                      Desactivar
                    </Button>
                  </div>
                )}
              </article>
            );
          })}
        </div>
      ) : (
        <div className="admin-section empty-requests">
          <span className="empty-requests__icon">
            <Icon name="tag" />
          </span>
          <p className="empty-requests__title">Aún no tienes descuentos</p>
          <p className="empty-requests__description">
            Los descuentos que publiques aparecerán en esta sección y en el directorio de los estudiantes.
          </p>
          <Button onClick={onNew}>Publicar mi primer descuento</Button>
        </div>
      )}

      {toDeactivate && (
        <ConfirmDialog
          title="¿Desactivar este descuento?"
          description={
            <>
              «{toDeactivate.benefit}» dejará de aparecer en el directorio y los estudiantes ya no podrán usarlo. Esta
              acción no se puede deshacer.
            </>
          }
          confirmLabel="Sí, desactivar"
          danger
          busy={busy}
          onConfirm={confirmDeactivate}
          onCancel={() => setToDeactivate(null)}
        />
      )}
    </BusinessShell>
  );
}
