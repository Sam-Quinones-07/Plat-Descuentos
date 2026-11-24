import { useEffect, useState } from "react";
import { api } from "../api";
import { toApiError, toDiscount } from "../api/mappers";
import { Alert, Button, Icon } from "../components";
import { type Discount } from "../types";
import { StudentDiscountCard } from "./StudentDiscountCard";
import { StudentShell, type StudentShellProps } from "./StudentShell";

/** HU 2.2.4 esc. 2 y 3: descuentos guardados por el estudiante; quitarlos no los saca del directorio. */
export function StudentSaved({
  onSelect,
  goToDirectory,
  shell,
}: {
  onSelect: (discount: Discount) => void;
  goToDirectory: () => void;
  shell: StudentShellProps;
}) {
  const [discounts, setDiscounts] = useState<Discount[] | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    api
      .savedDiscounts()
      .then((list) => !cancelled && setDiscounts(list.map(toDiscount)))
      .catch((e) => {
        const apiError = toApiError(e);
        if (!cancelled && apiError.status !== 401) setError(apiError.message);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function unsave(discount: Discount) {
    try {
      await api.unsaveDiscount(discount.id);
      setDiscounts((list) => (list ?? []).filter((d) => d.id !== discount.id));
    } catch (e) {
      const apiError = toApiError(e);
      if (apiError.status !== 401) setError(apiError.message);
    }
  }

  return (
    <StudentShell {...shell} active="saved">
      <div className="discount-page-heading">
        <div>
          <span className="eyebrow">MIS GUARDADOS</span>
          <p className="admin-page-title">Descuentos guardados</p>
          <p className="page-description">Los beneficios que guardaste para usarlos cuando los necesites.</p>
        </div>
      </div>
      {error && <Alert tone="error">{error}</Alert>}
      {!discounts && !error && (
        <div className="student-discount-grid" aria-busy="true">
          {[0, 1].map((item) => (
            <div className="student-discount-card skeleton-card" key={item}>
              <span className="skeleton skeleton--short" />
              <span className="skeleton skeleton--title" />
              <span className="skeleton" />
            </div>
          ))}
        </div>
      )}
      {discounts && discounts.length > 0 && (
        <div className="student-discount-grid">
          {discounts.map((discount) => (
            <div className="saved-item" key={discount.id}>
              <StudentDiscountCard
                discount={{ ...discount, saved: true }}
                onSelect={() => onSelect(discount)}
                onToggleSave={() => unsave(discount)}
              />
              <span className={`status-badge status-badge--${discount.available ? "approved" : "rejected"}`}>
                <span className="status-badge__dot" />
                {discount.available ? "Guardado · sin usar" : "Ya no está disponible"}
              </span>
            </div>
          ))}
        </div>
      )}
      {discounts && discounts.length === 0 && (
        <div className="admin-section empty-requests">
          <span className="empty-requests__icon">
            <Icon name="bookmark" />
          </span>
          <p className="empty-requests__title">Aún no tienes descuentos guardados</p>
          <p className="empty-requests__description">
            Usa el marcador de cada descuento para guardarlo y encontrarlo aquí.
          </p>
          <Button variant="secondary" onClick={goToDirectory}>
            Explorar descuentos
          </Button>
        </div>
      )}
    </StudentShell>
  );
}
