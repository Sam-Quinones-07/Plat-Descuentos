import { Alert, Button, Icon } from "../components";
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

/** Tablero "Mis descuentos" (HU 2.1.1). Editar, desactivar y reseñas llegan en Releases 2 y 3. */
export function DiscountsDashboard({
  businessName,
  discounts,
  notice,
  noticeTone,
  onNew,
  onLogout,
}: {
  businessName: string;
  discounts: Discount[];
  notice: string;
  noticeTone: "success" | "error";
  onNew: () => void;
  onLogout: () => void;
}) {
  return (
    <BusinessShell businessName={businessName} onLogout={onLogout} active="discounts" onNew={onNew}>
      <div className="discount-page-heading">
        <div>
          <span className="eyebrow">GESTIÓN DE BENEFICIOS</span>
          <p className="admin-page-title">Mis descuentos</p>
          <p className="page-description">
            Publica los descuentos de tu establecimiento para los estudiantes.
          </p>
        </div>
        <Button onClick={onNew}>
          <span aria-hidden="true">+</span> Publicar descuento
        </Button>
      </div>

      {notice && <Alert tone={noticeTone}>{notice}</Alert>}

      {discounts.length ? (
        <div className="discount-list">
          {discounts.map((discount) => {
            const state = publicationState(discount);
            return (
            <article className="discount-card" key={discount.id}>
              <div className="discount-card__top">
                <span className={`status-badge status-badge--${state.tone}`}>
                  <span className="status-badge__dot" />
                  {state.label}
                </span>
                {discount.code && (
                  <span className="discount-code">Código: {discount.code}</span>
                )}
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
                  {formatDiscountDate(discount.startDate)} —{" "}
                  {formatDiscountDate(discount.endDate)}
                </span>
              </div>
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
    </BusinessShell>
  );
}
