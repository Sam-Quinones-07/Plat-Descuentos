import { useEffect, useState } from "react";
import { api } from "../api";
import { toApiError, toDiscount } from "../api/mappers";
import { Alert, Button, CategoryIcon, Icon } from "../components";
import { type Discount, type DiscountCategory, discountCategories, getDiscountCategory } from "../types";
import { StudentDiscountCard } from "./StudentDiscountCard";
import { StudentShell, type StudentShellProps } from "./StudentShell";

const PAGE_SIZE = 12;

/**
 * HU 2.2.1 Visualizar directorio, HU 2.2.3 Buscar por categoría y HU 2.2.4 Guardar desde la tarjeta.
 * El servidor ya filtra activos y vigentes (HU 2.2.1 esc. 3) y pagina (deuda técnica 8).
 */
export function StudentDiscounts({
  onSelect,
  shell,
}: {
  onSelect: (discount: Discount) => void;
  shell: StudentShellProps;
}) {
  const [category, setCategory] = useState<DiscountCategory | null>(null);
  const [discounts, setDiscounts] = useState<Discount[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    api
      .listDiscounts(category, page, PAGE_SIZE)
      .then((result) => {
        if (cancelled) return;
        const items = result.items.map(toDiscount).filter((d) => Boolean(getDiscountCategory(d.category)));
        setDiscounts((current) => (page === 0 ? items : [...current, ...items]));
        setTotalPages(result.totalPages);
        setTotalItems(result.totalItems);
      })
      .catch((e) => {
        const apiError = toApiError(e);
        if (!cancelled && apiError.status !== 401) setError(apiError.message);
      })
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, [category, page]);

  function selectCategory(next: DiscountCategory | null) {
    setCategory(next);
    setPage(0);
    setDiscounts([]);
  }

  async function toggleSave(discount: Discount) {
    try {
      if (discount.saved) await api.unsaveDiscount(discount.id);
      else await api.saveDiscount(discount.id);
      setDiscounts((list) => list.map((d) => (d.id === discount.id ? { ...d, saved: !d.saved } : d)));
    } catch (e) {
      const apiError = toApiError(e);
      if (apiError.code === "DESCUENTO_YA_GUARDADO") {
        setDiscounts((list) => list.map((d) => (d.id === discount.id ? { ...d, saved: true } : d)));
      } else if (apiError.status !== 401) setError(apiError.message);
    }
  }

  const categoryName = category ? getDiscountCategory(category)?.name : "";
  const firstLoad = loading && discounts.length === 0;

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
        {!firstLoad && totalItems > 0 && (
          <span className="result-count">
            {totalItems} {totalItems === 1 ? "descuento disponible" : "descuentos disponibles"}
          </span>
        )}
      </div>

      <div className="category-filter" role="group" aria-label="Filtrar por categoría">
        <button
          type="button"
          className={`category-chip category-chip--all ${category === null ? "category-chip--selected" : ""}`}
          aria-pressed={category === null}
          onClick={() => selectCategory(null)}
        >
          <CategoryIcon name="all" />
          Todas
        </button>
        {discountCategories.map((item) => (
          <button
            type="button"
            key={item.id}
            className={`category-chip category-chip--${item.id} ${category === item.id ? "category-chip--selected" : ""}`}
            aria-pressed={category === item.id}
            onClick={() => selectCategory(item.id)}
          >
            <CategoryIcon name={item.icon} />
            {item.name}
          </button>
        ))}
      </div>

      {error && <Alert tone="error">{error}</Alert>}

      {firstLoad ? (
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
      ) : discounts.length ? (
        <>
          <div className="student-discount-grid">
            {discounts.map((discount) => (
              <StudentDiscountCard
                discount={discount}
                onSelect={() => onSelect(discount)}
                onToggleSave={() => toggleSave(discount)}
                key={discount.id}
              />
            ))}
          </div>
          {page + 1 < totalPages && (
            <div className="load-more">
              <Button variant="secondary" disabled={loading} onClick={() => setPage((p) => p + 1)}>
                {loading ? <Icon name="spinner" /> : null} Ver más descuentos
              </Button>
            </div>
          )}
        </>
      ) : (
        !error && (
          <div className="admin-section empty-requests">
            <span className="empty-requests__icon">
              <Icon name="tag" />
            </span>
            <p className="empty-requests__title">
              {category ? `No hay descuentos disponibles en ${categoryName}` : "No hay descuentos disponibles"}
            </p>
            <p className="empty-requests__description">
              {category
                ? "Prueba con otra categoría o vuelve a ver todas."
                : "Cuando los comercios aliados publiquen nuevos beneficios, aparecerán en esta sección."}
            </p>
            {category && (
              <Button variant="secondary" onClick={() => selectCategory(null)}>
                Ver todas las categorías
              </Button>
            )}
          </div>
        )
      )}
    </StudentShell>
  );
}
