import { Icon } from "../components";
import { type Discount, getDiscountCategory } from "../types";
import { StudentDiscountCard } from "./StudentDiscountCard";
import { StudentShell, type StudentShellProps } from "./StudentShell";

/** HU 2.2.1 Visualizar directorio de descuentos. (El filtro por categoría es de Release 2.) */
export function StudentDiscounts({
  discounts,
  loading,
  onSelect,
  shell,
}: {
  discounts: Discount[];
  loading: boolean;
  onSelect: (discount: Discount) => void;
  shell: StudentShellProps;
}) {
  // El servidor ya filtra activos y vigentes (esc. 3); aquí solo se descartan categorías desconocidas.
  const available = discounts.filter(
    (discount) => discount.status === "active" && Boolean(getDiscountCategory(discount.category)),
  );

  return (
    <StudentShell {...shell}>
      <div className="discount-page-heading">
        <div>
          <span className="eyebrow">DIRECTORIO</span>
          <p className="admin-page-title">Descuentos para ti</p>
          <p className="page-description">
            Beneficios vigentes de los comercios aliados. Presenta tu identificación digital al usarlos.
          </p>
        </div>
        {!loading && available.length > 0 && (
          <span className="result-count">
            {available.length} {available.length === 1 ? "descuento disponible" : "descuentos disponibles"}
          </span>
        )}
      </div>

      {loading ? (
        <div className="student-discount-grid" aria-busy="true" aria-label="Cargando descuentos">
          {[0, 1, 2].map((item) => (
            <div className="student-discount-card skeleton-card" key={item}>
              <span className="skeleton skeleton--short" />
              <span className="skeleton skeleton--title" />
              <span className="skeleton" />
              <span className="skeleton skeleton--short" />
            </div>
          ))}
        </div>
      ) : available.length ? (
        <div className="student-discount-grid">
          {available.map((discount) => (
            <StudentDiscountCard discount={discount} onSelect={() => onSelect(discount)} key={discount.id} />
          ))}
        </div>
      ) : (
        <div className="admin-section empty-requests">
          <span className="empty-requests__icon">
            <Icon name="tag" />
          </span>
          <p className="empty-requests__title">No hay descuentos disponibles</p>
          <p className="empty-requests__description">
            Cuando los comercios aliados publiquen nuevos beneficios, aparecerán en esta sección.
          </p>
        </div>
      )}
    </StudentShell>
  );
}
